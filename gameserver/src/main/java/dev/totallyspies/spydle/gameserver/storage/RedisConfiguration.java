package dev.totallyspies.spydle.gameserver.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "redis")
public class RedisConfiguration {

    private final Logger logger = LoggerFactory.getLogger(RedisConfiguration.class);

    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private int redisPort;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        logger.info("Created Jedis connection factory to redis at {}:{}", redisHost, redisPort);
        return new JedisConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

}