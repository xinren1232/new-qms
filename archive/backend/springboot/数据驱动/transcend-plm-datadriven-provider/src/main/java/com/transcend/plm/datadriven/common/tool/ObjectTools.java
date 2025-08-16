package com.transcend.plm.datadriven.common.tool;

import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.domain.support.external.object.CfgObjectService;
import com.transcend.plm.datadriven.domain.support.external.table.CfgTableService;
import com.transcend.plm.datadriven.api.model.config.ObjectAttributeVo;
import com.transcend.plm.datadriven.api.model.config.ObjectVo;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 对象工具箱
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/6 10:07
 * @since 1.0
 */
public class ObjectTools {


    /**
     * 填充表属性 这里需要定义扩展，收集需要移除的attr,或者新增attr(这里做个接口，至于如何生效，这里定义对象的 版本类型，无版本，关系，
     * 针对这个接口做实现，把类型与实现的类做个map)  TODO`
     * @param modelCode 模型code
     * @return TableDefinition
     */
    public static TableDefinition fillTableDefinition(@NotNull String modelCode, Set<String> resultFieldSet) {
        ObjectVo objectVo = CfgObjectService.getInstance().getByModelCode(modelCode);
        objectVo.setTableBid(StringUtils.isNotBlank(objectVo.getBaseModel()) ? objectVo.getBaseModel():objectVo.getModelCode());
        List<ObjectAttributeVo> attributes = objectVo.getAttributes();
        // 获取默认支持的扩展属性
        List<TableAttributeDefinition> defaultSupportExtTableAttributes = TableTools.getDefaultSupportExtTableAttributes();
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByModelCode(modelCode);
        List<TableAttributeDefinition> tableAttributeDefinitions = table.getTableAttributeDefinitions();
        Map<String,String> tableAttrMap = new HashMap<>(16);
        for(int i=tableAttributeDefinitions.size()-1;i>=0;i--){
            //去掉重复的
            if(tableAttrMap.containsKey(tableAttributeDefinitions.get(i).getProperty())){
                tableAttributeDefinitions.remove(i);
            }
            tableAttrMap.put(tableAttributeDefinitions.get(i).getProperty(),tableAttributeDefinitions.get(i).getColumnName());
        }


        // 获取非基础属性，并转换为非基础属性的表属性
        List<TableAttributeDefinition> extTableAttributes = attributes.stream().filter(
                attribute ->
                        !DataBaseConstant.BASE_COLUMN_FLAG.equals(attribute.getBaseAttr())&&!tableAttrMap.containsKey(attribute.getCode())
        ).map(attribute ->

                TableAttributeDefinition.of()
                        .setBaseFlag(DataBaseConstant.EXT_COLUMN_FLAG)
                        .setColumnName(attribute.getCode())
                        .setProperty(attribute.getCode())
                        .setType(attribute.getDataType())
        ).collect(Collectors.toList());

        // 对象扩展列收集到ext属性中
        tableAttributeDefinitions.addAll(extTableAttributes);
        // 配置的支持扩展列收集到ext属性中
        tableAttributeDefinitions.addAll(defaultSupportExtTableAttributes);
        // 暂时设置组合为 transcend

        // 复制全的属性
        table.setFullTableAttributeDefinitions(Lists.newArrayList(tableAttributeDefinitions));

//         如果过滤结果字段只要部分
        if(CollectionUtils.isNotEmpty(resultFieldSet) && CollectionUtils.isNotEmpty(tableAttributeDefinitions)){
            tableAttributeDefinitions.removeIf(tableAttributeDefinition -> !resultFieldSet.contains(tableAttributeDefinition.getProperty()));
        }
//        //过滤富文本字段
//        if (filterRichText) {
//            table.setTableAttributeDefinitions(table.getTableAttributeDefinitions().stream().filter(attr -> !pageFilterAttrList.contains(attr.getType())).collect(Collectors.toList()));
//        } else {
//            table.setTableAttributeDefinitions(table.getTableAttributeDefinitions());
//        }

        //塞入模型编码
        table.setModelCode(modelCode);
        return table;
    }

    public static TableDefinition fillTableDefinition(@NotNull String modelCode) {
        return fillTableDefinition(modelCode,null);
    }
}
