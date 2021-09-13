package com.walmart.iota.repository;

import com.walmart.iota.model.RefrigeratedTruckTelemetry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Repository
@Transactional
public interface RefrigeratedTruckTelemetryRepository extends CrudRepository<RefrigeratedTruckTelemetry, Long> {
    List<RefrigeratedTruckTelemetry> findRefrigeratedTruckTelemetriesByTimestampAfterAndTemperatureGreaterThanEqual(Date timestamp, Long temperature);
}