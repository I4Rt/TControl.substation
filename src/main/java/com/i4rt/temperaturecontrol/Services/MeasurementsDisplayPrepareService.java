package com.i4rt.temperaturecontrol.Services;

import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MIPMeasurementRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MeasurementRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.WeatherMeasurementRepo;
import com.i4rt.temperaturecontrol.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class MeasurementsDisplayPrepareService {
    
    @Autowired
    private static ControlObjectRepo controlObjectRepo;
    
    @Autowired
    private static WeatherMeasurementRepo weatherMeasurementRepo;
    
    @Autowired
    private static MeasurementRepo measurementRepo;

    @Autowired
    private static MIPMeasurementRepo mipMeasurementRepo;



    public MeasurementsDisplayPrepareService(ControlObjectRepo controlObjectRepo, WeatherMeasurementRepo weatherMeasurementRepo,
                                             MeasurementRepo measurementRepo, MIPMeasurementRepo mipMeasurementRepo){
        this.controlObjectRepo = controlObjectRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.measurementRepo = measurementRepo;
        this.mipMeasurementRepo = mipMeasurementRepo;
    }

    
    
    public static HashMap<String, Object> getPreparedMeasurementsArrays(Long id, Date beginning, Date ending){

        ControlObject controlObject = controlObjectRepo.getById(id);

        HashMap<String, Object> results = new HashMap();

        if(controlObject != null){
            ArrayList<MeasurementData> measurements = new ArrayList<>();
            measurements.addAll( weatherMeasurementRepo.getWeatherMeasurementByDatetimeInRange(beginning, ending));
            measurements.addAll( measurementRepo.getMeasurementByDatetimeInRange(id, beginning, ending));
            measurements.addAll( mipMeasurementRepo.getMIPMeasurementByDatetimeInRange(beginning, ending));

            Map<Date, Object> preparedToSendData = new HashMap<>();



            ArrayList<Date> totalDates = new ArrayList<>();


            for(MeasurementData measurementData : measurements){
                if(! totalDates.contains(measurementData.getDatetime())){
                    totalDates.add(measurementData.getDatetime());
                }
            }
            totalDates.sort(Comparator.naturalOrder());

            ArrayList<Double> tempWeatherArray = new ArrayList<>();
            ArrayList<Double> tempTemperatureArray = new ArrayList<>();
            ArrayList<Double> tempPowerArray = new ArrayList<>();

            for (int i = 0; i < totalDates.size(); i++){
                tempWeatherArray.add(null);
                tempTemperatureArray.add(null);
                tempPowerArray.add(null);
            }

            for(MeasurementData obj: measurements){
                if(obj instanceof WeatherMeasurement){
                    WeatherMeasurement finObj = (WeatherMeasurement) obj;
                    tempWeatherArray.set(totalDates.indexOf(obj.getDatetime()), finObj.getTemperature());
                }
                else if(obj instanceof Measurement){
                    Measurement finObj = (Measurement) obj;
                    tempTemperatureArray.set(totalDates.indexOf(finObj.getDatetime()), finObj.getTemperature());
                }
                else if(obj instanceof MIPMeasurement){
                    MIPMeasurement finObj = (MIPMeasurement) obj;
                    if(controlObject.getVoltageMeasurementChannel().equals("A"))
                        tempPowerArray.set(totalDates.indexOf(finObj.getDatetime()), finObj.getPowerA() / 1000);
                    else if(controlObject.getVoltageMeasurementChannel().equals("B"))
                        tempPowerArray.set(totalDates.indexOf(finObj.getDatetime()), finObj.getPowerB() / 1000);
                    else if(controlObject.getVoltageMeasurementChannel().equals("C"))
                        tempPowerArray.set(totalDates.indexOf(finObj.getDatetime()), finObj.getPowerC() / 1000);
                }
            }

            results.put("weather", tempWeatherArray);
            results.put("temperature", tempTemperatureArray);
            results.put("power", tempPowerArray);
            results.put("time", totalDates);
            results.put("message", "ok");
        }
        else{
            results.put("message", "NoSuchControlObject");
        }


        return results;
        
    }

    public static HashMap<String, Object> getPreparedMeasurementsArraysLimited(Long id, Integer limit){

        ControlObject controlObject = controlObjectRepo.getById(id);

        HashMap<String, Object> results = new HashMap();

        if(controlObject != null){
            ArrayList<MeasurementData> measurements = new ArrayList<>();
            ArrayList<Measurement> m = measurementRepo.getMeasurementByDatetime(id, limit);
            if(m.size() > 1){
                Date ending = m.get(0).getDatetime();
                Date beginning = m.get(m.size() - 1).getDatetime();
                System.out.println("date for mip: " + beginning + " -> " + ending);
                measurements.addAll(m);
                measurements.addAll( weatherMeasurementRepo.getWeatherMeasurementByDatetimeInRange(beginning, ending));
                measurements.addAll( mipMeasurementRepo.getMIPMeasurementByDatetimeInRange(beginning, ending));

                System.out.println("mip measurements in limited time: " + mipMeasurementRepo.getMIPMeasurementByDatetimeInRange(beginning, ending).size());
            }
            else{
                measurements.addAll(new ArrayList<Measurement>());
                measurements.addAll(new ArrayList<WeatherMeasurement>());
                measurements.addAll(new ArrayList<MIPMeasurement>());
            }


            Map<Date, Object> preparedToSendData = new HashMap<>();



            ArrayList<Date> totalDates = new ArrayList<>();


            for(MeasurementData measurementData : measurements){
                if(! totalDates.contains(measurementData.getDatetime())){
                    totalDates.add(measurementData.getDatetime());
                }
            }
            totalDates.sort(Comparator.naturalOrder());

            ArrayList<Double> tempWeatherArray = new ArrayList<>();
            ArrayList<Double> tempTemperatureArray = new ArrayList<>();
            ArrayList<Double> tempPowerArray = new ArrayList<>();

            for (int i = 0; i < totalDates.size(); i++){
                tempWeatherArray.add(null);
                tempTemperatureArray.add(null);
                tempPowerArray.add(null);
            }

            for(MeasurementData obj: measurements){
                if(obj instanceof WeatherMeasurement){
                    WeatherMeasurement finObj = (WeatherMeasurement) obj;
                    tempWeatherArray.set(totalDates.indexOf(obj.getDatetime()), finObj.getTemperature());
                }
                else if(obj instanceof Measurement){
                    Measurement finObj = (Measurement) obj;
                    tempTemperatureArray.set(totalDates.indexOf(finObj.getDatetime()), finObj.getTemperature());
                }
                else if(obj instanceof MIPMeasurement){
                    MIPMeasurement finObj = (MIPMeasurement) obj;
                    if(controlObject.getVoltageMeasurementChannel().equals("A"))
                        tempPowerArray.set(totalDates.indexOf(finObj.getDatetime()), finObj.getPowerA() / 1000);
                    else if(controlObject.getVoltageMeasurementChannel().equals("B"))
                        tempPowerArray.set(totalDates.indexOf(finObj.getDatetime()), finObj.getPowerB() / 1000);
                    else if(controlObject.getVoltageMeasurementChannel().equals("C"))
                        tempPowerArray.set(totalDates.indexOf(finObj.getDatetime()), finObj.getPowerC() / 1000);
                }

            }

            results.put("weather", tempWeatherArray);
            results.put("temperature", tempTemperatureArray);
            results.put("power", tempPowerArray);
            System.out.println("POWER SIZE: " + tempPowerArray);
            results.put("time", totalDates);
            results.put("message", "ok");
        }
        else{
            results.put("message", "NoSuchControlObject");
        }


        return results;

    }
    
    
}
