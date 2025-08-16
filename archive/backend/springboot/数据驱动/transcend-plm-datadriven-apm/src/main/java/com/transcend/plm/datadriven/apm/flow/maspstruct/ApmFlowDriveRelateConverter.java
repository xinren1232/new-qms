package com.transcend.plm.datadriven.apm.flow.maspstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowDriveRelateAO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowDriveRelate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程驱动关系转换器
 * @createTime 2023-10-26 10:02:00
 */
@Mapper
public interface ApmFlowDriveRelateConverter {
    ApmFlowDriveRelateConverter INSTANCE = Mappers.getMapper(ApmFlowDriveRelateConverter.class);

    /**
     * 对给定的 ApmFlowDriveRelateAO 对象进行转换，转换成 ApmFlowDriveRelate 对象。
     *
     * @param apmFlowDriveRelateAO 要转换的 ApmFlowDriveRelateAO 对象
     * @return 转换后的 ApmFlowDriveRelate 对象
     */
    ApmFlowDriveRelate ao2Entity(ApmFlowDriveRelateAO apmFlowDriveRelateAO);
}
