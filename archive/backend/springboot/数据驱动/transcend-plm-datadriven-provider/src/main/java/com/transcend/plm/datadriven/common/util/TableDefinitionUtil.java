package com.transcend.plm.datadriven.common.util;

import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.TableTypeConstant;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
public class TableDefinitionUtil {
    /**
     * @return {@link TableDefinition }
     */
    public static TableDefinition getUpdateRecordTableDefinition(){
        TableDefinition tableDefinition = TableDefinition.of()
                .setLogicTableName("transcend_update_record")
                .setUseLogicTableName(DataBaseConstant.BYTE_TRUE);
        // 表属性定义
        List<TableAttributeDefinition> attrs = new ArrayList<>();
        TableAttributeDefinition attrBid = TableAttributeDefinition.of()
                .setColumnName(TranscendModelBaseFields.BID).setProperty(TranscendModelBaseFields.BID).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition attrModelCode = TableAttributeDefinition.of()
                .setColumnName("model_code").setProperty(TranscendModelBaseFields.MODEL_CODE).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition tableName = TableAttributeDefinition.of()
                .setColumnName("table_name").setProperty("tableName").setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition updateContent = TableAttributeDefinition.of()
                .setColumnName("update_content").setProperty("updateContent").setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition attrCreatedBy = TableAttributeDefinition.of()
                .setColumnName("created_by").setProperty(TranscendModelBaseFields.CREATED_BY).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition attrUpdatedBy = TableAttributeDefinition.of()
                .setColumnName("updated_by").setProperty(TranscendModelBaseFields.UPDATED_BY).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition createdByName = TableAttributeDefinition.of()
                .setColumnName("created_by_name").setProperty("createdByName").setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition attrCreatedTime = TableAttributeDefinition.of()
                .setProperty(TranscendModelBaseFields.CREATED_TIME).setColumnName("created_time").setType(TableTypeConstant.DATE).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        attrs.add(attrBid);
        attrs.add(attrModelCode);
        attrs.add(tableName);
        attrs.add(updateContent);
        attrs.add(attrCreatedBy);
        attrs.add(attrUpdatedBy);
        attrs.add(createdByName);
        attrs.add(attrCreatedTime);

        // 给表定义设置表属性定义
        tableDefinition.setTableAttributeDefinitions(attrs);
        return tableDefinition;
    }
}
