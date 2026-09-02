package com.gomovie.payment.provider.mobi.deposit;

import com.gomovie.payment.provider.mobi.MobiChecksumService;
import com.gomovie.payment.provider.mobi.MobiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobiDepositService {

    private final MobiProperties mobiProperties;
    private final MobiChecksumService mobiChecksumService;

    public MobiDepositRequest createDepositRequest(
            String amount,
            String sellerOrderNo,
            String buyerName,
            String email) {

        log.info(
                "Creating Mobi FPX deposit request. sellerOrderNo={}",
                sellerOrderNo
        );

        String checksum =
                mobiChecksumService.generateChecksum(
                        amount,
                        sellerOrderNo,
                        mobiProperties.getSubMid()
                );

        return new MobiDepositRequest(
                amount,
                mobiProperties.getRedirectUrl(),
                sellerOrderNo,
                "01",
                mobiProperties.getMid(),
                buyerName,
                mobiProperties.getTid(),
                mobiProperties.getMerchantName(),
                "TEST0021",
                "FULL_LIST",
                email,
                mobiProperties.getSubMid(),
                checksum
        );
    }

    public String buildAutoSubmitForm(MobiDepositRequest request) {

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Redirecting to Mobi</title>
            </head>
            <body>
                <p>Redirecting to payment...</p>

                <form id="mobiForm"
                      method="POST"
                      action="https://m-uat.gomobi.io/payment/DirectToFPX.aspx">

                    <input type="hidden" name="amount" value="%s">
                    <input type="hidden" name="redirectUrl" value="%s">
                    <input type="hidden" name="sellerOrderNo" value="%s">
                    <input type="hidden" name="bankType" value="%s">
                    <input type="hidden" name="mid" value="%s">
                    <input type="hidden" name="buyerName" value="%s">
                    <input type="hidden" name="tid" value="%s">
                    <input type="hidden" name="merchantName" value="%s">
                    <input type="hidden" name="bank" value="%s">
                    <input type="hidden" name="service" value="%s">
                    <input type="hidden" name="email" value="%s">
                    <input type="hidden" name="subMID" value="%s">
                    <input type="hidden" name="checkSum" value="%s">

                </form>

                <script>
                    document.getElementById("mobiForm").submit();
                </script>
            </body>
            </html>
            """.formatted(
                request.amount(),
                request.redirectUrl(),
                request.sellerOrderNo(),
                request.bankType(),
                request.mid(),
                request.buyerName(),
                request.tid(),
                request.merchantName(),
                request.bank(),
                request.service(),
                request.email() == null ? "" : request.email(),
                request.subMID() == null ? "" : request.subMID(),
                request.checkSum()
        );
    }
}