package com.transcend.plm.configcenter.role.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.role.infrastructure.repository.po.CfgRoleUserPo;

import java.util.List;

public interface CfgRoleUserService extends IService<CfgRoleUserPo> {
    Boolean removeByRoleBidAndJobNumber(String roleBid, String jobNumber);

    List<CfgRoleUserPo> listByRoleCode(String roleCode);

    List<String> listRoleCodesByJobNumber(String jobNumber);

    Boolean removeByRoleCode(String roleCode);

    List<CfgRoleUserPo> listByRoleBidAndJobNumbers(String roleBid, List<String> jobNumbers);
}
