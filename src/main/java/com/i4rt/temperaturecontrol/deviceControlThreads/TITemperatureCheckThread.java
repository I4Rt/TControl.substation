package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.Services.AlertHolder;
import com.i4rt.temperaturecontrol.Services.ThermalImagersHolder;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.Measurement;
import com.i4rt.temperaturecontrol.model.User;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.SQLOutput;
import java.util.*;
import java.util.List;

@Setter
@NoArgsConstructor
public class TITemperatureCheckThread extends Thread{


    private ControlObjectRepo controlObjectRepo;

    private MeasurementRepo measurementRepo;


    private WeatherMeasurementRepo weatherMeasurementRepo;
    private UserRepo userRepo;
    
    private Long thermalImagerID;

    



    public TITemperatureCheckThread(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, WeatherMeasurementRepo weatherMeasurementRepo, UserRepo userRepo, Long thermalImagerID) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.userRepo = userRepo;
        this.thermalImagerID = thermalImagerID;

    }

    private static class AlertSetter{
        protected static void setAlert(Long id, Boolean value){
            AlertHolder alertHolder = AlertHolder.getInstance();
            System.out.println("id:" + id.toString());
            switch (id.toString()){
                case "1":
                    alertHolder.setFirstTIError(value);
                    break;
                case "2":
                    alertHolder.setSecondTIError(value);
                    break;
                case "3":
                    alertHolder.setThirdTIError(value);
                    break;
                case "4":
                    alertHolder.setFourthTIError(value);
                    break;
            }
        }
    }

    



    @SneakyThrows
    public void run(){
        AlertHolder alertHolder = AlertHolder.getInstance();
        ThermalImager thermalImager = ThermalImagersHolder.getTIByID(thermalImagerID);
        System.out.println("cur: " + thermalImagerID);
        try {
            System.out.println("Begin leaf thread");
            User user = null;

            System.out.println("Thermal imager found");

            if (thermalImager != null) {
                if(!thermalImager.status().equals("ok")){
                    System.out.println("TI unavailable");
                    AlertSetter.setAlert(thermalImager.getId(), true);
                    throw new Exception();
                }
                else{
                    System.out.println("Setting false");
                    AlertSetter.setAlert(thermalImager.getId(), false);
                }


                List<ControlObject> curControlObjects = this.controlObjectRepo.getControlObjectByTIID(thermalImager.getId());
                System.out.println("Collection size " + curControlObjects.size());
                Collections.sort(curControlObjects);
            /*
            List<ControlObject> curControlObjects = thermalImager.getControlObjectsArray();
            System.out.println("Collection size " + curControlObjects.size());

            Collections.sort(curControlObjects);

            List<Long> existsID = new ArrayList<>();


            List<ControlObject> curControlObjectsExist = new ArrayList<>();



            for(ControlObject co : curControlObjects){

                if(! existsID.contains(co.getId())){
                    existsID.add(co.getId());
                    curControlObjectsExist.add(co);
                }


            }

             */


                for (ControlObject co : curControlObjects) {
                    user = userRepo.getUserThatGrabbedThermalImager();
                    if (user != null) {
                        System.out.println("User is busy breaking leaf thread");
                        System.out.println("\n\n" + user.getThermalImagerGrabbed() + "\n\n");
                        break;
                    }
                    Thread.sleep(1000); // ???
                    System.out.println("Checking " + co.getName());
                    if (co.getHorizontal() != null && co.getVertical() != null && co.getFocusing() != null) {
                        System.out.println("Begin move to coordinates " + co.getName());
                        String result1 =  thermalImager.gotoAndSaveImage(co.getHorizontal(), co.getVertical(), co.getFocusing(), co.getName());
                        if(!result1.equals("ok")){
                            System.out.println("Did not came to point " + co.getName());
                            throw new Exception();
                        }
                        else{
                            System.out.println("Going to the point result (" + co.getName() + ") is " + result1);
                            if (co.getX() != null && co.getY() != null && co.getAreaWidth() != null && co.getAreaHeight() != null) {
                                String configureResult = thermalImager.configureArea(co.getX(), co.getY(), co.getX() + co.getAreaWidth(), co.getY() + co.getAreaHeight());
                                System.out.println("Configure result to " + co.getName() + " is " + configureResult);
                                if (configureResult.equals("ok")) {

                                    //focussing

                                    Double curTemperature = (Double) thermalImager.getTemperatureInArea(1);
                                    System.out.println("Temperature of " + co.getName() + " is " + curTemperature);
                                    if (curTemperature != null) {
                                        Measurement newData = new Measurement();

                                        newData.setTemperature(curTemperature);
                                        if(weatherMeasurementRepo.getLastWeatherMeasurement() != null){
                                            newData.setWeatherTemperatureDifference(Math.abs(curTemperature - weatherMeasurementRepo.getLastWeatherMeasurement().getTemperature())); // !
                                        }
                                        else{
                                            newData.setWeatherTemperatureDifference(Double.valueOf(0)); // !
                                        }


                                        newData.setDatetime(Calendar.getInstance().getTime());

                                        co.addMeasurement(newData);
                                        newData.setControlObject(co);


                                        ControlObject coToSave = controlObjectRepo.getById(co.getId());


                                        coToSave.updateTemperatureClass(curTemperature,weatherMeasurementRepo.getLastWeatherMeasurement() != null ? weatherMeasurementRepo.getLastWeatherMeasurement().getTemperature() : null);

                                        coToSave.addMeasurement(newData);
                                        newData.setControlObject(coToSave);

                                        System.out.println(coToSave.getId());
                                        System.out.println(coToSave.getName());
                                        System.out.println(coToSave.getDangerTemp());
                                        System.out.println(coToSave.getWarningTemp());
                                        System.out.println(coToSave.getTemperatureClass());

                                        controlObjectRepo.save(coToSave);
                                        System.out.println("saved");

                                        measurementRepo.save(newData);

                                        System.out.println("Data got");

                                        AlertSetter.setAlert(thermalImager.getId(), false);
                                        System.out.println(alertHolder);

                                    } else {
                                        System.out.println("Getting temperature error");
                                        AlertSetter.setAlert(thermalImager.getId(), true);
                                    }

                                } else {
                                    System.out.println("Configuring area error");
                                    AlertSetter.setAlert(thermalImager.getId(), true);
                                }
                            } else {
                                System.out.println("passing");
                                continue;
                            }
                        }
                    }
                    //Thread.sleep(5000);
                }
            } else {
                System.out.println("Passing: Thermal imager with current id is not exist");
            }
            thermalImager.setIsBusy(false);


        }
        catch (Exception e){
            thermalImager.setIsBusy(false);
            System.out.println("Leaf thread error: " + e);
            AlertSetter.setAlert(thermalImager.getId(), true);
            System.out.println(alertHolder);
        }
        Thread.sleep(1000);
    }
}
