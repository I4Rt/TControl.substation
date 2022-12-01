package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Setter
@Getter
@NoArgsConstructor
public class ThermalImagersMainControlThread extends Thread {

    private static ThermalImagersMainControlThread instance;


    private ControlObjectRepo controlObjectRepo;

    private MeasurementRepo measurementRepo;
    private WeatherMeasurementRepo weatherMeasurementRepo;

    private ThermalImagerRepo thermalImagerRepo;

    private UserRepo userRepo;

    private final Integer maxChildThreadsCountBeforeReboot = 1000;
    private HashMap<Long, Integer> curChildThreadsCount;



    public ThermalImagersMainControlThread(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, WeatherMeasurementRepo weatherMeasurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
    }

    public static void setInstance(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo){

        if(instance == null){
            instance = new ThermalImagersMainControlThread();
        }
        instance.setControlObjectRepo(controlObjectRepo);
        instance.setMeasurementRepo(measurementRepo);
        instance.setThermalImagerRepo(thermalImagerRepo);
        instance.weatherMeasurementRepo = weatherMeasurementRepo;
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

        curChildThreadsCount = new HashMap<>();


        for(ThermalImager ti : thermalImagerRepo.findAll()){
            ti.setIsBusy(false);
            ti.setNeedReboot(true);
            thermalImagerRepo.save(ti);

            curChildThreadsCount.put(ti.getId(), 0);
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
                        curChildThreadsCount.merge(thermalImagers.get(i).getId(), 1, Integer::sum);
                        if(thermalImagers.get(i).getNeedReboot()){
                            System.out.println("rebooting" + thermalImagers.get(i).getId());
                            thermalImagers.get(i).setNeedReboot(false);
                            //thermalImagers.get(i).reboot();

                        }
                        else{
                            System.out.println("Starting thermal imager thread");
                            System.out.println("parent thread cur: " + thermalImagers.get(i).getId());
                            TITemperatureCheckThread tiTemperatureCheckThread= new TITemperatureCheckThread(this.controlObjectRepo, measurementRepo, thermalImagerRepo, weatherMeasurementRepo, userRepo, thermalImagers.get(i).getId());

                            tiTemperatureCheckThread.start();

                            thermalImagers.get(i).setIsBusy(true);

                            if(curChildThreadsCount.get(thermalImagers.get(i).getId()) >= maxChildThreadsCountBeforeReboot){
                                System.out.println("set need reboot ti" + thermalImagers.get(i).getId());
                                thermalImagers.get(i).setNeedReboot(true);
                                curChildThreadsCount.put(thermalImagers.get(i).getId(), 0);
                            }
                        }
                        thermalImagerRepo.save(thermalImagers.get(i));
                    }
                    else{
                        Thread.sleep(500);
                    }
                }
            }
        }
    }
}
