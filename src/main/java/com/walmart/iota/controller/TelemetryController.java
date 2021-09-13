package com.walmart.iota.controller;

import com.walmart.iota.model.RefrigeratedTruckTelemetry;
import com.walmart.iota.service.TelemetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {
    @Autowired
    private TelemetryService telemetryService;

    @GetMapping(value = "/refrigeratedtruck")
    private @ResponseBody RefrigeratedTruckTelemetry getRefrigeratedTruckTelemetry() {
        return this.telemetryService.fetchRefrigeratedTruckTelemetry();
    }
}
