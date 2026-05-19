package src.server.core;

import java.util.concurrent.BlockingQueue;
import src.server.game.GameMaster;

public class Worker implements Runnable {
    private final BlockingQueue<ServiceType> queue;
    private final GameMaster gameMaster;
    private final Broadcaster broadcaster;
    private final ServiceFactory factory = new ServiceFactory();

    public Worker(BlockingQueue<ServiceType> queue, GameMaster gameMaster, Broadcaster broadcaster) {
        this.queue = queue;
        this.gameMaster = gameMaster;
        this.broadcaster = broadcaster;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ServiceType type = queue.take();
                factory.create(type, gameMaster, broadcaster).call();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
