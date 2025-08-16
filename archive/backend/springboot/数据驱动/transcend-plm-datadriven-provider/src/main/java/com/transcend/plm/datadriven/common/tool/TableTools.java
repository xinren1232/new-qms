package com.transcend.plm.datadriven.common.tool;

import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.common.config.ObjectProperties;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/6 10:07
 * @since 1.0
 */
public class TableTools {

    public static TableAttributeDefinition getTableExtAttributeDefinition() {
        return TableAttributeDefinition.of()
                .setBaseFlag(DataBaseConstant.EXT_COLUMN_FLAG)
                .setPropertyAndColumn(DataBaseConstant.COLUMN_EXT);
    }



    public static List<TableAttributeDefinition> getDefaultSupportExtTableAttributes() {
        ObjectProperties properties = PlmContextHolder.getBean(ObjectProperties.class);
        List<String> extSupportColumns = properties.getExtSupportColumns();
        if (CollectionUtils.isEmpty(extSupportColumns)){
            return Lists.newArrayList();
        }
        return extSupportColumns.stream().map(column->
                TableAttributeDefinition.of()
                        .setBaseFlag(DataBaseConstant.EXT_COLUMN_FLAG)
                        .setPropertyAndColumn(column)
        ).collect(Collectors.toList());
    }
}
