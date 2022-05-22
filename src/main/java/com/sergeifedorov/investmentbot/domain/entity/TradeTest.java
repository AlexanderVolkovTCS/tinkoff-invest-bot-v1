package com.sergeifedorov.investmentbot.domain.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "trade_test")
public class TradeTest extends BaseAbstractEntityId {

    private Double money;
    private Double value;
}
