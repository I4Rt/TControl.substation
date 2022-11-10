package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.model.Measurement;
import com.i4rt.temperaturecontrol.model.MeasurementData;
import com.i4rt.temperaturecontrol.model.WeatherMeasurement;
import com.i4rt.temperaturecontrol.tasks.CreateExcelReport;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ReportPageRestController {
    @Autowired
    private final ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;
    @Autowired
    private final ThermalImagerRepo thermalImagerRepo;
    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;

    public ReportPageRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
    }


    @RequestMapping(value = "/getReport", method = RequestMethod.POST)
    public String getReport(@RequestBody String jsonData) {
        JSONObject data = new JSONObject(jsonData);

        String begin = data.getString("begin");
        String end = data.getString("end");
        Long id = Long.parseLong("26");
        String name = "error";
        Map<String, String> result = new HashMap<>();

        try {
            Date beginningDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(begin);
            Date endingDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(end);
            System.out.println("dates " + beginningDate + " ----> " + endingDate);

            List<MeasurementData> results = new ArrayList<>();
            results.addAll( weatherMeasurementRepo.getWeatherMeasurementByDatetimeInRange(beginningDate, endingDate));
            results.addAll( measurementRepo.getMeasurementByDatetimeInRange(id, beginningDate, endingDate));

            Map<String, Object> preparedToSendData = new HashMap<>();

            preparedToSendData.put("time", new ArrayList<Date>());
            preparedToSendData.put("weatherTemperature", new ArrayList<>());


            for(MeasurementData obj: results){
                if(obj instanceof WeatherMeasurement){
                    WeatherMeasurement finObj = (WeatherMeasurement) obj;
                    ((ArrayList<Date>)preparedToSendData.get("time")).add(finObj.getDatetime());

                    ((ArrayList<WeatherMeasurement>)preparedToSendData.get("weatherTemperature")).add(finObj);
                }
                else if(obj instanceof Measurement){
                    Measurement finObj = (Measurement) obj;
                    ((ArrayList<Date>)preparedToSendData.get("time")).add(finObj.getDatetime());

                    ((ArrayList<Measurement>)preparedToSendData.get("weatherTemperature")).add(finObj);
                }
                //System.out.println(weatherMeasurement.getId());
            }

            CreateExcelReport createExcelReport = new CreateExcelReport(this.controlObjectRepo, this.measurementRepo,
                    this.thermalImagerRepo, this.userRepo, this.weatherMeasurementRepo);
            name = createExcelReport.createMainSheet(preparedToSendData);

            result.put("reportName", "reports/" + name);

            String jsonStringToSend = JSONObject.valueToString(result);

//            String jsonStringToSend = JSONObject.valueToString(preparedToSendData);
//            System.out.println("Clipped array: " + jsonStringToSend);
            return jsonStringToSend;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println("Report generation error: " + e);
        }
        return "error: getting temperature";

    }
}
