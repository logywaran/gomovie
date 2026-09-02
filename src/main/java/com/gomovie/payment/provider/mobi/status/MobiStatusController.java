package com.gomovie.payment.provider.mobi.status;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/mobi")
@RequiredArgsConstructor
public class MobiStatusController {

    private final MobiStatusService mobiStatusService;

    @GetMapping("/status")
    public ResponseEntity<MobiStatusResponse> checkTransactionStatus(
            @RequestParam String searchKey) {

        return ResponseEntity.ok(
                mobiStatusService.checkTransactionStatus(searchKey)
        );
    }
}