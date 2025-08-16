package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class CfgLifeCycleTemplateQo {
    /**
     * 名称
     */
    private String name;


    /**
     * 状态（启用标志，0未启用，1启用，2禁用）
     */
    private Integer enableFlag;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "最后修改开始时间", example = "2020-08-21 13:22:11")
    private Date startDate;



    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "最后修改结束时间", example = "2020-08-28 13:22:11")
    private Date endDate;
}
