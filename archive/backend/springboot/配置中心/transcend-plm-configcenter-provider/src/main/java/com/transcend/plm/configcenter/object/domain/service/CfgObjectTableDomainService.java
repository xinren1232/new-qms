package com.transcend.plm.configcenter.object.domain.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAttributeVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.common.constant.Object2DBType;
import com.transcend.plm.configcenter.common.constant.TableConst;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectAttributeRepository;
import com.transcend.plm.configcenter.table.domain.service.CfgTableDomainService;
import com.transcend.plm.configcenter.table.infrastructure.repository.CfgTableRepository;
import com.transcend.plm.configcenter.table.pojo.CfgTableAttributeConverter;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableAttributeDto;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableDto;
import com.transcend.plm.configcenter.table.pojo.dto.CloumnDto;
import com.transcend.plm.configcenter.table.pojo.dto.TableDto;
import com.transsion.framework.common.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 对象表领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/3/14 9:41
 * @since 1.0
 */
@Service
public class CfgObjectTableDomainService {

    @Resource
    private CfgTableDomainService cfgTableDomainService;

    @Resource
    private ICfgObjectAppService cfgObjectAppService;

    @Resource
    private CfgTableRepository tableRepository;

    @Resource
    private CfgObjectAttributeRepository cfgObjectAttributeRepository;


    @Transactional(rollbackFor = Exception.class)
    public Boolean publishByModelCode(String modelCode, List<String> selectedBids) {
        // 查询对象信息
        CfgObjectVo cfgObjectVo = cfgObjectAppService.getByModelCode(modelCode);
        Assert.notNull(cfgObjectVo, "对象不存在");
        //获取对象的属性列表,校验属性是否已经发布
        List<CfgObjectAttributeVo> fullAttributes = cfgObjectAppService.listAttrsByModelCode(modelCode);
        Set<String> publishedAttrsBids = fullAttributes.stream().filter(attribute -> Boolean.TRUE.equals(attribute.getPublished())).map(CfgObjectAttributeVo::getBid).collect(Collectors.toSet());
        selectedBids.forEach(attrBid -> {
            if (publishedAttrsBids.contains(attrBid)) {
                throw new IllegalStateException(String.format("[%s]属性已经发布", attrBid));
            }
        });
        //重新给属性赋值，防止前端传入的属性不全
        HashSet<String> selectedBidSet = Sets.newHashSet(selectedBids);
        List<CfgObjectAttributeVo> selectedObjectAttrs = fullAttributes.stream().filter(attribute -> selectedBidSet.contains(attribute.getBid())).collect(Collectors.toList());
        //查看基类是否已经发布
        String tableBid = StringUtils.isNotBlank(cfgObjectVo.getBaseModel()) ? cfgObjectVo.getBaseModel() : modelCode.substring(0, 3);
        CfgTableVo tableVo = cfgTableDomainService.getByBid(tableBid);
//        CfgObjectTableDomainService proxy = (CfgObjectTableDomainService) AopContext.currentProxy();
        boolean isBaseModelPublished = tableVo != null;
        if (isBaseModelPublished) {
            //基类已经发布，只需要更新表
            alterDBTable(tableBid, selectedObjectAttrs);
            //版本对象或者关系需要更新历史表
            if (ObjectTypeEnum.VERSION.getCode().equals(cfgObjectVo.getType()) || ObjectTypeEnum.RELATION.getCode().equals(cfgObjectVo.getType())) {
                String historyTableBid = tableBid + "_his";
                alterDBTable(historyTableBid, selectedObjectAttrs);
            }
        } else {
            //基类未发布，需要创建表
            createDBTable(tableBid, cfgObjectVo, selectedObjectAttrs);
            //版本对象或者关系对象需要创建历史表
            if (ObjectTypeEnum.VERSION.getCode().equals(cfgObjectVo.getType()) || ObjectTypeEnum.RELATION.getCode().equals(cfgObjectVo.getType())) {
                String historyTableBid = tableBid + "_his";
                createDBTable(historyTableBid, cfgObjectVo, selectedObjectAttrs);
            }
        }
        //更新对象属性发布状态
        if(CollectionUtils.isNotEmpty(selectedBids)){
            cfgObjectAttributeRepository.batchUpdatePublishStatus(selectedBids, true);
        }
        return Boolean.TRUE;
    }

    private void alterDBTable(String tableBid, List<CfgObjectAttributeVo> selectedObjectAttrs) {
        List<CfgTableAttributeDto> tableAttributeDtoList = buildTableMetaAndSave(tableBid, selectedObjectAttrs);
        //在数据库中更新表
        alterTableInDB(tableBid, tableAttributeDtoList, SsoHelper.getTenantCode());
    }

    /**
     * 添加createDBTable方法
     */
    private void createDBTable(String tableBid, CfgObjectVo cfgObjectVo, List<CfgObjectAttributeVo> selectedObjectAttrs) {
        // 构建表元数据,并保存
        CfgTableDto cfgTableDto = buildAndSaveTableMeta(tableBid, cfgObjectVo, selectedObjectAttrs);

        //在数据库中创建表
        createTableInDB(tableBid, cfgTableDto, SsoHelper.getTenantCode());
    }

    private void alterTableInDB(String tableBid, List<CfgTableAttributeDto> tableAttributeDtoList, String tenantCode) {
        TableDto tableAlterDto = new TableDto();
        CfgTableVo tableVo = cfgTableDomainService.getByBid(tableBid);
        String tableProfix = getTablePrefix(tenantCode);
        // 直接使用表名不需要拼接
        if (TableConst.USE_LOGIC_TABLE_NAME_TRUE.equals(tableVo.getUseLogicTableName())){
            tableProfix = "";
        }
        tableAlterDto.setTableName(tableProfix + tableBid);
        List<CloumnDto> cloumnDtos = tableAttributeDtoList.stream().map(this::buildDBTableColumn).collect(Collectors.toList());
        tableAlterDto.setCloumnDtos(cloumnDtos);
        tableRepository.addTableCloumns(tableAlterDto);
    }

    /**
     * 表前缀
     * @param tenantCode 租户名称
     * @return Table prefix
     */
    private static String getTablePrefix(String tenantCode) {
        return tenantCode + "_model_";
    }

    private void createTableInDB(String tableBid, CfgTableDto cfgTableDto, String tenantCode) {
        TableDto tableCreateDto = new TableDto();
        tableCreateDto.setTableName(tenantCode + "_model_" + tableBid);
        tableCreateDto.setTableDesc(cfgTableDto.getComment());
        List<CloumnDto> cloumnDtos = cfgTableDto.getAttributes().stream().filter(tableDto -> !"id".equalsIgnoreCase(tableDto.getProperty())).map(this::buildDBTableColumn).collect(Collectors.toList());
        tableCreateDto.setCloumnDtos(cloumnDtos);
        // 收集索引字段
        List<CloumnDto> createIndexs = cfgTableDto.getAttributes().stream().filter(tableDto -> Boolean.TRUE.equals(tableDto.getIndex())).map(this::buildDBTableColumn).collect(Collectors.toList());
        tableCreateDto.setCloumnIndexs(createIndexs);
        tableRepository.createTable(tableCreateDto);
    }

    private List<CfgTableAttributeDto> buildTableMetaAndSave(String tableBid, List<CfgObjectAttributeVo> selectedObjectAttrs) {
        List<CfgTableAttributeDto> tableAttributeDtoList = selectedObjectAttrs.stream().map(this::convert).collect(Collectors.toList());
        for (CfgTableAttributeDto cfgTableAttributeDto : tableAttributeDtoList) {
            cfgTableAttributeDto.setTableBid(tableBid);
        }
        cfgTableDomainService.blukAddAttribute(tableAttributeDtoList);
        return tableAttributeDtoList;
    }


    private CfgTableDto buildAndSaveTableMeta(String tableBid, CfgObjectVo cfgObjectVo, List<CfgObjectAttributeVo> selectedObjectAttrs) {
        // 构建表元数据,并保存
        CfgTableDto cfgTableDto = buildTableDto(tableBid, cfgObjectVo.getName());
        List<CfgTableAttributeDto> selectedTableAttrs = selectedObjectAttrs.stream().map(this::convert).collect(Collectors.toList());
        for (CfgTableAttributeDto cfgTableAttributeDto : selectedTableAttrs) {
            cfgTableAttributeDto.setTableBid(tableBid);
        }
        cfgTableDomainService.add(cfgTableDto);
        cfgTableDomainService.blukAddAttribute(selectedTableAttrs);

        // 构建创建表的数据
        List<CfgTableAttributeVo> cfgTableAttributeVos = cfgTableDomainService.listTableAttributesByTableBids(Lists.newArrayList(cfgObjectVo.getType(),ObjectTypeEnum.ROOT.getCode()));
        List<CfgTableAttributeDto> superTableAttrs = CfgTableAttributeConverter.INSTANCE.vos2dtos(cfgTableAttributeVos);
        List<CfgTableAttributeDto> fullTableAttrs = Lists.newArrayList();
        fullTableAttrs.addAll(superTableAttrs);
        fullTableAttrs.addAll(selectedTableAttrs);
        cfgTableDto.setAttributes(fullTableAttrs);
        return cfgTableDto;
    }

    private CloumnDto buildDBTableColumn(CfgTableAttributeDto cfgTableAttributeDto) {
        CloumnDto cloumnDto = new CloumnDto();
        cloumnDto.setCloumn(cfgTableAttributeDto.getColumnName());
        cloumnDto.setCloumnDesc(cfgTableAttributeDto.getComment());
        cloumnDto.setCloumnType(cfgTableAttributeDto.getType());
        cloumnDto.setDefaultValue(cfgTableAttributeDto.getDefaultValue());
        cloumnDto.setRequired(Boolean.TRUE.equals(cfgTableAttributeDto.getRequired()));
        return cloumnDto;
    }

    /**
     * 编写一个方法，CfgObjectAttributeVo转化成 CfgTableAttributeDto
     */
    private CfgTableAttributeDto convert(CfgObjectAttributeVo objectAttribute) {
        CfgTableAttributeDto cfgTableAttributeDto = new CfgTableAttributeDto();
        cfgTableAttributeDto.setProperty(objectAttribute.getCode());
        cfgTableAttributeDto.setColumnName(StringUtil.camelToUnderline(objectAttribute.getCode()));
        cfgTableAttributeDto.setComment(objectAttribute.getName());
        cfgTableAttributeDto.setType(Object2DBType.Obj2DBTypeMap.get(objectAttribute.getDataType()));
        cfgTableAttributeDto.setHandleType(objectAttribute.getDataType());
        cfgTableAttributeDto.setRequired(objectAttribute.getRequired());
        cfgTableAttributeDto.setDefaultValue(objectAttribute.getDefaultValue());
        cfgTableAttributeDto.setTenantId(objectAttribute.getTenantId());
        cfgTableAttributeDto.setBid(SnowflakeIdWorker.nextIdStr());
        return cfgTableAttributeDto;
    }

    private CfgTableDto buildTableDto(String tableBid, String comment) {
        CfgTableDto cfgTableDto = new CfgTableDto();
        cfgTableDto.setBid(tableBid);
        cfgTableDto.setUseLogicTableName(TableConst.USE_LOGIC_TABLE_NAME_FALSE);
        cfgTableDto.setLogicTableName(tableBid);
        cfgTableDto.setComment(comment);
        cfgTableDto.setTenantCode(SsoHelper.getTenantCode());
        return cfgTableDto;
    }

}
