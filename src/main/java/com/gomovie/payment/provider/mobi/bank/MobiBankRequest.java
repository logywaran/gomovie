package com.gomovie.payment.provider.mobi.bank;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MobiBankRequest(
        @JsonProperty("Service")
        String service
) {
}