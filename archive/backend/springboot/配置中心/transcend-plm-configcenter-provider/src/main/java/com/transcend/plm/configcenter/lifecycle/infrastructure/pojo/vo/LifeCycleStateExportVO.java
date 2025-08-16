package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo;



import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;

/**
 * 生命状态周期导出对应VO
 *
 * @author yikai.lian
 * @version: 1.0
 * @date 2021/01/05 16:27
 */
@Setter
@Getter
@HeadStyle
@ContentRowHeight(15)
@HeadRowHeight(20)
@ColumnWidth(30)
public class LifeCycleStateExportVO {

    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ExcelProperty(value = "序号")
    @ApiModelProperty(value = "序号")
    @ColumnWidth(7)
    private String num;
    /**
     * 名称
     */
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "名称",example = "部署中")
    @ExcelProperty(value = "*名称")
    @ColumnWidth(15)
    private String name;

    /**
     * 编码
     */
    @ApiModelProperty(value = "编码",example = "0001")
    @ExcelProperty(value = "*编码")
    @ColumnWidth(15)
    private String code;

    /**
     * 所属组编码
     */
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "所属组编码",example = "g0001")
    @ExcelProperty(value = "*状态组")
    @ColumnWidth(15)
    private String groupCode;

    /**
     * 说明
     */
    @ApiModelProperty(value = "说明",example = "")
    @ExcelProperty(value = "说明")
    @ColumnWidth(35)
    private String description;
}
