package com.gomovie.payment.provider;

import java.math.BigDecimal;

public record PaymentInitiationRequest(
        String sellerOrderNo,
        BigDecimal amount,
        String redirectUrl,
        String bankType,
        String buyerName,
        String bank,
        String email
) {
}