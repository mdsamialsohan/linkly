package com.samialsohan.linkly.service;

import com.samialsohan.linkly.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class UrlCacheService {
    private static final Logger log = LoggerFactory.getLogger(UrlCacheService.class);
    private static final String KEY_PREFIX = "url:";
    private static final String NOT_FOUND_SENTINEL = "NOT_FOUND";
    private final StringRedisTemplate redis;
    private final Duration ttl;
    private final Duration negativeTtl;


    public UrlCacheService(
            StringRedisTemplate redis,
            @Value("${linkly.cache.ttl-hours:24}") long ttlHours,
            @Value("${linkly.cache.negative-ttl-minutes:5}") long negativeTtlMinutes
    )
    {
        this.redis = redis;
        this.ttl = Duration.ofHours(ttlHours);
        this.negativeTtl = Duration.ofMinutes(negativeTtlMinutes);
    }
    public CacheLookup lookup(String shortCode)
    {
        try{
            String value = redis.opsForValue().get(KEY_PREFIX + shortCode);
            if(value == null)
            {
                return new CacheLookup.Miss();
            }
            if(NOT_FOUND_SENTINEL.equals(value))
            {
                return new CacheLookup.Hit(value);
            }
            return new CacheLookup.Hit(value);
        } catch (DataAccessException e){
            log.warn("Redis lookup failed for '{}', treating as miss: {}", shortCode, e.getMessage());
            return new CacheLookup.Miss();
        }
    }

    public void storeNotFound(String shortCode)
    {
        try{
            redis.opsForValue().set(KEY_PREFIX + shortCode, NOT_FOUND_SENTINEL, negativeTtl);
        }
        catch(DataAccessException e)
        {
            log.warn("Redis store failed for '{}': {}", shortCode, e.getMessage());
        }
    }
    public void store(String shortCode, String longUrl)
    {
        try{
            redis.opsForValue().set(KEY_PREFIX + shortCode, longUrl, ttl);
        }
        catch (DataAccessException e)
        {
            log.warn("Redis store failed for '{}': {}", shortCode, e.getMessage());
        }
    }
}
