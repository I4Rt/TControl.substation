package com.i4rt.temperaturecontrol.Services;

import com.i4rt.temperaturecontrol.databaseInterfaces.UserRepo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class AlertHolder {

    private static AlertHolder instance;

    private Boolean needBeep;
    private Boolean firstTIError;
    private Boolean secondTIError;
    private Boolean thirdTIError;
    private Boolean fourthTIError;

    private Boolean weatherStationError;
    private Boolean MIPError;

    private AlertHolder() {
        this.needBeep = false;
        this.firstTIError = false;
        this.secondTIError = false;
        this.thirdTIError = false;
        this.fourthTIError = false;
        this.weatherStationError = false;
        this.MIPError = false;
    }

    public static AlertHolder getInstance() {

        if(instance == null){
            instance = new AlertHolder();
        }
        return instance;
    }
}
