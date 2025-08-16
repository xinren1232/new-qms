package com.transcend.plm.configcenter.draft.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CfgDraftDto implements Serializable {


    /**
     * 业务bid
     */
    @ApiModelProperty("长度为32")
    @Max(32)
    @NotNull
    private String bid;

    /**
     * 业务code
     */
    @ApiModelProperty("长度为32")
    @Max(32)
    @NotNull
    private String bizCode;

    /**
     * 类别
     */
    @ApiModelProperty("长度为16")
    @Max(16)
    @NotNull
    private String category;

    /**
     * 内容
     */
    private String content;


    private static final long serialVersionUID = 1L;

    public static Object of() {
        return new CfgDraftDto();
    }
}
