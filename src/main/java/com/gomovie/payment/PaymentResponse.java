package com.gomovie.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(

        Long id,

        Long bookingId,

        String transactionId,

        String paymentUrl,

        BigDecimal amount,

        PaymentStatus status,

        PaymentMethod paymentMethod,

        String provider,

        String failureReason,

        LocalDateTime createdAt

) {
}