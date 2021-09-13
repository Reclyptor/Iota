package com.walmart.iota.controller;

import com.walmart.iota.model.RefrigeratedTruckTelemetry;
import com.walmart.iota.repository.RefrigeratedTruckTelemetryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/api/v1/reports")
public class ReportController {
    @Value("${telemetry.reporting.interval.write}")
    private Integer writeInterval;

    @Value("${telemetry.reporting.threshold.temperature}")
    private Integer temperature;

    @Autowired
    private RefrigeratedTruckTelemetryRepository refrigeratedTruckTelemetryRepository;

    @GetMapping(value = "/refrigeratedtruck")
    private @ResponseBody List<RefrigeratedTruckTelemetry> getRefrigeratedTruckTelemetryReport(@RequestParam(value = "secondsAgo") Long secondsAgo, @RequestParam(value = "temperature") Long temperature) {
        Instant timestamp = Instant.now();
        Date threshold = Date.from(timestamp.minus(secondsAgo == null ? this.writeInterval : secondsAgo, ChronoUnit.SECONDS));
        return this.refrigeratedTruckTelemetryRepository
                .findRefrigeratedTruckTelemetriesByTimestampAfterAndTemperatureGreaterThanEqual(threshold, temperature == null ? this.temperature : temperature);
    }
}
