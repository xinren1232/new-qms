package com.transcend.plm.configcenter.api.model.object.vo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import com.transsion.framework.desensitize.annotation.EmailDesensitize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@ApiModel("样例信息")
@Data
public class SampleVo extends BaseDto{
	
	@ApiModelProperty("名称")
	@NotBlank(message="test {tr.query.key.message}")
	@Size(min=4,max=10)
	String name;
	
	@ApiModelProperty("状态")
	@Min(1)
	@Max(5)
	Integer status;
	
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