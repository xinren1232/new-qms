package com.transcend.plm.datadriven.apm.common.share;

import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.vo.ThreeDeptVO;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.common.share.ContentProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 当前用户部门内容提供者
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 15:18
 */
@Component
public class CurrentUserDeptProvider implements ContentProvider<ThreeDeptVO> {

    @Resource
    private ApmRoleService apmRoleService;

    @Override
    public ThreeDeptVO getContent() {
        return apmRoleService.queryThreeDeptInfo(SsoHelper.getJobNumber());
    }
}
