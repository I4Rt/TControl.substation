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
public class MIPMeasurement extends MeasurementData{
    @Id
    @Column(name = "mip_measurement_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column
    private Double voltageA;
    @Column
    private Double voltageB;
    @Column
    private Double voltageC;
    @Column
    private Double amperageA;
    @Column
    private Double amperageB;
    @Column
    private Double amperageC;
    @Column
    private Double powerA;
    @Column
    private Double powerB;
    @Column
    private Double powerC;

    @Column(name = "datetime")
    private Date datetime;


}
