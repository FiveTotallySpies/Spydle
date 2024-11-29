package dev.totallyspies.spydle.gameserver.socket;

import dev.totallyspies.spydle.shared.SharedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class GameSocketConfig implements WebSocketConfigurer {

  private final Logger logger = LoggerFactory.getLogger(GameSocketConfig.class);

  private final GameSocketHandler gameSocketHandler;

  public GameSocketConfig(GameSocketHandler gameSocketHandler) {
    this.gameSocketHandler = gameSocketHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(gameSocketHandler, SharedConstants.GAME_SOCKET_ENDPOINT)
        .setAllowedOrigins("*");
    logger.info("Registered web socket handler");
  }
}
