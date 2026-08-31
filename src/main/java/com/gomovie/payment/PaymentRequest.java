package com.gomovie.payment;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(

        @NotNull(message = "Booking ID is required")
        Long bookingId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod

) {
}