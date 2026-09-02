package com.gomovie.payment.provider.mobi.deposit;

public record MobiDepositRequest(

        String amount,

        String redirectUrl,

        String sellerOrderNo,

        String bankType,

        String mid,

        String buyerName,

        String tid,

        String merchantName,

        String bank,

        String service,

        String email,

        String subMID,

        String checkSum

) {
}