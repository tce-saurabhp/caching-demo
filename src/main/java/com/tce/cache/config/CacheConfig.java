package com.tce.cache.config;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.event.EventType;
import org.ehcache.impl.config.event.DefaultCacheEventListenerConfiguration;
import org.ehcache.impl.config.persistence.DefaultPersistenceConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import java.io.File;
import java.util.Map;


@Configuration
public class CacheConfig {

    @Bean
    public DefaultCacheEventListenerConfiguration cacheEventListenerConfiguration() {
        return CacheEventListenerConfigurationBuilder
                .newEventListenerConfiguration(
                        cacheEvent -> System.out.println("Event Type: " + cacheEvent.getType() + " | Key: " + cacheEvent.getKey() + " | Old Value: " + cacheEvent.getOldValue() + " | New Value: " + cacheEvent.getNewValue()),
                        EventType.CREATED, EventType.UPDATED, EventType.REMOVED, EventType.EXPIRED)
                .unordered().asynchronous().build();
    }

    //https://spin.atomicobject.com/ehcache-spring-boot/
    @Bean
    public CacheManager cacheManager(final DefaultCacheEventListenerConfiguration cacheEventListenerConfiguration) {
        var config = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        String.class, String.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(500, EntryUnit.ENTRIES)
                                .offheap(1, MemoryUnit.GB)
                                .disk(3, MemoryUnit.GB, true)
                )
                .withService(cacheEventListenerConfiguration)
                .build();

        // A Map of all the caches we want to create (just one in this example)
        Map<String, CacheConfiguration<?, ?>> caches = Map.of("primary", config);

        var cachingProvider = Caching.getCachingProvider();
        var ehcacheProvider = (EhcacheCachingProvider) cachingProvider;

        var configuration = new DefaultConfiguration(
                caches,
                ehcacheProvider.getDefaultClassLoader(),
                new DefaultPersistenceConfiguration(new File("tmp/ehcache")));

        return new JCacheCacheManager(
                ehcacheProvider.getCacheManager(
                        ehcacheProvider.getDefaultURI(), configuration));
    }

    @Bean
    public Cache cache(final CacheManager cacheManager) {
        Cache cache = cacheManager.getCache("primary");
        if(cache == null) {
            throw new IllegalStateException("Cache not found");
        }
        return cache;
    }

}