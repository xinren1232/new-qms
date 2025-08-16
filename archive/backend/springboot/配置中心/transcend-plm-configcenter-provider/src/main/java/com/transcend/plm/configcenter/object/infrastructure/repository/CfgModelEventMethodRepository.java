package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgModelEventMethodPo;
import com.transcend.plm.configcenter.object.pojo.CfgModelEventMethodConverter;
import com.transcend.plm.configcenter.api.model.object.dto.CfgModelEventMethodDto;
import com.transcend.plm.configcenter.api.model.object.qo.CfgModelEventMethodQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgModelEventMethodVo;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * ModelEventMethodMapper
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2021/11/18 10:45
 */
@Repository
public class CfgModelEventMethodRepository {
    @Resource
    private CfgModelEventMethodService cfgModelEventMethodService;

    public CfgModelEventMethodVo save(CfgModelEventMethodDto cfgModelEventMethodDto) {
        CfgModelEventMethodPo cfgModelEventMethod = CfgModelEventMethodConverter.INSTANCE.dto2po(cfgModelEventMethodDto);
        cfgModelEventMethodService.save(cfgModelEventMethod);
        return CfgModelEventMethodConverter.INSTANCE.po2vo(cfgModelEventMethod);
    }

    public CfgModelEventMethodVo update(CfgModelEventMethodDto cfgModelEventMethodDto) {
        CfgModelEventMethodPo cfgModelEventMethod = CfgModelEventMethodConverter.INSTANCE.dto2po(cfgModelEventMethodDto);
        cfgModelEventMethodService.updateByBid(cfgModelEventMethod);
        return CfgModelEventMethodConverter.INSTANCE.po2vo(cfgModelEventMethod);
    }

    public CfgModelEventMethodVo getByBid(String bid) {
        return CfgModelEventMethodConverter.INSTANCE.po2vo(cfgModelEventMethodService.getByBid(bid));
    }

    public PagedResult<CfgModelEventMethodVo> page(BaseRequest<CfgModelEventMethodQo> pageQo) {
        return cfgModelEventMethodService.pageByCfgModelEventMethodQo(pageQo);
    }

    public boolean logicalDeleteByBid(String bid) {
        return cfgModelEventMethodService.logicalDeleteByBid(bid);
    }

    public List<CfgModelEventMethodVo> findEventMethodByCondition(CfgModelEventMethodQo cfgModelEventMethodQo) {
        CfgModelEventMethodPo cfgModelEventMethod = CfgModelEventMethodConverter.INSTANCE.qo2po(cfgModelEventMethodQo);
        List<CfgModelEventMethodPo> cfgModelEventMethods = cfgModelEventMethodService.findByCondition(cfgModelEventMethod);
        return CfgModelEventMethodConverter.INSTANCE.pos2vos(cfgModelEventMethods);
    }

    public List<CfgModelEventMethodVo> bulkAdd(List<CfgModelEventMethodDto> dtos) {
        List<CfgModelEventMethodPo> pos = CfgModelEventMethodConverter.INSTANCE.dtos2pos(dtos);
        cfgModelEventMethodService.bulkAdd(pos);
        return CfgModelEventMethodConverter.INSTANCE.pos2vos(pos);
    }

    public Boolean bulkDelete(List<String> bids) {
        return cfgModelEventMethodService.bulkDelete(bids);
    }
}
