package src.common;

public enum GamePhase {
    LOBBY,
    WAITING,
    NIGHT,
    MORNING,
    DAY_DISCUSSION,
    DAY_VOTE,
    EXECUTE,
    GAME_OVER;

    public String displayName() {
        return switch (this) {
            case LOBBY -> "ロビー";
            case WAITING -> "WAITING";
            case NIGHT -> "夜";
            case MORNING -> "朝（発表中）";
            case DAY_DISCUSSION -> "昼（議論）";
            case DAY_VOTE -> "投票中";
            case EXECUTE -> "処刑中";
            case GAME_OVER -> "ゲーム終了";
        };
    }
}
