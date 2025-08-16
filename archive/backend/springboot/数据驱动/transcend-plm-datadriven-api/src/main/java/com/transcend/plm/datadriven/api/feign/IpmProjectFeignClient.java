package com.transcend.plm.datadriven.api.feign;

import com.transcend.plm.datadriven.api.model.vo.FileVO;
import com.transcend.plm.datadriven.api.model.vo.ResponseVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description
 * @date 2024/07/26 14:12
 **/
@FeignClient(name = "service-ipm-project")
public interface IpmProjectFeignClient {

    /**
     * uploadFiles
     *
     * @param files files
     * @return {@link ResponseVO<List<FileVO>>}
     */
    @ApiOperation(value = "上传文件")
    @PostMapping(value = "ipm/doc/uploadFiles",
            consumes = {"multipart/form-data"})
    ResponseVO<List<FileVO>> uploadFiles(@RequestPart("files") MultipartFile[] files);
}
