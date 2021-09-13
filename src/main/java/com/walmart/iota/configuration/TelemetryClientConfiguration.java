package com.walmart.iota.configuration;

import com.walmart.iota.dto.RefrigeratedTruckTelemetryDTO;
import com.walmart.iota.model.RefrigeratedTruckTelemetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Configuration
public class TelemetryClientConfiguration {
    @Value("${telemetry.endpoint}")
    private String telemetryEndpoint;

    /**
     * We use a Supplier here since we're reading telemetry data from a single endpoint -- we can later update this to a
     * Function<ID, RefrigeratedTruckTelemetry> to fetch data for specific trucks if the telemetry API supports it
     * (Useful for more complex batch jobs).
     *
     * We also transform the raw data here since the conversion is trivial to do. This way the returned object is
     * immediately available for use without additional processing whilst avoiding timestamp delays. Additionally,
     * conversions to and from DTO can be used in other areas of the application without requiring a Spring Batch
     * ItemProcessor.
     */
    @Bean
    public Supplier<RefrigeratedTruckTelemetry> refrigeratedTruckTelemetrySupplier() {
        RestTemplate restTemplate = new RestTemplate();
        return () -> {
            RefrigeratedTruckTelemetryDTO refrigeratedTruckDTO = restTemplate.getForObject(this.telemetryEndpoint, RefrigeratedTruckTelemetryDTO.class);
            return refrigeratedTruckDTO == null ? null : refrigeratedTruckDTO.toEntity();
        };
    }
}