package com.caffinc.grex.app.services;

import com.caffinc.grex.app.entities.Node;
import com.caffinc.grex.app.utils.AuthToken;
import com.caffinc.grex.app.utils.GrexClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.RetrofitError;

import java.util.*;

/**
 * Manages nodes connecting to this node
 *
 * @author Sriram
 */
public class NodeManagement {
    private static final Logger LOG = LoggerFactory.getLogger(NodeManagement.class);

    private static final NodeManagement INSTANCE = new NodeManagement();
    private final List<Node> nodeList = new ArrayList<>();
    private final Map<String, GrexClient> clients = new HashMap<>();

    private NodeManagement() {
        new Thread("NodeManagementThread") {
            @Override
            public void run() {
                while (true) {
                    synchronized (nodeList) {
                        Iterator<Node> iterator = nodeList.iterator();
                        while (iterator.hasNext()) {
                            Node node = iterator.next();
                            try {
                                node.setStatus(clients.get(node.getId()).getStatus());
                            } catch (RetrofitError e) {
                                LOG.warn("Unable to reach node {}, removing", node.getId());
                                iterator.remove();
                                clients.remove(node.getId());
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            LOG.warn("Interrupted while waiting between refreshing nodes", e);
                        }
                    }
                }
            }
        }.start();
    }

    public static NodeManagement getInstance() {
        return INSTANCE;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void addNode(String id, String url) {
        synchronized (nodeList) {
            if (!clients.containsKey(id)) {
                LOG.info("Adding node ({},{})", id, url);
                GrexClient client = new GrexClient(url, AuthToken.getInstance().getAuthToken());
                nodeList.add(new Node(id, url, client.getStatus()));
                clients.put(id, client);
            }
        }
    }
}
