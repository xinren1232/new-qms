package com.transcend.plm.datadriven.core.action;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.domain.object.base.UpdateRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Api(value = "UpdateRecordController", tags = "修改记录-个性化配置")
@Validated
@RestController
@RequestMapping("/data-driven/api/update-record")
public class UpdateRecordController {
    @Resource
    private UpdateRecordService updateRecordService;

    /**
     * @param modelCode
     * @return {@link TranscendApiResponse }<{@link List }<{@link MBaseData }>>
     */
    @GetMapping("/listUpdateRecord/{modelCode}")
    public TranscendApiResponse<List<MBaseData>> listUpdateRecord(@ApiParam("模型编码") @PathVariable String modelCode){
        return TranscendApiResponse.success(updateRecordService.listUpdateRecord(modelCode));
    }
}
