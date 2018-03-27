package com.hujiang.autoscale.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @RequestMapping(value = "/health_check", method = RequestMethod.GET)
    @ResponseBody
    public String healthCheck() {
        return "health";
    }

}
