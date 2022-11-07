package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    public String getReport(@RequestBody String jsonData) throws IOException {
        JSONObject data = new JSONObject(jsonData);
        String begin = data.getString("begin");
        String end = data.getString("end");
        System.out.println("In Range begin: " + begin);
        System.out.println("In Range end: " + end);
        String name = "error";

        Map<String, String> result = new HashMap<>();
        try {
            Date beginningDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(begin);
            Date endingDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(end);
            CreateExcelReport createExcelReport = new CreateExcelReport(this.controlObjectRepo, this.measurementRepo,
                    this.thermalImagerRepo, this.userRepo, this.weatherMeasurementRepo);
            name = createExcelReport.createMainSheet(beginningDate, endingDate);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e){
            System.out.println("Report generation error: " + e);
        }

        result.put("reportName", "report/" + name);

        String jsonStringToSend = JSONObject.valueToString(result);
        return jsonStringToSend;
    }
}
