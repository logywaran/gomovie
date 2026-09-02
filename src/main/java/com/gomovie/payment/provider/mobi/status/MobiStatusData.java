package com.gomovie.payment.provider.mobi.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MobiStatusData(

        @JsonProperty("forSettlement")
        MobiTransactionStatus forSettlement

) {
}