package dev.totallyspies.spydle.frontend.interface_adapters.game_room;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class GuessWordEvent extends ApplicationEvent {

  private final String guess;

  public GuessWordEvent(Object source, String guess) {
    super(source);
    this.guess = guess;
  }
}
