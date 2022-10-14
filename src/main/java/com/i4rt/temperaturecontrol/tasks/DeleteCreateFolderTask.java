package com.i4rt.temperaturecontrol.tasks;

import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

public class DeleteCreateFolderTask implements Runnable{
    @Override
    public void run() {
        System.out.println("running daily");

        Calendar calendar = Calendar.getInstance();

        Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.add(Calendar.DAY_OF_MONTH, -14);



        File prevDirectory = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData", prevCalendar.get(Calendar.DAY_OF_MONTH) +"_"+ (prevCalendar.getInstance().get(Calendar.MONTH) + 1));

        try {
            FileUtils.deleteDirectory(prevDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }

        File newDirectory = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + calendar.get(Calendar.DAY_OF_MONTH) +"_"+ (calendar.get(Calendar.MONTH) + 1));
        if (!newDirectory.exists()){
            System.out.println(newDirectory.mkdirs());
            System.out.println("dir created at " + System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) +"_"+ (Calendar.getInstance().get(Calendar.MONTH) + 1));
        }

    }
}
