package com.i4rt.temperaturecontrol.additional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GotPicImageCounter {


    public static String getCurrentCounter() throws IOException{
        FileReader reader = new FileReader(System.getProperty("user.dir")+"/temp/gotPicImageCounterHolder.txt");
        BufferedReader lineReader = new BufferedReader(reader);
        String data = lineReader.readLine();

        reader.close();

        return data;
    }

    public static String increaseCounter() throws IOException{

        Integer counter = Integer.valueOf(getCurrentCounter());
        counter += 1;

        FileWriter writer = new FileWriter(System.getProperty("user.dir")+"/temp/gotPicImageCounterHolder.txt", false);
        String text = counter.toString();
        writer.write(text);
        writer.flush();
        writer.close();


        return counter.toString();
    }

}
