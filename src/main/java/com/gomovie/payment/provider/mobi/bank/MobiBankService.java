package com.gomovie.payment.provider.mobi.bank;

import com.gomovie.payment.provider.mobi.MobiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobiBankService {

    private final MobiProperties mobiProperties;
    private final RestClient restClient;

    public MobiBankResponse getAvailableBanks() {

        log.info("Requesting available FPX banks from Mobi");

        MobiBankRequest request =
                new MobiBankRequest("FULL_LIST");

        MobiBankResponse response =
                restClient
                        .post()
                        .uri(mobiProperties.getAvailableBanksUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(MobiBankResponse.class);

        if (response == null) {
            log.error("Mobi returned an empty bank response");

            throw new IllegalStateException(
                    "Unable to retrieve available FPX banks"
            );
        }

        log.info(
                "Mobi FPX bank response received. responseCode={}",
                response.responseCode()
        );

        if (!"0000".equals(response.responseCode())) {

            log.error(
                    "Mobi FPX bank request failed. responseCode={}, message={}, description={}",
                    response.responseCode(),
                    response.responseMessage(),
                    response.responseDescription()
            );

            throw new IllegalStateException(
                    "Mobi FPX bank request failed: "
                            + response.responseMessage()
                            + " - "
                            + response.responseDescription()
            );
        }

        return response;
    }
}