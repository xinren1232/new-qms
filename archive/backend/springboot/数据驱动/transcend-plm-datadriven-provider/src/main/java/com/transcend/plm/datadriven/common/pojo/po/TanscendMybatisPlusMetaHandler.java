package com.transcend.plm.datadriven.common.pojo.po;

import com.baomidou.mybatisplus.extension.config.MybatisPlusExtendsProperties;
import com.baomidou.mybatisplus.extension.handlers.DefaultMetaObjectHandler;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import org.apache.ibatis.reflection.MetaObject;

/**
 * @Program transcend-plm-datadriven
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-23 10:03
 **/
public class TanscendMybatisPlusMetaHandler extends DefaultMetaObjectHandler {

    public TanscendMybatisPlusMetaHandler(MybatisPlusExtendsProperties.MetaProperties metaProps){
        super(metaProps);
    }
    @Override
    public void insertFill(MetaObject metaObject) {
        super.insertFill(metaObject);
        this.strictInsertFill(metaObject, TranscendModelBaseFields.BID, () -> SnowflakeIdWorker.nextIdStr(), String.class);
        this.strictInsertFill(metaObject, TranscendModelBaseFields.ENABLE_FLAG, () -> Integer.valueOf(0), Integer.class);
    }

}
