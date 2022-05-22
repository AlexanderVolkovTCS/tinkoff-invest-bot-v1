package com.sergeifedorov.investmentbot.service;

import com.sergeifedorov.investmentbot.domain.dto.FigiInfo;
import com.sergeifedorov.investmentbot.util.PropertyValues;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.core.InvestApi;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final PropertyValues propertyValues;
    private InvestApi api;

    @PostConstruct
    public void postConstructor() {
        String token = propertyValues.getSecretToken();
        api = InvestApi.create(token);
    }

    /**
     * Получить список всех акций
     */
    @SneakyThrows
    public List<FigiInfo> getAllShares() {
        return api.getInstrumentsService().getAllSharesSync()
                .stream().map(currency -> FigiInfo.builder().figi(currency.getFigi()).name(currency.getName()).build())
                .toList();
    }
}
