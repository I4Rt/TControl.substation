package com.i4rt.temperaturecontrol.device;

import com.i4rt.temperaturecontrol.additional.GotPicImageCounter;
import com.i4rt.temperaturecontrol.additional.UploadedImageCounter;
import com.i4rt.temperaturecontrol.basic.FileUploadUtil;
import com.i4rt.temperaturecontrol.basic.HttpSenderService;
import com.i4rt.temperaturecontrol.model.ControlObject;
import lombok.*;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ThermalImager {

    @Id
    @Column(name = "thermal_imager_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String IP;

    @Column
    private String port;


    @OneToMany(mappedBy = "thermalImager", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ControlObject> controlObjectsArray;



    public boolean connection(){
        return true;
    }

    public double getTemperature(Double horizontal, Double vertical, Integer x, Integer y, Double focusing){

        gotoCoordinates(horizontal, vertical, focusing);
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        return random.nextDouble() % 10 * 10 + 50.0;
    }

    public Double getCurHorizontal(){
        return 0.0;
    }
    public Double getCurVertical(){
        return 0.0;
    }
    public Double getCurFocusing(){
        return 1.0;
    }

    public Boolean gotoCoordinates(Double horizontal, Double vertical, Double focusing){
        try {
            horizontal *= 10;
            Integer parsedHorizontal = horizontal.intValue();

            vertical *= 10;
            Integer parsedVertical = vertical.intValue();

            HttpSenderService httpSenderService = HttpSenderService.getInstance();

            String bodyMove = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<PTZData version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                    "    <AbsoluteHigh>\n" +
                    "        <elevation>"+parsedVertical+"</elevation>\n" +
                    "        <azimuth>"+parsedHorizontal+"</azimuth>\n" +
                    "        <absoluteZoom>50</absoluteZoom>\n" +
                    "    </AbsoluteHigh>\n" +
                    "</PTZData>";


            System.out.println(httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/absolute", bodyMove));

            String answer = "";
            Map<String, String> parsedAnswer;

            Integer tryCounter = 0;

            while(true){
                answer = httpSenderService.sendGetRequest("/ISAPI/PTZCtrl/channels/2/absoluteEx");
                System.out.println("counter: " + tryCounter);
                Thread.sleep(200);
                parsedAnswer = HttpSenderService.getMapFromXMLString(answer);

                System.out.println(Double.parseDouble(parsedAnswer.get("elevation")) + " " + vertical / 10);
                System.out.println(Double.parseDouble(parsedAnswer.get("azimuth")) + " " + horizontal / 10);
                System.out.println(tryCounter);

                if(Double.parseDouble(parsedAnswer.get("elevation")) == vertical / 10 && Double.parseDouble(parsedAnswer.get("azimuth")) == horizontal / 10){


                    String focusingBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<PTZAbsoluteEx xmlns=\"http://www.isapi.org/ver20/XMLSchema\" version=\"2.0\">\n" +
                            "    <elevation>" + vertical / 10 + "</elevation>\n" +
                            "    <azimuth>" + horizontal / 10 + "</azimuth>\n" +
                            "    <absoluteZoom>1.00</absoluteZoom>\n" +
                            "    <focus>" + focusing.intValue() + "</focus>\n" +
                            "    <focalLen>10000</focalLen>\n" +
                            "    <horizontalSpeed>10.00</horizontalSpeed>\n" +
                            "    <verticalSpeed>10.00</verticalSpeed>\n" +
                            "    <zoomType>absoluteZoom</zoomType>\n" +
                            "    <objectDistance>1</objectDistance>\n" +
                            "    <isContinuousTrackingEnabled>true</isContinuousTrackingEnabled>\n" +
                            "    <lookDownUpAngle>0.00</lookDownUpAngle>\n" +
                            "</PTZAbsoluteEx>";
                    System.out.println("focusing");
                    System.out.println(httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/absoluteEx", focusingBody));


                    Thread.sleep(1500);


                    String presetBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                            "<PTZPreset version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                            "    <enabled>true</enabled>\n" +
                            "    <id>1</id>\n" +
                            "    <presetName>area 1</presetName> \n" +
                            "    <AbsoluteHigh>\n" +
                            "        <elevation>" + parsedVertical + "</elevation>\n" +
                            "        <azimuth>" + parsedHorizontal + "</azimuth>\n" +
                            "        <absoluteZoom>150></absoluteZoom>\n" +
                            "    </AbsoluteHigh>\n" +
                            "</PTZPreset>";

                    httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/presets/1", presetBody);

                    System.out.println(httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/presets/1/goto", ""));


                    return true;
                }

                if(tryCounter > 50){
                    return false;
                }

                tryCounter += 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public String gotoAndGetImage(Double horizontal, Double vertical, Double focusing){
        try {
            HttpSenderService httpSenderService = HttpSenderService.getInstance();
            Boolean gotoResult = gotoCoordinates(horizontal, vertical, focusing);
            System.out.println(gotoResult);
            if(gotoResult){
                // получить новое фото и сохранить его с новым индексом
                httpSenderService.getImage(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\img", "/ISAPI/Streaming/channels/2/picture");
                return "img/got_pic" + GotPicImageCounter.getCurrentCounter() + ".jpg"; //!
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("gotoAndGetImage Error");
        return "error";
    }

    public String configureArea(Integer x1, Integer y1, Integer x2, Integer y2){
        try {
            System.out.println("x1 " + x1);
            System.out.println("y1 " + y1);
            System.out.println("x2 " + x2);
            System.out.println("y2 " + y2);

            Integer integerX1 = x1 * 1000 / 640 ;
            Integer integerX2 = x2 * 1000 / 640 ;

            Integer integerY1 = y1 * 1000 / 380;
            Integer integerY2 = y2 * 1000 / 380;

            System.out.println("x1" + integerX1);
            System.out.println("y1" + integerY2);
            System.out.println("x2" + integerX2);
            System.out.println("y2" + integerY2);



            String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<ThermometryRegion version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                    "    <id>1</id>\n" +
                    "    <enabled>true</enabled>\n" +
                    "    <name>Current area</name>\n" +
                    "    <emissivity>0.96</emissivity>\n" +
                    "    <distance>3000</distance>\n" +
                    "    <reflectiveEnable>true</reflectiveEnable>\n" +
                    "    <reflectiveTemperature>20.0</reflectiveTemperature>\n" +
                    "    <type>region</type>\n" +
                    "    <Region>\n" +
                    "        <RegionCoordinatesList>\n" +
                    "            <RegionCoordinates>\n" +
                    "                <positionX>" + integerX1 + "</positionX>\n" +
                    "                <positionY>" + integerY1 + "</positionY>\n" +
                    "            </RegionCoordinates>\n" +
                    "            <RegionCoordinates>\n" +
                    "                <positionX>" + integerX1 + "</positionX>\n" +
                    "                <positionY>" + integerY2 + "</positionY>\n" +
                    "            </RegionCoordinates>\n" +
                    "            <RegionCoordinates>\n" +
                    "                <positionX>"+ integerX2 +"</positionX>\n" +
                    "                <positionY>"+ integerY2 +"</positionY>\n" +
                    "            </RegionCoordinates>\n" +
                    "            <RegionCoordinates>\n" +
                    "                <positionX>"+ integerX2 +"</positionX>\n" +
                    "                <positionY>"+ integerY1 +"</positionY>\n" +
                    "            </RegionCoordinates>\n" +
                    "        </RegionCoordinatesList>\n" +
                    "    </Region>\n" +
                    "    <distanceUnit>centimeter</distanceUnit>\n" +
                    "    <emissivityMode>customsettings</emissivityMode>\n" +
                    "</ThermometryRegion>" ;

            HttpSenderService httpSenderService = HttpSenderService.getInstance();


            System.out.println(httpSenderService.sendPutRequest("/ISAPI/Thermal/channels/2/thermometry/1/regions/1", body)); // Need errors handler
            return "ok";
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("configureArea Error");
        return "error";
    }

    public Object getTemperatureInArea(Integer areaId){
        try {
            HttpSenderService httpSenderService = HttpSenderService.getInstance();






            String jsonString = httpSenderService.sendGetRequest("/ISAPI/Thermal/channels/2/thermometry/1/rulesTemperatureInfo/" + areaId + "?format=json");

            System.out.println(jsonString);

            org.json.JSONObject data = new org.json.JSONObject(jsonString);

            data = data.getJSONObject("ThermometryRulesTemperatureInfo");

            Double temperature = data.getDouble("maxTemperature");

            System.out.println(temperature);

            return temperature;

        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }



    public Boolean setAutoFocus(){
        try {
            String body1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<FocusConfiguration version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                           "    <focusStyle>MANUAL</focusStyle>\n" +
                           "    <focusLimited>5000</focusLimited>\n" +
                           "    <focusSpeed>1</focusSpeed>\n" +
                           "    <focusSensitivity>1</focusSensitivity>\n" +
                           "    <temperatureChangeAdaptEnabled>true</temperatureChangeAdaptEnabled>\n" +
                           "</FocusConfiguration>";

            String body2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<FocusConfiguration version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                    "    <focusStyle>SEMIAUTOMATIC</focusStyle>\n" +
                    "    <focusLimited>5000</focusLimited>\n" +
                    "    <focusSpeed>1</focusSpeed>\n" +
                    "    <focusSensitivity>1</focusSensitivity>\n" +
                    "    <temperatureChangeAdaptEnabled>true</temperatureChangeAdaptEnabled>\n" +
                    "</FocusConfiguration>";

            HttpSenderService httpSenderService = HttpSenderService.getInstance();


            httpSenderService.sendPutRequest("/ISAPI/Image/channels/2/focusConfiguration", body1);
            httpSenderService.sendPutRequest("/ISAPI/Image/channels/2/focusConfiguration", body2);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }



}
