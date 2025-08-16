//package com.transcend.plm.configcenter.attribute.infrastructure.repository;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.util.ReflectUtil;
//import com.baomidou.mybatisplus.core.conditions.Wrapper;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
//import com.baomidou.mybatisplus.core.metadata.TableInfo;
//
//import java.io.Serializable;
//import java.util.*;
//
///**
// * TODO 描述
// *
// * @author jie.luo <jie.luo1@transsion.com>
// * @version V1.0.0
// * @date 2023/4/27 16:27
// * @since 1.0
// */
//public class MyMapperImpl<T> implements MyBaseMapper<T> {
//    @Override
//    public List<Map<String, Object>> selectListByWrapper(QueryWrapper<?> queryWrapper, TableInfo tableInfo) {
//        String selectColumns = generateSelectColumns(tableInfo);
//        queryWrapper.select(selectColumns);
//        List<Object> result = selectObjs((Wrapper<T>) queryWrapper);
//        if (result == null || result.isEmpty()) {
//            return Collections.emptyList();
//        } else {
//            List<Map<String, Object>> resultList = new ArrayList<>();
//            for (Object obj : result) {
//                if (obj instanceof Map) {
//                    resultList.add((Map<String, Object>) obj);
//                } else {
//                    Map<String, Object> map = new HashMap<>();
//                    for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
//                        String fieldName = fieldInfo.getProperty();
//                        Object fieldValue = ReflectUtil.getFieldValue(obj, fieldName);
//                        map.put(fieldInfo.getColumn(), fieldValue);
//                    }
//                    resultList.add(map);
//                }
//            }
//            return resultList;
//        }
//    }
//
//    private String generateSelectColumns(TableInfo tableInfo) {
//        StringBuilder sb = new StringBuilder();
//        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
//            if (sb.length() > 0) {
//                sb.append(", ");
//            }
//            sb.append(fieldInfo.getColumn());
//        }
//
//        return sb.toString();
//    }
//
//    private void  a(){
//        // 定义一个 TableInfo 对象，用于描述表结构
//        TableInfo tableInfo = new TableInfo();
//        tableInfo.setTableName("user");
//        tableInfo.addField(new TableFieldInfo("id", "id"));
//        tableInfo.addField(new TableFieldInfo("name", "name"));
//
//// 构造查询条件
//        QueryWrapper<Map<String, Object>> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("id", 1);
//
//// 调用 Mapper 方法进行查询
//        List<Map<String, Object>> userList = myMapper.selectMaps(queryWrapper, tableInfo);
//    }
//
//
//}