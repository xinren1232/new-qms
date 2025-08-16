package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 基础模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@ApiModel("分组列表结果集")
public class GroupListResult<R> extends HashMap<String, List<R>> {

}
