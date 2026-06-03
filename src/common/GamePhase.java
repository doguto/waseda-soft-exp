package src.common;

public enum GamePhase {
    LOBBY,          // クライアント専用: 未接続・未入室
    WAITING,        // ルーム入室済み、ゲーム開始待ち
    NIGHT,          // 夜フェーズ（役職アクション）
    MORNING,        // サーバー内部: 夜→朝の遷移処理中
    DAY_DISCUSSION, // 昼フェーズ（議論）
    DAY_VOTE,       // 投票フェーズ
    EXECUTE,        // サーバー内部: 処刑→夜の遷移処理中
    GAME_OVER;      // ゲーム終了

    public String displayName() {
        return switch (this) {
            case LOBBY          -> "ロビー";
            case WAITING        -> "待機中";
            case NIGHT          -> "夜";
            case MORNING        -> "朝（発表中）";
            case DAY_DISCUSSION -> "昼（議論）";
            case DAY_VOTE       -> "投票中";
            case EXECUTE        -> "処刑中";
            case GAME_OVER      -> "ゲーム終了";
        };
    }
}
