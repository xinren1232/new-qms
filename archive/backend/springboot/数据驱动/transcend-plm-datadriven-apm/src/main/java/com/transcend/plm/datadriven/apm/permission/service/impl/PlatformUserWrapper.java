package com.transcend.plm.datadriven.apm.permission.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.api.model.vo.DepartmentTreeVo;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;
import com.transcend.plm.datadriven.apm.feign.model.qo.UserQueryRequest;
import com.transcend.plm.datadriven.apm.feign.model.vo.PlatFormUserDTO;
import com.transcend.plm.datadriven.apm.feign.service.UacUserFeignService;
import com.transcend.plm.datadriven.apm.permission.service.IPlatformUserWrapper;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transsion.framework.sdk.open.dto.OpenResponse;
import com.transsion.framework.uac.model.dto.DepartmentDTO;
import com.transsion.framework.uac.model.dto.PageDTO;
import com.transsion.framework.uac.model.dto.UserDTO;
import com.transsion.framework.uac.model.request.DeptQueryRequest;
import com.transsion.framework.uac.model.request.DeptUserQueryRequest;
import com.transsion.framework.uac.model.request.UacRequest;
import com.transsion.framework.uac.service.IUacDeptService;
import com.transsion.framework.uac.service.IUacUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 平台用户包装类
 * @createTime 2023-09-25 10:02:00
 */
@Service
@Slf4j
public class PlatformUserWrapper implements IPlatformUserWrapper {
    @Resource
    private IUacUserService uacUserService;

    @Resource
    private IUacDeptService uacDeptService;

    @Resource
    private UacUserFeignService uacUserFeignService;

    @Override
    @Cacheable(value = CacheNameConstant.DEPARTMENT, key = "#departmentId")
    public DepartmentDTO getDepartmentByDepartmentId(String departmentId) {
        OpenResponse<DepartmentDTO> departmentDTOOpenResponse = uacDeptService.queryDetail(Long.valueOf(departmentId));
        return Optional.ofNullable(departmentDTOOpenResponse)
                .filter(OpenResponse::isSuccess)
                .map(OpenResponse::getData)
                .orElse(null);
    }

    @Override
    @Cacheable(value = CacheNameConstant.PARENT_DEPARTMENT, key = "#departmentId")
    public List<DepartmentDTO> getParentDepartmentByDepartmentId(String departmentId) {
        OpenResponse<List<DepartmentDTO>> resp = uacDeptService.queryParentDepts(Long.valueOf(departmentId));
        return Optional.ofNullable(resp)
                .filter(OpenResponse::isSuccess)
                .map(OpenResponse::getData)
                .orElse(null);
    }


    @Override
    @Cacheable(value = CacheNameConstant.USER, key = "#empNO")
    public ApmUser getUserBOByEmpNO(String empNO) {
        //查询人员详细信息
        OpenResponse<UserDTO> openResponse = uacUserService.queryDetail(empNO);
        if(null == openResponse || null == openResponse.getData()){
            return null;
        }
        if(!openResponse.isSuccess()) {
            return null;
        }
        UserDTO userDTO = openResponse.getData();
        return transPlatform2ApmUser(userDTO);
    }

    @Override
    public List<ApmUser> listUserBOByDepartmentId(String departmentId, boolean includeChildren) {
        UacRequest<DeptUserQueryRequest> request = new UacRequest<>();
        request.setCurrent(1);
        request.setSize(20000);
        DeptUserQueryRequest deptUserQueryRequest = new DeptUserQueryRequest();
        deptUserQueryRequest.setDepartmentId(Long.valueOf(departmentId));
        deptUserQueryRequest.setIncludeSubDeptUser(includeChildren);
        request.setParam(deptUserQueryRequest);
        //查询部门下所有人员信息
        OpenResponse<PageDTO<UserDTO>> openResponse = uacUserService.queryDeptUserPage(request);
        if(null == openResponse || null == openResponse.getData()){
            return Lists.newArrayList();
        }
        if(!openResponse.isSuccess()) {
            return Lists.newArrayList();
        }
        PageDTO<UserDTO> pageDTO = openResponse.getData();
        if(null == pageDTO || CollectionUtils.isEmpty(pageDTO.getDataList())){
            return Lists.newArrayList();
        }
        return pageDTO.getDataList().stream().map(PlatformUserWrapper::transPlatform2ApmUser).collect(Collectors.toList());
    }

    @Override
    public PageDTO<PlatFormUserDTO> queryPlatformUser(UacRequest<UserQueryRequest> userQueryRequestUacRequest) {
        log.info("查询平台用户信息，请求参数：{}", JSON.toJSONString(userQueryRequestUacRequest));
        OpenResponse<PageDTO<PlatFormUserDTO>> pageDTOOpenResponse = uacUserFeignService.queryWebPages(userQueryRequestUacRequest);
        log.info("查询平台用户信息，响应参数：{}", JSON.toJSONString(pageDTOOpenResponse));
        if(Objects.isNull(pageDTOOpenResponse)  || !pageDTOOpenResponse.isSuccess()){
            log.error("查询平台用户信息失败，原因如下：{}",  pageDTOOpenResponse.getMessage());
            throw new PlmBizException("查询平台用户信息失败，请联系IT管理员");
        }
        return pageDTOOpenResponse.getData();
    }

    @Override
    @Cacheable(value = CacheNameConstant.DEPARTMENT_CHILD, key = "#deptId")
    public List<Tree<Long>> queryChildDept(Long deptId) {
        UacRequest<DeptQueryRequest> request = new UacRequest<>();
        DeptQueryRequest deptQueryRequest = DeptQueryRequest.create();
        deptQueryRequest.setDeptIds(Lists.newArrayList(deptId));
        List<DepartmentDTO> dataList = new ArrayList<>();
        //此用户中心提供的接口最大分页条数500
        for (int i = 0; i < 100; i++){
            request.withCurrent(i+1).withSize(500).withSearchTotal(Boolean.FALSE).withParam(deptQueryRequest);
            List<DepartmentDTO> pageData = uacDeptService.queryPages(request).getData().getDataList();
            if (CollectionUtils.isNotEmpty(pageData)){
                dataList.addAll(pageData);
            }
            if (CollectionUtils.isEmpty(pageData) || pageData.size() < 500){
                break;
            }
        }
        List<DepartmentTreeVo> departmentTreeVoList = dataList.stream().map(v -> {
            DepartmentTreeVo departmentTreeVo = new DepartmentTreeVo();
            BeanUtils.copyProperties(v, departmentTreeVo);
            return departmentTreeVo;
        }).collect(Collectors.toList());


        // 将dataList转换为树形结构
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("id");
        config.setParentIdKey("parentId");
        config.setDeep(5);
        config.setChildrenKey("children");
        config.setWeightKey("id");
        List<Tree<Long>> result = TreeUtil.build(departmentTreeVoList, deptId, config, ((object, treeNode) -> {
            treeNode.putExtra("id", object.getId());
            treeNode.putExtra("parentId", object.getParentId());
            treeNode.putExtra("deptNo", object.getDeptNo());
            treeNode.putExtra("name", object.getName());
            treeNode.putExtra("parentNo", object.getParentNo());
            treeNode.putExtra("level", object.getLevel());
        }));
        return result;

    }

    @NotNull
    private static ApmUser transPlatform2ApmUser(UserDTO userDTO) {
        ApmUser ampUserBO = new ApmUser();
        ampUserBO.setEmpNo(userDTO.getEmployeeNo());
        ampUserBO.setName(userDTO.getRealName());
        ampUserBO.setEnName(userDTO.getEnName());
        if (CollectionUtils.isNotEmpty(userDTO.getDepts())) {
            ampUserBO.setDepts(userDTO.getDepts());
        }
        if (CollectionUtils.isNotEmpty(userDTO.getDepts()) && userDTO.getDepts().get(0).getId() != null) {
            ampUserBO.setDepartmentList(Lists.newArrayList(userDTO.getDepts().get(0).getId().toString().intern()));
        }
        return ampUserBO;
    }

}
