package com.transcend.plm.datadriven.apm.supplier.service.impl;

import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.supplier.service.SupplierService;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import com.transsion.framework.exception.BusinessException;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 战略供应商管理服务接口实现类
 *
 * @author yanbing.ao
 * @version: 1.0
 * @date 2023/11/15 11:13
 */
@Service
public class SupplierServiceImpl implements SupplierService {
    @Resource
    private ObjectModelDomainService objectModelDomainService;

    @Resource
    private CfgObjectRelationFeignClient cfgObjectRelationFeignClient;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Override
    public List<MObject> listObjectsByRelations(RelationMObject relationObject) {

        List<MObject> results = new ArrayList<>();
        //源关系模型编码查询实例数据
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), relationObject.getSourceBid());
        List<MObject> sourceRelationObjectList = listByRelationEnumColumn(qo, relationObject.getSourceRelationModelCode());
        if(CollectionUtils.isEmpty(sourceRelationObjectList)){
            return results;
        }

        //收集关系实例targetBid
        List<Object> targetBids = sourceRelationObjectList.stream().map(mObject -> mObject.get(RelationEnum.TARGET_BID.getCode())).distinct().collect(Collectors.toList());

        //目标关系模型编码查询实例数据
        QueryWrapper qo2 = new QueryWrapper();
        qo2.in(RelationEnum.TARGET_BID.getColumn(), targetBids);
        List<MObject> targetRelationObjectList = listByRelationEnumColumn(qo2, relationObject.getTargetRelationModelCode());
        if(CollectionUtils.isEmpty(targetRelationObjectList)){
            return results;
        }

        //收集关系实例sourceBid
        List<Object> sourceBids = targetRelationObjectList.stream().map(mObject -> mObject.get(RelationEnum.SOURCE_BID.getCode())).distinct().collect(Collectors.toList());

        //查看源对象信息
        CfgObjectRelationVo objectRelationVo = cfgObjectRelationFeignClient.getRelation(relationObject.getTargetRelationModelCode()).getCheckExceptionData();
        if(objectRelationVo == null){
            return results;
        }
        ;
        //查询实例数据
        ModelFilterQo modelFilterQo = new ModelFilterQo();
        modelFilterQo.setProperty(RelationEnum.BID.getColumn());
        modelFilterQo.setCondition(QueryFilterConditionEnum.IN.getSqlCondition());
        modelFilterQo.setValues(sourceBids);
        ModelMixQo modelMixQo = new ModelMixQo();
        List<ModelFilterQo> queries = new ArrayList<>();
        queries.add(modelFilterQo);
        modelMixQo.setQueries(queries);
        List<QueryWrapper> queryWrappers = QueryConveterTool.convert(modelMixQo).getQueries();
        results = objectModelCrudI.listByQueryWrapper(objectRelationVo.getSourceModelCode(),queryWrappers);
        return results;
    }

    private List<MObject> listByRelationEnumColumn(QueryWrapper qo, String sourceModelCode) {

        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> sourceRelationObjectList;
        try {
            QueryCondition queryCondition = new QueryCondition();
            // 默认更新时间倒序
            queryCondition.setOrders(Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
            queryCondition.setQueries(queryWrappers);
            sourceRelationObjectList = objectModelDomainService.list(sourceModelCode, queryCondition);
        } catch (Exception e) {
            throw new BusinessException(String.format("查询关系实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", sourceModelCode));
        }
        return sourceRelationObjectList;
    }
}
