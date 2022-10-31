package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.Services.AlertHolder;
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
        AlertHolder alertHolder = AlertHolder.getInstance();
        while (true) {
            try {
                WeatherStation weatherStation = WeatherStation.getInstance();

                weatherStation.makeMeasurements();
                if (weatherStation.getTemperature() == 0 && weatherStation.getHumidity() == 0 &&
                        weatherStation.getAtmospherePressure() == 0 && weatherStation.getRainfall() == 0 &&
                        weatherStation.getWindForce() == 0) {
                    System.out.println("Weather Station Error");
                } else {
                    WeatherMeasurement weatherMeasurement = new WeatherMeasurement();

                    weatherMeasurement.setTemperature(weatherStation.getTemperature());
                    weatherMeasurement.setHumidity(weatherStation.getHumidity());
                    weatherMeasurement.setAtmospherePressure(weatherStation.getAtmospherePressure());
                    weatherMeasurement.setRainfall(weatherStation.getRainfall());
                    weatherMeasurement.setWindForce(weatherStation.getWindForce());
                    weatherMeasurement.setDateTime(Calendar.getInstance().getTime());

                    weatherMeasurementRepo.save(weatherMeasurement);
                }
                alertHolder.setWeatherStationError(false);
                Thread.sleep(5000);



            }catch(Exception e){
                alertHolder.setWeatherStationError(true);
                System.out.println(e);
            }
        }
    }
}
