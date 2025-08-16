package com.transcend.plm.datadriven.apm.integration.instance.impl;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleIdentityAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.integration.instance.IInstanceOperateService;
import com.transcend.plm.datadriven.apm.integration.instance.InstanceOperateStrategyFactory;
import com.transcend.plm.datadriven.apm.integration.message.InstanceOperateMessage;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleIdentityService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleIdentityDomainService;
import com.transcend.plm.datadriven.apm.permission.service.impl.PlatformUserWrapper;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleIdentityDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceVo;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleIdentityVO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transsion.framework.exception.BusinessException;
import com.transsion.framework.uac.model.dto.DepartmentDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

import static com.transcend.plm.datadriven.apm.enums.MessageLevelEnum.*;
import static com.transcend.plm.datadriven.apm.integration.constant.InstanceOperateConstant.*;

/**
 * @Author yanjie
 * @Date 2023/12/20 15:38
 * @Version 1.0
 */
@Service
@Slf4j
public class IdentityInstanceOperateService implements IInstanceOperateService, InitializingBean {

    @Resource
    ApmSpaceApplicationService apmSpaceApplicationService;

    @Resource
    ApmRoleService apmRoleService;

    @Resource
    ApmRoleIdentityService apmRoleIdentityService;

    @Resource
    ApmRoleIdentityDomainService apmRoleIdentityDomainService;

    @Resource
    private PlatformUserWrapper platformUserWrapper;


    @Override
    public boolean add(InstanceOperateMessage message) {
        log.info("IdentityInstanceOperateService - add start！ message：{}", JSON.toJSONString(message));

        if (!message.instanceOperateMessageParamCheck()) {
            return false;
        }

        String spaceForeignBid = message.getMObject().get(DATA_ID).toString();
        ApmSpaceVo apmSpaceVo = apmSpaceApplicationService.getApmSpaceByForeignBid(spaceForeignBid);
        if (Objects.isNull(apmSpaceVo)) {
            errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - add space does not exist！ spaceForeignBid: " + spaceForeignBid);
            return false;
        }

        String roleForeignBid = spaceForeignBid + RoleConstant.SEMICOLON + message.getMObject().get(GROUP_BID).toString();
        ApmRoleVO apmRoleVO = apmRoleService.getApmRoleVOByForeignBid(roleForeignBid);
        if (Objects.isNull(apmRoleVO)) {
            errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - add role does not exist！ roleForeignBid: " + roleForeignBid);
            return false;
        }

        String identityForeignBid = roleForeignBid + RoleConstant.SEMICOLON + message.getMObject().get(BaseDataEnum.BID.getCode()).toString();
        ApmRoleIdentityVO apmRoleIdentityVO = apmRoleIdentityService.getApmRoleIdentityVOByForeignBid(identityForeignBid);
        if (!Objects.isNull(apmRoleIdentityVO)) {
            errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - add identity already exists！ identityForeignBid: " + identityForeignBid);
            return false;
        }
        String roleIdentityType = MEMBER.equals(message.getMObject().get(TYPE).toString()) ?
                TypeEnum.EMPLOYEE.getCode() : message.getMObject().get(TYPE).toString();

        try {

            ApmRoleIdentityAO apmRoleIdentityAo = convertToapmRoleIdentityAO(apmRoleVO, roleIdentityType, message, identityForeignBid);

            log.info("IdentityInstanceOperateService - add identity, apmRoleIdentityAO:{}", apmRoleIdentityAo);
            if (! apmRoleIdentityService.saveRoleIdentity(apmRoleIdentityAo)) {
                errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - add error");
                return false;
            }

        } catch (Exception e) {
            if (StringUtils.isBlank(message.getErrMsg())) {
                errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - add error:" + e.getMessage());
            }
            return false;
        }

        log.info("IdentityInstanceOperateService - add end！");
        return true;
    }



    @Override
    public boolean update(InstanceOperateMessage message) {
        log.info("IdentityInstanceOperateService - update start！ message：{}", JSON.toJSONString(message));

        if (!message.instanceOperateMessageParamCheck()) {
            return false;
        }

        String spaceForeignBid = message.getMObject().get(DATA_ID).toString();
        ApmSpaceVo apmSpaceVo = apmSpaceApplicationService.getApmSpaceByForeignBid(spaceForeignBid);
        if (Objects.isNull(apmSpaceVo)) {
            errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - update space does not exist！ spaceForeignBid:" + spaceForeignBid);
            return false;
        }

        String roleForeignBid = spaceForeignBid + RoleConstant.SEMICOLON + message.getMObject().get(GROUP_BID).toString();
        ApmRoleVO apmRoleVO = apmRoleService.getApmRoleVOByForeignBid(roleForeignBid);
        if (Objects.isNull(apmRoleVO)) {
            errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - update role does not exist！ roleForeignBid: " + roleForeignBid);
            return false;
        }

        String identityForeignBid = roleForeignBid + RoleConstant.SEMICOLON + message.getMObject().get(BaseDataEnum.BID.getCode()).toString();
        ApmRoleIdentityVO apmRoleIdentityVO = apmRoleIdentityService.getApmRoleIdentityVOByForeignBid(identityForeignBid);
        if (Objects.isNull(apmRoleIdentityVO)) {
            errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - update identity does not exist！ identityForeignBid: " + identityForeignBid);
            return false;
        }

        String roleIdentityType = MEMBER.equals(message.getMObject().get(TYPE).toString()) ?
                TypeEnum.EMPLOYEE.getCode() : message.getMObject().get(TYPE).toString();

        try {

            if (CommonConst.DELETE_FLAG_DELETED.equals(message.getMObject().get(IS_DELETE))) {
                log.info("IdentityInstanceOperateService - logicRemoveRoleIdentity！");

                ApmRoleIdentityDto apmRoleIdentityDto = new ApmRoleIdentityDto();
                apmRoleIdentityDto.setId(apmRoleIdentityVO.getId());
                if (! apmRoleIdentityDomainService.remove(apmRoleIdentityDto)) {
                    errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - logicRemoveRoleIdentity error！");
                    return false;
                }
                return true;
            }

            ApmRoleIdentityAO apmRoleIdentityAo = convertToapmRoleIdentityAO(apmRoleVO, roleIdentityType, message, identityForeignBid);
            apmRoleIdentityAo.setId(apmRoleIdentityVO.getId());
            if (! apmRoleIdentityService.updateRoleIdentity(apmRoleIdentityAo)) {
                errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - update error！");
                return false;
            }

        } catch (Exception e) {
            if (StringUtils.isBlank(message.getErrMsg())) {
                errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - update error:" + e.getMessage());
            }
            return false;
        }

        log.info("IdentityInstanceOperateService - update end！");
        return true;
    }

    @Override
    public boolean delete(InstanceOperateMessage message) {

        log.info("IdentityInstanceOperateService - delete start！ message：{}", JSON.toJSONString(message));

        if (!message.instanceOperateMessageParamCheck()) {
            return false;
        }

        String spaceForeignBid = message.getMObject().get(DATA_ID).toString();
        ApmSpaceVo apmSpaceVo = apmSpaceApplicationService.getApmSpaceByForeignBid(spaceForeignBid);
        if (Objects.isNull(apmSpaceVo)) {
            errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - delete space does not exist！ spaceForeignBid:" + spaceForeignBid);
            return false;
        }

        String roleForeignBid = spaceForeignBid + RoleConstant.SEMICOLON + message.getMObject().get(GROUP_BID).toString();
        ApmRoleVO apmRoleVO = apmRoleService.getApmRoleVOByForeignBid(roleForeignBid);
        if (Objects.isNull(apmRoleVO)) {
            errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - delete role does not exist！ roleForeignBid:" + roleForeignBid);
            return false;
        }

        String identityForeignBid = roleForeignBid + RoleConstant.SEMICOLON + message.getMObject().get(BaseDataEnum.BID.getCode()).toString();
        ApmRoleIdentityVO apmRoleIdentityVO = apmRoleIdentityService.getApmRoleIdentityVOByForeignBid(identityForeignBid);
        if (Objects.isNull(apmRoleIdentityVO)) {
            errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - delete identity does not exist！ identityForeignBid:" + identityForeignBid);
            return false;
        }

        try {
            ApmRoleIdentityDto apmRoleIdentityDto = new ApmRoleIdentityDto();
            apmRoleIdentityDto.setId(apmRoleIdentityVO.getId());
            if (! apmRoleIdentityService.physicsRemove(apmRoleIdentityDto)) {
                errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - delete error！");
                return false;
            }

        } catch (Exception e) {
            errorMessageHandling(message, MIDDLE.getCode(), "IdentityInstanceOperateService - delete error：" + e.getMessage());
            return false;
        }

        log.info("IdentityInstanceOperateService - delete end！");

        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        InstanceOperateStrategyFactory.register("identity", this);
    }

    private ApmRoleIdentityAO convertToapmRoleIdentityAO(ApmRoleVO apmRoleVO,
                                                         String roleIdentityType,
                                                         InstanceOperateMessage message,
                                                         String identityForeignBid) {
        ApmRoleIdentityAO apmRoleIdentityAo = new ApmRoleIdentityAO();
        apmRoleIdentityAo.setRoleBid(apmRoleVO.getBid());
        apmRoleIdentityAo.setIdentity(message.getMObject().get(JOB_NUMBER).toString());
        apmRoleIdentityAo.setType(roleIdentityType);
        if (TypeEnum.EMPLOYEE.getCode().equals(roleIdentityType)) {
            String identity = apmRoleIdentityAo.getIdentity();
            String deptName = StringUtils.EMPTY;
            ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(identity);
            if (Objects.isNull(apmUser)) {
                errorMessageHandling(message, LOW.getCode(), "IdentityInstanceOperateService - " + message.getOperateType() + " error: 用户不存在！");
                throw new BusinessException("用户不存在！{}", identity);
            }
            if(CollectionUtils.isNotEmpty(apmUser.getDepts()) && StringUtils.isNotBlank(apmUser.getDepts().get(0).getName())){
                deptName = apmUser.getDepts().get(0).getName();
            }
            apmRoleIdentityAo.setName(apmUser.getName());
            apmRoleIdentityAo.setDeptName(deptName);
        } else if (TypeEnum.DEPARTMENT.getCode().equals(roleIdentityType)) {
            String identity = apmRoleIdentityAo.getIdentity();
            DepartmentDTO departmentInfo = platformUserWrapper.getDepartmentByDepartmentId(identity);
            if (Objects.isNull(departmentInfo)) {
                errorMessageHandling(message, LOW.getCode(), "IdentityInstanceOperateService - " + message.getOperateType() + " error: 部门不存在！");
                throw new BusinessException("部门不存在！{}", identity);
            }
            apmRoleIdentityAo.setName(departmentInfo.getName());
            apmRoleIdentityAo.setDeptName(departmentInfo.getName());
        } else {
            String identity = apmRoleIdentityAo.getIdentity();
            errorMessageHandling(message, LOW.getCode(), "IdentityInstanceOperateService - " + message.getOperateType() + " error: 群组暂时过滤！");
            throw new BusinessException("群组暂时过滤！{}", identity);
        }
        apmRoleIdentityAo.setForeignBid(identityForeignBid);
        return apmRoleIdentityAo;
    }

    private void errorMessageHandling(InstanceOperateMessage message, String errLevel, String errMsg) {
        message.setErrInfo(errLevel, errMsg);
        log.error(errMsg);
    }
}
