package com.lw.core;

import com.lw.source.OriginalSource;
import com.lw.source.eyes.core.SourceHolder;
import com.lw.util.IMulFileCloud;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leiWei
 * 资源拥有者（主）
 * 1、SourceHolder(注册中心的资源拥有者（client）)
 * 2、SourceRequesterMaster(资源请求者监听器，server（主）)
 * 3、Map<sourceId, OriginalSource> 存储资源拥有者的资源
 */
public class HolderSourcePool {
    //注册中心的Client
    private SourceHolder sourceHolder;
    //资源拥有者的“服务器”
    private SourceRequesterMaster sourceRequesterMaster;
    //资源拥有者的资源池
    private Map<String, OriginalSource> sourcePool;
    //判断是否连接到注册中心
    private static volatile Object singalFlag;

    private int bufferSize;

    public HolderSourcePool() throws IOException {
        HolderSourcePool.singalFlag = null;
        this.sourceRequesterMaster = SourceRequesterMaster.getInstance();
        this.sourceHolder = new SourceHolder();
        //设置拥有者服务器地址
        this.sourceHolder.setAddress(this.sourceRequesterMaster.getServerAddress());

        //设置资源进入监视器
        this.sourceRequesterMaster.setSourcePool(this);
        this.sourcePool = new HashMap<>();
        this.bufferSize = IMulFileCloud.DEFAULT_BUFFER_SIZE;
    }

    /**
     * 连接注册中心
     */
    public void connect() throws Exception {
        if (HolderSourcePool.singalFlag == null) {
            synchronized (HolderSourcePool.class) {
                if (HolderSourcePool.singalFlag == null) {
                    //连接注册中心的同时开启监视器
                    this.sourceHolder.connect();
                    this.sourceRequesterMaster.startServerMaster();
                    HolderSourcePool.singalFlag = new Object();
                }
            }
        }
    }

    /**
     * 关闭资源拥有者
     */
    public void close() {
        this.sourceHolder.offline();
        this.sourceRequesterMaster.closeServerMaster();
    }

    /**
     * 注册一个资源
     * @param originalSource
     */
    public void registSource(OriginalSource originalSource) {
        String sourceId = originalSource.getSourceId();
        if (sourceId == null || "".equals(sourceId) || this.sourcePool.containsKey(sourceId)) {
            return;
        }
        this.sourcePool.put(sourceId, originalSource);
        this.sourceHolder.registSource(sourceId);
    }

    public void registSource(List<OriginalSource> sourceList) {
        for (OriginalSource originalSource : sourceList) {
            registSource(originalSource);
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * 设置注册中心资源拥有者服务器地址
     * @param sourceHolderServerIp
     */
    public void setSourceRegistryCenterIp(String sourceHolderServerIp) {
        this.sourceHolder.setSourceRegistryCenterIp(sourceHolderServerIp);
    }

    public void setSourceRegistryCenterPort(int sourceHolderServerPort) {
        this.sourceHolder.setSourceRegistryCenterPort(sourceHolderServerPort);
    }

    public void setSourceRegistryCenterAddress(String sourceHolderServerIp, int sourceHolderServerPort) {
        setSourceRegistryCenterIp(sourceHolderServerIp);
        setSourceRegistryCenterPort(sourceHolderServerPort);
    }

    /**
     * 得到原始资源
     * @param sourceId
     * @return
     */
    public OriginalSource getOriginalSource(String sourceId) {
        return this.sourcePool.get(sourceId);
    }



}
