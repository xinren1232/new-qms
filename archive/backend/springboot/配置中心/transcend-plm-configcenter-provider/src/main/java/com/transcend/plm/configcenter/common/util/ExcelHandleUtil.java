package com.transcend.plm.configcenter.common.util;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * ExcelUtil工具类
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2020/11/23 18:56
 */
@Slf4j
public class ExcelHandleUtil {


    /**
     * 导出excel
     *
     * @param dataList   dataList
     * @param headList   headList
     * @return
     * @version: 1.0
     * @date: 2020-12-09 12:36
     * @author:
     */
    public static XSSFWorkbook exportExcel(List<List<Object>> dataList, List<String> headList) {
        // 第一步：定义一个新的工作簿
        XSSFWorkbook wb = new XSSFWorkbook();
        // 第二步：创建一个Sheet页
        XSSFSheet sheet = wb.createSheet();
        sheet.setDefaultRowHeight((short) (2 * 256));
        XSSFFont font = wb.createFont();
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short) 16);
        XSSFRow row = sheet.createRow(0);
        for (int j = 0; j < headList.size(); j++) {
            XSSFCell cell = row.createCell(j);
            String value = headList.get(j);
            cell.setCellValue(value);
        }

        for (int k = 0; k < dataList.size(); k++) {
            row = sheet.createRow(k + 1);
            List<Object> valueList = dataList.get(k);
            for (int j = 0; j < valueList.size(); j++) {
                XSSFCell cell = row.createCell(j);
                Object value = valueList.get(j);
                if (value instanceof String) {
                    cell.setCellValue(value.toString());
                } else if (value instanceof Date) {
                    cell.setCellValue(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(value));
                } else if(value instanceof LocalDateTime){
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    cell.setCellValue(((LocalDateTime) value).format(fmt));
                }else {
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    }

                }

            }
        }
        //自动调整列宽
        for (int i = 0; i < headList.size(); i++) {
            sheet.autoSizeColumn(i);
            //手动调整列宽，解决中文不能自适应问题
            //单元格单行最长支持255*256宽度（每个单元格样式已经设置自动换行，超出即换行）
            //设置最低列宽度，列宽约六个中文字符
            int width = Math.max(3500, Math.min(255 * 256, sheet.getColumnWidth(i) * 17 / 10));
            sheet.setColumnWidth(i, width);
        }
        return wb;
    }

    /**
     * 添加水印
     *
     * @param wb        wb
     * @param jobNumber jobNumber
     * @return
     * @version: 1.0
     * @date: 2020-12-09 13:40
     * @author:
     */
    /*private static void addWaterMark(XSSFWorkbook wb, String jobNumber) throws IOException {
        XSSFSheet sheet;
        FontImage.Watermark watermark = new FontImage.Watermark();
        watermark.setText(jobNumber);
        BufferedImage image = FontImage.createWatermarkImage(watermark);
        // 导出到字节流B
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);

        int pictureIdx = wb.addPicture(os.toByteArray(), Workbook.PICTURE_TYPE_PNG);
        POIXMLDocumentPart poixmlDocumentPart = wb.getAllPictures().get(pictureIdx);
        //获取每个Sheet表
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            sheet = wb.getSheetAt(i);
            PackagePartName ppn = poixmlDocumentPart.getPackagePart().getPartName();
            String relType = XSSFRelation.IMAGES.getRelation();
            //add relation from sheet to the picture data
            PackageRelationship pr = sheet.getPackagePart().addRelationship(ppn, TargetMode.INTERNAL, relType, null);
            //set background picture to sheet
            sheet.getCTWorksheet().addNewPicture().setId(pr.getId());
        }
    }*/

}
