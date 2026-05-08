package src.server;

import src.server.service.*;

public class ServiceFactory {
    public BroadcastService create(ServiceType type, GameMaster gameMaster, Broadcaster broadcaster) {
        String roomId = gameMaster.getRoomId();
        return switch (type) {
            case DISTRIBUTE_ROLE         -> new DistributeRoleService(roomId, gameMaster, broadcaster);
            case ANNOUNCE_MORNING        -> new AnnounceMorningService(roomId, gameMaster, broadcaster);
            case DISTRIBUTE_VOTE_RESULT  -> new DistributeVoteResultService(roomId, gameMaster, broadcaster);
            case EXECUTE                 -> new ExecuteService(roomId, gameMaster, broadcaster);
            case ANNOUNCE_GAME_OVER      -> new AnnounceGameOverService(roomId, gameMaster, broadcaster);
            case NIGHT_PHASE_START       -> new NightPhaseStartService(roomId, gameMaster, broadcaster);
            case VOTE_PHASE_START        -> new VotePhaseStartService(roomId, gameMaster, broadcaster);
        };
    }
}
