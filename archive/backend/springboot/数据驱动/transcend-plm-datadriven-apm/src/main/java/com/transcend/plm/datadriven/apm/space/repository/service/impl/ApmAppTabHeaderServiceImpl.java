package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.flow.maspstruct.ApmAppTabHeaderConverter;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmAppTabHeaderVO;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppTabHeaderDto;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmAppTabHeaderMapper;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmSpaceAppViewModelMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppTabHeader;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppViewModelPo;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppTabHeaderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author unknown
 */
@Service
public class ApmAppTabHeaderServiceImpl extends ServiceImpl<ApmAppTabHeaderMapper, ApmAppTabHeader>
        implements ApmAppTabHeaderService {

    @Resource
    private ApmSpaceAppViewModelMapper apmSpaceAppViewModelMapper;

    @Override
    public boolean saveOrUpdate(ApmAppTabHeaderDto apmAppTabHeaderDto) {
        String jobNumber = SsoHelper.getJobNumber();
        ApmAppTabHeader apmAppTabHeader;
        if (StringUtils.isNotEmpty(apmAppTabHeaderDto.getCode())) {
            apmAppTabHeader = getOne(Wrappers.<ApmAppTabHeader>lambdaQuery().eq(ApmAppTabHeader::getBizBid, apmAppTabHeaderDto.getBizBid()).eq(ApmAppTabHeader::getCreatedBy, jobNumber).eq(ApmAppTabHeader::getCode, apmAppTabHeaderDto.getCode()));
        } else {
            apmAppTabHeader = getOne(Wrappers.<ApmAppTabHeader>lambdaQuery().eq(ApmAppTabHeader::getBizBid, apmAppTabHeaderDto.getBizBid()).eq(ApmAppTabHeader::getCreatedBy, jobNumber));
        }
        if (apmAppTabHeader != null) {
            apmAppTabHeader.setConfigContent(apmAppTabHeaderDto.getConfigContent());
            apmAppTabHeader.setUpdatedTime(new Date());
            apmAppTabHeader.setUpdatedBy(jobNumber);
            return updateById(apmAppTabHeader);
        } else {
            apmAppTabHeader = new ApmAppTabHeader();
            apmAppTabHeader.setCreatedBy(jobNumber);
            apmAppTabHeader.setCode(apmAppTabHeaderDto.getCode());
            apmAppTabHeader.setConfigContent(apmAppTabHeaderDto.getConfigContent());
            apmAppTabHeader.setBizBid(apmAppTabHeaderDto.getBizBid());
            apmAppTabHeader.setCreatedTime(new Date());
            apmAppTabHeader.setUpdatedBy(jobNumber);
            apmAppTabHeader.setUpdatedTime(new Date());
            return save(apmAppTabHeader);
        }
    }

    @Override
    public ApmAppTabHeader getApmAppTabHeader(String bizBid) {
        String jobNumber = SsoHelper.getJobNumber();
        ApmAppTabHeader apmAppTabHeader = getOne(Wrappers.<ApmAppTabHeader>lambdaQuery().eq(ApmAppTabHeader::getBizBid, bizBid).eq(ApmAppTabHeader::getCreatedBy, jobNumber));
        return apmAppTabHeader;
    }

    @Override
    public List<ApmAppTabHeader> getApmAppTabHeaders(Collection<String> bizBids) {
        String jobNumber = SsoHelper.getJobNumber();
        List<ApmAppTabHeader> apmAppTabHeaders = list(Wrappers.<ApmAppTabHeader>lambdaQuery().in(ApmAppTabHeader::getBizBid, bizBids).eq(ApmAppTabHeader::getCreatedBy, jobNumber));
        return apmAppTabHeaders;
    }

    @Override
    public ApmAppTabHeaderVO getApmAppTabHeaderVO(String bizBid, String code) {
        ApmAppTabHeaderVO apmAppTabHeaderVO;
        String jobNumber = SsoHelper.getJobNumber();
        ApmAppTabHeader apmAppTabHeader = getOne(Wrappers.<ApmAppTabHeader>lambdaQuery().eq(ApmAppTabHeader::getBizBid, bizBid).eq(ApmAppTabHeader::getCreatedBy, jobNumber).eq(ApmAppTabHeader::getCode, code));

        if (apmAppTabHeader == null) {
            apmAppTabHeader = new ApmAppTabHeader();
        }
        apmAppTabHeaderVO = ApmAppTabHeaderConverter.INSTANCE.entityToVO(apmAppTabHeader);
        //查询 ApmSpaceAppViewModelPo
        ApmSpaceAppViewModelPo spaceAppViewModel = apmSpaceAppViewModelMapper
                .selectOne(Wrappers.<ApmSpaceAppViewModelPo>lambdaQuery()
                        .eq(ApmSpaceAppViewModelPo::getSpaceAppBid, bizBid).
                        eq(ApmSpaceAppViewModelPo::getCode, code));
        if (spaceAppViewModel != null) {
            apmAppTabHeaderVO.setViewConfigContent(spaceAppViewModel.getConfigContent());
        }
        return apmAppTabHeaderVO;
    }
}




