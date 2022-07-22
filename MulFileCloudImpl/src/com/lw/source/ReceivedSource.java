package com.lw.source;

import com.lw.core.HolderSourcePool;
import com.lw.file.*;
import com.lw.transmit.BreakpointContinuingly;
import com.lw.transmit.IAfterTransferFailed;
import com.lw.view.FileReceiveProgressMonitorDialog;
import com.lw.view.ProgressPanel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author leiWei
 * 接收资源，根据原始资源比较得到自身缺失或者不存在的文件列表
 */
public class ReceivedSource extends Source {
    public static final int DEFAULT_BUFFER_SIZE = 1 >> 15;

    //需要接收的文件id及对应的文件传输信息（文件信息的子类）
    private Map<Integer, FileAccessInfo> filePool;
    private int maxSectionLength;
    private OriginalSource originalSource;
    //断点续传的实现类
    private IAfterTransferFailed iAfterTransferFailed;

    private FileReceiveProgressMonitorDialog fileReceiveProgressMonitorDialog;

    public ReceivedSource(OriginalSource originalSource) {
        this.maxSectionLength = DEFAULT_BUFFER_SIZE;
        this.filePool = new HashMap<>();

        this.originalSource = originalSource;
        this.sourceId = originalSource.getSourceId();
        //在使用源文件生成接收文件前已经对文件路径进行了修改，此时的绝对根为接收路径
        this.absoluteRoot = originalSource.getAbsoluteRoot();

        this.iAfterTransferFailed = new BreakpointContinuingly();

        //检查接收所需的文件形成资源列表
        checkAndCollected(originalSource);
    }

    /**
     * 设置进度监视模态框
     * @param fileReceiveProgressMonitorDialog
     */
    public void setFileReceiveProgressMonitorDialog(FileReceiveProgressMonitorDialog fileReceiveProgressMonitorDialog) {
        this.fileReceiveProgressMonitorDialog = fileReceiveProgressMonitorDialog;
    }

    /**
     * 得到断点处理类
     * @return
     */
    public IAfterTransferFailed getiAfterTransferFailed() {
        return iAfterTransferFailed;
    }

    /**
     * 得到模态框
     * @return
     */
    public FileReceiveProgressMonitorDialog getFileReceiveProgressMonitorDialog() {
        return fileReceiveProgressMonitorDialog;
    }

    /**
     * 得到此文件的进度条，在接收时进行改变
     * @param fileId
     * @return
     */
    public ProgressPanel getProgressPanelById(int fileId) {
        return this.filePool.get(fileId).getFileReceiveProgress();
    }

    /**
     * 根据文件id删除此文件的进度条
     * @param filed
     */
    public void closeFileProgressById(int filed) {
        ProgressPanel fileReceiveProgress = this.filePool.get(filed).getFileReceiveProgress();
        this.fileReceiveProgressMonitorDialog.removeProgress(fileReceiveProgress);
    }

    /**
     * 得到一个文件未接收完毕的片段
     * @param fileId
     * @return
     */
    public UnReceiveFileSectionInfo getUnReceive(int fileId) {
        FileAccessInfo fileAccessInfo = this.filePool.get(fileId);
        if (fileAccessInfo == null) {
            throw new RuntimeException("文件编号【" + fileId + "】不存在");
        }
        return fileAccessInfo.getUnReceiveFileSectionInfo();
    }

    /**
     * 得到全部未接收到的片段
     * @return
     */
    public Map<Integer, UnReceiveFileSectionInfo> getUnReceivePool() {
        Map<Integer, UnReceiveFileSectionInfo> unReceiveFileSectionInfoPool = new ConcurrentHashMap<>();
        for (Integer fileId : this.filePool.keySet()) {
            UnReceiveFileSectionInfo unReceive = getUnReceive(fileId);
            if (!unReceive.isReceivedAll()) {
                unReceiveFileSectionInfoPool.put(fileId, unReceive);
            }
        }
        return unReceiveFileSectionInfoPool;
    }

    /**
     * 得到读写类
     * @param fileId
     * @return
     */
    public RandomAccessFile getRandomAccessFile(int fileId) throws FileNotFoundException {
        FileAccessInfo fileAccessInfo = this.filePool.get(fileId);
        if (fileAccessInfo == null) {
            throw new RuntimeException("文件编号【" + fileId + "】不存在");
        }
        return fileAccessInfo.open(this.absoluteRoot, "rw");
    }

    /**
     * 完成一个文件的读写操作后关闭读写操作
     * @param fileId
     * @throws IOException
     */
    public void closeRandomAccessFile(int fileId) throws IOException {
        FileAccessInfo fileAccessInfo = this.filePool.get(fileId);
        if (fileAccessInfo == null) {
            return;
        }
        fileAccessInfo.close();
    }

    public void addSender() {
        this.fileReceiveProgressMonitorDialog.addSender();
    }


    /**
     * 得到文件结构
     * @param originalSource
     */
    private void checkAndCollected(OriginalSource originalSource) {
        while (originalSource.hasNext()) {
            FileInfo fileInfo = originalSource.next();
            String fileName = fileInfo.getFileName();
            String filePath = this.absoluteRoot + fileName;
            File file = new File(filePath);
            //判断文件是否存在或大小不同
            if (!file.exists() || file.length() != fileInfo.getFileSize()) {
                FileAccessInfo fileAccessInfo = new FileAccessInfo(fileInfo);
                this.filePool.put(fileInfo.getId(), fileAccessInfo);
            }
        }
    }

    /**
     * 根据发送者个数对接收文件进行分片形成文件列表
     * @param senderCount
     * @return
     */
    public List<List<FileSectionInfo>> getFileSectionSenderList(int senderCount) {
        List<List<FileSectionInfo>> list = new ArrayList<>();
        while (senderCount > 0) {
            list.add(new ArrayList<>());
        }

        Set<Integer> idSet = this.filePool.keySet();
        int index = 0;
        //文件分片
        for(Integer fileId : idSet) {
            FileAccessInfo fileAccessInfo = this.filePool.get(fileId);
            long fileSize = fileAccessInfo.getFileSize();
            long offset = 0;
            spiltFileSection(list, fileId, fileSize, offset, senderCount);
        }
        return list;
    }

    /**
     * 分割文件
     */
    private int spiltFileSection(List<List<FileSectionInfo>> list, int fileId, long fileSize, long offset, int senderCount) {
        int length = 0;
        int index = 0;
        while (fileSize > 0) {
            length = fileSize > this.maxSectionLength ? this.maxSectionLength : (int) fileSize;
            list.get(index).add(new FileSectionInfo(fileId, offset, length));
            index = (index + 1) % senderCount;
            offset += length;
            fileSize -= length;
        }
        return index;
    }

    /**
     * 将断点处的未接受片段重新分片得到列表
     * @param unReceiveFileSectionInfoPool
     * @param senderCount
     * @return
     */
    public List<List<FileSectionInfo>> getFileSectionListByUnReceive(Map<Integer, UnReceiveFileSectionInfo> unReceiveFileSectionInfoPool, int senderCount) {
        List<List<FileSectionInfo>> list = new ArrayList<>();
        int index = 0;
        Set<Integer> idSet = unReceiveFileSectionInfoPool.keySet();
        for (Integer fileId : idSet) {
            UnReceiveFileSectionInfo unReceiveFileSectionInfo = unReceiveFileSectionInfoPool.get(fileId);
            //得到全部的未接收区间
            List<FileSectionInfo> unReceivedList = unReceiveFileSectionInfo.getUnReceivedList();
            for(FileSectionInfo fileSectionInfo : unReceivedList) {
                if (fileSectionInfo.getLength() > this.maxSectionLength) {
                    index = spiltFileSection(list, fileSectionInfo.getFileId(), fileSectionInfo.getLength(), fileSectionInfo.getOffset(), senderCount);
                } else {
                    list.get(index).add(fileSectionInfo);
                    //循环数组进行均匀分组
                    index = (index + 1) % senderCount;
                }
            }
        }
        return list;
    }

    /**
     * 得到文件传输信息
     * @param fileId
     * @return
     */
    public FileAccessInfo getFileAccessInfo(int fileId) {
        return this.filePool.get(fileId);
    }

    /**
     * 资源请求者接收资源完毕后进行身份转换
     */
    public void requesterToHolder() throws Exception {
        HolderSourcePool holderSourcePool = new HolderSourcePool();
        holderSourcePool.connect();
        holderSourcePool.registSource(originalSource);
    }

}
