package com.gomovie.payment.provider.mobi.ipn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/mobi")
@Slf4j
public class MobiIpnController {

    @GetMapping("/ipn")
    public ResponseEntity<String> handleIpn(
            @RequestParam String callBack) {

        log.info("Mobi IPN received");

        log.info(
                "Mobi IPN raw callback: {}",
                callBack
        );

        return ResponseEntity.ok(
                """
                {
                    "responseCode": 200,
                    "responseMessage": "Successful"
                }
                """
        );
    }
}