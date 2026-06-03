package src.common;

public enum Role {
    WOLF,
    VILLAGER,
    SEER,
    KNIGHT,
    CRAZY_VILLAGER,
    MEDIUM;

    /**
     * 人狼陣営に属するか。
     * 人狼に加え、狂人（CRAZY_VILLAGER）も人狼陣営として扱う
     * （人狼陣営の勝利時には狂人も勝者となる）。
     * ただし勝利条件の頭数計算上は「人狼ではない人間」として数える点に注意。
     */
    public boolean isWolfCamp() {
        return this == WOLF || this == CRAZY_VILLAGER;
    }
}
