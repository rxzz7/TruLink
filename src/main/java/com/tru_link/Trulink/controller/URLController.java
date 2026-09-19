package com.tru_link.Trulink.controller;

import com.tru_link.Trulink.entity.CreateRequest;
import com.tru_link.Trulink.entity.CreateResponse;
import com.tru_link.Trulink.entity.LinkItemResponse;
import com.tru_link.Trulink.service.URLShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class URLController {

    private final URLShortenerService shortenerService;


    @PostMapping("/create")
    public ResponseEntity<CreateResponse> createShortLink(
            @Valid @RequestBody CreateRequest request,
            @AuthenticationPrincipal Jwt jwt){
        UUID userId = (jwt != null) ? UUID.fromString(jwt.getSubject()) : null;
        CreateResponse response = shortenerService.createShortLink(request.originalUrl(), userId);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{shortKey}", method = { RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<Void> redirectUrl(@PathVariable String shortKey, HttpServletRequest request){

        String originalUrl = shortenerService.redirectUrl(shortKey);
        return ResponseEntity.status(302).location(URI.create(originalUrl)).build();

    }

    @GetMapping("/my-links")
    public ResponseEntity<Page<LinkItemResponse>> getMyLinks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ){

        Page<LinkItemResponse> links = shortenerService.getUserLinks(UUID.fromString(jwt.getSubject()), page, Math.min(size, 100));
        return ResponseEntity.ok(links);
    }
// registered users can only delete
    @DeleteMapping("/{shortKey}")
    public ResponseEntity<Void> deleteLink(@PathVariable String shortKey, @AuthenticationPrincipal Jwt jwt){
        shortenerService.deleteLink(shortKey, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }
}