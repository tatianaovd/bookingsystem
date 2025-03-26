package com.demo.bookingsystem.controller;

import com.demo.bookingsystem.domain.dto.EventResponse;
import com.demo.bookingsystem.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Events", description = "APIs for managing events related to units")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Get events for a unit", description = "Fetches all events for a given unit.")
    @ApiResponse(responseCode = "200", description = "List of events retrieved successfully")
    @GetMapping("/unit/{unitId}")
    public List<EventResponse> getUnitEvents(
            @Parameter(description = "ID of the unit") @PathVariable Long unitId) {
        return eventService.getEventsByUnitId(unitId);
    }
}
