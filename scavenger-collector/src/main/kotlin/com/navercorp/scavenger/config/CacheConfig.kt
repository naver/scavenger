package com.navercorp.scavenger.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.navercorp.scavenger.service.IntervalService
import com.navercorp.scavenger.service.LicenseService
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    fun buildCache(cacheName: String, duration: Duration, allowNullValue: Boolean, maxSize: Long = 1000L) =
        CaffeineCache(
            cacheName,
            Caffeine.newBuilder().expireAfterWrite(duration).recordStats().maximumSize(maxSize).build(),
            allowNullValue
        )

    @Bean
    fun cacheManager(): CacheManager =
        SimpleCacheManager().apply {
            setCaches(
                setOf(
                    buildCache(LicenseService.LICENSE_CACHE, Duration.ofHours(24), true),
                    buildCache(IntervalService.INTERVAL_CACHE, Duration.ofSeconds(IntervalService.TIMESLOT_SECONDS / 2), false)
                )
            )
        }
}
