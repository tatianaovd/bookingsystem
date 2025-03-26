package com.demo.bookingsystem.domain.dto;

import com.demo.bookingsystem.domain.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private Long id;
    private EventType eventType;
    private LocalDateTime createdAt;
}
