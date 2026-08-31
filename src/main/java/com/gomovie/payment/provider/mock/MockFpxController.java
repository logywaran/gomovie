package com.gomovie.payment.provider.mock;

import com.gomovie.payment.Payment;
import com.gomovie.payment.PaymentRepository;
import com.gomovie.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gomovie.payment.PaymentService;


@RestController
@RequestMapping("/api/mock-fpx")
@RequiredArgsConstructor
public class MockFpxController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @GetMapping("/{transactionId}")
    public ResponseEntity<String> showPaymentPage(
            @PathVariable String transactionId) {

        Payment payment =
                paymentRepository.findByTransactionId(transactionId)
                        .orElseThrow(() ->
                                new RuntimeException("Payment not found")
                        );

        return ResponseEntity.ok(
                """
                MOCK FPX PAYMENT

                Transaction: %s
                Amount: RM %s

                Use the payment result endpoint to simulate SUCCESS or FAILED.
                """.formatted(
                        payment.getTransactionId(),
                        payment.getAmount()
                )
        );
    }

    @PostMapping("/{transactionId}/success")
    public ResponseEntity<String> success(
            @PathVariable String transactionId) {

        paymentService.handlePaymentSuccess(transactionId);

        return ResponseEntity.ok(
                "Payment successful for transaction: "
                        + transactionId
        );
    }

    @PostMapping("/{transactionId}/failed")
    public ResponseEntity<String> failed(
            @PathVariable String transactionId) {

        paymentService.handlePaymentFailure(transactionId);

        return ResponseEntity.ok(
                "Payment failed for transaction: "
                        + transactionId
        );
    }
}