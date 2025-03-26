package com.demo.bookingsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Integer> redisTemplate;
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    public Optional<Integer> getCachedAvailabilityCount(LocalDate startDate, LocalDate endDate) {
        String key = buildKey(startDate, endDate);
        Integer cached = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(cached);
    }

    public void cacheAvailabilityCount(LocalDate startDate, LocalDate endDate, int count) {
        String key = buildKey(startDate, endDate);
        redisTemplate.opsForValue().set(key, count, CACHE_TTL);
    }

    public void invalidateAvailabilityCount(LocalDate startDate, LocalDate endDate) {
        String key = buildKey(startDate, endDate);
        redisTemplate.delete(key);
    }

    private String buildKey(LocalDate startDate, LocalDate endDate) {
        return "availability_count:" + startDate + ":" + endDate;
    }

    public Optional<Boolean> getCachedAvailability(Long unitId, LocalDate startDate, LocalDate endDate) {
        String key = buildUnitAvailabilityKey(unitId, startDate, endDate);
        Object cached = redisTemplate.opsForValue().get(key);
        return cached instanceof Boolean ? Optional.of((Boolean) cached) : Optional.empty();
    }

    public void cacheAvailability(Long unitId, LocalDate startDate, LocalDate endDate, boolean available) {
        String key = buildUnitAvailabilityKey(unitId, startDate, endDate);
        redisTemplate.opsForValue().set(key, available ? 1 : 0, CACHE_TTL);
    }

    public void invalidateAvailability(Long unitId, LocalDate startDate, LocalDate endDate) {
        String key = buildUnitAvailabilityKey(unitId, startDate, endDate);
        redisTemplate.delete(key);
    }

    private String buildUnitAvailabilityKey(Long unitId, LocalDate startDate, LocalDate endDate) {
        return "unit_availability:" + unitId + ":" + startDate + ":" + endDate;
    }
}
