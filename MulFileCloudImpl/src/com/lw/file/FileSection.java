package com.lw.file;

import com.lw.util.IMulFileCloud;
import com.mec.util.TypeParser;

/**
 * @author leiWei
 * 传输文件的全部信息，包括片段内容 + 片段基本信息
 */
public class FileSection {
    private FileSectionInfo fileSectionInfo;
    //文件基本信息
    private byte[] context;

    public FileSection() {
    }

    public static FileSection getEndSection() {
        FileSection fileSection = new FileSection();
        fileSection.setFileSectionInfo(FileSectionInfo.getEndSection());
        return fileSection;
    }

    public static boolean isEndSection(FileSection fileSection) {
        return FileSectionInfo.isEndSection(fileSection.getFileSectionInfo());
    }


    public void setFileSectionInfo(FileSectionInfo fileSectionInfo) {
        this.fileSectionInfo = fileSectionInfo;
    }

    public FileSectionInfo getFileSectionInfo() {
        return fileSectionInfo;
    }

    public int getFileId() {
        return this.fileSectionInfo.getFileId();
    }

    public long getOffset() {
        return this.fileSectionInfo.getOffset();
    }

    public long getLength() {
        return this.fileSectionInfo.getLength();
    }

    public byte[] getContext() {
        return context;
    }

    public void setContext(byte[] context) {
        this.context = context;
    }
}
