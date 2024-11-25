package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.frontend.client.message.CbMessageListener;
import dev.totallyspies.spydle.frontend.views.GameRoomView;
import dev.totallyspies.spydle.shared.proto.messages.CbNewTurn;
import dev.totallyspies.spydle.shared.proto.messages.CbTimerTick;
import dev.totallyspies.spydle.shared.proto.messages.CbUpdatePlayerList;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class GameRoomPresenter {

    private final GameRoomViewModel model;
    private final GameRoomView view;

    public GameRoomPresenter(GameRoomViewModel model, GameRoomView view) {
        this.model = model;
        this.view = view;
    }

    @CbMessageListener
    public void onTimerTick(CbTimerTick timerTick) {
        model.setTimerSeconds(timerTick.getGameTimeLeftSeconds());
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
        view.updateGame();
    }

}
