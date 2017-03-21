package com.hujiang.autoscale.controller;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hujiang.autoscale.pojo.Config;
import com.hujiang.autoscale.services.AutoScaleExecutor;
import com.hujiang.autoscale.services.TaskScheduledThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;


/**
 * Created by Kyne on 16/9/5.
 */
@Path("/v1")
@Slf4j
public class RestController {

    private TaskScheduledThreadPool scheduledThreadPool = new TaskScheduledThreadPool(5);

    private Gson gson = new Gson();

    @POST
    @Path("/apps")
    @Produces(MediaType.APPLICATION_JSON)
    public Response creates(String body) {
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

        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @POST
    @Path("/apps/{appName:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(String body, @PathParam("appName") String appName) {

        Config config = gson.fromJson(body,Config.class);
        if (StringUtils.isBlank(config.getAppName())){
            appName="/"+appName;
            config.setAppName(appName);
        }
        Runnable task = new AutoScaleExecutor(config);
        scheduledThreadPool.submit(config,task);
        return Response.status(Response.Status.CREATED).entity(gson.toJson(config)).build();
    }

    @DELETE
    @Path("/apps/{appName:.*}")
    public Response remove(@PathParam("appName") String appName, @QueryParam("type") String type) {
        if (StringUtils.isBlank(type) ||StringUtils.isBlank(appName)){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        appName="/"+appName;
        Config config = Config.builder().appName(appName).type(type).build();
        scheduledThreadPool.stop(config);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/apps/{appName:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response modify(String body, @PathParam("appName") String appName,@QueryParam("type") String type) {
        if (StringUtils.isBlank(type) ||StringUtils.isBlank(appName)){
            return Response.status(Response.Status.BAD_REQUEST).build();
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
        return Response.ok(gson.toJson(config)).build();
    }

    @GET
    @Path("/apps")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMonitoredApps() {
        Set<Config> configs = scheduledThreadPool.getMonitoredAppNames();
        return Response.ok(gson.toJson(configs)).build();
    }

}
