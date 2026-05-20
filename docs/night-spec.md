# 🌙 Night Phase Specification

## 概要
夜フェーズでは、各役職が行動を行い、その結果を朝フェーズに引き継ぐ。

---

## 初日夜

### 人狼
- 人狼仲間の確認
- 襲撃なし（初日は攻撃しない）

### 占い師
- 占いたいプレイヤーを選択
- 結果（黒 or 白）を取得

---

## 2日目以降の夜

### 人狼
- 襲撃対象を選択
- 対象プレイヤーを殺害

### 占い師
- 占い対象を選択
- 結果（黒 or 白）を取得

### 騎士
- 守るプレイヤーを選択
- 自分は守れない
- 連続で同じ人は守れない

### 霊媒師
- 処刑されたプレイヤーの黒白を知る

### 狂人
- 行動なし

---

## 通信インターフェース

### サーバー → クライアント

- 夜開始  
  `NIGHT_START <day>`

- 行動要求  
  `NIGHT_ACTION_REQUIRED <role>`

- 待機  
  `NIGHT_WAIT`

- 占い結果  
  `NIGHT_SEER_RESULT <targetId> <result>`

- 夜終了  
  `NIGHT_END`

---

### クライアント → サーバー

- 人狼  
  `NIGHT_WOLF_ATTACK <targetId>`

- 占い師  
  `NIGHT_SEER_CHECK <targetId>`

- 騎士  
  `NIGHT_KNIGHT_GUARD <targetId>`

---

## 実際の通信の流れ
Server → NIGHT_START 1

Server → NIGHT_ACTION_REQUIRED SEER
Client → NIGHT_SEER_CHECK player2

Server → NIGHT_WAIT

Server → NIGHT_END


---

## 設計方針

- サーバーはプレイヤーごとに異なるメッセージを送る
- 行動可能なプレイヤーのみ ACTION_REQUIRED を受信
- その他プレイヤーは WAIT 状態

---

## 追加要素（今後）

- 行動時のサウンド
- 人狼チャット / 墓場チャット
- UI

## クラス設計

### NightManager

夜フェーズ全体を管理するクラス。(サーバー側)

主な役割：
- 夜の開始
- プレイヤーの行動を受信
- 行動完了判定
- 結果生成

主なメソッド：
- startNight()
- setWolfTarget()
- setSeerTarget()
- setKnightTarget()
- isComplete()
- finishNight()

### NightResult
夜で起きたこと
↓
まとめる
↓
朝フェーズに渡す

