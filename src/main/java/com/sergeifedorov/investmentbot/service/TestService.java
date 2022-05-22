package com.sergeifedorov.investmentbot.service;

import com.sergeifedorov.investmentbot.domain.entity.CandleHistory;
import com.sergeifedorov.investmentbot.domain.entity.TradeTest;
import com.sergeifedorov.investmentbot.repository.CandleHistoryRepo;
import com.sergeifedorov.investmentbot.repository.TradeTestRepo;
import com.sergeifedorov.investmentbot.util.PropertyValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class TestService {

    private static final int SCALE = 9;
    private final CandleHistoryRepo candleHistoryRepo;
    private final PropertyValues propertyValues;
    private final TradeTestRepo tradeTestRepo;
    private int testEntityId;

    /**
     * Запуск теста стратегии по загруженной заранее истории свечей
     */
    public void testStrategy() {
        clearDB();

        List<CandleHistory> candleHistories = candleHistoryRepo.findAll();
        for (int i = 100; i < candleHistories.size(); i++) {
            CandleHistory candleHistory = candleHistories.get(i);

            double shortCut = getAverage(candleHistories.subList(i - propertyValues.getShortPeriod(), i)).doubleValue();
            double longCut = getAverage(candleHistories.subList(i - propertyValues.getLongPeriod(), i)).doubleValue();

            if (longCut != 0.00 && shortCut != 0.00) {
                double difference = longCut / shortCut * 100 - 100;

                if (difference > propertyValues.getDifferenceValue()) {
                    buy(candleHistory);

                } else if (difference < propertyValues.getDifferenceValue() * -1) {
                    sell(candleHistory);
                }
            }
        }
        TradeTest result = tradeTestRepo.getById(testEntityId);
        log.info("По завершению цикла сумма в рублях = {}", result.getMoney());
        log.info("По завершении цикла сумма акций в рублях = {}", result.getValue() * createBigDecimalForCandle(candleHistories.get(0)).doubleValue());
    }

    private void clearDB() {
        tradeTestRepo.deleteAll();
        TradeTest tradeTest = tradeTestRepo.save(TradeTest.builder().money(1000.00).value(0.00).build());
        testEntityId = tradeTest.getId();
    }

    private void sell(CandleHistory candleHistory) {
        TradeTest tradeTest = tradeTestRepo.getById(testEntityId);
        if (tradeTest.getValue() > 0) {
            tradeTest.setValue(tradeTest.getValue() - 1);
            tradeTest.setMoney(tradeTest.getMoney() + createBigDecimalForCandle(candleHistory).doubleValue());
            tradeTestRepo.save(tradeTest);
            PostOrderResponse.newBuilder().build();
        }
    }

    private void buy(CandleHistory candleHistory) {
        TradeTest tradeTest = tradeTestRepo.getById(testEntityId);
        if (tradeTest.getMoney() >= createBigDecimalForCandle(candleHistory).doubleValue()) {
            tradeTest.setValue(tradeTest.getValue() + 1);
            tradeTest.setMoney(tradeTest.getMoney() - createBigDecimalForCandle(candleHistory).doubleValue());
            tradeTestRepo.save(tradeTest);
            PostOrderResponse.newBuilder().build();
        }
    }

    private BigDecimal getAverage(List<CandleHistory> historicCandles) {
        BigDecimal sumCandles = historicCandles.stream().map(this::createBigDecimalForCandle).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal countCandles = new BigDecimal(String.valueOf(historicCandles.size()));
        return sumCandles.setScale(SCALE, RoundingMode.HALF_EVEN).divide(countCandles, RoundingMode.HALF_EVEN);
    }

    private BigDecimal createBigDecimalForCandle(CandleHistory candle) {
        return new BigDecimal(candle.getUnit() + "." + candle.getNano());
    }
}
