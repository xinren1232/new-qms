package com.transcend.plm.configcenter.attribute.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.transcend.plm.configcenter.attribute.infrastructure.repository.po.CfgAttributePo;

import java.util.List;
import java.util.Map;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/4/27 16:15
 * @since 1.0
 */
public interface MyBaseMapper<T> extends BaseMapper<T> {

//    default T getOne(Map<String, Object> entity) {
//        TableInfo tableInfo = TableInfoHelper.getTableInfo(CfgAttributePo.class);
//        QueryWrapper<T> wrapper = new QueryWrapper<>();
//        for (Map.Entry<String, Object> entry : entity.entrySet()) {
//            TableFieldInfo fieldInfo = getFieldInfo(tableInfo, entry.getKey());
//            if (fieldInfo != null) {
//                wrapper.eq(fieldInfo.getColumn(), entry.getValue());
//            }
//        }
//        return selectOne(wrapper);
//    }
//
//    default TableFieldInfo getFieldInfo(TableInfo tableInfo, String fieldName) {
//        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
//            if (fieldInfo.getProperty().equals(fieldName)) {
//                return fieldInfo;
//            }
//        }
//        return null;
//    }

    List<Map<String, Object>> selectListByWrapper(QueryWrapper<?> queryWrapper, TableInfo tableInfo);
}