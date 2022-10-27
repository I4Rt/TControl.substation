package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.Services.ConnectionHolder;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.model.WeatherMeasurement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class MainRestController {
    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;
    @Autowired
    private final ThermalImagerRepo thermalImagerRepo;
    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;

    public MainRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
    }

    @RequestMapping(value = "getWeather", method = RequestMethod.POST)
    public String getWeather(){
        WeatherMeasurement weatherMeasurement = weatherMeasurementRepo.getLastWeatherMeasurement();

        return "<text>v: " + weatherMeasurement.getWindForce() + " м/с</text>" +
                "<text>φ: " + weatherMeasurement.getHumidity() + "%</text>" +
                "<text>t: " + weatherMeasurement.getTemperature() + "°</text>" +
                "<text>p: " + weatherMeasurement.getAtmospherePressure() + " Атм</text>" +
                "<text>t: " + weatherMeasurement.getRainfall() + " мм</text>";
    }

    @RequestMapping(value = "resetConnections", method = RequestMethod.GET)
    public String resetConnections(){
        try {
            ConnectionHolder.removeAllConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return "Возникла ошибка";
        }
        return "Подключения обновлены";
    }
}
