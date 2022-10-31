package com.i4rt.temperaturecontrol.Services;

import com.i4rt.temperaturecontrol.databaseInterfaces.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlertHolder {

    private AlertHolder instance;

    private Boolean needBeep;
    private Boolean firstTIError;
    private Boolean secondTIError;
    private Boolean thirdTIError;
    private Boolean fourthTIError;

    private Boolean weatherStationError;

}
