package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.use_cases.guess_word.GuessWordInputBoundary;
import dev.totallyspies.spydle.frontend.use_cases.guess_word.GuessWordInputData;
import dev.totallyspies.spydle.frontend.use_cases.guess_word.GuessWordInteractor;
import dev.totallyspies.spydle.frontend.use_cases.update_guess.UpdateGuessInputBoundary;
import dev.totallyspies.spydle.frontend.use_cases.update_guess.UpdateGuessInputData;
import dev.totallyspies.spydle.frontend.views.WelcomeView;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbStartGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;

@Component
@Profile("!test")
public class GameRoomViewController {

  private final Logger logger = LoggerFactory.getLogger(GameRoomViewController.class);

  private final ApplicationEventPublisher publisher;
  private final ClientSocketHandler handler;
  private final GuessWordInputBoundary guessWordInteractor;
  private final GameRoomViewModel model;
  private final UpdateGuessInputBoundary updateGuessInteractor;
  private final GameRoomPresenter presenter;

  public GameRoomViewController(
      ApplicationEventPublisher publisher,
      ClientSocketHandler handler,
      GuessWordInteractor guessWordInteractor,
      GameRoomViewModel model,
      UpdateGuessInputBoundary updateGuessInteractor,
      @Lazy GameRoomPresenter presenter) {
    this.publisher = publisher;
    this.handler = handler;
    this.guessWordInteractor = guessWordInteractor;
    this.model = model;
    this.updateGuessInteractor = updateGuessInteractor;
    this.presenter = presenter;
  }

  /*
  Method called when View All Rooms Button is Pressed
   */
  public void openWelcomeView() {
    if (handler.isOpen()) {
      handler.close(
          new CloseStatus(CloseStatus.NORMAL.getCode(), "Client prompted session termination"));
    }
    publisher.publishEvent(new SwitchViewEvent(this, WelcomeView.class));
  }

  public void startGame() {
    if (!handler.isOpen()) {
      logger.error("Cannot start game: client session is not open!");
      return;
    }
    handler.sendSbMessage(
        SbMessage.newBuilder().setStartGame(SbStartGame.newBuilder().build()).build());
  }

  public void updateGuess() {
    updateGuessInteractor.execute(new UpdateGuessInputData(model.getStringEntered()));
  }

  public void guessWord() {
    guessWordInteractor.execute(new GuessWordInputData(model.getStringEntered()));
    presenter.clearSubstringInputField();
  }
}
