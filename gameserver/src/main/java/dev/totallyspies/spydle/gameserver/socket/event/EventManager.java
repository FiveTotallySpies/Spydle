package dev.totallyspies.spydle.gameserver.socket.event;

import dev.totallyspies.spydle.shared.proto.GameMessages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EventManager {

    @Bean
    public Map<GameMessages.ServerBoundMessage.PayloadCase, Class<? extends ServerBoundMessageEvent>> eventRegistry() {
        Map<GameMessages.ServerBoundMessage.PayloadCase, Class<? extends ServerBoundMessageEvent>> events = new HashMap<>();
        events.put(GameMessages.ServerBoundMessage.PayloadCase.JOIN_GAME, JoinGameMessageEvent.class);
        events.put(GameMessages.ServerBoundMessage.PayloadCase.START_GAME, StartGameMessageEvent.class);
        events.put(GameMessages.ServerBoundMessage.PayloadCase.LEAVE_GAME, LeaveGameMessageEvent.class);
        events.put(GameMessages.ServerBoundMessage.PayloadCase.GUESS, GuessMessageEvent.class);
        return events;
    }

}
