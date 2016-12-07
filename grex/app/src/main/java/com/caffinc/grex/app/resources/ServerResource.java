package com.caffinc.grex.app.resources;

import com.caffinc.grex.app.entities.ErrorResponse;
import com.caffinc.grex.app.entities.Node;
import com.caffinc.grex.app.services.NodeManagement;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import retrofit.RetrofitError;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Grex Server API
 *
 * @author Sriram
 */
@Path("/api/server")
@Api(value = "Server", description = "Manages the Server")
public class ServerResource {

    @GET
    @Path("/nodes")
    @ApiOperation(value = "Fetch All Nodes", notes = "Returns list of nodes connected to this server", response = Node.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNodes(@HeaderParam("authorization") final String authorizationHeader) {
        return Response.ok(NodeManagement.getInstance().getNodeList()).build();
    }

    @POST
    @Path("/add")
    @ApiOperation(value = "Add Node", notes = "Adds a node to this server")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNode(@HeaderParam("authorization") final String authorizationHeader, @QueryParam("id") final String id, @QueryParam("url") final String url) {
        try {
            NodeManagement.getInstance().addNode(id, url);
            return Response.ok().build();
        } catch (RetrofitError e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "Couldn't connect to node: " + e.getMessage())).build();
        }
    }
}
