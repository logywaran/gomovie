package com.gomovie.payment.provider.mobi.status;

import com.gomovie.payment.provider.mobi.MobiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobiStatusService {

    private final MobiProperties mobiProperties;
    private final RestClient restClient;

    public MobiStatusResponse checkTransactionStatus(String searchKey) {

        log.info(
                "Checking Mobi FPX transaction status. searchKey={}",
                searchKey
        );

        MobiStatusRequest request = new MobiStatusRequest(
                "EXTERNAL_TXN_HISTORY",
                mobiProperties.getMotoApiKey(),
                mobiProperties.getLoginId(),
                "FPX",
                searchKey
        );

        // Log request details.
        // motoApiKey is intentionally NOT logged because it is a secret.
        log.info(
                "Mobi status request: service={}, trxType={}, loginId={}, searchKey={}, statusCheckUrl={}",
                request.service(),
                request.trxType(),
                request.loginId(),
                request.searchKey(),
                mobiProperties.getStatusCheckUrl()
        );

        MobiStatusResponse response =
                restClient
                        .post()
                        .uri(mobiProperties.getStatusCheckUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(MobiStatusResponse.class);

        if (response == null) {

            log.error(
                    "Mobi returned an empty status response. searchKey={}",
                    searchKey
            );

            throw new IllegalStateException(
                    "Unable to retrieve Mobi transaction status"
            );
        }

        // Log complete response for debugging.
        log.info(
                "Mobi status response: code={}, message={}, description={}, data={}",
                response.responseCode(),
                response.responseMessage(),
                response.responseDescription(),
                response.responseData()
        );

        log.info(
                "Mobi transaction status response received. responseCode={}, searchKey={}",
                response.responseCode(),
                searchKey
        );

        if (!"0000".equals(response.responseCode())) {

            log.warn(
                    "Mobi transaction status check failed. responseCode={}, message={}, description={}, searchKey={}",
                    response.responseCode(),
                    response.responseMessage(),
                    response.responseDescription(),
                    searchKey
            );

            return response;
        }

        if (response.responseData() == null
                || response.responseData().forSettlement() == null) {

            log.warn(
                    "Mobi returned no transaction data. searchKey={}",
                    searchKey
            );

            return response;
        }

        log.info(
                "Mobi FPX transaction status={}. searchKey={}",
                response.responseData()
                        .forSettlement()
                        .status(),
                searchKey
        );

        return response;
    }
}