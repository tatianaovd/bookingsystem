package com.demo.bookingsystem.domain.entity;

import com.demo.bookingsystem.domain.enums.AccommodationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "units")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rooms;

    @Enumerated(EnumType.STRING)
    private AccommodationType type;

    private int floor;

    private double cost;

    private String description;
}
