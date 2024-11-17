package dev.totallyspies.spydle.frontend.interface_adapters.welcome;

import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Data
public class WelcomeViewModel {

    private String playerName;
    private String roomCode;

}
