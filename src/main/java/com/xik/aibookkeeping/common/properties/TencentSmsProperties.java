package com.xik.aibookkeeping.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tencent.sms")
public class TencentSmsProperties {
    private String secretId;
    private String secretKey;
    private String sdkAppId;
    private String signName;
    private String templateId;
}
