package dev.totallyspies.spydle.gameserver.message;

import dev.totallyspies.spydle.shared.SharedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class GameSocketConfiguration implements WebSocketConfigurer {

    private final Logger logger = LoggerFactory.getLogger(GameSocketConfiguration.class);

    @Autowired
    private GameSocketHandler gameSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameSocketHandler, SharedConstants.GAME_SOCKET_ENDPOINT)
                .setAllowedOrigins("*");
        logger.info("Registered web socket handler");
    }

}
