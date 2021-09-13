package com.walmart.iota.model;

import com.fasterxml.jackson.annotation.*;
import com.walmart.iota.dto.RefrigeratedTruckTelemetryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "refrigerated_truck_telemetry")
public class RefrigeratedTruckTelemetry {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Id Long id;
    private Long truckID;
    private Long temperature;
    private Long humidity;
    private Double latitude;
    private Double longitude;
    private @JsonFormat(shape = JsonFormat.Shape.STRING) @Column(name = "ts") Date timestamp;

    public RefrigeratedTruckTelemetryDTO toDTO() {
        String coordinates = String.format("%02.2f,%02.2f", this.latitude, this.longitude);
        return new RefrigeratedTruckTelemetryDTO(this.truckID, this.temperature, this.humidity, coordinates);
    }
}