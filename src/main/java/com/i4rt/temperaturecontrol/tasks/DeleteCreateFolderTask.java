package com.i4rt.temperaturecontrol.tasks;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

@Configuration
@EnableScheduling
public class DeleteCreateFolderTask{
    @Scheduled(cron = "0 0 3 * * ?")
    public void deleteCreateFolder () {
        System.out.println("running daily");

        Calendar calendar = Calendar.getInstance();

        File file = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData");
        String[] directories = file.list();

        for (String area: directories) {
            try{
                File folder = new File(System.getProperty("user.dir") + "\\src\\main\\upload\\static\\imgData\\" + area);
                SimpleDateFormat format = new SimpleDateFormat();
                format.applyPattern("dd_MM");

                Date docDate = format.parse(area);
                docDate.setYear(new Date(folder.lastModified()).getYear());
                Date nowDate = new Date();

                calendar.setTime(nowDate);
                calendar.add(Calendar.DAY_OF_MONTH, -14);
                nowDate = calendar.getTime();

                System.out.println(docDate + " , " + nowDate);

                if (docDate.before(nowDate)) {
                    FileUtils.deleteDirectory(folder);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        File newDirectory = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + calendar.get(Calendar.DAY_OF_MONTH) +"_"+ (calendar.get(Calendar.MONTH) + 1));
        if (!newDirectory.exists()){
            System.out.println(newDirectory.mkdirs());
            System.out.println("dir created at " + System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) +"_"+ (Calendar.getInstance().get(Calendar.MONTH) + 1));
        }

    }
}
