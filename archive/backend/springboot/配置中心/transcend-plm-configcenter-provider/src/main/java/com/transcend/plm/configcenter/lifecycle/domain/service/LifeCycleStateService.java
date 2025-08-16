package com.transcend.plm.configcenter.lifecycle.domain.service;

import com.alibaba.excel.EasyExcelFactory;
import com.google.common.collect.Lists;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.lifecycle.qo.LifeCycleStateListQo;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.LifeCycleStateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.LifeCycleStateExcelVO;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.LifeCycleStateImportVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.LifeCycleStateRepository;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 对象领域服务
 *
 * @author yuanhu.huang
 * @version V1.0.0
 * @date 2023/2/20 10:39
 * @since 1.0
 */
@Service
public class LifeCycleStateService {
    @Resource
    private LifeCycleStateRepository lifeCycleStatusRepository;

    public PagedResult<LifeCycleStateVo> page(BaseRequest<LifeCycleStateListQo> pageQo) {
        return lifeCycleStatusRepository.page(pageQo);

    }

    public List<LifeCycleStateVo> list(LifeCycleStateListQo lifeCycleStateListQo){
        return lifeCycleStatusRepository.list(lifeCycleStateListQo);
    }

    public List<LifeCycleStateVo> allList(){
        return lifeCycleStatusRepository.allList();
    }

    public LifeCycleStateImportVo importData(MultipartFile file) throws IOException {
        Assert.notNull(file, "LifeCycleStateDto is null");
        ExcelListener<LifeCycleStateExcelVO> excelListener = new ExcelListener();
        EasyExcelFactory.read(file.getInputStream(), LifeCycleStateExcelVO.class, excelListener).sheet(0).doRead();
        List<String> headList = excelListener.getHeadList();
        List<LifeCycleStateExcelVO> listData = excelListener.getDataList();
        boolean result = dataCheck(listData);
        LifeCycleStateImportVo lifeCycleStateImportVo = new LifeCycleStateImportVo();
        lifeCycleStateImportVo.setHeadList(headList);
        lifeCycleStateImportVo.setValues(listData);
        lifeCycleStateImportVo.setResult(result);
        return lifeCycleStateImportVo;
    }

    private Boolean dataCheck(List<LifeCycleStateExcelVO> listData){
        boolean result = true;
        LifeCycleStateListQo lifeCycleStateListQo = new LifeCycleStateListQo();
        List<LifeCycleStateVo> lifeCycleStateVos = list(lifeCycleStateListQo);
        List<String> codeList = lifeCycleStateVos.stream().map(LifeCycleStateVo::getCode).collect(Collectors.toList());
        Set<String> codeSet = new HashSet<>();
        for(LifeCycleStateExcelVO lifeCycleStateExcelVO:listData){
            StringBuffer msg = new StringBuffer();
            if(StringUtil.isBlank(lifeCycleStateExcelVO.getName()) || StringUtil.isBlank(lifeCycleStateExcelVO.getCode()) || StringUtil.isBlank(lifeCycleStateExcelVO.getGroupCode())){
                msg.append("必填字段为空!");
                result =false;
            }
            if(codeSet.contains(lifeCycleStateExcelVO.getCode()) || codeList.contains(lifeCycleStateExcelVO.getCode())){
                msg.append("编码重复!");
                result =false;
            }else{
                codeSet.add(lifeCycleStateExcelVO.getCode());
            }
            lifeCycleStateExcelVO.setCheckResultMessage(msg.toString());
        }
        //校验状态组
        return result;
    }

    public LifeCycleStateVo add(LifeCycleStateDto dto) {
        // bid暂时用guid，后期改为雪花bid
        dto.setBid(SnowflakeIdWorker.nextIdStr());
        return lifeCycleStatusRepository.add(dto);
    }

    public List<LifeCycleStateVo> saveImport(List<LifeCycleStateDto> dtos){
        for(LifeCycleStateDto dto:dtos){
            setLifeCycleStateDtoDefaultValue(dto);
        }
        return lifeCycleStatusRepository.addBatch(dtos);
    }

    public LifeCycleStateVo edit(LifeCycleStateDto dto) {
        Assert.notNull(dto, "LifeCycleStateDto is null");
        Assert.hasText(dto.getBid(),"LifeCycleStateDto bid is blank");
        return lifeCycleStatusRepository.edit(dto);
    }

    public boolean delete(String bid){
        return lifeCycleStatusRepository.delete(bid);
    }

    public List<LifeCycleStateVo> queryByCodes(List<String> codeList) {
        if (CollectionUtils.isEmpty(codeList)) {
            return Lists.newArrayList();
        }
        return lifeCycleStatusRepository.queryByCodes(codeList);
    }



    private void setLifeCycleStateDtoDefaultValue(LifeCycleStateDto dto){
        if(dto.getBid() == null){
            dto.setBid(SnowflakeIdWorker.nextIdStr());
        }
        if(dto.getDeleteFlag() == null){
            dto.setDeleteFlag(0);
        }
        if(dto.getBindingFlag() == null){
            dto.setBindingFlag(false);
        }
        if(dto.getTenantId() == null){
            dto.setTenantId(0L);
        }
        if(dto.getCreatedTime() == null){
            dto.setCreatedTime(new Date());
        }

        if(dto.getUpdatedTime() == null){
            dto.setUpdatedTime(new Date());
        }
    }
}
