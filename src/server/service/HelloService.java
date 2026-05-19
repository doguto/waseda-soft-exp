package src.server.service;

import src.message.HelloRequestMessage;
import src.message.HelloResponseMessage;

public class HelloService {
    public HelloResponseMessage call(HelloRequestMessage request) {
        String greeting = "Hello, " + request.name + "!";
        return new HelloResponseMessage(greeting);
    }
}
