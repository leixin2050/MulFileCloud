package com.lw.source;

import com.lw.file.FileSectionInfo;
import com.lw.source.eyes.core.NodeAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * @author leiWei
 * 添加了需要的片段信息
 * 含有文件片段列表
 */
public class RequestSourceInfo extends RequestSourceBase {
    private List<FileSectionInfo> fileSectionInfos;

    public RequestSourceInfo() {
        super();
        this.fileSectionInfos = new ArrayList<>();
    }

    public RequestSourceInfo(String sourceId, NodeAddress receiveServer, List<FileSectionInfo> fileSectionInfos) {
        super(sourceId, receiveServer);
        this.fileSectionInfos = fileSectionInfos;
    }

    public void addFileSection(FileSectionInfo fileSection) {
        if (!fileSectionInfos.contains(fileSection)) {
             fileSectionInfos.add(fileSection);
        }
    }

    public List<FileSectionInfo> getFileSectionInfos() {
        return fileSectionInfos;
    }

    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append(super.toString()).append("\n");

        for (FileSectionInfo sectionInfo : this.fileSectionInfos) {
            res.append('\t').append(sectionInfo).append('\n');
        }

        return res.toString();
    }
}
