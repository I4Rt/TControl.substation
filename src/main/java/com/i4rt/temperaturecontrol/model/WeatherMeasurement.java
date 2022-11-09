package com.i4rt.temperaturecontrol.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WeatherMeasurement extends MeasurementData{
    @Id
    @Column(name = "weather_measurement_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Double temperature;

    @Column
    private Double windForce;

    @Column
    private Double humidity;

    @Column
    private Double atmospherePressure;

    @Column
    private Double rainfall;

    @Column(name = "datetime")
    private Date datetime;
}
