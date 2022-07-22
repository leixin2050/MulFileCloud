package com.lw.core;

import com.lw.source.eyes.core.NodeAddress;
import com.lw.util.IMulFileCloud;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author leiWei
 * 资源请求者监听器(资源拥有者服务器) - 单例
 * 1、监听请求者“客户端”的连接，接收资源请求者“客户端”发送的RequestSourceInfo
 * 2、根据接收到的资源请求者“服务器”的地址，启动一个Socket(客户端)线程连接SourceRequester的Server，发送文件片段
 */
public class SourceRequesterMaster implements Runnable{
    //SourceHolder的服务器
    private ServerSocket server;
    private static String sourceHolderServerIp;

    private static SourceRequesterMaster me;

    private HolderSourcePool sourcePool;

    //控制侦听资源请求者客户端连接线程
    private boolean goon;
    private Object lock;

    private SourceRequesterMaster() {
        this.goon = false;
        this.lock = new Object();
    }

    public static SourceRequesterMaster getInstance() throws IOException {
        if (SourceRequesterMaster.me == null) {
            synchronized (SourceRequesterMaster.class) {
                if (SourceRequesterMaster.me == null) {
                    SourceRequesterMaster.sourceHolderServerIp = InetAddress.getLocalHost().getHostAddress();
                    SourceRequesterMaster.me = new SourceRequesterMaster();
                }
            }
        }
        return SourceRequesterMaster.me;
    }

    public HolderSourcePool getSourcePool() {
        return sourcePool;
    }

    public void setSourcePool(HolderSourcePool sourcePool) {
        this.sourcePool = sourcePool;
    }

    /**
     * 资源拥有者服务器地址
     * @return
     */
    public NodeAddress getServerAddress() {
        return new NodeAddress(sourceHolderServerIp, IMulFileCloud.DEFAULT_SOURCE_HOLDER_SERVER_PORT);
    }

    /**
     * 开启侦听线程
     */
    public void startServerMaster() {

        synchronized (this.lock) {
            try {
                this.goon = true;
                this.server = new ServerSocket(IMulFileCloud.DEFAULT_SOURCE_HOLDER_SERVER_PORT);
                new Thread(this).start();
                this.lock.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭拥有者服务器监视器
     */
    public void closeServerMaster() {
        this.goon = false;
        if (this.server == null || this.server.isClosed()) {
            return;
        }
        try {
            this.server.close();
        } catch (IOException e) {
        } finally {
            this.server = null;
        }
    }

    @Override
    public void run() {
        synchronized (this.lock) {
            this.lock.notify();
        }

        while (this.goon) {
            try {
                Socket requesterClient = this.server.accept();
                new SourceFileSectionSender(requesterClient, this.sourcePool);
            } catch (IOException e) {
                //监听发生异常，关闭监视器
                closeServerMaster();
            }
        }
        //正常结束，关闭监视器
        closeServerMaster();
    }


}
