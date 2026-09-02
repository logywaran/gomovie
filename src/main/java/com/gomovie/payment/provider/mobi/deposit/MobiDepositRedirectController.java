package com.gomovie.payment.provider.mobi.deposit;

import com.gomovie.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/mobi")
@RequiredArgsConstructor
@Slf4j
public class MobiDepositRedirectController {

    private final MobiRedirectService mobiRedirectService;

    @PostMapping("/redirect")
    public ResponseEntity<String> handleRedirect(
            @RequestParam Map<String, String> params) {

        log.info("Mobi payment redirect received");

        log.info(
                "Mobi callback parameters: {}",
                params
        );

        mobiRedirectService.processRedirect(params);

        return ResponseEntity.ok(
                "Payment response received"
        );
    }
}