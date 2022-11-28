package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.databaseInterfaces.MIPMeasurementRepo;
import com.i4rt.temperaturecontrol.model.MIPMeasurement;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

@Setter
@NoArgsConstructor
public class MIPSaver extends Thread{


    private MIPMeasurementRepo MIPMeasurementRepo;


    public MIPSaver(MIPMeasurementRepo voltageMeasurementRepo){
        this.MIPMeasurementRepo = voltageMeasurementRepo;
    }



    @SneakyThrows
    public void run(){

        while(true){

            try {

                while (true){
                    Thread.sleep(20000);

                    MIPMeasurement mipMeasurement = new MIPMeasurement();
                    mipMeasurement.setPowerA(MIPControlThread.lastMeasurement.getPowerA());
                    mipMeasurement.setPowerB(MIPControlThread.lastMeasurement.getPowerB());
                    mipMeasurement.setPowerC(MIPControlThread.lastMeasurement.getPowerC());
                    mipMeasurement.setAmperageA(MIPControlThread.lastMeasurement.getAmperageA());
                    mipMeasurement.setAmperageB(MIPControlThread.lastMeasurement.getAmperageB());
                    mipMeasurement.setAmperageC(MIPControlThread.lastMeasurement.getAmperageC());
                    mipMeasurement.setVoltageA(MIPControlThread.lastMeasurement.getVoltageA());
                    mipMeasurement.setVoltageB(MIPControlThread.lastMeasurement.getVoltageB());
                    mipMeasurement.setVoltageC(MIPControlThread.lastMeasurement.getVoltageC());
                    mipMeasurement.setDatetime(MIPControlThread.lastMeasurement.getDatetime());
                    MIPMeasurementRepo.save(mipMeasurement);


                }

            } catch (Exception e) {
                System.out.println("MIP save data error: " );
                System.out.println(e.toString());

                MIPSaver mipSaver = new MIPSaver(MIPMeasurementRepo);
                mipSaver.start();
                return;

            }

        }

    }
}
