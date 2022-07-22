package com.lw.service.loadBalance;

import com.lw.service.core.SourceHolder;

/**
 * @author leiWei
 * 实现负载均衡的接口
 *
 */
public interface ILoadBalanceStrategy {

    /**
     * 供用户实现，若未设置则使用默认方式
     * @param sourceHolder
     * @return
     */
    boolean isResourceOwnerValid(SourceHolder sourceHolder);
}
