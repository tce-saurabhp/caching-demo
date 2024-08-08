package com.tce.cache.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TestController {

    private final Cache cache;

    @GetMapping("/test/{tpId}")
    public String test(@PathVariable final String tpId) {
        if (cache.get(tpId) != null) {
            log.info("Cache hit for tpId: {}", tpId);
            return "cache hit";
        }
        log.info("Cache miss for tpId: {}", tpId);
        cache.put(tpId, "Success");
        return "cache miss";
    }

}
