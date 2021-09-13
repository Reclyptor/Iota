package com.walmart.iota.configuration;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfiguration implements FlywayConfigurationCustomizer {
    @Override
    public void customize(FluentConfiguration configuration) {
        // Use default configuration.
    }
}
