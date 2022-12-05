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
    private final UserRepo userRepo;
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;
    @Autowired
    private final MIPMeasurementRepo mipMeasurementRepo;

    public ReportPageRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo, MIPMeasurementRepo mipMeasurementRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.mipMeasurementRepo = mipMeasurementRepo;
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

//            System.out.println("\n\n\n" + results.get(1) + "\n\n\n");

            CreateExcelReport createExcelReport = new CreateExcelReport(this.controlObjectRepo, this.measurementRepo, this.userRepo, this.weatherMeasurementRepo, this.mipMeasurementRepo);
            name = createExcelReport.createMainSheet(beginningDate, endingDate);

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
