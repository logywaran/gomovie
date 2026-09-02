package com.gomovie.payment.provider.mobi.deposit;

import com.gomovie.common.exception.ResourceNotFoundException;
import com.gomovie.payment.Payment;
import com.gomovie.payment.PaymentRepository;
import com.gomovie.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobiRedirectService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public void processRedirect(Map<String, String> params) {

        String mobiSellerOrderNo =
                params.get("fpx_sellerOrderNo");

        String debitAuthCode =
                params.get("fpx_debitAuthCode");

        String debitAuthCodeString =
                params.get("fpx_debitAuthCodeString");

        String fpxTransactionId =
                params.get("fpx_fpxTxnId");

        log.info(
                "Processing Mobi redirect. " +
                        "mobiSellerOrderNo={}, fpxTransactionId={}, " +
                        "debitAuthCode={}, debitAuthCodeString={}",
                mobiSellerOrderNo,
                fpxTransactionId,
                debitAuthCode,
                debitAuthCodeString
        );

        if (mobiSellerOrderNo == null
                || mobiSellerOrderNo.isBlank()) {

            log.error(
                    "Mobi redirect does not contain fpx_sellerOrderNo"
            );

            throw new IllegalStateException(
                    "Mobi seller order number is missing"
            );
        }

        Payment payment =
                findPaymentByMobiSellerOrderNo(
                        mobiSellerOrderNo
                );

        String mobiAmount =
                params.get("fpx_txnAmount");

        if (mobiAmount == null || mobiAmount.isBlank()) {

            log.error(
                    "Mobi redirect does not contain transaction amount. " +
                            "transactionId={}",
                    payment.getTransactionId()
            );

            throw new IllegalStateException(
                    "Mobi transaction amount is missing"
            );
        }

        BigDecimal expectedAmount =
                payment.getAmount();

        BigDecimal receivedAmount;

        try {

            receivedAmount =
                    new BigDecimal(mobiAmount);

        } catch (NumberFormatException ex) {

            log.error(
                    "Invalid Mobi transaction amount. " +
                            "transactionId={}, amount={}",
                    payment.getTransactionId(),
                    mobiAmount
            );

            throw new IllegalStateException(
                    "Invalid Mobi transaction amount"
            );
        }

        if (expectedAmount.compareTo(receivedAmount) != 0) {

            log.error(
                    "Mobi payment amount mismatch. " +
                            "transactionId={}, expectedAmount={}, receivedAmount={}",
                    payment.getTransactionId(),
                    expectedAmount,
                    receivedAmount
            );

            throw new IllegalStateException(
                    "Mobi payment amount does not match booking amount"
            );
        }

        String transactionId =
                payment.getTransactionId();

        /*
         * FPX debitAuthCode = 00 means the transaction
         * was approved successfully.
         */
        if ("00".equals(debitAuthCode)) {

            log.info(
                    "Mobi payment approved. transactionId={}, fpxTransactionId={}",
                    transactionId,
                    fpxTransactionId
            );

            paymentService.handlePaymentSuccess(
                    transactionId
            );

            return;
        }

        log.warn(
                "Mobi payment failed. transactionId={}, " +
                        "debitAuthCode={}, reason={}",
                transactionId,
                debitAuthCode,
                debitAuthCodeString
        );

        paymentService.handlePaymentFailure(
                transactionId
        );
    }

    private Payment findPaymentByMobiSellerOrderNo(
            String mobiSellerOrderNo) {

        String normalizedMobiOrderNo =
                normalizeMobiOrderNo(mobiSellerOrderNo);

        log.debug(
                "Searching payment using normalized Mobi sellerOrderNo={}",
                normalizedMobiOrderNo
        );

        return paymentRepository.findAll()
                .stream()
                .filter(payment ->
                        normalizeOurTransactionId(
                                payment.getTransactionId()
                        ).equals(normalizedMobiOrderNo)
                )
                .findFirst()
                .orElseThrow(() -> {

                    log.error(
                            "Unable to find payment for Mobi sellerOrderNo={}",
                            mobiSellerOrderNo
                    );

                    return new ResourceNotFoundException(
                            "Payment not found for Mobi seller order: "
                                    + mobiSellerOrderNo
                    );
                });
    }

    private String normalizeOurTransactionId(
            String transactionId) {

        return transactionId
                .replace("-", "")
                .toUpperCase();
    }

    private String normalizeMobiOrderNo(
            String mobiSellerOrderNo) {

        String normalized =
                mobiSellerOrderNo
                        .replace("-", "")
                        .toUpperCase();

        if (normalized.endsWith("_MO")) {

            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 3
                    );
        }

        return normalized;
    }
}