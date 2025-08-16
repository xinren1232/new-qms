package com.transcend.plm.datadriven.apm.integration.instance.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleAO;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.integration.instance.IInstanceOperateService;
import com.transcend.plm.datadriven.apm.integration.instance.InstanceOperateStrategyFactory;
import com.transcend.plm.datadriven.apm.integration.message.InstanceOperateMessage;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleIdentityService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleIdentityDomainService;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleDomainService;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleIdentityDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceVo;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

import static com.transcend.plm.datadriven.apm.constants.RoleConstant.ROOT_BID;
import static com.transcend.plm.datadriven.apm.enums.MessageLevelEnum.MIDDLE;
import static com.transcend.plm.datadriven.apm.enums.TypeEnum.SPACE;
import static com.transcend.plm.datadriven.apm.integration.constant.InstanceOperateConstant.*;
import static com.transcend.plm.datadriven.common.constant.DataBaseConstant.*;

/**
 * @Author yanjie
 * @Date 2023/12/20 15:37
 * @Version 1.0
 */
@Service
@Slf4j
public class RoleInstanceOperateService implements IInstanceOperateService, InitializingBean {

    @Resource
    ApmSpaceApplicationService apmSpaceApplicationService;

    @Resource
    ApmRoleService apmRoleService;

    @Resource
    ApmRoleIdentityService apmRoleIdentityService;

    @Resource
    ApmRoleDomainService apmRoleDomainService;

    @Resource
    RoleInstanceOperateService roleInstanceOperateService;

    @Resource
    IApmRoleIdentityDomainService iApmRoleIdentityDomainService;

    /**
     * 目前transcend 角色没有做父子关系，所以暂时可以不考虑角色有父角色的情况。之后有父角色之后可以放开这个开关。
     */
    @Value("${transcend.plm.apm.role.parentRoleSwitch:false}")
        private boolean parentRoleSwitch;

    @Override
    public boolean add(InstanceOperateMessage message) {
        log.info("RoleInstanceOperateService - add start！ message：{}", JSON.toJSONString(message));

        if (!message.instanceOperateMessageParamCheck()) {
            return false;
        }
        
        String spaceForeignBid = message.getMObject().get(DATA_ID).toString();
        ApmSpaceVo apmSpaceVo = apmSpaceApplicationService.getApmSpaceByForeignBid(spaceForeignBid);
        if (Objects.isNull(apmSpaceVo)) {
            errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - add space does not exist！ spaceForeignBid:" + spaceForeignBid);
            return false;
        }
        
        // ipm 项目唯一标识 + 角色 
        String roleForeignBid = spaceForeignBid + RoleConstant.SEMICOLON + message.getMObject().get(COLUMN_BID).toString();
        ApmRoleVO apmRoleVO = apmRoleService.getApmRoleVOByForeignBid(roleForeignBid);
        if (!Objects.isNull(apmRoleVO)) {
            errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - add role already exist！ roleForeignBid:" + roleForeignBid);
            return false;
        }

        String roleParentBid = RoleConstant.ROOT_BID;
        String roleParentPath = ROOT_BID;

        if (parentRoleSwitch && !ROOT_BID.equals(message.getMObject().get(PARENT_BID).toString())) {
            String roleParentForeignBid = spaceForeignBid + RoleConstant.SEMICOLON +
                    message.getMObject().get(PARENT_BID).toString();
            ApmRoleVO parentApmRoleVO = apmRoleService.getApmRoleVOByForeignBid(roleParentForeignBid);

            if (Objects.isNull(parentApmRoleVO)) {
                errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - add parentRole does not exist！ roleParentForeignBid:" + roleParentForeignBid);
                return false;
            }

            roleParentBid = parentApmRoleVO.getBid();
            roleParentPath = parentApmRoleVO.getPath();
        }

        ApmRoleDto apmRoleDto = convertToApmRoleDto(message, apmSpaceVo, roleForeignBid, roleParentBid, roleParentPath);

        if (! apmRoleService.createRole(apmRoleDto)) {
            errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - add error！");

            return false;
        }

        log.info("RoleInstanceOperateService - add end！");
        return true;
    }

    @Override
    public boolean update(InstanceOperateMessage message) {
        log.info("RoleInstanceOperateService - update start！ message：{}", JSON.toJSONString(message));

        if (!message.instanceOperateMessageParamCheck()) {
            return false;
        }

        String spaceForeignBid = message.getMObject().get(DATA_ID).toString();
        ApmSpaceVo apmSpaceVo = apmSpaceApplicationService.getApmSpaceByForeignBid(spaceForeignBid);
        if (Objects.isNull(apmSpaceVo)) {
            errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - update space does not exist！ spaceForeignBid:" + spaceForeignBid);
            return false;
        }

        // ipm 项目唯一标识 + 角色
        String roleForeignBid = spaceForeignBid + RoleConstant.SEMICOLON + message.getMObject().get(COLUMN_BID).toString();
        ApmRoleVO apmRoleVO = apmRoleService.getApmRoleVOByForeignBid(roleForeignBid);
        if (Objects.isNull(apmRoleVO)) {
            errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - update role does not exist！ roleForeignBid:" + roleForeignBid);
            return false;
        }

        String roleParentBid = RoleConstant.ROOT_BID;
        if (parentRoleSwitch && !ROOT_BID.equals(message.getMObject().get(PARENT_BID).toString())) {
            String roleParentForeignBid = spaceForeignBid + RoleConstant.SEMICOLON
                    + message.getMObject().get(PARENT_BID).toString();
            ApmRoleVO parentApmRoleVO = apmRoleService.getApmRoleVOByForeignBid(roleParentForeignBid);

            if (Objects.isNull(parentApmRoleVO)) {
                errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - update parentRole does not exist！ roleParentForeignBid:" + roleParentForeignBid);
                return false;
            }
            roleParentBid = parentApmRoleVO.getBid();
        }

        ApmRoleAO apmRoleAo = convertToApmRoleAo(message, apmSpaceVo, apmRoleVO, roleParentBid);
        log.info("RoleInstanceOperateService - update role! apmRoleAO:{}", apmRoleAo);

        try {
            if (CommonConst.DELETE_FLAG_DELETED.equals(message.getMObject().get(IS_DELETE))) {
                log.info("RoleInstanceOperateService - logicRemoveRole！");

                if (! roleInstanceOperateService.logicRemoveRole(apmRoleVO)) {
                    errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - logicRemoveRole error！");
                    return false;
                }
                return true;
            }
            apmRoleDomainService.update(apmRoleAo);
        } catch (Exception e) {
            errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - update error:" + e.getMessage());
            return false;
        }

        log.info("RoleInstanceOperateService - update end!");
        return true;
    }

    @Override
    public boolean delete(InstanceOperateMessage message) {
        log.info("RoleInstanceOperateService - delete start！ message：{}", JSON.toJSONString(message));

        if (!message.instanceOperateMessageParamCheck()) {
            return false;
        }

        String spaceForeignBid = message.getMObject().get(DATA_ID).toString();
        ApmSpaceVo apmSpaceVo = apmSpaceApplicationService.getApmSpaceByForeignBid(spaceForeignBid);
        if (Objects.isNull(apmSpaceVo)) {
            errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - delete space does not exist！ spaceForeignBid:" + spaceForeignBid);
            return false;
        }

        // ipm 项目唯一标识 + 角色
        String roleForeignBid = spaceForeignBid + RoleConstant.SEMICOLON + message.getMObject().get(COLUMN_BID).toString();
        ApmRoleVO apmRoleVO = apmRoleService.getApmRoleVOByForeignBid(roleForeignBid);
        if (Objects.isNull(apmRoleVO)) {
            errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - delete role does not exist！ roleForeignBid:" + roleForeignBid);
            return false;
        }

        try {

            if (! roleInstanceOperateService.physicsRemoveRole(apmRoleVO)) {
                errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - delete error！");
                return false;
            }

        } catch (Exception e) {
            errorMessageHandling(message, MIDDLE.getCode(), "RoleInstanceOperateService - delete error:" + e.getMessage());
            return false;
        }

        log.info("RoleInstanceOperateService - delete end！");
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        InstanceOperateStrategyFactory.register("role", this);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean logicRemoveRole(ApmRoleVO apmRoleVO) {
        ApmRoleDto apmRoleDto = new ApmRoleDto();
        apmRoleDto.setBid(apmRoleVO.getBid());

        ApmRoleIdentityDto apmRoleIdentityDto = new ApmRoleIdentityDto();
        apmRoleIdentityDto.setRoleBids(Lists.newArrayList(apmRoleVO.getBid()));
        iApmRoleIdentityDomainService.remove(apmRoleIdentityDto);
        return apmRoleDomainService.remove(apmRoleDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean physicsRemoveRole(ApmRoleVO apmRoleVO) {
        String roleBid = apmRoleVO.getBid();
        ApmRoleDto apmRoleDto = new ApmRoleDto();
        apmRoleDto.setBid(roleBid);

        ApmRoleIdentityDto apmRoleIdentityDto = new ApmRoleIdentityDto();
        apmRoleIdentityDto.setRoleBids(Lists.newArrayList(roleBid));

        apmRoleIdentityService.physicsRemove(apmRoleIdentityDto);
        return apmRoleService.physicsRemove(apmRoleDto);
    }

    private ApmRoleDto convertToApmRoleDto(InstanceOperateMessage message,
                                           ApmSpaceVo apmSpaceVo,
                                           String roleForeignBid,
                                           String roleParentBid,
                                           String roleParentPath) {
        ApmRoleDto apmRoleDto = new ApmRoleDto();
        apmRoleDto.setBid(SnowflakeIdWorker.nextIdStr());
        apmRoleDto.setForeignBid(roleForeignBid);
        apmRoleDto.setPbid(roleParentBid);
        apmRoleDto.setName(message.getMObject().getName());
        apmRoleDto.setCode(message.getMObject().get(COLUMN_BID).toString());
        apmRoleDto.setDescription(Objects.isNull(message.getMObject().get(DESCRIPTION)) ?
                StringUtils.EMPTY : message.getMObject().get(DESCRIPTION).toString());
        apmRoleDto.setPath(StringUtils.isBlank(roleParentPath) ?
                roleParentBid + RoleConstant.SEMICOLON + apmRoleDto.getBid() :
                roleParentPath + RoleConstant.SEMICOLON + apmRoleDto.getBid());
        apmRoleDto.setSphereBid(apmSpaceVo.getSphereBid());
        apmRoleDto.setCreatedBy(message.getOperator());
        apmRoleDto.setCreatedTime(new Date());
        apmRoleDto.setUpdatedBy(message.getOperator());
        apmRoleDto.setUpdatedTime(new Date());
        apmRoleDto.setEnableFlag(CommonConst.ENABLE_FLAG_ENABLE);
        apmRoleDto.setDeleteFlag(CommonConst.DELETE_FLAG_NOT_DELETED);
        return apmRoleDto;
    }

    private ApmRoleAO convertToApmRoleAo(InstanceOperateMessage message,
                                         ApmSpaceVo apmSpaceVo,
                                         ApmRoleVO apmRoleVO,
                                         String parentBid) {
        ApmRoleAO apmRoleAo = new ApmRoleAO();
        apmRoleAo.setId(apmRoleVO.getId());
        apmRoleAo.setBid(apmRoleVO.getBid());
        apmRoleAo.setPbid(parentBid);
        apmRoleAo.setDescription(Objects.isNull(message.getMObject().get(DESCRIPTION)) ?
                StringUtils.EMPTY : message.getMObject().get(DESCRIPTION).toString());
        apmRoleAo.setCode(message.getMObject().get(COLUMN_BID).toString());
        apmRoleAo.setName(message.getMObject().getName());
        apmRoleAo.setSphereBid(apmSpaceVo.getSphereBid());
        apmRoleAo.setBizBid(apmSpaceVo.getBid());
        apmRoleAo.setBizType(SPACE.getCode());
        apmRoleAo.setUpdatedBy(message.getOperator());
        apmRoleAo.setUpdatedTime(new Date());
        return apmRoleAo;
    }

    private void errorMessageHandling(InstanceOperateMessage message, String errLevel, String errMsg) {
        message.setErrInfo(errLevel, errMsg);
        log.error(errMsg);
    }
}
