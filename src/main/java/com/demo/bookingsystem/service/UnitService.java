package com.demo.bookingsystem.service;

import com.demo.bookingsystem.domain.dto.UnitRequest;
import com.demo.bookingsystem.domain.entity.Unit;
import com.demo.bookingsystem.domain.enums.EventType;
import com.demo.bookingsystem.listener.event.UnitCreatedEvent;
import com.demo.bookingsystem.repository.UnitRepository;
import com.demo.bookingsystem.domain.searchcriteria.UnitSearchCriteria;
import com.demo.bookingsystem.repository.specification.UnitSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnitService {

    @Value("${unit.markup-percentage}")
    private double systemMarkupPercentage;

    private final UnitRepository unitRepository;
    private final EventService eventService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Unit createUnit(UnitRequest unitRequest) {
        double finalCost = unitRequest.getCost() + (unitRequest.getCost() * systemMarkupPercentage);

        Unit unit = new Unit();
        unit.setCost(finalCost);
        unit.setRooms(unitRequest.getRooms());
        unit.setFloor(unitRequest.getFloor());
        unit.setDescription(unitRequest.getDescription());
        unit.setType(unitRequest.getType());

        Unit savedUnit = unitRepository.save(unit);
        eventService.logEvent(savedUnit, EventType.UNIT_CREATED);
        applicationEventPublisher.publishEvent(new UnitCreatedEvent(savedUnit));

        return savedUnit;
    }

    public Page<Unit> searchUnitWithCriteria(UnitSearchCriteria criteria) {
        Pageable pageable = PageRequest.of(
                criteria.getPage(),
                criteria.getSize(),
                Sort.by(Sort.Direction.fromString(criteria.getSortDirection()), criteria.getSortBy())
        );
        Specification<Unit> specification = buildSpecification(criteria);

        return unitRepository.findAll(specification, pageable);
    }

    public static Specification<Unit> buildSpecification(UnitSearchCriteria criteria) {
        Specification<Unit> spec = Specification.where(null);

        if (criteria.getMinRooms() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rooms"), criteria.getMinRooms()));
        }
        if (criteria.getMaxRooms() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("rooms"), criteria.getMaxRooms()));
        }
        if (criteria.getMinCost() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("cost"), criteria.getMinCost()));
        }
        if (criteria.getMaxCost() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("cost"), criteria.getMaxCost()));
        }


        spec = spec.and(UnitSpecification.hasAccommodationType(criteria.getType()));
        spec = spec.and(UnitSpecification.hasFloor(criteria.getFloor()));
        spec = spec.and(UnitSpecification.availableInDateRange(criteria.getStartDate(), criteria.getEndDate()));

        return spec;
    }
}
