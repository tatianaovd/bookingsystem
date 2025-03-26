package com.demo.bookingsystem.service;

import com.demo.bookingsystem.domain.dto.EventResponse;
import com.demo.bookingsystem.domain.entity.Event;
import com.demo.bookingsystem.domain.entity.Unit;
import com.demo.bookingsystem.domain.enums.EventType;
import com.demo.bookingsystem.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @InjectMocks
    private EventService eventService;

    @Test
    void testLogEvent_savesEvent() {
        Unit unit = new Unit();
        EventType eventType = EventType.PAYMENT_CONFIRMED;

        eventService.logEvent(unit, eventType);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertEquals(unit, savedEvent.getUnit());
        assertEquals(eventType, savedEvent.getEventType());
        assertNotNull(savedEvent.getCreatedAt());
    }

    @Test
    void testGetEventsByUnitId_returnsMappedEvents() {
        Event event = new Event(1L, new Unit(), EventType.CANCELLED, LocalDateTime.now());
        when(eventRepository.findByUnitId(1L)).thenReturn(List.of(event));

        List<EventResponse> responses = eventService.getEventsByUnitId(1L);

        assertEquals(1, responses.size());
        assertEquals(event.getId(), responses.get(0).getId());
        assertEquals(event.getEventType(), responses.get(0).getEventType());
        assertEquals(event.getCreatedAt(), responses.get(0).getCreatedAt());
    }
}
