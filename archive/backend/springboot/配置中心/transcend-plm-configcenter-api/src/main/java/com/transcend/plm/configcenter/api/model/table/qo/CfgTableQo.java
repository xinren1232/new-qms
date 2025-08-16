package com.transcend.plm.configcenter.api.model.table.qo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("对象分页信息查询")
@Data
public class CfgTableQo extends BaseDto{
	
	@ApiModelProperty("名称")
	String name;

	@ApiModelProperty("化编码")
	String code;
	
	@ApiModelProperty("状态")
	Integer enableFlag;

	@ApiModelProperty("状态")
	List<Integer> enableFlags;

}