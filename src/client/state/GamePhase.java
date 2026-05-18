package src.client.state;

public enum GamePhase {
    LOBBY,         // 未接続・未入室
    WAITING,       // ルーム入室済み、ゲーム開始待ち
    NIGHT,         // 夜フェーズ（役職アクション）
    DAY_DISCUSSION,// 昼フェーズ（議論）
    DAY_VOTE,      // 投票フェーズ
    GAME_OVER      // ゲーム終了
}
