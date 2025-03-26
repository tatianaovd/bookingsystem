package com.demo.bookingsystem.controller;

import com.demo.bookingsystem.service.BookingService;
import com.demo.bookingsystem.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@Tag(name = "Statistics", description = "APIs for retrieving statistics")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final CacheService cacheService;
    private final BookingService bookingService;

    @Operation(summary = "Get available unit count", description = "Returns the number of available units for a given date range.")
    @GetMapping("/available-units/count")
    public ResponseEntity<Integer> getAvailableUnitsCount(
            @Parameter(description = "Start date for checking availability", schema = @Schema(type = "string", format = "date"))
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for checking availability", schema = @Schema(type = "string", format = "date"))
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Optional<Integer> cachedCount = cacheService.getCachedAvailabilityCount(startDate, endDate);

        int availableCount;
        if (cachedCount.isPresent()) {
            availableCount = cachedCount.get();
        } else {
            availableCount = bookingService.countAvailableUnits(startDate, endDate);
            cacheService.cacheAvailabilityCount(startDate, endDate, availableCount);
        }

        return ResponseEntity.ok(availableCount);
    }

    @DeleteMapping("/available-units/cache")
    public ResponseEntity<String> resetAvailableUnitsCache(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        cacheService.invalidateAvailabilityCount(startDate, endDate);
        return ResponseEntity.ok("Cache cleared for the period: " + startDate + " to " + endDate);
    }

}
