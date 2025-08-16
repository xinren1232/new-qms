package com.transcend.plm.configcenter.table.infrastructure.repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transcend.framework.common.util.StringUtil;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.table.qo.CfgTableQo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAttributeVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.common.config.CommonProperties;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.table.infrastructure.repository.mapper.CfgTableAttributeMapper;
import com.transcend.plm.configcenter.table.infrastructure.repository.mapper.CfgTableMapper;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTableAttributePo;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTablePo;
import com.transcend.plm.configcenter.table.pojo.CfgTableAttributeConverter;
import com.transcend.plm.configcenter.table.pojo.CfgTableConverter;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableAttributeDto;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableDto;
import com.transcend.plm.configcenter.table.pojo.dto.CloumnDto;
import com.transcend.plm.configcenter.table.pojo.dto.TableDto;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 动态模型资源库
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2021/9/22 17:21
 * @since 1.0
 */
@Slf4j
@Repository
public class CfgTableRepository {

    @Resource
    private CfgTableService cfgTableService;
    @Resource
    private CommonProperties commonProperties;

    @Resource
    private CfgTableAttributeService cfgTableAttributeService;

    @Resource
    private CfgTableAttributeMapper cfgTableAttributeMapper;

    @Resource
    private CfgTableMapper cfgTableMapper;

    public PagedResult<CfgTableVo> page(BaseRequest<CfgTableQo> pageQo) {
        return cfgTableService.pageByQo(pageQo);
    }

    public CfgTableVo getByBid(String bid) {
        CfgTablePo po = cfgTableService.getByBid(bid);
        return CfgTableConverter.INSTANCE.po2vo(po);
    }

    public CfgTableVo add(CfgTableDto dto) {
        CfgTablePo dictionaryPo = CfgTableConverter.INSTANCE.dto2po(dto);
        cfgTableService.save(dictionaryPo);
        return CfgTableConverter.INSTANCE.po2vo(dictionaryPo);
    }

    public CfgTableVo update(CfgTableDto dto) {
        CfgTablePo dictionaryPo = CfgTableConverter.INSTANCE.dto2po(dto);
        cfgTableService.updateByBid(dictionaryPo);
        return CfgTableConverter.INSTANCE.po2vo(dictionaryPo);
    }

    public boolean blukAddAttribute(List<CfgTableAttributePo> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return false;
        }
        return cfgTableAttributeService.saveBatch(pos);
    }

    public int blukUpdateAttribute(List<CfgTableAttributeDto> dictionaryItems) {
        if (CollectionUtils.isEmpty(dictionaryItems)) {
            return 0;
        }
        List<CfgTableAttributePo> pos = CfgTableAttributeConverter.INSTANCE.dtos2pos(dictionaryItems);
        return cfgTableAttributeMapper.blukUpdateByBid(pos);
    }

    public List<CfgTableAttributePo> listTableAttributesByTableBid(String tableBid) {
        return cfgTableAttributeMapper.listTableAttributeByTableBids(Sets.newHashSet(tableBid), null);
    }

    public List<CfgTableAttributePo> listTableAttributesByTableBids(List<String> tableBids) {
        return cfgTableAttributeMapper.listTableAttributeByTableBids(Sets.newHashSet(tableBids), null);
    }

    public CfgTableVo getTableByTableName(String code) {
        CfgTablePo po = cfgTableMapper.getByTableName(code);
        if (po == null) {
            return null;
        }
        return CfgTableConverter.INSTANCE.po2vo(po);
    }

    public Boolean logicalDeleteByBid(String bid) {
        return cfgTableService.logicalDeleteByBid(bid);
    }

    public List<CfgTableVo> listByTableNamesAndEnableFlags(@NotNull List<String> codes, List<Integer> enableFlags) {
        // codes 不能为空
        if (CollectionUtils.isEmpty(codes)) {
            throw new BusinessException(ErrorMsgEnum.DICT_ERROR_CODES_NOT_NULL.getCode(),
                    ErrorMsgEnum.DICT_ERROR_CODES_NOT_NULL.getDesc());
        }
        // codes 数量限制为1000
        if (commonProperties.getDbInLimit() < codes.size()) {
            throw new BusinessException(ErrorMsgEnum.DICT_ERROR_CODES_LIMIT.getCode(),
                    ErrorMsgEnum.DICT_ERROR_CODES_LIMIT.getDesc());
        }
        List<CfgTablePo> pos = cfgTableMapper.listByByTableNamesAndEnableFlags(codes, enableFlags);
        // 转换
        return CfgTableConverter.INSTANCE.pos2vos(pos);
    }

    public List<CfgTableAttributeVo> listDictionaryItemByDictionaryBids(Set<String> dictioanryBids) {
        List<CfgTableAttributePo> pos = cfgTableAttributeMapper.listTableAttributeByTableBids(dictioanryBids, null);
        return CfgTableAttributeConverter.INSTANCE.pos2vos(pos);
    }

    /**
     * 动态创建表
     *
     * @param tableCreateDto
     * @return
     */
    public void createTable(TableDto tableCreateDto){
        if(CollectionUtils.isEmpty(tableCreateDto.getCreateCloumns())){
            tableCreateDto.setCreateCloumns(getCloumnStr(tableCreateDto.getCloumnDtos()));
        }
        if(CollectionUtils.isNotEmpty(tableCreateDto.getCloumnIndexs())){
            tableCreateDto.setCreateIndexs(getIndexStr(tableCreateDto.getCloumnIndexs()));
        }
        if(StringUtil.isBlank(tableCreateDto.getTableDesc())){
            tableCreateDto.setTableDesc("'"+tableCreateDto.getTableName()+"'");
        }else{
            tableCreateDto.setTableDesc("'"+tableCreateDto.getTableDesc()+"'");
        }
        cfgTableMapper.createTable(tableCreateDto);
    }

    private List<String> getCloumnStr(List<CloumnDto> cloumnDtos){
        List<String> createCloumns = new ArrayList<>();
        // Fixed by WeiQiang 2024-09-11 17:53 发布对象，基类属性与自身私有属性重复， 用来去重，防止创建表的时候列名重复
        Set<String> cloumnSet = new HashSet<>();
        for(CloumnDto cloumnDto:cloumnDtos){
            String columnName = cloumnDto.getCloumn();
            // Fixed by WeiQiang 2024-09-11 17:53 如果已经添加到创建表的语句里面了，则跳过。
            if(cloumnSet.contains(columnName)){
                continue;
            }
            StringBuffer sb = new StringBuffer();
            sb.append("`").append(columnName).append("` ").append(cloumnDto.getCloumnType());
            if(cloumnDto.isRequired()){
                sb.append(" NOT NULL ");
            }
            if(StringUtil.isNotBlank(cloumnDto.getDefaultValue())){
                sb.append(" DEFAULT ").append(cloumnDto.getDefaultValue());
            }
            if(StringUtil.isNotBlank(cloumnDto.getCloumnDesc())){
                sb.append(" COMMENT '").append(cloumnDto.getCloumnDesc()).append("'");
            }
            cloumnSet.add(columnName);
            createCloumns.add(sb.toString());
        }
        return createCloumns;
    }

    /**
     * KEY `idx_bid` (`bid`) USING BTREE,
     * @param cloumnIndexs
     * @return
     */
    private List<String> getIndexStr(List<CloumnDto> cloumnIndexs){
        if(CollectionUtils.isEmpty(cloumnIndexs)){
            return Lists.newArrayList();
        }
        return cloumnIndexs.stream().map(CloumnDto::getCloumn)
                .map(column -> {
                    //注意：特殊逻辑，为防止bid重复，bid使用索引时为唯一索引
                    String uniqueIdxColumnName = "bid";
                    if (uniqueIdxColumnName.equalsIgnoreCase(column)) {
                        return String.format("UNIQUE KEY `uk_%s` (`%s`) ", column, column);
                    }
                    return String.format("KEY `idx_%s` (`%s`) USING BTREE ", column, column);
                }).collect(Collectors.toList());
    }

    /**
     * 动态添加表字段
     * @param tableCreateDto
     */
    public void addTableCloumns(TableDto tableCreateDto){
        if(CollectionUtils.isEmpty(tableCreateDto.getCreateCloumns())){
            tableCreateDto.setCreateCloumns(getCloumnStr(tableCreateDto.getCloumnDtos()));
        }
        cfgTableMapper.addTableCloumns(tableCreateDto);
    }
}
