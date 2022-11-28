package com.i4rt.temperaturecontrol.model;


import java.util.Date;

public abstract class MeasurementData {
    Date datetime;

    public Date getDatetime(){
        return datetime;
    }
}
