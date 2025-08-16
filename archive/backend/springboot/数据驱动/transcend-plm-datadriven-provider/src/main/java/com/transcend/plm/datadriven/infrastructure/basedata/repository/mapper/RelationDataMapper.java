package com.transcend.plm.datadriven.infrastructure.basedata.repository.mapper;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.Order;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.api.model.mata.RelationCrossUpQo;
import com.transcend.plm.datadriven.api.model.relation.qo.CrossRelationPathChainQueryQO;
import com.transcend.plm.datadriven.api.model.relation.vo.RelationAndTargetVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface RelationDataMapper {
    /**
     * listRelationDataFloat
     *
     * @param relationTable         relationTable
     * @param targetTable           targetTable
     * @param relationQueryWrappers relationQueryWrappers
     * @param targetQueryWrappers   targetQueryWrappers
     * @return {@link List<RelationAndTargetVo>}
     */
    List<RelationAndTargetVo> listRelationDataFloat(@Param("relationTable") TableDefinition relationTable, @Param("targetTable") TableDefinition targetTable,
                                                    @Param("relationQueryWrappers") List<QueryWrapper> relationQueryWrappers, @Param("targetQueryWrappers") List<QueryWrapper> targetQueryWrappers);

    /**
     * listRelationDataFixed
     *
     * @param relationTable         relationTable
     * @param targetTable           targetTable
     * @param relationQueryWrappers relationQueryWrappers
     * @param targetQueryWrappers   targetQueryWrappers
     * @return {@link List<RelationAndTargetVo>}
     */
    List<RelationAndTargetVo> listRelationDataFixed(@Param("relationTable") TableDefinition relationTable, @Param("targetTable") TableDefinition targetTable,
                                                    @Param("relationQueryWrappers") List<QueryWrapper> relationQueryWrappers, @Param("targetQueryWrappers") List<QueryWrapper> targetQueryWrappers);

    /**
     * 自下向上寻址的跨层级查询源对象的实例集合 强浮动以bid作为关联
     *
     * @param relationCrossUpQo relationCrossUpQo
     * @param wrappers          wrappers
     * @param orderQueries      orderQueries
     * @return {@link List<MObject>}
     */
    List<MObject> listPageCrossHierarchyUp(@Param("r") RelationCrossUpQo relationCrossUpQo,
                                           @Param("queryWrappers") List<QueryWrapper> wrappers,
                                           @Param("orderQueries") List<Order> orderQueries);

    /**
     * relationListPageByPathChain
     *
     * @param pathChainQueryQO pathChainQueryQO
     * @return {@link List<MObject>}
     */
    List<MObject> relationListPageByPathChain(@Param("r") CrossRelationPathChainQueryQO pathChainQueryQO,
                                              @Param("orderQueries") List<Order> orderQueries);
}
