package com.lw.source;

import com.lw.source.eyes.core.NodeAddress;

/**
 * @author leiWei
 * 请求信息，包含：
 * 1、资源id
 * 2、请求者的接收服务器地址
 */
public class RequestSourceBase {
    private String sourceId;
    private NodeAddress receiveServer;

    public RequestSourceBase() {
    }

    public RequestSourceBase(String sourceId, NodeAddress receiveServer) {
        this.sourceId = sourceId;
        this.receiveServer = receiveServer;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public NodeAddress getReceiveServer() {
        return receiveServer;
    }

    public void setReceiveServer(NodeAddress receiveServer) {
        this.receiveServer = receiveServer;
    }

    @Override
    public String toString() {
        StringBuffer res = new StringBuffer("资源 : ");

        res.append(this.sourceId).append("\n资源请求者地址 : ")
                .append(this.receiveServer);

        return res.toString();
    }
}
