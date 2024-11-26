package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.frontend.client.message.CbMessageListener;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.views.GameRoomView;
import dev.totallyspies.spydle.shared.proto.messages.CbGuessResult;
import dev.totallyspies.spydle.shared.proto.messages.CbGuessUpdate;
import dev.totallyspies.spydle.shared.proto.messages.CbNewTurn;
import dev.totallyspies.spydle.shared.proto.messages.CbTimerTick;
import dev.totallyspies.spydle.shared.proto.messages.CbUpdatePlayerList;
import dev.totallyspies.spydle.shared.proto.messages.Player;
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
    public void onTimerTickMessage(CbTimerTick timerTick) {
        model.setGameTimerSeconds(timerTick.getGameTimeLeftSeconds());
        model.setTurnTimerSeconds(timerTick.getTurnTimeLeftSeconds());
        view.updateGame();
    }

    @CbMessageListener
    public void onPlayerListUpdateMessage(CbUpdatePlayerList updatePlayerList) {
        model.setPlayerList(updatePlayerList.getPlayersList());
        model.getCurrentGuesses().clear();
        for (Player player : updatePlayerList.getPlayersList()) {
            model.getCurrentGuesses().put(player.getPlayerName(), new GameRoomViewModel.Guess("", GameRoomViewModel.Guess.Verdict.IN_PROGRESS));
        }
        view.updateGame();
    }

    @CbMessageListener
    public void onNewTurnMessage(CbNewTurn newTurn) {
        boolean updated = false;
        for (GameRoomViewModel.Guess guess : model.getCurrentGuesses().values()) {
            if (guess.getVerdict() == GameRoomViewModel.Guess.Verdict.IN_PROGRESS) {
                guess.setCurrentWord("");
                updated = true;
            }
        }
        if (updated) {
            view.updateGuessProgress();
        }
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
    public void onGuessWordEvent(GuessWordEvent event) {
        model.setStringEntered("");
        view.clearSubstringInputField();
    }

    @CbMessageListener
    public void onGuessUpdateMessage(CbGuessUpdate guessUpdate) {
        // This is handled on new turn
        if (guessUpdate.getGuess().isEmpty()) {
            return;
        }
        GameRoomViewModel.Guess guess = model.getCurrentGuesses().get(guessUpdate.getPlayer().getPlayerName());
        guess.setCurrentWord(guessUpdate.getGuess());
        guess.setVerdict(GameRoomViewModel.Guess.Verdict.IN_PROGRESS);
        view.updateGuessProgress();
    }

    @CbMessageListener
    public void onGuessResultMessage(CbGuessResult guessResult) {
        GameRoomViewModel.Guess guess = model.getCurrentGuesses().get(guessResult.getPlayer().getPlayerName());
        guess.setCurrentWord(guessResult.getGuess());
        guess.setVerdict(guessResult.getCorrect() ? GameRoomViewModel.Guess.Verdict.CORRECT : GameRoomViewModel.Guess.Verdict.INCORRECT);
        view.updateGuessProgress();
    }

}
