package com.caffinc.grex.app.resources;

import com.caffinc.grex.common.entities.NodeStatus;
import com.caffinc.grex.core.Load;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Grex Node API
 *
 * @author Sriram
 */
@Path("/api/node")
@Api(value = "Node", description = "Controls the Node")
public class NodeResource {
    @GET
    @Path("/status")
    @ApiOperation(value = "Status", notes = "Returns the status of this node", response = NodeStatus.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus(@HeaderParam("authorization") final String authorizationHeader) {
        return Response.ok(Load.getInstance().getStatus()).build();
    }

    @POST
    @Path("/start")
    @ApiOperation(value = "Start Node", notes = "Starts this node")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startNode(@HeaderParam("authorization") final String authorizationHeader) {
        Load.getInstance().start();
        return Response.ok().build();
    }

    @POST
    @Path("/stop")
    @ApiOperation(value = "Stop Node", notes = "Stops this node")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopNode(@HeaderParam("authorization") final String authorizationHeader) {
        try {
            Load.getInstance().stop();
            return Response.ok().build();
        } catch (InterruptedException e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/set")
    @ApiOperation(value = "Set Load", notes = "Sets the load on this node")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setLoad(@HeaderParam("authorization") final String authorizationHeader, @QueryParam("load") final double load) {
        Load.getInstance().setLoad(load);
        return Response.ok().build();
    }
}
