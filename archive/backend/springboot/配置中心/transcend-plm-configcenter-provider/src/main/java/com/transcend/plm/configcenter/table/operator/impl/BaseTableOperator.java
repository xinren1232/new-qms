package com.transcend.plm.configcenter.table.operator.impl;

import  com.transcend.plm.configcenter.table.meta.impl.BaseDbType;
import  com.transcend.plm.configcenter.table.operator.ITableOperator;

/**
 * 操作数据表基类
 * 
 * @company 广州宏天软件股份有限公司
 * @author heyifan
 * @email heyf@jee-soft.cn
 * @date 2018年4月25日
 */
public abstract class BaseTableOperator extends BaseDbType implements ITableOperator {
	
	protected String replaceLineThrough(String partition){
		return partition.toUpperCase().replaceAll("-", "");
	}
}
