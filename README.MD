# AutoScale-java
A Scaling Application running on Marathon and Mesos based on Application's name using Java. This Application can monitor multi app that registered in Marathon.

# Why
There are many projects can do the auto Scaling job. Some of them monitor the docker status like CPU and memory. some of them monitor the connections on the app itself, but most of them only monitor one app. This project monitor the multi-apps by ask Mesos metrics to calculate the CPU and memory usage of them.

# Build

    maven clean install

# Package
    docker build -t autoscale-jar .

# Usage
## Config
Please change the config file under

    src/resources/server-dev.properties

before you build the autosacale service like:

    server.port=<service port you want to manage>
    marathon.url = <the marathon url>

## Java
    java -jar autoscale-java.jar

## docker
    docke run -d autoscale-java

If you start the service you could tell the service which app you want to monitor and how to scale via restful API

# API
## 1. Resister a application
POST v1/apps/{appName}

### Body
    {
        "appName": "/sampleApp",
        "type": "up",
        "memPercent": 70,
        "cpuTime": 70,
        "triggerMode": "or",
        "scaleMultiplier": 1.5,
        "instances": 20,
        "strategy": [
          "replay",
          "20"
        ]
      }

| Name          |Type| Description   | Sample|
| ------------- |:---|:----------:| :-----:|
|appName|String|The name you want to monitor which registered in Mathron| /sampleApp|
|type|String|scale type: up or down| /up|
|memPercent|Int|the percent of mem usage which to trigger the scale up or down| 80|
|cpuTime|Int|the percent of cpu usage which to trigger the scale up or down| 80|
|triggerMode|String|mode type for how to use the cpu and memory usage: "or" or "and"| or|
|scaleMultiplier|Double|the multiplier of scale up or down| 1.5|
|instances|Int|max size or min size of instances| 20|
|strategy|json|the stragegy of scaling|repaly,fix|
