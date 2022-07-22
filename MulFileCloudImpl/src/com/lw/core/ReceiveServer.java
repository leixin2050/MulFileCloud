package com.lw.core;

import com.lw.source.ReceivedSource;
import com.lw.source.eyes.core.NodeAddress;
import com.lw.source.eyes.core.SourceRequester;
import com.lw.sourse.eyes.action.SourceRequesterAction;
import com.lw.transmit.SourceSenderAndReceive;
import com.lw.util.IMulFileCloud;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author leiWei
 * 资源请求者"服务器"
 * 1、侦听资源请求者发送客户端的连接
 * 2、接收文件片段
 */
public class ReceiveServer implements Runnable{
    private static String ip = "127.0.0.1";
    //发送者的数量，控制侦听线程是否继续
    private int senderCount;
    //当前数量，控制模态框的结束
    private int currCount;
    private ServerSocket server;
    private int port;
    private int bufferSize;

    //接收资源结构，接收文件时使用
    private ReceivedSource receivedSource;
    //用于断点续传
    private SourceRequesterAction sourceRequesterAction;

    public ReceiveServer() {
        this.port = IMulFileCloud.DEFAULT_RECEIVE_SERVER_PORT;
        this.bufferSize = IMulFileCloud.DEFAULT_BUFFER_SIZE;
    }

    public void setSourceRequesterAction(SourceRequesterAction sourceRequesterAction) {
        this.sourceRequesterAction = sourceRequesterAction;
    }

    public void setReceivedSource(ReceivedSource receivedSource) {
        this.receivedSource = receivedSource;
    }

    public int getSenderCount() {
        return senderCount;
    }

    public void setSenderCount(int senderCount) {
        this.senderCount = senderCount;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * 得到接收服务器地址
     * @return
     */
    public NodeAddress getReceiveServerAddress() {
        return new NodeAddress(ip, this.port);
    }

    /**
     * 开启接收端服务器，侦听客户端线程
     */
    public void startReceive() throws IOException {
        if (this.senderCount <= 0) {
            throw new RuntimeException("发送端未指定，无法开启接收服务器！");
        }
        this.server = new ServerSocket(this.port);
        new Thread(this, "接收服务器侦听客户端线程").start();
    }

    @Override
    public void run() {
        int count = 0;
        while (this.senderCount > count) {
            try {
                Socket holderClient = this.server.accept();
                new ReceiverConversation(holderClient);
                //添加一个接收进度条
                this.receivedSource.addSender();
                count++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //这里只是建立接收关系完毕，并没有接收完毕
        close();
    }

    private void close() {
        if (this.server == null || this.server.isClosed()) {
            return;
        }
        try {
            this.server.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.server = null;
        }

    }


    /**
     * 接收线程
     */
    class ReceiverConversation implements Runnable {
        private Socket holderClient;
        private InputStream is;

        public ReceiverConversation(Socket holderClient) throws IOException {
            this.holderClient = holderClient;
            this.is = this.holderClient.getInputStream();
            new Thread(this, "接收线程").start();
        }

        @Override
        public void run() {
            try {
                //接收文件
                SourceSenderAndReceive.receive(this.is, receivedSource, bufferSize);
            } catch (IOException e) {
                //断点异常处理
                receivedSource.getiAfterTransferFailed().breakpointContinuingly(getReceiveServerAddress(), receivedSource, sourceRequesterAction);
                close();
                e.printStackTrace();
            }
            ++currCount;
            //接收完毕后关闭模态框
            if (currCount > senderCount) {
                receivedSource.getFileReceiveProgressMonitorDialog().closeView();
            }
            close();
            try {
                //接收完毕后，进行身份转换，即接收者转换为资源拥有者
                receivedSource.requesterToHolder();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void close() {
            if (this.holderClient != null && !this.holderClient.isClosed()) {
                try {
                    this.holderClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    this.holderClient = null;
                }
            }

            if (this.is != null) {
                try {
                    this.is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    this.is = null;
                }
            }
        }
    }
}
