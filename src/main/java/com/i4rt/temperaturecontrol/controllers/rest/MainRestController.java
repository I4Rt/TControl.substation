package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.Services.AlertHolder;
import com.i4rt.temperaturecontrol.Services.ConnectionHolder;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.model.Measurement;
import com.i4rt.temperaturecontrol.model.MeasurementData;
import com.i4rt.temperaturecontrol.model.WeatherMeasurement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.SchemaOutputResolver;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

        String jsonStringToSend = JSONObject.valueToString(preparedToSendData);
        return jsonStringToSend;
    }
//
//    @RequestMapping(value = "getInRange_", method = RequestMethod.POST)
//    public String getInRange(@RequestBody String dataJson){
//        JSONObject data = new JSONObject(dataJson);
//
//        String begin = data.getString("begin");
//        String end = data.getString("end");
//        Long id = Long.parseLong("26");
//
//        try {
//            Date beginningDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(begin);
//            Date endingDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(end);
//            System.out.println("dates " + beginningDate + " ----> " + endingDate);
//
//            List<MeasurementData> results = new ArrayList<>();
//            results.addAll( weatherMeasurementRepo.getWeatherMeasurementByDatetimeInRange(beginningDate, endingDate));
//            results.addAll( measurementRepo.getMeasurementByDatetimeInRange(id, beginningDate, endingDate));
//
//            Map<String, Object> preparedToSendData = new HashMap<>();
//
//            preparedToSendData.put("time", new ArrayList<String>());
//            preparedToSendData.put("weatherTemperature", new ArrayList<Double>());
//
//
//            for(MeasurementData obj: results){
//                if(obj instanceof WeatherMeasurement){
//                    WeatherMeasurement finObj = (WeatherMeasurement) obj;
//                    ((ArrayList<String>)preparedToSendData.get("time")).add(finObj.getDatetime().toString());
//
//                    ((ArrayList<Double>)preparedToSendData.get("weatherTemperature")).add(finObj.getTemperature());
//                }
//                else if(obj instanceof Measurement){
//                    Measurement finObj = (Measurement) obj;
//                    ((ArrayList<String>)preparedToSendData.get("time")).add(finObj.getDatetime().toString());
//
//                    ((ArrayList<Double>)preparedToSendData.get("weatherTemperature")).add(finObj.getTemperature());
//                }
//                //System.out.println(weatherMeasurement.getId());
//            }
//
//            String jsonStringToSend = JSONObject.valueToString(preparedToSendData);
//            System.out.println("Clipped array: " + jsonStringToSend);
//            return jsonStringToSend;
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return "error: getting temperature";
//    }
}
