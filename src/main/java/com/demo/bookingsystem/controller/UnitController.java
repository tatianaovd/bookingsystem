package com.demo.bookingsystem.controller;

import com.demo.bookingsystem.domain.dto.UnitRequest;
import com.demo.bookingsystem.domain.entity.Unit;
import com.demo.bookingsystem.domain.searchcriteria.UnitSearchCriteria;
import com.demo.bookingsystem.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Units", description = "APIs for managing units")
@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;

    @Operation(summary = "Create a new unit", description = "Adds a new unit to the system.")
    @PostMapping("/add")
    public ResponseEntity<Unit> createUnit(@RequestBody UnitRequest unit) {
        return ResponseEntity.ok(unitService.createUnit(unit));
    }

    @Operation(summary = "Search for units", description = "Search for units based on criteria.")
    @GetMapping("/search")
    public Page<Unit> searchUnits(@ModelAttribute UnitSearchCriteria criteria) {
        return unitService.searchUnitWithCriteria(criteria);
    }
}
