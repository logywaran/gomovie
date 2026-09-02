package com.gomovie.payment.provider.mobi.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MobiStatusResponse(

        String responseCode,

        String responseMessage,

        String responseDescription,

        @JsonProperty("responseData")
        MobiStatusData responseData

) {
}