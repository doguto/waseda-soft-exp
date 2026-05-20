package src.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import src.message.DistributeRoleMessage;
import src.server.core.BroadcastService;
import src.server.core.Broadcaster;
import src.server.core.ServiceType;
import src.server.database.entity.Player;
import src.server.database.entity.Role;
import src.server.game.GameMaster;

public class DistributeRoleService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public DistributeRoleService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {

        List<Player> players = roomRepository.getPlayers(roomId);
        int count = players.size();

        List<Role> roles = new ArrayList<>(List.of(Role.WOLF, Role.SEER, Role.KNIGHT, Role.VILLAGER));
        if (count >= 5) roles.add(Role.CRAZY_VILLAGER);
        if (count >= 6) roles.add(Role.PLIEST);

        Collections.shuffle(roles);

        for (int i = 0; i < players.size(); i++) {
            String playerName = players.get(i).name;
            Role role = roles.get(i);
            gameMaster.playerRepository.setRole(roomId, playerName, role);
            broadcaster.sendTo(playerName, new DistributeRoleMessage(role.name()));
        }

        gameMaster.pushService(ServiceType.NIGHT_PHASE_START);
    }
}
