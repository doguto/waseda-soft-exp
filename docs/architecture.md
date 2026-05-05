# プロジェクト構成

## server

サーバー側のコードを格納するディレクトリです.

`JabberServer` でクライアントからの接続を受け付け、適切な Service クラスを call します。
各 Service クラスは Request 引数を受け取って Request に応じた処理を行い、Response を返します。

```java
public class CreateRoomService {
    public CreateRoomResultMessage call(CreateRoomMessage message) {
        boolean success = roomRepository.create(message.roomId);
        return new CreateRoomResultMessage(success);
    }
}
```

```java
public class CreateRoomMessage {
    public String roomId;
}
```

```java
public class CreateRoomResultMessage {
    public boolean success;
}
```

---

## ゲームシステム設計

### アーキテクチャ全体図

```mermaid
flowchart TD
    Client([Client])
    JabberServer[JabberServer]
    Service[Service]
    Repository[XxxRepository]
    GSM[GameStateManager]
    GameMaster[GameMaster]
    Queue[BlockingQueue]
    Worker[Worker]
    Factory[ServiceFactory]
    PushService[Service broadcast]
    Clients([全クライアント])

    Client -->|"① Request"| JabberServer
    JabberServer -->|"② call"| Service
    Service -->|"③ CRUD"| Repository
    Service -->|"④ check(event)"| GSM
    GSM -->|"⑤ 条件判定のため参照"| Repository
    GSM -->|"⑥ 条件成立 pushService()"| GameMaster
    GameMaster -->|"⑦ enqueue"| Queue
    Service -->|"⑧ Response"| Client
    Queue -->|"⑨ take()"| Worker
    Worker -->|"⑩ create(type)"| Factory
    Factory --> PushService
    PushService -->|"⑪ broadcast"| Clients
```

### 方針

**クライアントリクエスト → 即時 Service 呼び出し**
- `Service.call(message) → ResultMessage` をそのまま使う
- Service 内で Repository を通じてデータを CRUD し、最後に `GameStateManager.check(event)` を呼ぶ

**GameStateManager.check()**
- `currentPhase` と渡された `GameEvent` をもとに、Repository からデータを読んで条件を判定する
- 条件成立なら `GameMaster.pushService(ServiceType.Xxx)` でエンキューのみ行う
- データは自分でキャッシュせず、常に Repository から読む

**サーバー起点イベント → Queue 経由**
- Worker ループが Queue から取り出し、`ServiceFactory` 経由で Service を実行して全クライアントに broadcast する

**二重発火防止（タイマー + 条件の競合）**
- 「全員投票完了 OR 投票タイマー切れ」のような競合には `AtomicBoolean` で対応する

```java
private final AtomicBoolean voteResolved = new AtomicBoolean(false);

if (voteResolved.compareAndSet(false, true)) {
    gameMaster.pushService(ServiceType.DISTRIBUTE_VOTE_RESULT);
}
```

### 主要クラス

| クラス | 責務 |
|--------|------|
| `JabberServer` | クライアント接続の受付・即時 Service の呼び出し・broadcast の実装 |
| `Service`（各実装） | Repository で CRUD → `GameStateManager.check()` → Response を返す |
| `XxxRepository` | データの CRUD のみ。ゲームロジックを持たない |
| `GameStateManager` | `currentPhase` の保持・`check(event)` での条件判定・`pushService()` の呼び出し |
| `GameMaster` | `roomId` / `players` などの設定保持・Queue と Worker の管理・`pushService()` の提供 |
| `Worker`（スレッド） | Queue を監視し、ServiceFactory 経由で Service を実行 |
| `ServiceFactory` | ServiceType → Service インスタンスの生成 |

### シーケンス図（投票フローの例）

```mermaid
sequenceDiagram
    participant C  as Client
    participant J  as JabberServer
    participant VS as VoteService
    participant VR as VoteRepository
    participant GSM as GameStateManager
    participant GM as GameMaster
    participant Q  as BlockingQueue
    participant W  as Worker
    participant DS as DistributeVoteResultService

    C->>J: VoteMessage
    J->>VS: call(message)
    VS->>VR: save(playerId, targetId)
    VS->>GSM: check(VOTE_SUBMITTED)
    GSM->>VR: allVoted(roomId)?
    VR-->>GSM: true
    GSM->>GM: pushService(DISTRIBUTE_VOTE_RESULT)
    GM->>Q: enqueue
    VS-->>J: VoteResponse
    J-->>C: Response

    W->>Q: take()
    Q-->>W: DISTRIBUTE_VOTE_RESULT
    W->>DS: call()
    DS-->>C: broadcast（投票結果）
```

### クラス図

```mermaid
classDiagram
    class GameMaster {
        -String roomId
        -List~Player~ players
        -BlockingQueue~ServiceType~ queue
        -GameStateManager stateManager
        +pushService(ServiceType) void
        +startWorker() void
    }

    class GameStateManager {
        -GamePhase currentPhase
        -GameMaster gameMaster
        +check(GameEvent) void
    }

    class Service {
        <<interface>>
        +call(Object payload) void
    }

    class ServiceFactory {
        +create(ServiceType, GameMaster) Service
    }

    class Worker {
        -BlockingQueue~ServiceType~ queue
        +run() void
    }

    class JabberServer {
        +broadcast(GameEvent event) void
        +sendTo(String playerId, GameEvent event) void
    }

    class VoteService {
        -VoteRepository voteRepository
        -GameStateManager stateManager
        +call(Object payload) void
    }

    class VoteRepository {
        +save(String roomId, String playerId, String targetId) void
        +allVoted(String roomId) boolean
    }

    GameMaster "1" --> "1" GameStateManager
    GameMaster "1" --> "1" Worker
    Worker --> ServiceFactory : create(type)
    ServiceFactory --> Service
    Service <|.. VoteService
    VoteService --> VoteRepository
    VoteService --> GameStateManager : check(event)
    GameStateManager --> GameMaster : pushService()
    GameStateManager --> VoteRepository : 条件判定のため参照
```

### フェーズ遷移図

```mermaid
stateDiagram-v2
    [*] --> WAITING : ゲーム開始ボタン押下

    WAITING --> NIGHT : ロール配布完了

    NIGHT --> MORNING : 全役職の夜行動完了

    MORNING --> DISCUSSION : 朝のアナウンス完了

    DISCUSSION --> VOTE : 議論終了ボタン OR タイマー切れ

    VOTE --> EXECUTE : 全員投票完了 OR 投票タイマー切れ

    EXECUTE --> NIGHT : ゲーム継続
    EXECUTE --> GAME_OVER : 勝利条件成立

    GAME_OVER --> [*]
```

### フェーズと発火条件

| イベント | 発火条件 | 方式 |
|----------|----------|------|
| 夜フェーズ終了 → 朝へ | 全役職の夜行動完了 | check(NIGHT_ACTION_SUBMITTED) |
| 投票集計 | 全員投票完了 OR 投票タイマー切れ | check(VOTE_SUBMITTED) + AtomicBoolean |
| 議論終了 → 投票へ | ボタン押下 OR 議論タイマー切れ | check(DISCUSSION_ENDED) + AtomicBoolean |
| 処刑 → 勝利判定 | 投票集計完了後 | Queue 連鎖 |
| ゲーム終了 | 勝利条件成立 | Queue 連鎖 |
