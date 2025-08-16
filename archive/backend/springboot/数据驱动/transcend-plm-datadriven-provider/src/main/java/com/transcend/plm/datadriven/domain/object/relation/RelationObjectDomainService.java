package com.transcend.plm.datadriven.domain.object.relation;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.domain.object.base.RelationModelDomainService;
import com.transcend.plm.datadriven.domain.object.base.VersionModelDomainService;
import com.transcend.plm.datadriven.api.model.MRelationObject;
import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.api.model.relation.add.BatchRelationAdd;
import com.transcend.plm.datadriven.api.model.relation.add.RelationAdd;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationQo;
import com.transcend.plm.datadriven.api.model.relation.vo.RelationAndTargetVo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 处理关系和关系的关联数据
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
@Component
public class RelationObjectDomainService {
    @Resource
    private VersionModelDomainService versionModelDomainService;

    @Resource
    private RelationModelDomainService relationModelDomainService;


    /**
     * 批量绑定关系    TODO
     *
     * @param adds
     * @return MBaseData
     */
    public Boolean batchBindingRelation(BatchRelationAdd adds) {
        // 校验参数的完整性
        //

        return null;
    }

    public MRelationObject bindingRelation(RelationAdd add) {
        String relationConfigBid = add.getModelCode();
        MVersionObject sourceData = add.getSourceData();
        MRelationObject relationData = add.getRelationData();
        MVersionObject targetData = add.getTargetData();
        // 1.通过配置relationConfigBid,获取到对应的源和目标对象
        // 2.获取 源和目标的baseModel得到 transcend_relation_{source}_ref_{target}
        // 3.存储 sourceData的bid，与dataBid,作为关系表中的sourceBid,sourceDataBid,
        //   存储 targetData的bid，与dataBid,作为关系表中的targetBid,targetDataBid
        //   生产新的关系bid,如果relationData存在数据，则需要存储transcend_model_{relationModelCode}
        // 从relationConfigBid 中获取到关系得modelCode
        String relationModelCode = "";
        relationModelDomainService.add(relationModelCode, relationData);
        return null;
    }

    /**
     * 新增关系    TODO
     *
     * @return MBaseData
     */
    public MRelationObject addRelation(RelationAdd add) {
        String relationConfigBid = add.getModelCode();
        MVersionObject sourceData = add.getSourceData();
        MRelationObject relationData = add.getRelationData();
        MVersionObject targetData = add.getTargetData();
        // 1.通过配置relationConfigBid,获取到对应的源和目标对象
        // 1.1. 如果存在targetData，需要调用目标对象新增
        String targetModelCode = targetData.getModelCode();
        versionModelDomainService.add(targetModelCode, targetData);
        // 2.获取 源和目标的baseModel得到 transcend_relation_{source}_ref_{target}
        // 3.存储 sourceData的bid，与dataBid,作为关系表中的sourceBid,sourceDataBid,
        //   存储 targetData的bid，与dataBid,作为关系表中的targetBid,targetDataBid
        //   生产新的关系bid,如果relationData存在数据，则需要存储transcend_model_{relationModelCode}
        // 从relationConfigBid 中获取到关系得modelCode
        String relationModelCode = "";
        relationModelDomainService.add(relationModelCode, relationData);
        return null;
    }

    /**
     * 批量新增关系    TODO
     *
     * @param adds
     * @return MBaseData
     */
    public Boolean batchAddRelation(BatchRelationAdd adds) {
        // 校验参数的完整性
        //

        return null;
    }



    /**
     * TODO
     *
     * 根据modelCode获取当前关系是浮动还是固定/以及获取源对象当前生命周期是固定还是浮动
     * 规则：硬关系行为>生命周期行为>软关系行为
     * 1.如果是固定关系，则
     * 与历史表的bid与关系表的target_bid 关联查询
     * 2.如果是浮动关系，则
     * 与非历史表的data_bid与关系表的target_data_bid 关联查询
     SELECT
     st.*, relation.*, target.*
     FROM
     transcend_relation_source_ref_target st
     LEFT JOIN transcend_model_a03 relation ON st.bid = relation.bid
     AND relation_config_bid = '1'
     AND st.source_bid = '1'
     LEFT JOIN transcend_model_demo target ON target.bid = st.target_bid
     WHERE target.model_code = 'A03T92003'
     *
     * @return List<MBaseData>
     */
    public List<RelationAndTargetVo> listRelation(RelationQo relationQo) {
            // 按照注释得SQL 处理即可
        return null;
    }

    /**
     * 查询关系-根据固定
     * 与历史表的bid与关系表的target_bid 关联查询
     * @param relationQo
     * @return
     */
    public List<RelationAndTargetVo> listRelationByFixed(RelationQo relationQo) {
        // 按照注释得SQL 处理即可
        return null;
    }

    /**
     * 查询关系-根据浮动
     * 与非历史表的data_bid与关系表的target_data_bid 关联查询
     * @param relationQo
     * @return
     */
    public List<RelationAndTargetVo> listRelationByFloat(RelationQo relationQo) {
        // 按照注释得SQL 处理即可
        return null;
    }

    /**
     * TODO
     SELECT
     st.*, relation.*, target.*
     FROM
     transcend_relation_source_ref_target st
     LEFT JOIN transcend_model_a03 relation ON st.bid = relation.bid
     AND relation_config_bid = '1'
     AND st.source_bid = '1'
     LEFT JOIN transcend_model_demo target ON target.bid = st.target_bid
     WHERE target.model_code = 'A03T92003'
     *
     * @return List<MBaseData>
     */
    public PagedResult<RelationAndTargetVo> pageRelation(RelationQo relationQo) {
        // 按照注释得SQL 处理即可，用pageHelper处理集合
        return null;
    }

    /**
     SELECT
     st.*, relation.* , target.*
     FROM
     transcend_relation_source_ref_target st
     LEFT JOIN transcend_model_a03 relation ON st.bid = relation.bid
     AND relation_config_bid = '1'
     AND st.source_bid = '1'
     LEFT JOIN transcend_model_demo target ON target.bid = st.target_bid
     WHERE target.model_code = 'A03T92003' AND relation.bid = '1' LIMIT 1
     *
     * @return MBaseData
     */
    public RelationAndTargetVo getByBid(String modelCode, String bid) {
        // 与listRelation类似，补充AND relation.bid = '1' LIMIT 1即可
        return null;
    }



}
