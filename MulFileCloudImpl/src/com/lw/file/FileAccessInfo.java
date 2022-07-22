package com.lw.file;

import com.lw.view.FileReceiveProgressMonitorDialog;
import com.lw.view.ProgressPanel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author leiWei
 * 文件传输信息：
 *  1、文件未接收片段Unreceive
 *  2、文件读写RandomAccessFile
 */
public class FileAccessInfo extends FileInfo{
    //此文件剩余未接收片段
    private UnReceiveFileSectionInfo unReceiveFileSectionInfo;
    //字节文件读写工具
    private RandomAccessFile randomAccessFile;

    //外部传入的模态框
    private FileReceiveProgressMonitorDialog fileReceiveProgressMonitorDialog;
    //每个文件都有一个进度条，当开始写时创建，写操作完毕后删除
    private ProgressPanel fileReceiveProgress;

    public FileAccessInfo(FileInfo fileInfo) {
        super(fileInfo);
        this.unReceiveFileSectionInfo = new UnReceiveFileSectionInfo(fileInfo.getId(), fileInfo.getFileSize());
    }

    public void setFileReceiveProgressMonitorDialog(FileReceiveProgressMonitorDialog fileReceiveProgressMonitorDialog) {
        this.fileReceiveProgressMonitorDialog = fileReceiveProgressMonitorDialog;
    }

    /**
     * 得到文件传输进度条
     * @return
     */
    public ProgressPanel getFileReceiveProgress() {
        return fileReceiveProgress;
    }

    public UnReceiveFileSectionInfo getUnReceiveFileSectionInfo() {
        return unReceiveFileSectionInfo;
    }

    public void setUnReceiveFileSectionInfo(UnReceiveFileSectionInfo unReceiveFileSectionInfo) {
        this.unReceiveFileSectionInfo = unReceiveFileSectionInfo;
    }

    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }

    public void setRandomAccessFile(RandomAccessFile randomAccessFile) {
        this.randomAccessFile = randomAccessFile;
    }

    /**
     * 得到文件操作类
     * @param absoluteRoot
     * @param mode
     * @return
     * @throws FileNotFoundException
     */
    public RandomAccessFile open(String absoluteRoot, String mode) throws FileNotFoundException {
        if (this.randomAccessFile == null) {
            synchronized (FileAccessInfo.class) {
                if (this.randomAccessFile == null) {
                    String filePath = absoluteRoot + this.fileName;
                    //以读写方式打开，即接收端，需要创建目录
                    if ("rw".equals(mode)) {
                        //当存在模态框时直接进行进度条的初始化以及添加
                        if (this.fileReceiveProgressMonitorDialog != null) {
                            this.fileReceiveProgress = new ProgressPanel(0, fileSize, fileName);
                            this.fileReceiveProgressMonitorDialog.addProgress(this.fileReceiveProgress);
                        }
                        createDir(filePath);
                    }
                    this.randomAccessFile = new RandomAccessFile(filePath, mode);
                }
            }
        }
        return this.randomAccessFile;
    }

    /**
     * 创建目录
     * @param filePath
     */
    private void createDir(String filePath) {
        int index = filePath.lastIndexOf("\\");
        String dir = filePath.substring(0, index);
        File file = new File(dir);
        file.mkdirs();
    }

    /**
     * 关闭文件操作,包括关闭进度条
     */
    public void close() throws IOException {
        if(this.randomAccessFile == null) {
            return;
        }
        this.randomAccessFile.close();
        if (this.fileReceiveProgressMonitorDialog != null) {
            this.fileReceiveProgressMonitorDialog.removeProgress(this.fileReceiveProgress);
        }
    }


}
