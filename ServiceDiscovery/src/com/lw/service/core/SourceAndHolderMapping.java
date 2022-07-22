package com.lw.service.core;

import com.sun.org.apache.xpath.internal.operations.Or;

import javax.xml.soap.Node;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author leiWei
 * 资源与拥有者列表映射类
 */
public class SourceAndHolderMapping {
    public static SourceAndHolderMapping me;
    //资源id与资源拥有者地址映射关系
    public  Map<String, List<NodeAddress>> holderNodeAddressMap;

    //拥有者网络id与真实地址映射关系
    public Map<String, NodeAddress> holderMapping;

    //拥有者的使用频率，负载均衡时启用判断是否可用
    public Map<NodeAddress, Integer> holderUseTime;

    private SourceAndHolderMapping() {
        this.holderMapping = new ConcurrentHashMap<>();
        this.holderNodeAddressMap = new ConcurrentHashMap<>();
        this.holderUseTime = new ConcurrentHashMap<>();
    }

    /**
     * 得到一个单例的资源映射表
     * @return
     */
    public static SourceAndHolderMapping getInstance() {
        if (me == null) {
            synchronized (SourceAndHolderMapping.class) {
                if (me == null) {
                    me = new SourceAndHolderMapping();
                }
            }
        }
        return me;
    }

    /**
     * 添加资源拥有者
     * @param holderId
     * @param holderAddress
     */
    public void addHolder(String holderId, NodeAddress holderAddress) {
        synchronized (SourceAndHolderMapping.class) {
            if(!me.holderMapping.keySet().contains(holderId)) {
                me.holderMapping.put(holderId, holderAddress);
            }
        }
    }

    /**
     * 资源拥有者下线，且删除他的所有资源
     * @param holderId
     */
    public void removeHolder(String holderId) {
        synchronized (SourceAndHolderMapping.class) {
            if (me.holderMapping.containsKey(holderId)) {
                NodeAddress holder = me.holderMapping.remove(holderId);
                removeSource(holder);
            }
        }
    }

    /**
     * 得到一个资源拥有者的网络id
     * @param netId
     * @return
     */
    public NodeAddress getHolder(String netId) {
        synchronized (SourceAndHolderMapping.class) {
            return me.holderMapping.get(netId);
        }
    }

    /**
     * 一个资源拥有者添加一个资源
     * @param sourceId
     * @param nodeAddress
     */
    public void addSource(String sourceId, NodeAddress nodeAddress) {
        synchronized (SourceAndHolderMapping.class) {
            List<NodeAddress> nodeAddressesList = me.holderNodeAddressMap.get(sourceId);
            if (nodeAddressesList == null) {
                nodeAddressesList = new LinkedList<NodeAddress>();
                me.holderNodeAddressMap.put(sourceId, nodeAddressesList);
            }
            if(nodeAddressesList.contains(nodeAddress)) {
                return;
            }
            nodeAddressesList.add(nodeAddress);
         }
    }

    /**
     * 一个拥有者者注册多个资源
     * @param sourceIds
     * @param nodeAddress
     */
    public void addSource(List<String> sourceIds, NodeAddress nodeAddress) {
        for(String sourceId : sourceIds) {
            addSource(sourceId, nodeAddress);
        }
    }

    /**
     * 删除某个资源的某个拥有者
     * @param sourceId
     * @param nodeAddress
     */
    public void removeSource(String sourceId, NodeAddress nodeAddress) {
        synchronized(SourceAndHolderMapping.class) {
            if (me.holderNodeAddressMap.keySet().contains(sourceId)) {
                List<NodeAddress> nodeAddressList = me.holderNodeAddressMap.get(sourceId);
                if (nodeAddressList.contains(nodeAddress)) {
                    nodeAddressList.remove(nodeAddress);
                }
            }
        }
    }

    /**
     * 删除整个资源
     * @param sourceId
     */
    public void removeSource(String sourceId) {
        synchronized(SourceAndHolderMapping.class) {
            if (me.holderNodeAddressMap.keySet().contains(sourceId)) {
                me.holderNodeAddressMap.remove(sourceId);
            }
        }
    }

    /**
     * 删除某个资源拥有者的全部资源
     * @param nodeAddress
     */
    public void removeSource(NodeAddress nodeAddress) {
        synchronized (SourceAndHolderMapping.class) {
            Set<String> keySet = me.holderNodeAddressMap.keySet();
            for (String key : keySet) {
                List<NodeAddress> nodeAddresses = me.holderNodeAddressMap.get(key);
                if (nodeAddresses.contains(nodeAddress)) {
                    nodeAddresses.remove(nodeAddress);
                }
            }
        }
    }

    /**
     * 得到一个资源的拥有者列表,防止资源请求者对资源拥有者列表进行破坏，复制一份副本交由资源请求者
     * @return
     */
    public List<NodeAddress> getHolderList(String sourceId) {
        synchronized (SourceAndHolderMapping.class) {
            List<NodeAddress> tarNodeAddressList = new ArrayList<>();
            List<NodeAddress> orgNodeAddressList = me.holderNodeAddressMap.get(sourceId);

            if (orgNodeAddressList == null) {
                return null;
            }

            for (NodeAddress nodeAddress : orgNodeAddressList) {
                tarNodeAddressList.add(nodeAddress);
            }

            return tarNodeAddressList;
        }
    }

    /**
     * 得到整个资源列表
     * @return
     */
    public Map<String, List<NodeAddress>> getSourceMap() {
        synchronized (SourceAndHolderMapping.class) {
            if (me.holderNodeAddressMap.isEmpty()) {
                return  null;
            }
            return me.holderNodeAddressMap;
        }
    }

    /**
     * 根据拥有者地址得到它的所有资源id
     * @param holder
     * @return
     */
    public List<String> getSourceIdList(NodeAddress holder) {
        synchronized(SourceAndHolderMapping.class) {
            List<String> sourceIdList = new ArrayList<>();
            Set<String> idList = me.holderNodeAddressMap.keySet();
            for (String sourceId : idList) {
                List<NodeAddress> nodeAddressList = me.holderNodeAddressMap.get(sourceId);
                if (!nodeAddressList.contains(holder)) {
                    continue;
                }
                sourceIdList.add(sourceId);
            }
            return sourceIdList;
        }
    }

}
