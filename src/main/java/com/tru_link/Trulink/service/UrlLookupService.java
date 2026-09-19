package com.tru_link.Trulink.service;

import com.tru_link.Trulink.entity.ResolvedLink;
import com.tru_link.Trulink.entity.ResolvedLinkProjection;
import com.tru_link.Trulink.repo.UrlRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UrlLookupService {

    private final UrlRepo repo;

    @Cacheable(value = "urlCache", key = "#shortKey")
    public ResolvedLink resolveUrl(String shortKey){
        ResolvedLinkProjection projection = repo.findResolvedByShortKey(shortKey);
        if (projection == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid URL");
        return new ResolvedLink(projection.getLinkId(), projection.getOriginalUrl(),  projection.getUserId() != null);
    }

}
