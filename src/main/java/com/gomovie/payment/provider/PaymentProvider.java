package com.gomovie.payment.provider;

import com.gomovie.payment.Payment;

import java.math.BigDecimal;

public interface PaymentProvider {

    PaymentInitiationResult initiatePayment(
            String transactionId,
            BigDecimal amount
    );

    void checkStatus(Payment payment);
}