package com.transcend.plm.datadriven.apm.log.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 基础ES数据
 * @author yinbin
 * @version:
 * @date 2023/09/28 09:09
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class BaseEsData {

    /**
     * 业务主键
     */
    private String bizId;

    /**
     * 业务类型
     */
    private String type;

    /**
     * 创建人工号
     */
    private String createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Date createdTime;

    /**
     * json字符串数据
     */
    private String json;

}
