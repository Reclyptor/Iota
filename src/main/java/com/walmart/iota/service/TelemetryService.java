package com.walmart.iota.service;

import com.walmart.iota.model.RefrigeratedTruckTelemetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class TelemetryService {
    @Autowired
    private Supplier<RefrigeratedTruckTelemetry> refrigeratedTruckTelemetrySupplier;

    public RefrigeratedTruckTelemetry fetchRefrigeratedTruckTelemetry() {
        return this.refrigeratedTruckTelemetrySupplier.get();
    }
}
