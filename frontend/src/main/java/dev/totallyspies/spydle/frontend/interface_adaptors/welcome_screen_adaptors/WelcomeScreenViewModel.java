package dev.totallyspies.spydle.frontend.interface_adaptors.welcome_screen_adaptors;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class WelcomeScreenViewModel {

    private String playerName;
    private String roomCode;
    
}
