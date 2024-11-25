package dev.totallyspies.spydle.frontend.interface_adapters.game_end;

import dev.totallyspies.spydle.frontend.client.message.CbMessageListener;
import dev.totallyspies.spydle.frontend.views.GameEndView;
import dev.totallyspies.spydle.shared.proto.messages.CbGameEnd;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class GameEndPresenter {

    private final GameEndViewModel model;
    private final GameEndView view;

    public GameEndPresenter(GameEndViewModel model, GameEndView view) {
        this.model = model;
        this.view = view;
    }

    @CbMessageListener
    public void onGameEnd(CbGameEnd gameEnd) {
        model.setPlayers(gameEnd.getPlayersList());
        view.setPlacements();
    }

}
