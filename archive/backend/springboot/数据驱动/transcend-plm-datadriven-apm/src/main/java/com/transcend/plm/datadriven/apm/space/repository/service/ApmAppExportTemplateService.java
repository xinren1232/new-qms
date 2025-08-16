package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppExportTemplateDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmAppExportTemplateVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppExportTemplatePo;

import java.util.List;

/**
 * @author shu.zhang
 * @version 1.0
 * @className ApmAppExportTemplateService
 * @description desc
 * @date 2024/5/29 17:35
 */
public interface ApmAppExportTemplateService extends IService<ApmAppExportTemplatePo> {

    /**
     * 保存ApmAppExportTemplateDto对象，并返回保存后的ApmAppExportTemplateVo对象。
     *
     * @param dto 要保存的ApmAppExportTemplateDto对象
     * @return 保存后的ApmAppExportTemplateVo对象
     */
    ApmAppExportTemplateVo save(ApmAppExportTemplateDto dto);

    /**
     * 更新给定的ApmAppExportTemplateDto对象，并返回更新后的ApmAppExportTemplateVo对象。
     *
     * @param dto 要更新的ApmAppExportTemplateDto对象
     * @return 更新后的ApmAppExportTemplateVo对象
     */
    ApmAppExportTemplateVo update(ApmAppExportTemplateDto dto);

    /**
     * 通过业务ID（bid），逻辑删除ApmAppExportTemplate实体对象。
     *
     * @param bid 业务ID（bid）用于唯一标识ApmAppExportTemplate实体对象
     * @return 如果成功逻辑删除实体对象，则返回true；如果失败，则返回false
     */
    boolean logicalDeleteByBid(String bid);

    /**
     * 根据条件查询ApmAppExportTemplateVo列表。
     *
     * @param apmAppExportTemplateDto 查询条件对象
     * @return 匹配查询条件的ApmAppExportTemplateVo列表
     */
    List<ApmAppExportTemplateVo> queryByCondition(ApmAppExportTemplateDto apmAppExportTemplateDto);

    /**
     * 通过业务ID（bid）获取ApmAppExportTemplateVo对象。
     *
     * @param bid 业务ID（bid），用于唯一标识ApmAppExportTemplate实体对象
     * @return 根据业务ID（bid）获取的ApmAppExportTemplateVo对象，如果不存在则返回null
     */
    ApmAppExportTemplateVo getByBid(String bid);
}
