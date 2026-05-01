package src.message;

public class HelloRequestMessage {
    public static final String MessageType = "hello";
    public String message_type = MessageType;
    public String name;

    public HelloRequestMessage() {}

    public HelloRequestMessage(String name) {
        this.name = name;
    }
}
