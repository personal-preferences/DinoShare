package org.personal.dinoshare.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.personal.dinoshare.domain.LinkDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    @Value("${app.cache.linkDetailCache.max-size}")
    private int linkCacheMaxSize;

    @Value("${app.cache.ipCountCache.max-size}")
    private int ipCntCacheMaxSize;

    private final LinkRemovalListener linkRemovalListener;
    private final LinkExpiry linkExpiry;

    @Bean("linkDetailsCache")
    public Cache<String, LinkDetails> linkDetailsCache() {

        return Caffeine.newBuilder()
                .maximumSize(linkCacheMaxSize)
                .removalListener(linkRemovalListener)
                .expireAfter(linkExpiry)
                .build();
    }

    @Bean("ipCountCache")
    public Cache<String, Integer> ipCountCache() {

        return Caffeine.newBuilder()
                .maximumSize(ipCntCacheMaxSize)
                .build();
    }
}
