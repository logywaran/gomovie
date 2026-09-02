package com.gomovie.payment.provider.mobi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mobi.fpx")
@Getter
@Setter
public class MobiProperties {

    private String initiateDepositUrl;
    private String availableBanksUrl;
    private String statusCheckUrl;

    private String mid;
    private String tid;
    private String subMid;
    private String merchantName;

    private String motoApiKey;
    private String loginId;

    private String redirectUrl;
}