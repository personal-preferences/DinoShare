package org.personal.dinoshare.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.personal.dinoshare.domain.LinkDetails;
import org.personal.dinoshare.dto.FileDetailsDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class LinkService {

    private final Cache<String, LinkDetails> linkCache;
    private final IpLimitService ipLimitService;

    public LinkService(@Qualifier("linkDetailsCache") Cache<String, LinkDetails> linkCache, IpLimitService ipLimitService) {
        this.linkCache = linkCache;
        this.ipLimitService = ipLimitService;
    }

    public String createLink(List<FileDetailsDTO> files, Duration expMinutes, String clientIp) {

        String link = UUID.randomUUID().toString();

        LinkDetails linkdetails = new LinkDetails(link, files, expMinutes, clientIp);
        linkCache.put(link, linkdetails);

        ipLimitService.incrementCount(clientIp);

        return link;
    }

    public LinkDetails getLink(String link) {
        return linkCache.getIfPresent(link);
    }
}

