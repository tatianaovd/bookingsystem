package com.demo.bookingsystem.service;

import com.demo.bookingsystem.domain.dto.UnitRequest;
import com.demo.bookingsystem.domain.entity.Unit;
import com.demo.bookingsystem.domain.enums.AccommodationType;
import com.demo.bookingsystem.domain.enums.EventType;
import com.demo.bookingsystem.domain.searchcriteria.UnitSearchCriteria;
import com.demo.bookingsystem.listener.event.UnitCreatedEvent;
import com.demo.bookingsystem.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;
    @Mock
    private EventService eventService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private UnitService unitService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(unitService, "systemMarkupPercentage", 0.15);
    }

    @Test
    void testCreateUnit_shouldCalculateCostAndSaveAndPublishEvent() {
        UnitRequest request = UnitRequest.builder()
                .cost(1000)
                .rooms(3)
                .floor(2)
                .description("Nice unit")
                .type(AccommodationType.APARTMENTS)
                .build();

        Unit savedUnit = new Unit();
        savedUnit.setId(1L);
        savedUnit.setCost(1150);
        savedUnit.setRooms(3);
        savedUnit.setFloor(2);
        savedUnit.setDescription("Nice unit");
        savedUnit.setType(AccommodationType.APARTMENTS);

        when(unitRepository.save(any(Unit.class))).thenReturn(savedUnit);

        Unit result = unitService.createUnit(request);

        assertNotNull(result);
        assertEquals(1150, result.getCost());
        assertEquals(3, result.getRooms());
        assertEquals(2, result.getFloor());
        assertEquals("Nice unit", result.getDescription());
        assertEquals(AccommodationType.APARTMENTS, result.getType());

        verify(unitRepository).save(any(Unit.class));
        verify(eventService).logEvent(savedUnit, EventType.UNIT_CREATED);
        verify(applicationEventPublisher).publishEvent(any(UnitCreatedEvent.class));
    }

    @Test
    void testSearchUnitWithMinCost() {
        UnitSearchCriteria criteria = new UnitSearchCriteria();
        criteria.setMaxCost(200.0);
        criteria.setMinRooms(2);
        criteria.setSortBy("cost");
        criteria.setSortDirection("ASC");

        List<Unit> units = new ArrayList<>();
        units.add(Unit.builder()
                .id(1L)
                .cost(200.00)
                .floor(2)
                .rooms(2)
                .type(AccommodationType.HOME)
                .build());

        Page<Unit> mockPage = new PageImpl<>(units);
        when(unitRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Unit> resultPage = unitService.searchUnitWithCriteria(criteria);

        assertNotNull(resultPage);
        verify(unitRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
