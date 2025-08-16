package com.transcend.plm.datadriven.apm.log;

import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.plm.configcenter.api.feign.CfgViewFeignClient;
import com.transcend.plm.configcenter.api.model.object.dto.CfgViewRuleMatchDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.log.context.ValueChangeContext;
import com.transcend.plm.datadriven.apm.log.model.dto.GenericLogAddParam;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogAddParam;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogEsData;
import com.transcend.plm.datadriven.common.util.EsUtil;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.common.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 操作日志Service
 *
 * @author yinbin
 * @version:
 * @date 2023/10/07 14:40
 */
@Service
public class OperationLogEsService {

    @Resource
    private BaseEsService baseEsService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Resource
    private CfgViewFeignClient cfgViewFeignClient;

    @Resource
    private ValueChangeContext valueChangeContext;

    @Async
    public void saveAsync(OperationLogAddParam operationLogAddParam, MObject mObject){
       save(operationLogAddParam, mObject);
    }

    /**
     * 保存操作日志
     *
     * @param operationLogAddParam 新增参数
     * @return boolean
     * @version: 1.0
     * @date: 2023/10/7 14:45
     * @author: bin.yin
     */
    public boolean save(OperationLogAddParam operationLogAddParam, MObject mObject) {
        // 判断是否是应用视图,应用视图直接取视图元数据
        CfgViewMetaDto cfgViewMetaDto = null;
        if (Boolean.TRUE.equals(operationLogAddParam.getIsAppView())) {
            cfgViewMetaDto = operationLogAddParam.getCfgViewMetaDto();
        } else {
            // 查询固定的视图，取出修改属性的中文 以及 类型
            CfgViewVo cfgViewVo = cfgViewFeignClient.getLogView(CfgViewRuleMatchDto.builder().modelCode(operationLogAddParam.getModelCode()).build()).getCheckExceptionData();
            if (Objects.nonNull(cfgViewVo)) {
                Map<String, CfgViewMetaDto> metaDtoMap = cfgViewVo.getMetaList().stream().collect(Collectors.toMap(CfgViewMetaDto::getName, Function.identity()));
                cfgViewMetaDto = metaDtoMap.get(operationLogAddParam.getFieldName());

            }
        }
        cfgViewMetaDto = Optional.ofNullable(cfgViewMetaDto).orElse(CfgViewMetaDto.builder().label(operationLogAddParam.getFieldName()).build());

        OperationLogCfgViewMetaDto operationLogCfgViewMetaDto = new OperationLogCfgViewMetaDto();
        BeanUtils.copyProperties(cfgViewMetaDto, operationLogCfgViewMetaDto);
        operationLogCfgViewMetaDto.setProperties(operationLogAddParam.getProperties());

        // 查询实例数据修改前的值
        if(mObject == null){
            mObject = objectModelCrudI.getByBid(operationLogAddParam.getModelCode(), operationLogAddParam.getInstanceBid());
        }
        Object beforeValue = mObject.get(operationLogAddParam.getFieldName());
        Object afterValue = operationLogAddParam.getFieldValue();
        // 值处理
        String changeValue = valueChangeContext.getServiceName(operationLogCfgViewMetaDto)
                .getChangeValue(operationLogAddParam.getSpaceAppBid(), beforeValue, afterValue, operationLogCfgViewMetaDto);
        if (StrUtil.isBlank(changeValue)) {
            // 如果为null 则不记录
            return true;
        }
        // 多存入ext数据，方便后续扩展多语言
        Map<String, Object> ext = Maps.newHashMap();
        ext.put("fieldName", operationLogAddParam.getFieldName());
        ext.put("oldValue", beforeValue);
        ext.put("fieldValue", operationLogAddParam.getFieldValue());
        ext.put("cfgViewMetaDto", cfgViewMetaDto);
        OperationLogEsData operationLogEsData = OperationLogEsData.of()
                .setSpaceAppBid(operationLogAddParam.getSpaceAppBid())
                .setSpaceBid(operationLogAddParam.getSpaceBid())
                .setModelCode(operationLogAddParam.getModelCode())
                .setExt(JSON.toJSONString(ext));
        operationLogEsData.setCreatedBy(operationLogAddParam.getCreatedBy());
        operationLogEsData.setBizId(operationLogAddParam.getInstanceBid())
                .setType(EsUtil.EsType.LOG.getType()).setJson(changeValue);
        baseEsService.save(EsUtil.getOperationLogIndex(), operationLogEsData);
        return true;
    }

    public boolean save(OperationLogAddParam operationLogAddParam, Object beforeValue) {
        // 判断是否是应用视图,应用视图直接取视图元数据
        CfgViewMetaDto cfgViewMetaDto = null;
        if (Boolean.TRUE.equals(operationLogAddParam.getIsAppView())) {
            cfgViewMetaDto = operationLogAddParam.getCfgViewMetaDto();
        } else {
            // 查询固定的视图，取出修改属性的中文 以及 类型
            CfgViewVo cfgViewVo = cfgViewFeignClient.getLogView(CfgViewRuleMatchDto.builder().modelCode(operationLogAddParam.getModelCode()).build()).getCheckExceptionData();
            if (Objects.nonNull(cfgViewVo)) {
                Map<String, CfgViewMetaDto> metaDtoMap = cfgViewVo.getMetaList().stream().collect(Collectors.toMap(CfgViewMetaDto::getName, Function.identity()));
                cfgViewMetaDto = metaDtoMap.get(operationLogAddParam.getFieldName());

            }
        }
        cfgViewMetaDto = Optional.ofNullable(cfgViewMetaDto).orElse(CfgViewMetaDto.builder().label(operationLogAddParam.getFieldName()).build());
        // 查询实例数据修改前的值
        Object afterValue = operationLogAddParam.getFieldValue();
        // 值处理
        OperationLogCfgViewMetaDto operationLogCfgViewMetaDto = new OperationLogCfgViewMetaDto();
        BeanUtils.copyProperties(cfgViewMetaDto, operationLogCfgViewMetaDto);
        operationLogCfgViewMetaDto.setProperties(operationLogAddParam.getProperties());
        String changeValue = valueChangeContext.getServiceName(operationLogCfgViewMetaDto)
                .getChangeValue(operationLogAddParam.getSpaceAppBid(), beforeValue, afterValue, operationLogCfgViewMetaDto);
        if (StrUtil.isBlank(changeValue)) {
            // 如果为null 则不记录
            return true;
        }
        // 多存入ext数据，方便后续扩展多语言
        Map<String, Object> ext = Maps.newHashMap();
        ext.put("fieldName", operationLogAddParam.getFieldName());
        ext.put("oldValue", beforeValue);
        ext.put("fieldValue", operationLogAddParam.getFieldValue());
        ext.put("cfgViewMetaDto", cfgViewMetaDto);
        OperationLogEsData operationLogEsData = OperationLogEsData.of()
                .setSpaceAppBid(operationLogAddParam.getSpaceAppBid())
                .setSpaceBid(operationLogAddParam.getSpaceBid())
                .setModelCode(operationLogAddParam.getModelCode())
                .setExt(JSON.toJSONString(ext));
        operationLogEsData.setBizId(operationLogAddParam.getInstanceBid())
                .setType(EsUtil.EsType.LOG.getType()).setJson(changeValue);
        baseEsService.save(EsUtil.getOperationLogIndex(), operationLogEsData);
        return true;
    }

    /**
     * 通用保存日志信息
     * @param genericLogAddParam genericLogAddParam
     * @return boolean
     * @version: 1.0
     * @date: 2023/10/9 9:21
     * @author: bin.yin
     */
    public boolean genericSave(GenericLogAddParam genericLogAddParam) {
        OperationLogEsData operationLogEsData = OperationLogEsData.of().setSpaceBid(genericLogAddParam.getSpaceBid())
                .setModelCode(genericLogAddParam.getModelCode());
        operationLogEsData.setBizId(genericLogAddParam.getInstanceBid())
                .setType(genericLogAddParam.getType()).setJson(genericLogAddParam.getLogMsg());
        baseEsService.save(EsUtil.getOperationLogIndex(), operationLogEsData);
        return true;
    }

    /**
     * 通用保存日志信息
     * @param genericLogAddParam genericLogAddParam
     * @return boolean
     * @version: 1.0
     * @date: 2023/10/9 9:21
     * @author: bin.yin
     */
    public boolean genericBulkSave(List<GenericLogAddParam> genericLogAddParam) {
        List<OperationLogEsData> operationLogEsDataList = genericLogAddParam.stream().map(param -> {
                    OperationLogEsData operationLogEsData = OperationLogEsData.of().setSpaceBid(param.getSpaceBid())
                            .setModelCode(param.getModelCode());
                    operationLogEsData.setBizId(param.getInstanceBid())
                            .setType(param.getType()).setJson(param.getLogMsg());
                    return operationLogEsData;
                }).collect(Collectors.toList());
        baseEsService.bulkSave(EsUtil.getOperationLogIndex(), operationLogEsDataList);
        return true;
    }

    /**
     * 查询操作日志
     *
     * @param spaceBid spaceBid 空间bid
     * @param type 类型
     * @param bizId    bizId 业务bid
     * @return List<OperationLogEsData>
     * @version: 1.0
     * @date: 2023/10/7 14:45
     * @author: bin.yin
     */
    public List<OperationLogEsData> queryList(String spaceBid, String type, String bizId) {
        Query byBizId = EsUtil.initTermQuery(EsUtil.BIZ_ID, bizId);
        List<Query> queries = Lists.newArrayList(byBizId);
        if (StringUtil.isNotBlank(spaceBid)) {
            queries.add(EsUtil.initTermQuery(EsUtil.SPACE_BID, spaceBid));
        }
        if (StringUtil.isNotBlank(type)) {
            queries.add(EsUtil.initMatchQuery(EsUtil.TYPE, type));
        }
        return baseEsService.queryList(EsUtil.getOperationLogIndex(), OperationLogEsData.class, queries);
    }


    /**
     * 简单操作日志
     *
     * @param bizBid 业务Id
     * @param logMsg 日志消息
     */
    public void simpleOperationLog(String bizBid, String logMsg, EsUtil.EsType esType) {
        GenericLogAddParam genericLogAddParam = GenericLogAddParam.builder()
                .logMsg(logMsg)
                .instanceBid(bizBid)
                .type(esType.getType())
                .build();
        this.genericSave(genericLogAddParam);
    }
}
