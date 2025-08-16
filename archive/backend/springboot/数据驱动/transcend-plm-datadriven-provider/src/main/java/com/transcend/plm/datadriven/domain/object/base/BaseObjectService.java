package com.transcend.plm.datadriven.domain.object.base;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.domain.basedata.BaseDataService;
import com.transcend.plm.datadriven.domain.support.external.object.CfgObjectService;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 基类对象-领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
@Component
@Deprecated
public class BaseObjectService {

    @Resource
    private BaseDataService baseDataService;

    /**
     * 新增   后期分表策略再实现 TODO 如果attr 有重复，抛异常
     *
     * @param baseModel
     * @param mObject
     * @return MBaseData
     */
    public MObject add(String baseModel, MObject mObject) {
        // 通过tableBid获取table与attribute信息 TODO
        String tableBid  = CfgObjectService.getInstance().getByBaseModel(baseModel).getTableBid();
        MBaseData oneData = baseDataService.add(tableBid, mObject);
        return null == oneData ? null : (MObject) oneData;
    }


    /**
     * 新增-批量 TODO 如果attr 有重复，抛异常
     *
     * @param baseModel
     * @param mBaseDataList
     * @return Boolean
     */
    public Boolean addBatch(String baseModel, List<MObject> mBaseDataList) {
        // 通过tableBid获取table与attribute信息 TODO
        String tableBid  = CfgObjectService.getInstance().getByBaseModel(baseModel).getTableBid();
        return baseDataService.addBatch(tableBid, mBaseDataList);
    }

    /**
     * 更新
     *
     * @param baseModel
     * @param mObject
     * @param wrappers
     * @return Boolean
     */
    public Boolean update(String baseModel, MObject mObject, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        String tableBid  = CfgObjectService.getInstance().getByBaseModel(baseModel).getTableBid();
        return baseDataService.update(tableBid, mObject, wrappers);
    }

    /**
     * 删除
     *
     * @param baseModel
     * @param wrappers
     * @return Boolean
     */
    public Boolean delete(String baseModel, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        String tableBid  = CfgObjectService.getInstance().getByBaseModel(baseModel).getTableBid();
        return baseDataService.delete(tableBid, wrappers);
    }

    /**
     * 逻辑删除
     *
     * @param baseModel
     * @param wrappers
     * @return Boolean
     */
    public Boolean logicalDelete(String baseModel, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        String tableBid  = CfgObjectService.getInstance().getByBaseModel(baseModel).getTableBid();
        return baseDataService.logicalDelete(tableBid, wrappers);
    }

    /**
     * 列表 TODO 如果attr 有重复，需要+1
     *
     * @param baseModel
     * @param wrappers
     * @return List<MBaseData>
     */
    public List<MObject> list(String baseModel, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        String tableBid  = CfgObjectService.getInstance().getByBaseModel(baseModel).getTableBid();
        return baseDataService.list(tableBid, wrappers);
    }

    /**
     * 获取一个
     *
     * @param baseModel
     * @param value
     * @return MBaseData
     */
    public MObject getOneByProperty(String baseModel, String value) {
        // 通过tableBid获取table与attribute信息 TODO
        String tableBid  = CfgObjectService.getInstance().getByBaseModel(baseModel).getTableBid();
        MBaseData oneData = baseDataService.getOneByProperty(tableBid, TranscendModelBaseFields.BID, value);
        return null == oneData ? null : (MObject) oneData;
    }

    /**
     * 分页列表
     *
     * @param baseModel
     * @param pageQo
     * @param filterRichText
     * @return PagedResult<MBaseData>
     */
    public PagedResult<MObject> page(String baseModel, BaseRequest<List<QueryWrapper>> pageQo, boolean filterRichText) {
        // 通过tableBid获取table与attribute信息 TODO
        String tableBid  = CfgObjectService.getInstance().getByBaseModel(baseModel).getTableBid();
        return baseDataService.page(tableBid, pageQo, filterRichText);
    }

    /**
     * 计数
     *
     * @param baseModel
     * @param wrappers
     * @return int
     */
    public int count(String baseModel, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        String tableBid  = CfgObjectService.getInstance().getByBaseModel(baseModel).getTableBid();
        return baseDataService.count(tableBid, wrappers);
    }


}
