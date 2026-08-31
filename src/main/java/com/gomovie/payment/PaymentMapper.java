package com.gomovie.payment;

import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getTransactionId(),
                payment.getPaymentUrl(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getProvider(),
                payment.getFailureReason(),
                payment.getCreatedAt()
        );
    }
}