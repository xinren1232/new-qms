package com.transcend.plm.configcenter.table.domain.entity;

import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import com.transcend.plm.configcenter.common.spring.PlmContextHolder;
import com.transcend.plm.configcenter.object.domain.entity.CfgObject;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.table.infrastructure.repository.CfgTableRepository;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTableAttributePo;
import com.transcend.plm.configcenter.table.pojo.CfgTableAttributeConverter;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAttributeVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/15 16:55
 * @since 1.0
 */
@Slf4j
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CfgTable extends BasePoEntity {


    @ApiModelProperty("逻辑表名称")
    private String logicTableName;

    @ApiModelProperty("策略编码")
    private String strategyCode;

    @ApiModelProperty("分片字段")
    private String shardColumn;

    @ApiModelProperty("分片数量")
    private String tableNum;

    @ApiModelProperty("策略规则")
    private String rule;

    @ApiModelProperty("直接使用表名")
    private Byte useLogicTableName = 0;

    @ApiModelProperty("表属性列表")
    private List<CfgTableAttributeVo> attributes;

    private CfgTableRepository cfgTableRepository = PlmContextHolder.getBean(CfgTableRepository.class);

    public CfgTable(String logicTableName, String strategyCode, String shardColumn,
                    String tableNum, String rule, Byte useLogicTableName, List<CfgTableAttributeVo> attributes) {
        this.logicTableName = logicTableName;
        this.strategyCode = strategyCode;
        this.shardColumn = shardColumn;
        this.tableNum = tableNum;
        this.rule = rule;
        this.useLogicTableName = useLogicTableName;
        this.attributes = attributes;
    }

    /**
     * 构建getOne ObjectModel
     */
    public static CfgTable saveOrUpdate(String modelCode) {
        CfgObjectVo cfgObjectVo = CfgObject.buildWithModelCode(modelCode).populateBaseInfoByModelCode().build();
        String tableBid = cfgObjectVo.getTableBid();
//        CfgTable table = new CfgTable();
//        table.setBid(tableBid);
//        return table;
        return null;
    }

    /**
     * 构建getOne ObjectModel
     */
    public static CfgTable buildWithModelCode(String modelCode) {
        CfgObjectVo cfgObjectVo = CfgObject.buildWithModelCode(modelCode).populateBaseInfoByModelCode().build();
        String tableBid = cfgObjectVo.getTableBid();
//        CfgTable table = new CfgTable();
//        table.setBid(tableBid);
//        return table;
        return null;
    }

    /**
     * 填充基本信息
     * @return CfgTable
     */
    public CfgTable populateBaseInfo() {
        CfgTableVo tableVo = cfgTableRepository.getByBid(this.getBid());
        return CfgTableAttributeConverter.INSTANCE.vo2entity(tableVo);
    }

    /**
     * 填充属性
     * @return CfgTable实体
     */
    public CfgTable populateAttributes() {
        List<CfgTableAttributePo> cfgTableAttributePos =
                cfgTableRepository.listTableAttributesByTableBid(this.getBid());
        List<CfgTableAttributeVo> cfgTableAttributeVos = CfgTableAttributeConverter.INSTANCE.pos2vos(cfgTableAttributePos);
        setAttributes(cfgTableAttributeVos);
        return this;
    }

    /**
     * 构建VO
     * @return CfgTableVo
     */
    public CfgTableVo build() {
        return CfgTableAttributeConverter.INSTANCE.entity2vo(this);
    }

}
