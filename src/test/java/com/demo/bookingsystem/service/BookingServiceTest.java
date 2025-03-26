package com.demo.bookingsystem.service;

import com.demo.bookingsystem.domain.entity.Booking;
import com.demo.bookingsystem.domain.entity.Unit;
import com.demo.bookingsystem.domain.entity.User;
import com.demo.bookingsystem.domain.enums.BookingStatus;
import com.demo.bookingsystem.domain.enums.EventType;
import com.demo.bookingsystem.repository.BookingRepository;
import com.demo.bookingsystem.repository.UnitRepository;
import com.demo.bookingsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UnitRepository unitRepository;
    @Mock
    private EventService eventService;
    @Mock
    private CacheService cacheService;
    @InjectMocks
    private BookingService bookingService;

    private final Long unitId = 1L;
    private final Long userId = 10L;
    private final LocalDate startDate = LocalDate.now().plusDays(1);
    private final LocalDate endDate = LocalDate.now().plusDays(5);

    @Test
    void testBookUnit_successfulBooking() {
        User user = new User();
        user.setId(userId);
        Unit unit = new Unit();
        unit.setId(unitId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(bookingRepository.findConflictingBookings(unitId, startDate, endDate)).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.bookUnit(unitId, userId, startDate, endDate);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(unit, result.getUnit());
        assertEquals(BookingStatus.PENDING, result.getStatus());

        verify(eventService).logEvent(unit, EventType.BOOKED);
        verify(cacheService).invalidateAvailability(unitId, startDate, endDate);
        verify(cacheService).invalidateAvailabilityCount(startDate, endDate);
    }

    @Test
    void testBookUnit_unitUnavailable_throwsException() {
        User user = new User();
        user.setId(userId);
        Unit unit = new Unit();
        unit.setId(unitId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(bookingRepository.findConflictingBookings(unitId, startDate, endDate)).thenReturn(List.of(new Booking()));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.bookUnit(unitId, userId, startDate, endDate));

        assertEquals("Unit is already booked for this period", exception.getMessage());
    }

    @Test
    void testCountAvailableUnits_cacheHit() {
        when(cacheService.getCachedAvailabilityCount(startDate, endDate))
                .thenReturn(Optional.of(5));

        int count = bookingService.countAvailableUnits(startDate, endDate);

        assertEquals(5, count);
        verify(cacheService, never()).cacheAvailabilityCount(any(), any(), anyInt());
    }

    @Test
    void testCountAvailableUnits_cacheMiss() {
        when(cacheService.getCachedAvailabilityCount(startDate, endDate)).thenReturn(Optional.empty());
        when(unitRepository.findAllUnitIds()).thenReturn(List.of(1L, 2L, 3L));
        when(bookingRepository.findBookedUnitIdsBetween(startDate, endDate)).thenReturn(List.of(2L));

        int count = bookingService.countAvailableUnits(startDate, endDate);

        assertEquals(2, count);
        verify(cacheService).cacheAvailabilityCount(startDate, endDate, 2);
    }

    @Test
    void testIsUnitAvailable_cacheHit_true() {
        when(cacheService.getCachedAvailability(unitId, startDate, endDate))
                .thenReturn(Optional.of(true));

        boolean available = bookingService.isUnitAvailable(unitId, startDate, endDate);

        assertTrue(available);
        verify(bookingRepository, never()).findConflictingBookings(anyLong(), any(), any());
    }

    @Test
    void testIsUnitAvailable_cacheMissAndAvailable() {
        when(cacheService.getCachedAvailability(unitId, startDate, endDate)).thenReturn(Optional.empty());
        when(bookingRepository.findConflictingBookings(unitId, startDate, endDate)).thenReturn(Collections.emptyList());

        boolean available = bookingService.isUnitAvailable(unitId, startDate, endDate);

        assertTrue(available);
        verify(cacheService).cacheAvailability(unitId, startDate, endDate, true);
    }

    @Test
    void testCancelBooking_success() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUser(new User());
        booking.getUser().setId(userId);
        booking.setUnit(new Unit());
        booking.getUnit().setId(unitId);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(1L, userId);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(eventService).logEvent(booking.getUnit(), EventType.CANCELLED);
        verify(cacheService).invalidateAvailability(unitId, startDate, endDate);
        verify(cacheService).invalidateAvailabilityCount(startDate, endDate);
    }

    @Test
    void testCancelBooking_wrongUser_throwsException() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(new User());
        booking.getUser().setId(999L);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> bookingService.cancelBooking(1L, userId));

        assertEquals("User is not allowed to cancel this booking", ex.getMessage());
    }
}
