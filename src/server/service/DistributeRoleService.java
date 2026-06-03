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

        // 人数に応じた役職構成を組み立てる
        //   基本: 人狼1・占い師1・騎士1
        //   7人以上: 人狼を2人に
        //   5人以上: 狂人を追加
        //   6人以上: 霊媒師を追加
        //   残りの枠はすべて村人で埋める
        List<Role> roles = new ArrayList<>();
        roles.add(Role.WOLF);
        roles.add(Role.SEER);
        roles.add(Role.KNIGHT);
        if (count >= 7) roles.add(Role.WOLF);
        if (count >= 5) roles.add(Role.CRAZY_VILLAGER);
        if (count >= 6) roles.add(Role.MEDIUM);
        while (roles.size() < count) roles.add(Role.VILLAGER);
        // 万一役職数が人数を超えた場合は人数ぶんに切り詰める（安全策）
        if (roles.size() > count) roles = new ArrayList<>(roles.subList(0, count));

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
            // 初日は朝フェーズを挟まずそのまま昼(議論)へ遷移する
            gameMaster.startInitialDay();
        } else {
            gameMaster.pushService(ServiceType.NIGHT_PHASE_START);
        }
    }
}
