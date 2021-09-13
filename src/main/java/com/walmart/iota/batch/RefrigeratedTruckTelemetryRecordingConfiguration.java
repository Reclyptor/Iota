package com.walmart.iota.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.iota.io.Io;
import com.walmart.iota.model.RefrigeratedTruckTelemetry;
import com.walmart.iota.repository.RefrigeratedTruckTelemetryRepository;
import com.walmart.iota.service.TelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Configuration
public class RefrigeratedTruckTelemetryRecordingConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger("RefrigeratedTruckTelemetryRecordingLog");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${telemetry.logging.directory}")
    private String directory;

    @Value("${telemetry.logging.prefix}")
    private String prefix;

    @Value("${telemetry.logging.interval.read}")
    private Integer readInterval;

    @Value("${telemetry.logging.interval.write}")
    private Integer writeInterval;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private TelemetryService telemetryService;

    @Autowired
    private RefrigeratedTruckTelemetryRepository refrigeratedTruckTelemetryRepository;

    private ItemReader<RefrigeratedTruckTelemetry> refrigeratedTruckTelemetryReader(int interval) {
        Lock lock = new ReentrantLock();
        return () -> {
            synchronized (lock) {
                try {
                    RefrigeratedTruckTelemetry refrigeratedTruckTelemetry = this.telemetryService.fetchRefrigeratedTruckTelemetry();
                    LOGGER.info(String.valueOf(refrigeratedTruckTelemetry));
                    return refrigeratedTruckTelemetry;
                } finally {
                    Thread.sleep(interval * 1000L);
                }
            }
        };
    }

    private ItemWriter<RefrigeratedTruckTelemetry> refrigeratedTruckTelemetryFileWriter() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String directory = this.directory == null ? "." : this.directory;
        Io.createDirectoryIfNotExists(Paths.get(directory));
        return records -> {
            if (records.size() > 0) {
                String timestamp = simpleDateFormat.format(Date.from(Instant.now()));
                String filename = String.format("%s_%s.json", this.prefix, timestamp);
                Path filepath = Paths.get(directory, filename);
                Io.writeObjectJSON(filepath, records);
            }
        };
    }

    private ItemWriter<RefrigeratedTruckTelemetry> refrigeratedTruckTelemetryDatabaseWriter() {
        return this.refrigeratedTruckTelemetryRepository::saveAll;
    }

    private ItemWriter<RefrigeratedTruckTelemetry> refrigeratedTruckTelemetryWriter() {
        CompositeItemWriter<RefrigeratedTruckTelemetry> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(refrigeratedTruckTelemetryFileWriter(), refrigeratedTruckTelemetryDatabaseWriter()));
        return compositeItemWriter;
    }

    private Step recordRefrigeratedTruckTelemetry() {
        int chunkSize = Math.max(1, this.writeInterval / Math.max(1, this.readInterval));
        return this.stepBuilderFactory.get("recordRefrigeratedTruckTelemetry")
                .<RefrigeratedTruckTelemetry, RefrigeratedTruckTelemetry>chunk(chunkSize)
                .reader(refrigeratedTruckTelemetryReader(this.readInterval))
                .writer(refrigeratedTruckTelemetryWriter())
                .build();
    }

    @Bean
    public Job refrigeratedTruckTelemetryRecorder() {
        return this.jobBuilderFactory.get("refrigeratedTruckTelemetryRecorder")
                .preventRestart()
                .incrementer(new RunIdIncrementer())
                .flow(recordRefrigeratedTruckTelemetry())
                .end()
                .build();
    }
}