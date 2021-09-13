package com.walmart.iota.dto;

import com.walmart.iota.model.RefrigeratedTruckTelemetry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "data")
public class RefrigeratedTruckTelemetryDTO {
    private @Id Long id;
    private Long temperature;
    private Long humidity;
    private String location;

    public RefrigeratedTruckTelemetry toEntity() {
        String regex = "(?<latitude>[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)),(?<longitude>[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.location);
        if (matcher.matches()) {
            Double latitude = Double.parseDouble(matcher.group("latitude"));
            Double longitude = Double.parseDouble(matcher.group("longitude"));
            return new RefrigeratedTruckTelemetry(null, this.id, this.temperature, this.humidity, latitude, longitude, Date.from(Instant.now()));
        }
        return new RefrigeratedTruckTelemetry(null, this.id, this.temperature, this.humidity, null, null, Date.from(Instant.now()));
    }
}
