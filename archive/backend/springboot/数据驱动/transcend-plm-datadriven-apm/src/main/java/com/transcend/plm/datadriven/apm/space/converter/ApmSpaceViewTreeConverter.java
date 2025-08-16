package com.transcend.plm.datadriven.apm.space.converter;

import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceViewTreeVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpace;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视图树Vo对象转换器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/19 15:36
 */
@Mapper
public interface ApmSpaceViewTreeConverter {
    ApmSpaceViewTreeConverter INSTANCE = Mappers.getMapper(ApmSpaceViewTreeConverter.class);

    /**
     * 转换为vo对象
     *
     * @param apmSpace 空间
     * @return 视图树Vo对象
     */
    @Mapping(target = "defaultSpace", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "apmSpaceAppVoList", ignore = true)
    ApmSpaceViewTreeVo spacePo2vo(ApmSpace apmSpace);

    /**
     * 转换为vo对象
     *
     * @param apmSpaceApp 应用
     * @return 视图树Vo对象
     */
    @Mapping(target = "children", ignore = true)
    ApmSpaceViewTreeVo.ApmSpaceAppViewTreeVo spaceAppPo2vo(ApmSpaceApp apmSpaceApp);

    /**
     * 转换为vo对象
     *
     * @param spaceList    空间列表
     * @param spaceAppList 空间应用列表
     * @param viewList     视图列表
     * @return 视图树Vo对象列表
     */
    default List<ApmSpaceViewTreeVo> toVoList(List<ApmSpace> spaceList, List<ApmSpaceApp> spaceAppList,
                                              List<CfgViewVo> viewList) {
        Map<String, List<CfgViewVo>> viewMap = viewList.stream().collect(Collectors.groupingBy(CfgViewVo::getBelongBid));
        Map<String, List<ApmSpaceViewTreeVo.ApmSpaceAppViewTreeVo>> spaceAppMap = spaceAppList.stream().map(apmSpaceApp -> {
            ApmSpaceViewTreeVo.ApmSpaceAppViewTreeVo vo = spaceAppPo2vo(apmSpaceApp);
            vo.setChildren(viewMap.get(apmSpaceApp.getBid()));
            return vo;
        }).filter(vo -> vo.getChildren() != null).collect(Collectors.groupingBy(ApmSpaceAppVo::getSpaceBid));

        return spaceList.stream().map(apmSpace -> {
            ApmSpaceViewTreeVo vo = spacePo2vo(apmSpace);
            vo.setChildren(spaceAppMap.get(apmSpace.getBid()));
            return vo;
        }).filter(vo -> vo.getChildren() != null).collect(Collectors.toList());
    }
}
