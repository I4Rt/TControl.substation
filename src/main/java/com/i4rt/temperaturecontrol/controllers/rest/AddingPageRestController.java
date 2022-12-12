package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.Services.ThermalImagersHolder;
import com.i4rt.temperaturecontrol.additional.GotPicImageCounter;
import com.i4rt.temperaturecontrol.basic.HttpSenderService;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.User;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.SchemaOutputResolver;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AddingPageRestController {

    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;

    @Autowired
    private final UserRepo userRepo;

    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;

    private boolean getUserIsAccessed(){
        User curUser = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        return curUser.getThermalImagerGrabbed();
    }



    public AddingPageRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
    }

    @RequestMapping(value = "getCoordinatesById", method = RequestMethod.POST)
    public String getCoordinates(HttpServletRequest request, HttpServletResponse response, @RequestParam Long id) throws IOException, ServletException {

        if(!getUserIsAccessed()) {
            return "doubleUsers";
        }


        ControlObject controlObject = controlObjectRepo.getById(id);
        Map<String, Object> data = new HashMap<>();




        if(controlObject.getThermalImager() != null){
            ThermalImager ti = ThermalImagersHolder.getTIByID(controlObject.getThermalImager());
//            Integer i = 0;
//            while(ti.getIsBusy()){
//                try{
//                    Thread.sleep(500);
//                }
//                catch (Exception e){
//                    System.out.println("Waiting for thermal imager not busy error: " + e);
//                }
//                finally{
//                    i += 1;
//                    if(i > 100){
//                        break;
//                    }
//                }
//                ti = thermalImagerRepo.getById(ti.getId());
//            }
//

            data.put("id", id);
            data.put("tiID", ThermalImagersHolder.getTIByID(controlObject.getThermalImager()).getId());
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
//        ti.setIsBusy(false);
//        thermalImagerRepo.save(ti);
        return jsonStringToSend;
    }

    @RequestMapping(value = "setCoordinatesThermalViewer", method = RequestMethod.POST)
    public String setCoordinates(HttpServletRequest request, HttpServletResponse response, @RequestBody String rowDataJson) throws IOException, ServletException {

        if(!getUserIsAccessed()) {
            return "doubleUsers";
        }

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

        controlObject.setThermalImager(tiID);
        controlObject.setVertical(vertical);
        controlObject.setHorizontal(horizontal);

        focusing = Double.valueOf(Math.round(focusing/100) * 100);

        controlObject.setFocusing(focusing);
        controlObject.setX(x);
        controlObject.setY(y);
        controlObject.setAreaHeight(areaHeight);
        controlObject.setAreaWidth(areaWidth);

        if(focusing > 599 && focusing < 6001){

            controlObjectRepo.save(controlObject);


            return "Координаты объекта контроля изменены";
        }
        else{
            return "Данные не сохранены.\nДопустимые значение параметра \"focusing\" находятся в диапазоне от 600 до 6000";
        }


    }

    @RequestMapping(value = "gotoAndGetImage", method = RequestMethod.POST)
    public String gotoAndGetImage(HttpServletRequest request, HttpServletResponse response, @RequestBody String rowDataJson) throws IOException, ServletException {

        if(!getUserIsAccessed()) {
            return "doubleUsers";
        }

        JSONObject gotData = new JSONObject(rowDataJson);

        Long tiID = gotData.getLong("tiId");
        Double vertical = gotData.getDouble("vertical");
        Double horizontal = gotData.getDouble("horizontal");
        Double focusing = gotData.getDouble("focusing");
        ThermalImager ti = ThermalImagersHolder.getTIByID(tiID);
//        Integer i = 0;
//        while(ti.getIsBusy()){
//            try{
//                Thread.sleep(500);
//            }
//            catch (Exception e){
//                System.out.println("Waiting for thermal imager not busy error: " + e);
//            }
//            finally{
//                i += 1;
//                if(i > 100){
//                    break;
//                }
//            }
//            ti = thermalImagerRepo.getById(tiID);
//        }
//
//        ti.setIsBusy(true);
//        thermalImagerRepo.save(ti);

        Map<String, Object> data = new HashMap<>();





        data.put("vertical", vertical);
        data.put("horizontal", horizontal);
        data.put("focusing", focusing);

        Integer sleepTimeCounter = 0;
        while(ti.getIsBusy()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sleepTimeCounter += 1;
            if(sleepTimeCounter > 100){
                data.put("newUrl", "error");
                String jsonStringToSend = JSONObject.valueToString(data);
                return jsonStringToSend;
            }
        }
        try{
            String newUrl = ti.gotoAndGetImage(horizontal, vertical, focusing);
            System.out.println(newUrl);
            data.put("newUrl", newUrl);
        }
        catch (Exception e){
            data.put("newUrl", "conError");
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

//        ti.setIsBusy(false);
//        thermalImagerRepo.save(ti);
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
    public String autoFocusing(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "thermalImagerId") Long id) throws IOException, ServletException {
        if(!getUserIsAccessed()) {
            return "doubleUsers";
        }

        Map<String, Object> result = new HashMap<>();
        try {

        ThermalImager ti = ThermalImagersHolder.getTIByID(id);
        Integer focus = ti.setAutoFocus();
        if(focus != -1){
            result.put("focus", focus);

            HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(ti.getIP(), ti.getPort(), ti.getRealm(), ti.getNonce());

            if (httpSenderService.getImage(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\img", "/ISAPI/Streaming/channels/2/picture").equals("conError")){
                result.put("newUrl", "conError");
            }
            else{
                result.put("newUrl", "img/got_pic" + GotPicImageCounter.getCurrentCounter() + ".jpg");
            }

        }
        else{
            result.put("focus", 800);
            result.put("newUrl", "img/disconnect.png");
            result.put("message", "Соединение с тепловизором не установлено.");

        }


        String jsonStringToSend = JSONObject.valueToString(result);
        return jsonStringToSend;
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }


    }

    @RequestMapping(value = "setNotBusy", method = RequestMethod.POST)
    void finishAdding(){
        User curUser = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.setThermalImagerGrabbed(false);
        userRepo.save(curUser);
    }



}
