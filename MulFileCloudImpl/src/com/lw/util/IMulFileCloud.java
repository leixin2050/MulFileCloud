package com.lw.util;

/**
 * @author leiWei
 */
public interface IMulFileCloud {
    int HEAD_SIZE = 20;
    int ENDDING_OF_FILE = -1;
    int ENDDING_OF_SECTION = -1;
    int DEFAULT_RECEIVE_SERVER_PORT = 54191;
    int DEFAULT_SOURCE_HOLDER_SERVER_PORT = 54194;
    int HEAD_LENGTH = 20;
    String DEFAULT_SOURCE_REGISTRY_CENTER_IP = "127.0.0.1";
    int DEFAULT_BUFFER_SIZE = 1<<15;
}
