package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.additional.GotPicImageCounter;
import com.i4rt.temperaturecontrol.basic.HttpSenderService;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.User;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.SchemaOutputResolver;
import java.io.IOException;
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
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;

    public AddingPageRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
    }

    @RequestMapping(value = "getCoordinatesById", method = RequestMethod.POST)
    public String getCoordinates(@RequestParam Long id){

        ControlObject controlObject = controlObjectRepo.getById(id);
        Map<String, Object> data = new HashMap<>();
        ThermalImager ti = controlObject.getThermalImager();
        if(ti != null) {



            data.put("id", id);
            data.put("tiID", controlObject.getThermalImager().getId());
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

        }
        else{
            data.put("tiID", "null");
        }
        String jsonStringToSend = JSONObject.valueToString(data);
        return jsonStringToSend;
    }

    @RequestMapping(value = "setCoordinatesThermalViewer", method = RequestMethod.POST)
    public String setCoordinates(@RequestBody String rowDataJson){

        JSONObject data = new JSONObject(rowDataJson);
        Long searchControlObjectId = data.getLong("id");
        Long tiID = data.getLong("tiID");
        Double vertical = data.getDouble("vertical");
        Double horizontal = data.getDouble("horizontal");
        Double focusing = data.getDouble("focusing");

        Integer x = data.getInt("x");
        Integer y = data.getInt("y");
        Integer areaHeight = data.getInt("areaHeight");
        Integer areaWidth = data.getInt("areaWidth");

        ControlObject controlObject = controlObjectRepo.getById(searchControlObjectId);

        controlObject.setThermalImager(thermalImagerRepo.getById(tiID));
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

        if(ti.getIsBusy()){
            data.put("newUrl", "error");
        }
        else{
            try{
                String newUrl = ti.gotoAndGetImage(horizontal, vertical, focusing);
                System.out.println(newUrl);
                data.put("newUrl", newUrl);
            }
            catch (Exception e){
                data.put("newUrl", "conError");
            }
        }

//        while(ti.getIsBusy()){
//            try{
//                System.out.println("Waiting for TI finish its tasks");
//                Thread.sleep(500);
//            }
//            catch (Exception e){
//                System.out.println("Goto and Get Image Error Sleep: " + e);
//                data.put("newUrl", "error");
//                String jsonStringToSend = JSONObject.valueToString(data);
//                return jsonStringToSend;
//            }
//        }
//
//        try{
//            String newUrl = ti.gotoAndGetImage(horizontal, vertical, focusing);
//            System.out.println(newUrl);
//            data.put("newUrl", newUrl);
//        }
//        catch (Exception e){
//            data.put("newUrl", "conError");
//        }
//
//
//
        String jsonStringToSend = JSONObject.valueToString(data);

        return jsonStringToSend;
    }

    @RequestMapping(value = "setTINotInUse", method = RequestMethod.POST)
    public void setTINotInUse(){
        User curUser = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.setThermalImagerGrabbed(false);
        userRepo.save(curUser);
    }

    @RequestMapping(value = "autoFocusing", method = RequestMethod.POST)
    public String autoFocusing(@RequestParam(name = "thermalImagerId") Long id){
        Map<String, Object> result = new HashMap<>();
        try {

        ThermalImager ti = thermalImagerRepo.getById(id);

        result.put("focus", ti.setAutoFocus());

        HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(ti.getIP(), ti.getPort(), ti.getRealm(), ti.getNonce());

        httpSenderService.getImage(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\img", "/ISAPI/Streaming/channels/2/picture");

        result.put("newUrl", "img/got_pic" + GotPicImageCounter.getCurrentCounter() + ".jpg");

        String jsonStringToSend = JSONObject.valueToString(result);
        return jsonStringToSend;
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }


    }



}
