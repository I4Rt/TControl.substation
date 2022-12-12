package com.i4rt.temperaturecontrol.basic;

import java.io.*;

import java.util.HashMap;
import java.util.Map;

import com.i4rt.temperaturecontrol.Services.ConnectionHolder;
import com.i4rt.temperaturecontrol.additional.GotPicImageCounter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HttpSenderService {

//    public static HttpSenderService instance;



    private HttpHost targetHost;
    private final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
    private final BasicAuthCache authCache = new BasicAuthCache();
    // Add AuthCache to the execution context
    private final HttpClientContext context = HttpClientContext.create();
    private CloseableHttpClient httpClient;


    public HttpSenderService(HttpHost targetHost, CloseableHttpClient httpClient) {
        this.targetHost = targetHost;
        this.httpClient = httpClient;
    }

    public HttpSenderService(String IP, Integer port, String realm, String nonce){
        this.targetHost = new HttpHost(IP, port, "http");

        this.credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "ask226226"));

        DigestScheme digestAuth = new DigestScheme();
        digestAuth.overrideParamter("realm", realm);
        digestAuth.overrideParamter("nonce", nonce);
        this.authCache.put(targetHost, digestAuth);

        this.context.setAuthCache(authCache);
        this.context.setCredentialsProvider(credsProvider);
    }






/*
    public static HttpSenderService setInstance(String IP, Integer port, String realm, String nonce){ // !!!
        HttpSenderService newConnections = new HttpSenderService(realm, nonce);
//        if(instance == null){
//            instance = new HttpSenderService(realm, nonce);
//        }

        newConnections.targetHost = new HttpHost(IP, port, "http");
        return newConnections;                                      //!!!
    }

*/

    public static HttpSenderService getHttpSenderService(String IP, Integer port, String realm, String nonce){
        HttpSenderService hss = new HttpSenderService(IP, port, realm, nonce);
        // hss.targetHost = new HttpHost(IP, port, "http"); // not necessary after updating constructor

        return hss;
    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public static Map<String , String> getMapFromXMLString(String StringXML){

        HashMap<String, String> valuesMap = new HashMap<>();
        if(StringXML.equals("{}") | StringXML.equals("")){
            return valuesMap;
        }

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
        String result = "";
        try {
            HttpGet httpget = new HttpGet(request);

            this.httpClient = HttpClients.createDefault();
            System.out.println("after creating con is  "  + httpClient);
            ConnectionHolder.addConnection(httpClient);
            CloseableHttpResponse response = httpClient.execute(targetHost, httpget, context);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        }
        catch (Exception e){
            System.out.println("http:GET Request Error: " + e);
            result = "conError";
        }
        finally {
            ConnectionHolder.removeConnection(httpClient);
            return result;
        }
    }

    public String sendPutRequest(String request, String body) throws IOException {
        String result = "";
        try{
            HttpPut httpPut = new HttpPut(request);
            httpPut.setHeader("Content-type", "application/xml");
            CloseableHttpResponse response;


            StringEntity stringEntity = new StringEntity(body);
            httpPut.getRequestLine();
            httpPut.setEntity(stringEntity);
            this.httpClient = HttpClients.createDefault();
            System.out.println("after creating con is  "  + httpClient);
            ConnectionHolder.addConnection(httpClient);
            response = httpClient.execute(targetHost , httpPut, context);

            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        }
        catch (Exception e){
            System.out.println("http:PUT Request Error: " + e);
        }
        finally {
            ConnectionHolder.removeConnection(httpClient);
            return result;
        }



    }


    public String getImage(String location, String request) throws IOException {
        String result = "";
        //"/ISAPI/Streaming/channels/2/picture"
        try{
            HttpGet httpget = new HttpGet(request);
            //System.out.println("connection " + httpClient);

            this.httpClient = HttpClients.createDefault();
            System.out.println("after creating con is  "  + httpClient);
            ConnectionHolder.addConnection(httpClient);
            CloseableHttpResponse response = httpClient.execute(targetHost, httpget, context);
            //System.getProperty("user.dir")+"\\src\\main\\upload\\static\\img"

            HttpEntity entity = response.getEntity();
            File prev_file = new File(location, "got_pic"+ GotPicImageCounter.getCurrentCounter() +".jpg");
            prev_file.delete();

            File file = new File(location, "got_pic"+ GotPicImageCounter.increaseCounter() +".jpg");
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream fileOS  = new FileOutputStream(file);
            entity.writeTo(fileOS);
            entity.consumeContent();
            fileOS.flush();
            fileOS.close();
        }
        catch (Exception e){
            System.out.println("http:Get Image error: " + e);
            return "conError";
        }
        finally {
            ConnectionHolder.removeConnection(httpClient);
            return "got_pic"+ GotPicImageCounter.getCurrentCounter() +".jpg";
        }

    }

    public String saveImage(String location, String request, String name) throws IOException {
        String result = "";

        try{
            //"/ISAPI/Streaming/channels/2/picture"
            HttpGet httpget = new HttpGet(request);

            //System.out.println("connection " + httpClient);
            this.httpClient = HttpClients.createDefault();
            System.out.println("after creating con is  "  + httpClient);
            ConnectionHolder.addConnection(httpClient);
            CloseableHttpResponse response = httpClient.execute(targetHost, httpget, context);
            //System.getProperty("user.dir")+"\\src\\main\\upload\\static\\img"

            HttpEntity entity = response.getEntity();


            File file = new File(location,  name +".jpg");
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream fileOS  = new FileOutputStream(file);
            entity.writeTo(fileOS);
            entity.consumeContent();
            fileOS.flush();
            fileOS.close();


        }
        catch (Exception e){
            System.out.println("http:Save Image Error: " + e);
        }
        finally {
            ConnectionHolder.removeConnection(httpClient);

            return name +".jpg";
        }
    }

/*
    @Override
    public HttpSenderService clone() {
        try {
            return (HttpSenderService) super.clone();
        } catch (CloneNotSupportedException e) {
            return new HttpSenderService(this.targetHost, this.httpClient);
        }
    }
 */
}
