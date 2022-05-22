package com.sergeifedorov.investmentbot.service;

import com.sergeifedorov.investmentbot.util.PropertyValues;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;
import ru.tinkoff.piapi.core.InvestApi;

import javax.annotation.PostConstruct;
import javax.security.auth.login.AccountNotFoundException;
import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final PropertyValues propertyValues;
    private InvestApi api;

    @PostConstruct
    public void postConstructor() {
        String token = propertyValues.getSecretToken();
        api = InvestApi.create(token);
    }

    /**
     * Получить текущий брокерский счет
     */
    @SneakyThrows
    public Account getActiveAccount() {
        Optional<Account> optional = api.getUserService().getAccountsSync().stream()
                .filter(account -> account.getStatus().equals(AccountStatus.ACCOUNT_STATUS_OPEN)
                        && account.getType().equals(AccountType.ACCOUNT_TYPE_TINKOFF))
                .findFirst();
        if (optional.isEmpty()) throw new AccountNotFoundException();
        return optional.get();
    }
}
