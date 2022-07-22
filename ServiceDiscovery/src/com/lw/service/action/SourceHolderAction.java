package com.lw.service.action;

import com.lw.service.core.NodeAddress;
import com.lw.service.core.SourceAndHolderMapping;
import com.mec.nio.action.Action;
import com.mec.nio.action.ActionClass;
import com.mec.nio.action.Para;

import java.util.List;

/**
 * @author leiWei
 */
@ActionClass
public class SourceHolderAction {

    public SourceHolderAction() {
    }

    /**
     * 连接上服务器之后进行资源拥有者的注册
     * @param netId
     * @param nodeAddress
     */
    @Action(action = SourceHolderActionCommond.registSourceHolder)
    public void registSourceHolder(@Para("netId")String netId, @Para("nodeAddress") NodeAddress nodeAddress) {
        SourceAndHolderMapping.getInstance().addHolder(netId, nodeAddress);
    }

    /**
     * 根据netId和sourceId进行资源的注册
     * @param netId
     * @param sourceId
     */
    @Action(action = SourceHolderActionCommond.registSource)
    public void registSource(@Para("netId") String netId, @Para("sourceId") String sourceId) {
        SourceAndHolderMapping holderMapping = SourceAndHolderMapping.getInstance();
        NodeAddress holder = holderMapping.getHolder(netId);
        holderMapping.addSource(sourceId, holder);
    }

    /**
     * 一个资源拥有者注册多个资源
     * @param netId
     * @param sourceIdList
     */
    @Action(action = SourceHolderActionCommond.registSourceList)
    public void registSourceList(@Para("netId") String netId, @Para("sourceIdList") List<String> sourceIdList) {
        SourceAndHolderMapping holderMapping = SourceAndHolderMapping.getInstance();
        NodeAddress holder = holderMapping.getHolder(netId);
        holderMapping.addSource(sourceIdList, holder);
    }

    /**
     * 注销一个资源拥有者的一个资源
     * @param netId
     * @param sourceId
     */
    @Action(action = SourceHolderActionCommond.logoutSource)
    public void logoutSource(@Para("netId") String netId, @Para("sourceId") String sourceId) {
        SourceAndHolderMapping holderMapping = SourceAndHolderMapping.getInstance();
        NodeAddress holder = holderMapping.getHolder(netId);
        holderMapping.removeSource(sourceId, holder);
    }

    /**
     * 资源拥有者下线后对其拥有的资源进行注销
     * @param netId
     */
    @Action(action = SourceHolderActionCommond.logoutSourceHolder)
    public void removeSourceHolder(@Para("netId") String netId) {
        SourceAndHolderMapping holderMapping = SourceAndHolderMapping.getInstance();
        NodeAddress holder = holderMapping.getHolder(netId);
        holderMapping.removeSource(holder);
    }
}
