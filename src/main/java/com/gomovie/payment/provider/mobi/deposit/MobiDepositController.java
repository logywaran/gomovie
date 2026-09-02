package com.gomovie.payment.provider.mobi.deposit;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/mobi")
@RequiredArgsConstructor
public class MobiDepositController {

    private final MobiDepositService mobiDepositService;

    @PostMapping(
            value = "/deposit",
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> initiateDeposit(
            @RequestParam String amount,
            @RequestParam String sellerOrderNo,
            @RequestParam String buyerName,
            @RequestParam(required = false) String email) {

        MobiDepositRequest request =
                mobiDepositService.createDepositRequest(
                        amount,
                        sellerOrderNo,
                        buyerName,
                        email
                );

        String html = mobiDepositService.buildAutoSubmitForm(request);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}