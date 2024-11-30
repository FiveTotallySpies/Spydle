package dev.totallyspies.spydle.frontend.interface_adapters.welcome;

import lombok.Data;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Data
@Profile("!test")
public class WelcomeViewModel {

  private String playerName = "";
  private String roomCode = "";
}
