package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.deviceControlThreads.ThermalImagersMainControlThread;
import com.i4rt.temperaturecontrol.deviceControlThreads.WeatherStationControlThread;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.User;
import com.i4rt.temperaturecontrol.tasks.DeleteCreateFolderTask;
import com.i4rt.temperaturecontrol.tasks.Executor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;


@RestController
public class MainPageRestHolder {
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

    public MainPageRestHolder(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo) {

        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;

        ThermalImagersMainControlThread.setInstance(controlObjectRepo, measurementRepo, thermalImagerRepo, userRepo);

        ThermalImagersMainControlThread thermalImagersMainControlThread = ThermalImagersMainControlThread.getInstance();

        thermalImagersMainControlThread.start();

        WeatherStationControlThread weatherStationControlThread = new WeatherStationControlThread(weatherMeasurementRepo);

        //weatherStationControlThread.start();

        Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.add(Calendar.DAY_OF_MONTH, -15);

        System.out.println(prevCalendar);
        Executor ex = new Executor(0,0,0, new DeleteCreateFolderTask(), "daily");
        ex.start();
    }

    //{'id': id:Long, 'x': x:Integer, 'y': y:Integer, }
    @RequestMapping(value = "changeCoordinates", method = RequestMethod.POST)
    public void changeCoordinates(@RequestBody String jsonData){
        System.out.println("coordinate change");
        org.json.JSONObject data = new org.json.JSONObject(jsonData);

        ControlObject controlObject = controlObjectRepo.getById(data.getLong("id"));

        controlObject.setMapX(data.getInt("x"));
        controlObject.setMapY(data.getInt("y"));

        controlObjectRepo.save(controlObject);
    }

    @RequestMapping(value = "getMapAndListArraysJSON", method = RequestMethod.POST)
    public String getMapAndListArraysJSON(){

        List<ControlObject> controlObjectsToDisplay = controlObjectRepo.getControlObjectsToDisplay();
        String jsonStringObjectsToDisplay = "[";
        for (ControlObject controlObject : controlObjectsToDisplay) {
            jsonStringObjectsToDisplay += controlObject.getJsonStringWithMap();
            jsonStringObjectsToDisplay += ",";
        }
        jsonStringObjectsToDisplay = jsonStringObjectsToDisplay.substring(0,jsonStringObjectsToDisplay.length() - 1);
        jsonStringObjectsToDisplay += "]";
        

        List<ControlObject> controlObjectsList = controlObjectRepo.findAll();
        String jsonStringObjectsList = "[";
        for (ControlObject controlObject : controlObjectsList) {
            jsonStringObjectsList += controlObject.getJsonStringNoMap();
            jsonStringObjectsList += ",";
        }
        jsonStringObjectsList = jsonStringObjectsList.substring(0,jsonStringObjectsList.length() - 1);
        jsonStringObjectsList += "]";


        return "[" + jsonStringObjectsList +", " + jsonStringObjectsToDisplay + "]";
    }

}
