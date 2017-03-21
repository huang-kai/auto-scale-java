package com.hujiang.autoscale;

import com.hujiang.autoscale.controller.HealthCheckController;
import com.hujiang.autoscale.utils.MarathonUtil;
import com.hujiang.autoscale.controller.RestController;
import com.hujiang.ocs.server.components.http.HttpServer;
import com.hujiang.ocs.server.resteasy.ResteasyServer;
import lombok.extern.slf4j.Slf4j;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyne on 16/9/1.
 */
@Slf4j
public class AppMain {
    public static void main(String[] args) throws FileNotFoundException {
        log.info("Welcome to use Auto Scale");
        List<Object> resources = new ArrayList<>();
        resources.add(new RestController());
        resources.add(new HealthCheckController());

        HttpServer server = new ResteasyServer(resources);
        String url = server.getConfig().getValue("marathon.url");
        MarathonUtil.getInstance().setMarathonUrl(url);
        server.startup();
    }

}
