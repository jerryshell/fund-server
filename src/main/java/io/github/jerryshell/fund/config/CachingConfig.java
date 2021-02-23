package io.github.jerryshell.fund.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

@Slf4j
@Configuration
@EnableCaching
@EnableScheduling
public class CachingConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("fund-server");
    }

    // 每个小时清空一次缓存
    @Scheduled(cron = "0 0 * * * ?")
    @CacheEvict(allEntries = true, cacheNames = "fund-server")
    public void cacheEvict() {
        log.info("Flush Cache {}", new Date());
    }
}
