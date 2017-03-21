package com.hujiang.autoscale.services;

import com.hujiang.autoscale.utils.MarathonUtil;
import com.hujiang.autoscale.utils.MesosUtil;
import com.hujiang.autoscale.pojo.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Kyne on 16/9/2.
 */
@Slf4j
public class AutoScaleExecutor implements Runnable{

    private Config config = null;

    public AutoScaleExecutor (Config config){
        this.config=config;
    }

    @Override
    public void run() {
        try {
            Map<String, Double> details = MesosUtil.getAppAvgDetail(config.getAppName());
            switch (config.getType()){
                case "up":
                    // Scale out
                    if ("and".equals(config.getTriggerMode())) {
                        if (details.get("CPU_AVG") > config.getCpuTime() && details.get("MEM_AVG") > config.getMemPercent()) {
                            log.info("Auto scale out triggered based on 'both' Mem & CPU exceeding threshold");
                            MarathonUtil.getInstance().scaleOutApp(config.getAppName(), config.getInstances(), config.getScaleMultiplier());
                        } else {
                            log.info("Both values were not greater than auto scale out targets");
                        }
                    } else if ("or".equals(config.getTriggerMode())) {
                        if (details.get("CPU_AVG") > config.getCpuTime() || details.get("MEM_AVG") > config.getMemPercent()) {
                            log.info("Auto scale out triggered based on 'both' Mem & CPU exceeding threshold");
                            MarathonUtil.getInstance().scaleOutApp(config.getAppName(), config.getInstances(), config.getScaleMultiplier());
                        } else {
                            log.info("Neither Mem 'or' CPU values exceeding threshold");
                        }
                    }
                    break;
                case "down":
                    // Scale Down
                    if ("and".equals(config.getTriggerMode())) {
                        if (details.get("CPU_AVG") < config.getCpuTime() && details.get("MEM_AVG") < config.getMemPercent()) {
                            log.info("Auto scale down triggered based on 'both' Mem & CPU exceeding threshold");
                            MarathonUtil.getInstance().scaleDownApp(config.getAppName(), config.getInstances(), config.getScaleMultiplier());
                        } else {
                            log.info(" values were not greater than auto scale down targets");
                        }
                    } else if ("or".equals(config.getTriggerMode())) {
                        if (details.get("CPU_AVG") < config.getCpuTime() || details.get("MEM_AVG") < config.getMemPercent()) {
                            log.info("Auto scale down triggered based on 'both' Mem & CPU exceeding threshold");
                            MarathonUtil.getInstance().scaleDownApp(config.getAppName(), config.getInstances(), config.getScaleMultiplier());
                        } else {
                            log.info("Neither values were not greater than auto scale down targets");
                        }
                    }
                    break;
            }

        } catch (IOException e) {
            log.error("error", e);
        }
    }
}
