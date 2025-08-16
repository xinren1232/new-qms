package com.transcend.plm.datadriven.apm.tools;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.api.feign.IpmProjectFeignClient;
import com.transcend.plm.datadriven.api.model.vo.FileVO;
import com.transcend.plm.datadriven.api.model.vo.ResponseVO;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transsion.framework.tool.SpringBeanHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 文件工具类
 * @date 2024/07/26 10:37
 **/
@Slf4j
public class FileUtils {
    /**
     * 先下载再上传文件
     * @return
     */
    public static List<FileVO>  uploadFile(List<FileVO> fileVo) {
        IpmProjectFeignClient ipmProjectFeignClient = SpringBeanHelper.getBean(IpmProjectFeignClient.class);
        if(CollectionUtils.isEmpty(fileVo)) {
            return null;
        }
        MultipartFile[] multipartFiles = new MultipartFile[fileVo.size()];
        for (int i = 0; i < fileVo.size(); i++) {
            FileVO file= fileVo.get(i);
            InputStream inputStream = null;
            //下载文件
            try {
                URL exitFile = new URL(file.getUrl());
                HttpURLConnection connection = (HttpURLConnection) exitFile.openConnection();
                inputStream = connection.getInputStream();
                multipartFiles[i] = new MockMultipartFile(file.getName(),file.getName(),"multipart/form-data",inputStream);
            } catch (Exception ex) {
                log.info("下载模板文件失败", ex);
                throw new PlmBizException("下载模板文件失败");
            } finally {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    log.info("流关闭失败");
                }
            }
        }

        // 发起请求
        log.info("上传文件开始------------------入参:{}", JSON.toJSONString(JSON.toJSONString(fileVo)));
        ResponseVO<List<FileVO>> response =  ipmProjectFeignClient.uploadFiles(multipartFiles);
        if("200".equals(response.getCode())) {
            return response.getData();
        }else {
            log.info("上传文件失败，响应信息:{}", JSON.toJSONString(response));
            throw new PlmBizException(response.getMessage());
        }
    }
}
