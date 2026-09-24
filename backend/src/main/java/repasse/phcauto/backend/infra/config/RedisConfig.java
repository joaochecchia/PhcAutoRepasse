package repasse.phcauto.backend.infra.config;

import java.time.Duration;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(
            @Value("${app.cache.ttl}") Duration ttl,
            @Value("${app.cache.key-prefix}") String keyPrefix) {
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("O TTL do cache deve ser positivo");
        }
        if (keyPrefix.isBlank()) {
            throw new IllegalArgumentException("O prefixo do cache não pode ser vazio");
        }
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> keyPrefix + cacheName + "::")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(GenericJacksonJsonRedisSerializer.builder()
                        .enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("repasse.phcauto.backend.infra.")
                                .allowIfSubType("java.util.")
                                .allowIfSubType("java.time.")
                                .allowIfSubType("java.math.")
                                .allowIfSubType("java.lang.")
                                .build())
                        .build()));
    }
}
