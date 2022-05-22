package com.sergeifedorov.investmentbot.service;

import com.sergeifedorov.investmentbot.domain.entity.CandleHistory;
import com.sergeifedorov.investmentbot.repository.CandleHistoryRepo;
import com.sergeifedorov.investmentbot.util.PropertyValues;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.InvestApi;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LoadHistoryService {

    private final PropertyValues propertyValues;
    private final CandleHistoryRepo candleHistoryRepo;
    private InvestApi apiReal;

    @PostConstruct
    public void postConstructor() {
        apiReal = InvestApi.create(propertyValues.getSecretToken());
    }

    /**
     * Загрузка свечей за прошедший месяц для последующего анализа стратегии
     */
    @SneakyThrows
    public void loadHistory() {
        log.info("Start save history candle");
        List<String> figis = propertyValues.getFigis().stream().toList();

        figis.forEach(candleHistoryRepo::deleteAllByFigi);

        figis.forEach(figi -> {
            for (int i = 0; i < 30; i++) {
                LocalDateTime start = LocalDateTime.now().minusDays(i + 1);
                LocalDateTime end = LocalDateTime.now().minusDays(i);
                try {
                    List<HistoricCandle> historicCandles = apiReal.getMarketDataService().getCandlesSync(figi, parseToInstant(start), parseToInstant(end), CandleInterval.CANDLE_INTERVAL_1_MIN);
                    candleHistoryRepo.saveAll(parseListCandleHistory(historicCandles, figi));
                } catch (Exception e) {
                    log.error("Exception in time for load history candle");
                }
            }
        });
        log.info("End save history candle");
    }

    private Instant parseToInstant(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant();
    }

    private List<CandleHistory> parseListCandleHistory(List<HistoricCandle> input, String figi) {
        List<CandleHistory> result = new ArrayList<>();
        input.forEach(historicCandle -> result.add(parseToEntity(historicCandle, figi)));
        return result;
    }

    private CandleHistory parseToEntity(HistoricCandle input, String figi) {
        return CandleHistory.builder()
                .date(Instant.ofEpochSecond(input.getTime().getSeconds(),
                        input.getTime().getNanos()).atZone(ZoneId.systemDefault()).toLocalDateTime())
                .figi(figi)
                .unit(input.getHigh().getUnits())
                .nano(input.getHigh().getNano())
                .build();
    }
}
