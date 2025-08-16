package com.transcend.plm.configcenter.common.constant;

import com.google.common.collect.ImmutableMap;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023-07-14 13:42:00
 */
public class Object2DBType {

    public static final ImmutableMap<String,String> Obj2DBTypeMap = ImmutableMap.<String, String>builder()
            .put("string","varchar(255)")
            .put("int","int")
            .put("double","double")
            .put("date","datetime")
            .put("boolean","tinyint(1)")
            .put("long","bigint")
            .put("float","float")
            .put("text","text")
            .put("json","json")
            .put(TableHandleTypeConstant.SELECT,"varchar(255)")
            .put(TableHandleTypeConstant.MULTI_SELECT,"varchar(255)")
            .put(TableHandleTypeConstant.FILE,"json")
            .put(TableHandleTypeConstant.OBJECT_TYPE, "varchar(64)")
            .build();

}
