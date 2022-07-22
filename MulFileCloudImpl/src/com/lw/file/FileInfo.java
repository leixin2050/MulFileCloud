package com.lw.file;

import com.lw.util.IMulFileCloud;

import java.io.File;

/**
 * @author leiWei
 * 传输的文件的基本信息
 */
public class FileInfo {
    //文件id
    protected int id;
    //文件名，供之后显示进度条使用
    protected String fileName;
    //文件大小
    protected long fileSize;

    public FileInfo(FileInfo fileInfo) {
        this(fileInfo.getId(), fileInfo.getFileName(), fileInfo.getFileSize());
    }

    public FileInfo(int id, String fileName, long fileSize) {
        this.id = id;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    //得到接收完文件的标志
    public static FileInfo getEndFileInfo() {
        return new FileInfo(IMulFileCloud.ENDDING_OF_FILE, "", 0);
    }

    public static boolean isEndFile(FileInfo fileInfo) {
        return fileInfo.getId() == IMulFileCloud.ENDDING_OF_FILE;
    }
}
