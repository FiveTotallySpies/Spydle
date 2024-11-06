package dev.totallyspies.spydle.gameserver.storage;

import dev.totallyspies.spydle.gameserver.storage.local.LocalStorage;
import dev.totallyspies.spydle.gameserver.storage.redis.RedisStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class GameServerStorageConfiguration {

    private final Logger logger = LoggerFactory.getLogger(GameServerStorageConfiguration.class);

    @Bean
    @Primary // Fallback option
    @ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "redis")
    public GameServerStorage redisStorage(RedisTemplate<String, Object> redisTemplate) {
        logger.info("Found storage.type=redis, loading RedisStorage");
        return new RedisStorage(redisTemplate);
    }

    @Bean
    @ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "redis")
    public GameServerStorage localStorage() {
        logger.info("Found storage.type=local, loading LocalStorage");
        return new LocalStorage();
    }

}
