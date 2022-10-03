package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MeasurementRepo;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.Measurement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class AreaPageRestController {

    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;

    public AreaPageRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
    }

    //Может быть баг с добавлением новой области
    //{"id": 1, "name": "Зона 1", "warningTemp": 60, "dangerTemp": 120}
    @RequestMapping(value = "saveArea", method = RequestMethod.POST)
    public String addingAreaPage(@RequestBody String dataJson){
        try{
            JSONObject data = new org.json.JSONObject(dataJson);

            System.out.println(data.getLong("id"));
            System.out.println(data.getString("name"));
            System.out.println(data.getDouble("warningTemp"));
            System.out.println(data.getDouble("dangerTemp"));

            ControlObject curControlObject = new ControlObject();
            curControlObject.setTemperatureClass("noData");

            if(!(controlObjectRepo.findById(data.getLong("id")).isEmpty())){
                curControlObject = controlObjectRepo.getById(data.getLong("id"));
            }


            curControlObject.setName(data.getString("name"));
            curControlObject.setWarningTemp(data.getDouble("warningTemp"));
            curControlObject.setDangerTemp(data.getDouble("dangerTemp"));

            System.out.println("dt: " + curControlObject.getDangerTemp());


            controlObjectRepo.save(curControlObject);
            curControlObject = controlObjectRepo.getById(data.getLong("id"));
            System.out.println("dt after: " + curControlObject.getDangerTemp());
        }
        catch (Exception e){
            System.out.println(e);
            return "Произошла ошибка, попробуйте обновить страницу и повторить операцию. В случае неудаче отправьте лог разработчику.";
        }
        return "Данные сохранены";
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
            ((ArrayList<String>)preparedToSendData.get("time")).add(measurement.getDatetime());
            ((ArrayList<Double>)preparedToSendData.get("temperature")).add(measurement.getTemperature());
        }

        String jsonStringToSend = JSONObject.valueToString(preparedToSendData);

        System.out.println("temp_data: " + jsonStringToSend);

        return jsonStringToSend;
    }

    @RequestMapping(value="getTemperatureInRange",method = RequestMethod.POST)
    public String getTemperatureMeasurementInRangeJsonString(@RequestBody String dataJson){
        JSONObject data = new JSONObject(dataJson);
        Long searchControlObjectId = data.getLong("id");
        String begin = data.getString("begin");
        String end = data.getString("end");
        List<Measurement> results = new ArrayList<>();
        try {
            Date beginningDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(begin);
            Date endingDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(end);



            List<Measurement> curMeasurements = measurementRepo.getMeasurementByAreaId(searchControlObjectId);

            for(Measurement m : curMeasurements){
                Date curDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(m.getDatetime());

                if(curDate.before(endingDate) && curDate.after(beginningDate)){
                    results.add(m);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Map<String, Object> preparedToSendData = new HashMap<>();

        preparedToSendData.put("time", new ArrayList<String>());
        preparedToSendData.put("temperature", new ArrayList<Double>());
        preparedToSendData.put("temperatureClass", controlObjectRepo.getById(searchControlObjectId).getTemperatureClass());

        for(Measurement measurement: results){
            ((ArrayList<String>)preparedToSendData.get("time")).add(measurement.getDatetime());
            ((ArrayList<Double>)preparedToSendData.get("temperature")).add(measurement.getTemperature());
        }

        String jsonStringToSend = JSONObject.valueToString(preparedToSendData);

        return jsonStringToSend;
    }



}
