package com.hujiang.autoscale.utils;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Created by Kyne on 16/9/1.
 */
@Slf4j
public class MarathonUtil {
    private static MarathonUtil INSTANCE = new MarathonUtil();

    private String marathonHost = "http://0.0.0.0:8080";

    private MarathonUtil(){

    }

    public static MarathonUtil getInstance(){
        return INSTANCE;
    }

    public void setMarathonUrl(String host) {
        marathonHost = host;
    }

    public List<String> getAllApps() throws IOException {
        List<String> result = Lists.newArrayList();
        String resp = OKHttpUtil.getInstance().doGet(marathonHost+"/v2/apps",null,null);
        JSONObject object = new JSONObject(resp);
        for (Object obj: object.getJSONArray("apps")){
            JSONObject jsonObj = (JSONObject) obj;
            result.add(jsonObj.getString("id"));
        }

        return result;
    }

    public Map<String,String> getAppDetails(String appName) throws IOException {
        Map<String, String> result = Maps.newHashMap();
        String resp = OKHttpUtil.getInstance().doGet(marathonHost+"/v2/apps/"+appName,null,null);
        JSONObject object = new JSONObject(resp);
//        System.out.println(object.getJSONObject("app").get("tasks"));
        for (Object obj : object.getJSONObject("app").getJSONArray("tasks")){
            JSONObject jsonObj = (JSONObject) obj;
            result.put(jsonObj.getString("id"),jsonObj.getString("host"));
        }
        log.debug("App detail: {}",result);
        return result;
    }

    public int getAppInstances(String appName) throws IOException {

        String resp = OKHttpUtil.getInstance().doGet(marathonHost+"/v2/apps/"+appName,null,null);
        JSONObject object = new JSONObject(resp);
        return object.getJSONObject("app").getInt("instances");
    }

    public void scaleOutApp(String appName, int maxInstances, double multiplier) throws IOException {
        int currentInstances = getAppInstances(appName);
        if (currentInstances >= maxInstances){
            log.info("Current instances are bigger than max instances, do nothing.");
            return;
        }
        int targetInstances = (int)Math.ceil(currentInstances * multiplier);
        if (targetInstances >maxInstances){
            log.info("Reached the set maximum instances of " + maxInstances);
            targetInstances = maxInstances;
        }
        JSONObject object = new JSONObject();
        object.put("instances", targetInstances);
        Map<String,String> headers = Maps.newHashMap();
        headers.put("Content-type","application/json");

        OKHttpUtil.getInstance().doPut(marathonHost + "/v2/apps/" + appName, object.toString(), null, headers);
        log.info("scale out to " + targetInstances);

    }

    public void scaleDownApp(String appName, int minInstances, double multiplier) throws IOException {
        int currentInstances = getAppInstances(appName);
        if (currentInstances<=minInstances){
            log.info("Current instances are small than minmum instances. do nothing");
            return;
        }
        int targetInstances = (int)Math.ceil(currentInstances / multiplier);
        if (targetInstances <minInstances){
            log.info("Reached the set minmum instances of " + minInstances);
            targetInstances = minInstances;
        }
        JSONObject object = new JSONObject();
        object.put("instances", targetInstances);
        Map<String,String> headers = Maps.newHashMap();
        headers.put("Content-type","application/json");

        OKHttpUtil.getInstance().doPut(marathonHost + "/v2/apps/" + appName, object.toString(), null,headers);
        log.info("scale down to " + targetInstances);

    }


    public static void main(String[] args) {
        try {
            MarathonUtil.getInstance().getAppDetails("/ocs-api/courseware");
            MarathonUtil.getInstance().scaleOutApp("/ocs-api/courseware",10,1.5);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
