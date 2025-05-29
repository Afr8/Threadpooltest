package com.example.requestor.controller;

import com.example.requestor.model.Metrics;
import com.example.requestor.service.BrokerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class RequestorController {
    private static final Logger logger = LoggerFactory.getLogger(RequestorController.class);
    private final BrokerService brokerService;

    public RequestorController(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    @CrossOrigin(origins = "null")
    @GetMapping("/trigger-slow-requests")
    public ResponseEntity<Metrics> triggerSlowRequests() {
        logger.info("Triggering slow requests");
        try {
            Metrics metrics = brokerService.triggerRequests("slow", "/slow", 1);
            if (metrics != null) {
                return ResponseEntity.ok(metrics);
            } else {
                logger.warn("Metrics for slow requests are null after triggering.");
                return ResponseEntity.internalServerError().body(null);
            }
        } catch (Exception e) {
            logger.error("Error triggering slow requests", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @CrossOrigin(origins = "null")
    @GetMapping("/trigger-fast-requests")
    public ResponseEntity<Metrics> triggerFastRequests() {
        logger.info("Triggering fast requests");
        try {
            Metrics metrics = brokerService.triggerRequests("fast", "/fast", 15);
            if (metrics != null) {
                return ResponseEntity.ok(metrics);
            } else {
                logger.warn("Metrics for fast requests are null after triggering.");
                return ResponseEntity.internalServerError().body(null);
            }
        } catch (Exception e) {
            logger.error("Error triggering fast requests", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @CrossOrigin(origins = "null")
    @GetMapping("/metrics")
    public ResponseEntity<Metrics> getMetrics() {
        logger.info("Fetching metrics");
        Metrics metrics = brokerService.getMetrics();
        if (metrics != null) {
            return ResponseEntity.ok(metrics);
        } else {
            logger.info("No metrics available yet.");
            return ResponseEntity.ok().body(null);
        }
    }
}
