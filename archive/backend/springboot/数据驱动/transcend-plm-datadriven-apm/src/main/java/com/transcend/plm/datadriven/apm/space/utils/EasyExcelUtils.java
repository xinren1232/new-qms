package com.transcend.plm.datadriven.apm.space.utils;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author shu.zhang
 * @version 1.0
 * @className EasyExcelUtils
 * @description desc
 * @date 2024/6/3 18:03
 */
public class EasyExcelUtils {

    public static HorizontalCellStyleStrategy getStyleStrategy(List<Map<String, Object>> header) {
        // 头的策略  样式调整
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 头背景 浅绿
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        WriteFont headWriteFont = new WriteFont();
        // 头字号
        headWriteFont.setFontHeightInPoints((short) 12);
        // 字体样式
        headWriteFont.setFontName("微软雅黑");
        headWriteFont.setBold(false);
        headWriteFont.setColor(IndexedColors.BLACK.index);
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 自动换行
        headWriteCellStyle.setWrapped(true);
        // 设置细边框
        headWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        headWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        headWriteCellStyle.setBorderRight(BorderStyle.THIN);
        headWriteCellStyle.setBorderTop(BorderStyle.THIN);
        // 设置边框颜色 25灰度
        headWriteCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        headWriteCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headWriteCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headWriteCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        // 水平对齐方式
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 垂直对齐方式
        headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 内容的策略 宋体
        WriteCellStyle contentStyle = new WriteCellStyle();
        // 设置垂直居中
        contentStyle.setWrapped(true);
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置 水平居中
        contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        WriteFont contentWriteFont = new WriteFont();
        // 内容字号
        contentWriteFont.setFontHeightInPoints((short) 10);
        // 字体样式
        contentWriteFont.setFontName("微软雅黑");
        contentWriteFont.setColor(IndexedColors.BLACK.index);
        contentStyle.setWriteFont(contentWriteFont);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        Map<String, String> groupColor = new HashMap<>();
        for (Map<String, Object> map : header) {
            groupColor.put(map.get("name").toString(), ObjectUtil.isNotEmpty(map.get("color")) ? map.get("color").toString() : "#FCCCCCC");
        }
        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentStyle){
            @Override
            protected void setHeadCellStyle(CellWriteHandlerContext context) {
                WriteCellData<?> cellData = context.getFirstCellData();
                CellStyle originCellStyle = cellData.getOriginCellStyle();
                if (Objects.isNull(originCellStyle)) {
                    originCellStyle = context.getWriteWorkbookHolder().getWorkbook().createCellStyle();
                }
                // 设置背景颜色
                originCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                WriteCellStyle writeCellStyle = cellData.getWriteCellStyle();
                writeCellStyle.setFillForegroundColor(null);
                if (context.getRowIndex() == 1) {
                    String groupName = context.getHeadData().getHeadNameList().get(0);
                    if (groupColor.containsKey(groupName)) {
                        String color = groupColor.get(groupName);
                        ((XSSFCellStyle) originCellStyle).setFillForegroundColor(new XSSFColor(new java.awt.Color(Integer.valueOf(color.substring(1, 3), 16), Integer.valueOf(color.substring(3, 5), 16), Integer.valueOf(color.substring(5, 7), 16)), new DefaultIndexedColorMap()));
                    }

                }
                cellData.setOriginCellStyle(originCellStyle);
            }

        };
    }
}
