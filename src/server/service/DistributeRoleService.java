package src.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import src.message.DistributeRoleMessage;
import src.server.core.BroadcastService;
import src.server.core.Broadcaster;
import src.server.core.ServiceType;
import src.server.database.entity.Player;
import src.common.Role;
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
        List<String> playerNames = players.stream().map(player -> player.name).toList();

        List<Role> roles = new ArrayList<>(List.of(Role.WOLF, Role.SEER, Role.KNIGHT, Role.VILLAGER));
        if (count >= 5) roles.add(Role.CRAZY_VILLAGER);
        if (count >= 6) roles.add(Role.MEDIUM);

        Collections.shuffle(roles);

        // 初日の扱い判定
        boolean initialStart = gameMaster.getStateManager().getNightCount() == 0;

        for (int i = 0; i < players.size(); i++) {
            String playerName = players.get(i).name;
            Role role = roles.get(i);
            gameMaster.playerRepository.setRole(playerName, role);
            DistributeRoleMessage drm = new DistributeRoleMessage(role.name(), playerNames);
            drm.startDay = initialStart;
            broadcaster.sendTo(playerName, drm);
        }
        if (initialStart) {
            // NPC 発見のシステムメッセージを全体に流す
            broadcaster.broadcast(roomId, new src.message.ChatBroadcastMessage("VILLAGE", "[システム]", "NPCが無残な姿で発見されました"));
            // フェーズを昼(議論)に遷移（GameMaster ヘルパーを使用）
            gameMaster.startInitialDay();
        } else {
            gameMaster.pushService(ServiceType.NIGHT_PHASE_START);
        }
    }
}
