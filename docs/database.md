# GameDatabase 設計

## 概要

永続化不要のため、全ゲームデータはメモリ上の `GameDatabase` シングルトンで管理する。
Service は直接 `GameDatabase` にアクセスせず、必ず Repository 経由でアクセスする。

```
Service → Repository → GameDatabase (RoomData)
```

---

## クラス構成

```
src/server/database/
├── GameDatabase.java          ← シングルトン。roomId -> RoomData のマップを保持
├── RoomData.java              ← 部屋ごとの全データ
├── entity/
│   ├── Role.java              ← WOLF / VILLAGER / SEER / KNIGHT / CRAZY_VILLAGER / PLIEST
│   ├── Player.java            ← id, name, role, alive
│   └── ChatMessage.java       ← senderId, senderName, text
└── repository/
    ├── RoomRepository.java         ← ルーム作成・削除・参加
    ├── PlayerRepository.java       ← 役職配布・死亡・勝利判定
    ├── VoteRepository.java         ← 投票登録・集計
    ├── NightActionRepository.java  ← 夜行動（人狼・占い師・騎士）
    └── ChatRepository.java         ← チャットメッセージ
```

---

## RoomData フィールド一覧

| フィールド | 型 | 用途 | リセットタイミング |
|---|---|---|---|
| `players` | `List<Player>` | プレイヤー一覧（役職・生死含む） | ゲーム終了時 |
| `votes` | `Map<String,String>` | playerId → targetId | `ExecuteService` 後 |
| `wolfAttacks` | `Map<String,String>` | wolfId → targetId | `AnnounceMorningService` 後 |
| `knightTarget` | `String` | 騎士の護衛対象 | `AnnounceMorningService` 後 |
| `seerTarget` | `String` | 占い師の調査対象 | `AnnounceMorningService` 後 |
| `executedPlayerId` | `String` | 処刑されたプレイヤーID（霊媒師通知用） | 次の `ExecuteService` 前 |
| `villageChat` | `List<ChatMessage>` | 全体チャット履歴 | ゲーム終了時 |
| `wolfChat` | `List<ChatMessage>` | 人狼チャット履歴 | ゲーム終了時 |
| `graveChat` | `List<ChatMessage>` | 墓場チャット履歴 | ゲーム終了時 |

---

## Repository 責務

### RoomRepository
- `create(roomId)` / `delete(roomId)` / `exists(roomId)`
- `addPlayer(roomId, player)` / `getPlayers(roomId)`
- `canStart(roomId)` ← 4人以上か判定

### PlayerRepository
- `findById(roomId, playerId)`
- `setRole(roomId, playerId, role)` ← DistributeRoleService が使用
- `kill(roomId, playerId)` ← ExecuteService / AnnounceMorningService が使用
- `getAlivePlayers(roomId)`
- `wolvesWin(roomId)` ← 生存人狼数 >= 生存村人陣営数
- `villagersWin(roomId)` ← 生存人狼が0

### VoteRepository
- `save(roomId, playerId, targetId)`
- `allVoted(roomId)` ← GameStateManager.check() で使用
- `resolveTarget(roomId)` ← 最多票。同票はランダム
- `reset(roomId)`

### NightActionRepository
- `saveWolfAttack(roomId, wolfId, targetId)`
- `allWolvesAttacked(roomId)` ← GameStateManager.check() で使用
- `resolveAttack(roomId)` ← 最多票。同票はランダム
- `saveSeerTarget(roomId, targetId)` / `getSeerTarget(roomId)`
- `saveKnightTarget(roomId, targetId)` / `getKnightTarget(roomId)`
- `reset(roomId)`

### ChatRepository
- `addVillageMessage` / `addWolfMessage` / `addGraveMessage`
- `getVillageMessages` / `getWolfMessages` / `getGraveMessages`

---

## 設計メモ

- **seerTarget は翌朝通知のため DB に保持が必要**。`SeerInvestigateService`（夜）→ `AnnounceMorningService`（朝）をまたぐため。
- **CRAZY_VILLAGER（狂人）の占い判定**は `SeerInvestigateService` 内で `role != Role.WOLF` と判定するだけで対応できる（DB への影響なし）。
- **PLIEST（霊媒師）の通知**は `ExecuteService` が `executedPlayerId` を DB に書き込み、続く broadcast Service が参照する。
- **同票ランダム**は `VoteRepository.resolveTarget()` と `NightActionRepository.resolveAttack()` の両方で `Collections.max` + `Random` で実装。
