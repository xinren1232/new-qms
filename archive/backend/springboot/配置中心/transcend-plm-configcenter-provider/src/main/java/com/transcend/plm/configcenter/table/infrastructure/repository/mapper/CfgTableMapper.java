package com.transcend.plm.configcenter.table.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTablePo;
import com.transcend.plm.configcenter.api.model.table.qo.CfgTableListQo;
import com.transcend.plm.configcenter.table.pojo.dto.TableDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CfgTableMapper extends BaseMapper<CfgTablePo> {

	CfgTablePo getByBid(String bid);

	List<CfgTablePo> list(CfgTableListQo cfgTableListQo);


	CfgTablePo getByTableName(String tableName);

    List<CfgTablePo> listByByTableNamesAndEnableFlags(@Param("tableNames") List<String> tableNames,
													  @Param("enableFlags") List<Integer> enableFlags);

	void createTable(@Param("tableCreateDto") TableDto tableCreateDto);

	void addTableCloumns(@Param("tableCreateDto") TableDto tableCreateDto);
}
