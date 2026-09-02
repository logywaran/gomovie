package com.gomovie.payment.provider.mobi.bank;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MobiBankType(

        @JsonProperty("ID")
        Integer id,

        @JsonProperty("Name")
        String name,

        @JsonProperty("Active")
        String active,

        @JsonProperty("BankTypeCode")
        String bankTypeCode

) {
}