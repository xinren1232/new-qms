package com.transcend.plm.configcenter.api.model.object.vo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import com.transsion.framework.desensitize.annotation.EmailDesensitize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@ApiModel("对象信息")
@Data
public class ObjectVo extends BaseDto{
	
	@ApiModelProperty("名称")
	@NotBlank(message="test {tr.query.key.message}")
	String name;

	@ApiModelProperty("模型优化编码")
	Integer modelCode;
	
	@ApiModelProperty("状态")
	@Min(1)
	@Max(5)
	Integer enableFlag;
	
	@ApiModelProperty("数量")
	Integer qty;
	
	@ApiModelProperty("测试正则")
	@EmailDesensitize
	@Email
	@NotBlank
	String email;
	
	//@ApiModelProperty("测试正则")
	//@Pattern(regexp ="\\d+",message="test {tr.validation.number.message}")
	//String test;
}