package com.transcend.plm.configcenter.api.model.object.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.LinkedHashSet;

/**
 * @author:jie.luo
 * 权限-数据
 */
@Setter
@Getter
@Accessors(chain = true)
@ToString
@ApiModel("基础权限权限")
public class BasePermission implements Serializable{

    /**
     * 生命周期状态CODE(还有 ALL)
     */
    @ApiModelProperty(value = "生命周期状态CODE(还有 ALL)", example = "ALL")
    @NotBlank(message = "生命周期状态不能为空！")
    private String lcStateCode;

    /**
     * 操作权限code集合
     */
    @ApiModelProperty(value = "操作权限code集合", example = "['ADD','EDIT']")
    @Size(min = 1, message = "操作权限至少选择一个！")
    private LinkedHashSet<String> operations;

    public static BasePermission of(){
        return new BasePermission();
    }

    private static final long serialVersionUID = 1L;
}