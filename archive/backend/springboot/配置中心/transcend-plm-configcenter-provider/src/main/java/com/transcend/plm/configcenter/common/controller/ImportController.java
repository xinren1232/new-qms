package com.transcend.plm.configcenter.common.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.transcend.plm.configcenter.common.listener.ExcelListener;
import com.transcend.plm.configcenter.common.pojo.dto.ImportDto;
import com.transcend.plm.configcenter.common.service.IExcelStrategy;
import com.transcend.plm.configcenter.common.util.ExcelUtil;
import com.transsion.framework.enums.ResultEnum;
import com.transsion.framework.exception.BusinessException;
import com.transsion.framework.tool.SpringBeanHelper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Api(value = "导入controller", tags = "导入controller")
@RestController
@RequestMapping(value = "/manager/cfg/import/")
public class ImportController {

    @PostMapping("excel/{beanName}")
    public boolean importData(MultipartFile file,@PathVariable("beanName")  String beanName){
        //校验文件数据
        if (!ExcelUtil.isExcelFormat(Objects.requireNonNull(file.getOriginalFilename()))) {
            throw new BusinessException(ResultEnum.ERROR.getCode(), "文件格式不正确");
        }
        try{
            ExcelListener listener = new ExcelListener();
            InputStream inputStream = file.getInputStream();
            ExcelReader reader = EasyExcelFactory.read(inputStream, listener).build();
            // 读取Sheet,从第2行开始读取(因为表头占两行)
            ReadSheet readSheet = EasyExcel.readSheet(0).headRowNumber(1).build();
            reader.read(readSheet);
            reader.finish();
            List<Map<Integer,Object>> dataList = listener.getData();
            IExcelStrategy iExcelStatery = SpringBeanHelper.getBean(beanName);
            return iExcelStatery.importData(dataList,null);
        }catch (Exception e){
            throw new BusinessException(e.getMessage());
        }
    }
}
