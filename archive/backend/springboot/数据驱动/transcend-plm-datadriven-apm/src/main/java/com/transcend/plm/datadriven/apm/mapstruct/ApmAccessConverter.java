package com.transcend.plm.datadriven.apm.mapstruct;


import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmAccessAO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmAccess;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmAccessVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmAccessConverter {
    ApmAccessConverter INSTANCE = Mappers.getMapper(ApmAccessConverter.class);


    /**
     * 将ApmAccessAO对象转换为ApmAccess对象。
     *
     * @param apmAccessAO 需要转换的ApmAccessAO对象
     * @return 转换后的ApmAccess对象
     */
    ApmAccess ao2Entity(ApmAccessAO apmAccessAO);

    /**
     * 将ApmAccess对象转换为ApmAccessVO对象。
     *
     * @param apmAccess 需要转换的ApmAccess对象
     * @return 转换后的ApmAccessVO对象
     */
    ApmAccessVO entity2VO(ApmAccess apmAccess);

    /**
     * 将Entity对象列表转换为VO对象列表。
     *
     * @param apmAccesses 需要转换的Entity对象列表
     * @return 转换后的VO对象列表
     */
    List<ApmAccessVO> entityList2VOList(List<ApmAccess> apmAccesses);
}
