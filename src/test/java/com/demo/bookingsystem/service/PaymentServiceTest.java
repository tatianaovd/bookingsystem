package com.demo.bookingsystem.service;

import com.demo.bookingsystem.domain.entity.Booking;
import com.demo.bookingsystem.domain.entity.Payment;
import com.demo.bookingsystem.domain.entity.Unit;
import com.demo.bookingsystem.domain.enums.BookingStatus;
import com.demo.bookingsystem.domain.enums.EventType;
import com.demo.bookingsystem.listener.event.BookingCancelledEvent;
import com.demo.bookingsystem.repository.BookingRepository;
import com.demo.bookingsystem.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private EventService eventService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks
    private PaymentService paymentService;

    @Test
    void testProcessPayment_successfulPayment() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.PENDING);
        Unit unit = new Unit();
        booking.setUnit(unit);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        paymentService.processPayment(1L);

        verify(paymentRepository).save(any(Payment.class));
        verify(bookingRepository).save(booking);
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(eventService).logEvent(unit, EventType.PAYMENT_CONFIRMED);
    }

    @Test
    void testProcessPayment_bookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.processPayment(1L));
        assertEquals("Booking not found", ex.getMessage());
    }

    @Test
    void testProcessPayment_invalidBookingStatus() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> paymentService.processPayment(1L));
        assertEquals("Booking is not pending", ex.getMessage());
    }

    @Test
    void testAutoCancelUnpaidBookings() {
        Booking unpaidBooking = new Booking();
        unpaidBooking.setId(1L);
        unpaidBooking.setStatus(BookingStatus.PENDING);
        Unit unit = new Unit();
        unpaidBooking.setUnit(unit);

        when(bookingRepository.findUnpaidBefore(any())).thenReturn(List.of(unpaidBooking));

        paymentService.autoCancelUnpaidBookings();

        assertEquals(BookingStatus.CANCELLED, unpaidBooking.getStatus());
        verify(applicationEventPublisher).publishEvent(any(BookingCancelledEvent.class));
        verify(eventService).logEvent(unit, EventType.CANCELLED);
        verify(bookingRepository).saveAll(List.of(unpaidBooking));
    }
}
