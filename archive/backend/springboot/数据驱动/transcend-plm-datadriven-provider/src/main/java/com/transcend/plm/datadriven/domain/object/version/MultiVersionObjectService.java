package com.transcend.plm.datadriven.domain.object.version;

import com.transcend.plm.datadriven.common.constant.VersionObjectConstant;
import com.transcend.plm.datadriven.domain.basedata.BaseDataService;
import com.transcend.plm.datadriven.domain.object.base.pojo.dto.MultiVersionObjectDto;
import com.transcend.plm.datadriven.domain.support.external.object.CfgObjectService;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 版本对象-领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
@Component
public class MultiVersionObjectService {

    @Resource
    private BaseDataService baseDataService;


    /**
     * 新增   后期分表策略再实现 TODO 如果attr 有重复，抛异常
     *
     * @param multiVersionObjectDtos
     * @return MBaseData
     */
    public boolean add(List<MultiVersionObjectDto> multiVersionObjectDtos) {
        for(MultiVersionObjectDto mObject:multiVersionObjectDtos){
            // 通过tableBid获取table与attribute信息 TODO
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mObject.getBaseModel()).getTableBid();
            MBaseData oneData = baseDataService.add(tableBid, mObject.getMObject());
            // 2.新增历史 TODO
            baseDataService.addHis(tableBid, mObject.getMObject());
        }
        return true;
    }


    /**
     * 新增-批量 TODO 如果attr 有重复，抛异常
     *
     * @param multiVersionObjectDtos
     * @return Boolean
     */
    public Boolean addBatch(List<MultiVersionObjectDto> multiVersionObjectDtos) {
        for(MultiVersionObjectDto mObject:multiVersionObjectDtos){
            // 通过tableBid获取table与attribute信息 TODO
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mObject.getBaseModel()).getTableBid();
            // 2.新增历史 TODO
            return baseDataService.addBatch(tableBid, mObject.getMBaseDataList());
        }
        return true;
    }

    /**
     * 更新
     *
     * @param multiVersionObjectDtos
     * @return Boolean
     */
    public Boolean update(List<MultiVersionObjectDto> multiVersionObjectDtos) {
        for(MultiVersionObjectDto mObject:multiVersionObjectDtos){
            // 通过tableBid获取table与attribute信息 TODO
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mObject.getBaseModel()).getTableBid();
            return baseDataService.update(tableBid, mObject.getMObject(), mObject.getWrappers());
        }
        return true;
    }

    /**
     * 更新
     *
     * @param multiVersionObjectDtos
     * @return Boolean
     */
    public Boolean updateByDataBid(List<MultiVersionObjectDto> multiVersionObjectDtos) {
        // 通过tableBid获取table与attribute信息 TODO
        for(MultiVersionObjectDto mObject:multiVersionObjectDtos){
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mObject.getBaseModel()).getTableBid();

            QueryWrapper qo = new QueryWrapper();
            qo.eq(VersionObjectConstant.COLUMN_DATA_BID, mObject.getDataBid());
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            baseDataService.update(tableBid, mObject.getMObject(), queryWrappers);
        }
        return true;
    }

    /**
     * 删除
     *
     * @param multiVersionObjectDtos
     * @return Boolean
     */
    public Boolean deleteByDataBid(List<MultiVersionObjectDto> multiVersionObjectDtos) {
        for(MultiVersionObjectDto mObject:multiVersionObjectDtos){
            // 通过tableBid获取table与attribute信息 TODO
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mObject.getBaseModel()).getTableBid();
            QueryWrapper qo = new QueryWrapper();
            qo.eq(VersionObjectConstant.COLUMN_DATA_BID, mObject.getDataBid());
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            baseDataService.delete(tableBid, queryWrappers);
        }
        return true;
    }

    /**
     * 删除
     *
     * @param multiVersionObjectDtos
     * @return Boolean
     */
    public Boolean delete(List<MultiVersionObjectDto> multiVersionObjectDtos) {
        for(MultiVersionObjectDto mObject:multiVersionObjectDtos){
            // 通过tableBid获取table与attribute信息 TODO
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mObject.getBaseModel()).getTableBid();
            baseDataService.delete(tableBid, mObject.getWrappers());
        }
        return true;
    }

    /**
     * 逻辑删除-根据dataBid
     *
     * @param multiVersionObjectDtos
     * @return Boolean
     */
    public Boolean logicalDelete(List<MultiVersionObjectDto> multiVersionObjectDtos) {
        for(MultiVersionObjectDto mObject:multiVersionObjectDtos){
            // 通过tableBid获取table与attribute信息 TODO
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mObject.getBaseModel()).getTableBid();
            QueryWrapper qo = new QueryWrapper();
            qo.eq(VersionObjectConstant.COLUMN_DATA_BID, mObject.getDataBid());
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            baseDataService.logicalDelete(tableBid, queryWrappers);
        }
        return true;
    }


    /**
     * 逻辑删除
     *
     * @param multiVersionObjectDtos
     * @return Boolean
     */
    public Boolean logicalDeleteByDataBid(List<MultiVersionObjectDto> multiVersionObjectDtos) {
        for(MultiVersionObjectDto mObject:multiVersionObjectDtos){
            // 通过tableBid获取table与attribute信息 TODO
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mObject.getBaseModel()).getTableBid();
            QueryWrapper qo = new QueryWrapper();
            qo.eq(VersionObjectConstant.COLUMN_DATA_BID, mObject.getDataBid());
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            baseDataService.logicalDelete(tableBid, queryWrappers);
        }
        return true;
    }

    /**
     * 列表 TODO 如果attr 有重复，需要+1
     *
     * @param multiVersionObjectDtos
     * @return List<MBaseData>
     */
    public List<MVersionObject> list(List<MultiVersionObjectDto> multiVersionObjectDtos) {
        List<MVersionObject> mVersionObjects = new ArrayList<>();
        for(MultiVersionObjectDto mObject:multiVersionObjectDtos){
            // 通过tableBid获取table与attribute信息 TODO
            String tableBid  = CfgObjectService.getInstance().getByBaseModel(mObject.getBaseModel()).getTableBid();
            mVersionObjects.addAll(baseDataService.list(tableBid, mObject.getWrappers()));
        }
        return mVersionObjects;
    }

}
