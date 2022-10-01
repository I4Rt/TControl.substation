package com.i4rt.temperaturecontrol;

import com.i4rt.temperaturecontrol.deviceControlThreads.DefaultTemperatureCheck;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TemperatureControlApplication {



    public static void main(String[] args) {



        SpringApplication.run(TemperatureControlApplication.class, args);

    }

}
