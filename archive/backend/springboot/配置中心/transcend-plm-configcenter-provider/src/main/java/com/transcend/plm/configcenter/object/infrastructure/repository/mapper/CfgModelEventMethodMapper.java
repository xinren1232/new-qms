package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgModelEventMethodPo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * ModelEventMethodMapper
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2021/11/18 10:45
 */
@Repository
@Mapper
public interface CfgModelEventMethodMapper extends BaseMapper<CfgModelEventMethodPo> {
}
