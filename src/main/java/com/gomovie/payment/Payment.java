package com.gomovie.payment;

import com.gomovie.booking.Booking;
import com.gomovie.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_transaction_id",
                        columnNames = "transaction_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "booking_id",
            nullable = false,
            unique = true
    )
    private Booking booking;

    @Column(
            name = "transaction_id",
            nullable = false,
            unique = true,
            length = 255
    )
    private String transactionId;

    @Column(
            name = "payment_url",
            length = 500
    )
    private String paymentUrl;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false,
            length = 50
    )
    private PaymentMethod paymentMethod;



    @Column(
            nullable = false,
            length = 50
    )
    private String provider;

    @Column(
            name = "failure_reason",
            length = 500
    )
    private String failureReason;
}