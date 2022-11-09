package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MeasurementRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.WeatherMeasurementRepo;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.Measurement;
import com.i4rt.temperaturecontrol.model.WeatherMeasurement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.SchemaOutputResolver;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class AreaPageRestController {

    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;

    public AreaPageRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, WeatherMeasurementRepo weatherMeasurementRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
    }

    //Может быть баг с добавлением новой области
    //{"id": 1, "name": "Зона 1", "warningTemp": 60, "dangerTemp": 120}
    @RequestMapping(value = "saveArea", method = RequestMethod.POST)
    public String addingAreaPage(@RequestBody String dataJson){
        System.out.println("saving");
        System.out.println(dataJson);
        Map<String, String> result = new HashMap<>();

        try{
            JSONObject data = new org.json.JSONObject(dataJson);

            System.out.println(data.getString("name"));
            System.out.println(data.getDouble("warningTemp"));
            System.out.println(data.getDouble("dangerTemp"));


            ControlObject curControlObject = new ControlObject();
            curControlObject.setTemperatureClass("noData");
            if(! data.get("id").equals(null)){
                if(!(controlObjectRepo.findById(data.getLong("id")).isEmpty())){
                    curControlObject = controlObjectRepo.getById(data.getLong("id"));

                    if(! measurementRepo.getMeasurementByAreaId(curControlObject.getId(), 1).get(0).getTemperature().equals(null)){
                        curControlObject.setName(data.getString("name"));
                        curControlObject.setWarningTemp(data.getDouble("warningTemp"));
                        curControlObject.setDangerTemp(data.getDouble("dangerTemp"));
                        curControlObject.updateTemperatureClass(measurementRepo.getMeasurementByAreaId(curControlObject.getId(), 1).get(0).getTemperature(), weatherMeasurementRepo.getLastWeatherMeasurement().getTemperature());
                    }
                }
            }
            else{
                curControlObject.setName(data.getString("name"));
                curControlObject.setWarningTemp(data.getDouble("warningTemp"));
                curControlObject.setDangerTemp(data.getDouble("dangerTemp"));
            }




            result.put("temperatureClass", curControlObject.getTemperatureClass());

            controlObjectRepo.save(curControlObject);
            result.put("message", "Данные сохранены");
        }
        catch (Exception e){
            System.out.println(e);
            result.put("message", "Данные сохранены");
        }
        finally {
            String jsonStringToSend = JSONObject.valueToString(result);
            return jsonStringToSend;
        }
    }

    @RequestMapping(value="getTemperature",method = RequestMethod.POST)
    public String getTemperatureMeasurementJsonString(@RequestBody String dataJson){
        JSONObject data = new JSONObject(dataJson);
        Long searchControlObjectId = data.getLong("id");
        Integer limit = data.getInt("limit");

        List<Measurement> curMeasurements = measurementRepo.getMeasurementByDatetime(searchControlObjectId, limit);

        Map<String, Object> preparedToSendData = new HashMap<>();

        preparedToSendData.put("time", new ArrayList<String>());
        preparedToSendData.put("temperature", new ArrayList<Double>());
        preparedToSendData.put("temperatureClass", controlObjectRepo.getById(searchControlObjectId).getTemperatureClass());

        for(Measurement measurement: curMeasurements){
            ((ArrayList<String>)preparedToSendData.get("time")).add(measurement.getDatetime().toString());
            ((ArrayList<Double>)preparedToSendData.get("temperature")).add(measurement.getTemperature());
        }

        Date beginningDate = curMeasurements.get(0).getDatetime();
        Date endingDate = curMeasurements.get(curMeasurements.size() - 1).getDatetime();

        System.out.println("begin date " + beginningDate);
        System.out.println("end date " + endingDate);

        // getting images names:
        preparedToSendData.put("imagesNames", new ArrayList<String>());

        File folder = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData");
        File[] listOfFiles = folder.listFiles();

        System.out.println("basic: " + listOfFiles.length);

        for (int i = 0; i < listOfFiles.length; i++) {
            File insideFolder = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + listOfFiles[i].getName());
            File[] listOfInsideFolders = insideFolder.listFiles();

            for(int j = 0; j < listOfInsideFolders.length; j++){
                System.out.println("equals: " + listOfInsideFolders[j].getName().equals(controlObjectRepo.getById(searchControlObjectId).getName()));
                if(listOfInsideFolders[j].getName().equals(controlObjectRepo.getById(searchControlObjectId).getName())){
                    File insideFiles = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + listOfFiles[i].getName() + "\\" + listOfInsideFolders[j].getName());
                    File[] listOfInsideFiles = insideFiles.listFiles();
                    Arrays.sort(listOfInsideFiles, Comparator.comparingLong(File::lastModified));

                    for(int n = 0; n < listOfInsideFiles.length; n++){
                        Date tempDate = null; // select year!
                        try {
                            tempDate = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").parse(listOfFiles[i].getName() + "_" + Calendar.getInstance().get(Calendar.YEAR) + "_" + listOfInsideFiles[n].getName());
                            System.out.println("temp date: " + tempDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if(endingDate.compareTo(tempDate) <= 0 && beginningDate.compareTo(tempDate) >= 0){
                            ((ArrayList<String>)preparedToSendData.get("imagesNames")).add("imgData/" + listOfFiles[i].getName() + "/" + listOfInsideFolders[j].getName() + "/" + listOfInsideFiles[n].getName());
                        }
                    }


                }
            }



        }

        System.out.println("Images array: " + preparedToSendData.get("imagesNames"));

        String jsonStringToSend = JSONObject.valueToString(preparedToSendData);

        //System.out.println("temp_data: " + jsonStringToSend);

        return jsonStringToSend;
    }@RequestMapping(value="getWeatherTemperature",method = RequestMethod.POST)
    public String getTemperatureWeatherMeasurementJsonString(@RequestBody String dataJson){
        JSONObject data = new JSONObject(dataJson);



        String begin = data.getString("begin");
        String end = data.getString("end");

        try {
            Date beginningDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(begin);
            Date endingDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(end);
            System.out.println("dates " + beginningDate + " ----> " + endingDate);

            List<WeatherMeasurement> results = weatherMeasurementRepo.getWeatherMeasurementByDatetimeInRange(beginningDate, endingDate);

            System.out.println("Weather array size_" + results.size());

            Map<String, Object> preparedToSendData = new HashMap<>();

            preparedToSendData.put("time", new ArrayList<String>());
            preparedToSendData.put("weatherTemperature", new ArrayList<Double>());


            for(WeatherMeasurement weatherMeasurement: results){
                //System.out.println(weatherMeasurement.getId());
                ((ArrayList<String>)preparedToSendData.get("time")).add(weatherMeasurement.getDatetime().toString());

                ((ArrayList<Double>)preparedToSendData.get("weatherTemperature")).add(weatherMeasurement.getTemperature());
            }

            String jsonStringToSend = JSONObject.valueToString(preparedToSendData);

            return jsonStringToSend;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "error: getting temperature";

    }

    @RequestMapping(value="getTemperatureInRange",method = RequestMethod.POST)
    public String getTemperatureMeasurementInRangeJsonString(@RequestBody String dataJson){
        JSONObject data = new JSONObject(dataJson);
        Long searchControlObjectId = data.getLong("id");
        String begin = data.getString("begin");
        String end = data.getString("end");
        System.out.println("In Range begin: " + begin);
        System.out.println("In Range end: " + end);
        try {
            Date beginningDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(begin);
            Date endingDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(end);

            System.out.println("In Range begin: " + beginningDate);
            System.out.println("In Range end: " + endingDate);

            List<Measurement> results = measurementRepo.getMeasurementByDatetimeInRange(searchControlObjectId, beginningDate, endingDate);


            Map<String, Object> preparedToSendData = new HashMap<>();

            preparedToSendData.put("time", new ArrayList<String>());
            preparedToSendData.put("temperature", new ArrayList<Double>());
            preparedToSendData.put("temperatureClass", controlObjectRepo.getById(searchControlObjectId).getTemperatureClass());

            for(Measurement measurement: results){
                ((ArrayList<String>)preparedToSendData.get("time")).add(measurement.getDatetime().toString());
                ((ArrayList<Double>)preparedToSendData.get("temperature")).add(measurement.getTemperature());
            }


            preparedToSendData.put("imagesNames", new ArrayList<String>());

            File folder = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData");
            File[] listOfFiles = folder.listFiles();

            System.out.println("basic: " + listOfFiles.length);

            for (int i = 0; i < listOfFiles.length; i++) {
                File insideFolder = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + listOfFiles[i].getName());
                File[] listOfInsideFolders = insideFolder.listFiles();

                for(int j = 0; j < listOfInsideFolders.length; j++){
                    System.out.println("equals: " + listOfInsideFolders[j].getName().equals(controlObjectRepo.getById(searchControlObjectId).getName()));
                    if(listOfInsideFolders[j].getName().equals(controlObjectRepo.getById(searchControlObjectId).getName())){
                        File insideFiles = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + listOfFiles[i].getName() + "\\" + listOfInsideFolders[j].getName());
                        File[] listOfInsideFiles = insideFiles.listFiles();

                        for(int n = 0; n < listOfInsideFiles.length; n++){
                            Date tempDate = null; // select year!
                            try {
                                tempDate = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").parse(listOfFiles[i].getName() + "_" + Calendar.getInstance().get(Calendar.YEAR) + "_" + listOfInsideFiles[n].getName());
                                //System.out.println("temp date: " + tempDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            System.out.println("in range ending compare" + endingDate.compareTo(tempDate));
                            System.out.println("in range beginning compare" + beginningDate.compareTo(tempDate));
                            if((endingDate.compareTo(tempDate) == 1) && beginningDate.compareTo(tempDate) == -1){
                                ((ArrayList<String>)preparedToSendData.get("imagesNames")).add("imgData/" + listOfFiles[i].getName() + "/" + listOfInsideFolders[j].getName() + "/" + listOfInsideFiles[n].getName());
                            }
                        }
                    }
                }
            }
            //System.out.println("get images in range: " + preparedToSendData.get("imagesNames"));

            String jsonStringToSend = JSONObject.valueToString(preparedToSendData);
            return jsonStringToSend;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping(value = "deleteArea", method = RequestMethod.POST)
    public String deleteArea(@RequestParam Long id){
        measurementRepo.deleteByCOID(id);
        controlObjectRepo.deleteByID(id);

        System.out.println("Deleting area by id " + id);
        return "Область контроля удалена";
    }


}
