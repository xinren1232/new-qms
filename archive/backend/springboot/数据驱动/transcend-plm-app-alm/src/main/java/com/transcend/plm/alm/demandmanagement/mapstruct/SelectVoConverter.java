package com.transcend.plm.alm.demandmanagement.mapstruct;

import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transsion.framework.common.ObjectUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/06/21 17:06
 */
@Mapper
public interface SelectVoConverter {
    SelectVoConverter INSTANCE = Mappers.getMapper(SelectVoConverter.class);

    /**
     * 将MObjectTree对象转换为SelectVo对象的方法。
     *
     * @param mObjectTree 要转换的MObjectTree对象
     * @return 转换后的SelectVo对象
     */
    @Mappings({
            @Mapping(target = "domainLeader", source = "mObjectTree", qualifiedByName = "domainLeader"),
            @Mapping(target = "domainSe", source = "mObjectTree", qualifiedByName = "domainSe"),
            @Mapping(target = "spaceAppBid", source = "mObjectTree", qualifiedByName = "spaceAppBid"),
            @Mapping(target = "nodeId", source = "mObjectTree", qualifiedByName = "nodeId"),
            @Mapping(target = "nodeParentId", source = "mObjectTree", qualifiedByName = "nodeParentId"),
            @Mapping(target = "disabled", source = "mObjectTree", qualifiedByName = "disabled")
    })
    SelectVo toSelectVo(MObjectTree mObjectTree);

    /**
     * 将MObjectTree列表转换为SelectVo列表。
     *
     * @param treeList MObjectTree列表
     * @return SelectVo列表
     */
    List<SelectVo> mObjectTree2Vo(List<MObjectTree> treeList);

    /**
     * 返回MObjectTree对象中与DOMAIN_LEADER代码关联的值。
     *
     * @param mObjectTree 要检索值的MObjectTree对象
     * @return 与DOMAIN_LEADER代码关联的值
     */
    @Named("domainLeader")
    default String domainLeader(MObjectTree mObjectTree) {
        return (String) mObjectTree.get(DemandManagementEnum.DOMAIN_LEADER.getCode());
    }

    /**
     * 从给定的MObjectTree对象中返回与DOMAIN_SE代码关联的值。
     *
     * @param mObjectTree 要检索值的MObjectTree对象
     * @return 与DOMAIN_SE代码关联的值
     */
    @Named("domainSe")
    default Object domainSe(MObjectTree mObjectTree) {
        return mObjectTree.get(DemandManagementEnum.DOMAIN_SE.getCode());
    }

    @Named("nodeId")
    default String nodeId(MObjectTree mObjectTree) {
        return String.valueOf(mObjectTree.get("nodeId"));
    }

    @Named("nodeParentId")
    default String nodeParentId(MObjectTree mObjectTree) {
        return ObjectUtil.isEmpty(mObjectTree.get("nodeParentId")) ? "0" : String.valueOf(mObjectTree.get("nodeParentId"));
    }

    /**
     * 返回给定MObjectTree对象中与SpaceAppDataEnum.SPACE_APP_BID.getCode()关联的值。
     *
     * @param mObjectTree 要检索值的MObjectTree对象
     * @return 与SpaceAppDataEnum.SPACE_APP_BID.getCode()关联的值
     */
    @Named("spaceAppBid")
    default String spaceAppBid(MObjectTree mObjectTree) {
        return (String) mObjectTree.get(SpaceAppDataEnum.SPACE_APP_BID.getCode());
    }

    /**
     * 返回MObjectTree对象中的disabled属性值。
     *
     * @param mObjectTree 要获取disabled属性的MObjectTree对象
     * @return MObjectTree对象的disabled属性值
     */
    @Named("disabled")
    default Boolean disabled(MObjectTree mObjectTree) {
        return (Boolean) mObjectTree.get("disabled");
    }

}
