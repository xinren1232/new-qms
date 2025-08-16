package com.transcend.plm.datadriven.apm.space.utils;

import com.alibaba.excel.write.style.row.AbstractRowHeightStyleStrategy;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.Iterator;

/**
 * @author shu.zhang
 * @version 1.0
 * @className CustomCellWriteHeightConfig
 * @description desc
 * @date 2024/6/4 10:57
 */
public class CustomCellWriteHeightConfig extends AbstractRowHeightStyleStrategy {

    /**
     * 默认高度
     */
    private static final Integer DEFAULT_HEIGHT = 300;

    @Override
    protected void setHeadColumnHeight(Row row, int relativeRowIndex) {
    }

    @Override
    protected void setContentColumnHeight(Row row, int relativeRowIndex) {
        Iterator<Cell> cellIterator = row.cellIterator();
        if (!cellIterator.hasNext()) {
            return;
        }
        // 默认为 1行高度
        int maxHeight = 1;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (cell.getCellTypeEnum() == CellType.STRING) {
                String value = cell.getStringCellValue();
                int len = value.length();
                int num = 0;
                if (len > 50) {
                    num = len % 50 > 0 ? len / 50 : len / 2 - 1;
                }
                if (num > 0) {
                    for (int i = 0; i < num; i++) {
                        if(StringUtils.isNotEmpty(value) && value.length() > len + i){
                            value = value.substring(0, (i + 1) * 50 + i) + "\n" + value.substring((i + 1) * 50 + i, len + i);
                        }
                    }
                }
                if (value.contains("\n")) {
                    int length = value.split("\n").length;
                    maxHeight = Math.max(maxHeight, length) + 1;
                }
            }
        }
        row.setHeight((short) ((maxHeight) * DEFAULT_HEIGHT));
    }

}
