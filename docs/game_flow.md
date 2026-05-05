# ゲームフロー ─ Service 呼び出しタイミング一覧

## 1. Service 呼び出しパターン

| 種別 | 説明 | 戻り値 |
|------|------|--------|
| **クライアント起点** | クライアントの Request を受けて即時実行 | Response を送信元に返す |
| **サーバー起点 (broadcast)** | Worker が Queue から取り出して実行 | 全クライアントへ broadcast |

---

## 2. 全 Service 一覧

| Service | 起点 | 発火タイミング |
|---------|------|----------------|
| `CreateRoomService` | クライアント | ルーム作成ボタン押下 |
| `JoinRoomService` | クライアント | ルーム参加ボタン押下 |
| `DeleteRoomService` | クライアント | ルーム削除時 |
| `StartGameService` | クライアント | ゲーム開始ボタン押下 |
| `DistributeRoleService` | **サーバー (broadcast)** | StartGameService 完了後 → Queue |
| `WolfAttackService` | クライアント（人狼のみ） | 夜フェーズに人狼が襲撃対象を選択 |
| `SeerInvestigateService` | クライアント（占い師のみ） | 夜フェーズに占い師が調査対象を選択 |
| `KnightGuardService` | クライアント（騎士のみ） | 夜フェーズに騎士が護衛対象を選択 |
| `AnnounceMorningService` | **サーバー (broadcast)** | 全役職の夜行動完了 → `check(NIGHT_ACTION_SUBMITTED)` → Queue |
| `EndDiscussionService` | クライアント or タイマー | 議論終了ボタン押下 / 議論タイマー切れ |
| `VoteService` | クライアント | 投票フェーズに各プレイヤーが投票 |
| `DistributeVoteResultService` | **サーバー (broadcast)** | 全員投票完了 or 投票タイマー切れ → `check(VOTE_SUBMITTED)` → Queue |
| `ExecuteService` | **サーバー (broadcast)** | DistributeVoteResultService 完了後 → Queue 連鎖 |
| `AnnounceGameOverService` | **サーバー (broadcast)** | ExecuteService で勝利条件成立 → Queue 連鎖 |
| `SendVillageChatService` | クライアント | 昼フェーズに生存プレイヤーがチャット送信 |
| `SendWolfChatService` | クライアント | 夜フェーズに人狼がチャット送信 |
| `SendGraveChatService` | クライアント | 死亡プレイヤーが墓場チャット送信 |

---

## 3. ゲーム全体フロー

```mermaid
flowchart TD
    classDef clientSvc fill:#d4e8ff,stroke:#4a90d9
    classDef serverSvc fill:#ffe8d4,stroke:#d97a4a
    classDef phase fill:#e8f5e9,stroke:#388e3c,font-weight:bold

    LOBBY([ロビー]):::phase

    LOBBY --> CS[CreateRoomService]:::clientSvc
    LOBBY --> JS[JoinRoomService]:::clientSvc
    CS & JS --> WAITING([待機画面]):::phase

    WAITING -->|4人以上| SS[StartGameService]:::clientSvc
    SS --> DR["DistributeRoleService<br/>broadcast: 役職配布"]:::serverSvc
    DR --> NIGHT1([1夜目]):::phase

    NIGHT1 --> SI1["SeerInvestigateService<br/>占い師のみ行動"]:::clientSvc
    SI1 -->|全行動完了| AM1["AnnounceMorningService<br/>broadcast: 朝アナウンス"]:::serverSvc
    AM1 --> DISC1([昼: 議論]):::phase

    DISC1 --> ED["EndDiscussionService<br/>ボタン or タイマー"]:::clientSvc
    ED --> VOTE([投票フェーズ]):::phase

    VOTE --> VS["VoteService<br/>各プレイヤー投票"]:::clientSvc
    VS -->|全員投票 or タイマー切れ| DVR["DistributeVoteResultService<br/>broadcast: 投票結果"]:::serverSvc
    DVR --> EX["ExecuteService<br/>broadcast: 処刑"]:::serverSvc

    EX -->|ゲーム継続| NIGHTN([N夜目]):::phase
    EX -->|勝利条件成立| AGO["AnnounceGameOverService<br/>broadcast: ゲーム終了"]:::serverSvc

    NIGHTN --> WA["WolfAttackService<br/>人狼の襲撃"]:::clientSvc
    NIGHTN --> SIN["SeerInvestigateService<br/>占い師の調査"]:::clientSvc
    NIGHTN --> KG["KnightGuardService<br/>騎士の護衛"]:::clientSvc
    WA & SIN & KG -->|全行動完了| AMN["AnnounceMorningService<br/>broadcast: 朝アナウンス"]:::serverSvc
    AMN --> DISCN([昼: 議論]):::phase
    DISCN --> ED
```

---

## 4. 各フェーズの詳細シーケンス

### 4-1. ルーム管理

```mermaid
sequenceDiagram
    participant C as Client
    participant J as JabberServer
    participant SVC as Service

    C->>J: CreateRoomMessage(roomId)
    J->>SVC: CreateRoomService.call()
    SVC-->>J: CreateRoomResultMessage(success)
    J-->>C: Response

    C->>J: JoinRoomMessage(roomId, playerId)
    J->>SVC: JoinRoomService.call()
    SVC-->>J: JoinRoomResultMessage(success)
    J-->>C: Response
```

---

### 4-2. ゲーム開始 → 役職配布

```mermaid
sequenceDiagram
    participant C  as Client（ホスト）
    participant J  as JabberServer
    participant SS as StartGameService
    participant DR as DistributeRoleService
    participant Q  as BlockingQueue
    participant W  as Worker
    participant All as 全クライアント

    C->>J: StartGameMessage
    J->>SS: call()
    SS->>Q: enqueue(DISTRIBUTE_ROLE)
    SS-->>J: StartGameResponse
    J-->>C: Response

    W->>Q: take()
    Q-->>W: DISTRIBUTE_ROLE
    W->>DR: call()
    DR-->>All: broadcast（役職通知）
    Note over DR,All: 各プレイヤーに個別の役職情報を送信
```

---

### 4-3. 夜フェーズ（初日 ─ 占い師のみ行動）

```mermaid
sequenceDiagram
    participant Seer  as Client（占い師）
    participant Other as Client（人狼 / 村人）
    participant J     as JabberServer
    participant SI    as SeerInvestigateService
    participant GSM   as GameStateManager
    participant Q     as BlockingQueue
    participant AM    as AnnounceMorningService
    participant All   as 全クライアント

    Note over J,All: 初日は人狼の襲撃なし
    Other-->>J: 行動なし（待機）

    Seer->>J: SeerInvestigateMessage(targetId)
    J->>SI: call()
    SI->>GSM: check(NIGHT_ACTION_SUBMITTED)
    Note over GSM: 占い師のみ完了 → 全役職完了と判定
    GSM->>Q: enqueue(ANNOUNCE_MORNING)
    SI-->>J: SeerInvestigateResponse（調査結果は翌朝通知）
    J-->>Seer: Response

    Q-->>AM: take()
    AM-->>All: broadcast（朝アナウンス・死者なし）
```

---

### 4-4. 夜フェーズ（2日目以降 ─ 全役職行動）

```mermaid
sequenceDiagram
    participant Wolf  as Client（人狼）
    participant Seer  as Client（占い師）
    participant Knight as Client（騎士）
    participant J     as JabberServer
    participant WA    as WolfAttackService
    participant SI    as SeerInvestigateService
    participant KG    as KnightGuardService
    participant GSM   as GameStateManager
    participant Q     as BlockingQueue
    participant AM    as AnnounceMorningService
    participant All   as 全クライアント

    par 各役職が並行して行動
        Wolf->>J: WolfAttackMessage(targetId)
        J->>WA: call()
        WA->>GSM: check(NIGHT_ACTION_SUBMITTED)
        WA-->>J: WolfAttackResponse
        J-->>Wolf: Response
    and
        Seer->>J: SeerInvestigateMessage(targetId)
        J->>SI: call()
        SI->>GSM: check(NIGHT_ACTION_SUBMITTED)
        SI-->>J: SeerInvestigateResponse
        J-->>Seer: Response
    and
        Knight->>J: KnightGuardMessage(targetId)
        J->>KG: call()
        KG->>GSM: check(NIGHT_ACTION_SUBMITTED)
        KG-->>J: KnightGuardResponse
        J-->>Knight: Response
    end

    Note over GSM: 3役職すべて完了 → 条件成立
    GSM->>Q: enqueue(ANNOUNCE_MORNING)

    Q-->>AM: take()
    AM-->>All: broadcast（朝アナウンス・死亡者の発表）
    Note over AM,All: 護衛が成功していた場合は死亡なし
```

---

### 4-5. 昼フェーズ ─ 議論 → 投票 → 処刑

```mermaid
sequenceDiagram
    participant C   as Client（生存プレイヤー）
    participant J   as JabberServer
    participant ED  as EndDiscussionService
    participant VS  as VoteService
    participant VR  as VoteRepository
    participant GSM as GameStateManager
    participant Q   as BlockingQueue
    participant W   as Worker
    participant DVR as DistributeVoteResultService
    participant EX  as ExecuteService
    participant AGO as AnnounceGameOverService
    participant All as 全クライアント

    Note over J,All: 議論タイマー開始

    C->>J: EndDiscussionMessage（ボタン or タイマー切れ）
    J->>ED: call()
    Note over ED: AtomicBoolean で二重発火防止
    ED->>Q: enqueue(VOTE_PHASE_START) ※フェーズ遷移
    ED-->>J: EndDiscussionResponse
    J-->>C: Response

    loop 各プレイヤーが投票
        C->>J: VoteMessage(targetId)
        J->>VS: call()
        VS->>VR: save(playerId, targetId)
        VS->>GSM: check(VOTE_SUBMITTED)
        GSM->>VR: allVoted(roomId)?
        alt 全員投票完了 or タイマー切れ
            Note over GSM: AtomicBoolean で二重発火防止
            GSM->>Q: enqueue(DISTRIBUTE_VOTE_RESULT)
        end
        VS-->>J: VoteResponse
        J-->>C: Response
    end

    W->>Q: take() → DISTRIBUTE_VOTE_RESULT
    W->>DVR: call()
    DVR-->>All: broadcast（投票結果・処刑対象）
    DVR->>Q: enqueue(EXECUTE)

    W->>Q: take() → EXECUTE
    W->>EX: call()
    EX-->>All: broadcast（処刑結果）

    alt 勝利条件成立
        EX->>Q: enqueue(ANNOUNCE_GAME_OVER)
        W->>Q: take() → ANNOUNCE_GAME_OVER
        W->>AGO: call()
        AGO-->>All: broadcast（ゲーム終了・勝利陣営）
    else ゲーム継続
        EX->>Q: enqueue(NIGHT_PHASE_START) ※次の夜へ
    end
```

---

### 4-6. チャットサービス（随時呼び出し）

```mermaid
sequenceDiagram
    participant C   as Client
    participant J   as JabberServer
    participant SVC as ChatService
    participant Tgt as 対象クライアント群

    Note over C,Tgt: 3種類のチャットが独立して動作

    C->>J: VillageChatMessage（生存者全員向け）
    J->>SVC: SendVillageChatService.call()
    SVC-->>Tgt: broadcast（生存プレイヤー全員）

    C->>J: WolfChatMessage（人狼のみ向け）
    J->>SVC: SendWolfChatService.call()
    SVC-->>Tgt: broadcast（人狼プレイヤーのみ）

    C->>J: GraveChatMessage（死亡者のみ向け）
    J->>SVC: SendGraveChatService.call()
    SVC-->>Tgt: broadcast（死亡プレイヤーのみ）
```

---

## 5. GameStateManager.check() イベント一覧

| GameEvent | チェック条件 | 成立時に Queue に積む ServiceType |
|-----------|------------|----------------------------------|
| `NIGHT_ACTION_SUBMITTED` | 当該夜に必要な全役職の行動が完了した | `ANNOUNCE_MORNING` |
| `VOTE_SUBMITTED` | 全員投票完了 **または** 投票タイマー切れ | `DISTRIBUTE_VOTE_RESULT` |
| `DISCUSSION_ENDED` | 議論終了ボタン押下 **または** 議論タイマー切れ | （投票フェーズ開始） |

> **二重発火防止**: `AtomicBoolean.compareAndSet(false, true)` により、タイマーとボタン押下が競合しても Queue へは1度だけ積まれますね。

---

## 6. フェーズ遷移 と 発火する Service の対応

```mermaid
stateDiagram-v2
    [*] --> WAITING : CreateRoomService / JoinRoomService

    WAITING --> ROLE_DISTRIBUTE : StartGameService（クライアント起点）
    ROLE_DISTRIBUTE --> NIGHT : DistributeRoleService（broadcast）

    NIGHT --> MORNING : AnnounceMorningService（broadcast）<br/>全夜行動完了 → check(NIGHT_ACTION_SUBMITTED)

    MORNING --> DISCUSSION : AnnounceMorningService 完了後（自動遷移）

    DISCUSSION --> VOTE : EndDiscussionService<br/>ボタン or タイマー

    VOTE --> EXECUTE : DistributeVoteResultService（broadcast）<br/>全票確定 → check(VOTE_SUBMITTED)

    EXECUTE --> NIGHT : ExecuteService（broadcast）<br/>ゲーム継続
    EXECUTE --> GAME_OVER : ExecuteService（broadcast）<br/>勝利条件成立

    GAME_OVER --> [*] : AnnounceGameOverService（broadcast）
```
