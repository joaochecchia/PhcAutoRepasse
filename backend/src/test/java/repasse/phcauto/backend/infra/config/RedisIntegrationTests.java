package repasse.phcauto.backend.infra.config;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "app.cache.ttl=30s")
@EnabledIfEnvironmentVariable(named = "REDIS_INTEGRATION_TEST", matches = "true")
class RedisIntegrationTests {

    @Autowired CacheManager cacheManager;
    @Autowired StringRedisTemplate redis;
    @Value("${app.cache.key-prefix}") String prefix;

    @Test
    void gravaLeRemoveJsonComExpiracaoNoRedisReal() {
        assertInstanceOf(RedisCacheManager.class, cacheManager);
        var cache = cacheManager.getCache("integration-test");
        assertNotNull(cache);
        String id = UUID.randomUUID().toString();
        String redisKey = prefix + "integration-test::" + id;
        var value = new CacheProbe("verificacao", Instant.parse("2026-01-01T00:00:00Z"));
        try {
            cache.put(id, value);
            assertEquals(value, cache.get(id, CacheProbe.class));
            String json = redis.opsForValue().get(redisKey);
            assertNotNull(json);
            assertTrue(json.contains("verificacao"));
            Long ttl = redis.getExpire(redisKey, TimeUnit.SECONDS);
            assertNotNull(ttl);
            assertTrue(ttl > 0 && ttl <= 30);
            // Spring Data Redis 4 pode executar evict de forma assíncrona.
            // Esta operação garante remoção imediata antes da verificação.
            cache.evictIfPresent(id);
            assertFalse(redis.hasKey(redisKey));
        } finally {
            redis.delete(redisKey);
        }
    }

    public record CacheProbe(String nome, Instant criadoEm) { }
}
