package com.demo.bookingsystem.domain.dto;

import com.demo.bookingsystem.domain.enums.AccommodationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitRequest {

    private int rooms;
    @Enumerated(EnumType.STRING)
    private AccommodationType type;
    private int floor;
    private double cost;
    private String description;
}
