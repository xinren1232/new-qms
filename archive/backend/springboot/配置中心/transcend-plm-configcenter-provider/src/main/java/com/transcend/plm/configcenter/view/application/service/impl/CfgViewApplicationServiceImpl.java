package com.transcend.plm.configcenter.view.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.BaseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transcend.plm.configcenter.api.model.enums.ViewEnums;
import com.transcend.plm.configcenter.api.model.object.dto.CfgPropertyMatchDto;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectViewRuleVo;
import com.transcend.plm.configcenter.api.model.tabmanage.enums.ViewComponentEnum;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.qo.CfgViewQo;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.common.exception.PlmBizException;
import com.transcend.plm.configcenter.common.expression.SimpleExpression;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import com.transcend.plm.configcenter.object.domain.service.CfgObjectViewRuleDomainService;
import com.transcend.plm.configcenter.view.application.service.ICfgViewApplicationService;
import com.transcend.plm.configcenter.view.domain.service.CfgViewDomainService;
import com.transcend.plm.configcenter.view.infrastructure.repository.CfgViewService;
import com.transcend.plm.configcenter.view.infrastructure.repository.CfgViewSyncRecordService;
import com.transcend.plm.configcenter.view.infrastructure.repository.po.CfgViewPo;
import com.transcend.plm.configcenter.view.infrastructure.repository.po.CfgViewSyncRecordPo;
import com.transcend.plm.configcenter.view.pojo.CfgViewConverter;
import com.transcend.plm.configcenter.view.pojo.qo.SyncViewContentQo;
import com.transsion.framework.common.StringUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Program transcend-plm-configcenter
 *
 * @author peng.qin
 * @version 1.0
 * Date 2023-02-22 10:36
 **/
@Service
public class CfgViewApplicationServiceImpl implements ICfgViewApplicationService {
    @Resource
    private CfgViewDomainService cfgViewDomainService;

    @Resource
    private CfgObjectViewRuleDomainService cfgObjectViewRuleDomainService;

    @Resource
    private CfgViewSyncRecordService cfgViewSyncRecordService;

    @Resource
    private CfgViewService cfgViewService;


    /**
     * 保存或新增基础属性
     *
     * @param cfgViewDto 视图dto
     * @return 视图vo
     */
    @Override
    public CfgViewVo saveOrUpdate(CfgViewDto cfgViewDto) {
        Assert.notNull(cfgViewDto, "attribute is null");
        cfgViewDto.setUpdatedTime(new Date());
        return StringUtil.isBlank(cfgViewDto.getBid()) ? cfgViewDomainService.save(cfgViewDto) : cfgViewDomainService.update(cfgViewDto);
    }

    /**
     * 保存或新增基础属性(判断bid是否存在，存在则更新，不存在则新增
     *
     * @param bid        bid
     * @param cfgViewDto 视图dto
     * @return 视图vo
     */
    @Override
    @CacheEvict(value = CacheNameConstant.OBJECT_VIEW, key = "#bid")
    public CfgViewVo saveOrUpdate(String bid, CfgViewDto cfgViewDto) {
        // 判断bid是否存在，存在则更新，不存在则新增
        boolean exist = cfgViewDomainService.existByBid(bid);
        return exist ? cfgViewDomainService.update(cfgViewDto) : cfgViewDomainService.save(cfgViewDto);
    }

    @Override
    public boolean copyViews(CfgViewDto cfgViewDto) {
        return cfgViewDomainService.copyViews(cfgViewDto);
    }

    @Override
    @CacheEvict(value = CacheNameConstant.OBJECT_VIEW, key = "#bid")
    public Boolean updatePartialContent(String bid, Map<String, Object> partialContent) {
        return Boolean.TRUE;
    }

    @Override
    public CfgViewVo getView(ViewQueryParam param) {
        if (CharSequenceUtil.isBlank(param.getViewBelongBid())) {
            throw new PlmBizException("视图所属bid不能为空");
        }
        // 构造请求参数对象
        CfgViewQo qo = new CfgViewQo()
                .setBelongBid(param.getViewBelongBid())
                .setViewScope(param.getViewScope())
                .setViewType(param.getViewType());
        Map<String, List<CfgViewVo>> resultMap = cfgViewDomainService.listByConditions(qo);
        // 作用域视图
        List<CfgViewVo> viewVoList = resultMap.get(param.getViewType());
        // 如果查出来作用域视图为空，则直接返回默认视图
        if (CollUtil.isEmpty(viewVoList)) {
            return getDefaultView(resultMap);
        }
        // 如果查出来的是多视图，需要根据权限对视图进行过滤，如果过滤后还是多视图，就根据优先级进行排序，返回优先级最高的视图
        // 先查询视图权限
        List<String> viewBids = viewVoList.stream().map(CfgViewVo::getBid).collect(Collectors.toList());
        // 根据视图bid查询视图权限
        List<CfgObjectViewRuleVo> viewRuleVoList = cfgObjectViewRuleDomainService.listByViewBids(viewBids);
        //使用规则中的优先级
        Map<String, Integer> viewPriorityMap = viewRuleVoList.stream().collect(Collectors.toMap(
                CfgObjectViewRuleVo::getViewBid, rule -> Optional.of(rule).map(CfgObjectViewRuleVo::getPriority)
                        .orElse(Integer.MAX_VALUE), (v1, v2) -> v1));

        // 获取需要拦截的视图Bid
        Set<String> interceptViewBidList = getInterceptViewBidList(param, viewRuleVoList);


        viewVoList.forEach(view ->
                view.setPriority(viewPriorityMap.getOrDefault(view.getBid(), Integer.MAX_VALUE))
        );

        return viewVoList.stream()
                // 过滤启用状态
                .filter(view -> view.getEnableFlag() == 1)
                // 过滤视图未被拦截或者视图类型为默认视图
                .filter(view -> !interceptViewBidList.contains(view.getBid())
                        || view.getViewType().equals(ViewComponentEnum.DEFAULT_CONSTANT))
                // 优先级排序
                .min(Comparator.comparing(CfgViewVo::getPriority))
                // 如果为空则返回默认视图
                .orElseGet(() -> getDefaultView(resultMap));
    }

    /**
     * 获取需要拦截的视图Bid集合
     *
     * @param param          视图查询参数
     * @param viewRuleVoList 视图规则列表
     * @return 需要拦截的Bid列表
     */
    private Set<String> getInterceptViewBidList(ViewQueryParam param, List<CfgObjectViewRuleVo> viewRuleVoList) {
        viewRuleVoList = CollUtil.filter(viewRuleVoList, new Filter<CfgObjectViewRuleVo>() {
            @Override
            public boolean accept(CfgObjectViewRuleVo cfgObjectViewRuleVo) {
                CfgPropertyMatchDto propertyMatchParams = cfgObjectViewRuleVo.getPropertyMatchParams();
                List<String> roleCodes = cfgObjectViewRuleVo.getRoleCodes();
                LinkedHashSet<String> tags = cfgObjectViewRuleVo.getTags();
                //获取不满足的条件的数据，故取反
                return !(judgePropertyMatch(propertyMatchParams) && judgeRoleCodes(roleCodes) && judgeTag(tags));
            }

            /**
             * 判断标签是否满足条件
             * 判断标签 (如果传入标签不为空，就判断标签是否满足条件)
             *
             * @param tags 标签
             * @return 是否满足 true表示满足 false不满足
             */
            private boolean judgeTag(LinkedHashSet<String> tags) {
                // 没有配置标签 满足
                return CollUtil.isEmpty(tags)
                        //没传入标签 满足
                        || StringUtils.isBlank(param.getTag())
                        //匹配上标签 满足
                        || tags.contains(param.getTag());
            }

            /**
             * 判断角色是否匹配条件
             * 判断角色是否包含传入的角色，如果没有交集则返回false
             *
             * @param roleCodes 角色编码
             * @return 是否满足 true表示满足 false不满足
             */
            private boolean judgeRoleCodes(List<String> roleCodes) {
                // 没配置角色 满足
                return CollUtil.isEmpty(roleCodes) ||
                        // 没传入角色列表 满足
                        CollUtil.isEmpty(param.getRoleCodes()) ||
                        // 传入角色列表不为空，就判断权限是否包含当前角色
                        !CollUtil.intersection(roleCodes, param.getRoleCodes()).isEmpty();
            }

            /**
             * 判断实例字段匹配情况
             * @param params 匹配参数
             * @return 判断结果，true表示满足 false不满足
             */
            private boolean judgePropertyMatch(CfgPropertyMatchDto params) {
                //表达式、实例字段任意一个为空，则返回直接返回true
                Map<String, Object> instance = param.getInstance();
                if (params == null || CollUtil.isEmpty(params.getExpressions()) || instance == null) {
                    return true;
                }

                //转换为简单表达式
                List<SimpleExpression> expressionList = params.getExpressions().stream().map(expression -> {
                    Object instanceValue = instance.get(expression.getProperty());
                    return SimpleExpression.of(instanceValue, expression.getCondition(), expression.getValue());
                }).collect(Collectors.toList());

                //执行条件匹配
                return SimpleExpression.evaluateExpressions(expressionList, Boolean.TRUE.equals(params.getAnyMatch()));
            }
        });
        return viewRuleVoList.stream().map(CfgObjectViewRuleVo::getViewBid).collect(Collectors.toSet());
    }

    private CfgViewVo getDefaultView(Map<String, List<CfgViewVo>> viewMap) {
        List<CfgViewVo> defaultViewVoList = viewMap.get(ViewEnums.DEFAULT.getCode());
        // 如果为空返回空对象
        if (CollUtil.isEmpty(defaultViewVoList)) {
            return new CfgViewVo();
        }
        return defaultViewVoList.get(0);
    }

    @Override
    public Map<String, CfgViewVo> getViews(ViewQueryParam param) {
        return Optional.ofNullable(param.getViewBelongBids())
                .filter(CollUtil::isNotEmpty)
                .map(belongbids -> cfgViewDomainService.listByBelongBids(belongbids))
                .orElseThrow(() -> new PlmBizException("视图所属bid列表不能为空"));
    }

    @Override
    public List<CfgViewVo> listView(String belongBid) {
        return Optional.ofNullable(belongBid)
                .filter(CharSequenceUtil::isNotBlank)
                .map(cfgViewDomainService::listByBelongBid)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<CfgViewVo> listView(List<String> belongBids) {
        List<CfgViewPo> list = cfgViewService.list(cfgViewService.lambdaQuery()
                .select(BasePoEntity::getBid,
                        CfgViewPo::getBelongBid,
                        CfgViewPo::getViewScope,
                        CfgViewPo::getType,
                        CfgViewPo::getClientType,
                        CfgViewPo::getName,
                        CfgViewPo::getDescription,
                        CfgViewPo::getModelCode,
                        CfgViewPo::getTag,
                        CfgViewPo::getPriority,
                        CfgViewPo::getViewType)
                .getWrapper().in(CfgViewPo::getBelongBid, belongBids)
                .eq(BasePoEntity::getDeleteFlag, 0));
        return CfgViewConverter.INSTANCE.pos2vos(list);
    }

    @Override
    public List<CfgViewMetaDto> getMetaModelsByParam(ViewQueryParam param) {
        return Optional.ofNullable(getView(param))
                .map(CfgViewVo::getMetaList)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<CfgViewVo> listTypeOrDefaultView(ViewQueryParam param) {
        //参数准备
        Assert.notNull(param, "参数为空");
        List<String> viewBelongBids = Optional.ofNullable(param.getViewBelongBids()).orElseGet(ArrayList::new);
        if (StringUtils.isNotBlank(param.getViewBelongBid())) {
            viewBelongBids.add(param.getViewBelongBid());
        }
        List<String> viewTypes = new ArrayList<>(2);
        String viewType = Optional.ofNullable(param.getViewType()).orElse(ViewEnums.DEFAULT.getCode());
        viewTypes.add(viewType);
        if (!ViewEnums.DEFAULT.getCode().equals(viewType)) {
            viewTypes.add(ViewEnums.DEFAULT.getCode());
        }

        //查询数据
        List<CfgViewPo> list = cfgViewService.list(cfgViewService.lambdaQuery().getWrapper()
                .in(CfgViewPo::getBelongBid, viewBelongBids)
                .in(CfgViewPo::getViewType, viewTypes)
                .eq(CfgViewPo::getDeleteFlag, 0)
                .eq(CfgViewPo::getEnableFlag, 1));

        //去重操作
        list = new ArrayList<>(list.stream().collect(Collectors.toMap(CfgViewPo::getBelongBid, Function.identity(),
                (v1, v2) -> viewType.equals(v1.getViewType()) ? v1 : v2)).values());

        return CfgViewConverter.INSTANCE.pos2vos(list);
    }


    @SneakyThrows
    @CacheEvict(value = CacheNameConstant.OBJECT_VIEW, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncViewContent(SyncViewContentQo qo) {
        Assert.notNull(qo, "同步参数不能为空");
        Assert.notNull(qo.getSourceBid(), "同步源数据不能为空");
        Assert.notEmpty(qo.getTargetBids(), "同步目标数据不能为空");

        //1、查询源数据
        CfgViewPo sourceView = cfgViewService.getByBid(qo.getSourceBid());
        Assert.notNull(sourceView, "同步源数据不存在");
        Assert.notNull(sourceView.getContent(), "同步源数据内容不能为空");
        Assert.notNull(sourceView.getMetaList(), "同步源数据元数据不能为空");

        //2、保存数据
        ObjectMapper mapper = new ObjectMapper();
        cfgViewService.update(cfgViewService.lambdaUpdate()
                .set(CfgViewPo::getContent, mapper.writeValueAsString(sourceView.getContent()))
                .set(CfgViewPo::getMetaList, mapper.writeValueAsString(sourceView.getMetaList()))
                .getWrapper().in(CfgViewPo::getBid, qo.getTargetBids()).eq(BasePoEntity::getDeleteFlag, 0));

        //3、记录操作记录
        CfgViewSyncRecordPo recordPo = new CfgViewSyncRecordPo();
        recordPo.setSourceBid(qo.getSourceBid());
        recordPo.setTargetBids(qo.getTargetBids());
        cfgViewSyncRecordService.save(recordPo);
    }

    @Override
    public List<String> lastSyncConfig(String viewBid) {
        return cfgViewSyncRecordService.getObj(cfgViewSyncRecordService.lambdaQuery()
                        .select(CfgViewSyncRecordPo::getTargetBids)
                        .getWrapper().eq(CfgViewSyncRecordPo::getSourceBid, viewBid)
                        .eq(BasePoEntity::getDeleteFlag, 0)
                        .last("limit 1")
                        .orderByDesc(BaseEntity::getCreatedTime),
                targetBids -> JSON.parseArray((String) targetBids, String.class));
    }


}
