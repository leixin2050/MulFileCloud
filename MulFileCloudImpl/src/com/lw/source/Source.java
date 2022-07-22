package com.lw.source;

import java.util.Objects;

/**
 * @author leiWei
 * 资源结构基类，源资源与传输资源均继承此类
 */
public class Source {
    //资源根目录
    protected String absoluteRoot;
    //资源id
    protected String sourceId;

    public Source() {
    }

    public Source(String absoluteRoot, String sourceId) {
        this.absoluteRoot = absoluteRoot;
        this.sourceId = sourceId;
    }

    public String getAbsoluteRoot() {
        return absoluteRoot;
    }

    public void setAbsoluteRoot(String absoluteRoot) {
        this.absoluteRoot = absoluteRoot;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * 之后需要对资源进行比较，查找到所需的资源，故需要重写equals方法
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Source source = (Source) o;
        return Objects.equals(absoluteRoot, source.absoluteRoot) &&
                Objects.equals(sourceId, source.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(absoluteRoot, sourceId);
    }

    @Override
    public String toString() {
        return "资源 " + "-> " + sourceId;
    }
}
