package com.gomovie.payment.provider.mobi.status;

public record MobiStatusRequest(
        String service,
        String motoApiKey,
        String loginId,
        String trxType,
        String searchKey
) {
}