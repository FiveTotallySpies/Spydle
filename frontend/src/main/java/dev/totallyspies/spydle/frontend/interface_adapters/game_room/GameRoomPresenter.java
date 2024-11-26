package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.frontend.client.message.CbMessageListener;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.views.GameRoomView;
import dev.totallyspies.spydle.shared.proto.messages.CbNewTurn;
import dev.totallyspies.spydle.shared.proto.messages.CbTimerTick;
import dev.totallyspies.spydle.shared.proto.messages.CbUpdatePlayerList;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
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
        model.setGameTimerSeconds(timerTick.getGameTimeLeftSeconds());
        model.setTurnTimerSeconds(timerTick.getTurnTimeLeftSeconds());
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

    @EventListener
    public void onSwitchView(SwitchViewEvent event) {
        if (!event.getViewClass().equals(GameRoomView.class)) {
            model.reset();
            view.updateGame();
        }
    }

    @EventListener
    public void onGuessWord(GuessWordEvent event) {
        model.setStringEntered("");
        view.clearSubstringInputField();
    }

    // TODO KAI could you update the correct message
    @CbMessageListener
    public void onSubstringUpdate(CbNewTurn substringUpdate) {
        // TODO KAI, update to get the correct string.
        model.setStringCurrentPlayer(substringUpdate.getStringCurrentPlayer());
        // determine if the guess is correct
        model.setCurrentStringVerdict(substringUpdate.getStringCurrentPlayerVerdict());
        view.updateStringDisplayed();
    }

}
