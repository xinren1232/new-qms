package com.transcend.plm.datadriven.apm.mapstruct;

import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmObjectRelationAppVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Mickey Qiu
 * @desc
 * @date 2025/3/15
 */
@Mapper
public interface ApmRelationConverter {

    ApmRelationConverter INSTANCE = Mappers.getMapper(ApmRelationConverter.class);


    /**
     * 将ApmSpaceAppTab对象转换为ApmObjectRelationAppVo对象。
     *
     * @param apmSpaceAppTab 需要转换的ApmSpaceAppTab对象
     * @return {@link ApmObjectRelationAppVo}
     */
//    @Mappings({
//            @Mapping(target = "checked", expression = "java(Boolean.TRUE.equals(apmSpaceAppTab.getEnableFlag())")
//    })
//    ApmObjectRelationAppVo appTabToRelationAppVo(ApmSpaceAppTab apmSpaceAppTab);

}
