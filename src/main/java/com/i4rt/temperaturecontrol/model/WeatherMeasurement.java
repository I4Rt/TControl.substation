package com.i4rt.temperaturecontrol.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WeatherMeasurement {
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

    @Column
    private String dateTime;
}
