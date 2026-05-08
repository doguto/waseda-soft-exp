package src.message;

public class SendChatResultMessage {
    public static final String MessageType = "send_chat_result";
    public String message_type = MessageType;
    public boolean success;

    public SendChatResultMessage() {}
    public SendChatResultMessage(boolean success) { this.success = success; }
}
