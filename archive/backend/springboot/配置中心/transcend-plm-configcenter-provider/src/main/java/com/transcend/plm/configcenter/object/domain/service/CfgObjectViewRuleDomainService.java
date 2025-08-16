package com.transcend.plm.configcenter.object.domain.service;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.api.model.object.dto.CfgViewRuleMatchDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.qo.CfgViewQo;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectViewRulePo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectViewRuleRepository;
import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectViewRuleEditParam;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectViewRuleAndViewInfoVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectViewRuleVo;
import com.transcend.plm.configcenter.view.domain.service.CfgViewDomainService;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/3/14 9:41
 * @since 1.0
 */
@Service
public class CfgObjectViewRuleDomainService {

    @Resource
    private CfgObjectViewRuleRepository cfgObjectViewRuleRepository;

    @Resource
    @Lazy
    private CfgViewDomainService cfgViewDomainService;


    public List<CfgObjectViewRuleVo> listByModelCode(String modelCode) {
        return cfgObjectViewRuleRepository.listByModelCode(modelCode);
    }

    public CfgObjectViewRuleVo saveOrUpdate(CfgObjectViewRuleEditParam cfgObjectViewRuleEditParam) {
        return StringUtil.isBlank(cfgObjectViewRuleEditParam.getBid()) ?
                cfgObjectViewRuleRepository.save(cfgObjectViewRuleEditParam) :
                cfgObjectViewRuleRepository.updateByBid(cfgObjectViewRuleEditParam);
    }

    public Boolean logicalDeleteByBid(String bid) {
        return cfgObjectViewRuleRepository.logicalDeleteByBid(bid);
    }

    /**
     * 批量保存并修改
     *
     * @param params
     * @return
     */
    public Boolean batchSaveOrUpdate(List<CfgObjectViewRuleEditParam> params) {
        params.forEach(this::saveOrUpdate);
        return true;
    }

    public Boolean batchCoverByModelCode(String modelCode, List<CfgObjectViewRuleEditParam> params) {
        // 1.过滤只能提交相同modelCode的数据，不能提交 非 modelCode的数据
        params = params.stream()
                .filter(saveParam -> modelCode.equals(saveParam.getModelCode()))
                .collect(Collectors.toList());
        // 2.获取绑定modelCode的权限
        Set<String> bidSet = cfgObjectViewRuleRepository.listBidByModelCode(modelCode);
        // 3.收集需要的bid集合
        Set<String> handlerBidSet = params.stream()
                .map(CfgObjectViewRuleEditParam::getBid).collect(Collectors.toSet());
        // bid的差集，获取需要删除的bid集合
        if (bidSet.removeAll(handlerBidSet)) {
            cfgObjectViewRuleRepository.logicalDeleteByBids(bidSet);
        }
        // 4.收集需要新增的数据，收集需要更新的数据,分别处理 增删改
        batchSaveOrUpdate(params);
        return true;
    }


    public List<CfgObjectViewRuleVo> listInheritByModelCode(String modelCode) {
        LinkedHashSet<String> modelCodeDescSet = ObjectCodeUtils.splitModelCodeDesc(modelCode);
        List<CfgObjectViewRuleVo> vos = cfgObjectViewRuleRepository.listByCondition(Lists.newArrayList(modelCodeDescSet));
        vos.forEach(vo -> {
            // 对象类型不匹配则为其父类
            if (!modelCode.equals(vo.getModelCode())) {
                vo.setInherit(Boolean.TRUE);
            }
        });
        return vos;
    }

    /**
     * 以modelCode为查询条件，以视图信息为基础，规则为辅的填充列表
     *
     * @param modelCode
     * @return
     */
    public List<CfgObjectViewRuleAndViewInfoVo> listAndViewInfoByModelCode(String modelCode) {
        // 1.查询视图(启用的视图)
        List<CfgViewVo> cfgViewVos = cfgViewDomainService.listByQoNoContent(
                new CfgViewQo().setModelCode(modelCode).setEnableFlag(CommonConst.ENABLE_FLAG_ENABLE)
        );
        // 2.查询视图规则
        List<CfgObjectViewRuleVo> cfgObjectViewRuleVos = listByModelCode(modelCode);
        // 转map
        Map<String, CfgObjectViewRuleVo> viewRuleMap = cfgObjectViewRuleVos.stream().collect(
                Collectors.toMap(CfgObjectViewRuleVo::getViewBid, Function.identity()));
        // 3.遍历视图列表，填充规则
        return cfgViewVos.stream().map(cfgViewVo -> {
            String viewVoBid = cfgViewVo.getBid();
            CfgObjectViewRuleAndViewInfoVo vo = new CfgObjectViewRuleAndViewInfoVo();

            CfgObjectViewRuleVo cfgObjectViewRuleVo = viewRuleMap.get(viewVoBid);
            if (cfgObjectViewRuleVo != null) {
                vo = BeanUtil.copyProperties(cfgObjectViewRuleVo, CfgObjectViewRuleAndViewInfoVo.class);
            }
            // 设置视图信息
            vo.setViewName(cfgViewVo.getName());
            vo.setViewBid(viewVoBid);
            vo.setModelCode(cfgViewVo.getModelCode());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取对象匹配规则
     *
     * @param cfgViewRuleMatchDto cfgViewRuleMatchDto
     * @return 视图bid
     */
    public String getViewBidByOptimalMatch(CfgViewRuleMatchDto cfgViewRuleMatchDto) {
        // 优先级，modelCode > tag > lcStateCode > 角色 TODO ,临时只获取modelCode的第一个排序
        List<CfgObjectViewRuleVo> cfgObjectViewRuleVos =
                cfgObjectViewRuleRepository.listByCondition(
                        Lists.newArrayList(cfgViewRuleMatchDto.getModelCode()),
                        cfgViewRuleMatchDto.getRoleType(),
                        cfgViewRuleMatchDto.getRoleCodeSet(),
                        cfgViewRuleMatchDto.getLcStateCode(),
                        cfgViewRuleMatchDto.getTag());
        // 不存在则直接返回null
        if (CollectionUtils.isEmpty(cfgObjectViewRuleVos)) {
            return null;
        }
        // 否则获取第一个
        CfgObjectViewRuleVo cfgObjectViewRuleVo = cfgObjectViewRuleVos.get(0);
        return cfgObjectViewRuleVo.getViewBid();
    }

    public String getViewBidByModelCodeAndTag(String modelCode, String tag) {
        return cfgObjectViewRuleRepository.getViewBidByModelCodeAndTag(modelCode, tag);
    }

    public List<CfgObjectViewRuleVo> listByViewBids(List<String> viewBids) {
        return cfgObjectViewRuleRepository.listByViewBids(viewBids);
    }

    public CfgObjectViewRuleVo getByViewBid(String viewBid) {
        List<CfgObjectViewRuleVo> viewRuleVoList = cfgObjectViewRuleRepository.listByViewBids(Lists.newArrayList(viewBid));
        if (CollectionUtils.isEmpty(viewRuleVoList)) {
            return null;
        }
        return viewRuleVoList.get(0);
    }

    /**
     * 复制视图规则
     *
     * @param copyRuleBidMap 复制视图bid列表 key = 新视图Bid value = 原视图Bid
     */
    @Transactional(rollbackFor = Exception.class)
    public void copyViewRule(Map<String, String> copyRuleBidMap) {
        if (CollUtil.isEmpty(copyRuleBidMap)) {
            return;
        }

        //获取旧视图信息
        List<String> oldViewBidList = copyRuleBidMap.values().stream().distinct().collect(Collectors.toList());
        Map<String, List<CfgObjectViewRulePo>> oldRuleMap = cfgObjectViewRuleRepository.listByViewPoBids(oldViewBidList)
                .stream().collect(Collectors.groupingBy(CfgObjectViewRulePo::getViewBid));

        //创建新视图规则
        List<CfgObjectViewRulePo> poList = copyRuleBidMap.entrySet().stream().flatMap(entry -> {
            List<CfgObjectViewRulePo> oldViewRuleList = oldRuleMap.get(entry.getValue());
            if (CollUtil.isEmpty(oldViewRuleList)) {
                return Stream.empty();
            }
            return oldViewRuleList.stream().map(oldViewRule -> {
                CfgObjectViewRulePo po = new CfgObjectViewRulePo();
                BeanUtils.copyProperties(oldViewRule, po);
                po.setId(null);
                po.setBid(null);
                po.setViewBid(entry.getKey());
                return po;
            });
        }).collect(Collectors.toList());

        //保存入库
        cfgObjectViewRuleRepository.saveBatch(poList);
    }
}
