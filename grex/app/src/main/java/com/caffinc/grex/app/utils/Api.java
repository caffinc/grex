package com.caffinc.grex.app.utils;

import com.caffinc.grex.common.entities.Tuple2;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.*;


/**
 * Builder class to build APIs
 *
 * @author Sriram
 */

public class Api {
    private static final Logger LOG = LoggerFactory.getLogger(Api.class);
    private final Server server;
    private final List<Tuple2<FilterHolder, String>> filterList;
    private final Set<String> serviceResources;
    private final List<ContextHandler> staticResources;
    private String baseUrl = "/";

    /**
     * Builder constructor to start the API at the specified port
     *
     * @param port Port to start the API on
     */
    public Api(int port) {
        this.server = new Server(port);
        this.filterList = new ArrayList<>();
        this.staticResources = new ArrayList<>();
        this.serviceResources = new HashSet<>();
    }


    /**
     * Builder constructor to start the API on a random port
     */
    public Api() {
        this(0);
    }


    /**
     * Enables CORS support on all path specs
     *
     * @return this
     */
    public Api enableCors() {
        return enableCors("/*");
    }


    /**
     * Enables CORS support at the given path spec
     *
     * @param pathSpec Path Spec to add the CORS support to
     * @return this
     */
    public Api enableCors(String pathSpec) {
        FilterHolder corsFilter = new FilterHolder(CrossOriginFilter.class);
        corsFilter.setAsyncSupported(true);
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "*");
        corsFilter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD,DELETE,PUT,OPTIONS");
        return addFilter(corsFilter, pathSpec);
    }


    /**
     * Adds a given filter class to all path specs
     *
     * @param filter Filter to add
     * @return this
     */
    public Api addFilter(Class<? extends Filter> filter) {
        return addFilter(filter, "/*");
    }


    /**
     * Adds a given filter class to the list at the given path spec
     *
     * @param filter   Filter to add
     * @param pathSpec Path Spec to filter
     * @return this
     */
    public Api addFilter(Class<? extends Filter> filter, String pathSpec) {
        FilterHolder filterHolder = new FilterHolder(filter);
        filterHolder.setAsyncSupported(true);
        return addFilter(filterHolder, pathSpec);
    }


    /**
     * Adds a filter to the list of filters
     *
     * @param filter   Filter Holder to add
     * @param pathSpec Path to add the filter to
     * @return this
     */
    public Api addFilter(FilterHolder filter, String pathSpec) {
        filterList.add(new Tuple2<>(filter, pathSpec));
        return this;
    }


    /**
     * Applies the given filters to all the ServletContextHandlers
     *
     * @param servletContextHandler Servlet Context Handler to add the filters to
     */
    private void applyFilters(ServletContextHandler servletContextHandler) {
        for (Tuple2<FilterHolder, String> filter : filterList) {
            LOG.info("Adding " + filter._1() + " filter");
            servletContextHandler.addFilter(filter._1(), filter._2(), EnumSet.allOf(DispatcherType.class));
        }
    }


    /**
     * Adds a static resource as an endpoint
     *
     * @param staticResourceLocation Location of static resource in the classpath
     * @param staticResourceEndpoint Endpoint name
     * @return this
     */
    public Api addStaticResource(String staticResourceLocation, String staticResourceEndpoint) {
        final ResourceHandler staticResourceHandler = new ResourceHandler();
        staticResourceHandler.setResourceBase(staticResourceLocation);
        final ContextHandler staticContext = new ContextHandler();
        staticContext.setContextPath(staticResourceEndpoint);
        staticContext.setHandler(staticResourceHandler);
        this.staticResources.add(staticContext);
        return this;
    }


    /**
     * Adds a given service to the server
     *
     * @param serviceResource Service Resource class
     * @return this
     */
    public Api addServiceResource(Class serviceResource) {
        this.serviceResources.add(serviceResource.getPackage().getName());
        return this;
    }


    /**
     * Adds a given service to the server
     *
     * @param serviceResource Service Resource class
     * @param title           Title of the resource
     * @param description     Description of the resource
     * @return this
     */
    public Api addServiceResource(Class serviceResource, String title, String description) {
        this.serviceResources.add(serviceResource.getPackage().getName());
        // Build Swagger for this class
        buildSwagger(serviceResource, title, description);
        return this;
    }


    /**
     * Wrapper for the startNonBlocking method that calls the server.join() method and awaits server shutdown
     *
     * @throws Exception Thrown by server.start()
     */
    public void start() throws Exception {
        try {
            startNonBlocking();
            server.join();
        } finally {
            server.stop();
        }
    }


    /**
     * Starts the API in a non-blocking call to server.start() and returns the server
     *
     * @return Server that was started
     * @throws Exception Thrown by the server.start() method
     */
    public Server startNonBlocking() throws Exception {
        // Workaround for resources from JAR files
        Resource.setDefaultUseCaches(false);

        // Apply all the filters to the service resources
        ServletContextHandler servletContextHandler = buildService(
                serviceResources.toArray(new String[serviceResources.size()]));
        applyFilters(servletContextHandler);

        // Add all the handlers to a list
        HandlerCollection handlers = new HandlerCollection();
        for (ContextHandler handler : staticResources) {
            handlers.addHandler(handler);
        }
        handlers.addHandler(servletContextHandler);

        // Start server
        server.setHandler(handlers);
        server.start();
        return server;
    }


    /**
     * Adds the given list of Resources into a ServletContextHandler
     *
     * @param serviceResourceLocation List of resources
     * @return Servlet Context Handler for the resources
     */
    private ServletContextHandler buildService(String[] serviceResourceLocation) {
        String serviceResourceEndpoint = baseUrl;
        String pathSpec = "/*";
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages(serviceResourceLocation);
        resourceConfig.register(MultiPartFeature.class);
        resourceConfig.register(JacksonFeature.class);
        ServletHolder holder = new ServletHolder(new ServletContainer(resourceConfig));
        holder.setAsyncSupported(true);
        handler.setContextPath(serviceResourceEndpoint);
        handler.addServlet(holder, pathSpec);
        return handler;
    }


    /**
     * Adds Swagger Bean Config for the given Resource
     *
     * @param clazz       Resource Class to enable Swagger for
     * @param title       Title of the Resource
     * @param description Description of the Resource
     */
    private void buildSwagger(Class clazz, String title, String description) {
        // Add the Swagger endpoint
        this.serviceResources.add(ApiListingResource.class.getPackage().getName());

        // This configures Swagger bean
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setResourcePackage(clazz.getPackage().getName());
        beanConfig.setBasePath(baseUrl);
        beanConfig.setDescription(description);
        beanConfig.setTitle(title);
        beanConfig.setScan(true);
    }


    /**
     * Set base URL
     *
     * @param baseUrl base url
     * @return Api
     */
    public Api setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
}