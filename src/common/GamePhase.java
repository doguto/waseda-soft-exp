package src.common;

public enum GamePhase {
    LOBBY,          // クライアント専用: 未接続・未入室
    WAITING,        // ルーム入室済み、ゲーム開始待ち
    NIGHT,          // 夜フェーズ（役職アクション）
    MORNING,        // サーバー内部: 夜→朝の遷移処理中
    DAY_DISCUSSION, // 昼フェーズ（議論）
    DAY_VOTE,       // 投票フェーズ
    EXECUTE,        // サーバー内部: 処刑→夜の遷移処理中
    GAME_OVER       // ゲーム終了
}
