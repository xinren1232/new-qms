package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.datadriven.common.strategy.MultipleStrategyContext;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * 多对象树查询服务持有者
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/7 14:41
 */
@Service
public class MultiTreeDataQueryHolder
        extends MultipleStrategyContext<MultiTreeDataQueryService.Params, MultiTreeDataQueryService> {

    public MultiTreeDataQueryHolder(@NonNull List<MultiTreeDataQueryService> strategyList) {
        super(strategyList);
    }

    /**
     * 查询操作
     *
     * @param params 查询参数
     * @return 查询结果
     */
    @Nonnull
    public MultiTreeDataQueryService.Data query(MultiTreeDataQueryService.Params params) {
        return Optional.ofNullable(this.getStrategyService(params, false))
                .map(service -> service.query(params))
                .orElseGet(() -> new MultiTreeDataQueryService.Data(params.getMultiAppTreeConfig()));
    }
}
