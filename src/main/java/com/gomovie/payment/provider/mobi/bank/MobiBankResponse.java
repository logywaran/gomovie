package com.gomovie.payment.provider.mobi.bank;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MobiBankResponse(

        String responseCode,

        String responseMessage,

        String responseDescription,

        @JsonProperty("responseDataB2C")
        MobiBankList responseDataB2C,

        @JsonProperty("responseDataB2B")
        MobiBankList responseDataB2B,

        @JsonProperty("responseDataBT")
        MobiBankTypeList responseDataBT

) {
}