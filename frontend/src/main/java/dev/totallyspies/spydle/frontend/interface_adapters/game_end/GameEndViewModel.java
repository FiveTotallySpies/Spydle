package dev.totallyspies.spydle.frontend.interface_adapters.game_end;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
@Data
public class GameEndViewModel {

}
