package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MeasurementRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.ThermalImagerRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.UserRepo;
import com.i4rt.temperaturecontrol.deviceControlThreads.DefaultTemperatureCheck;
import com.i4rt.temperaturecontrol.model.ControlObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    public MainPageRestHolder(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo) {

        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;

        DefaultTemperatureCheck.setInstance(controlObjectRepo, measurementRepo, thermalImagerRepo, userRepo);

        DefaultTemperatureCheck dtcThread = DefaultTemperatureCheck.getInstance();

        dtcThread.start();
    }

    //{'id': id:Long, 'x': x:Integer, 'y': y:Integer, }
    @RequestMapping(value = "changeCoordinates", method = RequestMethod.POST)
    public void changeCoordinates(@RequestBody String jsonData){
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
