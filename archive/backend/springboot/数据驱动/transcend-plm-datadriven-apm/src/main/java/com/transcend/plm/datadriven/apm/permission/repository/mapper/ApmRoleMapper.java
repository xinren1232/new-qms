package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author peng.qin
* @description 针对表【apm_role】的数据库操作Mapper
* @createDate 2023-09-20 16:15:29
* @Entity com.transcend.plm.datadriven.apm.repository.entity.ApmRole
*/
public interface ApmRoleMapper extends BaseMapper<ApmRole> {
    /**
     *
     * 方法描述
     * @param sphereBid sphereBid
     * @param identityList identityList
     * @return 返回值
     */
    List<String> getRoleListBySphereBidAndIdentityList(@Param("sphereBid") String sphereBid, @Param("list") List<String> identityList);

    /**
     *
     * 方法描述
     * @param departmentList departmentList
     * @param sphereBid sphereBid
     * @return 返回值
     */
    List<ApmRole> getRoleListByJobNumAndSphereBid(@Param("list") List<String> departmentList, String sphereBid);

    /**
     *
     * 方法描述
     * @param apmRoleDto apmRoleDto
     * @return 返回值
     */
    boolean physicsRemove(@Param("roleDto") ApmRoleDto apmRoleDto);

    /**
     *
     * 方法描述
     * @param type type
     * @param bizBid bizBid
     * @param identity identity
     * @return 返回值
     */
    List<String> getRoleListByBizBidAndIdentity(@Param("type") String type,@Param("bizBid") String bizBid, @Param("identity") String identity);

    /**
     *
     * 方法描述
     * @param departmentList departmentList
     * @param sphereBids sphereBids
     * @return 返回值
     */
    List<ApmRole> getRoleListByJobNumAndSphereBidList(@Param("list") List<String> departmentList, @Param("sphereBids") List<String> sphereBids);


    /**
     * 获取角色下所有的子角色
     *
     * @param roleCode 角色编码
     * @return 返回值
     */
    List<String> getAllChildCode(String roleCode);

    /**
     * 获取角色下所有的子bid
     *
     * @param bid 角色bid
     * @return 返回值
     */
    List<String> getAllChildBid(String bid);
}




