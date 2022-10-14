package com.i4rt.temperaturecontrol.basic;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.api.gax.core.CredentialsProvider;
import com.i4rt.temperaturecontrol.additional.GotPicImageCounter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.swing.text.html.parser.Entity;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HttpSenderService {

    public static HttpSenderService instance;



    private HttpHost targetHost = new HttpHost("192.168.1.64", 80, "http");
    final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
    final BasicAuthCache authCache = new BasicAuthCache();
    // Add AuthCache to the execution context
    final HttpClientContext context = HttpClientContext.create();
    CloseableHttpClient httpClient = HttpClients.createDefault();


    public HttpSenderService(){

        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "ask226226"));

        DigestScheme digestAuth = new DigestScheme();
        digestAuth.overrideParamter("realm", "IP Camera(J3898)");
        digestAuth.overrideParamter("nonce", "4e6d5a6a4e475a6b5a5745364f4456684e6a4e6c5a6a673d");
        authCache.put(targetHost, digestAuth);

        context.setAuthCache(authCache);
        context.setCredentialsProvider(credsProvider);
    }



    public static HttpSenderService getInstance(){
        if(instance == null){
            instance = new HttpSenderService();
        }
        return instance;
    }

    public static HttpSenderService setInstance(String IP, Integer port){
        if(instance == null){
            instance = new HttpSenderService();
        }

        instance.targetHost = new HttpHost(IP, port, "http");
        return instance;
    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public static Map<String , String> getMapFromXMLString(String StringXML){

        HashMap<String, String> valuesMap = new HashMap<>();
        try {
            Document xmlDoc = loadXMLFromString(StringXML);
            NodeList root = (NodeList) xmlDoc.getDocumentElement();

            for (int i = 0; i < root.getLength(); i++){
                if (!root.item(i).getTextContent().equals("\n")){
                    valuesMap.put(root.item(i).getNodeName(), root.item(i).getTextContent());
                }
            }
            System.out.println(valuesMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            return valuesMap;
        }
    }

    public String sendGetRequest(String request) throws IOException {

        HttpGet httpget = new HttpGet(request);
        CloseableHttpResponse response = httpClient.execute(targetHost, httpget, context);
        HttpEntity entity = response.getEntity();

        httpClient.close();
        httpClient = HttpClients.createDefault();
        return EntityUtils.toString(entity, "UTF-8");
    }

    public String sendPutRequest(String request, String body) throws IOException {

        HttpPut httpPut = new HttpPut(request);
        httpPut.setHeader("Content-type", "application/xml");
        CloseableHttpResponse response;

        try{
            StringEntity stringEntity = new StringEntity(body);
            httpPut.getRequestLine();
            httpPut.setEntity(stringEntity);

            response = httpClient.execute(targetHost , httpPut, context);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        response = httpClient.execute(targetHost, httpPut, context);
        HttpEntity entity = response.getEntity();

        httpClient.close();
        httpClient = HttpClients.createDefault();
        return EntityUtils.toString(entity, "UTF-8");
    }


    public String getImage(String location, String request) throws IOException {
        //"/ISAPI/Streaming/channels/2/picture"
        HttpGet httpget = new HttpGet(request);

        CloseableHttpResponse response = httpClient.execute(targetHost, httpget, context);
        //System.getProperty("user.dir")+"\\src\\main\\upload\\static\\img"

        HttpEntity entity = response.getEntity();

        File file = new File(location, "got_pic"+ GotPicImageCounter.increaseCounter() +".jpg");
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileOutputStream fileOS  = new FileOutputStream(file);
        entity.writeTo(fileOS);
        entity.consumeContent();
        fileOS.flush();
        fileOS.close();

        httpClient.close();
        httpClient = HttpClients.createDefault();
        return "got_pic"+ GotPicImageCounter.getCurrentCounter() +".jpg";
    }

}
