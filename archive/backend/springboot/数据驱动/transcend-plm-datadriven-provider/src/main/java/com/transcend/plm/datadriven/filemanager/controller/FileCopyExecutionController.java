package com.transcend.plm.datadriven.filemanager.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.filemanager.pojo.ao.FileCopyExecutionAo;
import com.transcend.plm.datadriven.filemanager.pojo.vo.FileCopyExecutionVo;
import com.transcend.plm.datadriven.filemanager.service.FileCopyExecutionRecordService;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author bin.yin
 * @date 2024/04/30 15:20
 */
@Api(value = "FileCopyExecution Controller", tags = "文件复制执行-控制器")
@RestController
@RequestMapping("/file/copy/execution")
public class FileCopyExecutionController {
    @Resource
    private FileCopyExecutionRecordService fileCopyExecutionRecordService;


    /**
     * @param request
     * @return {@link TranscendApiResponse }<{@link PagedResult }<{@link FileCopyExecutionVo }>>
     */
    @PostMapping("/queryPageByCondition")
    @ApiOperation("分页查询文件复制执行")
    public TranscendApiResponse<PagedResult<FileCopyExecutionVo>> queryPageByCondition(@RequestBody BaseRequest<FileCopyExecutionAo> request) {
        return TranscendApiResponse.success(fileCopyExecutionRecordService.queryPageByCondition(request));
    }

    /**
     * @param recordBids
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @PostMapping("/batchExecute")
    @ApiOperation("文件复制批量执行")
    public TranscendApiResponse<Boolean> batchExecute(@RequestBody List<String> recordBids) {
        return TranscendApiResponse.success(fileCopyExecutionRecordService.batchExecute(recordBids));
    }
}
