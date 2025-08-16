package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.object.infrastructure.po.DomainObjectConfigPo;
import com.transcend.plm.configcenter.api.model.object.dto.DomainObjectConfigFindParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * DomainObjectConfigMapper
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/30 10:48
 */
@Mapper
public interface DomainObjectConfigMapper {

    /**
     * 批量新增
     *
     * @param poList
     * @return
     * @version: 1.0
     * @date: 2022/12/7 17:05
     * @author: jingfang.luo
     */
    int bulkInsert(@Param("poList") List<DomainObjectConfigPo> poList);

    /**
     * 根据domainBid删除域对象配置
     *
     * @param domainBid
     * @return
     * @version: 1.0
     * @date: 2022/12/30 13:51
     * @author: jingfang.luo
     */
    int deleteByDomainBid(String domainBid);

    /**
     * 根据bid删除域对象配置
     *
     * @param bid
     * @return
     * @version: 1.0
     * @date: 2022/12/30 13:51
     * @author: jingfang.luo
     */
    int deleteByBid(String bid);

    /**
     * 根据domainBid查询域对象配置
     *
     * @param domainBid
     * @return
     * @version: 1.0
     * @date: 2022/12/30 13:51
     * @author: jingfang.luo
     */
    List<DomainObjectConfigPo> findByDomainBid(String domainBid);

    List<DomainObjectConfigPo> find(@Param("domainObjConfigQO") DomainObjectConfigFindParam domainObjectConfigFindParam);

}
