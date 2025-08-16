package com.transcend.plm.configcenter.object.application.service.impl;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.object.application.service.ICfgModelEventMethodDomainService;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgModelEventMethodRepository;
import com.transcend.plm.configcenter.api.model.object.dto.CfgModelEventMethodDto;
import com.transcend.plm.configcenter.api.model.object.qo.CfgModelEventMethodQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgModelEventMethodVo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * ModelEventMethodServiceImpl
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2021/11/18 10:40
 */
@Service
public class CfgModelEventMethodDomainServiceImpl implements ICfgModelEventMethodDomainService {

    @Resource
    private CfgModelEventMethodRepository repository;

    /**
     * 更新或保存
     *
     * @param cfgModelEventMethodDto
     * @return
     */
    @Override
    public CfgModelEventMethodVo saveOrUpdate(CfgModelEventMethodDto cfgModelEventMethodDto) {
        return StringUtil.isBlank(cfgModelEventMethodDto.getBid()) ? repository.save(cfgModelEventMethodDto) : repository.update(cfgModelEventMethodDto);
    }

    /**
     * 获取详情
     *
     * @param bid
     * @return
     */
    @Override
    public CfgModelEventMethodVo getByBid(String bid) {
        return repository.getByBid(bid);
    }

    /**
     * 分页获取
     *
     * @param pageQo
     * @return
     */
    @Override
    public PagedResult<CfgModelEventMethodVo> page(BaseRequest<CfgModelEventMethodQo> pageQo) {
        return repository.page(pageQo);
    }

    /**
     * 批量添加
     *
     * @param dtos
     * @return
     */
    @Override
    public List<CfgModelEventMethodVo> bulkAdd(List<CfgModelEventMethodDto> dtos) {
        return repository.bulkAdd(dtos);
    }

    /**
     * 逻辑删除
     *
     * @param bid
     * @return
     */
    @Override
    public Boolean logicalDeleteByBid(String bid) {
        return repository.logicalDeleteByBid(bid);
    }

    /**
     * 批量删除
     *
     * @param bids
     * @return
     */
    @Override
    public Boolean logicalBulkDelete(List<String> bids) {
        return repository.bulkDelete(bids);
    }
}
