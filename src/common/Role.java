package src.common;

public enum Role {
    WOLF,
    VILLAGER,
    SEER,
    KNIGHT,
    CRAZY_VILLAGER,
    MEDIUM;

    public boolean isWolfCamp() {
        return this == WOLF || this == CRAZY_VILLAGER;
    }

    public String displayName() {
        return switch (this) {
            case WOLF -> "人狼";
            case VILLAGER -> "村人";
            case SEER -> "占い師";
            case KNIGHT -> "騎士";
            case CRAZY_VILLAGER -> "狂人";
            case MEDIUM -> "霊媒師";
        };
    }
}
