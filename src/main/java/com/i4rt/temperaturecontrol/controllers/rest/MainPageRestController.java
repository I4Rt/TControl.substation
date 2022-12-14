package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.deviceControlThreads.MIPControlThread;
import com.i4rt.temperaturecontrol.deviceControlThreads.MIPSaver;
import com.i4rt.temperaturecontrol.deviceControlThreads.ThermalImagersMainControlThread;
import com.i4rt.temperaturecontrol.deviceControlThreads.WeatherStationControlThread;
import com.i4rt.temperaturecontrol.model.ControlObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;


@RestController
public class MainPageRestController {
    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;

    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;
    @Autowired
    private final MIPMeasurementRepo mipMeasurementRepo;

    public MainPageRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo, MIPMeasurementRepo mipMeasurementRepo) {

        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.mipMeasurementRepo = mipMeasurementRepo;

        userRepo.setThermalImagerNotGrabbedForAllUsers();

        ThermalImagersMainControlThread.setInstance(controlObjectRepo, measurementRepo, userRepo, weatherMeasurementRepo);

        ThermalImagersMainControlThread thermalImagersMainControlThread = ThermalImagersMainControlThread.getInstance();

        thermalImagersMainControlThread.start();

        WeatherStationControlThread weatherStationControlThread = new WeatherStationControlThread(weatherMeasurementRepo);

        weatherStationControlThread.start();

        MIPControlThread mipControlThread = new MIPControlThread();
        mipControlThread.start();

        MIPSaver mipSaver = new MIPSaver(this.mipMeasurementRepo);
        mipSaver.start();


        Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.add(Calendar.DAY_OF_MONTH, -15);

        System.out.println(prevCalendar);
//        Executor ex = new Executor(0,0,0, new DeleteCreateFolderTask(), "daily");
//        ex.start();

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
        if(jsonStringObjectsToDisplay.length() > 1){
            jsonStringObjectsToDisplay = jsonStringObjectsToDisplay.substring(0,jsonStringObjectsToDisplay.length() - 1);
        }
        jsonStringObjectsToDisplay += "]";

        System.out.println("objects to display: " + jsonStringObjectsToDisplay);


        List<ControlObject> controlObjectsList = controlObjectRepo.getOrderedByName();
        String jsonStringObjectsList = "[";

        for (ControlObject controlObject : controlObjectsList) {
            jsonStringObjectsList += controlObject.getJsonStringNoMap();
            jsonStringObjectsList += ",";
        }
        jsonStringObjectsList = jsonStringObjectsList.substring(0,jsonStringObjectsList.length() - 1);
        jsonStringObjectsList += "]";

        System.out.println("objects to list: " + jsonStringObjectsList);

        System.out.println("[" + jsonStringObjectsList +", " + jsonStringObjectsToDisplay + "]");
        return "[" + jsonStringObjectsList +", " + jsonStringObjectsToDisplay + "]";
    }


    @RequestMapping(value = "/dropMapCoordinates", method = RequestMethod.PUT)
    public void dropCoordinates(@RequestParam Long id){
        ControlObject co = controlObjectRepo.getById(id);

        co.setMapX(null);
        co.setMapY(null);

        controlObjectRepo.save(co);
    }
}
