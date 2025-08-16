package com.transcend.plm.configcenter.common.util;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Map;


import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.springframework.util.StringUtils;


import lombok.extern.slf4j.Slf4j;

/**
 * @description: excel工具类
 * @author: renhaijun
 * @create: 2021-11-03 09:46
 **/
@Slf4j
public class ExcelUtil {


    public static boolean isExcelFormat(String fileName) {
        if (!fileName.contains(".")) {
            return false;
        } else {
            String fileType = fileName.substring(fileName.lastIndexOf(46));
            return ".xls".equals(fileType) || ".xlsx".equals(fileType);
        }
    }



    /**
     * 表头样式设置
     *
     * @param workbook
     * @return
     */
    private static CellStyle headCellStyle(SXSSFWorkbook workbook) {
        //创建表头样式
        CellStyle headCellStyle = workbook.createCellStyle();
        //左右居中
        headCellStyle.setAlignment(HorizontalAlignment.CENTER);
        //上下居中
        headCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //四周边框
        headCellStyle.setBorderBottom(BorderStyle.THIN);
        headCellStyle.setBorderLeft(BorderStyle.THIN);
        headCellStyle.setBorderRight(BorderStyle.THIN);
        headCellStyle.setBorderTop(BorderStyle.THIN);
        //背景色
        headCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        //CellStyle.SOLID_FOREGROUND
        headCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //字体设置
        Font font = workbook.createFont();
        //加粗
        font.setBold(true);
        headCellStyle.setFont(font);
        return headCellStyle;
    }


    /**
     * 下载文件
     *
     * @param name
     * @param response
     * @param workbook
     */
    private static void downLoad(String name, HttpServletResponse response, SXSSFWorkbook workbook) throws UnsupportedEncodingException {
        String excelName = "downLoad.xlsx";
        if (!StringUtils.isEmpty(name)) {
            excelName = name + ".xlsx";
        }
      /*  //配置返回内容类型
        response.setHeader("Access-Control-Expose-Headers","Content-disposition");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=utf-8");
        //默认Excel名称
        response.setHeader("Content-disposition", "attachment;filename*=utf-8'zh_cn'" + URLEncoder.encode(excelName, "UTF-8"));*/
        response.addHeader("Access-Control-Expose-Headers","Content-Disposition");
        response.addHeader("Content-Disposition", "attachment;filename*=utf-8'zh_cn'" + URLEncoder.encode(excelName, "UTF-8"));
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        try {
            response.flushBuffer();
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 格式化下载内容
     *
     * @param value
     * @return
     */
    private static String transCellType(Object value) {
        String result = "";
        if (value instanceof Date) {
            Date date = (Date) value;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result = sdf.format(date);
        } else {
            if (null != value){
                result = String.valueOf(value);
            }
            if (StringUtils.isEmpty(result)) {
                result = "\\";
            }
        }
        return result;
    }
    //设置下拉框
    private static void setOption(Map<Integer, String[]> optMap, SXSSFWorkbook workbook, Sheet sheet) {
        if (optMap == null || optMap.size()<=0){
            return;
        }
        int i =1;
        for (Map.Entry<Integer, String[]> entry : optMap.entrySet()) {
            Integer k = entry.getKey();
            String[] v = entry.getValue();
            setLongHSSFValidation(workbook, v, sheet, 3, 1000, k, i);
            i++;
        }
    }

    //创建下拉框
    private static void creatDropDownList(Sheet taskInfoSheet, DataValidationHelper helper, String[] list,
                                          Integer firstRow, Integer lastRow, Integer firstCol, Integer lastCol) {
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        //设置下拉框数据
        DataValidationConstraint constraint = helper.createExplicitListConstraint(list);
        DataValidation dataValidation = helper.createValidation(constraint, addressList);
        //处理Excel兼容性问题
        if (dataValidation instanceof XSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }
        taskInfoSheet.addValidationData(dataValidation);
    }

    public static void setLongHSSFValidation(SXSSFWorkbook workbook, String[] deptList , Sheet sheet , int firstRow, int endRow, int cellNum, int sheetIndex) {
        String hiddenName = "hidden"+cellNum;
        //1.创建隐藏的sheet页。        起个名字吧！叫"hidden"！
        Sheet hidden = workbook.createSheet(hiddenName);
        //2.循环赋值（为了防止下拉框的行数与隐藏域的行数相对应，将隐藏域加到结束行之后）
        for (int i = 0, length = deptList.length; i < length; i++) {
            hidden.createRow(endRow + i).createCell(cellNum).setCellValue(deptList[i]);
        }
        Name category1Name = workbook.createName();
        category1Name.setNameName(hiddenName);
        //3 A1:A代表隐藏域创建第N列createCell(N)时。以A1列开始A行数据获取下拉数组
        category1Name.setRefersToFormula(hiddenName + "!A1:A" + (deptList.length + endRow));
        //
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(hiddenName);
        CellRangeAddressList addressList = new CellRangeAddressList(1, endRow, cellNum, cellNum);
        DataValidation dataValidation = helper.createValidation(constraint, addressList);
        if (dataValidation instanceof XSSFDataValidation) {
            // 数据校验
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }
        // 作用在目标sheet上
        sheet.addValidationData(dataValidation);
        // 设置hiddenSheet隐藏
        workbook.setSheetHidden(sheetIndex, true);
    }
}
