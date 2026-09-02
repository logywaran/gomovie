package com.gomovie.payment.provider.mobi.bank;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/mobi")
@RequiredArgsConstructor
public class MobiBankController {

    private final MobiBankService mobiBankService;

    @GetMapping("/banks")
    public ResponseEntity<MobiBankResponse> getAvailableBanks() {

        return ResponseEntity.ok(
                mobiBankService.getAvailableBanks()
        );
    }
}