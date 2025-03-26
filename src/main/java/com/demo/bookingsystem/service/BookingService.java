package com.demo.bookingsystem.service;

import com.demo.bookingsystem.domain.entity.Booking;
import com.demo.bookingsystem.domain.entity.Unit;
import com.demo.bookingsystem.domain.entity.User;
import com.demo.bookingsystem.domain.enums.BookingStatus;
import com.demo.bookingsystem.domain.enums.EventType;
import com.demo.bookingsystem.repository.BookingRepository;
import com.demo.bookingsystem.repository.UnitRepository;
import com.demo.bookingsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final UnitRepository unitRepository;
    private final EventService eventService;
    private final CacheService cacheService;

    @Transactional
    public Booking bookUnit(Long unitId, Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        if (!isUnitAvailable(unitId, startDate, endDate)) {
            throw new RuntimeException("Unit is already booked for this period");
        }

        Booking booking = new Booking();
        booking.setUnit(unit);
        booking.setUser(user);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        eventService.logEvent(unit, EventType.BOOKED);

        cacheService.invalidateAvailability(unitId, startDate, endDate);
        cacheService.invalidateAvailabilityCount(startDate, endDate);

        return savedBooking;
    }

    /**
     * Calculates available units by checking bookings in the date range
     */
    public int countAvailableUnits(LocalDate startDate, LocalDate endDate) {
        // Check the cache first
        Optional<Integer> cachedCount = cacheService.getCachedAvailabilityCount(startDate, endDate);
        if (cachedCount.isPresent()) {
            return cachedCount.get();
        }

        // If cache miss, query the DB for the available units
        List<Long> allUnitIds = unitRepository.findAllUnitIds();
        List<Long> bookedUnitIds = bookingRepository.findBookedUnitIdsBetween(startDate, endDate);
        Set<Long> bookedSet = new HashSet<>(bookedUnitIds);

        int availableCount = (int) allUnitIds.stream()
                .filter(unitId -> !bookedSet.contains(unitId))
                .count();

        // Cache the result
        cacheService.cacheAvailabilityCount(startDate, endDate, availableCount);

        return availableCount;
    }

    public boolean isUnitAvailable(Long unitId, LocalDate startDate, LocalDate endDate) {
        // Check the cache first
        Optional<Boolean> cachedAvailability = cacheService.getCachedAvailability(unitId, startDate, endDate);
        if (cachedAvailability.isPresent()) {
            return cachedAvailability.get();
        }

        // If not cached, perform DB check
        boolean available = bookingRepository.findConflictingBookings(unitId, startDate, endDate).isEmpty();

        // Cache the result
        cacheService.cacheAvailability(unitId, startDate, endDate, available);

        return available;
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalStateException("User is not allowed to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            eventService.logEvent(booking.getUnit(), EventType.CANCELLED);

            cacheService.invalidateAvailability(booking.getUnit().getId(), booking.getStartDate(), booking.getEndDate());
            cacheService.invalidateAvailabilityCount(booking.getStartDate(), booking.getEndDate());

        }
    }
}
