package com.transcend.plm.datadriven.apm.integration.instance.impl;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmRoleQO;
import com.transcend.plm.datadriven.api.model.VersionObjectEnum;
import com.transcend.plm.datadriven.apm.integration.instance.IInstanceOperateService;
import com.transcend.plm.datadriven.apm.integration.instance.InstanceOperateStrategyFactory;
import com.transcend.plm.datadriven.apm.integration.message.InstanceOperateMessage;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleIdentityService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleIdentityDomainService;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleIdentityDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceVo;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceService;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.enums.MessageLevelEnum.*;
import static com.transcend.plm.datadriven.apm.enums.TypeEnum.SPACE;
import static com.transcend.plm.datadriven.apm.integration.constant.InstanceOperateConstant.*;
import static com.transcend.plm.datadriven.apm.integration.consumer.InstanceOperateConsumer.setUser;

/**
 * @Author yanjie
 * @Date 2023/12/20 15:37
 * @Version 1.0
 */

@Slf4j
@Service
public class SpaceInstanceOperateService implements IInstanceOperateService, InitializingBean {

    @Value("${transcend.plm.apm.space.foreign.defaultTemplateBid:1186635102130671616}")
    private String foreignDefaultTemplateBid;
    @Value("${transcend.plm.apm.product-project.model-code:A00A00}")
    private String productProjectModelCode;
    @Value("${transcend.plm.apm.space.defaultIconUrl:https://oss-sz-test-01.oss-cn-shenzhen.aliyuncs.com/ipm/doc/files/1701654773260/128x128.png}")
    private String spaceDefaultIconUrl;
    @Resource
    ApmSpaceApplicationService apmSpaceApplicationService;

    @Resource
    IApmRoleDomainService iApmRoleDomainService;

    @Resource
    IApmRoleIdentityDomainService iApmRoleIdentityDomainService;

    @Resource
    ApmSpaceService apmSpaceService;

    @Resource
    ApmRoleService apmRoleService;

    @Resource
    ApmRoleIdentityService apmRoleIdentityService;

    @Resource
    SpaceInstanceOperateService spaceInstanceOperateService;

    @Resource
    private IAppDataService appDataService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private ObjectModelDomainService objectModelDomainService;

    @Override
    public boolean add(InstanceOperateMessage message) {

        if (Objects.nonNull(message.getMObject().get(OWNER))) {
            setUser(message.getMObject().get(OWNER).toString());
        }

        log.info("SpaceInstanceOperate - add start！ message：{}", JSON.toJSONString(message));

        if (!message.instanceOperateMessageParamCheck()) {
            return false;
        }
        String foreignBid = message.getMObject().get(DATA_BID).toString();

        ApmSpaceVo apmSpaceVo = apmSpaceApplicationService.getApmSpaceByForeignBid(foreignBid);
        if (!Objects.isNull(apmSpaceVo)) {
            errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - add space already exists！ foreignBid:" + foreignBid);
            return false;
        }

        ApmSpaceDto apmSpaceDto = convertToApmSpaceDto(message, foreignBid);

        try {
            //创建项目
            MObject project = addProject(message, foreignBid, apmSpaceDto);
            apmSpaceDto.setOriginBid(project.getBid());
            apmSpaceDto.setOriginModelCode(project.getModelCode());
            if (! apmSpaceApplicationService.saveApmSpace(apmSpaceDto)) {
                errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - add error: copy space failed！");
                return false;
            }

        } catch (Exception e) {
            errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - add error：" + e.getMessage());
            return false;
        }

        log.info("SpaceInstanceOperate - add end！ ");
        return true;
    }

    private MObject addProject(InstanceOperateMessage message, String foreignBid, ApmSpaceDto apmSpaceDto) {
        //检测项目是否存在
        MObject mObject = objectModelDomainService.getOneByProperty(productProjectModelCode, ObjectEnum.FOREIGN_BID.getCode(), foreignBid);
        if(mObject != null){
            log.info("SpaceInstanceOperate - addProject project already exists！ foreignBid:{}", foreignBid);
            return mObject;
        }
        message.getMObject().put(ObjectEnum.FOREIGN_BID.getCode(), foreignBid);
        message.getMObject().setBid(null);
        message.getMObject().setId(null);
        return appDataService.add(productProjectModelCode, message.getMObject());

    }

    @Override
    public boolean update(InstanceOperateMessage message) {
        log.debug("InstanceOperate - update start！ message：{}", JSON.toJSONString(message));

        if (!message.instanceOperateMessageParamCheck()) {
            return false;
        }
        String foreignBid = message.getMObject().get(DATA_BID).toString();

        ApmSpaceVo apmSpaceVo = apmSpaceApplicationService.getApmSpaceByForeignBid(foreignBid);
        if (Objects.isNull(apmSpaceVo)) {
            errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - update space does not exist！ foreignBid:" + foreignBid);
            return false;
        }

        ApmSpaceDto apmSpaceDto = convertToApmSpaceDto(message, foreignBid);
        apmSpaceDto.setBid(apmSpaceVo.getBid());
        apmSpaceDto.setSphereBid(apmSpaceVo.getSphereBid());
        apmSpaceDto.setTemplateFlag(apmSpaceVo.getTemplateFlag());
        apmSpaceDto.setTemplateBid(apmSpaceVo.getTemplateBid());

        try {
            if (CommonConst.DELETE_FLAG_DELETED.equals(message.getMObject().get(IS_DELETE))) {
                log.info("SpaceInstanceOperate - logicRemoveSpace！");

                if (! spaceInstanceOperateService.logicRemoveSpace(apmSpaceVo)) {
                    errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - logicRemoveSpace error！");
                    return false;
                }
                return true;
            }
            //更新项目信息
            MObject mObject = objectModelDomainService.getOneByProperty(productProjectModelCode, ObjectEnum.FOREIGN_BID.getCode(), foreignBid);
            if (Objects.isNull(mObject)) {
                errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - update project does not exist！ foreignBid:" + foreignBid);
                return false;
            }
            message.getMObject().put(BaseDataEnum.BID.getCode(), mObject.get(BaseDataEnum.BID.getCode()));
            message.getMObject().put(ObjectEnum.FOREIGN_BID.getCode(), mObject.get(ObjectEnum.FOREIGN_BID.getCode()));
            message.getMObject().put(VersionObjectEnum.DATA_BID.getCode(), mObject.get(VersionObjectEnum.DATA_BID.getCode()));
            appDataService.updateByBid(productProjectModelCode, mObject.getBid(), message.getMObject());
            if (! apmSpaceApplicationService.updateApmSpace(apmSpaceDto)) {
                errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - update error！");
                return false;
            }

        } catch (Exception e) {
            errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - update error: " + e.getMessage());
            return false;
        }

        log.info("InstanceOperate - update end");
        return true;
    }

    @Override
    public boolean delete(InstanceOperateMessage message) {
        log.debug("SpaceInstanceOperate - delete start！ message：{}", JSON.toJSONString(message));

        if (!message.instanceOperateMessageParamCheck()) {
            return false;
        }
        String foreignBid = message.getMObject().get(DATA_BID).toString();

        MObject mObject = objectModelDomainService.getOneByProperty(productProjectModelCode, ObjectEnum.FOREIGN_BID.getCode(), foreignBid);
        if (Objects.isNull(mObject)) {
            errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - delete project does not exist！ foreignBid:" + foreignBid);
            return false;
        }
        objectModelCrudI.deleteByBid(productProjectModelCode, mObject.getBid());

        ApmSpaceVo apmSpaceVo = apmSpaceApplicationService.getApmSpaceByForeignBid(foreignBid);
        if (Objects.isNull(apmSpaceVo)) {
            errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - delete space does not exist！ foreignBid:" + foreignBid);
            return false;
        }

        try {

            if (! spaceInstanceOperateService.physicsRemoveSpace(apmSpaceVo)) {
                errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - delete error! ");
                return false;
            }

        } catch (Exception e) {
            errorMessageHandling(message, MIDDLE.getCode(), "SpaceInstanceOperate - delete error:" + e.getMessage());
            return false;
        }

        log.info("SpaceInstanceOperate - delete end！");
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        InstanceOperateStrategyFactory.register("space", this);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean logicRemoveSpace(ApmSpaceVo apmSpaceVo) {
        ApmRoleQO apmRoleQo = new ApmRoleQO();
        apmRoleQo.setBizBid(apmSpaceVo.getBid());
        apmRoleQo.setBizType(SPACE.getCode());
        apmRoleQo.setSphereBid(apmSpaceVo.getSphereBid());
        List<ApmRoleVO> apmRoleVos = iApmRoleDomainService.list(apmRoleQo);
        if (CollectionUtils.isNotEmpty(apmRoleVos)) {
            List<String> roleBids = apmRoleVos.stream().map(ApmRoleVO::getBid).collect(Collectors.toList());
            ApmRoleDto apmRoleDto = new ApmRoleDto();
            apmRoleDto.setSphereBid(apmSpaceVo.getSphereBid());

            ApmRoleIdentityDto apmRoleIdentityDto = new ApmRoleIdentityDto();
            apmRoleIdentityDto.setRoleBids(roleBids);

            iApmRoleDomainService.remove(apmRoleDto);
            iApmRoleIdentityDomainService.remove(apmRoleIdentityDto);
        }
        return apmSpaceApplicationService.logicDeleteApmSpace(apmSpaceVo.getBid());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean physicsRemoveSpace(ApmSpaceVo apmSpaceVo) {
        ApmRoleQO apmRoleQo = new ApmRoleQO();
        apmRoleQo.setBizBid(apmSpaceVo.getBid());
        apmRoleQo.setBizType(SPACE.getCode());
        apmRoleQo.setSphereBid(apmSpaceVo.getSphereBid());
        List<ApmRoleVO> apmRoleVos = iApmRoleDomainService.list(apmRoleQo);
        if (CollectionUtils.isNotEmpty(apmRoleVos)) {
            List<String> roleBids = apmRoleVos.stream().map(ApmRoleVO::getBid).collect(Collectors.toList());
            ApmRoleDto apmRoleDto = new ApmRoleDto();
            apmRoleDto.setSphereBid(apmSpaceVo.getSphereBid());

            ApmRoleIdentityDto apmRoleIdentityDto = new ApmRoleIdentityDto();
            apmRoleIdentityDto.setRoleBids(roleBids);

            apmRoleService.physicsRemove(apmRoleDto);
            apmRoleIdentityService.physicsRemove(apmRoleIdentityDto);
        }
        return apmSpaceService.physicsRemoveByBid(apmSpaceVo.getBid());
    }

    private ApmSpaceDto convertToApmSpaceDto(InstanceOperateMessage message, String foreignBid) {
        ApmSpaceDto apmSpaceDto = new ApmSpaceDto();
        apmSpaceDto.setName(message.getMObject().getName());

        apmSpaceDto.setDescription(Objects.isNull(message.getMObject().get(DESCRIPTION)) ?
                StringUtils.EMPTY : message.getMObject().get(DESCRIPTION).toString());
//        apmSpaceDto.setIconUrl(Objects.isNull(message.getMObject().get(ICON_URL)) ?
//                StringUtils.EMPTY : message.getMObject().get(ICON_URL).toString());
//
//        apmSpaceDto.setIconUrl(spaceDefaultIconUrl);
        apmSpaceDto.setForeignBid(foreignBid);
        apmSpaceDto.setTemplateBid(foreignDefaultTemplateBid);
        return apmSpaceDto;
    }

    private void errorMessageHandling(InstanceOperateMessage message, String errLevel, String errMsg) {
        message.setErrInfo(errLevel, errMsg);
        log.error(errMsg);
    }
}
