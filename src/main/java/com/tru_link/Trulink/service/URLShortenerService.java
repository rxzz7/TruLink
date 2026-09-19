package com.tru_link.Trulink.service;

import com.tru_link.Trulink.config.AppProperties;
import com.tru_link.Trulink.config.KeyStore;
import com.tru_link.Trulink.entity.CreateResponse;
import com.tru_link.Trulink.entity.LinkItemResponse;
import com.tru_link.Trulink.entity.ResolvedLink;
import com.tru_link.Trulink.entity.WebUrl;
import com.tru_link.Trulink.exception.ShortKeyGenerationException;
import com.tru_link.Trulink.exception.ShortKeyNotFoundException;
import com.tru_link.Trulink.repo.UrlRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Slf4j
@Service
public class URLShortenerService {

    private static final int MAX_SHORTKEY_ATTEMPTS = 3;
    private static final int MAX_URL_LENGTH = 1024;

    private final UrlRepo repo;
    private final KeyStore keyStore;
    private final AppProperties appProperties;
    private final UrlLookupService urlLookupService;
    private final CacheManager cacheManager;

    public URLShortenerService(UrlRepo repo, KeyStore keyStore, AppProperties appProperties, UrlLookupService urlLookupService, CacheManager cacheManager){
        this.repo = repo;
        this.keyStore = keyStore;
        this.appProperties = appProperties;
        this.urlLookupService = urlLookupService;
        this.cacheManager = cacheManager;
    }


    public CreateResponse createShortLink(String originalUrl, UUID userId) {
        validate(originalUrl);

        String shortKey = null;
        for (int attempt = 1; attempt <= MAX_SHORTKEY_ATTEMPTS; attempt++){
            String candidate = ShortKeyGenerator.generateShortKey();

            if(keyStore.contains(candidate)) continue;
            keyStore.addKey(candidate);
            //saves and immediately sends pending changes to the database using flush().
            try {
                repo.saveAndFlush(new WebUrl(originalUrl, candidate, userId));
                shortKey = candidate;
                break;
            }catch (DataIntegrityViolationException e){
                keyStore.removeKey(candidate);
                log.debug("Short-key collision on '{}' (attempt {}/{}), retrying.",
                        candidate,
                        attempt,
                        MAX_SHORTKEY_ATTEMPTS);
            }catch (Exception e){
                keyStore.removeKey(candidate);
                log.error("Error while saving short-key: {}", e.getMessage());
                throw e;
            }
        }
        if (shortKey == null){
            log.error("Couldn't generate  a unique short-key after {} attempts", MAX_SHORTKEY_ATTEMPTS);
            throw new ShortKeyGenerationException("Could not generate a unique short key, please retry");
        }

        return new CreateResponse(appProperties.getBaseUrl() + "/" + shortKey);

    }

    private void validate(String originalUrl){
        if ((originalUrl != null && originalUrl.length() > MAX_URL_LENGTH)){
            throw new IllegalStateException("URL must be at most " + MAX_URL_LENGTH + "characters");
        }

        URI uri;
        try {
            uri = new URI(originalUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed URL");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.startsWith("http") || scheme.startsWith("https"))){
            throw new IllegalArgumentException("URL must use http or https");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank() || !host.contains(".")){
            throw new IllegalArgumentException("URL must contain a valid host");
        }
    }

    public String redirectUrl(String shortKey){
        if(!StringUtils.hasText(shortKey)){
            throw new IllegalArgumentException("ShortKey must not be blank");
        }
        if (!keyStore.contains(shortKey)){
            throw new ShortKeyNotFoundException("Invalid Link");
        }

        ResolvedLink resolved = urlLookupService.resolveUrl(shortKey);
        return resolved.originalUrl();
    }

    public Page<LinkItemResponse> getUserLinks(UUID userId, int page, int size) {
        Pageable pageable = createPage(page, size);
        Page<LinkItemResponse> rawLinks = repo.findUserLinks(userId, pageable);

        return rawLinks.map(link -> new LinkItemResponse(
                link.shortKey(),
                appProperties.getBaseUrl() + "/" + link.shortKey(),
                link.originalUrl(),
                link.createdAt()
        ));

    }
    private Pageable createPage(int page, int size){
        return (PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public void deleteLink(String shortKey, UUID userId){
        UUID linkId = repo.findLinkIdByShortKeyAndUserId(shortKey, userId)
                .orElseThrow(() -> new ShortKeyNotFoundException("Link not found or access denied"));

        repo.deleteByShortKeyAndUserId(shortKey, userId);
        keyStore.removeKey(shortKey);
        Cache urlCache = cacheManager.getCache("urlCache");
        if (urlCache != null) urlCache.evict(shortKey);
    }
}
