package com.demo.bookingsystem.controller;

import com.demo.bookingsystem.domain.entity.Booking;
import com.demo.bookingsystem.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Bookings", description = "APIs for booking management")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @Operation(summary = "Book a unit", description = "Allows users to book a unit for a specified period.")
    @ApiResponse(responseCode = "200", description = "Booking successful")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PostMapping("/{unitId}/book")
    public ResponseEntity<Booking> bookUnit(
            @Parameter(description = "ID of the unit to book") @PathVariable Long unitId,
            @Parameter(description = "User ID making the booking") @RequestParam Long userId,
            @Parameter(description = "Start date of the booking", schema = @Schema(type = "string", format = "date"))
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date of the booking", schema = @Schema(type = "string", format = "date"))
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(bookingService.bookUnit(unitId, userId, startDate, endDate));
    }

    @Operation(summary = "Cancel a booking", description = "Cancels an existing booking.")
    @ApiResponse(responseCode = "200", description = "Booking cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelUnit(
            @Parameter(description = "ID of the booking to cancel") @PathVariable Long bookingId,
            @Parameter(description = "User ID making the cancellation") @RequestParam Long userId) {
        bookingService.cancelBooking(bookingId, userId);

        return ResponseEntity.ok().build();
    }
}
