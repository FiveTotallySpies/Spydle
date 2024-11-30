package dev.totallyspies.spydle.gameserver.agones;

import io.grpc.ManagedChannelBuilder;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.infumia.agones4j.Agones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "agones.enabled", havingValue = "true")
public class AgonesConfig {

  private final Logger logger = LoggerFactory.getLogger(AgonesConfig.class);

  @Bean
  public Agones agones(@Value("${agones.host-port}") int agonesPort) {
    // Construct agones SDK wrapper on GRPC
    final ExecutorService gameServerWatcherExecutor = Executors.newSingleThreadExecutor();
    final ScheduledExecutorService healthCheckExecutor =
        Executors.newSingleThreadScheduledExecutor();
    Agones agones =
        Agones.builder()
            .withAddress("localhost", agonesPort)
            .withChannel(
                ManagedChannelBuilder.forAddress("localhost", agonesPort).usePlaintext().build())
            .withGameServerWatcherExecutor(gameServerWatcherExecutor)
            .withHealthCheck(
                Duration.ofSeconds(1L), // Delay
                Duration.ofSeconds(2L) // Period
                )
            .withHealthCheckExecutor(healthCheckExecutor)
            .build();
    logger.info("Instantiated Agones hook on localhost:{}", agonesPort);
    if (agones.canHealthCheck()) {
      agones.startHealthChecking();
      logger.info("Began Agones health checking");
    } else {
      throw new IllegalStateException("Failed to begin Agones health checking");
    }
    if (agones.canWatchGameServer()) {
      agones.addGameServerWatcher(
          gameServer -> {
            logger.info(
                "Received state updated from Agones: {}", gameServer.getStatus().getState());
          });
    } else {
      logger.warn("Failed to add game server watcher: Not allowed");
    }
    return agones;
  }
}
