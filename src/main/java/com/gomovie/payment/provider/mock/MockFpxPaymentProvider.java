package com.gomovie.payment.provider.mock;

import com.gomovie.payment.Payment;
import com.gomovie.payment.provider.PaymentInitiationResult;
import com.gomovie.payment.provider.PaymentProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MockFpxPaymentProvider implements PaymentProvider {

    @Override
    public PaymentInitiationResult initiatePayment(
            String transactionId,
            BigDecimal amount
    ) {

        return new PaymentInitiationResult(
                transactionId,
                "/api/mock-fpx/" + transactionId
        );
    }

    @Override
    public void checkStatus(Payment payment) {

        // Payment status will be handled by the mock FPX flow.
    }
}