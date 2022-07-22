package com.lw.file;

import com.lw.util.IMulFileCloud;
import com.mec.util.TypeParser;

/**
 * @author leiWei
 * 传输的文件片段的基本信息，不包括文件片段内容
 */
public class FileSectionInfo {
    //片段所属文件id
    private int fileId;
    //在此文件中的偏移量
    private long offset;
    //文件片段长度
    private long length;

    public FileSectionInfo() {
    }

    public FileSectionInfo(int fileId, long offset, long length) {
        this.fileId = fileId;
        this.offset = offset;
        this.length = length;
    }

    /**
     * 由信息头字节流得到所有信息
     * @param headInfo
     */
    public FileSectionInfo(byte[] headInfo) {
        set(headInfo);
    }

    /**
     * 根据字节流以及偏移长度解析信息头
     * @param fileSectionHead
     * @param offset
     */
    public FileSectionInfo(byte[] fileSectionHead, long offset) {
        byte[] head = new byte[IMulFileCloud.HEAD_LENGTH];
        for(int i = 0; i < IMulFileCloud.HEAD_LENGTH; i++) {
            head[i] = fileSectionHead[i + (int) offset];
        }
        set(head);
    }

    /**
     * 字节数组转化为信息头
     * @param head
     */
    private void set(byte[] head) {
        this.fileId = TypeParser.bytesToInt(head,0);
        this.offset = TypeParser.bytesToLong(head, 4);
        this.length = TypeParser.bytesToLong(head, 12);
    }

    /**
     * 信息头转换为字节流信息
     * @return
     */
    public byte[] toByte() {
        byte[] head = new byte[IMulFileCloud.HEAD_LENGTH];
        TypeParser.intToBytes(this.fileId, head, 0);
        TypeParser.longToBytes(this.offset, head, 4);
        TypeParser.longToBytes(this.length, head, 12);
        return head;
    }

    /**
     * 加入字节流信息头
     * @param fileSectionInfo
     * @param offset
     * @return
     */
    public byte[] toByte(byte[] fileSectionInfo, int offset) {
        byte[] head = toByte();
        for(int i = 0; i < head.length; i++) {
            fileSectionInfo[i + offset] = head[i];
        }
        return fileSectionInfo;
    }

    /**
     * 文件的最后一个片段
     * @return
     */
    public static FileSectionInfo getEndSection() {
        return new FileSectionInfo(IMulFileCloud.ENDDING_OF_SECTION, 0, 0);
    }

    /**
     * 判断此片段是否为最后一个片段
     * @param section
     * @return
     */
    public static boolean isEndSection(FileSectionInfo section) {
        return section.fileId == IMulFileCloud.ENDDING_OF_SECTION;
    }


    public int getFileId() {
        return fileId;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return this.fileId + " -> " + this.length;
    }
}
