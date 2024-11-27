package dev.totallyspies.spydle.frontend.interface_adapters.game_end;

import dev.totallyspies.spydle.frontend.client.message.CbMessageListener;
import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.SwitchViewEvent;
import dev.totallyspies.spydle.frontend.views.GameEndView;
import dev.totallyspies.spydle.shared.proto.messages.CbGameEnd;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class GameEndPresenter {

    private final GameEndViewModel model;
    private final GameEndView view;
    private final ApplicationEventPublisher publisher;

    public GameEndPresenter(GameEndViewModel model, GameEndView view, ApplicationEventPublisher publisher) {
        this.model = model;
        this.view = view;
        this.publisher = publisher;
    }

    @CbMessageListener
    public void onGameEndMessage(CbGameEnd gameEnd) {
        model.setPlayers(gameEnd.getPlayersList());
        view.setPlacements();
        publisher.publishEvent(new SwitchViewEvent(this, GameEndView.class));
    }

}
