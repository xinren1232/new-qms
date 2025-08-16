package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleAccess;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppAccessVo;

import java.util.List;

/**
* @author peng.qin
* @description 针对表【apm_role_access】的数据库操作Mapper
* @createDate 2023-09-20 16:15:29
* @Entity com.transcend.plm.datadriven.apm.repository.entity.ApmRoleAccess
*/
public interface ApmRoleAccessMapper extends BaseMapper<ApmRoleAccess> {
    /**
     *
     * 方法描述
     * @param roleBidList roleBidList
     * @return 返回值
     */
    List<ApmSpaceAppAccessVo> getAccessByRoleBidList(List<String> roleBidList);


}




