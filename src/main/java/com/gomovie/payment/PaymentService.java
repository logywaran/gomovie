package com.gomovie.payment;

public interface PaymentService {

    PaymentResponse initiatePayment(PaymentRequest request);

    void handlePaymentSuccess(String transactionId);

    void handlePaymentFailure(String transactionId);

}