package dev.totallyspies.spydle.gameserver.socket.event;

import dev.totallyspies.spydle.gameserver.proto.GameMessages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EventManager {

    @Bean
    public Map<GameMessages.ServerBoundMessage.PayloadCase, Class<? extends ServerBoundEvent>> eventRegistry() {
        Map<GameMessages.ServerBoundMessage.PayloadCase, Class<? extends ServerBoundEvent>> events = new HashMap<>();
        events.put(GameMessages.ServerBoundMessage.PayloadCase.PLAYER_JOIN, PlayerJoinEvent.class);
        events.put(GameMessages.ServerBoundMessage.PayloadCase.GUESS_WORD, PlayerGuessWordEvent.class);
        return events;
    }

}
