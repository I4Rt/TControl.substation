package com.i4rt.temperaturecontrol.model;

import com.i4rt.temperaturecontrol.device.ThermalImager;
import lombok.*;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ControlObject {
    @Id
    @Column(name = "object_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;


    @OneToMany(mappedBy = "controlObject", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Measurement> measurement;

    @Column
    private String temperatureClass;

    @ManyToOne
    @JoinColumn(name = "thermal_imager_id", nullable=true)
    private ThermalImager thermalImager;

    // ImageViewer

    @Column
    private Double horizontal;
    @Column
    private Double vertical;

    @Column
    private Integer x;
    @Column
    private Integer y;
    @Column
    private Integer areaWidth;
    @Column
    private Integer areaHeight;

    @Column
    private Double focusing;

    // Map

    @Column
    private Integer mapX;
    @Column
    private Integer mapY;

    @Column(nullable = false)
    private Double warningTemp;
    @Column(nullable = false)
    private Double dangerTemp;


    public void updateTemperatureClass(Double curTemp){
        if(curTemp < warningTemp){
            temperatureClass = "normal";
        }
        else if(curTemp < dangerTemp){
            temperatureClass = "warning";
        }
        else{
            temperatureClass = "danger";
        }
    }




    public void addMeasurement(Measurement m){
        measurement.add(m);
    }

    public Boolean parseJsonStringAreaPageAndUpdateData(String rowJsonData){

        JSONObject data = new org.json.JSONObject(rowJsonData);

        System.out.println(data.getLong("id"));
        System.out.println(data.getString("name"));
        System.out.println(data.getDouble("warningTemp"));
        System.out.println(data.getDouble("dangerTemp"));

        return false;
    }



    public String getJsonStringNoMap(){

        return "{" +
                "\"id\": " + this.id + ", " +
                "\"name\": \"" + this.name + "\", " +
                "\"temperatureClass\": \"" + this.temperatureClass + "\"" +
                "}";
    }

    public String getJsonStringWithMap(){

        return "{" +
                "\"id\": " + this.id + ", " +
                "\"name\": \"" + this.name + "\", " +
                "\"temperatureClass\": \"" + this.temperatureClass + "\", " +
                "\"mapX\": " + this.mapX + ", " +
                "\"mapY\": " + this.mapY +
                "}";
    }

    public String getCoordinatesString(){
        if(getHorizontal() != null && getVertical() != null && getX() != null && getY() != null && getFocusing() != null){
            return "(("+getHorizontal()+"°, "+getVertical()+"°), ("+getX()+", "+getY()+"), "+getFocusing()+")";
        }
        return "Не заданы";

    }
}
