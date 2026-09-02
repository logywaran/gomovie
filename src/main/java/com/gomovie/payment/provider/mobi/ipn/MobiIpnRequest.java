package com.gomovie.payment.provider.mobi.ipn;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MobiIpnRequest(

        @JsonProperty("transaction_date")
        String transactionDate,

        @JsonProperty("transaction_amount")
        String transactionAmount,

        @JsonProperty("transaction_id")
        String transactionId,

        @JsonProperty("transaction_order")
        String transactionOrder,

        @JsonProperty("transaction_exorder")
        String transactionExOrder,

        @JsonProperty("transaction_description")
        String transactionDescription,

        @JsonProperty("transaction_buyer")
        String transactionBuyer,

        @JsonProperty("transaction_bank")
        String transactionBank,

        @JsonProperty("transaction_code")
        String transactionCode,

        @JsonProperty("transaction_callBack")
        String transactionCallback,

        @JsonProperty("transaction_type")
        String transactionType,

        @JsonProperty("transaction_signature")
        String transactionSignature

) {
}