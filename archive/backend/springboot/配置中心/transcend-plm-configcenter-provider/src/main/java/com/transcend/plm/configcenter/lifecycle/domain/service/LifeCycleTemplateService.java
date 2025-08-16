package com.transcend.plm.configcenter.lifecycle.domain.service;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.CfgLifeCycleTemplateVersionConverter;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateNodeQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVersionVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.CfgLifeCycleTemplateRepository;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.*;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service.CfgLifeCycleStateService;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectLifecycleAppService;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLifeCyclePo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectLifeCycleRepository;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LifeCycleTemplateService {
    @Resource
    private CfgLifeCycleTemplateRepository cfgLifeCycleTemplateRepository;
    @Resource
    private CfgObjectLifeCycleRepository cfgObjectLifeCycleRepository;

    @Resource
    private ICfgObjectLifecycleAppService lifecycleAppService;
    @Resource
    private CfgLifeCycleStateService cfgLifeCycleStateService;
    /**
     * 新增是第一个版本号
     */
    private static final String FIRST_VERSION = "V1";

    /**
     * 未启用
     */
    private static final Integer OFF = 0;

    private static final String NOT_ALLOW_USE = "0";

    public PagedResult<CfgLifeCycleTemplateVo> page(BaseRequest<CfgLifeCycleTemplateQo> pageQo) {
        return cfgLifeCycleTemplateRepository.pageByCfgLifeCycleTemplateQo(pageQo);
    }

    @Transactional
    public CfgLifeCycleTemplateVo edit(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        //编辑模板
        Assert.notNull(cfgLifeCycleTemplateDto, "lifeCycleTemplate is null");
        Assert.hasText(cfgLifeCycleTemplateDto.getBid(), "lifeCycleTemplate bid is blank");
        CfgLifeCycleTemplateVersionPo cfgLifeCycleTemplateVersion = CfgLifeCycleTemplateVersionConverter.INSTANCE.dto2po(cfgLifeCycleTemplateDto);
        cfgLifeCycleTemplateVersion.setStateCode(NOT_ALLOW_USE);
        cfgLifeCycleTemplateVersion.setTemplateBid(cfgLifeCycleTemplateDto.getBid());
        cfgLifeCycleTemplateVersion.setBid(SnowflakeIdWorker.nextIdStr());
        cfgLifeCycleTemplateVersion.setName(cfgLifeCycleTemplateDto.getName());
        long count = cfgLifeCycleTemplateRepository.countByTemplateBid(cfgLifeCycleTemplateDto.getBid());
        //编辑后的数据需要升版
        String version = String.format("V%s", (count + 1));
        cfgLifeCycleTemplateVersion.setVersion(version);
        //保存模板相关的节点，线，版本，关系等明细数据（编辑和保存通用）
        saveOtherData(cfgLifeCycleTemplateDto, cfgLifeCycleTemplateVersion);
        return cfgLifeCycleTemplateRepository.updateDescription(cfgLifeCycleTemplateDto);
    }

    public CfgLifeCycleTemplateVersionPo getCfgLifeCycleTemplateVersion(String templateBid, String version){
        return cfgLifeCycleTemplateRepository.getCfgLifeCycleTemplateVersion(templateBid, version);
    }

    public void saveOtherData(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto,
        CfgLifeCycleTemplateVersionPo cfgLifeCycleTemplateVersion) {
        //模板修改，其他下面的节点明细数据随着版本变动。该方法用于保存模板下的节点明细数据，新增，编辑均适用
        List<JSONObject> layouts = cfgLifeCycleTemplateDto.getLayouts();
        if (!CollectionUtils.isEmpty(layouts)) {
            List<CfgLifeCycleTemplateTransitionLinePo> lifeCycleTemplateTransitionLines = new ArrayList<CfgLifeCycleTemplateTransitionLinePo>();
            List<CfgLifeCycleTemplateNodePo> cfgLifeCycleTemplateNodes = new ArrayList<>();
            List<CfgLifeCycleTemplateObjRelPo> cfgLifeCycleTemplateObjRelList = new ArrayList<>();
            // 解析布局数据
            for (JSONObject jsonObject : layouts) {
                String shape = (String) jsonObject.get("shape");
                String id = (String) jsonObject.get("id");
                // 业务数据map
                Map<String, Object> data = (Map<String, Object>) jsonObject.get("data");
                if ("edge".endsWith(shape)) {
                    // 线
                    CfgLifeCycleTemplateTransitionLinePo cfgLifeCycleTemplateTransitionLine = new CfgLifeCycleTemplateTransitionLinePo();
                    cfgLifeCycleTemplateTransitionLine.setBid(id);
                    cfgLifeCycleTemplateTransitionLine.setTemplateBid(cfgLifeCycleTemplateDto.getBid());
                    cfgLifeCycleTemplateTransitionLine.setTemplateVersion(cfgLifeCycleTemplateVersion.getVersion());
                    Map<String, Object> source = (Map<String, Object>) jsonObject.get("source");
                    Map<String, Object> target = (Map<String, Object>) jsonObject.get("target");
                    cfgLifeCycleTemplateTransitionLine.setSource(source.get("cell") + "");
                    cfgLifeCycleTemplateTransitionLine.setTarget(target.get("cell") + "");
                    cfgLifeCycleTemplateTransitionLine.setLayout(JSON.toJSONString(jsonObject));
                    if (!CollectionUtils.isEmpty(data)) {
                        if (data.get("roleBid") != null) {
                            cfgLifeCycleTemplateTransitionLine.setRoleBid(data.get("roleBid") + "");
                        }
                        if (data.get("description") != null) {
                            cfgLifeCycleTemplateTransitionLine.setDescription(data.get("description") + "");
                        }
                        if (data.get("afterMethod") != null) {
                            cfgLifeCycleTemplateTransitionLine.setAfterMethod(data.get("afterMethod") + "");
                        }
                        if (data.get("beforeMethod") != null) {
                            cfgLifeCycleTemplateTransitionLine.setBeforeMethod(data.get("beforeMethod") + "");
                        }
                    }
                    lifeCycleTemplateTransitionLines.add(cfgLifeCycleTemplateTransitionLine);
                }
                else if ("rect".endsWith(shape)) {
                    // 节点
                    CfgLifeCycleTemplateNodePo cfgLifeCycleTemplateNode = new CfgLifeCycleTemplateNodePo();
                    cfgLifeCycleTemplateNode.setBid(id);
                    cfgLifeCycleTemplateNode.setTemplateBid(cfgLifeCycleTemplateDto.getBid());
                    cfgLifeCycleTemplateNode.setVersion(cfgLifeCycleTemplateVersion.getVersion());
                    if (!CollectionUtils.isEmpty(data)) {
                        if (data.get("code") != null) {
                            cfgLifeCycleTemplateNode.setLifeCycleCode(data.get("code") + "");
                        }
                        if (data.get("description") != null) {
                            cfgLifeCycleTemplateNode.setDescription(data.get("description") + "");
                        }
                        if (data.get("name") != null) {
                            cfgLifeCycleTemplateNode.setName(data.get("name") + "");
                        }
                        if (data.get("groupCode") != null) {
                            cfgLifeCycleTemplateNode.setGroupCode(data.get("groupCode") + "");
                        }
                        if (data.get("avatar") != null) {
                            cfgLifeCycleTemplateNode.setAvatar(data.get("avatar") + "");
                        }
                        if (data.get("flag") != null) {
                            cfgLifeCycleTemplateNode.setFlag(data.get("flag") + "");
                        }
                        if (data.get("behaviorScope") != null) {
                            cfgLifeCycleTemplateNode.setBehaviorScope(Integer.parseInt(data.get("behaviorScope")+""));
                        }
                        if (data.get("behavior") != null) {
                            cfgLifeCycleTemplateNode.setBehavior(data.get("behavior") + "");
                        }
                        if (data.get("bindProcess") != null) {
                            cfgLifeCycleTemplateNode.setBindProcess(data.get("bindProcess") + "");
                        }
                        if (data.get("sort") != null) {
                            cfgLifeCycleTemplateNode.setSort(Integer.parseInt(data.get("sort")+""));
                        }
                        if (data.get("keyPathFlag") != null) {
                            Boolean keyPathFlag = BooleanUtil.toBooleanObject(data.get("keyPathFlag")+"");
                            cfgLifeCycleTemplateNode.setKeyPathFlag(BooleanUtil.toInteger(keyPathFlag));
                        }
                    }
                    cfgLifeCycleTemplateNode.setLayout(JSON.toJSONString(jsonObject));
                    cfgLifeCycleTemplateNodes.add(cfgLifeCycleTemplateNode);
                    if (data.get("lifeCycTemObjRelList") != null) {
                        List<Map<String, Object>> lifeCycleTemplateObjRels = (List<Map<String, Object>>) data
                            .get("lifeCycTemObjRelList");
                        if (!CollectionUtils.isEmpty(lifeCycleTemplateObjRels)) {
                            for (Map<String, Object> map : lifeCycleTemplateObjRels) {
                                CfgLifeCycleTemplateObjRelPo cfgLifeCycleTemplateObjRel = new CfgLifeCycleTemplateObjRelPo();
                                cfgLifeCycleTemplateObjRel.setTemplateBid(cfgLifeCycleTemplateDto.getBid());
                                cfgLifeCycleTemplateObjRel.setTemplateVersion(cfgLifeCycleTemplateVersion.getVersion());
                                cfgLifeCycleTemplateObjRel.setLifeCycleCode(data.get("lifeCycleCode") + "");
                                cfgLifeCycleTemplateObjRel.setBid(SnowflakeIdWorker.nextIdStr());
                                if (map.get("targetModelCode") != null) {
                                    cfgLifeCycleTemplateObjRel.setTargetModelCode(map.get("targetModelCode") + "");
                                }
                                if (map.get("targetObjBid") != null) {
                                    cfgLifeCycleTemplateObjRel.setTargetObjBid(map.get("targetObjBid") + "");
                                }
                                if (map.get("targetObjName") != null) {
                                    cfgLifeCycleTemplateObjRel.setTargetObjName(map.get("targetObjName") + "");
                                }
                                if (map.get("targetObjRel") != null) {
                                    cfgLifeCycleTemplateObjRel.setTargetObjRel(map.get("targetObjRel") + "");
                                }
                                cfgLifeCycleTemplateObjRel.setDeleteFlag(0);
                                cfgLifeCycleTemplateObjRelList.add(cfgLifeCycleTemplateObjRel);
                            }
                        }
                    }
                }
            }
            cfgLifeCycleTemplateRepository.saveCfgLifeCycleTemplateTransitionLine(lifeCycleTemplateTransitionLines);
            cfgLifeCycleTemplateRepository.saveCfgLifeCycleTemplateNodes(cfgLifeCycleTemplateNodes);
            cfgLifeCycleTemplateRepository.saveCfgLifeCycleTemplateObjRel(cfgLifeCycleTemplateObjRelList);
            cfgLifeCycleTemplateVersion.setId(null);
            cfgLifeCycleTemplateRepository.saveCfgLifeCycleTemplateVersion(cfgLifeCycleTemplateVersion);
        }
    }

    @Transactional
    public CfgLifeCycleTemplateVo add(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        //新增模板
        cfgLifeCycleTemplateDto.setBid(SnowflakeIdWorker.nextIdStr());
        cfgLifeCycleTemplateDto.setCurrentVersion(FIRST_VERSION);
        cfgLifeCycleTemplateDto.setEnableFlag(OFF);
        CfgLifeCycleTemplateVersionPo cfgLifeCycleTemplateVersion = CfgLifeCycleTemplateVersionConverter.INSTANCE.dto2po(cfgLifeCycleTemplateDto);
        cfgLifeCycleTemplateVersion.setStateCode(NOT_ALLOW_USE);
        cfgLifeCycleTemplateVersion.setTemplateBid(cfgLifeCycleTemplateDto.getBid());
        cfgLifeCycleTemplateVersion.setVersion(cfgLifeCycleTemplateDto.getCurrentVersion());
        cfgLifeCycleTemplateVersion.setName(cfgLifeCycleTemplateDto.getName());
        //保存模板相关的节点，线，版本，关系等明细数据（编辑和保存通用）
        saveOtherData(cfgLifeCycleTemplateDto, cfgLifeCycleTemplateVersion);
        return cfgLifeCycleTemplateRepository.saveCfgLifeCycleTemplate(cfgLifeCycleTemplateDto);
    }

    public boolean setEnable(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto){
       return cfgLifeCycleTemplateRepository.setEnable(cfgLifeCycleTemplateDto);
    }

    @CacheEvict(value = CacheNameConstant.LIFECYCLE_TEMPLATE, key = "#cfgLifeCycleTemplateDto.bid")
    public boolean setVersion(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        return cfgLifeCycleTemplateRepository.setVersion(cfgLifeCycleTemplateDto);
    }

    public List<CfgLifeCycleTemplateVersionVo> getVersions(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        List<CfgLifeCycleTemplateVersionVo> list = cfgLifeCycleTemplateRepository.getVersions(cfgLifeCycleTemplateDto);
        // 设置当前使用版本
        for (CfgLifeCycleTemplateVersionVo cfgLifeCycleTemplateVersionVo : list) {
            if (cfgLifeCycleTemplateDto.getCurrentVersion().equals(cfgLifeCycleTemplateVersionVo.getVersion())) {
                cfgLifeCycleTemplateVersionVo.setStateCode("1");
                break;
            }
        }
        return list;
    }

    @CacheEvict(value = CacheNameConstant.LIFECYCLE_TEMPLATE, key = "#bid")
    public boolean deleteTemplate(String bid) {
        return cfgLifeCycleTemplateRepository.deleteTemplate(bid);
    }

    public List<CfgLifeCycleTemplateNodePo> getNextTemplateNodes(TemplateDto templateDto) {
        CfgLifeCycleTemplateNodeQo cfgLifeCycleTemplateNodeQo = new CfgLifeCycleTemplateNodeQo();
        cfgLifeCycleTemplateNodeQo.setTemplateBid(templateDto.getTemplateBid());
        cfgLifeCycleTemplateNodeQo.setVersion(templateDto.getVersion());
        cfgLifeCycleTemplateNodeQo.setLifeCycleCode(templateDto.getCurrentLifeCycleCode());
        CfgLifeCycleTemplateNodePo cfgLifeCycleTemplateNodePo = cfgLifeCycleTemplateRepository.getCfgLifeCycleTemplateNode(cfgLifeCycleTemplateNodeQo);
        List<CfgLifeCycleTemplateNodePo> resList = new ArrayList<>();
        if (cfgLifeCycleTemplateNodePo != null) {
            resList.add(cfgLifeCycleTemplateNodePo);
            templateDto.setSourceBid(cfgLifeCycleTemplateNodePo.getBid());
            List<CfgLifeCycleTemplateTransitionLinePo> cfgLifeCycleTemplateTransitionLinePos = cfgLifeCycleTemplateRepository.getCfgLifeCycleTemplateTransitionLine(templateDto);
            if(!CollectionUtils.isEmpty(cfgLifeCycleTemplateTransitionLinePos)){
                List<String> targetBids = cfgLifeCycleTemplateTransitionLinePos.stream().map(CfgLifeCycleTemplateTransitionLinePo::getTarget).collect(Collectors.toList());
                List<CfgLifeCycleTemplateNodePo> cfgLifeCycleTemplateNodePos = cfgLifeCycleTemplateRepository.getCfgLifeCycleTemplateNodeByBids(targetBids,templateDto.getTemplateBid(),templateDto.getVersion());
                resList.addAll(cfgLifeCycleTemplateNodePos);
            }
        }
        return resList;
    }

    public List<CfgLifeCycleTemplateNodePo> getTemplateNodes(TemplateDto templateDto) {
        ObjectModelLifeCycleVO objectModelLifeCycleVO = lifecycleAppService.findObjectLifecycleByModelCode(templateDto.getModelCode());
        if(objectModelLifeCycleVO == null){
            return null;
        }
        templateDto.setTemplateBid(objectModelLifeCycleVO.getLcTemplBid());
        if(objectModelLifeCycleVO.getLcTemplVersion().startsWith("V")){
            templateDto.setVersion(objectModelLifeCycleVO.getLcTemplVersion());
        }else{
            templateDto.setVersion("V"+objectModelLifeCycleVO.getLcTemplVersion());
        }
        List<CfgLifeCycleTemplateNodePo> cfgLifeCycleTemplateNodes = cfgLifeCycleTemplateRepository
                .getCfgLifeCycleTemplateNode(templateDto);
        return cfgLifeCycleTemplateNodes;
    }

    public CfgLifeCycleTemplateVo getCfgLifeCycleTemplateVo(TemplateDto templateDto) {
        if(StringUtil.isNotBlank(templateDto.getModelCode()) && (StringUtil.isBlank(templateDto.getTemplateBid()) || StringUtil.isBlank(templateDto.getVersion()))){
            //查询对象绑定的生命周期模板
            CfgObjectLifeCyclePo cfgObjectLifeCyclePo = cfgObjectLifeCycleRepository.find(templateDto.getModelCode());
            if(cfgObjectLifeCyclePo == null){
                return null;
            }
            templateDto.setTemplateBid(cfgObjectLifeCyclePo.getLcTemplBid());
            if(cfgObjectLifeCyclePo.getLcTemplVersion().startsWith("V")){
                templateDto.setVersion(cfgObjectLifeCyclePo.getLcTemplVersion());
            }else{
                templateDto.setVersion("V"+cfgObjectLifeCyclePo.getLcTemplVersion());
            }
        }
        CfgLifeCycleTemplateVersionPo cfgLifeCycleTemplateVersion =getCfgLifeCycleTemplateVersion(templateDto.getTemplateBid(),templateDto.getVersion());
        if(cfgLifeCycleTemplateVersion == null){
            return null;
        }
        CfgLifeCycleTemplateVo cfgLifeCycleTemplateVo = new CfgLifeCycleTemplateVo();
        cfgLifeCycleTemplateVo.setBid(cfgLifeCycleTemplateVersion.getTemplateBid());
        cfgLifeCycleTemplateVo.setVersion(cfgLifeCycleTemplateVersion.getVersion());
        cfgLifeCycleTemplateVo.setName(cfgLifeCycleTemplateVersion.getName());
        cfgLifeCycleTemplateVo.setDescription(cfgLifeCycleTemplateVersion.getDescription());
        CfgLifeCycleTemplateVo byBid = cfgLifeCycleTemplateRepository.getByBid(templateDto.getTemplateBid());
        cfgLifeCycleTemplateVo.setPhaseState(byBid.getPhaseState());
        List<CfgLifeCycleTemplateNodePo> cfgLifeCycleTemplateNodes = cfgLifeCycleTemplateRepository
            .getCfgLifeCycleTemplateNode(templateDto);
        List<CfgLifeCycleTemplateTransitionLinePo> cfgLifeCycleTemplateTransitionLines = cfgLifeCycleTemplateRepository
            .getCfgLifeCycleTemplateTransitionLine(templateDto);
        List<JSONObject> layouts = new ArrayList<>();
        for(CfgLifeCycleTemplateTransitionLinePo cfgLifeCycleTemplateTransitionLine:cfgLifeCycleTemplateTransitionLines){
            String layout = cfgLifeCycleTemplateTransitionLine.getLayout();
            if(StringUtil.isNotBlank(layout)){
                JSONObject jsonObject = (JSONObject) JSONObject.parse(layout);
                layouts.add(jsonObject);
            }
        }
        List<String> lifeCycleNodes = new ArrayList<>();
        for(CfgLifeCycleTemplateNodePo cfgLifeCycleTemplateNode:cfgLifeCycleTemplateNodes){
            if(StringUtils.isNotEmpty(cfgLifeCycleTemplateNode.getLifeCycleCode())){
                lifeCycleNodes.add(cfgLifeCycleTemplateNode.getLifeCycleCode());
            }
        }
        Map<String,String> colorMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(lifeCycleNodes)){
            List<CfgLifeCycleStatePo> cfgLifeCycleStatePos = cfgLifeCycleStateService.listByCodes(lifeCycleNodes);
            for(CfgLifeCycleStatePo cfgLifeCycleStatePo:cfgLifeCycleStatePos){
                colorMap.put(cfgLifeCycleStatePo.getCode(),cfgLifeCycleStatePo.getColor());
            }
        }
        for(CfgLifeCycleTemplateNodePo cfgLifeCycleTemplateNode:cfgLifeCycleTemplateNodes){
            String layout = cfgLifeCycleTemplateNode.getLayout();
            String color = colorMap.get(cfgLifeCycleTemplateNode.getLifeCycleCode());
            if(StringUtil.isNotBlank(layout)){
                JSONObject jsonObject = (JSONObject) JSONObject.parse(layout);
                if(jsonObject.get("data") != null){
                   JSONObject map = (JSONObject) jsonObject.get("data");
                    map.put("color",color);
                    jsonObject.put("data",map);
                }
                layouts.add(jsonObject);
            }
        }
        //前端用原始的json数据展示明细
        cfgLifeCycleTemplateVo.setLayouts(layouts);
        return cfgLifeCycleTemplateVo;
    }

    /**
     * 获取模板节点，且按照连线排序
     */
    @NotNull
    @Cacheable(value = CacheNameConstant.LIFECYCLE_TEMPLATE, key = "#templateDto.templateBid")
    public List<CfgLifeCycleTemplateNodePo> getCfgLifeCycleTemplateNodePos(TemplateDto templateDto) {

        List<CfgLifeCycleTemplateNodePo> cfgLifeCycleTemplateNodes = cfgLifeCycleTemplateRepository
                .getCfgLifeCycleTemplateNode(templateDto);
        List<CfgLifeCycleTemplateTransitionLinePo> cfgLifeCycleTemplateTransitionLines = cfgLifeCycleTemplateRepository
                .getCfgLifeCycleTemplateTransitionLine(templateDto);
        Map<String, CfgLifeCycleTemplateNodePo> lifecycleNodeMap = cfgLifeCycleTemplateNodes.stream().collect(Collectors.toMap(CfgLifeCycleTemplateNodePo::getBid, a -> a, (k1, k2) -> k1));
        Map<String,String> linePreMap = new HashMap<>();
        Map<String,String> lineNextMap = new HashMap<>();
        cfgLifeCycleTemplateTransitionLines.forEach(cfgLifeCycleTemplateTransitionLinePo -> {
            linePreMap.put(cfgLifeCycleTemplateTransitionLinePo.getTarget(),cfgLifeCycleTemplateTransitionLinePo.getSource());
            lineNextMap.put(cfgLifeCycleTemplateTransitionLinePo.getSource(),cfgLifeCycleTemplateTransitionLinePo.getTarget());
        });
        //确定头结点
        CfgLifeCycleTemplateNodePo head = null;
        for (CfgLifeCycleTemplateNodePo cfgLifeCycleTemplateNode : cfgLifeCycleTemplateNodes) {
            if(!linePreMap.containsKey(cfgLifeCycleTemplateNode.getBid())){
                head = cfgLifeCycleTemplateNode;
                break;
            }
        }
        if (head == null) {
            log.error("生命周期模板数据异常，没有头结点,modelCode={}",templateDto.getModelCode());
            return cfgLifeCycleTemplateNodes;
        }
        List<CfgLifeCycleTemplateNodePo> resList = new ArrayList<>();
        resList.add(head);
        Set<String> sortedNodeBids = Sets.newHashSet();
        sortedNodeBids.add(head.getBid());
        while(lineNextMap.containsKey(head.getBid())){
            head = lifecycleNodeMap.get(lineNextMap.get(head.getBid()));
            if(sortedNodeBids.contains(head.getBid())){
                log.error("生命周期模板数据异常，存在环路,modelCode={}",templateDto.getModelCode());
                return cfgLifeCycleTemplateNodes;
            }
            sortedNodeBids.add(head.getBid());
            resList.add(head);
        }
        if(resList.size() != cfgLifeCycleTemplateNodes.size()){
            log.error("生命周期模板数据异常，存在孤立节点,modelCode={}",templateDto.getModelCode());
            return cfgLifeCycleTemplateNodes;
        }
        return resList;
    }

    public List<CfgLifeCycleTemplateNodePo> getKeyPathNodes(TemplateDto templateDto) {
        List<CfgLifeCycleTemplateNodePo> cfgLifeCycleTemplateNodes = cfgLifeCycleTemplateRepository
                .getCfgLifeCycleTemplateNode(templateDto);
        List<CfgLifeCycleTemplateTransitionLinePo> cfgLifeCycleTemplateTransitionLines = cfgLifeCycleTemplateRepository
                .getCfgLifeCycleTemplateTransitionLine(templateDto);
        List<CfgLifeCycleTemplateNodePo> keyNodes = cfgLifeCycleTemplateNodes.stream().filter(v -> 1 == v.getKeyPathFlag()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(keyNodes)) {
            return Collections.emptyList();
        }
        Set<String> keyNodeCodes = keyNodes.stream().map(CfgLifeCycleTemplateNodePo::getBid).collect(Collectors.toSet());
        List<CfgLifeCycleTemplateTransitionLinePo> keyLine = cfgLifeCycleTemplateTransitionLines.stream().filter(v -> keyNodeCodes.contains(v.getSource()) && keyNodeCodes.contains(v.getTarget())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(keyLine)) {
            return Collections.emptyList();
        }
        Map<String, CfgLifeCycleTemplateNodePo> keyNodeMap = keyNodes.stream().collect(Collectors.toMap(CfgLifeCycleTemplateNodePo::getBid, a -> a, (k1, k2) -> k1));
        Map<String,String> linePreMap = new HashMap<>();
        Map<String,String> lineNextMap = new HashMap<>();
        keyLine.forEach(cfgLifeCycleTemplateTransitionLinePo -> {
            linePreMap.put(cfgLifeCycleTemplateTransitionLinePo.getTarget(),cfgLifeCycleTemplateTransitionLinePo.getSource());
            lineNextMap.put(cfgLifeCycleTemplateTransitionLinePo.getSource(),cfgLifeCycleTemplateTransitionLinePo.getTarget());
        });
        //确定头结点
        CfgLifeCycleTemplateNodePo head = null;
        for (CfgLifeCycleTemplateNodePo cfgLifeCycleTemplateNode : keyNodes) {
            if(!linePreMap.containsKey(cfgLifeCycleTemplateNode.getBid())){
                head = cfgLifeCycleTemplateNode;
                break;
            }
        }
        if (head == null) {
            log.error("阶段生命周期模板数据异常，没有头结点,modelCode={}",templateDto.getModelCode());
            return keyNodes;
        }
        List<CfgLifeCycleTemplateNodePo> resList = new ArrayList<>();
        resList.add(head);
        Set<String> sortedNodeBids = Sets.newHashSet();
        sortedNodeBids.add(head.getBid());
        String nextNodeBid  = head.getBid();
        for (int i = 0; i < keyNodes.size(); i++) {
            nextNodeBid = lineNextMap.get(nextNodeBid);
            if (nextNodeBid == null || sortedNodeBids.contains(nextNodeBid)) {
                break;
            }
            sortedNodeBids.add(nextNodeBid);
            resList.add(keyNodeMap.get(nextNodeBid));
        }
        if(resList.size() != keyNodes.size()){
            log.error("阶段生命周期模板数据异常，存在孤立节点后者闭环,modelCode={}",templateDto.getModelCode());
            return keyNodes;
        }
        return resList;
    }

    public CfgLifeCycleTemplateNodePo getKeyPathNode(TemplateDto templateDto) {
        List<CfgLifeCycleTemplateNodePo> cfgLifeCycleTemplateNodes = cfgLifeCycleTemplateRepository
                .getCfgLifeCycleTemplateNode(templateDto);
        String currentLifeCycleCode = templateDto.getCurrentLifeCycleCode();
        CfgLifeCycleTemplateNodePo currentNodePo = cfgLifeCycleTemplateNodes.stream().filter(v -> currentLifeCycleCode.equals(v.getLifeCycleCode())).findFirst().orElse(null);
        if (currentNodePo == null) {
            return null;
        }
        List<CfgLifeCycleTemplateTransitionLinePo> cfgLifeCycleTemplateTransitionLines = cfgLifeCycleTemplateRepository
                .getCfgLifeCycleTemplateTransitionLine(templateDto);
        List<CfgLifeCycleTemplateNodePo> keyNodes = cfgLifeCycleTemplateNodes.stream().filter(v -> 1 == v.getKeyPathFlag()).collect(Collectors.toList());
        Set<String> keyNodeBids = keyNodes.stream().map(CfgLifeCycleTemplateNodePo::getBid).collect(Collectors.toSet());
        if (keyNodeBids.contains(currentNodePo.getBid())) {
            return currentNodePo;
        }
        Map<String, Set<String>> lastNodeMap = cfgLifeCycleTemplateTransitionLines.stream().collect(Collectors.groupingBy(CfgLifeCycleTemplateTransitionLinePo::getTarget, Collectors.mapping(CfgLifeCycleTemplateTransitionLinePo::getSource, Collectors.toSet())));
        List<String> bids = Lists.newArrayList(currentNodePo.getBid());
        for (int i = 0; i < cfgLifeCycleTemplateNodes.size(); i++) {
            List<String> lastBids = new ArrayList<>();
            for (String bid : bids) {
                Set<String> bids2 = lastNodeMap.get(bid);
                if (bids2 != null) {
                    Collection<String> intersection = org.apache.commons.collections4.CollectionUtils.intersection(bids2, keyNodeBids);
                    if (CollectionUtils.isEmpty(intersection)) {
                        lastBids.addAll(bids2);
                    } else {
                        return cfgLifeCycleTemplateNodes.stream().filter(v -> intersection.contains(v.getBid())).findFirst().orElse(null);
                    }
                }
            }
            if (CollectionUtils.isEmpty(lastBids)) {
                return null;
            }
            bids = lastBids;
        }
        return null;
    }
}
