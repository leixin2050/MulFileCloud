package com.lw.core;

import com.lw.file.FileSectionInfo;
import com.lw.source.OriginalSource;
import com.lw.source.ReceivedSource;
import com.lw.source.RequestSourceInfo;
import com.lw.source.eyes.core.NodeAddress;
import com.lw.source.eyes.core.SourceRequester;
import com.lw.sourse.eyes.action.SourceRequesterAction;
import com.lw.transmit.NetAction;
import com.lw.view.FileReceiveProgressMonitorDialog;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * @author leiWei
 * 资源请求者核心
 * 1、SourceRequester(注册中心（client）)
 * 2、ReceiveServer（资源请求者“服务器”，等待SourceHolder“客户端”连接，并开启线程接收文件片段信息
 * 3、ReceivedSource（资源接收者需要接收的文件结构）
 */
public class RequesterSource {
    private SourceRequester sourceRequester;
    private ReceivedSource receivedSource;
    private ReceiveServer receiveServer;

    //初始化进度监视器，并且set进receivedSource中
    private FileReceiveProgressMonitorDialog fileReceiveProgressMonitorDialog;


    /**
     * @param originalSource 此原始资源的根目录以及被更换为接收根目录，在初始化之前已经更改过
     */
    public RequesterSource(OriginalSource originalSource) {
        //初始化接收资源结构
        this.receivedSource = new ReceivedSource(originalSource);
        this.sourceRequester = new SourceRequester();
        this.receiveServer = new ReceiveServer();
    }

    /**
     * 供用户修改根目录
     * @param absoluteRoot
     */
    public void setAbsoluteRoot(String absoluteRoot) {
        this.receivedSource.setAbsoluteRoot(absoluteRoot);
    }

    /**
     * 设置注册中心资源请求者服务器地址
     * @param sourceRequesterServerIp
     */
    public void setSourceRegistryCenterIp(String sourceRequesterServerIp) {
        this.sourceRequester.setSourceRegistryCenterIp(sourceRequesterServerIp);
    }

    public void setSourceRegistryCenterPort(int sourceRequesterServerPort) {
        this.sourceRequester.setSourceRegistryCenterPort(sourceRequesterServerPort);
    }

    public void setSourceRegistryCenterAddress(String sourceRequesterServerIp, int sourceRequesterServerPort) {
        setSourceRegistryCenterIp(sourceRequesterServerIp);
        setSourceRegistryCenterPort(sourceRequesterServerPort);
    }

    /**
     * 开始接收
     * 1、资源拥有者的查询
     * 2、请求信息的发送
     * 3、启动接收服务器等待拥有者客户端的连接
     * @param parentFrame
     * @param toolTip
     */
    public void startReceive(JFrame parentFrame, String toolTip, boolean showFileReceiveProgress) throws IOException {
        String sourceId = this.receivedSource.getSourceId();
        SourceRequesterAction sourceRequesterAction = this.sourceRequester.getProxy(parentFrame, toolTip, SourceRequesterAction.class);
        List<NodeAddress> holderAddressList = sourceRequesterAction.getHolderAddressList(sourceId);
        int senderCount = holderAddressList.size();

        // 开启进度监视模态框
        if (showFileReceiveProgress) {
            this.fileReceiveProgressMonitorDialog = new FileReceiveProgressMonitorDialog(parentFrame, senderCount);
        }

        this.receiveServer.setSenderCount(senderCount);
        this.receiveServer.setSourceRequesterAction(sourceRequesterAction);
        this.receiveServer.setReceivedSource(this.receivedSource);
        this.receiveServer.startReceive();

        //开启线程显示模态框
        new Thread(new Runnable() {
            @Override
            public void run() {
                receivedSource.setFileReceiveProgressMonitorDialog(fileReceiveProgressMonitorDialog);
                fileReceiveProgressMonitorDialog.showView();
            }
        }).start();


        //准备发送请求信息文件
        int index = 0;
        List<List<FileSectionInfo>> fileSectionSenderList = this.receivedSource.getFileSectionSenderList(senderCount);
        NodeAddress receiveServerAddress = this.receiveServer.getReceiveServerAddress();

        for (NodeAddress nodeAddress : holderAddressList) {
            List<FileSectionInfo> fileSectionInfoList = fileSectionSenderList.get(index++);
            new SectionSender(nodeAddress, sourceId, fileSectionInfoList, receiveServerAddress);
        }

    }

    /**
     * 发送请求信息
     */
    class SectionSender implements Runnable {
        private Socket requestClient;
        private DataOutputStream dos;
        private RequestSourceInfo requestSourceInfo;

        public SectionSender(NodeAddress nodeAddress, String sourceId, List<FileSectionInfo> fileSectionInfoList, NodeAddress receiveServerAddress) throws IOException {
            this.requestClient = new Socket(nodeAddress.getIp(), nodeAddress.getPort());
            this.dos = new DataOutputStream(this.requestClient.getOutputStream());
            this.requestSourceInfo = new RequestSourceInfo(sourceId, receiveServerAddress, fileSectionInfoList);
            new Thread(this, "发送需求线程").start();
        }

        @Override
        public void run() {
            try {
                NetAction.sendFileSectionInfo(this.dos, this.requestSourceInfo);
            } catch (IOException e) {
                close();
                e.printStackTrace();
            }
            //发送完毕后关闭
            close();
        }

        private void close() {
            if (this.requestClient != null && !this.requestClient.isClosed()) {
                try {
                    this.requestClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    this.requestClient = null;
                }
            }
            if (this.dos != null) {
                try {
                    this.dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    this.dos = null;
                }
            }
        }
    }
}
