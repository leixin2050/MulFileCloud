package com.lw.service.action;

import com.lw.service.core.NodeAddress;
import com.lw.service.core.SourceAndHolderMapping;

import java.util.List;

/**
 * @author leiWei
 * RMI服务器端实现的对资源拥有者列表的获取
 * 供资源请求者使用RMI请求调用
 */
public class SourceRequesterActionImpl implements SourceRequesterAction {
    @Override
    public List<NodeAddress> getSourceHolderAddress(String sourceId) {
        if (sourceId != null) {
            return SourceAndHolderMapping.getInstance().getHolderList(sourceId);
        }
        return null;
    }
}
