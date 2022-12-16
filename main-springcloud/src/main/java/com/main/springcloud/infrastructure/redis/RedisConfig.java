
package com.main.springcloud.infrastructure.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;


/**
 * Redis缓存配置类
 *
 */
@Configuration
@Slf4j
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${redis.cache.expire:3600}")
    private int cacheExpire;

    /**
     * redis缓存失效时间配置
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        //初始化一个不带锁机制的RedisCacheWriter
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig();

        log.info("============>entryTtl:{}", cacheExpire);
        RedisCacheConfiguration configuration = defaultCacheConfig.entryTtl(Duration.ofSeconds(cacheExpire));

        //初始化RedisCacheManager
        return new RedisCacheManager(redisCacheWriter, configuration);
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {


        log.debug("配置redis连接");

        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        template.setValueSerializer(serializer);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();


        return template;
    }


    /**
     * 缓存对象集合中，缓存是以key-value形式保存的。
     * 当不指定缓存的key时，SpringBoot会使用SimpleKeyGenerator生成key
     * 此时有一个问题：
     * 如果2个方法，参数是一样的，但执行逻辑不同，那么将会导致执行第二个方法时命中第一个方法的缓存。
     * 解决办法是在@Cacheable注解参数中指定key，或者自己实现一个KeyGenerator，在注解中指定KeyGenerator。
     * 但是如果这样的情况很多，每一个都要指定key、KeyGenerator很麻烦。
     * Spring同样提供了方案：继承CachingConfigurerSupport并重写keyGenerator()
     * 此时，缓存的key是包名+方法名+参数列表，这样就很难会冲突了。
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {

        log.debug("配置redis key生成器");

        return (target, method, objects) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append("::").append(method.getName()).append(":");
            for (Object obj : objects) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }


    /**
     * 配置redis异常时的处理策略，使得redis异常时，不影响程序
     */
    @Override
    @Bean
    public CacheErrorHandler errorHandler() {

        log.debug("配置redis 异常处理器");

        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.error("redis异常：key=[{}]", key, e);
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.error("redis异常：key=[{}]", key, e);
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.error("redis异常：key=[{}]", key, e);
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.error("redis异常：", e);
            }
        };
    }

}