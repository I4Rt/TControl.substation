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
public class VoltageMeasurement {
    @Id
    @Column(name = "voltage_measurement_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "control_object_id", nullable=false)
    private ControlObject controlObject;

    @Column
    private Double voltage;

    @Column(name = "datetime")
    private Date datetime;


}
