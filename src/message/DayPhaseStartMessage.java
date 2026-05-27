package src.message;

public class DayPhaseStartMessage {
    public static final String MessageType = "day_phase_start";
    public String message_type = MessageType;
    public int votesFor;
    public int aliveCount;
    public int need;

    public DayPhaseStartMessage() {}

    public DayPhaseStartMessage(int votesFor, int aliveCount, int need) {
        this.votesFor = votesFor;
        this.aliveCount = aliveCount;
        this.need = need;
    }
}
