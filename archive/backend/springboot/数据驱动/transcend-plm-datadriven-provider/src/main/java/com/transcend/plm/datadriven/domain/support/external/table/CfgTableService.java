package com.transcend.plm.datadriven.domain.support.external.table;

import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.api.feign.CfgTableFeignClient;

import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAttributeVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 表配置 转换
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/11 15:09
 */
@Component
public class CfgTableService {

    @Resource
    private CfgTableFeignClient cfgTableClient;

    /**
     * @return {@link CfgTableService }
     */
    public static CfgTableService getInstance() {
        return PlmContextHolder.getBean(CfgTableService.class);
    }


    /**
     * 获取表定义以及表定义的属性 TODO
     *
     * @param tableBid 表bid
     * @return TableDefinition
     */
    public TableDefinition getTableDefinitionByBid(String tableBid) {
        // 获取表信息
        CfgTableVo cfgTableVo = cfgTableClient.getTableAndAttributeByBid(tableBid).getCheckExceptionData();

        return convertTableDefinition(cfgTableVo);
    }

    /**
     * @param modelCode
     * @return {@link TableDefinition }
     */
    public TableDefinition getTableDefinitionByModelCode(String modelCode) {
        // 获取表信息
        CfgTableVo cfgTableVo = cfgTableClient.getTableAndAttributeByModelCode(modelCode).getCheckExceptionData();

        return convertTableDefinition(cfgTableVo);
    }

    /**
     * 批量获取表定义以及表定义的属性 TODO 待优化，现在是循环调用
     *
     * @param modelCodes
     * @return
     */
    public List<TableDefinition> listTableDefinitionByModelCodes(List<String> modelCodes) {
        if (CollectionUtils.isEmpty(modelCodes)) {
            return Collections.emptyList();
        }
        // 超过10个暂时不支持
        if (modelCodes.size() > 10) {
            throw new PlmBizException("暂时不支持超过10个modelCode的查询");
        }
        List<TableDefinition> tableDefinitions = Lists.newArrayList();
        modelCodes.forEach(modelCode -> {
            // 获取表信息
            CfgTableVo cfgTableVo = cfgTableClient.getTableAndAttributeByModelCode(modelCode).getCheckExceptionData();
            tableDefinitions.add(convertTableDefinition(cfgTableVo));
        });
        return tableDefinitions;
    }

    /**
     * @param cfgTableVo
     * @return {@link TableDefinition }
     */
    @NotNull
    private static TableDefinition convertTableDefinition(CfgTableVo cfgTableVo) {
        if (null == cfgTableVo || CollectionUtils.isEmpty(cfgTableVo.getAttributes())) {
            throw new PlmBizException("", "找不到表信息，或者表属性信息");
        }

        // 以下为转换过程
        List<CfgTableAttributeVo> attributes = cfgTableVo.getAttributes();
        TableDefinition tableDefinition = TablePojoConverter.INSTANCE.cfg2definition(cfgTableVo);

        List<TableAttributeDefinition> attributeDefinitions = TableAttritubePojoConverter.INSTANCE.cfg2definitions(attributes);
        tableDefinition.setTableAttributeDefinitions(attributeDefinitions);
        return tableDefinition;
    }


}
