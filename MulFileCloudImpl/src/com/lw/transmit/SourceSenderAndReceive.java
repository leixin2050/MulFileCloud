package com.lw.transmit;

import com.lw.file.*;
import com.lw.source.OriginalSource;
import com.lw.source.ReceivedSource;
import com.lw.util.IMulFileCloud;
import com.lw.view.ProgressPanel;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leiWei
 * 文件真正的发送（读）与接收（写）
 */
public class SourceSenderAndReceive {

    public SourceSenderAndReceive() {
    }

    public static void send(OutputStream os, OriginalSource originalSource, List<FileSectionInfo> fileSectionInfoList) throws IOException {
        send(os, originalSource, fileSectionInfoList, IMulFileCloud.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 资源拥有者client向资源请求者server发送文件
     * @param os
     * @param originalSource
     * @param fileSectionInfoList
     * @param bufferSize
     */
    public static void send(OutputStream os, OriginalSource originalSource, List<FileSectionInfo> fileSectionInfoList, int bufferSize) throws IOException {
        Map<Integer, FileAccessInfo> fileAccessInfoPool = getOriginalSource(originalSource, fileSectionInfoList);

        int curFileId;
        int lastFileId = -1;
        for (FileSectionInfo fileSectionInfo : fileSectionInfoList) {
            curFileId = fileSectionInfo.getFileId();
            //如果此文件是新文件的第一个片段，需要关闭上一个文件的全部
            if (lastFileId != -1 && lastFileId != curFileId) {
                fileAccessInfoPool.get(lastFileId).close();
            }
            FileAccessInfo fileAccessInfo = fileAccessInfoPool.get(curFileId);
            RandomAccessFile raf = fileAccessInfo.open(originalSource.getAbsoluteRoot(), "r");
            //读入文件内容
            FileSection fileSection = new FileSection();
            fileSection.setFileSectionInfo(fileSectionInfo);
            FileReadAndWrite.read(raf, fileSection);
            //发送信息
            NetAction.send(os, fileSection, bufferSize);
         }
         //文件片段发送完毕，关闭最后一个文件
        fileAccessInfoPool.get(lastFileId).close();
        //发送一个结束标志
        NetAction.send(os, FileSection.getEndSection(), bufferSize);
    }

    public static void receive(InputStream is, ReceivedSource receivedSource) throws IOException {
        receive(is, receivedSource, IMulFileCloud.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 资源请求者的接收服务器接收到文件
     * @param is
     * @param receivedSource
     * @param bufferSize
     */
    public static void receive(InputStream is, ReceivedSource receivedSource, int bufferSize) throws IOException {
        FileSection fileSection = NetAction.receive(is, bufferSize);
        FileSectionInfo fileSectionInfo = fileSection.getFileSectionInfo();
        while (!FileSection.isEndSection(fileSection)) {
            int fileId = fileSection.getFileId();
            RandomAccessFile raf = receivedSource.getRandomAccessFile(fileId);
            FileReadAndWrite.write(raf, fileSection);

            //未接收片段的整理
            UnReceiveFileSectionInfo unReceiveFileSectionInfo = receivedSource.getUnReceive(fileId);
            unReceiveFileSectionInfo.receive(fileSectionInfo);

            //改变进度条
            ProgressPanel fileReceivePanel = receivedSource.getProgressPanelById(fileId);
            if (fileReceivePanel != null) {
                fileReceivePanel.increase((int) fileSectionInfo.getLength());
            }

            //此文件接收完毕后关闭此文件，包括关闭进度条
            if (unReceiveFileSectionInfo.isReceivedAll()) {
                receivedSource.closeRandomAccessFile(fileId);
            }

            //继续接收文件片段信息
            fileSection = NetAction.receive(is, bufferSize);
        }
    }

    /**
     * 得到资源中文件的读写文件信息
     * @param originalSource
     * @return
     */
    private static Map<Integer, FileAccessInfo> getOriginalSource(OriginalSource originalSource, List<FileSectionInfo> fileSectionInfoList) {
        Map<Integer, FileAccessInfo> fileAccessInfoPool = new HashMap<Integer, FileAccessInfo>();
        //采用文件片段的方式是因为此次发送不一定需要全部的文件信息
        for (FileSectionInfo fileSectionInfo : fileSectionInfoList) {
            int fileId = fileSectionInfo.getFileId();
            if (fileAccessInfoPool.containsKey(fileId)) {
                continue;
            }
            //初始化除RandomAccessFile外的所有属性
            FileInfo fileInfo = originalSource.getFileInfo(fileId);
            FileAccessInfo fileAccessInfo = new FileAccessInfo(fileInfo);
            fileAccessInfoPool.put(fileId, fileAccessInfo);
        }
        return fileAccessInfoPool;
    }

}
