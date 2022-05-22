package com.sergeifedorov.investmentbot.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Getter
public class PropertyValues {

    @Value("${secret-token-sandbox}")
    private String secretTokenSandbox;
    @Value("${secret-token}")
    private String secretToken;
    @Value("${short-period-of-time}")
    private Integer shortPeriod;
    @Value("${long-period-of-time}")
    private Integer longPeriod;
    @Value("${difference-value}")
    private Double differenceValue;
    @Value("${figis}")
    private Collection<String> figis;
    @Value("${order-id}")
    private String orderId;
    @Value("${buy-size}")
    private Integer buySize;
}
