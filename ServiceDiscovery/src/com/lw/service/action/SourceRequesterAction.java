package com.lw.service.action;

import com.lw.service.core.NodeAddress;

import java.util.List;

/**
 * @author leiWei
 */
public interface SourceRequesterAction {

    List<NodeAddress> getSourceHolderAddress(String sourceId);
}
