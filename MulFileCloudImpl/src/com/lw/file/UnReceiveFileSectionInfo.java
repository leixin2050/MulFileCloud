package com.lw.file;

import java.util.LinkedList;
import java.util.List;

/**
 * @author leiWei
 * 文件未接收完的片段
 * 且之后作为断点续传的准备
 */
public class UnReceiveFileSectionInfo {
    //未接收完全的片段信息列表
    private List<FileSectionInfo> unReceivedList;
    //文件id
    private int fileId;

    public UnReceiveFileSectionInfo(int fileId, long fileLength) {
        this.fileId = fileId;
        //用链表来存储信息长度，便于插入与删除
        this.unReceivedList = new LinkedList<>();
        //初始化时对于未接受片段为文件的一整个片段
        this.unReceivedList.add(new FileSectionInfo(fileId, 0, fileLength));
    }

    /**
     * 返回是否接收完毕
     * @return
     */
    public boolean isReceivedAll() {
        return this.unReceivedList.isEmpty();
    }

    /**
     * 接收一个片段
     * 1、得到需要添加到的片段
     * 2、删除此接收片段
     * 3、添加此接收片段所在的另外两个未接收片段
     * 1  2 3 8
     * @param recSection
     */
    public void receive(FileSectionInfo recSection) {
        FileSectionInfo orgSection = getFileSection(recSection.getOffset(), recSection.getLength());
        if (orgSection == null) {
            throw new RuntimeException("接收区间不存在！");
        }
        this.unReceivedList.remove(orgSection);

        long leftOffset = orgSection.getOffset();
        long leftlength = recSection.getOffset() - leftOffset;
        if(leftlength > 0) {
            this.unReceivedList.add(new FileSectionInfo(recSection.getFileId(), leftOffset, leftlength));
        }
        long rightOffset = recSection.getOffset() + recSection.getLength();
        long rightLength = orgSection.getOffset() + orgSection.getLength() - rightOffset;
        if (rightLength > 0) {
            this.unReceivedList.add(new FileSectionInfo(recSection.getFileId(), rightOffset, rightLength));
        }

    }

    /**
     * 根据文件偏移量与偏移长度得到插入的文件片段位置
     * offset + length <= file.offset + file.length
     * offset < file.offset + file.length
     * offset > file.offset
     * @param offset
     * @param length
     * @return
     */
    private FileSectionInfo getFileSection(long offset, long length) {
        for (FileSectionInfo orgSection : this.unReceivedList) {
            if (offset > orgSection.getOffset() && offset < (orgSection.getOffset() + orgSection.getLength()) && (offset + length < (orgSection.getOffset() + orgSection.getLength()))) {
                return orgSection;
            }
        }
        return null;
    }


}
