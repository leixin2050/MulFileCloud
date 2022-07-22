package com.lw.core;

import com.lw.file.FileSectionInfo;
import com.lw.source.OriginalSource;
import com.lw.source.RequestSourceInfo;
import com.lw.source.eyes.core.NodeAddress;
import com.lw.transmit.NetAction;
import com.lw.transmit.SourceSenderAndReceive;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * @author leiWei
 * 资源拥有者服务器接收到请求者客户端的连接，开启线程处理此请求
 * 接收资源请求信息
 * 发送资源文件
 */
public class SourceFileSectionSender implements Runnable{
    private Socket requesterClient;
    private HolderSourcePool sourcePool;
    //输入信道用来接收请求者客户端的信息
    private DataInputStream dis;


    /**
     * 资源拥有者客户端向请求者服务器连接
     * @param requesterClient
     * @param sourcePool
     * @throws IOException
     */
    public SourceFileSectionSender(Socket requesterClient, HolderSourcePool sourcePool) throws IOException {
        this.requesterClient = requesterClient;
        this.dis = new DataInputStream(this.requesterClient.getInputStream());
        this.sourcePool = sourcePool;
        new Thread(this).start();
    }

    public void setSourcePool(HolderSourcePool sourcePool) {
        this.sourcePool = sourcePool;
    }

    @Override
    public void run() {
        try {
            //接收到资源请求者客户端发送的请求信息
            RequestSourceInfo requestSourceInfo = NetAction.receiveFileSectionInfo(dis);
            String sourceId = requestSourceInfo.getSourceId();
            OriginalSource originalSource = this.sourcePool.getOriginalSource(sourceId);
            List<FileSectionInfo> fileSectionInfoList = requestSourceInfo.getFileSectionInfos();

            //连接资源请求者服务器
            NodeAddress receiveServerAddress = requestSourceInfo.getReceiveServer();
            Socket holderClient = new Socket(receiveServerAddress.getIp(), receiveServerAddress.getPort());
            OutputStream os = holderClient.getOutputStream();

            SourceSenderAndReceive.send(os, originalSource, fileSectionInfoList, this.sourcePool.getBufferSize());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
