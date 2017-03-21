package com.hujiang.autoscale.utils;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by Kyne on 16/9/1.
 */
@Slf4j
public class MesosUtil {


    public static JSONObject getTaskStatistics(String taskName, String host) throws IOException {
        String resp = OKHttpUtil.getInstance().doGet("http://" + host + ":5051/monitor/statistics.json", null,null);
        JSONArray array = new JSONArray(resp);
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObj = array.getJSONObject(i);
            String executorId = jsonObj.getString("executor_id");
            if (executorId.equals(taskName)) {
                return jsonObj.getJSONObject("statistics");
            }
        }
        return null;
    }

    public static Map<String, Double> getAppAvgDetail(String appName) throws IOException {
        Map<String, Double> result = Maps.newHashMap();
        Map<String, String> taskMap = MarathonUtil.getInstance().getAppDetails(appName);
        if (taskMap != null && !taskMap.isEmpty()) {
            List<Double> cpuValues = Lists.newArrayList();
            List<Double> memValues = Lists.newArrayList();
            taskMap.forEach((taskName, host) -> {
                try {
                    JSONObject statistics0 = getTaskStatistics(taskName, host);
                    Thread.sleep(1000);
                    JSONObject statistics1 = getTaskStatistics(taskName, host);
                    if (statistics0 != null && statistics1 != null) {
                        BigDecimal cpuSysTimeSecs0 = new BigDecimal(statistics0.getDouble("cpus_system_time_secs"));
                        BigDecimal cpuUserTimeSecs0 = new BigDecimal(statistics0.getDouble("cpus_user_time_secs"));
                        BigDecimal timestamp0 = new BigDecimal(statistics0.getDouble("timestamp"));
                        BigDecimal cpuSysTimeSecs1 = new BigDecimal(statistics1.getDouble("cpus_system_time_secs"));
                        BigDecimal cpuUserTimeSecs1 = new BigDecimal(statistics1.getDouble("cpus_user_time_secs"));
                        BigDecimal timestamp1 = new BigDecimal(statistics1.getDouble("timestamp"));
                        BigDecimal cpuTimeTotal0 = cpuSysTimeSecs0.add(cpuUserTimeSecs0);
                        BigDecimal cpuTimeTotal1 = cpuSysTimeSecs1.add(cpuUserTimeSecs1);
                        BigDecimal cpusTimeDelta = cpuTimeTotal1.subtract(cpuTimeTotal0);
                        BigDecimal timeStampDelta = timestamp1.subtract(timestamp0);
                        BigDecimal usage = cpusTimeDelta.divide(timeStampDelta, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                        cpuValues.add(usage.doubleValue());

                        BigDecimal memRssBytes = new BigDecimal(statistics1.getLong("mem_rss_bytes"));
                        BigDecimal memLimitBytes = new BigDecimal(statistics1.getLong("mem_limit_bytes"));
                        BigDecimal memUtlization = memRssBytes.divide(memLimitBytes, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                        memValues.add(memUtlization.doubleValue());
                    }

                } catch (IOException |InterruptedException e) {
                    log.error("Get exception",e);
                }
            });
            double cpuAvg = cpuValues.stream().mapToDouble((x) -> x).summaryStatistics().getAverage();
            log.info("AppName: {} 's cpu avg={}", appName,cpuAvg);
            double memAvg = memValues.stream().mapToDouble((x) -> x).summaryStatistics().getAverage();
            log.info("AppName: {} 's mem avg={}", appName,memAvg);
            result.put("CPU_AVG", cpuAvg);
            result.put("MEM_AVG", memAvg);
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            System.out.println(MesosUtil.getAppAvgDetail("/ocs-api/courseware"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
