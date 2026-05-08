package src.server.service;

import src.message.AnnounceGameOverMessage;
import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;
import src.server.database.entity.Player;
import src.server.database.entity.Role;
import src.server.database.repository.PlayerRepository;
import src.server.database.repository.RoomRepository;

import java.util.List;
import java.util.stream.Collectors;

public class AnnounceGameOverService extends BaseService implements BroadcastService {
    private final PlayerRepository playerRepo = new PlayerRepository();
    private final RoomRepository roomRepo = new RoomRepository();
    private final Broadcaster broadcaster;

    public AnnounceGameOverService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        String winner = playerRepo.villagersWin(roomId) ? "VILLAGER" : "WOLF";

        List<AnnounceGameOverMessage.PlayerResult> results = roomRepo.getPlayers(roomId).stream()
            .map(p -> new AnnounceGameOverMessage.PlayerResult(p.id, p.name, p.role.name()))
            .collect(Collectors.toList());

        broadcaster.broadcast(roomId, new AnnounceGameOverMessage(winner, results));
    }
}
