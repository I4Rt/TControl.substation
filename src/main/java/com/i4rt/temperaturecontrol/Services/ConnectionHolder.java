package com.i4rt.temperaturecontrol.Services;

import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHolder {

    private static List<CloseableHttpClient> connections = new ArrayList<>();

    public static void addConnection(CloseableHttpClient httpClient){
        connections.add(httpClient);
        System.out.println("connections size: " + connections.size());
    }

    public static void removeConnection(CloseableHttpClient httpClient) throws IOException {
        try{
            httpClient.close();
        } catch (Exception e){
            System.out.println("Close error" + e);
        }
        finally {
            connections.remove(httpClient);
            System.out.println("connections size: " + connections.size());
        }
    }

    public static void removeAllConnection() throws IOException {
        for (CloseableHttpClient chc : connections){
            try{
                chc.close();
            }
            catch (Exception e){
                System.out.println("Already closed");
            }
        }
        connections = new ArrayList<>();
        System.out.println("connections size: " + connections.size());
    }
}
