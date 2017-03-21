package com.hujiang.autoscale.controller;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Kyne on 2016/9/29.
 */
@Path("")
@Slf4j
public class HealthCheckController {
    @GET
    @Path("/health_check")
    @Produces(MediaType.TEXT_PLAIN)
    public Response healthChekc() {
        return Response.ok("health").build();
    }
}
