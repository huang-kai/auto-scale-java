package com.hujiang.autoscale.services;

import com.hujiang.autoscale.pojo.Config;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kyne on 16/9/5.
 */
@Slf4j
public class TaskScheduledThreadPool extends ScheduledThreadPoolExecutor {


    private ConcurrentHashMap<Config, Future> appsHashMap = new ConcurrentHashMap<>();


    public TaskScheduledThreadPool(int corePoolSize) {
        super(corePoolSize);
        setRemoveOnCancelPolicy(true);
    }


    public void submit(Config config, Runnable command){
        if (appsHashMap.containsKey(config)){
            log.info("App: {} already monitored", config);
            return;
        }
        String[] strategy = config.getStrategy();
        Future result=null;
        switch (strategy[0]){
            case "replay":
                result = scheduleAtFixedRate(command,0, Long.parseLong(strategy[1]),TimeUnit.SECONDS);
                break;
            case "fixed":
                String[] spliteTime = strategy[1].split(":");
                LocalTime scheduledTime = LocalTime.of(Integer.parseInt(spliteTime[0]),Integer.parseInt(spliteTime[1]));
                LocalTime now = LocalTime.now();
                long delay = Duration.between(now,scheduledTime).getSeconds();
                result = scheduleAtFixedRate(command,delay>0?delay:24*60*60+delay, 24*60*60L,TimeUnit.SECONDS);
                break;
            default: break;
        }
        appsHashMap.put(config,result);
        log.debug("task account:{}",super.getTaskCount());
        log.debug("task active account:{}",super.getActiveCount());
        log.debug("task complete account:{}",super.getCompletedTaskCount());
    }

    public void stop(Config config){
        if (appsHashMap.containsKey(config)){
            if (appsHashMap.get(config).cancel(true)) {
                appsHashMap.remove(config);
                log.info("stop config: {} success", config);
                super.purge();
            } else {
                log.info("stop config: {} failed", config);
            }
        }else{
            log.info("There is no config:{}, do nothing.", config);
        }
        log.debug("task account:{}",super.getTaskCount());
        log.debug("task active account:{}",super.getActiveCount());
        log.debug("task complete account:{}",super.getCompletedTaskCount());
    }

    public Set<Config> getMonitoredAppNames(){
        return appsHashMap.keySet();
    }

}
