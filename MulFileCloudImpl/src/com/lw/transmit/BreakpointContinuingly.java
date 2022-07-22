package com.lw.transmit;

import com.lw.file.FileSectionInfo;
import com.lw.file.UnReceiveFileSectionInfo;
import com.lw.source.ReceivedSource;
import com.lw.source.RequestSourceInfo;
import com.lw.source.eyes.core.NodeAddress;
import com.lw.source.eyes.core.SourceRequester;
import com.lw.sourse.eyes.action.SourceRequesterAction;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * @author leiWei
 * 断点续传的实现
 */
public class BreakpointContinuingly implements IAfterTransferFailed{

    @Override
    public void breakpointContinuingly(NodeAddress nodeAddress, ReceivedSource receivedSource, SourceRequesterAction sourceRequesterAction) throws IOException {
        Map<Integer, UnReceiveFileSectionInfo> unReceivePool = receivedSource.getUnReceivePool();
        List<NodeAddress> holderAddressList = sourceRequesterAction.getHolderAddressList(receivedSource.getSourceId());
        //未接受完毕的片段重新划分为的片段
        int sendCount = holderAddressList.size();
        List<List<FileSectionInfo>> fileSectionListByUnReceive = receivedSource.getFileSectionListByUnReceive(unReceivePool, sendCount);

        String sourceId = receivedSource.getSourceId();
        receive(sourceId, nodeAddress, holderAddressList, fileSectionListByUnReceive);
    }

    /**
     * 二次发送未接收完毕文件片段请求信息
     * @param sourceId
     * @param nodeAddress
     * @param holderAddressList
     * @param fileSectionListByUnReceive
     * @throws IOException
     */
    private void receive(String sourceId, NodeAddress nodeAddress, List<NodeAddress> holderAddressList, List<List<FileSectionInfo>> fileSectionListByUnReceive) throws IOException {
        int index = 0;
        for (NodeAddress holder : holderAddressList) {
            RequestSourceInfo requestSourceInfo = new RequestSourceInfo(sourceId, nodeAddress, fileSectionListByUnReceive.get(index++));
            Socket socket = new Socket(holder.getIp(), holder.getPort());
            //重新发送请求
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        NetAction.sendFileSectionInfo(dos, requestSourceInfo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


}
