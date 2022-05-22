package com.sergeifedorov.investmentbot.service;

import com.sergeifedorov.investmentbot.util.PropertyValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.core.InvestApi;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_MARKET;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TradeService {

    private static final int SCALE = 9;
    private final PropertyValues propertyValues;
    private final AccountService accountService;
    private InvestApi api;

    @PostConstruct
    public void postConstructor() {
        String token = propertyValues.getSecretToken();
        String tokenTest = propertyValues.getSecretTokenSandbox();
        api = InvestApi.create(token);
//        api = InvestApi.create(tokenTest);
    }

    /**
     * Алгоритм анализа рынка и выставления заявок
     */
    @Scheduled(cron = "${time-update}")
    public void tradeTick() {
        log.info("start trade tick");
        propertyValues.getFigis().forEach(figi -> {
            double shortCut = getAverage(figi, propertyValues.getShortPeriod()).doubleValue();
            double longCut = getAverage(figi, propertyValues.getLongPeriod()).doubleValue();

            log.info(String.valueOf(shortCut));
            log.info(String.valueOf(longCut));
            System.out.println(shortCut);
            System.out.println(longCut);
            if (longCut != 0.00 && shortCut != 0.00) {
                double difference = longCut / shortCut * 100 - 100;
                log.info("================");
                log.info("короткое значение: " + shortCut);
                log.info("длинное значение: " + longCut);
                log.info("разница значений: " + difference);

                if (difference > propertyValues.getDifferenceValue()) {
                    log.info("купили за " + (getLastPriceValue(getLastPrice(figi))));
                    log.info(buy(figi, propertyValues.getBuySize()).getMessage());
                } else if (difference < propertyValues.getDifferenceValue() * -1) {
                    log.info("продали за " + (getLastPriceValue(getLastPrice(figi))));
                    log.info(sell(figi, propertyValues.getBuySize()).getMessage());
                } else {
                    log.info("Находимся в коридоре, сделок не было");
                }
                log.info("================");
            }
        });
    }

    private BigDecimal getAverage(String figi, long period) {
        LocalDateTime start = LocalDateTime.now().minusMinutes(period);
        List<HistoricCandle> historicCandles = api.getMarketDataService()
                .getCandlesSync(figi, start.atZone(ZoneId.systemDefault()).toInstant(), Instant.now(), CandleInterval.CANDLE_INTERVAL_1_MIN);
        if (historicCandles.isEmpty()) {
            return BigDecimal.valueOf(0.00);
        }
        BigDecimal sumCandles = historicCandles.stream()
                .map(candle -> new BigDecimal(candle.getHigh().getUnits() + "." + candle.getHigh().getNano()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal countCandles = new BigDecimal(String.valueOf(historicCandles.size()));
        return sumCandles.setScale(SCALE, RoundingMode.HALF_EVEN).divide(countCandles, RoundingMode.HALF_EVEN);
    }

    private LastPrice getLastPrice(String figi) {
        return api.getMarketDataService().getLastPricesSync(Collections.singleton(figi)).get(0);
    }

    private BigDecimal getLastPriceValue(LastPrice lastPrices) {
        return new BigDecimal(lastPrices.getPrice().getUnits() + "." + lastPrices.getPrice().getNano());
    }

    private PostOrderResponse buy(String figi, int quantity) {
        return submittingApplication(OrderDirection.ORDER_DIRECTION_BUY, figi, quantity);
    }

    private PostOrderResponse sell(String figi, int quantity) {
        return submittingApplication(OrderDirection.ORDER_DIRECTION_SELL, figi, quantity);
    }

    private PostOrderResponse submittingApplication(OrderDirection operation, String figi, int quantity) {
        Quotation lastPrice = getLastPrice(figi).getPrice();
        Quotation quotation = Quotation.newBuilder().setUnits(lastPrice.getUnits() * quantity).setNano(lastPrice.getNano() * quantity).build();
        return api.getOrdersService().postOrderSync(figi, quantity, quotation, operation,
                accountService.getActiveAccount().getId(), ORDER_TYPE_MARKET, propertyValues.getOrderId());
    }
}
