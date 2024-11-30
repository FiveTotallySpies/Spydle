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
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "redis")
public class RedisConfiguration {

  private final Logger logger = LoggerFactory.getLogger(RedisConfiguration.class);

  private final String redisHost;
  private final int redisPort;

  public RedisConfiguration(
      @Value("${redis.host}") String redisHost, @Value("${redis.port}") int redisPort) {
    this.redisHost = redisHost;
    this.redisPort = redisPort;
  }

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration configuration =
        new RedisStandaloneConfiguration(redisHost, redisPort);
    logger.info("Created Jedis connection factory to redis at {}:{}", redisHost, redisPort);
    return new JedisConnectionFactory(configuration);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(jedisConnectionFactory());
    template.setKeySerializer(new StringRedisSerializer());
    return template;
  }
}
