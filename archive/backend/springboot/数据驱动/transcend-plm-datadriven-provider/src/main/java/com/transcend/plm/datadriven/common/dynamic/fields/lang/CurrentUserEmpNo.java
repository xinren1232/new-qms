package com.transcend.plm.datadriven.common.dynamic.fields.lang;

import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.common.dynamic.fields.DynamicFields;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户工号
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 11:10
 */
@Component
public class CurrentUserEmpNo implements DynamicFields {

    @Override
    public String getCode() {
        return "LOGIN_USER";
    }

    @Override
    public String getName() {
        return "当前登录用户";
    }

    @Override
    public String getValue() {
        return SsoHelper.getJobNumber();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
