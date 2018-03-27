package com.hujiang.autoscale.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hujiang.autoscale.pojo.Config;
import com.hujiang.autoscale.services.AutoScaleExecutor;
import com.hujiang.autoscale.services.TaskScheduledThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@Slf4j
public class AutoScaleController {

    private Gson gson = new Gson();

    private TaskScheduledThreadPool scheduledThreadPool = new TaskScheduledThreadPool(5);

    @RequestMapping(value = "/apps", method = RequestMethod.POST)
    public void creates(String body) {
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(body).getAsJsonArray();
        for (JsonElement ele: array){
            Config config = gson.fromJson(ele,Config.class);
            if (StringUtils.isBlank(config.getAppName())){
                log.error("No application name");
                continue;
            }
            Runnable task = new AutoScaleExecutor(config);
            log.info("Create new monitor for app :{}", config.getAppName());
            scheduledThreadPool.submit(config,task);
        }

    }

    @RequestMapping(value = "/apps/{appName:.*}", method = RequestMethod.POST)
    public void create(String body, @PathVariable("appName") String appName) {
        Config config = gson.fromJson(body,Config.class);
        if (StringUtils.isBlank(config.getAppName())){
            appName="/"+appName;
            config.setAppName(appName);
        }
        Runnable task = new AutoScaleExecutor(config);
        scheduledThreadPool.submit(config,task);
    }

    @RequestMapping(value = "/apps/{appName:.*}", method = RequestMethod.DELETE)
    public void remove(@PathVariable("appName") String appName, @RequestParam("type") String type) {
        if (StringUtils.isBlank(type) ||StringUtils.isBlank(appName)){
            ;
        }
        appName="/"+appName;
        Config config = Config.builder().appName(appName).type(type).build();
        scheduledThreadPool.stop(config);
    }


    @RequestMapping(value = "/apps/{appName:.*}", method = RequestMethod.PUT)
    @ResponseBody
    public String modify(String body, @PathVariable("appName") String appName,@RequestParam("type") String type) {
        if (StringUtils.isBlank(type) ||StringUtils.isBlank(appName)){
            ;
        }
        Config config = gson.fromJson(body,Config.class);
        if (StringUtils.isBlank(config.getAppName())){
            appName="/"+appName;
            config.setAppName(appName);
        }
        config.setType(type);
        log.info("Stop current monitor: {}", config);
        scheduledThreadPool.stop(config);
        log.info("Create new monitor: {}", config);
        Runnable task = new AutoScaleExecutor(config);
        scheduledThreadPool.submit(config,task);
        return gson.toJson(config);
    }


    @RequestMapping(value = "/apps", method = RequestMethod.GET)
    @ResponseBody
    public String getMonitoredApps() {
        Set<Config> configs = scheduledThreadPool.getMonitoredAppNames();
        return gson.toJson(configs);
    }
}
