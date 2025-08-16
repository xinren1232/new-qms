package com.transcend.plm.configcenter.object.infrastructure.extension;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.common.web.exception.ExceptionConstructor;
import com.transcend.plm.configcenter.draft.domain.service.CfgDraftDomainService;
import com.transcend.plm.configcenter.draft.pojo.dto.CfgDraftDto;
import com.transcend.plm.configcenter.draft.pojo.vo.CfgDraftVo;
import com.transcend.plm.configcenter.object.domain.entity.CfgObject;
import com.transcend.plm.configcenter.object.infrastructure.common.constant.ObjectModelConstants;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLifeCyclePo;
import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectPermissionSaveParam;
import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectViewRuleEditParam;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectPermissionVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectViewVO;
import com.transsion.framework.common.BeanUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 远程调用扩展服务 - 统一入参出参处理 && 日志处理
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/09 09:54
 */
@Slf4j
@Service
public class ExtensionService {

    @Resource
    private CfgDraftDomainService draftDomainService;
//
//    @Resource
//    private AuthObjFeignRemoteService authObjFeignRemoteService;
//
//    @Resource
//    private ViewConfigFeignService viewConfigFeignService;
//
//    @Resource
//    private LifeCycleStatusFeignService lifeCycleStatusFeignService;
//
//    @Resource
//    private LcTempFeginService lcTempFeginService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    public Boolean staging(CfgObject cfgObject) {
        CfgDraftDto draftData = new CfgDraftDto();
        String bid = cfgObject.getBid();
        String jobNumber = SsoHelper.getJobNumber();
        String draftCode = getObjectDraftCode(bid, jobNumber);
        draftData.setBizCode(draftCode);
        draftData.setCategory(getDraftObjectCategory());
        CfgObjectVo objectModelVO = BeanUtil.copy(cfgObject, CfgObjectVo.class);
        try {
            draftData.setContent(objectMapper.writeValueAsString(objectModelVO));
            draftDomainService.saveOrReplace(draftData);
        } catch (JsonProcessingException e) {
            log.error(ObjectModelConstants.JSON_PARSE_ERROR, e);
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), ObjectModelConstants.JSON_PARSE_ERROR);
        }
        return true;
    }

    @NotNull
    private String getDraftObjectCategory() {
        return "OBJECT";
    }

    public CfgObjectVo readDraft(String bid) {
        String jobNumber = SsoHelper.getJobNumber();
        SsoHelper.getJobNumber();
        String draftCode = getObjectDraftCode(bid, jobNumber);
        CfgDraftVo cfgDraftVo = draftDomainService.getByCategoryAndBizCode(getDraftObjectCategory(), draftCode);
        log.info("读取草稿入参：【bid：{}；jobNumber：{}】；返回结果：【{}】",
                bid, jobNumber, cfgDraftVo);
        if (cfgDraftVo == null ){
            return null;
        }
        CfgObjectVo objectModelVO;
        try {
            objectModelVO = objectMapper.readValue(cfgDraftVo.getContent(), CfgObjectVo.class);
        } catch (IOException e) {
            log.error(ObjectModelConstants.JSON_PARSE_ERROR, e);
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), ObjectModelConstants.JSON_PARSE_ERROR);
        }
        return objectModelVO;
    }

    public Boolean deleteDraftData(String modelCode) {
        String jobNumber = SsoHelper.getJobNumber();
        Boolean result = draftDomainService.logicalDeleteByCategoryAndBizCode(getDraftObjectCategory(), getObjectDraftCode(modelCode, jobNumber));
        log.info("删除草稿入参：【modelCode：{}；jobNumber：{}】；返回结果：【{}】",
                modelCode, jobNumber, JSON.toJSONString(result));
        return result;
    }

    @NotNull
    private String getObjectDraftCode(String modelCode, String jobNumber) {
        return modelCode + ":" + jobNumber;
    }

    public Boolean deleteDraftByList(List<String> modelCodeList) {
        return true;
//        Boolean result = draftFeignService.deleteDraftByList(modelCodeList);
//        log.info("删除多对象草稿入参：【modelCodeList：{}】；返回结果：【{}】",
//                JSON.toJSONString(modelCodeList), JSON.toJSONString(result));
//        return CheckResponseUtils.getResult(result);
    }

    public List<CfgObjectPermissionVo> findAuthInModelCode(String modelCode) {
//        List<AuthObjRoleVO>> result = authObjFeignRemoteService.findInModelCode(modelCode);
//        log.info("查询权限入参：【modelCode：{}】；返回结果：【{}】",
//                modelCode, result.getMessage());
//        return CheckResponseUtils.getResult(result);
        return null;
    }

    public CfgObjectPermissionVo saveObjectAuth(CfgObjectPermissionSaveParam cfgObjectPermissionSaveParam) {
//        AuthObjRoleVO> result = authObjFeignRemoteService.save(objectAuthSaveParam);
//        log.info("保存权限入参：【objectAuthSaveParam：{}】；返回结果：【{}】",
//                JSON.toJSONString(objectAuthSaveParam), result.getMessage());
//        return CheckResponseUtils.getResult(result);
        return null;
    }

    public Boolean deleteObjectPermission(String bid) {
//        Boolean result = authObjFeignRemoteService.delete(objectAuthDeleteParam);
//        log.info("删除权限入参：【objectAuthDeleteParam：{}】；返回结果：【{}】",
//                JSON.toJSONString(objectAuthDeleteParam), result.getMessage());
//        return CheckResponseUtils.getResult(result);
        return true;
    }

    public Boolean deleteAuthByList(List<String> modelCodeList) {
//        Boolean result = authObjFeignRemoteService.deleteAuthByList(modelCodeList);
//        log.info("删除多对象的所有权限入参：【modelCodeList：{}】；返回结果：【{}】",
//                JSON.toJSONString(modelCodeList), result.getMessage());
//        return CheckResponseUtils.getResult(result);
        return true;
    }

    public List<ObjectViewVO> findViewByModelCode(String modelCode) {
//        List<ViewConfigVO>> result = viewConfigFeignService.findByViewConfigCondition(
//                ViewConfigSearchVO.of().setModelCode(modelCode).setenableFlag(ObjectModelConstants.ENABLE)
//                        .setDeleted(Boolean.FALSE)
//        );
//        log.info("查询视图入参：【modelCode：{}】；返回结果：【{}】",
//                modelCode, result.getMessage());
//        return BeanUtil.copy(CheckResponseUtils.getResult(result), ObjectViewVO.class);
        return null;
    }

    public Boolean editObjectView(CfgObjectViewRuleEditParam param) {
//        ViewConfigDTO viewConfigDTO = BeanUtil.copy(param, ViewConfigDTO.class);
//        Boolean result = viewConfigFeignService.bulkEditViewConfig(Lists.newArrayList(viewConfigDTO));
//        log.info("编辑视图入参：【viewConfigDTO：{}】；返回结果：【{}】",
//                JSON.toJSONString(viewConfigDTO), result.getMessage());
//        return CheckResponseUtils.getResult(result);
        return true;
    }

    public Boolean deleteViewByList(List<String> modelCodeList) {
//        List<ViewConfigVO> voList = CheckResponseUtils.getResult(
//                viewConfigFeignService.findViewWithoutAttrByModelCodeList(modelCodeList));
//        List<String> viewBidList = voList.stream().map(ViewConfigVO::getBid).collect(Collectors.toList());
//        Boolean result = viewConfigFeignService.deleteObjectView(viewBidList);
//        log.info("删除视图入参：【modelCodeList：{}】；需要删除的视图bid :【{}】；返回结果：【{}】",
//                JSON.toJSONString(modelCodeList), JSON.toJSONString(viewBidList), result.getMessage());
//        return CheckResponseUtils.getResult(result);
        return true;
    }

    public String findLifeCycleStatusNameByCode(String initState) {
//        Map<String, String>> result = lifeCycleStatusFeignService.getNameByCode(initState);
//        log.info("查询生命周期状态名称入参：【initState：{}】；返回结果：【{}】",
//                initState, JSON.toJSONString(result));
//        return Optional.ofNullable(CheckResponseUtils.getResult(result).get(initState)).orElse("");
        return "";
    }

    public String findLifeCycleTemplateName(CfgObjectLifeCyclePo po) {
//        LifeCycTemplateSearchVO searchVO = new LifeCycTemplateSearchVO();
//        String lcTemplBid = po.getLcTemplBid();
//        String lcTemplVersion = po.getLcTemplVersion();
//        searchVO.setTemplateBid(lcTemplBid);
//        searchVO.setVersion(lcTemplVersion);
//        LifeCycleTemplateVO> result = lcTempFeginService.getOne(searchVO);
//        String name = Optional.ofNullable(CheckResponseUtils.getResult(result).getName()).orElse("");
//        log.info("查询生命周期入参：【LcTemplBid：{}，lcTemplVersion：{}】；返回结果：【{}】；返回数据：【{}】",
//                lcTemplBid, lcTemplVersion, result.getMessage(), name);
//        return name;
        return null;
    }

}
