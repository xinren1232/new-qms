package com.transcend.plm.datadriven.domain.object.base;

import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.common.util.TableDefinitionUtil;
import com.transcend.plm.datadriven.domain.datasource.pojo.dto.UpdateRecordDto;
import com.transcend.plm.datadriven.infrastructure.basedata.repository.BaseDataRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Component
public class UpdateRecordService {
    @Resource
    private BaseDataRepository baseDataRepository;

    /**
     * @param modelCode
     * @return {@link List }<{@link MBaseData }>
     */
    public List<MBaseData> listUpdateRecord(String modelCode){
        TableDefinition tableDefinition = TableDefinitionUtil.getUpdateRecordTableDefinition();
        QueryWrapper qo = new QueryWrapper();
        qo.eq("model_code", modelCode);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MBaseData> list =  baseDataRepository.list(tableDefinition,queryWrappers);
        return list;
    }

    /**
     * @param updateRecordDto
     * @return boolean
     */
    public boolean save(UpdateRecordDto updateRecordDto){
        TableDefinition tableDefinition = TableDefinitionUtil.getUpdateRecordTableDefinition();
        // 实例数据
        MObject mBaseData = new MObject();
        mBaseData.setModelCode(updateRecordDto.getModelCode());
        mBaseData.setCreatedBy(updateRecordDto.getJobNumber());
        mBaseData.setUpdatedBy(updateRecordDto.getJobNumber());
        mBaseData.put("updateContent",updateRecordDto.getUpdateContent());
        mBaseData.setBid(SnowflakeIdWorker.nextIdStr());
        mBaseData.put("tableName",updateRecordDto.getTableName());
        baseDataRepository.add(tableDefinition, mBaseData);
        return true;
    }

}
