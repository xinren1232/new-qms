package com.transcend.plm.configcenter.dictionary.application.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryDetail;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.common.pojo.dto.ImportDto;
import com.transcend.plm.configcenter.common.service.IExcelStrategy;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.dictionary.application.service.ICfgDictionaryApplicationService;
import com.transcend.plm.configcenter.dictionary.converter.CfgDictionaryConverter;
import com.transcend.plm.configcenter.dictionary.domain.service.CfgDictionaryDomainService;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.CfgDictionaryRepository;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryDto;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryItemDto;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryAndCheckVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对象应用组合实现服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/2/20 10:26
 * @since 1.0
 */
@Service("cfgDictionaryServiceApplicationServiceImpl")
@Qualifier("cfgDictionaryServiceApplicationServiceImpl")
public class ICfgDictionaryServiceApplicationServiceImpl implements ICfgDictionaryApplicationService, IExcelStrategy {

    @Resource
    private CfgDictionaryDomainService dictionaryDomainService;
    @Resource
    private CfgDictionaryRepository cfgDictionaryRepository;

    @Override
    public PagedResult<CfgDictionaryVo> page(BaseRequest<CfgDictionaryQo> pageQo) {
        return dictionaryDomainService.page(pageQo);
    }

    @Override
    public CfgDictionaryVo getByBid(String modelCode) {
        return dictionaryDomainService.getByBid(modelCode);
    }

    @Override
    public CfgDictionaryVo add(CfgDictionaryDto dto) {
        return dictionaryDomainService.add(dto);
    }

    @Override
    public CfgDictionaryVo getDictionaryAndItemByBid(String bid) {
        // 1.获取字典主信息
        CfgDictionaryVo vo = getByBid(bid);
        // 2.获取字典条目信息
        List<CfgDictionaryItemVo> itemVos = cfgDictionaryRepository.listDictionaryItemByDictionaryBid(bid);
        vo.setDictionaryItems(itemVos);
        return vo;
    }

    @Override
    public Boolean blukSaveOrUpdateDictionaryAndItem(List<CfgDictionaryDto> dtos) {

        return null;
    }

    @Override
    public List<CfgDictionaryAndCheckVo> checkReturnBlukDictionaryAndItem(List<CfgDictionaryDto> dtos) {
        return null;
    }

    @Override
    public CfgDictionaryVo saveOrUpdate(CfgDictionaryDto dto) {
        String dtoBid = dto.getBid();
        String code = dto.getCode();
        // 1.校验code是否重复
        checkDictionaryCodeRepeat(dtoBid, code);
        if (StringUtil.isBlank(dtoBid)) {
            return add(dto);
        }
        return update(dto);
    }

    /**
     * 检查code是否重复
     *
     * @param dtoBid bid
     * @param code   字典code
     */
    private void checkDictionaryCodeRepeat(String dtoBid, String code) {
        CfgDictionaryVo cfgDictionaryVo = cfgDictionaryRepository.getDictionaryByCode(code);
        // 数据库不存在，且查出来的bid不匹配调用方传参（即非更新的的情况）
        if (cfgDictionaryVo != null && !cfgDictionaryVo.getBid().equals(dtoBid)) {
            throw new BusinessException(ErrorMsgEnum.DICT_ERROR_CODE_REPERT.getCode(),
                    ErrorMsgEnum.DICT_ERROR_CODE_REPERT.getDesc());
        }
    }

    @Override
    public CfgDictionaryVo saveOrUpdateDictionaryAndItem(CfgDictionaryDto dto) {
        // 1.保存字典
        CfgDictionaryVo cfgDictionaryVo = saveOrUpdate(dto);
        List<CfgDictionaryItemDto> dictionaryItems = dto.getDictionaryItems();
        List<CfgDictionaryItemDto> addCfgDictionaryItemDtos = Lists.newArrayList();
        List<CfgDictionaryItemDto> updateCfgDictionaryItemDtos = Lists.newArrayList();
        dictionaryItems.forEach(itemDto -> {
            // 设置字典bid
            itemDto.setDictionaryBid(cfgDictionaryVo.getBid());
            // 1.新增item
            if (StringUtil.isBlank(itemDto.getBid())) {
                addCfgDictionaryItemDtos.add(itemDto);
                return;
            }
            // 2.更新item
            updateCfgDictionaryItemDtos.add(itemDto);
        });
        dictionaryDomainService.blukAddDictionaryItem(addCfgDictionaryItemDtos);
        dictionaryDomainService.blukUpdateDictionaryItem(updateCfgDictionaryItemDtos);
        return cfgDictionaryVo;
    }

    @Override
    public Boolean changeEnableFlag(String bid, Integer enableFlag) {
        CfgDictionaryVo cfgDictionaryVo = getByBid(bid);
        cfgDictionaryVo.setEnableFlag(enableFlag);
        CfgDictionaryDto dto = CfgDictionaryConverter.INSTANCE.vo2dto(cfgDictionaryVo);
        update(dto);
        return true;
    }

    @Override
    public Boolean logicalDelete(String bid) {
        return dictionaryDomainService.logicalDelete(bid);
    }

    private CfgDictionaryVo update(CfgDictionaryDto dto) {
        return dictionaryDomainService.update(dto);
    }


    /**
     * 此接口后期压力大，需要做缓存 TODO
     *
     * @param codes
     * @param enableFlags
     * @return
     */
    @Override
    public List<CfgDictionaryVo> listDictionaryAndItemByCodesAndEnableFlags(@NotNull List<String> codes, List<Integer> enableFlags) {
        // 1.获取字典主信息
        List<CfgDictionaryVo> dictionaryVos = dictionaryDomainService.listByByCodesAndEnableFlags(codes, enableFlags);
        if (CollectionUtils.isEmpty(dictionaryVos)){
            return Lists.newArrayList();
        }
        // 1.1收集bid集合
        Set<String> dictioanryBids = dictionaryVos.stream().map(CfgDictionaryVo::getBid).collect(Collectors.toSet());
        // 2.获取字典条目信息
        List<CfgDictionaryItemVo> dictionaryItemVos = dictionaryDomainService.listDictionaryItemByDictionaryBids(dictioanryBids);
        // 2.1 把字典条目以字典bid为key,条目list为value
        Map<String, List<CfgDictionaryItemVo>> itemMap = Maps.newHashMap();
        dictionaryItemVos.forEach(cfgDictionaryItemVo -> {
                    String dictionaryBid = cfgDictionaryItemVo.getDictionaryBid();
                    List<CfgDictionaryItemVo> cfgDictionaryItemVos = itemMap.get(dictionaryBid);
                    if (CollectionUtils.isEmpty(cfgDictionaryItemVos)) {
                        cfgDictionaryItemVos = Lists.newArrayList();
                        itemMap.put(dictionaryBid, cfgDictionaryItemVos);
                    }
                    // 补充条目
                    cfgDictionaryItemVos.add(cfgDictionaryItemVo);
                }
        );
        // 3. 给字典主信息组合条目信息
        dictionaryVos.forEach(cfgDictionaryVo -> {
            cfgDictionaryVo.setDictionaryItems(itemMap.get(cfgDictionaryVo.getBid()));
        });
        return dictionaryVos;
    }

    @Override
    public List<Object> getExportExcelData(Object param) {
        List<String> heardList = new ArrayList<>();
        Arrays.asList("字典名称","字典编码","状态","更新人","更新时间","key","中文","英文").forEach(heardList::add);
        List<CfgDictionaryDetail> cfgDictionaryDetails =  cfgDictionaryRepository.listDictionaryDetails();
        List<List<Object>> dataList = new ArrayList<>();
        Map<Integer,String> enableMap = new HashMap<>();
        enableMap.put(0,"未启用");
        enableMap.put(1,"启用");
        enableMap.put(2,"禁用");
        cfgDictionaryDetails.forEach(cfgDictionaryDetail -> {
            List<Object> data = new ArrayList<>();
            data.add(cfgDictionaryDetail.getName());
            data.add(cfgDictionaryDetail.getCode());
            data.add(enableMap.get(cfgDictionaryDetail.getEnableFlag()));
            data.add(cfgDictionaryDetail.getUpdatedBy());
            data.add(cfgDictionaryDetail.getUpdatedTime());
            data.add(cfgDictionaryDetail.getKeyCode());
            data.add(cfgDictionaryDetail.getZhValue());
            data.add(cfgDictionaryDetail.getEnValue());
            dataList.add(data);
        });
        List<Object> result = new ArrayList<>();
        result.add(heardList);
        result.add(dataList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importData(List<Map<Integer,Object>> dataList, ImportDto importDto) {
        Map<String,String> codeBidMap = new HashMap<>();
        List<CfgDictionaryDto> cfgDictionaryDtos = new ArrayList<>();
        List<String> codes = new ArrayList<>();
        List<CfgDictionaryItemDto> addCfgDictionaryItemDtos = Lists.newArrayList();
        String jobNumber = SsoHelper.getJobNumber();
        if(CollectionUtils.isNotEmpty(dataList)){
            for(Map<Integer,Object> map:dataList){
                if(map.get(0) == null || map.get(1) == null|| map.get(2) == null|| map.get(3) == null|| map.get(4) == null){
                    throw new BusinessException("字典必填数据不能为空");
                }
                String dicBid = codeBidMap.get(map.get(1)+"");
                if(dicBid == null){
                    dicBid = SnowflakeIdWorker.nextIdStr();
                    CfgDictionaryDto cfgDictionaryDto = new CfgDictionaryDto();
                    cfgDictionaryDto.setBid(dicBid);
                    cfgDictionaryDto.setCode(map.get(1)+"");
                    cfgDictionaryDto.setName(map.get(0)+"");
                    cfgDictionaryDto.setPermissionScope("SYSTEM");
                    cfgDictionaryDtos.add(cfgDictionaryDto);
                    codes.add(cfgDictionaryDto.getCode());
                    codeBidMap.put(map.get(1)+"",dicBid);
                }
                CfgDictionaryItemDto cfgDictionaryItemDto = new CfgDictionaryItemDto();
                cfgDictionaryItemDto.setDictionaryBid(dicBid);
                cfgDictionaryItemDto.setBid(SnowflakeIdWorker.nextIdStr());
                cfgDictionaryItemDto.setKeyCode(map.get(2)+"");
                Map<String, String> multilingualValueMap = new HashMap<>();
                multilingualValueMap.put("en",map.get(4)+"");
                multilingualValueMap.put("zh",map.get(3)+"");
                cfgDictionaryItemDto.setMultilingualValueMap(multilingualValueMap);
                cfgDictionaryItemDto.setCreatedBy(jobNumber);
                cfgDictionaryItemDto.setCreatedTime(new Date());
                if(map.get(5) != null){
                    cfgDictionaryItemDto.setSort(Integer.valueOf(map.get(5)+""));
                }
                cfgDictionaryItemDto.setEnableFlag(1);
                cfgDictionaryItemDto.setParentBid("0");
                //默认颜色
                cfgDictionaryItemDto.setCustom1("rgba(199, 21, 133, 1)");
                addCfgDictionaryItemDtos.add(cfgDictionaryItemDto);
            }
            //先判断code是否有重复的
            List<CfgDictionaryVo> list = dictionaryDomainService.listByByCodesAndEnableFlags(codes, null);
            if(CollectionUtils.isNotEmpty(list)){
                throw new BusinessException("新增的编码重复");
            }
            dictionaryDomainService.addBatch(cfgDictionaryDtos);
            dictionaryDomainService.blukAddDictionaryItem(addCfgDictionaryItemDtos);
        }
        return true;
    }
}
