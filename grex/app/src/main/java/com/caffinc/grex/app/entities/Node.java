package com.caffinc.grex.app.entities;

import com.caffinc.grex.common.entities.NodeStatus;

/**
 * Represents one connected node
 *
 * @author Sriram
 */
public class Node {
    private String id;
    private String url;
    private NodeStatus status;

    public Node() {
    }

    public Node(String id, String url, NodeStatus status) {
        this.id = id;
        this.url = url;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }
}
