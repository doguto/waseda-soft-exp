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
                System.out.println("[WORKER] Executing: " + type + " room=" + gameMaster.getRoomId());
                try {
                    factory.create(type, gameMaster, broadcaster).call();
                    System.out.println("[WORKER] Completed: " + type + " room=" + gameMaster.getRoomId());
                } catch (Exception e) {
                    System.out.println("[ERROR] Worker service failed: " + type + " room=" + gameMaster.getRoomId() + " " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
