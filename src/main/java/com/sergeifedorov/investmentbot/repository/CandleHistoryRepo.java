package com.sergeifedorov.investmentbot.repository;

import com.sergeifedorov.investmentbot.domain.entity.CandleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandleHistoryRepo extends JpaRepository<CandleHistory, Integer> {

    void deleteAllByFigi(String figi);
}
