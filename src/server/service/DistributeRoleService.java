package src.server.service;

import src.message.DistributeRoleMessage;
import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;
import src.server.database.entity.Player;
import src.server.database.entity.Role;
import src.server.database.repository.PlayerRepository;
import src.server.database.repository.RoomRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DistributeRoleService extends BaseService implements BroadcastService {
    private final RoomRepository roomRepo = new RoomRepository();
    private final PlayerRepository playerRepo = new PlayerRepository();
    private final Broadcaster broadcaster;

    public DistributeRoleService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        List<Player> players = roomRepo.getPlayers(roomId);
        List<Role> roles = buildRoleList(players.size());
        Collections.shuffle(roles);

        for (int i = 0; i < players.size(); i++) {
            Role role = roles.get(i);
            playerRepo.setRole(roomId, players.get(i).id, role);
            broadcaster.sendTo(players.get(i).id, new DistributeRoleMessage(role.name()));
        }
    }

    // 4人: wolf×1, seer×1, villager×2
    // 5人: wolf×1, seer×1, knight×1, villager×2
    // 6人以上: wolf×2, seer×1, knight×1, villager×残り
    private List<Role> buildRoleList(int count) {
        List<Role> list = new ArrayList<>();
        int wolves = count >= 6 ? 2 : 1;
        for (int i = 0; i < wolves; i++) list.add(Role.WOLF);
        list.add(Role.SEER);
        if (count >= 5) list.add(Role.KNIGHT);
        while (list.size() < count) list.add(Role.VILLAGER);
        return list;
    }
}
