package com.i4rt.temperaturecontrol.deviceControlThreads;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Setter
@NoArgsConstructor
public class DefaultTemperatureCheck extends Thread{

    private static DefaultTemperatureCheck instance;


    private ControlObjectRepo controlObjectRepo;

    private MeasurementRepo measurementRepo;

    private ThermalImagerRepo thermalImagerRepo;

    private UserRepo userRepo;



    public DefaultTemperatureCheck(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
    }

    public static void setInstance(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo){

        if(instance == null){
            instance = new DefaultTemperatureCheck();
        }
        instance.setControlObjectRepo(controlObjectRepo);
        instance.setMeasurementRepo(measurementRepo);
        instance.setThermalImagerRepo(thermalImagerRepo);
        instance.setUserRepo(userRepo);
    }
    public static DefaultTemperatureCheck getInstance(){
        if(instance == null){
            instance = new DefaultTemperatureCheck();
        }
        return instance;
    }



    @SneakyThrows
    public void run(){
        User user = null;
        while(true){
            System.out.println("\n\nНачало измерений\n\n");
            List<ThermalImager> thermalImagers = thermalImagerRepo.findAll();

            for(Integer i = 0; i < thermalImagers.size(); i++){
                //проверка на занятость


                List<ControlObject> curControlObjects = thermalImagers.get(i).getControlObjectsArray();

                for (ControlObject co : curControlObjects){
                    user = userRepo.getUserThatGrabbedThermalImager();;
                    if(user != null){
                        System.out.println("\n\n" + user.getThermalImagerGrabbed() + "\n\n");
                        Thread.sleep(1000);
                        continue;
                    }

                    Double curTemperature = thermalImagers.get(i).getTemperature(co.getHorizontal(), co.getVertical(), co.getX(), co.getMapY(), co.getFocusing());
                    Measurement newData = new Measurement();

                    newData.setTemperature(curTemperature);

                    newData.setDatetime(new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").format(Calendar.getInstance().getTime()));

                    co.addMeasurement(newData);
                    newData.setControlObject(co);

                    measurementRepo.save(newData);

                    co.updateTemperatureClass(curTemperature);

                    controlObjectRepo.save(co);

                    Thread.sleep(5000);
                }


            }


        }


    }
}
