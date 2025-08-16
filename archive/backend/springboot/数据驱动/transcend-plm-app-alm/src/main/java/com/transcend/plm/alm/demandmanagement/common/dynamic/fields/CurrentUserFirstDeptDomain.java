package com.transcend.plm.alm.demandmanagement.common.dynamic.fields;

import com.transcend.plm.alm.demandmanagement.common.share.CurrentUserFirstDeptDomainProvider;
import com.transcend.plm.datadriven.common.dynamic.fields.DynamicFields;
import com.transcend.plm.datadriven.common.share.RequestShareContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 当前用户一级部门邻域
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 11:36
 */
@Component
@AllArgsConstructor
public class CurrentUserFirstDeptDomain implements DynamicFields {

    private RequestShareContext context;

    @Override
    public String getCode() {
        return "CURRENT_USER_FIRST_DEPT_DOMAIN";
    }

    @Override
    public String getName() {
        return "当前登录人一级部门研发领域";
    }

    @Override
    public String getValue() {
        return context.getContent(CurrentUserFirstDeptDomainProvider.class);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
