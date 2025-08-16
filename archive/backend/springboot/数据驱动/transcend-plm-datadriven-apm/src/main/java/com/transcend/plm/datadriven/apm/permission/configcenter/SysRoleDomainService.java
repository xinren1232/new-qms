package com.transcend.plm.datadriven.apm.permission.configcenter;

import com.transcend.plm.configcenter.api.feign.CfgRoleFeignClient;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author unknown
 */
@Service
public class SysRoleDomainService {

    @Resource
    private CfgRoleFeignClient cfgRoleFeignClient;

    /**
     * 获取系统用户角色集
     * @param jobNumber
     * @return
     */
    @Cacheable(value = RoleConstant.SYS_ROLE, key = "#jobNumber")
    public List<String> listSysRoleCode(String jobNumber){
        if(StringUtils.isEmpty(jobNumber)){
            return new ArrayList<>();
        }
        return cfgRoleFeignClient.getSysGlobalRolesByUserId(jobNumber).getCheckExceptionData();
    }

    public List<CfgRoleVo> listSysRole(){
        return cfgRoleFeignClient.listGlobalRole().getCheckExceptionData();
    }

}
