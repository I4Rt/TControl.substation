package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MeasurementRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.ThermalImagerRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.UserRepo;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.User;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AddingPageRestController {

    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;
    @Autowired
    private final ThermalImagerRepo thermalImagerRepo;
    @Autowired
    private final UserRepo userRepo;

    public AddingPageRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
    }

    @RequestMapping(value = "getCoordinatesById", method = RequestMethod.POST)
    public String getCoordinates(@RequestParam Long id){

        ControlObject controlObject = controlObjectRepo.getById(id);

        ThermalImager ti = controlObject.getThermalImager();

        Map<String, Object> data = new HashMap<>();

        data.put("id", id);
        data.put("vertical", controlObject.getVertical());
        data.put("horizontal", controlObject.getHorizontal());

        data.put("focusing", controlObject.getFocusing());

        data.put("x", controlObject.getX());
        data.put("y", controlObject.getY());
        data.put("areaHeight", controlObject.getAreaHeight());
        data.put("areaWidth", controlObject.getAreaWidth());

        String newUrl = ti.gotoAndGetImage(controlObject.getHorizontal(), controlObject.getVertical(), controlObject.getFocusing());
        System.out.println(newUrl);
        data.put("newUrl", newUrl);

        data.put("newUrl", newUrl);

        String jsonStringToSend = JSONObject.valueToString(data);



        return jsonStringToSend;
    }

    @RequestMapping(value = "setCoordinatesThermalViewer", method = RequestMethod.POST)
    public String setCoordinates(@RequestBody String rowDataJson){

        JSONObject data = new JSONObject(rowDataJson);
        Long searchControlObjectId = data.getLong("id");
        Double vertical = data.getDouble("vertical");
        Double horizontal = data.getDouble("horizontal");
        Double focusing = data.getDouble("focusing");

        Integer x = data.getInt("x");
        Integer y = data.getInt("y");
        Integer areaHeight = data.getInt("areaHeight");
        Integer areaWidth = data.getInt("areaWidth");

        ControlObject controlObject = controlObjectRepo.getById(searchControlObjectId);

        controlObject.setVertical(vertical);
        controlObject.setHorizontal(horizontal);
        controlObject.setFocusing(focusing);

        controlObject.setX(x);
        controlObject.setY(y);
        controlObject.setAreaHeight(areaHeight);
        controlObject.setAreaWidth(areaWidth);

        controlObjectRepo.save(controlObject);

        return "Координаты объекта контроля изменены";
    }

    @RequestMapping(value = "gotoAndGetImage", method = RequestMethod.POST)
    public String gotoAndGetImage(@RequestBody String rowDataJson){
        JSONObject gotData = new JSONObject(rowDataJson);

        Long tiID = gotData.getLong("tiId");
        Double vertical = gotData.getDouble("vertical");
        Double horizontal = gotData.getDouble("horizontal");
        Double focusing = gotData.getDouble("focusing");

        ThermalImager ti = thermalImagerRepo.getById(tiID);

        Map<String, Object> data = new HashMap<>();

        data.put("vertical", vertical);
        data.put("horizontal", horizontal);
        data.put("focusing", focusing);

        String newUrl = ti.gotoAndGetImage(horizontal, vertical, focusing);
        System.out.println(newUrl);
        data.put("newUrl", newUrl);

        String jsonStringToSend = JSONObject.valueToString(data);

        return jsonStringToSend;
    }

    @RequestMapping(value = "setTINotInUse", method = RequestMethod.POST)
    public void setTINotInUse(){
        User curUser = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.setThermalImagerGrabbed(false);
        userRepo.save(curUser);
    }
}
