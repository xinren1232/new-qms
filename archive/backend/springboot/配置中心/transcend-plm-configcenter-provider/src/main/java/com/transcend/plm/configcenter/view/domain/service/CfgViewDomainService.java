package com.transcend.plm.configcenter.view.domain.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.common.constant.ViewConst;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgOptionItemDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.qo.CfgViewQo;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.configcenter.common.exception.PlmBizException;
import com.transcend.plm.configcenter.common.pojo.dto.ImportDto;
import com.transcend.plm.configcenter.common.service.IExcelStrategy;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.common.validator.UniqueValidateParam;
import com.transcend.plm.configcenter.object.domain.entity.CfgObject;
import com.transcend.plm.configcenter.object.domain.service.CfgObjectViewRuleDomainService;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ConfigCommonMapper;
import com.transcend.plm.configcenter.view.infrastructure.repository.CfgViewRepository;
import com.transcend.plm.configcenter.view.infrastructure.repository.po.CfgViewPo;
import com.transcend.plm.configcenter.view.pojo.CfgViewConverter;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:38
 **/
@Service
public class CfgViewDomainService implements IExcelStrategy {
    public static final String PROPERTIES_LIST = "propertiesList";
    @Resource
    private CfgViewRepository repository;

    @Resource
    private ConfigCommonMapper commonMapper;

    @Resource
    private CfgObjectViewRuleDomainService cfgObjectViewRuleDomainService;


    @Transactional(rollbackFor = Exception.class)
    public boolean copyViews(CfgViewDto cfgViewDto){
        if(CollectionUtils.isEmpty(cfgViewDto.getViewBidMap()) && CollectionUtils.isEmpty(cfgViewDto.getViewBatchCopyMap())){
            return true;
        }
        //key 老的viewBid value 新的viewBid列表
        Map<String,List<String>> viewMap;
        if(CollectionUtils.isEmpty(cfgViewDto.getViewBatchCopyMap())){
            viewMap = cfgViewDto.getViewBidMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e-> ImmutableList.of(e.getValue())));
        }else {
            viewMap = cfgViewDto.getViewBatchCopyMap();
        }
        List<CfgViewPo> viewPoList = repository.listByBelongBids(viewMap.keySet());
        if(CollectionUtils.isNotEmpty(viewPoList)){
            List<CfgViewPo> copyList = Lists.newArrayList();
            Map<String, String> viewBidMap = new HashMap<>(16);
            for(CfgViewPo cfgViewPo:viewPoList){
                String oldBelongBid = cfgViewPo.getBelongBid();
                List<String> newBelongBids = viewMap.get(oldBelongBid);
                if(CollectionUtils.isNotEmpty(newBelongBids)){
                    newBelongBids.forEach(newBelongBid->{
                        CfgViewPo newCfgViewPo = new CfgViewPo();
                        BeanUtils.copyProperties(cfgViewPo,newCfgViewPo);
                        newCfgViewPo.setContent(cfgViewPo.getContent());
                        newCfgViewPo.setMetaList(cfgViewPo.getMetaList());
                        newCfgViewPo.setBelongBid(newBelongBid);
                        newCfgViewPo.setBid(SnowflakeIdWorker.nextIdStr());
                        newCfgViewPo.setId(null);
                        if(StringUtils.isNotEmpty(cfgViewDto.getType())){
                            String oldBidNew = oldBelongBid.replaceAll("#","-");
                            String newBidNew = newBelongBid.replaceAll("#","-");
                            if(newCfgViewPo.getName().contains(oldBidNew)){
                                newCfgViewPo.setName(newCfgViewPo.getName().replaceAll(oldBidNew,newBidNew));
                            }else{
                                newCfgViewPo.setName(newCfgViewPo.getName()+"-copy-"+newBidNew);
                            }
                        }else{
                            if(newCfgViewPo.getName().contains(oldBelongBid)){
                                newCfgViewPo.setName(newCfgViewPo.getName().replaceAll(oldBelongBid,newBelongBid));
                            }else{
                                newCfgViewPo.setName(newCfgViewPo.getName()+"-copy-"+newBelongBid);
                            }
                        }
                        newCfgViewPo.setCreatedTime(LocalDateTime.now());
                        newCfgViewPo.setUpdatedTime(LocalDateTime.now());
                        copyList.add(newCfgViewPo);
                        viewBidMap.put(newCfgViewPo.getBid(),cfgViewPo.getBid());
                    });
                }
            }
            cfgObjectViewRuleDomainService.copyViewRule(viewBidMap);
            return repository.saveBatch(copyList);
        }
        return true;
    }

    public CfgViewVo save(CfgViewDto cfgViewDto) {
        Assert.notNull(cfgViewDto, "view is null");
        UniqueValidateParam updateParam = UniqueValidateParam.builder()
                .tableName("cfg_view")
                .columnName("name")
                .value(cfgViewDto.getName())
                .excludeCurrentRecord(Boolean.FALSE)
                .excludeLogicDeleteItems(Boolean.TRUE)
                .logicDeleteFieldName("delete_flag")
                .logicDeleteValue(Boolean.FALSE).build();
        if (commonMapper.countByField(updateParam) > 0) {
            throw new PlmBizException("视图名称已存在");
        }
        // 填充初始化视图内容
        fillInitViewContent(cfgViewDto);
        return repository.save(cfgViewDto);
    }

    /**
     * 填充初始化视图内容
     *
     * @param cfgViewDto
     * @return
     */
    private CfgViewDto fillInitViewContent(CfgViewDto cfgViewDto) {
        if (StringUtil.isBlank(cfgViewDto.getBid())) {
            cfgViewDto.setBid(SnowflakeIdWorker.nextIdStr());
        }

        // 第一次新增需要按照类型初始化视图
        String type = cfgViewDto.getType();
        // 常规视图就是默认为NORMAL
        String initBid = ObjectTypeEnum.NORMAL.getCode();
        // 视图内容
        Map<String, Object> content = cfgViewDto.getContent();
        // 对象的视图，需要分解为有/无版本
        if (ViewConst.TYPE_OBJECT.equals(type)) {
            String modelCode = cfgViewDto.getModelCode();
            // 获取基类对象的类型
            String objectType = CfgObject
                    .buildWithModelCodeToBase(modelCode)
                    .populateBaseInfoByModelCode().getType();
            initBid = objectType;
        }
        // 对应类型就是内置的bid对于的视图
        CfgViewVo cfgViewVo = getByBid(initBid);
        // 视图不为空，以及视图内容为空，才需要填充
        if(cfgViewVo != null && CollectionUtils.isEmpty(content)){
            cfgViewDto.setContent(cfgViewVo.getContent());
            cfgViewDto.setMetaList(transferMetaList(content));
        }
        return cfgViewDto;
    }

    /**
     * content 转为 视图元数据列表
     * @param content 视图内容
     * @return 视图元数据列表
     */
    private List<CfgViewMetaDto> transferMetaList(Map<String, Object> content) {
        List<CfgViewMetaDto> metaList = Lists.newArrayList();
        if(CollectionUtils.isEmpty(content) || content.get(PROPERTIES_LIST) == null){
            return Lists.newArrayList();
        }
        JSONArray propertiesList = JSON.parseArray(JSON.toJSONString(content.get(PROPERTIES_LIST)));
        if (CollectionUtils.isEmpty(propertiesList)) {
            return Lists.newArrayList();
        }
        for (Object object : propertiesList) {
            JSONObject property = (JSONObject) object;
            metaList.add(CfgViewMetaDto.builder().name(property.getString("name")).label((String) JSONPath.eval(property, "$.field.options.label"))
                    .type((String) JSONPath.eval(property, "$.type")).defaultValue(JSONPath.eval(property, "$.field.options.defaultValue"))
                    .multiple((Boolean) JSONPath.eval(property, "$.field.options.multiple"))
                    .remoteDictType((String) JSONPath.eval(property, "$.field.options.remoteDictType"))
                    .optionItems((List<CfgOptionItemDto>) JSONPath.eval(property, "$.field.options.optionItems"))
                    .required((Boolean) JSONPath.eval(property, "$.field.options.required")).hidden((Boolean) JSONPath.eval(property, "$.field.options.hidden"))
                    .readonly((Boolean) JSONPath.eval(property, "$.field.options.readonly")).disabled((Boolean) JSONPath.eval(property, "$.field.options.disabled"))
                    .showPassword((Boolean) JSONPath.eval(property, "$.field.options.showPassword"))
                    .sourceModelCode(StringUtil.isNotBlank((String) JSONPath.eval(property, "$.field.options.sourceModelCodeTrue")) ?
                            (String) JSONPath.eval(property, "$.field.options.sourceModelCodeTrue") : (String) JSONPath.eval(property, "$.field.options.relationInfo.sourceModelCode"))
                    .relationModelCode((String) JSONPath.eval(property, "$.field.options.relationInfo.modelCode"))
                    .targetModelCode((String) JSONPath.eval(property, "$.field.options.relationInfo.targetModelCode"))
                    .instanceSelectAppBid((String) JSONPath.eval(property, "$.field.options.instanceSelectConfig.applicationBid"))
                    .build());
        }
        return metaList;
    }

    public CfgViewVo update(CfgViewDto cfgViewDto) {
        Assert.notNull(cfgViewDto, "view is null");
        Assert.hasText(cfgViewDto.getBid(), "view bid is blank");
        UniqueValidateParam updateParam = UniqueValidateParam.builder()
                .tableName("cfg_view")
                .columnName("name")
                .idFieldName("bid")
                .id(cfgViewDto.getBid())
                .value(cfgViewDto.getName())
                .excludeCurrentRecord(Boolean.TRUE)
                .excludeLogicDeleteItems(Boolean.TRUE)
                .logicDeleteFieldName("delete_flag")
                .logicDeleteValue(Boolean.FALSE).build();
        if (commonMapper.countByField(updateParam) > 0) {
            throw new PlmBizException("视图名称已存在");
        }
        cfgViewDto.setMetaList(transferMetaList(cfgViewDto.getContent()));
        return repository.update(cfgViewDto);
    }

    public boolean existByBid(String bid) {
        return repository.countByBid(bid) > 0;
    }

    public CfgViewVo getByBid(String bid) {
        return repository.getByBid(bid);
    }

    public Map<String,CfgViewVo> getByBids(Set<String> bids) {
        Map<String,CfgViewVo> resMap = new HashMap<>(10);
        List<CfgViewPo> list = repository.listByIds(bids);
        if (CollectionUtils.isNotEmpty(list)) {
            for(CfgViewPo cfgViewPo : list){
                CfgViewVo cfgViewVo = CfgViewConverter.INSTANCE.po2vo(cfgViewPo);
                resMap.put(cfgViewVo.getBid(),cfgViewVo);
            }
        }
        return resMap;
    }

    public List<CfgViewMetaDto> getMetaModelsByBid(String bid) {
        return repository.getMetaModelsByBid(bid);
    }

    public PagedResult<CfgViewVo> page(BaseRequest<CfgViewQo> pageQo) {
        return repository.page(pageQo);
    }

    public List<CfgViewVo> bulkAdd(List<CfgViewDto> cfgViewDtos) {
        for (CfgViewDto cfgViewDto : cfgViewDtos) {
            cfgViewDto.setMetaList(transferMetaList(cfgViewDto.getContent()));
        }
        return repository.bulkAdd(cfgViewDtos);
    }

    public boolean logicalDeleteByBid(String bid) {
        return repository.logicalDeleteByBid(bid);
    }

    public List<CfgViewVo> listByModelCode(String modelCode) {
        return repository.listByModelCode(modelCode);
    }

    public Boolean changeEnableFlag(String bid, Integer enableFlag) {
        CfgViewVo cfgViewVo = getByBid(bid);
        cfgViewVo.setEnableFlag(enableFlag);
        CfgViewDto dto = CfgViewConverter.INSTANCE.vo2dto(cfgViewVo);
        update(dto);
        return true;
    }

    public List<CfgViewVo> listByQo(CfgViewQo qo) {
        return repository.listByQo(qo);
    }

    public List<CfgViewVo> listByQoNoContent(CfgViewQo qo) {
        return repository.listByQoNoContent(qo);
    }

    @Override
    public List<Object> getExportExcelData(Object param) {
        CfgViewQo qo = JSON.parseObject(JSON.toJSONString(param), CfgViewQo.class);
        List<CfgViewVo> cfgViewVos = repository.listByQoNoContent(qo);
        List<String> headers = Lists.newArrayList("视图名称", "视图类型", "状态", "终端类型", "更新人", "更新时间");
        List<List<Object>> data = Lists.newArrayList();
        cfgViewVos.forEach(cfgViewVo -> {
            List<Object> rowData = Lists.newArrayList();
            rowData.add(cfgViewVo.getName());
            rowData.add(cfgViewVo.getType());
            rowData.add(cfgViewVo.getEnableFlag() == 1 ? "启用" : "禁用");
            rowData.add(cfgViewVo.getClientType());
            rowData.add(cfgViewVo.getUpdatedBy());
            rowData.add(cfgViewVo.getUpdatedTime());
            data.add(rowData);
        });
        List<Object> result = Lists.newArrayList();
        result.add(headers);
        result.add(data);
        return result;
    }

    @Override
    public boolean importData(List<Map<Integer, Object>> dataList, ImportDto importDto) {
        return false;
    }

    public PagedResult<CfgViewVo> mataPage(BaseRequest<CfgViewQo> pageQo) {
        // 防止空异常
        if (pageQo.getParam() == null) {
            pageQo.setParam(new CfgViewQo());
        }
        pageQo.getParam().setType(ViewConst.TYPE_META);
        return page(pageQo);
    }

    public PagedResult<CfgViewVo> noMataPage(BaseRequest<CfgViewQo> pageQo) {
        // 防止空异常
        if (pageQo.getParam() == null) {
            pageQo.setParam(new CfgViewQo());
        }
        Set<String> types = pageQo.getParam().getTypes();
        // 默认不带条件自带类型入参
        if (CollectionUtils.isEmpty(types)) {
            types = Sets.newHashSet(ViewConst.TYPE_CUSTOM, ViewConst.TYPE_OBJECT, ViewConst.TYPE_PROPERTY);
        }
        pageQo.getParam().setTypes(types);
        return page(pageQo);
    }

    public CfgViewVo getByModelCode(String modelCode) {
        return repository.getByModelCode(modelCode);
    }


    public Map<String, List<CfgViewVo>> listByConditions(CfgViewQo qo) {
        return repository.listByConditions(qo);
    }

    public Map<String, CfgViewVo> listByBelongBids(List<String> viewBelongBids) {
        List<CfgViewPo> viewPoList = repository.listByBelongBids(viewBelongBids);
        return viewPoList.stream()
                .collect(Collectors.toMap(
                        CfgViewPo::getBelongBid,
                        CfgViewConverter.INSTANCE::po2vo,
                        (k1, k2) -> k1)
                );
    }

    public List<CfgViewVo> listByBelongBid(String belongBid) {
        return repository.listByBelongBid(belongBid);
    }
}
