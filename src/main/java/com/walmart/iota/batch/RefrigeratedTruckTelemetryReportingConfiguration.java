package com.walmart.iota.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.walmart.iota.io.Io;
import com.walmart.iota.model.RefrigeratedTruckTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class RefrigeratedTruckTelemetryReportingConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger("RefrigeratedTruckTelemetryReportingLog");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${telemetry.logging.directory}")
    private String logDirectory;

    @Value("${telemetry.logging.prefix}")
    private String logPrefix;

    @Value("${telemetry.reporting.directory}")
    private String directory;

    @Value("${telemetry.reporting.prefix}")
    private String prefix;

    @Value("${telemetry.reporting.interval.write}")
    private Integer writeInterval;

    @Value("${telemetry.reporting.threshold.temperature}")
    private Integer temperature;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    private Predicate<Path> validPathPredicate() {
        String regex = this.logPrefix + "_" + "(?<timestamp>\\d{14})\\.json";
        Pattern pattern = Pattern.compile(regex);
        return path -> {
            File file = path.toFile();
            if (!file.isFile()) {
                return false;
            }

            Matcher matcher = pattern.matcher(file.getName());
            if (!matcher.matches()) {
                return false;
            }

            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Instant now = Instant.now();
                Instant threshold = now.minus(this.writeInterval, ChronoUnit.SECONDS);
                Instant timestamp = simpleDateFormat.parse(matcher.group("timestamp")).toInstant();
                return threshold.isBefore(timestamp);
            } catch (ParseException ignored) {
                return false;
            }
        };
    }

    private Stream<RefrigeratedTruckTelemetry> parseFile(Path path) {
        try {
            File file = path.toFile();
            CollectionType collectionType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, RefrigeratedTruckTelemetry.class);
            Collection<RefrigeratedTruckTelemetry> refrigeratedTruckTelemetry = OBJECT_MAPPER.readerFor(collectionType).readValue(file);
            return refrigeratedTruckTelemetry.stream();
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    private ItemReader<Stream<RefrigeratedTruckTelemetry>> refrigeratedTruckTelemetryLogReader(int interval) {
        Lock lock = new ReentrantLock();
        String directory = this.logDirectory == null ? "." : this.logDirectory;
        return () -> {
            synchronized (lock) {
                try {
                    return Files.list(Paths.get(directory))
                            .filter(validPathPredicate())
                            .flatMap((this::parseFile));
                } finally {
                    Thread.sleep(interval * 1000L);
                }
            }
        };
    }

    private ItemProcessor<Stream<RefrigeratedTruckTelemetry>, List<RefrigeratedTruckTelemetry>> refrigeratedTruckTelemetryFilter() {
        return paths -> {
            Instant timestamp = Instant.now();
            Instant threshold = timestamp.minus(this.writeInterval, ChronoUnit.SECONDS);
            return paths
                    .filter(telemetry -> telemetry.getTimestamp().after(Date.from(threshold)))
                    .filter(telemetry -> telemetry.getTemperature() >= this.temperature)
                    .collect(Collectors.toList());
        };
    }

    private ItemWriter<List<RefrigeratedTruckTelemetry>> refrigeratedTruckTelemetryReportLogger() {
        return telemetries -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("\n========== Refrigerated Truck Telemetry Report (Interval: %d sec prior | Temperature >= %d) ==========\n", this.writeInterval, this.temperature));
            telemetries.stream()
                    .flatMap(Collection::stream)
                    .map(String::valueOf)
                    .map(s -> String.format("  ⦿ %s\n", s))
                    .forEach(stringBuilder::append);
            stringBuilder.append("======================================================================================================\n");
            LOGGER.info(String.valueOf(stringBuilder));
        };
    }

    private ItemWriter<List<RefrigeratedTruckTelemetry>> refrigeratedTruckTelemetryReportFileWriter() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String directory = this.directory == null ? "." : this.directory;
        Io.createDirectoryIfNotExists(Paths.get(directory));
        return telemetries -> {
            List<RefrigeratedTruckTelemetry> records = telemetries.stream().flatMap(Collection::stream).collect(Collectors.toList());
            if (records.size() > 0) {
                String timestamp = simpleDateFormat.format(java.sql.Date.from(Instant.now()));
                String filename = String.format("%s_%s.json", this.prefix, timestamp);
                Path filepath = Paths.get(directory, filename);
                Io.writeObjectJSON(filepath, records);
            }
        };
    }

    private ItemWriter<List<RefrigeratedTruckTelemetry>> refrigeratedTruckTelemetryReportWriter() {
        CompositeItemWriter<List<RefrigeratedTruckTelemetry>> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(refrigeratedTruckTelemetryReportLogger(), refrigeratedTruckTelemetryReportFileWriter()));
        return compositeItemWriter;
    }

    private Step reportRefrigeratedTruckTelemetry() {
        return this.stepBuilderFactory.get("reportRefrigeratedTruckTelemetry")
                .<Stream<RefrigeratedTruckTelemetry>, List<RefrigeratedTruckTelemetry>>chunk(1)
                .reader(refrigeratedTruckTelemetryLogReader(this.writeInterval))
                .processor(refrigeratedTruckTelemetryFilter())
                .writer(refrigeratedTruckTelemetryReportWriter())
                .build();
    }

    @Bean
    public Job refrigeratedTruckTelemetryReporter() {
        return this.jobBuilderFactory.get("refrigeratedTruckTelemetryReporter")
                .incrementer(new RunIdIncrementer())
                .flow(reportRefrigeratedTruckTelemetry())
                .end()
                .build();
    }
}