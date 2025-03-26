package com.demo.bookingsystem.domain.searchcriteria;

import com.demo.bookingsystem.domain.enums.AccommodationType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UnitSearchCriteria {
    private Integer minRooms;
    private Integer maxRooms;
    private AccommodationType type;
    private Integer floor;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double minCost;
    private Double maxCost;
    private int page = 0;
    private int size = 10;
    private String sortBy = "cost";
    private String sortDirection = "ASC";
}
