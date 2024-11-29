package dev.totallyspies.spydle.frontend.interface_adapters.game_end;

import dev.totallyspies.spydle.shared.proto.messages.Player;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@Data
public class GameEndViewModel {

  private List<Player> players = new ArrayList<>();
}
