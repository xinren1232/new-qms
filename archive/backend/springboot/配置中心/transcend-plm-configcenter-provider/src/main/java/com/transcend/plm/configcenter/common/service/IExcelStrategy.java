package com.transcend.plm.configcenter.common.service;

import com.transcend.plm.configcenter.common.pojo.dto.ImportDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @Program 通用导出 导入接口
 * @Description
 * @Author huanghu.huang
 * @Version 1.0
 * @Date 2023-02-22 10:36
 **/
public interface IExcelStrategy {
    /**
     * 导出
     * @param param
     * @return
     */
    List<Object> getExportExcelData(Object param);


    /*
    导入
     */
    boolean importData(List<Map<Integer,Object>> dataList, ImportDto importDto);
}
