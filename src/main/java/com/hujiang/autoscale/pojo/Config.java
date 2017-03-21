package com.hujiang.autoscale.pojo;

import lombok.Builder;
import lombok.Data;


/**
 * Created by Kyne on 16/9/1.
 */
@Data
@Builder
public class Config {
    private String appName;
    private String type;
    private int memPercent;
    private int cpuTime;
    private String triggerMode;
    private double scaleMultiplier;
    private int instances;
    private String[] strategy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config)) return false;

        Config config = (Config) o;

        if (!appName.equals(config.appName)) return false;
        return type.equals(config.type);

    }

    @Override
    public int hashCode() {
        int result = appName.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}

