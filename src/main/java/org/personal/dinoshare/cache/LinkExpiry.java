package org.personal.dinoshare.cache;

import com.github.benmanes.caffeine.cache.Expiry;
import org.personal.dinoshare.domain.LinkDetails;
import org.springframework.stereotype.Component;

@Component
public class LinkExpiry implements Expiry<String, LinkDetails> {

    @Override
    public long expireAfterCreate(String key, LinkDetails value, long currentTime) {

        return value.getExpMinutes().toNanos();
    }

    @Override
    public long expireAfterUpdate(String key, LinkDetails value, long currentTime, long currentExpTime) {
        return currentTime;
    }

    @Override
    public long expireAfterRead(String key, LinkDetails value, long currentTime, long currentExpTime) {
        return currentTime;
    }
}
