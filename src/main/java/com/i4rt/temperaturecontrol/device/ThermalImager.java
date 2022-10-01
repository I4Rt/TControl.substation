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
                    "        <absoluteZoom>"+focusing.intValue()+"</absoluteZoom>\n" +
                    "    </AbsoluteHigh>\n" +
                    "</PTZData>";


            System.out.println(httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/absolute", bodyMove));
            Thread.sleep(1000);
            String answer = "";
            Map<String, String> parsedAnswer;

            Integer tryCounter = 0;

            while(true){
                answer = httpSenderService.sendGetRequest("/ISAPI/PTZCtrl/channels/2/absoluteEx");
                Thread.sleep(200);
                parsedAnswer = HttpSenderService.getMapFromXMLString(answer);

                System.out.println(Double.parseDouble(parsedAnswer.get("elevation")) + " " + vertical / 10);
                System.out.println(Double.parseDouble(parsedAnswer.get("azimuth")) + " " + horizontal / 10);
                System.out.println(tryCounter);

                if(Double.parseDouble(parsedAnswer.get("elevation")) == vertical / 10 && Double.parseDouble(parsedAnswer.get("azimuth")) == horizontal / 10){
                    return true;
                }

                if(tryCounter > 20){
                    return false;
                }

                tryCounter += 1;
            }

        } catch (Exception e) {
            System.out.println(e);
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
        return "error";
    }




}
