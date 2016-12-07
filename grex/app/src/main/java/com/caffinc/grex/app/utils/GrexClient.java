package com.caffinc.grex.app.utils;

import com.caffinc.grex.common.entities.NodeStatus;
import com.squareup.okhttp.Response;
import org.apache.commons.codec.binary.Base64;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

import java.io.UnsupportedEncodingException;

/**
 * Grex client to communicate with Grex API
 *
 * @author Sriram
 */
public class GrexClient {
    private interface GrexInterface {
        @GET("/api/node/status")
        NodeStatus getStatus();

        @POST("/api/server/add")
        Response addNode(@Query("id") String id, @Query("url") String url);
    }

    private GrexInterface grex = null;

    public GrexClient(String apiUrl, String authToken) {
        try {
            final String authHeader = "Basic " + new String(Base64.encodeBase64(authToken.getBytes()), "UTF-8");
            this.grex = new RestAdapter.Builder().setEndpoint(apiUrl).setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("Authorization", authHeader);
                }
            }).build().create(GrexInterface.class);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public NodeStatus getStatus() {
        return grex.getStatus();
    }

    public Response addNode(String id, String url) {
        return grex.addNode(id, url);
    }
}
