package dev.totallyspies.spydle.frontend.interface_adapters.welcome;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class WelcomeViewModel {

    private String playerName = "";
    private String roomCode = "";


}
