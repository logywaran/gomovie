package com.gomovie.payment.provider;

public record PaymentInitiationResult(

        String providerTransactionId,

        String paymentUrl

) {
}