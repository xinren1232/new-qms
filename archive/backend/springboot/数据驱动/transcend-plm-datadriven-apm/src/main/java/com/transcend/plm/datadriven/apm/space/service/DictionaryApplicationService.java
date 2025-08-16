package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.configcenter.api.feign.DictionaryFeignClient;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.datadriven.api.model.Order;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典应用服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/30 14:43
 */
@Service
@AllArgsConstructor
public class DictionaryApplicationService {
    private DictionaryFeignClient dictionaryFeignClient;

    /**
     * 获取组件排序集合
     *
     * @param orders 排序列表
     * @return 组件排序集合
     */
    public Map<String, Map<String, Integer>> getDictSortMap(List<Order> orders) {
        //查询字典顺序
        Map<String, Map<String, Integer>> dictMap = new HashMap<>(16);
        List<String> dictList = orders.stream()
                .filter(order -> StringUtils.isNotBlank(order.getProperty())
                        && StringUtils.isNotBlank(order.getRemoteDictType())
                        && order.getDesc() == null).map(Order::getRemoteDictType).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(dictList)) {
            CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
            qo.setCodes(dictList);
            List<CfgDictionaryVo> dictionaryList = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo)
                    .getCheckExceptionData();

            if (CollectionUtils.isNotEmpty(dictionaryList)) {
                dictMap = dictionaryList.stream().collect(Collectors.toMap(CfgDictionaryVo::getCode,
                        vo -> vo.getDictionaryItems().stream().collect(Collectors.toMap(
                                CfgDictionaryItemVo::getKeyCode,
                                CfgDictionaryItemVo::getSort
                        ))
                ));
            }
        }
        return dictMap;
    }
}
