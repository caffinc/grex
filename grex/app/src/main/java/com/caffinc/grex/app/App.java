package com.caffinc.grex.app;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.caffinc.grex.app.entities.GrexCLParams;
import com.caffinc.grex.app.filters.AuthFilter;
import com.caffinc.grex.app.resources.NodeResource;
import com.caffinc.grex.app.resources.ServerResource;
import com.caffinc.grex.app.utils.Api;
import com.caffinc.grex.app.utils.AuthToken;
import com.caffinc.grex.app.utils.GrexClient;
import com.caffinc.grex.common.util.IPUtil;
import com.caffinc.grex.core.Load;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.RetrofitError;

/**
 * Launches Grex
 *
 * @author Sriram
 */
public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        final GrexCLParams params = new GrexCLParams();
        JCommander jCommander = new JCommander(params);
        try {
            jCommander.parse(args);

            AuthToken.getInstance().setAuthToken(params.getAuth());

            // Workaround for resources from JAR files
            Resource.setDefaultUseCaches(false);
            Api api = new Api(params.getPort());
            api.setBaseUrl(params.getBaseUrl()).addFilter(AuthFilter.class);
            api.addStaticResource(App.class.getClassLoader().getResource("swaggerui").toURI().toString(), params.getBaseUrl() + "/docs/");

            api.addServiceResource(NodeResource.class, "Grex API", "API for managing Grex");
            if (params.getHost() == null) {
                api.addStaticResource(App.class.getClassLoader().getResource("ui").toURI().toString(), params.getBaseUrl() + "/");
            }

            api.enableCors();
            Server server = api.startNonBlocking();

            final String url = (params.getUrl() == null) ? "http://" + IPUtil.getIp() + ":" + params.getPort() + params.getBaseUrl() : params.getUrl();

            LOG.info("Grex Started:\nID:   {}\nURL:  {}\nAuth: {}\nHost: {}", Load.getInstance().getId(), url + "?auth=" + params.getAuth(), params.getAuth(), params.getHost());
            if (params.getHost() != null) {
                final GrexClient client = new GrexClient(params.getHost(), AuthToken.getInstance().getAuthToken());
                client.addNode(Load.getInstance().getId(), url);
                LOG.info("Successfully added self to host");
                new Thread("KeepNodeConnected") {
                    @Override
                    public void run() {
                        boolean connectionStatus = true;
                        while (true) {
                            try {
                                Thread.sleep(5000);
                                client.addNode(Load.getInstance().getId(), url);
                                if (!connectionStatus) {
                                    connectionStatus = true;
                                    LOG.info("Communications with host resumed");
                                }
                            } catch (InterruptedException e) {
                                LOG.warn("Interrupted while waiting to contact host", e);
                            } catch (RetrofitError e) {
                                LOG.warn("Communications with host lost: {}", e.getMessage());
                                connectionStatus = false;
                            }
                        }
                    }
                }.start();
            } else {
                LOG.info("Successfully started in Host mode");
                new GrexClient(url, AuthToken.getInstance().getAuthToken()).addNode(Load.getInstance().getId(), url);
            }
            server.join();
        } catch (ParameterException e) {
            jCommander.setProgramName("grex");
            jCommander.usage();
        } catch (RetrofitError e) {
            LOG.error("Unable to establish connection with host: {}", e.getMessage());
        } catch (Exception e) {
            LOG.error("Exception while launching Grex", e);
            System.exit(1);
        }
    }
}
