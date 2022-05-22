package com.sergeifedorov.investmentbot.repository;

import com.sergeifedorov.investmentbot.domain.entity.TradeTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeTestRepo extends JpaRepository<TradeTest, Integer> {
}
