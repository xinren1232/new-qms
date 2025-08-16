package com.transcend.plm.datadriven.apm.mapstruct;


import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppAo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceObjectVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmSpaceAppConverter {
    ApmSpaceAppConverter INSTANCE = Mappers.getMapper(ApmSpaceAppConverter.class);

    /**
     * 方法描述
     *
     * @param apmSpaceAppAo apmSpaceAppAo
     * @return 返回值
     */
    ApmSpaceApp ao2Entity(ApmSpaceAppAo apmSpaceAppAo);

    /**
     * 方法描述
     *
     * @param apmSpaceApp apmSpaceApp
     * @return 返回值
     */
    ApmSpaceAppVo po2vo(ApmSpaceApp apmSpaceApp);

    /**
     * 方法描述
     *
     * @param apmSpaceAppAoList apmSpaceAppAoList
     * @return 返回值
     */
    List<ApmSpaceApp> aoList2EntityList(List<ApmSpaceAppAo> apmSpaceAppAoList);

    /**
     * 方法描述
     *
     * @param apmSpaceAppList apmSpaceAppList
     * @return 返回值
     */
    List<ApmSpaceAppVo> entityList2VoList(List<ApmSpaceApp> apmSpaceAppList);

    /**
     * 方法描述
     *
     * @param cfgObjectVo cfgObjectVo
     * @return 返回值
     */
    ApmSpaceObjectVo cfgObjectVo2ApmSpaceObjectVo(CfgObjectVo cfgObjectVo);
}
