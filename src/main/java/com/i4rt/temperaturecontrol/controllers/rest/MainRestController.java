package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.Services.AlertHolder;
import com.i4rt.temperaturecontrol.Services.ConnectionHolder;
import com.i4rt.temperaturecontrol.Services.ThermalImagersHolder;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.WeatherMeasurement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

@RestController
public class MainRestController {
    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;

    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;

    public MainRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;

    }

    @RequestMapping(value = "getWeather", method = RequestMethod.POST)
    public String getWeather(){
        WeatherMeasurement weatherMeasurement = weatherMeasurementRepo.getLastWeatherMeasurement();
        if(weatherMeasurement != null){
            return "<text>v: " + weatherMeasurement.getWindForce() + " м/с</text>" +
                    "<text>φ: " + weatherMeasurement.getHumidity() + "%</text>" +
                    "<text>t: " + weatherMeasurement.getTemperature() + "°</text>" +
                    "<text>p: " + weatherMeasurement.getAtmospherePressure() + " Атм</text>" +
                    "<text>h: " + weatherMeasurement.getRainfall() + " мм</text>";
        }
        else{
            return "<text>v: ? м/с</text>" +
                    "<text>φ: ?%</text>" +
                    "<text>t: ?°</text>" +
                    "<text>p: ? Атм</text>" +
                    "<text>h: ? мм</text>";
        }


    }

    @RequestMapping(value = "resetConnections", method = RequestMethod.GET)
    public String resetConnections(){
        ConnectionHolder.removeAllConnection();
        return "Подключения обновлены";
    }
    @RequestMapping(value = "rebootTI", method = RequestMethod.GET)
    public String rebootAllTIs(){
        StringBuilder sb = new StringBuilder();
        try {
            List<ThermalImager> thermalImagers = ThermalImagersHolder.findAll();
            for(ThermalImager ti : thermalImagers){
                sb.append("Тепловизор ");
                sb.append(ti.getId());
                sb.append(": ");
                sb.append(ti.reboot());
                sb.append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Возникла ошибка";
        }
        return sb.toString();
    }

    @RequestMapping(value = "getAlerts", method = RequestMethod.GET)
    public String getAlerts(){
        AlertHolder alertHolder = AlertHolder.getInstance();
        System.out.println("Alert holder: " + alertHolder);
        Map<String, Object> preparedToSendData = new HashMap<>();
        preparedToSendData.put("beep", controlObjectRepo.getDanger().size() > 0 || controlObjectRepo.getDangerDifference().size() > 0 ? true : false);
        preparedToSendData.put("firstTIError", alertHolder.getFirstTIError());
        preparedToSendData.put("secondTIError", alertHolder.getSecondTIError());
        preparedToSendData.put("thirdTIError", alertHolder.getThirdTIError());
        preparedToSendData.put("fourthTIError", alertHolder.getFourthTIError());
        preparedToSendData.put("weatherStationError", alertHolder.getWeatherStationError());
        preparedToSendData.put("MIPError", alertHolder.getMIPError());

        String jsonStringToSend = JSONObject.valueToString(preparedToSendData);
        return jsonStringToSend;
    }
}
