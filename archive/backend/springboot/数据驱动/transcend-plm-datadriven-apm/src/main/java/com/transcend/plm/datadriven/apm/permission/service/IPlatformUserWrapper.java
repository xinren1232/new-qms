package com.transcend.plm.datadriven.apm.permission.service;

import cn.hutool.core.lang.tree.Tree;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.feign.model.qo.UserQueryRequest;
import com.transcend.plm.datadriven.apm.feign.model.vo.PlatFormUserDTO;
import com.transsion.framework.uac.model.dto.DepartmentDTO;
import com.transsion.framework.uac.model.dto.PageDTO;
import com.transsion.framework.uac.model.request.UacRequest;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 平台用户包装类
 * @createTime 2023-09-25 09:58:00
 */
public interface IPlatformUserWrapper {
    /**
     * 通过部门id获取部门
     * @param departmentId 部门id
     * @return 部门信息
     */
    DepartmentDTO getDepartmentByDepartmentId(String departmentId);

    /**
     * 通过部门id获取父级部门
     * @param departmentId 部门id
     * @return 父级部门信息
     */
    List<DepartmentDTO> getParentDepartmentByDepartmentId(String departmentId);


    /**
     * 根据员工编号获取用户信息
     * @param empNO 员工编号
     * @return 用户信息
     */
    ApmUser getUserBOByEmpNO(String empNO);

    /**
     * 根据部门id获取用户信息
     *
     * @param departmentId    部门id
     * @param includeChildren 是否包含子部门
     * @return 用户信息
     */
    List<ApmUser> listUserBOByDepartmentId(String departmentId, boolean includeChildren);

    /**
     * 中转前端查询用户中心的用户信息（同前端查询用户中心入参）
     * @param request
     * @return
     */
    PageDTO<PlatFormUserDTO> queryPlatformUser(UacRequest<UserQueryRequest> request);

    List<Tree<Long>> queryChildDept(Long deptId);
}
