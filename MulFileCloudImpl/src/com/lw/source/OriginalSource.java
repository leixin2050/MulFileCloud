package com.lw.source;

import com.lw.file.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author leiWei
 * 资源拥有者的资源类
 */
public class OriginalSource extends Source {
    //文件id，资源中不同文件的区分
    private static int fileId;
    //原文件列表，资源拥有者不需要对文件进行读写操作，故只需要基本信息
    private List<FileInfo> fileList;
    //判断是否还存在文件，为hasNext与next进行遍历的操作提供支持
    private int index;

    public OriginalSource(Source source) {
        this(source.getSourceId(), source.getAbsoluteRoot());
    }

    public OriginalSource() {
        this.index = 0;
        this.fileList = new ArrayList<>();
        fileId = 0;
    }

    public OriginalSource(String absoluteRoot, String sourceId) {
        this();
        this.sourceId = sourceId;
        this.absoluteRoot = absoluteRoot;
    }

    /**
     * 资源的文件数量
     * @return
     */
    public int fileCount() {
        return this.fileList.size();
    }

    /**
     * 得到此资源
     * @return
     */
    public Source getSource() {
        return new Source(this.sourceId, this.absoluteRoot);
    }

    /**
     * 遍历过程中判断是否遍历完毕
     * @return
     */
    public boolean hasNext() {
        if (this.index >= fileCount()) {
            this.index = 0;
            return false;
        }
        return true;
    }

    /**
     * 顺序遍历所有文件
     * @return
     */
    public FileInfo next() {
        return this.fileList.get(this.index++);
    }

    /**
     * 得到指定文件
     * @param fileId
     * @return
     */
    public FileInfo getFileInfo(int fileId) {
        for (FileInfo fileInfo : this.fileList) {
            if (fileInfo.getId() == fileId) {
                return fileInfo;
            }
        }
        return null;
    }

    /**
     * 添加文件
     * @param fileInfo
     */
    public void addFile(FileInfo fileInfo) {
        this.fileList.add(fileInfo);
    }

    /**
     * 添加一个文件作为一个资源
     * @param path
     */
    public void setOnlyFilePath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("文件路径【" + path + "】不存在");
        }
        String fileName = file.getAbsolutePath().substring(this.absoluteRoot.length());
        long fileSize = file.length();
        FileInfo fileInfo = new FileInfo(1, fileName, fileSize);
        this.fileList.add(fileInfo);
    }

    /**
     * 扫描根目录下的文件
     */
    public void scanAbsoluteRoot() {
        String absoluteRoot = this.getAbsoluteRoot();
        File root = new File(absoluteRoot);
        if (!root.exists()) {
            throw new RuntimeException("根目录【" + absoluteRoot
                    + "】错误，无法识别源文件");
        }
        collecte(absoluteRoot, root);
    }

    /**
     * 得到根目录下的文件列表
     * @param absoluteRoot
     * @param root
     */
    private void collecte(String absoluteRoot, File root) {
        File[] files = root.listFiles();
        for(File file : files) {
            if (file.isDirectory()) {
                collecte(absoluteRoot, file);
            } else {
                String absolutePath = file.getAbsolutePath();
                String name = absolutePath.substring(absoluteRoot.length());
                this.fileList.add(new FileInfo(++index, name, file.length()));
            }
        }
    }

}
