package com.sergeifedorov.investmentbot.controller;

import com.sergeifedorov.investmentbot.service.LoadHistoryService;
import com.sergeifedorov.investmentbot.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminController {

    private final TestService testService;
    private final LoadHistoryService loadHistoryService;

    @GetMapping
    @RequestMapping("/load-history")
    public void loadHistoryForTest() {
        loadHistoryService.loadHistory();
    }

    @GetMapping
    @RequestMapping("/start-test")
    public void startTest() {
        testService.testStrategy();
    }
}
