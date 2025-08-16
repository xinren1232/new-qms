package com.transcend.plm.datadriven.domain.object.base;

import com.transcend.plm.datadriven.domain.basedata.BaseDataService;
import com.transcend.plm.datadriven.domain.object.base.pojo.dto.MutiBaseClassDto;
import com.transcend.plm.datadriven.domain.support.external.object.CfgObjectService;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 多基类对象-领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/5 14:50
 * @since 1.0
 */
@Component
public class MultiBaseObjectService {
    @Resource
    private BaseDataService baseDataService;

    /**
     * 多基类新增-批量 TODO 如果attr 有重复，抛异常
     *  map参数中 key是modelCode, value 是  List<MObject>
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean addBatch(Map<String, List<MObject>> multiBaseClassMap) {
        // 通过tableBid获取table与attribute信息 TODO
        for(Map.Entry<String,List<MObject>> entry : multiBaseClassMap.entrySet()){
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(entry.getKey()).getTableBid();
            baseDataService.addBatch(tableBid, entry.getValue());
        }
        return true;
    }

    /**
     * 多基类更新
     *
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(List<MutiBaseClassDto> mutiBaseClassDtoList) {
        // 通过tableBid获取table与attribute信息 TODO
        for(MutiBaseClassDto mutiBaseClassDto:mutiBaseClassDtoList){
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mutiBaseClassDto.getBaseModel()).getTableBid();
            baseDataService.update(tableBid, mutiBaseClassDto.getMObject(), mutiBaseClassDto.getWrappers());
        }
        return true;
    }

    /**
     * 多基类删除
     *
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Map<String, List<QueryWrapper>> multiBaseClassMap) {
        // 通过tableBid获取table与attribute信息 TODO
        for(Map.Entry<String,List<QueryWrapper>> entry : multiBaseClassMap.entrySet()){
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(entry.getKey()).getTableBid();
            baseDataService.delete(tableBid, entry.getValue());
        }
        return true;
    }

    /**
     * 多基类逻辑删除
     *
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean logicalDelete(Map<String, List<QueryWrapper>> multiBaseClassMap) {
        // 通过tableBid获取table与attribute信息 TODO
        for(Map.Entry<String,List<QueryWrapper>> entry : multiBaseClassMap.entrySet()){
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(entry.getKey()).getTableBid();
            baseDataService.logicalDelete(tableBid, entry.getValue());
        }
        return true;
    }

    /**
     * 多基类列表 TODO 如果attr 有重复，需要+1
     *
     * @return List<MBaseData>
     */
    public List<MObject> list(List<MutiBaseClassDto> mutiBaseClassDtoList) {
        // 通过tableBid获取table与attribute信息 TODO
        List<MObject> resList = new ArrayList<>();
        for(MutiBaseClassDto mutiBaseClassDto : mutiBaseClassDtoList){
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mutiBaseClassDto.getBaseModel()).getTableBid();
            resList.addAll(baseDataService.list(tableBid, mutiBaseClassDto.getWrappers()));
        }
        return resList;
    }
}
