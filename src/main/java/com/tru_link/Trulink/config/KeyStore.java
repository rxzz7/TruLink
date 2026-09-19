package com.tru_link.Trulink.config;

import com.tru_link.Trulink.repo.UrlRepo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class KeyStore {

    private final Set<String> keySet = ConcurrentHashMap.newKeySet();
    private final UrlRepo repo;

    public KeyStore(UrlRepo repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void loadKeys(){
        keySet.addAll(repo.findAllShortKeys());
    }

    public void addKey(String key){
        keySet.add(key);
    }
    public boolean contains(String key){
        return keySet.contains(key);
    }
    public void removeKey(String key){
        keySet.remove(key);
    }
}
