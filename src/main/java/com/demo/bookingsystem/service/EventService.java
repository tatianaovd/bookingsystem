package com.demo.bookingsystem.service;

import com.demo.bookingsystem.domain.dto.EventResponse;
import com.demo.bookingsystem.domain.entity.Event;
import com.demo.bookingsystem.domain.entity.Unit;
import com.demo.bookingsystem.domain.enums.EventType;
import com.demo.bookingsystem.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void logEvent(Unit unit, EventType eventType) {
        Event event = Event.builder()
                .unit(unit)
                .eventType(eventType)
                .createdAt(LocalDateTime.now())
                .build();
        eventRepository.save(event);
    }

    public List<EventResponse> getEventsByUnitId(Long unitId) {
        return eventRepository.findByUnitId(unitId).stream()
                .map(e -> new EventResponse(e.getId(), e.getEventType(), e.getCreatedAt()))
                .collect(Collectors.toList());
    }

}
