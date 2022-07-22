package com.lw.transmit;

import com.lw.file.FileInfo;
import com.lw.file.FileSection;
import com.lw.file.FileSectionInfo;
import com.lw.source.OriginalSource;
import com.lw.source.RequestSourceBase;
import com.lw.source.RequestSourceInfo;
import com.lw.source.Source;
import com.lw.util.IMulFileCloud;
import com.mec.util.ArgumentMaker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author leiWei
 * 网络传输行为
 */
public class NetAction {

    public NetAction() {
    }

    /**
     * 资源请求者向资源拥有者发送需要的片段信息以及自身的接收服务器地址
     * @param dos
     * @param requestSourceInfo
     */
    public static void sendFileSectionInfo(DataOutputStream dos, RequestSourceInfo requestSourceInfo) throws IOException {
        //发送资源头信息
        RequestSourceBase requestSourceBase = new RequestSourceBase(requestSourceInfo.getSourceId(), requestSourceInfo.getReceiveServer());
        dos.writeUTF(ArgumentMaker.gson.toJson(requestSourceBase));
        //发送文件列表信息，即文件片段总数,供对端判断是否接收完毕
        List<FileSectionInfo> fileSectionInfos = requestSourceInfo.getFileSectionInfos();
        int sectionCount = fileSectionInfos.size();
        dos.write(new FileSectionInfo(1, sectionCount, 0).toByte());

        //发送文件结构
        for (int i = 0; i < sectionCount; i++) {
            dos.write(fileSectionInfos.get(i).toByte());
        }
    }

    /**
     * 资源拥有者端接收资源请求者的请求信息
     * @param dis
     * @return
     */
    public static RequestSourceInfo receiveFileSectionInfo(DataInputStream dis) throws IOException {
        //根据资源头构造请求资源
        String strBase = dis.readUTF();
        RequestSourceBase requestSourceBase = ArgumentMaker.gson.fromJson(strBase, RequestSourceBase.class);
        List<FileSectionInfo> fileSectionInfoList = new ArrayList<>();
        RequestSourceInfo requestSourceInfo = new RequestSourceInfo(requestSourceBase.getSourceId(), requestSourceBase.getReceiveServer(), fileSectionInfoList);

        //接收文件片段总数
        byte[] buffer = new byte[IMulFileCloud.HEAD_SIZE];
        dis.read(buffer);
        FileSectionInfo fileSectionInfo = new FileSectionInfo(buffer);
        int sectionCount = (int) fileSectionInfo.getOffset();

        //接收文件片段信息
        while (sectionCount > 0) {
            dis.read(buffer);
            fileSectionInfo = new FileSectionInfo(buffer);
            requestSourceInfo.addFileSection(fileSectionInfo);
            sectionCount--;
        }
        return requestSourceInfo;
    }

    /**
     * 资源拥有者向资源请求者发送原始资源文件结构
     * @param dos
     * @param originalSource
     */
    public static void sendOriiginSource(DataOutputStream dos, OriginalSource originalSource) throws IOException {
        //发送资源头部
        Source source = originalSource.getSource();
        String strSource = ArgumentMaker.gson.toJson(source);
        dos.writeUTF(strSource);

        //发送资源文件结构
        while (originalSource.hasNext()) {
            FileInfo fileInfo = originalSource.next();
            dos.writeUTF(ArgumentMaker.gson.toJson(fileInfo));
        }

        //发送文件结束标志
        dos.writeUTF(ArgumentMaker.gson.toJson(FileInfo.getEndFileInfo()));
    }

    /**
     * 资源请求者接收源资源结构
     * @param dis
     * @return
     */
    public static OriginalSource receiveOriiginSource(DataInputStream dis) throws IOException {
        String strSource = dis.readUTF();
        Source source = ArgumentMaker.gson.fromJson(strSource, Source.class);
        OriginalSource originalSource = new OriginalSource(source);

        String strFileInfo = dis.readUTF();
        FileInfo fileInfo = ArgumentMaker.gson.fromJson(strFileInfo, FileInfo.class);

        while (!FileInfo.isEndFile(fileInfo)) {
            originalSource.addFile(fileInfo);
            strFileInfo = dis.readUTF();
            fileInfo = ArgumentMaker.gson.fromJson(strFileInfo, FileInfo.class);
        }
        return originalSource;
    }

    /**
     * 发送文件片段信息，以二进制的方式发送
     * @param os
     * @param fileSection
     * @param bufferSize
     */
    public static void send(OutputStream os, FileSection fileSection, int bufferSize) throws IOException {
        //发送信息头
        FileSectionInfo fileSectionInfo = fileSection.getFileSectionInfo();
        os.write(fileSectionInfo.toByte());

        //发送片段内容
        byte[] buffer = fileSection.getContext();
        int length = (int) fileSection.getLength();
        int offset = 0;
        int len = 0;
        while(length > 0) {
            len = length > bufferSize ? bufferSize : length;
            os.write(buffer, offset, len);
            offset += len;
            length -= len;
        }
    }

    /**
     * 未定义片段长度
     * @param os
     * @param fileSection
     */
    public static void send(OutputStream os, FileSection fileSection) throws IOException {
        send(os, fileSection, IMulFileCloud.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 接收片段
     * @param is
     * @param bufferSize
     * @return
     */
    public static FileSection receive(InputStream is, int bufferSize) throws IOException {
        //接收头部
        byte[] head = new byte[IMulFileCloud.HEAD_LENGTH];
        is.read(head);
        FileSectionInfo fileSectionInfo = new FileSectionInfo(head);

        FileSection fileSection = new FileSection();
        fileSection.setFileSectionInfo(fileSectionInfo);

        int length = (int) fileSectionInfo.getLength();
        byte[] context = new byte[length];
        int off = 0;
        int len = 0;
        while (length > 0) {
            len = length > bufferSize ? bufferSize : length;
            len = is.read(context, off, len);
            off += len;
            length -= len;
        }
        fileSection.setContext(context);
        return fileSection;
    }

    /**
     * 接收片段
     * @param is
     * @return
     */
    public static FileSection receive(InputStream is) throws IOException {
        return receive(is, IMulFileCloud.DEFAULT_BUFFER_SIZE);
    }
}
