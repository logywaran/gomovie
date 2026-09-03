package com.gomovie.payment.provider.mobi.deposit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/payments/mobi")
@RequiredArgsConstructor
@Slf4j
public class MobiDepositRedirectController {

    private final MobiRedirectService mobiRedirectService;

    @PostMapping("/redirect")
    public String handleRedirect(
            @RequestParam Map<String, String> params) {

        log.info("Mobi payment redirect received");

        MobiRedirectResult result =
                mobiRedirectService.processRedirect(params);

        return result == MobiRedirectResult.SUCCESS
                ? "redirect:/payment-success.html"
                : "redirect:/payment-failure.html";
    }
}