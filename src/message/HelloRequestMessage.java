package src.message;

public class HelloRequestMessage {
    public String type = "hello";
    public String name;

    public HelloRequestMessage(String name) {
        this.name = name;
    }

    public HelloRequestMessage(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
