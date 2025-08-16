package com.transcend.plm.alm.demandmanagement.common.dynamic.fields;

import com.transcend.plm.alm.demandmanagement.common.share.CurrentUserTechRespArBidListProvider;
import com.transcend.plm.datadriven.common.dynamic.fields.DynamicFields;
import com.transcend.plm.datadriven.common.share.RequestShareContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 当前用户技术负责人AR bid列表
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/7/1 15:46
 */
@Component
@AllArgsConstructor
public class CurrentUserTechRespArBidList implements DynamicFields {

    private final RequestShareContext context;

    @Override
    public String getCode() {
        return "CURRENT_USER_TECH_RESP_AR_BID_LIST";
    }

    @Override
    public String getName() {
        return "当前用户技术负责人拥有的AR列表";
    }

    @Override
    public Object getValue() {
        return context.getContent(CurrentUserTechRespArBidListProvider.class);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
