package com.demo.bookingsystem.repository;

import com.demo.bookingsystem.domain.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt < :time")
    List<Booking> findUnpaidBefore(LocalDateTime time);

    @Query("SELECT b FROM Booking b WHERE b.unit.id = :unitId AND b.status IN ('PENDING', 'CONFIRMED') AND " +
            "((b.startDate <= :endDate) AND (b.endDate >= :startDate))")
    List<Booking> findConflictingBookings(@Param("unitId") Long unitId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT b.unit.id FROM Booking b WHERE b.startDate <= :endDate AND b.endDate >= :startDate")
    List<Long> findBookedUnitIdsBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
