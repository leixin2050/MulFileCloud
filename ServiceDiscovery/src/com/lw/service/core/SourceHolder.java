package com.lw.service.core;

import com.lw.service.action.SourceHolderActionCommond;
import com.mec.nio.core.Client;
import com.mec.nio.core.ClientActionAdapter;
import com.mec.util.ArgumentMaker;
import com.mec.util.PropertiesParser;

import java.io.IOException;
import java.util.List;

/**
 * @author leiWei
 * 资源拥有者
 */
public class SourceHolder {
    //与注册中心使用NIO通信的资源拥有者
    private Client sourceHolder;

    //网络id，即与注册中心接后得到的id
    private String netId;

    //拥有者的地址,注册中心不关心它的意义如何，只做存储,且使用set的方式设置进来
    private NodeAddress nodeAddress;


    public SourceHolder() {
        this.sourceHolder = new Client();
        this.sourceHolder.setClientAction(new NIOClientAction());
    }

    public NodeAddress getNodeAddress() {
        return nodeAddress;
    }

    /**
     * 由外部设置拥有者的地址信息
     * @param nodeAddress
     */
    public void setNodeAddress(NodeAddress nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    /**
     * 连接注册中心的服务器端
     */
    public void connect() throws IOException {
        this.sourceHolder.connect();
        //连接完毕后得到网络id，即netId
        this.netId = this.sourceHolder.getId();

        //连接完毕后进行拥有者的注册
        this.sourceHolder.request(SourceHolderActionCommond.registSourceHolder,
                new ArgumentMaker().addArg("netId",this.netId)
                                   .addArg("nodeAddress", this.nodeAddress).toString());
    }

    /**
     * 连接完毕后，已经注册完毕资源拥有者，下面就是对资源的注册
     * @param sourceId
     */
    public void registSource(String sourceId) {
        this.sourceHolder.request(SourceHolderActionCommond.registSource, new ArgumentMaker()
                .addArg("netId", this.netId)
                .addArg("sourceId", sourceId).toString());
    }

    /**
     * 注册多个资源
     * @param sourceIdList
     */
    public void registSource(List<String> sourceIdList) {
        this.sourceHolder.request(SourceHolderActionCommond.registSourceList,new ArgumentMaker()
                .addArg("netId", this.netId)
                .addArg("sourceIdList", sourceIdList).toString());
    }

    /**
     * 对拥有的某个资源的注销
     * @param sourceId
     */
    public void logoutSource(String sourceId) {
        this.sourceHolder.request(SourceHolderActionCommond.logoutSource, new ArgumentMaker()
                .addArg("netId", this.netId)
                .addArg("sourceId", sourceId).toString());
    }

    /**
     * 资源拥有者正常下线，由beforeOffline删除所有资源
     */
    public void offline() {
        this.sourceHolder.offline();
    }

    class NIOClientAction extends ClientActionAdapter {
        public NIOClientAction() {
        }

        //下线前对资源进行注销
        @Override
        public void beforeOffline() {
            sourceHolder.request(SourceHolderActionCommond.logoutSourceHolder, new ArgumentMaker()
                    .addArg("netId", netId).toString());
        }
    }

    //由外部设置ip和port
    public void setSourceRegistryCenterIp(String sourceHolderServerIp) {
        this.sourceHolder.setIp(sourceHolderServerIp);
    }

    public void setSourceRegistryCenterPort(int sourceHolderServerPort) {
        this.sourceHolder.setPort(sourceHolderServerPort);
    }

    public void loadSourceHolderConfig(String configPath) {
        PropertiesParser.load(configPath);

        String strValue = "";
        int intValue = 0;

        try {
            strValue = PropertiesParser.get("source_registry_center_ip", String.class);
            if (strValue != null) {
                setSourceRegistryCenterIp(strValue);
            }
        } catch (Exception e) {
        }

        try {
            intValue = PropertiesParser.get("source_registry_center_port", int.class);
            if (intValue > 0) {
                setSourceRegistryCenterPort(intValue);
            }
        } catch (Exception e) {
        }
    }



}
