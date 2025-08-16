package com.transcend.plm.configcenter.language.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.language.infrastructure.repository.po.CfgLanguagePo;
import com.transcend.plm.configcenter.language.pojo.qo.CfgLanguageQo;
import com.transcend.plm.configcenter.language.pojo.vo.CfgLanguageVo;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgLanguageService extends IService<CfgLanguagePo> {
    /**
     * 根据bid进行更新
     * @param cfgLanguagePo
     * @return
     */
    CfgLanguagePo updateByBid(CfgLanguagePo cfgLanguagePo);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgLanguagePo getByBid(String bid);

    PagedResult<CfgLanguageVo> pageByCfgLanguageQo(BaseRequest<CfgLanguageQo> pageQo);

    boolean logicalDeleteByBid(String bid);
}
