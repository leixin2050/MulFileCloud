package com.lw.transmit;

import com.lw.source.ReceivedSource;
import com.lw.source.eyes.core.NodeAddress;
import com.lw.sourse.eyes.action.SourceRequesterAction;

import java.io.IOException;


/**
 * @author leiWei
 */
public interface IAfterTransferFailed {

    /**
     * 断点续传的接口
     * @param Map<Integer, UnReceiveFileSectionInfo> 未发送完的接口
     */
    void breakpointContinuingly(NodeAddress nodeAddress, ReceivedSource receivedSource, SourceRequesterAction sourceRequesterAction) throws IOException;
}
