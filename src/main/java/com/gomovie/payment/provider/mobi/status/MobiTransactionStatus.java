package com.gomovie.payment.provider.mobi.status;

public record MobiTransactionStatus(

        String trxId,
        String mid,
        String tid,
        String status,
        String date,
        String time,
        String stan,
        String rrn,
        String latitude,
        String longitude,
        String amount,
        String invoiceId,
        String txnId,
        String txnType,
        String hostType,
        String aidResponse

) {
}