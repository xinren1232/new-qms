package com.transcend.plm.datadriven.common.tool;

import com.transcend.framework.core.exception.TranscendBizException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 通用比较器
 * @createTime 2023-11-30 11:04:00
 */

public class UniversalComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return 1;
        } else if (o2 == null) {
            return -1;
        } else if (o1 instanceof Date && o2 instanceof Date) {
            return compareDates((Date) o1, (Date) o2);
        } else if (o1 instanceof Number && o2 instanceof Number) {
            return compareNumbers((Number) o1, (Number) o2);
        } else if (o1 instanceof String && o2 instanceof String) {
            return compareStrings((String) o1, (String) o2);
        } else if (o1 instanceof LocalDateTime && o2 instanceof LocalDateTime) {
            return compareLocalDateTime((LocalDateTime) o1, (LocalDateTime) o2);
        } else if (o1 instanceof LocalDate && o2 instanceof LocalDate) {
            return compareLocalDate((LocalDate) o1, (LocalDate) o2);
        } else {
            throw new TranscendBizException("此类型数据不支持排序");
        }
    }

    private int compareLocalDate(LocalDate o1, LocalDate o2) {
        return o1.compareTo(o2);
    }

    private int compareDates(Date date1, Date date2) {
        return date1.compareTo(date2);
    }

    private int compareNumbers(Number num1, Number num2) {
        // Assume num1 and num2 are of the same numeric type (e.g., Integer, Double)
        double value1 = num1.doubleValue();
        double value2 = num2.doubleValue();
        return Double.compare(value1, value2);
    }

    private int compareStrings(String str1, String str2) {
        return str1.compareTo(str2);
    }

    private int compareLocalDateTime(LocalDateTime o1, LocalDateTime o2) {
        return o1.compareTo(o2);
    }

}
