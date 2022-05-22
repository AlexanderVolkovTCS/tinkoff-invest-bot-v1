package com.sergeifedorov.investmentbot.domain.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FigiInfo {

    private String figi;
    private String name;
}
