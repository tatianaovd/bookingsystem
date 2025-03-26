package com.demo.bookingsystem.listener.event;

import com.demo.bookingsystem.domain.entity.Booking;

public record  BookingCancelledEvent(Booking booking) {
}
