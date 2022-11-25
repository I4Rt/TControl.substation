package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.databaseInterfaces.MIPMeasurementRepo;
import com.i4rt.temperaturecontrol.model.MIPMeasurement;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.*;
import java.net.URL;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;

@Setter
@NoArgsConstructor
public class MIPControlThread extends Thread{


    private MIPMeasurementRepo voltageMeasurementRepo;

    public static MIPMeasurement lastMeasurement = new MIPMeasurement();



    @SneakyThrows
    public void run(){

        while(true){

            try {
                System.out.println("MIP Read Try");
                URL oracle = new URL("http://192.168.200.31/eventsource/telemech.csv");
                BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

                while (true){
                    String resultString = in.readLine();
                    if(!resultString.equals(null) | resultString.length() < 2){
                        resultString.replace("data: ", "");
                        String[] dataArray = resultString.split(",");

                        MIPMeasurement mipMeasurement = new MIPMeasurement();
                        mipMeasurement.setAmperageA(Double.parseDouble(dataArray[22]));
                        mipMeasurement.setAmperageB(Double.parseDouble(dataArray[23]));
                        mipMeasurement.setAmperageC(Double.parseDouble(dataArray[24]));
                        mipMeasurement.setVoltageA(Double.parseDouble(dataArray[34]));
                        mipMeasurement.setVoltageB(Double.parseDouble(dataArray[35]));
                        mipMeasurement.setVoltageC(Double.parseDouble(dataArray[36]));
                        mipMeasurement.setPowerA(Double.parseDouble(dataArray[45]));
                        mipMeasurement.setPowerA(Double.parseDouble(dataArray[46]));
                        mipMeasurement.setPowerA(Double.parseDouble(dataArray[47]));
                        mipMeasurement.setDatetime(Calendar.getInstance().getTime());
                        lastMeasurement = mipMeasurement;
                        in.close();
                        in = new BufferedReader(new InputStreamReader(oracle.openStream()));
                    }


                }

            } catch (Exception e) {
                File f = new File("error.txt");
                f.createNewFile();
                FileWriter writer = new FileWriter(f, true);
                writer.write(Calendar.getInstance().getTime() + "MIP error: " );
                writer.write(e.toString());
                writer.close();

                System.out.println("MIP error: " );
                System.out.println(e.toString());
                System.out.println("MIPCT Break run again");
                //recursy?
                MIPControlThread mipControlThread = new MIPControlThread();
                mipControlThread.start();
            }

        }

    }
}
