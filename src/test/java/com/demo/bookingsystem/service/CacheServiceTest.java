package com.demo.bookingsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Integer> redisTemplate;
    @Mock
    private ValueOperations<String, Integer> valueOperations;
    @InjectMocks
    private CacheService cacheService;

    private final LocalDate startDate = LocalDate.of(2024, 3, 1);
    private final LocalDate endDate = LocalDate.of(2024, 3, 10);
    private final Long unitId = 100L;

    @Test
    void testGetCachedAvailabilityCount_returnsCachedValue() {
        String key = "availability_count:" + startDate + ":" + endDate;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(5);

        Optional<Integer> result = cacheService.getCachedAvailabilityCount(startDate, endDate);

        assertTrue(result.isPresent());
        assertEquals(5, result.get());
        verify(valueOperations).get(key);
    }

    @Test
    void testGetCachedAvailabilityCount_returnsEmptyIfNotCached() {
        String key = "availability_count:" + startDate + ":" + endDate;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        Optional<Integer> result = cacheService.getCachedAvailabilityCount(startDate, endDate);

        assertTrue(result.isEmpty());
    }

    @Test
    void testCacheAvailabilityCount_savesValueWithTTL() {
        String key = "availability_count:" + startDate + ":" + endDate;
        int count = 7;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.cacheAvailabilityCount(startDate, endDate, count);

        verify(valueOperations).set(eq(key), eq(count), eq(Duration.ofHours(1)));
    }

    @Test
    void testInvalidateAvailabilityCount_deletesCacheKey() {
        String key = "availability_count:" + startDate + ":" + endDate;

        cacheService.invalidateAvailabilityCount(startDate, endDate);

        verify(redisTemplate).delete(key);
    }

    @Test
    void testCacheAvailability_savesAvailabilityWithTTL_trueAndFalse() {
        String key = "unit_availability:" + unitId + ":" + startDate + ":" + endDate;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.cacheAvailability(unitId, startDate, endDate, true);
        verify(valueOperations).set(eq(key), eq(1), eq(Duration.ofHours(1)));

        reset(valueOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        cacheService.cacheAvailability(unitId, startDate, endDate, false);
        verify(valueOperations).set(eq(key), eq(0), eq(Duration.ofHours(1)));
    }

    @Test
    void testInvalidateAvailability_deletesUnitCacheKey() {
        String key = "unit_availability:" + unitId + ":" + startDate + ":" + endDate;

        cacheService.invalidateAvailability(unitId, startDate, endDate);

        verify(redisTemplate).delete(key);
    }
}
