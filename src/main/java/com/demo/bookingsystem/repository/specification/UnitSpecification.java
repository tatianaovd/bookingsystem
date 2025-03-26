package com.demo.bookingsystem.repository.specification;

import com.demo.bookingsystem.domain.entity.Booking;
import com.demo.bookingsystem.domain.entity.Unit;
import com.demo.bookingsystem.domain.enums.AccommodationType;
import com.demo.bookingsystem.domain.enums.BookingStatus;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class UnitSpecification {
    public static Specification<Unit> hasRooms(Integer rooms) {
        return (root, query, criteriaBuilder) ->
                rooms == null ? null : criteriaBuilder.equal(root.get("rooms"), rooms);
    }

    public static Specification<Unit> hasAccommodationType(AccommodationType type) {
        return (root, query, criteriaBuilder) ->
                type == null ? null : criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<Unit> hasFloor(Integer floor) {
        return (root, query, criteriaBuilder) ->
                floor == null ? null : criteriaBuilder.equal(root.get("floor"), floor);
    }

    public static Specification<Unit> costLessThanOrEqual(Double maxCostWithMarkup) {
        return (root, query, cb) ->
                maxCostWithMarkup == null ? null : cb.lessThanOrEqualTo(root.get("cost"), maxCostWithMarkup);
    }

    public static Specification<Unit> availableInDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null || endDate == null) return null;
            // Subquery to exclude units booked in this range
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Booking> booking = subquery.from(Booking.class);
            subquery.select(booking.get("unit").get("id"));
            subquery.where(
                    cb.and(
                            cb.equal(booking.get("unit").get("id"), root.get("id")),
                            cb.or(
                                    cb.between(cb.literal(startDate), booking.get("startDate"), booking.get("endDate")),
                                    cb.between(cb.literal(endDate), booking.get("startDate"), booking.get("endDate")),
                                    cb.and(
                                            cb.lessThanOrEqualTo(booking.get("startDate"), startDate),
                                            cb.greaterThanOrEqualTo(booking.get("endDate"), endDate)
                                    )
                            ),
                            cb.notEqual(booking.get("status"), BookingStatus.CANCELLED)
                    )
            );
            return cb.not(cb.exists(subquery));
        };
    }
}
