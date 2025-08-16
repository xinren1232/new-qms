package com.transcend.plm.datadriven.apm.permission.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleAccessAO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleAccessAddAO;
import com.transcend.plm.datadriven.apm.mapstruct.ApmRoleAccessConverter;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleAccess;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleAccessService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleAccessDomainService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleAccessVO;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transsion.framework.sdk.open.dto.OpenResponse;
import com.transsion.framework.uac.model.dto.DepartmentDTO;
import com.transsion.framework.uac.model.dto.PageDTO;
import com.transsion.framework.uac.model.dto.UserDTO;
import com.transsion.framework.uac.model.request.DeptUserQueryRequest;
import com.transsion.framework.uac.model.request.UacRequest;
import com.transsion.framework.uac.service.IUacDeptService;
import com.transsion.framework.uac.service.IUacUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author unknown
 */
@Service
public class ApmRoleAccessDomainService implements IApmRoleAccessDomainService {
    @Resource
    private ApmRoleAccessService apmRoleAccessService;

    @Resource
    private ApmAccessDomainService apmAccessDomainService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private IUacUserService uacUserService;

    @Resource
    private IUacDeptService uacDeptService;

    private void test(){
        String jobNumber = SsoHelper.getJobNumber();
        //查询人员详细信息
        OpenResponse<UserDTO> openResponse = uacUserService.queryDetail(jobNumber);
        UserDTO userDTO = openResponse.getData();
        long depId = userDTO.getDepts().get(0).getId();
        String deptNo = userDTO.getDepts().get(0).getDeptNo();
        //查询所以部门信息
        OpenResponse<List<DepartmentDTO>> openResponse1 = uacDeptService.queryParentDepts(depId);
        List<DepartmentDTO> departmentDTOs = openResponse1.getData();
        UacRequest<DeptUserQueryRequest> request = new UacRequest<>();
        request.setCurrent(1);
        request.setSize(20000);
        DeptUserQueryRequest deptUserQueryRequest = new DeptUserQueryRequest();
        deptUserQueryRequest.setDepartmentId(departmentDTOs.get(1).getId());
        deptUserQueryRequest.setIncludeSubDeptUser(true);
        request.setParam(deptUserQueryRequest);
        //查询部门下所有人员信息
        OpenResponse<PageDTO<UserDTO>> openResponse2 = uacUserService.queryDeptUserPage(request);
        PageDTO<UserDTO> pageDTO = openResponse2.getData();
        System.out.println(userDTO);
        /*List<Long> depats = new ArrayList<>();
        depats.add(employeeInfos.get(0).get)
        *//**查询成员的部门信息*//*
        List<DepartmentInfo> departmentList = newDeptService.batchQueryDeptByIds();*/
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(ApmRoleAccessAddAO apmRoleAccessAddAO) {
        //判断逻辑 如果apmRoleAccessAOS 为空 即删除功能
        if(CollectionUtils.isEmpty(apmRoleAccessAddAO.getApmRoleAccessAOS())){
           return apmRoleAccessService.remove(Wrappers.<ApmRoleAccess>lambdaQuery().eq(ApmRoleAccess::getRoleBid, apmRoleAccessAddAO.getRoleBid()));
        }else{
            //先删除原来的数据 在保存最新的
            List<ApmRoleAccess> apmRoleAccessList = apmRoleAccessService.list(Wrappers.<ApmRoleAccess>lambdaQuery().eq(ApmRoleAccess::getRoleBid, apmRoleAccessAddAO.getRoleBid()).eq(ApmRoleAccess::getDeleteFlag,0));
            if(CollectionUtils.isNotEmpty(apmRoleAccessList)){
                List<Integer> ids = new ArrayList<>();
                for(ApmRoleAccess apmRoleAccess:apmRoleAccessList){
                    ids.add(apmRoleAccess.getId());
                }
                apmRoleAccessService.removeByIds(ids);
            }
            //新增数据
            for(ApmRoleAccessAO apmRoleAccessAO:apmRoleAccessAddAO.getApmRoleAccessAOS()){
                apmRoleAccessAO.setRoleBid(apmRoleAccessAddAO.getRoleBid());
            }
            String jobNumber = SsoHelper.getJobNumber();
            List<ApmRoleAccess> apmRoleAccesses = ApmRoleAccessConverter.INSTANCE.aoList2EntityList(apmRoleAccessAddAO.getApmRoleAccessAOS());
            for(ApmRoleAccess apmRoleAccess:apmRoleAccesses){
                apmRoleAccess.setCreatedBy(jobNumber);
                apmRoleAccess.setUpdatedBy(jobNumber);
                apmRoleAccess.setDeleteFlag(CommonConst.DELETE_FLAG_NOT_DELETED);
            }
            return apmRoleAccessService.saveBatch(apmRoleAccesses);
        }
    }

    @Override
    public List<ApmRoleAccessVO> listByRoleBid(String roleBid) {
        List<ApmRoleAccess> apmRoleAccessList = apmRoleAccessService.list(Wrappers.<ApmRoleAccess>lambdaQuery().eq(ApmRoleAccess::getRoleBid, roleBid).eq(ApmRoleAccess::getDeleteFlag,0));
        return ApmRoleAccessConverter.INSTANCE.entityList2VOList(apmRoleAccessList);
    }
}
