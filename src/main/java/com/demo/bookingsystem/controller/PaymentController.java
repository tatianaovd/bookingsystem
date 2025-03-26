package com.demo.bookingsystem.controller;

import com.demo.bookingsystem.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments", description = "APIs for processing payments")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "Process payment", description = "Processes a payment for a booking.")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    @PostMapping("/{bookingId}/pay")
    public ResponseEntity<Void> processPayment(
            @Parameter(description = "ID of the booking for payment") @PathVariable Long bookingId) {
        paymentService.processPayment(bookingId);

        return ResponseEntity.ok().build();
    }
}
