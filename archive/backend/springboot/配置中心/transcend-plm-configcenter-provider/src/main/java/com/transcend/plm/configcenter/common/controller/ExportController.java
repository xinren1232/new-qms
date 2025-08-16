package com.transcend.plm.configcenter.common.controller;

import com.transcend.plm.configcenter.common.pojo.dto.ExportDto;
import com.transcend.plm.configcenter.common.service.IExcelStrategy;
import com.transcend.plm.configcenter.common.util.ExcelHandleUtil;
import com.transsion.framework.exception.BusinessException;
import com.transsion.framework.tool.SpringBeanHelper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Api(value = "导出controller", tags = "导出controller")
@RestController
@RequestMapping(value = "/manager/cfg/")
public class ExportController {

    @PostMapping("exportExcel")
    public void exportExcel(HttpServletResponse response,@RequestBody ExportDto exprotDto){
        //请求报文示例
        /*{
            "title": "测试导出",
                "beanName": "cfgAttributeApplicationService",
                "param": {
            "key": "name",
                    "value": "张三"
        }
        }*/
        //根据表头数组和List数据导出Excel
        String fielName = exprotDto.getTitle()+".xlsx";
        setResponseHeader(response,fielName);
        XSSFWorkbook wb;
        if(exprotDto.isTemplate()){
            //如果是导出模板
            List<String> headers = getHearders(exprotDto.getBeanName());
            List<List<Object>> datas = new ArrayList<>();
            wb = ExcelHandleUtil.exportExcel(datas, headers);
        }else{
            IExcelStrategy iExcelStatery = SpringBeanHelper.getBean(exprotDto.getBeanName());
            List<Object> excelDatas = iExcelStatery.getExportExcelData(exprotDto.getParam());
            //导出操作
            wb = ExcelHandleUtil.exportExcel((List<List<Object>>) excelDatas.get(1), (List<String>) excelDatas.get(0));
        }
        try {
            OutputStream out = response.getOutputStream();
            wb.write(out);
            out.close();
            wb.close();
        } catch (IOException e) {
            log.info("export exception", e);
        }
    }

    private List<String> getHearders(String beanName){
        List<String> list = new ArrayList<>();
        if("cfgAttributeApplicationService".equals(beanName)){
            //属性导入模板
            list.add("属性名称");
            list.add("属性编码");
            list.add("数据类型");
            list.add("属性组");
            list.add("多语言");
        }else if ("lifeCycleApplicationServiceImpl".equals(beanName)){
            //生命周期状态导入模板
            list.add("状态名称");
            list.add("状态编码");
            list.add("属性组");
            list.add("说明");
        }else if ("cfgRoleDomainService".equals(beanName)){
            //角色
            list.add("角色名称");
            list.add("角色编码");
            list.add("父角色编码");
            list.add("说明");
        }else if ("cfgDictionaryServiceApplicationServiceImpl".equals(beanName)){
            //字典
            list.add("名称");
            list.add("编码");
            list.add("KEY");
            list.add("中文");
            list.add("英文");
            list.add("排序");
        }
        return list;
    }

    /**
     * 响应流的方式
     *
     * @Param response
     * @param fileName
     */
    public void setResponseHeader(HttpServletResponse response,String fileName) {
        response.addHeader("Access-Control-Expose-Headers","Content-Disposition");
        try {
            fileName=new String(fileName.getBytes("GBK"), StandardCharsets.ISO_8859_1);
        }catch (UnsupportedEncodingException e){
            throw new BusinessException("编码转换错误");
        }
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+fileName);
        response.addHeader("Param","no-cache");
        response.addHeader("Cache-Control","no-cache");
    }
}
