package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.frontend.client.message.CbMessageListener;
import dev.totallyspies.spydle.frontend.views.GameRoomView;
import dev.totallyspies.spydle.shared.proto.messages.CbNewTurn;
import dev.totallyspies.spydle.shared.proto.messages.CbTimerTick;
import dev.totallyspies.spydle.shared.proto.messages.CbUpdatePlayerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GameRoomPresenter {

    @Autowired
    private GameRoomViewModel model;

    @Autowired
    private GameRoomView view;

    @CbMessageListener
    public void onTimerTick(CbTimerTick timerTick) {
        model.setTimerSeconds(timerTick.getTimeLeftSeconds());
        view.updateGame();
    }

    @CbMessageListener
    public void onPlayerListUpdate(CbUpdatePlayerList updatePlayerList) {
        model.setPlayerList(updatePlayerList.getPlayersList());
        view.updateGame();
    }

    @CbMessageListener
    public void onNewTurn(CbNewTurn newTurn) {
        model.setCurrentTurnPlayer(newTurn.getCurrentPlayer());
        model.setCurrentSubstring(newTurn.getAssignedString());
    }

}
