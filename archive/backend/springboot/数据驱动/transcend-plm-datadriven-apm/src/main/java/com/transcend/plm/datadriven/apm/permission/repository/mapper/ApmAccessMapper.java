package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmAccess;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
* @author peng.qin
* @description 针对表【apm_access】的数据库操作Mapper
* @createDate 2023-09-20 16:15:29
* @Entity com.transcend.plm.datadriven.apm.repository.entity.ApmAccess
*/
public interface ApmAccessMapper extends BaseMapper<ApmAccess> {

    /**
     * 根据角色和领域获取ApmAccess列表。
     *
     * @param roleBids 角色ID集合
     * @param sphereBid 领域ID
     * @return ApmAccess列表
     */
    List<ApmAccess> listByRoles(@Param("roleBids") Set<String> roleBids, @Param("sphereBid") String sphereBid);

    /**
     * 根据一组bid值获取ApmAccess列表。
     *
     * @param bids bid值的集合
     * @return ApmAccess列表
     */
    List<ApmAccess> listByBids(@Param("bids") Set<String> bids);

    /**
     * 该方法根据领域ID查询具有角色的ApmAccess列表。
     *
     * @param sphereBid 领域ID
     * @return 包含ApmActionConfigVo对象的列表
     */
    List<ApmActionConfigVo> queryAccessWithRoleBySphereBid(@Param("sphereBid") String sphereBid);
}




