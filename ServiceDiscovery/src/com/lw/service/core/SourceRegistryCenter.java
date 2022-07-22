package com.lw.service.core;

import com.lw.rmi.core.MethodFactory;
import com.lw.rmi.core.RmiServer;
import com.mec.nio.action.ActionBeanPool;
import com.mec.nio.core.IServerAction;
import com.mec.nio.core.Server;
import com.mec.util.IListener;
import com.mec.util.ISpeaker;
import com.mec.util.PropertiesParser;
import com.mec.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author leiWei
 * 资源注册中心
 * 内部由两个服务器完成
 * 1、资源拥有者使用NIO服务器
 * 2、资源申请者采用RMI服务器
 */
public class SourceRegistryCenter implements IListener, ISpeaker {
    //资源拥有者服务器，使用NIOCSFramework
    private Server sourceHolderServer;
    //资源请求者服务器，使用RMIFramework，使用短连接方式
    private RmiServer sourceRequestServer;

    private List<IListener> listenerList;
    //线程池
    private ThreadPool threadPool;

    public SourceRegistryCenter() throws Exception {
        //包扫描得到action
        ActionBeanPool.scanMappingByAnnotation("com.lw.service.action");

        this.sourceHolderServer = new Server();
        this.sourceHolderServer.addListener(this);
        this.sourceHolderServer.setServerAction(new NIOServerAction());

        //解析XML文件得到接口与实现类映射关系
        MethodFactory.scanRMIConfig("/src.mapping.xml");

        this.sourceRequestServer = new RmiServer();
        this.sourceRequestServer.addListener(this);

        this.listenerList = new ArrayList<>();
        this.threadPool = new ThreadPool();
    }

    /**
     *  开启资源注册中心，即开启两个服务器
     */
    public void startUp() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = this.threadPool.createThreadPool();
        this.sourceHolderServer.setThreadPoolExecutor(threadPoolExecutor);
        this.sourceRequestServer.setThreadPoolExecutor(threadPoolExecutor);

        this.sourceHolderServer.startUp();
        this.sourceRequestServer.startUp();
    }

    public void shutDown() {
        this.sourceHolderServer.shutDown();
        this.sourceRequestServer.shutDown();
    }
    public String getSourceHolderInformation() {
        SourceAndHolderMapping mapping = SourceAndHolderMapping.getInstance();
        Map<String, List<NodeAddress>> sourceList = mapping.getSourceMap();
        if(sourceList == null) {
            return "注册中心无资源";
        }
        StringBuffer str = new StringBuffer();
        for(String sourceId : sourceList.keySet()) {
            str.append("资源id：").append(sourceId).append("\n");
            List<NodeAddress> holderList = sourceList.get(sourceId);
            for(NodeAddress address : holderList) {
                str.append("\t").append(address).append("\n");
            }
        }
        return str.toString();
    }

    public boolean isStartup() {
        return this.sourceHolderServer.isStartUp();
    }

    public void setCorePoolSize(int corePoolSize) {
        this.threadPool.setCorePoolSize(corePoolSize);
    }
    public void setMaximumPoolSize(int maximumPoolSize) {
        this.threadPool.setMaximumPoolSize(maximumPoolSize);
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.threadPool.setKeepAliveTime(keepAliveTime);
    }

    public void setSourceHolderPort(int sourceHolderPort) {
        this.sourceHolderServer.setPort(sourceHolderPort);
    }

    public void setSourceRequestPort(int sourceRequestPort) {
        this.sourceRequestServer.setPort(sourceRequestPort);
    }

    /**
     * 配置文件设置服务器端口号
     * @param configPath
     */
    public void scanConfig(String  configPath) {
        PropertiesParser.load(configPath);
        int intValue;

        try {
             intValue = PropertiesParser.get("sourceHolderPort", Integer.class);
             if (intValue > 0) {
                 setSourceHolderPort(intValue);
             }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            intValue = PropertiesParser.get("sourceRequestPort", Integer.class);
            if (intValue > 0) {
                setSourceRequestPort(intValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dealMessage(String s) {
        speakOut(s);
    }

    @Override
    public void addListener(IListener iListener) {
        if(!this.listenerList.contains(iListener)) {
            this.listenerList.add(iListener);
        }
    }

    @Override
    public void removeListener(IListener iListener) {
        if(this.listenerList.contains(iListener)) {
            this.listenerList.remove(iListener);
        }
    }

    @Override
    public void speakOut(String s) {
        for(IListener listener : this.listenerList) {
            listener.dealMessage(s);
        }
    }

    class NIOServerAction implements IServerAction {

        public NIOServerAction() {
        }

        /**
         * 拥有者异常下线
         * @param id
         */
        @Override
        public void clientAbnormalDrop(String id) {
            System.out.println("资源拥有者【" + id + "】异常下线！");
            //删除该资源拥有者的所有资源
            SourceAndHolderMapping.getInstance().removeHolder(id);
        }
    }
}
