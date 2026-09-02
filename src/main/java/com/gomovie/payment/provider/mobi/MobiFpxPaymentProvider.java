package com.gomovie.payment.provider.mobi;

import com.gomovie.payment.Payment;
import com.gomovie.payment.provider.PaymentInitiationResult;
import com.gomovie.payment.provider.PaymentProvider;
import com.gomovie.payment.provider.mobi.deposit.MobiDepositRequest;
import com.gomovie.payment.provider.mobi.deposit.MobiDepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("mobiFpxPaymentProvider")
@RequiredArgsConstructor
public class MobiFpxPaymentProvider implements PaymentProvider {

    private final MobiDepositService mobiDepositService;

    @Override
    public PaymentInitiationResult initiatePayment(
            String transactionId,
            BigDecimal amount
    ) {

        MobiDepositRequest request =
                mobiDepositService.createDepositRequest(
                        amount.toPlainString(),
                        transactionId,
                        "GoMovie Customer",
                        null
                );

        String redirectHtml =
                mobiDepositService.buildAutoSubmitForm(request);

        return new PaymentInitiationResult(
                transactionId,
                null,
                redirectHtml
        );
    }

    @Override
    public void checkStatus(Payment payment) {

        // Mobi status-check integration will be implemented
        // after the redirect/IPN flow is completed.
    }
}