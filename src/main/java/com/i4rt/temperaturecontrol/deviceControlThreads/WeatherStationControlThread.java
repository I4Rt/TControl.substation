package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.device.WeatherStation;
import com.i4rt.temperaturecontrol.model.WeatherMeasurement;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Setter
public class WeatherStationControlThread extends Thread{


    private WeatherMeasurementRepo weatherMeasurementRepo;







    public WeatherStationControlThread(WeatherMeasurementRepo weatherMeasurementRepo) {
        this.weatherMeasurementRepo = weatherMeasurementRepo;
    }

    @SneakyThrows
    public void run(){
        while(true){
            WeatherStation weatherStation = WeatherStation.getInstance();

            weatherStation.makeMeasurements();

            WeatherMeasurement weatherMeasurement = new WeatherMeasurement();

            weatherMeasurement.setTemperature(weatherStation.getTemperature());
            weatherMeasurement.setHumidity(weatherStation.getHumidity());
            weatherMeasurement.setAtmospherePressure(weatherStation.getAtmospherePressure());
            weatherMeasurement.setRainfall(weatherStation.getRainfall());
            weatherMeasurement.setWindForce(weatherStation.getWindForce());
            weatherMeasurement.setDateTime(Calendar.getInstance().getTime());

            weatherMeasurementRepo.save(weatherMeasurement);


            Thread.sleep(2000);
        }

    }
}
