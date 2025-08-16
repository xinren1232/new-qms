package com.transcend.plm.datadriven.filemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.filemanager.pojo.ao.FileCopyExecutionAo;
import com.transcend.plm.datadriven.filemanager.pojo.po.FileCopyExecutionRecordPo;
import com.transcend.plm.datadriven.filemanager.pojo.vo.FileCopyExecutionVo;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;

/**
 * @author bin.yin
 * @date 2024/07/24
 */
public interface FileCopyExecutionRecordService extends IService<FileCopyExecutionRecordPo> {

    /**
     * 查询分页列表
     *
     * @param request {@link BaseRequest }
     * @return {@link PagedResult }<{@link FileCopyExecutionVo }>
     */
    PagedResult<FileCopyExecutionVo> queryPageByCondition(BaseRequest<FileCopyExecutionAo> request);

    /**
     * 批量执行
     *
     * @param recordBids recordBids
     * @return {@link Boolean }
     */
    Boolean batchExecute(List<String> recordBids);
}
