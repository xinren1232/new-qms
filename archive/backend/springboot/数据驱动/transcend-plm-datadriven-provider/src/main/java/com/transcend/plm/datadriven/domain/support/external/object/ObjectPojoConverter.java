package com.transcend.plm.datadriven.domain.support.external.object;

import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.api.model.config.ObjectVo;
import com.transcend.plm.datadriven.api.model.vo.DraftVO;
import com.transcend.plm.datadriven.infrastructure.draft.po.DraftPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Mapper
public interface ObjectPojoConverter {

    ObjectPojoConverter INSTANCE = Mappers.getMapper(ObjectPojoConverter.class);

    /**
     * cfg2definition
     *
     * @param dto
     * @return
     */
    ObjectVo cfg2definition(CfgObjectVo dto);

    /**
     * DfratVO2DraftPO
     *
     * @param draftVO
     * @return {@link DraftPO }
     */
    DraftPO draftVO2DraftPO(DraftVO draftVO);


}
