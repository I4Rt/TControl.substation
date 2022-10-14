package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MeasurementRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.ThermalImagerRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.UserRepo;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.Measurement;
import com.i4rt.temperaturecontrol.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


@Setter
@Getter
@NoArgsConstructor
public class ThermalImagersMainControlThread extends Thread {

    private static ThermalImagersMainControlThread instance;


    private ControlObjectRepo controlObjectRepo;

    private MeasurementRepo measurementRepo;

    private ThermalImagerRepo thermalImagerRepo;

    private UserRepo userRepo;



    public ThermalImagersMainControlThread(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
    }

    public static void setInstance(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo){

        if(instance == null){
            instance = new ThermalImagersMainControlThread();
        }
        instance.setControlObjectRepo(controlObjectRepo);
        instance.setMeasurementRepo(measurementRepo);
        instance.setThermalImagerRepo(thermalImagerRepo);
        instance.setUserRepo(userRepo);
    }
    public static ThermalImagersMainControlThread getInstance(){
        if(instance == null){
            instance = new ThermalImagersMainControlThread();
        }
        return instance;
    }



    @SneakyThrows
    public void run(){

        for(ThermalImager ti : thermalImagerRepo.findAll()){
            System.out.println("set thermal imager");
            ti.setIsBusy(false);
            thermalImagerRepo.save(ti);
        }

        for(User u : userRepo.findAll()){
            System.out.println("set user");
            u.setThermalImagerGrabbed(false);
            userRepo.save(u);
        }

        User user = null;
        while(true) {
            user = userRepo.getUserThatGrabbedThermalImager();
            if (user != null) {
                //System.out.println("\n\n" + user.getThermalImagerGrabbed() + "\n\n");
                Thread.sleep(500);
                continue;
            } else {
                List<ThermalImager> thermalImagers = thermalImagerRepo.findAll();


                for(Integer i = 0; i < thermalImagers.size(); i++){
                    //проверка на занятость
                    if( !thermalImagers.get(i).getIsBusy() ){
                        System.out.println("Starting thermal imager thread");
                        TITemperatureCheckThread tiTemperatureCheckThread= new TITemperatureCheckThread(this.controlObjectRepo, measurementRepo, thermalImagerRepo, userRepo, thermalImagers.get(i).getId());

                        tiTemperatureCheckThread.start();

                        thermalImagers.get(i).setIsBusy(true);

                        thermalImagerRepo.save(thermalImagers.get(i));

                    }


                }
            }

        }

    }
}
