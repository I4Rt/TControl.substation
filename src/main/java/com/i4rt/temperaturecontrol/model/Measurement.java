package com.i4rt.temperaturecontrol.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Measurement {
    @Id
    @Column(name = "measurement_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "control_object_id")
    private ControlObject controlObject;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private String datetime;
}
