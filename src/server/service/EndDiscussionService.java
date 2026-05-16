package src.server.service;

import src.message.EndDiscussionMessage;
import src.message.EndDiscussionResultMessage;
import src.server.core.ServiceType;
import src.server.game.GameEvent;
import src.server.game.GameMaster;

public class EndDiscussionService extends BaseService {

    public EndDiscussionService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public EndDiscussionResultMessage call(EndDiscussionMessage msg) {
        // stateManager.check(GameEvent.DISCUSSION_ENDED) を呼んでフェーズを VOTE に遷移させる
        stateManager.check(GameEvent.DISCUSSION_ENDED);
        //   → discussionEnded フラグで二重呼び出しを防止済み
        // gameMaster.pushService(ServiceType.VOTE_PHASE_START) をキューに積む
        gameMaster.pushService(ServiceType.VOTE_PHASE_START);

        // TODO: 現状、ボタン連打やタイマーの競合（マルチスレッド）が発生した場合、
        // gameMaster.pushService() が二重に発火して投票開始アナウンスが重複する懸念があります。
        // 
        // 【改善案】
        // GameStateManager.java の `currentPhase` に volatile を付与した上で、
        // DISCUSSION_ENDED の switch-case 内で直接 pushService を呼ぶ形に統一するか、
        // もしくは check() メソッドの戻り値を boolean に変更し、初めてフェーズ変更に
        // 成功した（AtomicBoolean の勝者）スレッドのみがこの service 側で pushService を
        // 実行できるようにリファクタリングすることを提案します（他のVOTE_SUBMITTED等の処理と共通化）。


        // 成功を EndDiscussionResultMessage に設定して返す
        return new EndDiscussionResultMessage();
    }
}
