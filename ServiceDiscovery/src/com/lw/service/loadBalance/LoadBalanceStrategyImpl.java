package com.lw.service.loadBalance;

import com.lw.service.core.SourceHolder;

/**
 * @author leiWei
 * 默认的负载均衡策略
 */
public class LoadBalanceStrategyImpl implements ILoadBalanceStrategy {

    @Override
    public boolean isResourceOwnerValid(SourceHolder sourceHolder) {

        return false;
    }
}
