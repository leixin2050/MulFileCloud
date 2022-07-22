package com.lw.service.core;

/**
 * @author leiWei
 * 资源拥有者列表
 */
public class NodeAddress {
    private int port;
    private String ip;

    public NodeAddress() {
    }

    public NodeAddress(int port, String ip) {
        this.port = port;
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return  port + " @ " + ip ;
    }
}
