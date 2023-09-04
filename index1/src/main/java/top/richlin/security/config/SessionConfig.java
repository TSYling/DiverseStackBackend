package top.richlin.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

import java.time.Duration;

/**
 * SessionConfig
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/17 19:35
 * @description
 */
@Configuration
@EnableSpringHttpSession
public class SessionConfig {
    final RedisConnectionFactory connectionFactory;

    public SessionConfig(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    @Bean
    public RedisOperations<String, Object> template() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public RedisIndexedSessionRepository repository() {
        RedisIndexedSessionRepository redisIndexedSessionRepository = new RedisIndexedSessionRepository(template());
        redisIndexedSessionRepository.setRedisKeyNamespace("richlin:session");
        //  设置为30分钟不过期
        redisIndexedSessionRepository.setDefaultMaxInactiveInterval(Duration.ofSeconds(1800));
        return redisIndexedSessionRepository;
    }
}

