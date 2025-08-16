package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 读取草稿参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 11:43
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "读取草稿参数")
public class ReadDraftParam implements Serializable {

    public static ReadDraftParam of() {
        return new ReadDraftParam();
    }

    @NotBlank(message = "bid")
    @ApiModelProperty(value = "bid")
    private String bid;

    private static final long serialVersionUID = 1L;

}
