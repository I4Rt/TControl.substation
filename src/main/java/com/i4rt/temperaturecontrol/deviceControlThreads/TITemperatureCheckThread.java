package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.Services.ConnectionHolder;
import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MeasurementRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.ThermalImagerRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.UserRepo;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.Measurement;
import com.i4rt.temperaturecontrol.model.User;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Setter
@NoArgsConstructor
public class TITemperatureCheckThread extends Thread{


    private ControlObjectRepo controlObjectRepo;

    private MeasurementRepo measurementRepo;

    private ThermalImagerRepo thermalImagerRepo;
    private UserRepo userRepo;
    
    private Long thermalImagerID;

    



    public TITemperatureCheckThread(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo, Long thermalImagerID) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
        this.thermalImagerID = thermalImagerID;
    }

    



    @SneakyThrows
    public void run(){
        try {
            System.out.println("Begin leaf thread");
            User user = null;
            ThermalImager thermalImager = thermalImagerRepo.getById(thermalImagerID);
            System.out.println("Thermal imager found");

            if (thermalImager != null) {
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
                    } else {
                        System.out.println("User not busy");
                    }
                    Thread.sleep(1000); // ???
                    System.out.println("Checking " + co.getName());
                    if (co.getHorizontal() != null && co.getVertical() != null && co.getFocusing() != null) {
                        System.out.println("Begin move to coordinates");
                        thermalImager.gotoAndSaveImage(co.getHorizontal(), co.getVertical(), co.getFocusing(), co.getName());
                        System.out.println("Moved to coordinates");

                        if (co.getX() != null && co.getY() != null && co.getAreaWidth() != null && co.getAreaHeight() != null) {
                            String configureResult = thermalImager.configureArea(co.getX(), co.getY(), co.getX() + co.getAreaWidth(), co.getY() + co.getAreaHeight());
                            System.out.println("Moving result " + configureResult);
                            if (configureResult.equals("ok")) {

                                //focussing

                                Double curTemperature = (Double) thermalImager.getTemperatureInArea(1);

                                if (curTemperature != null) {
                                    Measurement newData = new Measurement();

                                    newData.setTemperature(curTemperature);

                                    newData.setDatetime(Calendar.getInstance().getTime());

                                    co.addMeasurement(newData);
                                    newData.setControlObject(co);


                                    ControlObject coToSave = controlObjectRepo.getById(co.getId());


                                    coToSave.updateTemperatureClass(curTemperature);


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

                                } else {
                                    System.out.println("Getting temperature error");
                                }

                            } else {
                                System.out.println("Configuring area error");
                            }
                        } else {
                            continue;
                        }
                    }
                    //Thread.sleep(5000);
                }
                thermalImager.setIsBusy(false);

                thermalImagerRepo.save(thermalImager);
            } else {
                System.out.println("Passing: Thermal imager with current id is not exist");
            }

        }
        catch (Exception e){
            System.out.println(e);
            run();
        }
    }
}
