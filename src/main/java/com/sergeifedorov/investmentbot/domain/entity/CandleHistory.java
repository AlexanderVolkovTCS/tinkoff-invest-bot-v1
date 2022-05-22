package com.sergeifedorov.investmentbot.domain.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "candle_history")
public class CandleHistory extends BaseAbstractEntityId {

    private LocalDateTime date;
    private String figi;
    private Long unit;
    private Integer nano;
}
