package com.lw.service.core;

import com.lw.rmi.core.IServerAddress;
import com.lw.rmi.core.RmiClient;
import com.lw.rmi.core.ServerAddress;
import com.mec.util.PropertiesParser;

import javax.swing.*;

/**
 * @author leiWei
 * 资源请求者
 */
public class SourceRequester {
    private RmiClient sourceRequester;
    private RMIServerAddress sourceRequesterServerAddress;

    public SourceRequester() {
        this.sourceRequester = new RmiClient();
        this.sourceRequesterServerAddress = new RMIServerAddress();
        this.sourceRequester.setServerAddress(this.sourceRequesterServerAddress);
    }

    public <T> T getProxy(Class<?> interfase) {
        return this.sourceRequester.getJDKProxy(interfase);
    }

    public <T> T getProxy(JFrame parent, String title, Class<?> interfase) {
        return this.sourceRequester.getJDKProxy(parent, title, interfase);
    }

    public void loadSourceRequesterServerConfig(String configPath) {
        PropertiesParser.load(configPath);

        String strValue = "";
        int intValue = 0;

        try {
            strValue = PropertiesParser.get("source_registry_center_ip", String.class);
            if (strValue != null && strValue.length() > 0) {
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

    public void setSourceRegistryCenterIp(String sourceRequesterServerIp) {
        this.sourceRequesterServerAddress.setServerAddressIp(sourceRequesterServerIp);
    }

    public void setSourceRegistryCenterPort(int sourceRequesterServerPort) {
        this.sourceRequesterServerAddress.setServerAddressPort(sourceRequesterServerPort);
    }


    class RMIServerAddress implements IServerAddress {
        private ServerAddress serverAddress;

        public RMIServerAddress() {
        }

        public void setServerAddressPort(int port) {
            this.serverAddress.setPort(port);
        }

        public void setServerAddressIp(String ip) {
            this.serverAddress.setIp(ip);
        }

        @Override
        public ServerAddress getServerAddress() {
            return this.serverAddress;
        }
    }
}
