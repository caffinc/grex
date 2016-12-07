package com.caffinc.grex.app.entities;

import com.beust.jcommander.Parameter;

/**
 * Command line parameters
 *
 * @author Sriram
 */
public class GrexCLParams {
    @Parameter(names = {"-h",
            "--host"}, description = "Grex Host to connect to. If not present, node becomes host.", required = false)
    private String host;

    @Parameter(names = {"-a",
            "--auth"}, description = "Auth token to use. Preferably unique for each cluster.", required = false)
    private String auth = "grex";

    @Parameter(names = {"-p",
            "--port"}, description = "Port to listen to. Defaults to 4739.", required = false)
    private int port = 4739;

    @Parameter(names = {"-b",
            "--baseUrl"}, description = "Base URL to launch Grex under.", required = false)
    private String baseUrl = "/grex";

    @Parameter(names = {"-u",
            "--url"}, description = "URL Grex will use instead of the automatically generated URL.", required = false)
    private String url;

    public String getHost() {
        return host;
    }

    public String getAuth() {
        return auth;
    }

    public int getPort() {
        return port;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getUrl() {
        return url;
    }
}
