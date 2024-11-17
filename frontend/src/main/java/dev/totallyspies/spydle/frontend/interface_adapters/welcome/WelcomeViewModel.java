package dev.totallyspies.spydle.frontend.interface_adapters.welcome;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class WelcomeViewModel {

    private String playerName;
    private String roomCode;

}
