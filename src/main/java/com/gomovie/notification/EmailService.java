package com.gomovie.notification;

public interface EmailService {

    void sendBookingConfirmation(Long bookingId);
}