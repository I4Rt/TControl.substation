package com.i4rt.temperaturecontrol.Services;

import com.i4rt.temperaturecontrol.device.ThermalImager;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@ToString
public class ThermalImagersHolder {
    private static ArrayList<ThermalImager> tiList = new ArrayList<>();

    public ThermalImagersHolder(){
        ThermalImager firstTI = new ThermalImager(Long.valueOf(1), "192.168.127.21", "IP Camera(J3898)", "4d6d55334d7a67304e7a706a4e4459775a6a646c4f413d3d", 80, false, false);
        ThermalImager secondTI = new ThermalImager(Long.valueOf(2), "192.168.127.22", "IP Camera(G7837)", "5a4456694e32526c4d7a6b36597a51324d324d785a6a453d", 80, false, false);
        ThermalImager thirdTI = new ThermalImager(Long.valueOf(3), "192.168.127.23", "IP Camera(J1488)", "4d5749344e574e6a5a6a6736597a51324d445a6b4e474d3d", 80, false, false);
        ThermalImager fourthTI = new ThermalImager(Long.valueOf(4), "192.168.127.24", "IP Camera(J3898)", "4e32526c4f5464684f475536597a51794e6a4d315a54593d", 80, false, false);

        tiList.add(firstTI);
        tiList.add(secondTI);
        tiList.add(thirdTI);
        tiList.add(fourthTI);
    }


    public static ThermalImager getTIByID(Long thermalImager) {

        System.out.println(tiList.get(Math.toIntExact(thermalImager) - 1));

        return tiList.get(Math.toIntExact(thermalImager) - 1);
    }
    public static ArrayList<ThermalImager> findAll(){
        for(ThermalImager ti : tiList){
            System.out.println(ti);
        }
        return tiList;

    }
}
