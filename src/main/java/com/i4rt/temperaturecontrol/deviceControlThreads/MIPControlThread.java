package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.Services.AlertHolder;
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


    public static BufferedReader in = null;

    public static MIPMeasurement lastMeasurement = new MIPMeasurement();



    @SneakyThrows
    public void run(){

        // Hope it helps to handle mistook connections problem
        try{
            in.close();
        }
        catch (Exception e){
            System.out.println("Drop connection error: " + e);
        }
        finally{
            in = null;
        }

        while(true){


            try {

                URL oracle = new URL("http://192.168.200.31/eventsource/telemech.csv");
                in  = new BufferedReader(new InputStreamReader(oracle.openStream()));

                while (true){
                    AlertHolder alertHolder = AlertHolder.getInstance();
                    alertHolder.setMIPError(false);
                    String resultString = in.readLine();
                    if(resultString != null){
                        if(resultString.length() > 20){
                            System.out.println(resultString);
                            String[] dataArray = resultString.split(",");
                            try{
                                MIPMeasurement mipMeasurement = new MIPMeasurement();
                                mipMeasurement.setAmperageA(Double.parseDouble(dataArray[22]));
                                mipMeasurement.setAmperageB(Double.parseDouble(dataArray[23]));
                                mipMeasurement.setAmperageC(Double.parseDouble(dataArray[24]));
                                mipMeasurement.setVoltageA(Double.parseDouble(dataArray[34]));
                                mipMeasurement.setVoltageB(Double.parseDouble(dataArray[35]));
                                mipMeasurement.setVoltageC(Double.parseDouble(dataArray[36]));
                                mipMeasurement.setPowerA(Double.parseDouble(dataArray[45]));
                                mipMeasurement.setPowerB(Double.parseDouble(dataArray[46]));
                                mipMeasurement.setPowerC(Double.parseDouble(dataArray[47]));
                                mipMeasurement.setDatetime(Calendar.getInstance().getTime());
                                lastMeasurement = mipMeasurement;
                            }catch (Exception e){
                                System.out.println("MIP split error: " + e);
                            }
                        }

                        in.mark(0);
                        in.reset();
                    }
                }
            } catch (Exception e) {
                AlertHolder alertHolder = AlertHolder.getInstance();
                alertHolder.setMIPError(true);
                if(in != null){
                    in.close();
                }
                File f = new File("error.txt");
                f.createNewFile();
                FileWriter writer = new FileWriter(f, true);
                writer.write(Calendar.getInstance().getTime() + "MIP error: " );
                writer.write(e.toString());
                writer.close();

                System.out.println("MIP error: " );
                System.out.println(e.toString());
                Thread.sleep(5000);

                MIPControlThread mipControlThread = new MIPControlThread();
                mipControlThread.start();
                return;
            }

        }

    }
}
