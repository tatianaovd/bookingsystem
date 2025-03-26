package com.demo.bookingsystem.service;

import com.demo.bookingsystem.domain.entity.Booking;
import com.demo.bookingsystem.domain.entity.Payment;
import com.demo.bookingsystem.domain.enums.BookingStatus;
import com.demo.bookingsystem.domain.enums.EventType;
import com.demo.bookingsystem.listener.event.BookingCancelledEvent;
import com.demo.bookingsystem.repository.BookingRepository;
import com.demo.bookingsystem.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final EventService eventService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void processPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not pending");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaid(true);
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        eventService.logEvent(booking.getUnit(), EventType.PAYMENT_CONFIRMED);
    }

    // Auto cancel unpaid bookings older than 15 mins (runs every minute)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelUnpaidBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Booking> unpaid = bookingRepository.findUnpaidBefore(threshold);

        unpaid.forEach(b -> {
            b.setStatus(BookingStatus.CANCELLED);
            applicationEventPublisher.publishEvent(new BookingCancelledEvent(b));
            eventService.logEvent(b.getUnit(), EventType.CANCELLED);
        });

        bookingRepository.saveAll(unpaid);
    }
}