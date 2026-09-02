package com.gomovie.payment.provider.mobi.bank;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MobiBank(

        @JsonProperty("BankCode")
        String bankCode,

        @JsonProperty("BankName")
        String bankName,

        @JsonProperty("BankDisplayName")
        String bankDisplayName,

        @JsonProperty("Logo")
        String logo,

        @JsonProperty("DDBankCode")
        String ddBankCode,

        @JsonProperty("Active")
        String active

) {
}