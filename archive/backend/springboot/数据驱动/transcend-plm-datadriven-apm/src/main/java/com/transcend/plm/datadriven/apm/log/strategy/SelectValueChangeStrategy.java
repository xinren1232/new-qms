package com.transcend.plm.datadriven.apm.log.strategy;

import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.api.feign.DictionaryFeignClient;
import com.transcend.plm.configcenter.api.feign.LifeCycleFeignClient;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgOptionItemDto;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.configcenter.api.model.lifecycle.qo.LifeCycleStateListQo;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.enums.LanguageEnum;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 下拉框值转换策略
 * @author yinbin
 * @version:
 * @date 2023/10/08 11:41
 */
@Component(ViewComponentEnum.SELECT_CONSTANT + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX)
public class SelectValueChangeStrategy extends AbstractValueChangeStrategy {

    @Resource
    private LifeCycleFeignClient lifeCycleFeignClient;
    @Resource
    private DictionaryFeignClient dictionaryFeignClient;

    public static final String LIFE_CYCLE_CODE_DICT_TYPE = "__lcTepState__";

    @Override
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        String remoteDictType = cfgViewMetaDto.getRemoteDictType();
        boolean oldIsArray = isArray(oldValue);
        // 下拉框多选 或者为多选框
        boolean newIsArray = (ViewComponentEnum.SELECT.getType().equals(cfgViewMetaDto.getType()) && Boolean.TRUE.equals(cfgViewMetaDto.getMultiple())) ||
                ViewComponentEnum.CHECKBOX.getType().equals(cfgViewMetaDto.getType());
        List<String> oldValueList = Lists.newArrayList();
        List<String> newValueList = Lists.newArrayList();
        // 判断是否是数组
        if (oldIsArray) {
            oldValueList = transferArrayValue(oldValue, String.class);
        }
        if (newIsArray) {
            newValueList = transferArrayValue(newValue, String.class);
        }
        if (StringUtil.isBlank(remoteDictType)) {
            // 使用自定义下拉选项
            List<CfgOptionItemDto> optionItems = cfgViewMetaDto.getOptionItems();
            if (optionItems != null && !optionItems.isEmpty()) {
                Map<String, String> optionItemsMap = optionItems.stream().collect(Collectors.toMap(CfgOptionItemDto::getValue, CfgOptionItemDto::getLabel));
                // 转换值
                oldValue = transferDictValue(oldValue, oldValueList, oldIsArray, optionItemsMap);
                newValue = transferDictValue(newValue, newValueList, newIsArray, optionItemsMap);
            }
        } else {
            // 判断是否是生命周期状态下拉,生命周期只能是单选
            if (LIFE_CYCLE_CODE_DICT_TYPE.equals(cfgViewMetaDto.getRemoteDictType())) {
                // 查询所有生命状态匹配值
                LifeCycleStateListQo lifeCycleStateListQo = new LifeCycleStateListQo();
                lifeCycleStateListQo.setEnableFlag(CommonConst.ENABLE_FLAG_ENABLE);
                Map<String, String> lifeCycleCodeMap = lifeCycleFeignClient.queryByCodes(Lists.newArrayList((String) oldValue, (String) newValue)).getCheckExceptionData()
                        .stream().collect(Collectors.toMap(LifeCycleStateVo::getCode, LifeCycleStateVo::getName, (k1, k2) -> k1));
                oldValue = lifeCycleCodeMap.getOrDefault(oldValue, (String) oldValue);
                newValue = lifeCycleCodeMap.getOrDefault(newValue, (String) newValue);
            } else {
                // 查询字典
                CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
                qo.setCodes(Lists.newArrayList(remoteDictType));
                qo.setEnableFlags(Lists.newArrayList(CommonConst.ENABLE_FLAG_ENABLE));
                List<CfgDictionaryVo> cfgDictionaryVos = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getCheckExceptionData();
                if (CollectionUtil.isNotEmpty(cfgDictionaryVos)) {
                    // 暂时中文,后续需要根据语言切换
                    Map<String, String> dictItemMap = cfgDictionaryVos.get(0).getDictionaryItems().stream()
                            .collect(Collectors.toMap(CfgDictionaryItemVo::getKeyCode, e -> (String) e.get(LanguageEnum.ZH.getCode()), (k1, k2) -> k1));
                    // 转换值
                    oldValue = transferDictValue(oldValue, oldValueList, oldIsArray, dictItemMap);
                    newValue = transferDictValue(newValue, newValueList, newIsArray, dictItemMap);
                }
            }
        }
        return super.getChangeValue(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
    }


}
