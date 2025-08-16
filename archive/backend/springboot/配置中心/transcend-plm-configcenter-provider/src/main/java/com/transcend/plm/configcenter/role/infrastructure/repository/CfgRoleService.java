package com.transcend.plm.configcenter.role.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.configcenter.role.infrastructure.repository.po.CfgRolePo;
import com.transcend.plm.configcenter.role.pojo.qo.CfgRoleQo;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgRoleService extends IService<CfgRolePo> {
    /**
     * 根据bid进行更新
     * @param cfgAttribute
     * @return
     */
    CfgRolePo updateByBid(CfgRolePo cfgAttribute);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgRolePo getByBid(String bid);

    PagedResult<CfgRoleVo> pageByCfgAttributeQo(BaseRequest<CfgRoleQo> pageQo);

    Boolean logicalDeleteByBid(String bid);

    /**
     * 根据角色code列表查询角色信息列表
     * @param codeList
     * @return
     */
    List<CfgRolePo> queryByCodes(List<String> codeList);
}
