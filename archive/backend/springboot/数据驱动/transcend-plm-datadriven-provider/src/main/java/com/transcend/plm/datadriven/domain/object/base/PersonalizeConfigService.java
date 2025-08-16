package com.transcend.plm.datadriven.domain.object.base;

import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.TableTypeConstant;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.domain.datasource.pojo.dto.PersonalizeConfigDto;
import com.transcend.plm.datadriven.infrastructure.basedata.repository.BaseDataRepository;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Component
public class PersonalizeConfigService {
    @Resource
    private BaseDataRepository baseDataRepository;


    /**
     * @return {@link TableDefinition }
     */
    private TableDefinition getTableDefinition(){
        TableDefinition tableDefinition = TableDefinition.of()
                .setLogicTableName("transcend_personalize_config")
                .setUseLogicTableName(DataBaseConstant.BYTE_TRUE);
        // 表属性定义
        List<TableAttributeDefinition> attrs = new ArrayList<>();
        TableAttributeDefinition attrBid = TableAttributeDefinition.of()
                .setColumnName(TranscendModelBaseFields.BID).setProperty(TranscendModelBaseFields.BID).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition attrModelCode = TableAttributeDefinition.of()
                .setColumnName("model_code").setProperty(TranscendModelBaseFields.MODEL_CODE).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition attrConfigContent = TableAttributeDefinition.of()
                .setColumnName("config_content").setProperty("configContent").setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition attrCreatedBy = TableAttributeDefinition.of()
                .setColumnName("created_by").setProperty(TranscendModelBaseFields.CREATED_BY).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition attrUpdatedBy = TableAttributeDefinition.of()
                .setColumnName("updated_by").setProperty(TranscendModelBaseFields.UPDATED_BY).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition attrCreatedTime = TableAttributeDefinition.of()
                .setProperty(TranscendModelBaseFields.CREATED_TIME).setColumnName("created_time").setType(TableTypeConstant.DATE).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        attrs.add(attrBid);
        attrs.add(attrModelCode);
        attrs.add(attrConfigContent);
        attrs.add(attrCreatedBy);
        attrs.add(attrUpdatedBy);
        attrs.add(attrCreatedTime);

        // 给表定义设置表属性定义
        tableDefinition.setTableAttributeDefinitions(attrs);
        return tableDefinition;
    }

    /**
     * @param personalizeConfigDto
     * @return boolean
     */
    public boolean saveOrUpdate(PersonalizeConfigDto personalizeConfigDto){
        TableDefinition tableDefinition = getTableDefinition();
        // 实例数据
        MObject mBaseData = new MObject();
        mBaseData.setModelCode(personalizeConfigDto.getModelCode());
        String createdBy = SsoHelper.getJobNumber();
        mBaseData.setCreatedBy(createdBy);
        mBaseData.setUpdatedBy(createdBy);
        mBaseData.put("configContent",personalizeConfigDto.getConfigContent());
       if(StringUtil.isBlank(personalizeConfigDto.getBid())){
           //新增
           mBaseData.setBid(SnowflakeIdWorker.nextIdStr());
           baseDataRepository.add(tableDefinition, mBaseData);
       }else{
           //修改
           QueryWrapper qo = new QueryWrapper();
           qo.eq(DataBaseConstant.COLUMN_BID, personalizeConfigDto.getBid());
           List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
           baseDataRepository.update(tableDefinition,mBaseData,queryWrappers);
       }
       return true;
    }

    public MBaseData getByModelCode(String modelCode){
        QueryWrapper qo = new QueryWrapper();
        qo.eq("model_code", modelCode);
        qo.and().eq("created_by",SsoHelper.getJobNumber());
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        TableDefinition tableDefinition = getTableDefinition();
        return baseDataRepository.getOneByProperty(tableDefinition,queryWrappers);
    }
}
