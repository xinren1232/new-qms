package com.transcend.plm.datadriven.apm.common.dynamic.fields;

import com.transcend.plm.datadriven.api.model.vo.ThreeDeptVO;
import com.transcend.plm.datadriven.apm.common.share.CurrentUserDeptProvider;
import com.transcend.plm.datadriven.common.dynamic.fields.DynamicFields;
import com.transcend.plm.datadriven.common.share.RequestShareContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 当前用户一级部门
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 11:36
 */
@Component
@AllArgsConstructor
public class CurrentUserFirstDept implements DynamicFields {

    private RequestShareContext context;

    @Override
    public String getCode() {
        return "CURRENT_USER_FIRST_DEPT";
    }

    @Override
    public String getName() {
        return "当前登录人一级部门";
    }

    @Override
    public String getValue() {
        return Optional.ofNullable(context.getContent(CurrentUserDeptProvider.class))
                .map(ThreeDeptVO::getFirstDeptId).orElse(null);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
