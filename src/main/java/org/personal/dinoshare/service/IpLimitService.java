package org.personal.dinoshare.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IpLimitService {

    @Value("${app.upload.ip-limit}")
    private int ipLimit;

    private Cache<String, AtomicInteger> ipCountCache;

    @PostConstruct
    public void init() {
        ipCountCache = Caffeine.newBuilder().maximumSize(10000).build();
    }

    // IP당 count 제한 검증
    public boolean isOverLimit(String ip) {

        // 유효하지 않은 IP -> 통과
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        AtomicInteger currentCount = ipCountCache.getIfPresent(ip);
        int count = (currentCount == null) ? 0 : currentCount.get();

        return count >= ipLimit;
    }

    // IP당 count 추가
    public void incrementCount(String ip) {

        // 유효하지 않은 IP -> X
        if (ip == null || ip.trim().isEmpty()) {
            return;
        }

        ipCountCache.asMap().compute(ip, (key, count) -> {
            if (count == null) {
                // 새로운 IP는 1로 초기화
                return new AtomicInteger(1);
            } else {
                count.incrementAndGet();
                return count;
            }
        });

        AtomicInteger currentCount = ipCountCache.getIfPresent(ip);
        System.out.println("IP: " + ip + " incremented count to: " + (currentCount != null ? currentCount.get() : 0));
    }

    // IP당 count 감소
    public void decrementCount(String ip) {

        // 유효하지 않은 IP -> X
        if (ip == null || ip.trim().isEmpty()) {
            return;
        }

        ipCountCache.asMap().compute(ip, (key, count) -> {
            if (count == null) {
                // 감소할 count가 없는 경우
                return null;
            }

            int newCount = count.decrementAndGet();

            if (newCount <= 0) {
                return null;
            } else {
                return count;
            }
        });

        AtomicInteger currentCount = ipCountCache.getIfPresent(ip);
        System.out.println("IP: " + ip + " decremented count to: " + (currentCount != null ? currentCount.get() : 0));
    }
}
