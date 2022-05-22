package com.sergeifedorov.investmentbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InvestmentBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestmentBotApplication.class, args);
    }

}
