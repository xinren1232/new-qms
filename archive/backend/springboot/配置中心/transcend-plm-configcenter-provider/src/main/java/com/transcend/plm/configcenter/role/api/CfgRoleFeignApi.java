package com.transcend.plm.configcenter.role.api;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.CfgRoleFeignClient;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleUserVo;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.role.domain.service.CfgRoleDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @author yinbin
 * @version:
 * @date 2023/10/08 18:52
 */
@Api(value = "Role Controller", tags = "API-角色-控制器")
@RestController
public class CfgRoleFeignApi implements CfgRoleFeignClient {
    @Resource
    private CfgRoleDomainService cfgRoleDomainService;

    @Override
    public TranscendApiResponse<List<CfgRoleVo>> queryByCodes(List<String> codeList) {
        return TranscendApiResponse.success(cfgRoleDomainService.queryByCodes(codeList));
    }

    //    @Cacheable(value = CacheNameConstant.API_USER_ROLE_LIST, key = "#jobNumber")
    public TranscendApiResponse<List<String>> getRolesByUserId(String jobNumber) {
        return TranscendApiResponse.success(cfgRoleDomainService.getRoleCodesByJobNumber(jobNumber));
    }

    /**
     * 根据用户获取全局角色集
     *
     * @param jobNumber
     * @return
     */
    public TranscendApiResponse<List<String>> getSysGlobalRolesByUserId(String jobNumber) {
        return TranscendApiResponse.success(cfgRoleDomainService.getRoleCodesByJobNumber(jobNumber));
    }

    /**
     * @param roleCode
     * @return
     */
    @Cacheable(value = CacheNameConstant.API_ROLE_USER_LIST, key = "#roleCode")
    @Override
    public TranscendApiResponse<Set<String>> listUserSetByRoleCode(String roleCode) {
        return TranscendApiResponse.success(cfgRoleDomainService.listUserSetByRoleCode(roleCode));
    }

    /**
     * @return
     */
    @Override
    public TranscendApiResponse<List<CfgRoleVo>> tree() {
        return TranscendApiResponse.success(cfgRoleDomainService.tree());
    }

    @Override
    public TranscendApiResponse<List<CfgRoleVo>> listGlobalRole() {
        return TranscendApiResponse.success(cfgRoleDomainService.listGlobalRole());
    }

    /**
     * 根据角色编码查询所属用户
     *
     * @param roleCode
     * @return
     */
    @Override
    public TranscendApiResponse<List<CfgRoleUserVo>> listUsersByRoleCode(String roleCode) {
        return TranscendApiResponse.success(cfgRoleDomainService.getUsersByRoleCode(roleCode));
    }
}