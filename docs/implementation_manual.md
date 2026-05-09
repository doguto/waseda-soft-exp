# 実装マニュアル ─ 人狼ゲームサーバー

## 概要

**基盤（実装済み）**: JabberServer / GameMaster / GameStateManager / Worker / ServiceFactory / 全Repository / 全Messageクラス

**実装対象**: 各 Service クラスの `call()` メソッド（15クラス）

---

## アーキテクチャ早見

```
Client --(JSON)--> JabberServer --(call)--> ClientService --> Repository
                                                           --> GameStateManager.check()
                                                           --> ResultMessage

GameStateManager.check() --(条件成立)--> GameMaster.pushService(ServiceType)
                                                           --> BlockingQueue

Worker.take() --> ServiceFactory.create() --> BroadcastService.call() --> broadcaster.broadcast/sendTo
```

### 2種類のService

| 種別 | 基底 | 戻り値 | 例 |
|------|------|--------|-----|
| クライアント起点 | `BaseService` | `XxxResultMessage` | `VoteService` |
| サーバー起点 | `BaseService + BroadcastService` | なし（broadcast） | `ExecuteService` |

---

## 使えるフィールド（BaseService継承で自動取得）

```java
protected final String roomId;
protected final GameMaster gameMaster;
protected final GameStateManager stateManager;
```

---

## Repository の取得方法

各Serviceの先頭でフィールドとして宣言する：

```java
private final RoomRepository roomRepo = new RoomRepository();
private final PlayerRepository playerRepo = new PlayerRepository();
private final VoteRepository voteRepo = new VoteRepository();
private final NightActionRepository nightActionRepo = new NightActionRepository();
private final ChatRepository chatRepo = new ChatRepository();
```

---

## Repository API 一覧

### RoomRepository
```java
boolean create(String roomId)                      // ルーム作成。重複ならfalse
boolean delete(String roomId)                      // ルーム削除
boolean exists(String roomId)                      // 存在確認
boolean addPlayer(String roomId, Player player)    // プレイヤー追加
List<Player> getPlayers(String roomId)             // 全プレイヤー取得
boolean canStart(String roomId)                    // 4人以上か
```

### PlayerRepository
```java
Optional<Player> findByName(String roomId, String name)
boolean setRole(String roomId, String name, Role role)
boolean kill(String roomId, String name)
List<Player> getAlivePlayers(String roomId)
boolean wolvesWin(String roomId)    // 生存人狼数 >= 生存村人陣営数
boolean villagersWin(String roomId) // 生存人狼が0
```

### VoteRepository
```java
void save(String roomId, String playerName, String targetName)
boolean allVoted(String roomId)
Optional<String> resolveTarget(String roomId)  // 最多票。同票はランダム
void reset(String roomId)
```

### NightActionRepository
```java
void saveWolfAttack(String roomId, String wolfName, String targetName)
boolean allWolvesAttacked(String roomId)
Optional<String> resolveAttack(String roomId)   // 最多票。同票はランダム

void saveSeerTarget(String roomId, String targetName)
Optional<String> getSeerTarget(String roomId)

void saveKnightTarget(String roomId, String targetName)
Optional<String> getKnightTarget(String roomId)

void reset(String roomId)
```

### ChatRepository
```java
void addVillageMessage(String roomId, ChatMessage msg)
void addWolfMessage(String roomId, ChatMessage msg)
void addGraveMessage(String roomId, ChatMessage msg)
```

---

## Broadcaster API

`broadcaster` フィールド（コンストラクタで受け取る）から呼ぶ：

```java
broadcaster.broadcast(roomId, Object message)        // ルーム全員
broadcaster.broadcastAlive(roomId, Object message)   // 生存プレイヤーのみ
broadcaster.broadcastToRole(roomId, Role.WOLF, msg)  // 特定ロールのみ
broadcaster.broadcastDead(roomId, Object message)    // 死亡プレイヤーのみ
broadcaster.sendTo(playerName, Object message)       // 1人のみ
```

---

## 各Service実装ガイド

---

### 1. CreateRoomService

```java
private final RoomRepository roomRepo = new RoomRepository();

public CreateRoomResultMessage call(CreateRoomMessage msg) {
    boolean created = roomRepo.create(msg.roomId);
    if (!created) return new CreateRoomResultMessage(false, "ルームIDが既に存在します");
    roomRepo.addPlayer(msg.roomId, new Player(msg.name));
    stateManager.setPhase(GamePhase.WAITING);
    return new CreateRoomResultMessage(true, "ルームを作成しました");
}
```

**必要なimport**: `src.server.database.entity.Player`, `src.server.game.GamePhase`, `src.server.database.repository.RoomRepository`

---

### 2. JoinRoomService

```java
private final RoomRepository roomRepo = new RoomRepository();

public JoinRoomResultMessage call(JoinRoomMessage msg) {
    if (!roomRepo.exists(msg.roomId)) return new JoinRoomResultMessage(false, "ルームが存在しません");
    boolean added = roomRepo.addPlayer(msg.roomId, new Player(msg.name));
    if (!added) return new JoinRoomResultMessage(false, "参加に失敗しました");
    return new JoinRoomResultMessage(true, "参加しました");
}
```

---

### 3. DeleteRoomService

```java
private final RoomRepository roomRepo = new RoomRepository();

public DeleteRoomResultMessage call(DeleteRoomMessage msg) {
    boolean deleted = roomRepo.delete(msg.roomId);
    return new DeleteRoomResultMessage(deleted);
}
```

---

### 4. StartGameService

```java
private final RoomRepository roomRepo = new RoomRepository();

public StartGameResultMessage call(StartGameMessage msg) {
    if (!roomRepo.canStart(msg.roomId)) {
        return new StartGameResultMessage(false, "4人以上必要です");
    }
    gameMaster.startWorker(broadcaster);  // Workerスレッド起動
    gameMaster.pushService(ServiceType.DISTRIBUTE_ROLE);
    return new StartGameResultMessage(true, "ゲームを開始します");
}
```

**ポイント**: `startWorker(broadcaster)` でWorkerスレッドを起動してから、キューに積む。

---

### 5. DistributeRoleService（BroadcastService）

```java
private final RoomRepository roomRepo = new RoomRepository();
private final PlayerRepository playerRepo = new PlayerRepository();

@Override
public void call() {
    List<Player> players = roomRepo.getPlayers(roomId);
    List<Role> roles = buildRoleList(players.size());
    Collections.shuffle(roles);

    for (int i = 0; i < players.size(); i++) {
        Player p = players.get(i);
        Role role = roles.get(i);
        playerRepo.setRole(roomId, p.name, role);
        broadcaster.sendTo(p.name, new DistributeRoleMessage(role.name()));
    }

    stateManager.setPhase(GamePhase.NIGHT);
    stateManager.incrementNight();  // nightCount = 1
}

private List<Role> buildRoleList(int count) {
    List<Role> roles = new ArrayList<>();
    roles.add(Role.WOLF);
    roles.add(Role.SEER);
    roles.add(Role.KNIGHT);
    while (roles.size() < count) roles.add(Role.VILLAGER);
    return roles;
}
```

**ポイント**: `broadcast()` ではなく `sendTo()` で個別送信。役職が他プレイヤーに漏れない。

---

### 6. WolfAttackService

```java
private final NightActionRepository nightActionRepo = new NightActionRepository();

public WolfAttackResultMessage call(WolfAttackMessage msg) {
    nightActionRepo.saveWolfAttack(msg.roomId, msg.wolfName, msg.targetName);
    stateManager.check(GameEvent.NIGHT_ACTION_SUBMITTED);
    return new WolfAttackResultMessage(true);
}
```

---

### 7. SeerInvestigateService

```java
private final NightActionRepository nightActionRepo = new NightActionRepository();

public SeerInvestigateResultMessage call(SeerInvestigateMessage msg) {
    nightActionRepo.saveSeerTarget(msg.roomId, msg.targetName);
    stateManager.check(GameEvent.NIGHT_ACTION_SUBMITTED);
    return new SeerInvestigateResultMessage(true);
}
```

**ポイント**: 占い結果は**ここでは返さない**。翌朝 `AnnounceMorningService` が `sendTo()` で通知する。

---

### 8. KnightGuardService

```java
private final NightActionRepository nightActionRepo = new NightActionRepository();

public KnightGuardResultMessage call(KnightGuardMessage msg) {
    nightActionRepo.saveKnightTarget(msg.roomId, msg.targetName);
    stateManager.check(GameEvent.NIGHT_ACTION_SUBMITTED);
    return new KnightGuardResultMessage(true);
}
```

---

### 9. AnnounceMorningService（BroadcastService）

```java
private final NightActionRepository nightActionRepo = new NightActionRepository();
private final PlayerRepository playerRepo = new PlayerRepository();

@Override
public void call() {
    stateManager.setPhase(GamePhase.MORNING);

    String deadPlayerName = null;

    if (!stateManager.isFirstNight()) {
        Optional<String> attackedId = nightActionRepo.resolveAttack(roomId);
        Optional<String> guardedId  = nightActionRepo.getKnightTarget(roomId);

        if (attackedId.isPresent()) {
            boolean guarded = guardedId.map(g -> g.equals(attackedId.get())).orElse(false);
            if (!guarded) {
                playerRepo.kill(roomId, attackedId.get());
                deadPlayerName = attackedId.get();
            }
        }
    }

    AnnounceMorningMessage morningMsg = new AnnounceMorningMessage();
    morningMsg.deadPlayerName = deadPlayerName;
    broadcaster.broadcast(roomId, morningMsg);

    nightActionRepo.reset(roomId);

    // 占い結果を占い師のみに個別送信
    nightActionRepo.getSeerTarget(roomId).ifPresent(targetName ->
        playerRepo.findByName(roomId, targetName).ifPresent(target -> {
            boolean isWolf = target.role == Role.WOLF;
            playerRepo.getAlivePlayers(roomId).stream()
                .filter(p -> p.role == Role.SEER)
                .findFirst()
                .ifPresent(seer ->
                    broadcaster.sendTo(seer.name, new SeerResultMessage(target.name, isWolf))
                );
        })
    );

    stateManager.setPhase(GamePhase.DISCUSSION);
    stateManager.resetRoundState();  // AtomicBooleanをリセット
}
```

**ポイント**: `resetRoundState()` はここだけが呼ぶ。次の議論フェーズで `AtomicBoolean` が初期化される。

---

### 10. EndDiscussionService

```java
public EndDiscussionResultMessage call(EndDiscussionMessage msg) {
    stateManager.check(GameEvent.DISCUSSION_ENDED);
    // check()内でdiscussionEnded.compareAndSet(false, true)が実行される
    gameMaster.pushService(ServiceType.VOTE_PHASE_START);
    return new EndDiscussionResultMessage(true);
}
```

**ポイント**: タイマーとボタン押下の競合は `check()` 内の `AtomicBoolean` が制御する。ここでは `check()` を呼ぶだけ。

---

### 11. VotePhaseStartService（BroadcastService）

```java
@Override
public void call() {
    stateManager.setPhase(GamePhase.VOTE);
}
```

---

### 12. VoteService

```java
private final VoteRepository voteRepo = new VoteRepository();

public VoteResultMessage call(VoteMessage msg) {
    voteRepo.save(msg.roomId, msg.playerName, msg.targetName);
    stateManager.check(GameEvent.VOTE_SUBMITTED);
    return new VoteResultMessage(true);
}
```

**ポイント**: 全員投票完了の判定と `DISTRIBUTE_VOTE_RESULT` キューへの積み込みは `check()` が行う。

---

### 13. DistributeVoteResultService（BroadcastService）

```java
private final VoteRepository voteRepo = new VoteRepository();
private final PlayerRepository playerRepo = new PlayerRepository();

@Override
public void call() {
    Optional<String> target = voteRepo.resolveTarget(roomId);
    if (target.isEmpty()) return;

    // 票数Map（String→Integer）を組み立てる
    List<Player> alive = playerRepo.getAlivePlayers(roomId);
    Map<String, Integer> voteCounts = new HashMap<>();
    alive.forEach(p -> voteCounts.put(p.name, 0));
    // voteRepoのresolveTargetは内部でカウント済み—別途countを組み立てる場合はRoomDataを参照

    broadcaster.broadcast(roomId, new DistributeVoteResultMessage(target.get(), voteCounts));
    gameMaster.pushService(ServiceType.EXECUTE);
}
```

**シンプル版**（voteCounts省略）:
```java
broadcaster.broadcast(roomId, new DistributeVoteResultMessage(target.get(), Map.of()));
gameMaster.pushService(ServiceType.EXECUTE);
```

---

### 14. ExecuteService（BroadcastService）

```java
private final VoteRepository voteRepo = new VoteRepository();
private final PlayerRepository playerRepo = new PlayerRepository();

@Override
public void call() {
    Optional<String> targetOpt = voteRepo.resolveTarget(roomId);
    if (targetOpt.isEmpty()) return;

    String targetName = targetOpt.get();
    String roleName = playerRepo.findByName(roomId, targetName)
        .map(p -> p.role.name())
        .orElse("UNKNOWN");

    playerRepo.kill(roomId, targetName);
    broadcaster.broadcast(roomId, new ExecuteMessage(targetName, roleName));
    voteRepo.reset(roomId);

    if (playerRepo.villagersWin(roomId) || playerRepo.wolvesWin(roomId)) {
        gameMaster.pushService(ServiceType.ANNOUNCE_GAME_OVER);
    } else {
        gameMaster.pushService(ServiceType.NIGHT_PHASE_START);
    }
}
```

**ポイント**: 勝利判定後に次のServiceTypeをキューに積む。この連鎖がゲームを進める。

---

### 15. AnnounceGameOverService（BroadcastService）

```java
private final RoomRepository roomRepo = new RoomRepository();
private final PlayerRepository playerRepo = new PlayerRepository();

@Override
public void call() {
    List<Player> all = roomRepo.getPlayers(roomId);
    String winner = playerRepo.villagersWin(roomId) ? "VILLAGER" : "WOLF";

    List<AnnounceGameOverMessage.PlayerResult> results = all.stream()
        .map(p -> new AnnounceGameOverMessage.PlayerResult(p.name, p.role.name()))
        .toList();

    broadcaster.broadcast(roomId, new AnnounceGameOverMessage(winner, results));
}
```

---

### 16. NightPhaseStartService（BroadcastService）

```java
@Override
public void call() {
    nightActionRepo.reset(roomId);  // 前夜のデータをクリア
    stateManager.setPhase(GamePhase.NIGHT);
    stateManager.incrementNight();
}
```

---

## よくある実装ミス

| ミス | 正しい対処 |
|------|-----------|
| `broadcast()` で役職通知 | `sendTo()` で個別送信（役職漏洩防止） |
| `check()` を呼ばずに直接キューに積む | 必ず `check()` 経由（AtomicBoolean制御のため） |
| `resetRoundState()` を複数箇所で呼ぶ | `AnnounceMorningService` のみ |
| `startWorker()` を呼ばずにキューに積む | StartGameService で先に `startWorker()` |
| キューに次のServiceTypeを積み忘れる | BroadcastServiceの末尾は必ず次フェーズを確認 |
| VoteRepository.reset()を忘れる | ExecuteService の末尾で必ず呼ぶ |

---

## フェーズ遷移と担当Service

| フェーズ遷移 | 担当 |
|------------|------|
| `WAITING → NIGHT` | `DistributeRoleService`（`stateManager.setPhase(NIGHT)` + `incrementNight()`） |
| `NIGHT → MORNING` | `AnnounceMorningService`（`setPhase(MORNING)` → `setPhase(DISCUSSION)`） |
| `DISCUSSION → VOTE` | `GameStateManager.check(DISCUSSION_ENDED)` + `VotePhaseStartService` |
| `VOTE → EXECUTE` | `DistributeVoteResultService` → `ExecuteService`（Queue連鎖） |
| `EXECUTE → NIGHT` | `ExecuteService` → `NightPhaseStartService`（Queue連鎖） |
| `EXECUTE → GAME_OVER` | `ExecuteService` → `AnnounceGameOverService`（Queue連鎖） |

---

## 実装確認チェックリスト

- [ ] CreateRoomService: ルーム作成・プレイヤー登録・フェーズ初期化
- [ ] JoinRoomService: ルーム存在確認・プレイヤー追加
- [ ] DeleteRoomService: ルーム削除
- [ ] StartGameService: 人数確認・Worker起動・DistributeRole積む
- [ ] DistributeRoleService: ロールシャッフル・sendTo個別送信・フェーズNIGHT
- [ ] WolfAttackService: saveWolfAttack → check(NIGHT_ACTION_SUBMITTED)
- [ ] SeerInvestigateService: saveSeerTarget → check(NIGHT_ACTION_SUBMITTED)
- [ ] KnightGuardService: saveKnightTarget → check(NIGHT_ACTION_SUBMITTED)
- [ ] AnnounceMorningService: 護衛照合・kill・broadcast・sendTo占い師・resetRoundState
- [ ] EndDiscussionService: check(DISCUSSION_ENDED) → pushService(VOTE_PHASE_START)
- [ ] VotePhaseStartService: setPhase(VOTE)
- [ ] VoteService: save → check(VOTE_SUBMITTED)
- [ ] DistributeVoteResultService: resolveTarget → broadcast → pushService(EXECUTE)
- [ ] ExecuteService: kill → broadcast → 勝利判定 → 次フェーズ積む
- [ ] AnnounceGameOverService: 勝利陣営・全役職をbroadcast
- [ ] NightPhaseStartService: reset夜行動 → setPhase(NIGHT) → incrementNight
