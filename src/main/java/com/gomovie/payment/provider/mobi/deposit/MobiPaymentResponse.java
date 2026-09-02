package com.gomovie.payment.provider.mobi.deposit;

public record MobiPaymentResponse(

        String fpxMsgToken,
        String fpxFpxTxnId,
        String fpxSellerExOrderNo,
        String fpxFpxTxnTime,
        String fpxSellerTxnTime,
        String fpxSellerOrderNo,
        String fpxTxnCurrency,
        String fpxTxnAmount,
        String fpxBuyerName,
        String fpxBuyerBankId,
        String fpxBuyerId,
        String fpxMakerName,
        String fpxDebitAuthCode,
        String fpxDebitAuthCodeString,
        String fpxDebitAuthNo,
        String fpxCreditAuthCode,
        String fpxCreditAuthCodeString,
        String fpxCreditAuthNo,
        String bankName,
        String tid,
        String mobiLink,
        String redirectUrl,
        String merchantName,
        String date,
        String time,
        String fpxMobiliteTID
) {
}