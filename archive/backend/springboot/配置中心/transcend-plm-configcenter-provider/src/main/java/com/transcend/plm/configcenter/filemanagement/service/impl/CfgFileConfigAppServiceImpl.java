package com.transcend.plm.configcenter.filemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.*;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.plm.configcenter.filemanagement.domain.*;
import com.transcend.plm.configcenter.filemanagement.mapper.*;
import com.transcend.plm.configcenter.filemanagement.mapstruct.*;
import com.transcend.plm.configcenter.filemanagement.pojo.dto.*;
import com.transcend.plm.configcenter.filemanagement.service.*;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CfgFileConfigAppServiceImpl implements ICfgFileConfigAppService {

    @Resource
    private CfgFileLibraryService cfgFileLibraryService;

    @Resource
    private CfgFileTypeService cfgFileTypeService;

    @Resource
    private CfgFileCopyRuleService cfgFileCopyRuleService;
    @Resource
    private CfgFileViewerService cfgFileViewerService;
    @Resource
    private CfgFileTypeRuleRelService cfgFileTypeRuleRelService;
    @Resource
    private CfgFileLibraryRuleRelService cfgFileLibraryRuleRelService;
    @Resource
    private CfgFileTypeRuleRelMapper cfgFileTypeRuleRelMapper;
    @Resource
    private CfgFileLibraryRuleRelMapper cfgFileLibraryRuleRelMapper;
    @Resource
    private CfgFileTypeMapper cfgFileTypeMapper;
    @Resource
    private CfgFileLibraryMapper cfgFileLibraryMapper;
    @Resource
    private CfgFileViewerMapper cfgFileViewerMapper;

    @Resource
    private CfgFileCopyRuleMapper cfgFileCopyRuleMapper;


    public PagedResult<CfgFileLibraryVo> page(BaseRequest<CfgFileLibraryDto> pageQo) {
        CfgFileLibraryDto cfgFileLibraryDto = pageQo.getParam();
        Page<CfgFileLibrary> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgFileLibrary> queryWrapper = Wrappers.<CfgFileLibrary> lambdaQuery();
        queryWrapper.eq(CfgFileLibrary::getDeleteFlag, false);
        if (StringUtil.isNotBlank(cfgFileLibraryDto.getName())) {
            queryWrapper.like(CfgFileLibrary::getName, cfgFileLibraryDto.getName());
        }
        IPage<CfgFileLibrary> iPage = cfgFileLibraryService.page(page, queryWrapper);
        List<CfgFileLibraryVo> cfgFileLibraryVos = CfgFileLibraryConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage, cfgFileLibraryVos);
    }

    @Override
    public CfgFileLibraryVo getCfgFileLibrary(String bid) {
        CfgFileLibrary cfgFileLibrary = cfgFileLibraryService.getOne(Wrappers.<CfgFileLibrary>lambdaQuery().eq(CfgFileLibrary::getBid, bid).eq(CfgFileLibrary::getDeleteFlag, false));
        if (cfgFileLibrary != null) {
            CfgFileLibraryVo cfgFileLibraryVo = CfgFileLibraryConverter.INSTANCE.po2vo(cfgFileLibrary);
            //组织规则数据
            List<CfgFileLibraryRuleRel> cfgFileLibraryRuleRels = cfgFileLibraryRuleRelService.list(Wrappers.<CfgFileLibraryRuleRel>lambdaQuery().eq(CfgFileLibraryRuleRel::getSourceLibraryBid, bid).eq(CfgFileLibraryRuleRel::getDeleteFlag, false));
            if(CollectionUtils.isNotEmpty(cfgFileLibraryRuleRels)){
                //获取规则bid
                List<String> ruleBids = cfgFileLibraryRuleRels.stream().map(CfgFileLibraryRuleRel::getRuleBid).collect(Collectors.toList());
                //获取规则
                List<CfgFileCopyRule> cfgFileRules = cfgFileCopyRuleService.list(Wrappers.<CfgFileCopyRule>lambdaQuery().in(CfgFileCopyRule::getBid, ruleBids).eq(CfgFileCopyRule::getDeleteFlag, false));
                List<CfgFileCopyRuleVo> cfgFileCopyRuleVos = CfgFileCopyRuleConverter.INSTANCE.pos2vos(cfgFileRules);
                cfgFileLibraryVo.setCfgFileCopyRuleVos(cfgFileCopyRuleVos);
            }
            return cfgFileLibraryVo;
        }
        return null;
    }

    /**
     * 规则下面配置文件库信息
     * @param cfgFileLibraryRuleRelDto
     * @return
     */

    public CfgFileLibraryRuleRelVo addCfgFileLibraryRuleRel(CfgFileLibraryRuleRelDto cfgFileLibraryRuleRelDto) {
        CfgFileLibraryRuleRel cfgFileLibraryRuleRel;
        if(StringUtils.isEmpty(cfgFileLibraryRuleRelDto.getBid())){
            //新增
            cfgFileLibraryRuleRel = new CfgFileLibraryRuleRel();
            cfgFileLibraryRuleRel.setRuleBid(cfgFileLibraryRuleRelDto.getRuleBid());
            cfgFileLibraryRuleRel.setSourceLibraryBid(cfgFileLibraryRuleRelDto.getSourceLibraryBid());
            cfgFileLibraryRuleRel.setTargetLibraryBids(cfgFileLibraryRuleRelDto.getTargetLibraryBids());
            cfgFileLibraryRuleRel.setBid(SnowflakeIdWorker.nextIdStr());
            cfgFileLibraryRuleRel.setCreatedBy(SsoHelper.getJobNumber());
            cfgFileLibraryRuleRel.setCreatedTime(new Date());
            cfgFileLibraryRuleRelService.save(cfgFileLibraryRuleRel);
        }else{
            //修改
            cfgFileLibraryRuleRel = cfgFileLibraryRuleRelService.getOne(Wrappers.<CfgFileLibraryRuleRel>lambdaQuery().eq(CfgFileLibraryRuleRel::getBid, cfgFileLibraryRuleRelDto.getBid()));
            cfgFileLibraryRuleRel.setSourceLibraryBid(cfgFileLibraryRuleRelDto.getSourceLibraryBid());
            cfgFileLibraryRuleRel.setTargetLibraryBids(cfgFileLibraryRuleRelDto.getTargetLibraryBids());
            cfgFileLibraryRuleRel.setCreatedBy(SsoHelper.getJobNumber());
            cfgFileLibraryRuleRel.setCreatedTime(new Date());
            cfgFileLibraryRuleRelService.updateById(cfgFileLibraryRuleRel);
        }

        CfgFileLibraryRuleRelVo cfgFileLibraryRuleRelVo = CfgFileLibraryRuleRelConverter.INSTANCE.po2vo(cfgFileLibraryRuleRel);
        List<String> cfgLibraryBids = new ArrayList<>();
        cfgLibraryBids.add(cfgFileLibraryRuleRelVo.getSourceLibraryBid());
        cfgLibraryBids.addAll(cfgFileLibraryRuleRelVo.getTargetLibraryBids());
        List<CfgFileLibrary> cfgFileLibraries = cfgFileLibraryService.list(Wrappers.<CfgFileLibrary>lambdaQuery().in(CfgFileLibrary::getBid, cfgLibraryBids).eq(CfgFileLibrary::getDeleteFlag, false));
        Map<String,CfgFileLibrary> cfgFileLibraryMap = new HashMap<>();
        for (CfgFileLibrary cfgFileLibrary : cfgFileLibraries) {
            cfgFileLibraryMap.put(cfgFileLibrary.getBid(),cfgFileLibrary);
        }
        cfgFileLibraryRuleRelVo.setSourceLibrary(CfgFileLibraryConverter.INSTANCE.po2vo(cfgFileLibraryMap.get(cfgFileLibraryRuleRel.getSourceLibraryBid())));
        List<CfgFileLibrary> targetLibrarys = new ArrayList<>();
        for (String targetLibraryBid : cfgFileLibraryRuleRel.getTargetLibraryBids()) {
            if(cfgFileLibraryMap.containsKey(targetLibraryBid)){
                targetLibrarys.add(cfgFileLibraryMap.get(targetLibraryBid));
            }
        }
        List<CfgFileLibraryVo> cfgFileLibraryVos = CfgFileLibraryConverter.INSTANCE.pos2vos(targetLibrarys);
        cfgFileLibraryRuleRelVo.setTargetFileLibraryList(cfgFileLibraryVos);
        return cfgFileLibraryRuleRelVo;
    }

    public boolean deleteCfgFileLibraryRuleRel(String bid) {
        return cfgFileLibraryRuleRelMapper.deleteByBid(bid) > 0;
    }

    /**
     * 通过文件类型code 查询文件复制规则(包含目标文件库)
     * @param code
     * @return
     */
    public List<CfgFileCopyRuleVo> getCfgFileCopyRuleByFileTypeCode(String code) {
        CfgFileType cfgFileType = cfgFileTypeService.getOne(Wrappers.<CfgFileType>lambdaQuery().eq(CfgFileType::getCode, code).eq(CfgFileType::getDeleteFlag, false));
        if(cfgFileType == null){
            throw new BusinessException("文件类型不存在");
        }
        String fileTypeBid = cfgFileType.getBid();
        List<CfgFileTypeRuleRel> cfgFileTypeRuleRels = cfgFileTypeRuleRelService.list(Wrappers.<CfgFileTypeRuleRel>lambdaQuery().eq(CfgFileTypeRuleRel::getFileTypeBid, fileTypeBid).eq(CfgFileTypeRuleRel::getDeleteFlag, false));
        if(CollectionUtils.isNotEmpty(cfgFileTypeRuleRels)){
            List<String> ruleBids = cfgFileTypeRuleRels.stream().map(CfgFileTypeRuleRel::getRuleBid).collect(Collectors.toList());
            List<CfgFileCopyRule> cfgFileCopyRules = cfgFileCopyRuleService.list(Wrappers.<CfgFileCopyRule>lambdaQuery().in(CfgFileCopyRule::getBid, ruleBids).eq(CfgFileCopyRule::getDeleteFlag, false).eq(CfgFileCopyRule::getEnableFlag, 1));
            List<CfgFileCopyRuleVo> cfgFileCopyRuleVos = CfgFileCopyRuleConverter.INSTANCE.pos2vos(cfgFileCopyRules);
            List<CfgFileLibraryRuleRel> cfgFileLibraryRuleRels = cfgFileLibraryRuleRelService.list(Wrappers.<CfgFileLibraryRuleRel>lambdaQuery().in(CfgFileLibraryRuleRel::getRuleBid, ruleBids).eq(CfgFileLibraryRuleRel::getDeleteFlag, false));
            if (CollectionUtils.isNotEmpty(cfgFileLibraryRuleRels)) {
                List<String> fileLibraryBids = new ArrayList<>();
                for (CfgFileLibraryRuleRel cfgFileLibraryRuleRel : cfgFileLibraryRuleRels) {
                    fileLibraryBids.add(cfgFileLibraryRuleRel.getSourceLibraryBid());
                    fileLibraryBids.addAll(cfgFileLibraryRuleRel.getTargetLibraryBids());
                }
                List<CfgFileLibrary> cfgFileLibraries = cfgFileLibraryService.list(Wrappers.<CfgFileLibrary>lambdaQuery().in(CfgFileLibrary::getBid, fileLibraryBids).eq(CfgFileLibrary::getDeleteFlag, false));
                Map<String,CfgFileLibrary> cfgFileLibraryMap = new HashMap<>();
                for (CfgFileLibrary cfgFileLibrary : cfgFileLibraries) {
                    cfgFileLibraryMap.put(cfgFileLibrary.getBid(),cfgFileLibrary);
                }
                List<CfgFileLibraryRuleRelVo> cfgFileLibraryRuleRelVos = new ArrayList<>();
                for (CfgFileLibraryRuleRel cfgFileLibraryRuleRel : cfgFileLibraryRuleRels) {
                    CfgFileLibraryRuleRelVo cfgFileLibraryRuleRelVo =CfgFileLibraryRuleRelConverter.INSTANCE.po2vo(cfgFileLibraryRuleRel);
                    cfgFileLibraryRuleRelVo.setRuleBid(cfgFileLibraryRuleRel.getRuleBid());
                    cfgFileLibraryRuleRelVo.setSourceLibraryBid(cfgFileLibraryRuleRel.getSourceLibraryBid());
                    cfgFileLibraryRuleRelVo.setTargetLibraryBids(cfgFileLibraryRuleRel.getTargetLibraryBids());
                    cfgFileLibraryRuleRelVo.setSourceLibrary(CfgFileLibraryConverter.INSTANCE.po2vo(cfgFileLibraryMap.get(cfgFileLibraryRuleRel.getSourceLibraryBid())));
                    List<CfgFileLibrary> targetLibrarys = new ArrayList<>();
                    for (String targetLibraryBid : cfgFileLibraryRuleRel.getTargetLibraryBids()) {
                        if(cfgFileLibraryMap.containsKey(targetLibraryBid)){
                            targetLibrarys.add(cfgFileLibraryMap.get(targetLibraryBid));
                        }
                    }
                    cfgFileLibraryRuleRelVo.setTargetFileLibraryList(CfgFileLibraryConverter.INSTANCE.pos2vos(targetLibrarys));
                    cfgFileLibraryRuleRelVos.add(cfgFileLibraryRuleRelVo);
                }
                Map<String,List<CfgFileLibraryRuleRelVo>> cfgFileLibraryRuleRelVoMap = cfgFileLibraryRuleRelVos.stream().collect(Collectors.groupingBy(CfgFileLibraryRuleRelVo::getRuleBid));
                for (CfgFileCopyRuleVo cfgFileCopyRuleVo : cfgFileCopyRuleVos) {
                    cfgFileCopyRuleVo.setCfgFileLibraryRuleRelVos(cfgFileLibraryRuleRelVoMap.get(cfgFileCopyRuleVo.getBid()));
                }
            }
            return cfgFileCopyRuleVos;
        }
        return null;
    }

    /**
     * 复制规则明细查询
     * @param bid
     * @return
     */
    @Override
    public CfgFileCopyRuleVo getCfgFileCopyRule(String bid) {
        CfgFileCopyRule cfgFileCopyRule = cfgFileCopyRuleService.getOne(Wrappers.<CfgFileCopyRule>lambdaQuery().eq(CfgFileCopyRule::getBid, bid).eq(CfgFileCopyRule::getDeleteFlag, false));
        if(cfgFileCopyRule != null) {
            CfgFileCopyRuleVo cfgFileCopyRuleVo = CfgFileCopyRuleConverter.INSTANCE.po2vo(cfgFileCopyRule);
            //组织文件类型数据
            List<CfgFileTypeRuleRel> cfgFileTypeRuleRels = cfgFileTypeRuleRelService.list(Wrappers.<CfgFileTypeRuleRel>lambdaQuery().eq(CfgFileTypeRuleRel::getRuleBid, bid).eq(CfgFileTypeRuleRel::getDeleteFlag, false));
            if (CollectionUtils.isNotEmpty(cfgFileTypeRuleRels)) {
                List<String> fileTypeBids = cfgFileTypeRuleRels.stream().map(CfgFileTypeRuleRel::getFileTypeBid).collect(Collectors.toList());
                List<CfgFileType> cfgFileTypes = cfgFileTypeService.list(Wrappers.<CfgFileType>lambdaQuery().in(CfgFileType::getBid, fileTypeBids).eq(CfgFileType::getDeleteFlag, false));
                List<CfgFileTypeVo> cfgFileTypeVos = CfgFileTypeConverter.INSTANCE.pos2vos(cfgFileTypes);
                cfgFileCopyRuleVo.setCfgFileTypeVos(cfgFileTypeVos);
            }
            //获取文件库配置信息
            List<CfgFileLibraryRuleRel> cfgFileLibraryRuleRels = cfgFileLibraryRuleRelService.list(Wrappers.<CfgFileLibraryRuleRel>lambdaQuery().eq(CfgFileLibraryRuleRel::getRuleBid, bid).eq(CfgFileLibraryRuleRel::getDeleteFlag, false));
            if (CollectionUtils.isNotEmpty(cfgFileLibraryRuleRels)) {
                List<String> fileLibraryBids = new ArrayList<>();
                for (CfgFileLibraryRuleRel cfgFileLibraryRuleRel : cfgFileLibraryRuleRels) {
                    fileLibraryBids.add(cfgFileLibraryRuleRel.getSourceLibraryBid());
                    fileLibraryBids.addAll(cfgFileLibraryRuleRel.getTargetLibraryBids());
                }
                //查询文件库信息
                List<CfgFileLibrary> cfgFileLibraries = cfgFileLibraryService.list(Wrappers.<CfgFileLibrary>lambdaQuery().in(CfgFileLibrary::getBid, fileLibraryBids).eq(CfgFileLibrary::getDeleteFlag, false));
                Map<String,CfgFileLibrary> cfgFileLibraryMap = new HashMap<>();
                for (CfgFileLibrary cfgFileLibrary : cfgFileLibraries) {
                    cfgFileLibraryMap.put(cfgFileLibrary.getBid(),cfgFileLibrary);
                }
                List<CfgFileLibraryRuleRelVo> cfgFileLibraryRuleRelVos = new ArrayList<>();
                for (CfgFileLibraryRuleRel cfgFileLibraryRuleRel : cfgFileLibraryRuleRels) {
                    CfgFileLibraryRuleRelVo cfgFileLibraryRuleRelVo =CfgFileLibraryRuleRelConverter.INSTANCE.po2vo(cfgFileLibraryRuleRel);
                    cfgFileLibraryRuleRelVo.setRuleBid(cfgFileLibraryRuleRel.getRuleBid());
                    cfgFileLibraryRuleRelVo.setSourceLibraryBid(cfgFileLibraryRuleRel.getSourceLibraryBid());
                    cfgFileLibraryRuleRelVo.setTargetLibraryBids(cfgFileLibraryRuleRel.getTargetLibraryBids());
                    cfgFileLibraryRuleRelVo.setSourceLibrary(CfgFileLibraryConverter.INSTANCE.po2vo(cfgFileLibraryMap.get(cfgFileLibraryRuleRel.getSourceLibraryBid())));
                    List<CfgFileLibrary> targetLibrarys = new ArrayList<>();
                    for (String targetLibraryBid : cfgFileLibraryRuleRel.getTargetLibraryBids()) {
                        if(cfgFileLibraryMap.containsKey(targetLibraryBid)){
                            targetLibrarys.add(cfgFileLibraryMap.get(targetLibraryBid));
                        }
                    }
                    cfgFileLibraryRuleRelVo.setTargetFileLibraryList(CfgFileLibraryConverter.INSTANCE.pos2vos(targetLibrarys));
                    cfgFileLibraryRuleRelVos.add(cfgFileLibraryRuleRelVo);
                }
                cfgFileCopyRuleVo.setCfgFileLibraryRuleRelVos(cfgFileLibraryRuleRelVos);
            }
            return cfgFileCopyRuleVo;
        }
        return null;
    }

    private void setOtherDefaultLibrary(){
        List<CfgFileLibrary> cfgFileLibraries = cfgFileLibraryService.list(Wrappers.<CfgFileLibrary>lambdaQuery().eq(CfgFileLibrary::getDefaultFlag, true).eq(CfgFileLibrary::getDeleteFlag, false));
        if(!CollectionUtils.isEmpty(cfgFileLibraries)){
            List<String> bids = cfgFileLibraries.stream().map(CfgFileLibrary::getBid).collect(Collectors.toList());
            cfgFileLibraryService.update(Wrappers.<CfgFileLibrary>lambdaUpdate().set(CfgFileLibrary::getDefaultFlag, false).in(CfgFileLibrary::getBid, bids));
        }
    }

    public boolean deleteCfgLibrary(String bid){
       return cfgFileLibraryMapper.deleteByBid(bid) > 0;
    }

    public boolean deleteCfgFileViewer(String bid){
        return cfgFileViewerMapper.deleteByBid(bid) > 0;
    }

    public boolean deleteCfgFileCopyRule(String bid){
        return cfgFileCopyRuleMapper.deleteByBid(bid) > 0;
    }

    public boolean updateCfgLibraryEnableFlag(CfgFileLibraryDto cfgFileLibraryDto){
        CfgFileLibrary cfgFileLibrary = cfgFileLibraryService.getOne(Wrappers.<CfgFileLibrary>lambdaQuery().eq(CfgFileLibrary::getBid, cfgFileLibraryDto.getBid()).eq(CfgFileLibrary::getDeleteFlag, false));
        if(cfgFileLibrary == null){
            throw new BusinessException("文件库不存在");
        }
        cfgFileLibrary.setEnableFlag(cfgFileLibraryDto.getEnableFlag());
        return cfgFileLibraryService.updateById(cfgFileLibrary);
    }


    public boolean updateCfgFileTypeEnableFlag(CfgFileTypeDto dto){
        CfgFileType cfgFileType = cfgFileTypeService.getOne(Wrappers.<CfgFileType>lambdaQuery().eq(CfgFileType::getBid, dto.getBid()).eq(CfgFileType::getDeleteFlag, false));
        if(cfgFileType == null){
            throw new BusinessException("文件类型不存在");
        }
        cfgFileType.setEnableFlag(dto.getEnableFlag());
        return cfgFileTypeService.updateById(cfgFileType);
    }

    /**
     * 保存或修改文件库
     *
     * @param cfgFileLibraryDto
     * @return
     */
    @Override
    public CfgFileLibrary saveCfgFileLibrary(CfgFileLibraryDto cfgFileLibraryDto) {
        if(StringUtils.isEmpty(cfgFileLibraryDto.getBid())){
            //新增
            long count = cfgFileLibraryService.count(Wrappers.<CfgFileLibrary>lambdaQuery().eq(CfgFileLibrary::getName, cfgFileLibraryDto.getName()).eq(CfgFileLibrary::getDeleteFlag, false));
            if(count > 0){
                throw new BusinessException("文件库名称["+cfgFileLibraryDto.getName()+"]重复");
            }
            CfgFileLibrary cfgFileLibrary = CfgFileLibraryConverter.INSTANCE.dot2po(cfgFileLibraryDto);
            cfgFileLibrary.setBid(SnowflakeIdWorker.nextIdStr());
            cfgFileLibrary.setCreatedBy(SsoHelper.getJobNumber());
            cfgFileLibrary.setUpdatedBy(SsoHelper.getJobNumber());
            cfgFileLibrary.setCreatedTime(new Date());
            cfgFileLibrary.setUpdatedTime(new Date());
            cfgFileLibrary.setDeleteFlag(false);
            if(cfgFileLibrary.getEnableFlag() == null){
                cfgFileLibrary.setEnableFlag(0);
            }
            //设置默认值
            long countAll = cfgFileLibraryService.count(Wrappers.<CfgFileLibrary>lambdaQuery().eq(CfgFileLibrary::getDeleteFlag, false));
            if(countAll == 0){
                cfgFileLibrary.setDefaultFlag(true);
            }
            //默认库处理
            if(cfgFileLibrary.getDefaultFlag()){
                setOtherDefaultLibrary();
            }
            cfgFileLibraryService.save(cfgFileLibrary);
            return cfgFileLibrary;
        }else{
            //修改
            CfgFileLibrary cfgFileLibrary = cfgFileLibraryService.getOne(Wrappers.<CfgFileLibrary>lambdaQuery().eq(CfgFileLibrary::getBid, cfgFileLibraryDto.getBid()).eq(CfgFileLibrary::getDeleteFlag, false));
            if(cfgFileLibrary != null){
                //判断名称是否重复
                if(!StringUtils.isEmpty(cfgFileLibraryDto.getName()) && !cfgFileLibraryDto.getName().equals(cfgFileLibrary.getName()) && cfgFileLibraryDto.getDeleteFlag() != null && !cfgFileLibraryDto.getDeleteFlag()){
                    //判断名称是否重复
                    long count = cfgFileLibraryService.count(Wrappers.<CfgFileLibrary>lambdaQuery().eq(CfgFileLibrary::getName, cfgFileLibraryDto.getName()).eq(CfgFileLibrary::getDeleteFlag, false));
                    if(count > 0){
                        throw new BusinessException("文件库名称["+cfgFileLibraryDto.getName()+"]重复");
                    }
                }
                if(cfgFileLibraryDto.getDefaultFlag() && !cfgFileLibrary.getDefaultFlag()){
                    setOtherDefaultLibrary();
                }
                cfgFileLibrary.setName(cfgFileLibraryDto.getName());
                cfgFileLibrary.setAddress(cfgFileLibraryDto.getAddress());
                cfgFileLibrary.setUrl(cfgFileLibraryDto.getUrl());
                cfgFileLibrary.setUpdatedBy(SsoHelper.getJobNumber());
                cfgFileLibrary.setUpdatedTime(new Date());
                cfgFileLibrary.setUrlRule(cfgFileLibraryDto.getUrlRule());
                cfgFileLibrary.setDescription(cfgFileLibraryDto.getDescription());
                if(cfgFileLibraryDto.getDefaultFlag() != null){
                    cfgFileLibrary.setDefaultFlag(cfgFileLibraryDto.getDefaultFlag());
                }
                if(cfgFileLibraryDto.getEnableFlag() != null){
                    cfgFileLibrary.setEnableFlag(cfgFileLibraryDto.getEnableFlag());
                }
                if(cfgFileLibraryDto.getDeleteFlag() != null){
                    cfgFileLibrary.setDeleteFlag(cfgFileLibraryDto.getDeleteFlag());
                }
                cfgFileLibrary.setEndpoint(cfgFileLibraryDto.getEndpoint());
                cfgFileLibrary.setSecretKey(cfgFileLibraryDto.getSecretKey());
                cfgFileLibrary.setAccessKey(cfgFileLibraryDto.getAccessKey());
                cfgFileLibraryService.updateById(cfgFileLibrary);
                return cfgFileLibrary;
            }
        }
        return null;
    }

    /**
     * 添加或删除文件类型查看器
     * @param cfgFileTypeViewerRelDto
     * @return
     */
    public boolean addOrDeleteFileTypeViewer(CfgFileTypeViewerRelDto cfgFileTypeViewerRelDto){
        if(StringUtils.isEmpty(cfgFileTypeViewerRelDto.getFileTypeBid()) || CollectionUtils.isEmpty(cfgFileTypeViewerRelDto.getFileViewerBidList())){
            throw new BusinessException("参数不能为空");
        }
        CfgFileType cfgFileType = cfgFileTypeService.getOne(new LambdaQueryWrapper<CfgFileType>().eq(CfgFileType::getBid, cfgFileTypeViewerRelDto.getFileTypeBid()).eq(CfgFileType::getDeleteFlag, false));
        if(cfgFileType == null){
            throw new BusinessException("文件类型不存在");
        }
        List<String>  viewerBids = cfgFileType.getFileViewerBids();
        if(viewerBids == null){
            viewerBids = new ArrayList<>();
        }
        if("add".equals(cfgFileTypeViewerRelDto.getOperateType())){
           //新增
            for(String viewerBid : cfgFileTypeViewerRelDto.getFileViewerBidList()){
                if(!viewerBids.contains(viewerBid)){
                    viewerBids.add(viewerBid);
                }
            }
       }else if ("delete".equals(cfgFileTypeViewerRelDto.getOperateType())){
            //删除
            viewerBids.removeAll(cfgFileTypeViewerRelDto.getFileViewerBidList());
        }
        cfgFileType.setFileViewerBids(viewerBids);
       return cfgFileTypeService.updateById(cfgFileType);
    }

    public CfgFileTypeVo getCfgFileTypeByCode(String code){
        CfgFileType cfgFileType = cfgFileTypeService.getOne(new LambdaQueryWrapper<CfgFileType>().eq(CfgFileType::getCode, code).eq(CfgFileType::getDeleteFlag, false));
        if(cfgFileType != null){
            CfgFileTypeVo cfgFileTypeVo = CfgFileTypeConverter.INSTANCE.po2vo(cfgFileType);
            List<String> fileViewerBids = cfgFileTypeVo.getFileViewerBids();
            if(CollectionUtils.isNotEmpty(fileViewerBids)){
                List<CfgFileViewer> cfgFileViewers = cfgFileViewerService.list(new LambdaQueryWrapper<CfgFileViewer>().in(CfgFileViewer::getBid, fileViewerBids).eq(CfgFileViewer::getDeleteFlag, false));
                if(CollectionUtils.isNotEmpty(cfgFileViewers)){
                    List<CfgFileViewerVo> cfgFileViewerVos = CfgFileViewerConverter.INSTANCE.pos2vos(cfgFileViewers);
                    cfgFileTypeVo.setCfgFileViewerVos(cfgFileViewerVos);
                }
            }
            return cfgFileTypeVo;
        }
        return null;
    }

    public CfgFileTypeVo getCfgFileTypeByBid(String bid){
        CfgFileType cfgFileType = cfgFileTypeService.getOne(new LambdaQueryWrapper<CfgFileType>().eq(CfgFileType::getBid, bid).eq(CfgFileType::getDeleteFlag, false));
        if(cfgFileType != null){
            CfgFileTypeVo cfgFileTypeVo = CfgFileTypeConverter.INSTANCE.po2vo(cfgFileType);
            List<String> fileViewerBids = cfgFileTypeVo.getFileViewerBids();
            if(CollectionUtils.isNotEmpty(fileViewerBids)){
                List<CfgFileViewer> cfgFileViewers = cfgFileViewerService.list(new LambdaQueryWrapper<CfgFileViewer>().in(CfgFileViewer::getBid, fileViewerBids).eq(CfgFileViewer::getDeleteFlag, false));
                if(CollectionUtils.isNotEmpty(cfgFileViewers)){
                    List<CfgFileViewerVo> cfgFileViewerVos = CfgFileViewerConverter.INSTANCE.pos2vos(cfgFileViewers);
                    cfgFileTypeVo.setCfgFileViewerVos(cfgFileViewerVos);
                }
            }
            return cfgFileTypeVo;
        }
        return null;
    }

    public boolean deleteFileType(String fileTypeBid){
        return cfgFileTypeMapper.deleteByBid(fileTypeBid) > 0;
    }

    /**
     * 文件类型列表
     * @param pageQo
     * @return
     */
    public PagedResult<CfgFileTypeVo> listCfgFileType(BaseRequest<CfgFileTypeDto> pageQo){
        CfgFileTypeDto dto = pageQo.getParam();
        Page<CfgFileType> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgFileType> queryWrapper = Wrappers.<CfgFileType> lambdaQuery();
        queryWrapper.eq(CfgFileType::getDeleteFlag, false);
        if (StringUtil.isNotBlank(dto.getName())) {
            queryWrapper.like(CfgFileType::getName, dto.getName());
        }
        if(dto.getEnableFlag() != null){
            queryWrapper.eq(CfgFileType::getEnableFlag, dto.getEnableFlag());
        }
        IPage<CfgFileType> iPage = cfgFileTypeService.page(page, queryWrapper);
        List<CfgFileTypeVo> cfgFileTypeVos = CfgFileTypeConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage, cfgFileTypeVos);
    }

    public CfgFileType saveCfgFileType(CfgFileTypeDto dto){
        if(StringUtils.isEmpty(dto.getBid())){
            if(cfgFileTypeService.count(Wrappers.<CfgFileType>lambdaQuery().eq(CfgFileType::getCode, dto.getCode()).eq(CfgFileType::getDeleteFlag, false))>0){
                throw new BusinessException("文件类型标识["+dto.getCode()+"]重复");
            }
            //新增
            CfgFileType cfgFileType = CfgFileTypeConverter.INSTANCE.dto2po(dto);
            cfgFileType.setCreatedBy(SsoHelper.getJobNumber());
            cfgFileType.setCreatedTime(new Date());
            cfgFileType.setUpdatedBy(SsoHelper.getJobNumber());
            cfgFileType.setUpdatedTime(new Date());
            cfgFileType.setBid(SnowflakeIdWorker.nextIdStr());
            cfgFileType.setDeleteFlag(false);
            if(cfgFileType.getEnableFlag() == null){
                cfgFileType.setEnableFlag(0);
            }
            cfgFileTypeService.save(cfgFileType);
            return cfgFileType;
        }else{
            //修改
            CfgFileType cfgFileType = cfgFileTypeService.getOne(Wrappers.<CfgFileType>lambdaQuery().eq(CfgFileType::getBid, dto.getBid()).eq(CfgFileType::getDeleteFlag, false));
            if(cfgFileType != null){
                cfgFileType.setDescription(dto.getDescription());
                cfgFileType.setEnableFlag(dto.getEnableFlag());
                cfgFileType.setUpdatedBy(SsoHelper.getJobNumber());
                cfgFileType.setUpdatedTime(new Date());
                cfgFileType.setDeleteFlag(dto.getDeleteFlag());
                cfgFileType.setMatching(dto.getMatching());
                cfgFileType.setName(dto.getName());
                cfgFileType.setMimeType(dto.getMimeType());
                cfgFileType.setStorageRule(dto.getStorageRule());
                cfgFileType.setSuffixName(dto.getSuffixName());
                cfgFileType.setReadRule(dto.getReadRule());
                cfgFileType.setPriority(dto.getPriority());
                if(dto.getEnableFlag() != null){
                    cfgFileType.setEnableFlag(dto.getEnableFlag());
                }
                cfgFileTypeService.updateById(cfgFileType);
                return cfgFileType;
            }
        }
        return null;
    }

    public PagedResult<CfgFileCopyRuleVo> listCfgFileCopyRule(BaseRequest<CfgFileCopyRuleDto> pageQo){
        CfgFileCopyRuleDto dto = pageQo.getParam();
        Page<CfgFileCopyRule> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgFileCopyRule> queryWrapper = Wrappers.<CfgFileCopyRule> lambdaQuery();
        queryWrapper.eq(CfgFileCopyRule::getDeleteFlag, false);
        if (StringUtil.isNotBlank(dto.getName())) {
            queryWrapper.like(CfgFileCopyRule::getName, dto.getName());
        }
        if (StringUtil.isNotBlank(dto.getCopyEvent())) {
            queryWrapper.eq(CfgFileCopyRule::getCopyEvent, dto.getCopyEvent());
        }
        if (StringUtil.isNotBlank(dto.getCopyMode())) {
            queryWrapper.eq(CfgFileCopyRule::getCopyMode, dto.getCopyMode());
        }
        if (StringUtil.isNotBlank(dto.getCopyType())) {
            queryWrapper.eq(CfgFileCopyRule::getCopyType, dto.getCopyType());
        }
        IPage<CfgFileCopyRule> iPage = cfgFileCopyRuleService.page(page, queryWrapper);
        List<CfgFileCopyRuleVo> cfgFileCopyRuleVos = CfgFileCopyRuleConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage, cfgFileCopyRuleVos);
    }

    public boolean updateCfgFileCopyRuleEnable(CfgFileCopyRuleDto dto) {
        CfgFileCopyRule cfgFileCopyRule = cfgFileCopyRuleService.getOne(Wrappers.<CfgFileCopyRule>lambdaQuery().eq(CfgFileCopyRule::getBid, dto.getBid()).eq(CfgFileCopyRule::getDeleteFlag, false));
        if (cfgFileCopyRule == null) {
            throw new BusinessException("未找到对应的文件复制规则");
        }
        cfgFileCopyRule.setEnableFlag(dto.getEnableFlag());
        return cfgFileCopyRuleService.updateById(cfgFileCopyRule);
    }

    @Override
    public CfgFileCopyRule saveCfgFileCopyRule(CfgFileCopyRuleDto dto) {
        if(StringUtils.isEmpty(dto.getBid())){
            //新增
            //判断名称是否重复
            if(cfgFileCopyRuleService.count(Wrappers.<CfgFileCopyRule>lambdaQuery().eq(CfgFileCopyRule::getName, dto.getName()).eq(CfgFileCopyRule::getDeleteFlag, false))>0){
                throw new BusinessException("复制规则名称["+dto.getName()+"]重复");
            }
            CfgFileCopyRule cfgFileCopyRule = CfgFileCopyRuleConverter.INSTANCE.dto2po(dto);
            cfgFileCopyRule.setBid(SnowflakeIdWorker.nextIdStr());
            cfgFileCopyRule.setCreatedBy(SsoHelper.getJobNumber());
            cfgFileCopyRule.setCreatedTime(new Date());
            cfgFileCopyRule.setUpdatedBy(SsoHelper.getJobNumber());
            cfgFileCopyRule.setUpdatedTime(new Date());
            cfgFileCopyRule.setDeleteFlag(false);
            if(cfgFileCopyRule.getEnableFlag() == null){
                cfgFileCopyRule.setEnableFlag(0);
            }
            cfgFileCopyRuleService.save(cfgFileCopyRule);
            return cfgFileCopyRule;
        }else{
            //修改
            CfgFileCopyRule cfgFileCopyRule = cfgFileCopyRuleService.getOne(Wrappers.<CfgFileCopyRule>lambdaQuery().eq(CfgFileCopyRule::getBid, dto.getBid()).eq(CfgFileCopyRule::getDeleteFlag, false));
            if(cfgFileCopyRule != null){
                if(!StringUtils.isEmpty(dto.getName()) && !dto.getName().equals(cfgFileCopyRule.getName()) && dto.getDeleteFlag() != null && !dto.getDeleteFlag()){
                    //判断名称是否重复
                    if(cfgFileCopyRuleService.count(Wrappers.<CfgFileCopyRule>lambdaQuery().eq(CfgFileCopyRule::getName, dto.getName()).eq(CfgFileCopyRule::getDeleteFlag, false))>0){
                        throw new BusinessException("复制规则名称["+dto.getName()+"]重复");
                    }
                }
                cfgFileCopyRule.setCopyMode(dto.getCopyMode());
                cfgFileCopyRule.setCopyEvent(dto.getCopyEvent());
                cfgFileCopyRule.setCopyType(dto.getCopyType());
                cfgFileCopyRule.setDescription(dto.getDescription());
                cfgFileCopyRule.setPlanTime(dto.getPlanTime());
                cfgFileCopyRule.setDelayDuration(dto.getDelayDuration());
                cfgFileCopyRule.setFilterMethod(dto.getFilterMethod());
                cfgFileCopyRule.setName(dto.getName());
                cfgFileCopyRule.setTimeOut(dto.getTimeOut());
                if(dto.getEnableFlag() != null){
                    cfgFileCopyRule.setEnableFlag(dto.getEnableFlag());
                }
                if(dto.getDeleteFlag() != null){
                    cfgFileCopyRule.setDeleteFlag(dto.getDeleteFlag());
                }
                cfgFileCopyRule.setUpdatedBy(SsoHelper.getJobNumber());
                cfgFileCopyRule.setUpdatedTime(new Date());
                cfgFileCopyRuleService.updateById(cfgFileCopyRule);
                return cfgFileCopyRule;
            }
        }
        return null;
    }

    public boolean updateFileViewerEnable(CfgFileViewerDto dto) {
        if(StringUtils.isEmpty(dto.getBid())){
            throw new BusinessException("文件查看器ID不能为空");
        }
        CfgFileViewer cfgFileViewer = cfgFileViewerService.getOne(Wrappers.<CfgFileViewer>lambdaQuery().eq(CfgFileViewer::getBid, dto.getBid()).eq(CfgFileViewer::getDeleteFlag, false));
        if(cfgFileViewer == null){
            throw new BusinessException("文件查看器ID["+dto.getBid()+"]不存在");
        }
        cfgFileViewer.setEnableFlag(dto.getEnableFlag());
        cfgFileViewer.setUpdatedBy(SsoHelper.getJobNumber());
        cfgFileViewer.setUpdatedTime(new Date());
        return cfgFileViewerService.updateById(cfgFileViewer);
    }

    @Override
    public CfgFileViewer saveCfgFileViewer(CfgFileViewerDto dto) {
        if(StringUtils.isEmpty(dto.getBid())){
            //新增
            //判断名称是否重复
            if(cfgFileViewerService.count(Wrappers.<CfgFileViewer>lambdaQuery().eq(CfgFileViewer::getName, dto.getName()).eq(CfgFileViewer::getDeleteFlag, false)) > 0){
                throw new BusinessException("查看器名称["+dto.getName()+"]重复");
            }
            CfgFileViewer cfgFileViewer = CfgFileViewerConverter.INSTANCE.dto2po(dto);
            cfgFileViewer.setBid(SnowflakeIdWorker.nextIdStr());
            cfgFileViewer.setCreatedTime(new Date());
            cfgFileViewer.setUpdatedTime(new Date());
            cfgFileViewer.setCreatedBy(SsoHelper.getJobNumber());
            cfgFileViewer.setUpdatedBy(SsoHelper.getJobNumber());
            cfgFileViewer.setDeleteFlag(false);
            if(cfgFileViewer.getEnableFlag() == null){
                cfgFileViewer.setEnableFlag(0);
            }
            cfgFileViewerService.save(cfgFileViewer);
            return cfgFileViewer;
        }else{
            //修改
            CfgFileViewer cfgFileViewer = cfgFileViewerService.getOne(Wrappers.<CfgFileViewer>lambdaQuery().eq(CfgFileViewer::getBid, dto.getBid()).eq(CfgFileViewer::getDeleteFlag, false));
            if(cfgFileViewer != null){
                if(!StringUtils.isEmpty(dto.getName()) && !dto.getName().equals(cfgFileViewer.getName()) && dto.getDeleteFlag() != null && !dto.getDeleteFlag()){
                    if(cfgFileViewerService.count(Wrappers.<CfgFileViewer>lambdaQuery().eq(CfgFileViewer::getName, dto.getName()).eq(CfgFileViewer::getDeleteFlag, false)) > 0){
                        throw new BusinessException("查看器名称["+dto.getName()+"]重复");
                    }
                }
                cfgFileViewer.setName(dto.getName());
                cfgFileViewer.setUrl(dto.getUrl());
                cfgFileViewer.setDescription(dto.getDescription());
                cfgFileViewer.setUpdatedTime(new Date());
                cfgFileViewer.setUpdatedBy(SsoHelper.getJobNumber());
                if(dto.getEnableFlag() != null){
                    cfgFileViewer.setEnableFlag(dto.getEnableFlag());
                }
                if(dto.getDeleteFlag() != null){
                    cfgFileViewer.setDeleteFlag(dto.getDeleteFlag());
                }
                cfgFileViewerService.updateById(cfgFileViewer);
                return cfgFileViewer;
            }
        }
        return null;
    }

    @Override
    public PagedResult<CfgFileViewerVo> listCfgFileViewer(BaseRequest<CfgFileViewerDto> pageQo) {
        CfgFileViewerDto dto = pageQo.getParam();
        Page<CfgFileViewer> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgFileViewer> queryWrapper = Wrappers.<CfgFileViewer> lambdaQuery();
        queryWrapper.eq(CfgFileViewer::getDeleteFlag, false);
        if (StringUtil.isNotBlank(dto.getName())) {
            queryWrapper.like(CfgFileViewer::getName, dto.getName());
        }
        if(dto.getEnableFlag() != null){
            queryWrapper.eq(CfgFileViewer::getEnableFlag, dto.getEnableFlag());
        }
        IPage<CfgFileViewer> iPage = cfgFileViewerService.page(page, queryWrapper);
        List<CfgFileViewerVo> cfgFileViewerVos = CfgFileViewerConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage, cfgFileViewerVos);
    }


    public CfgFileViewerVo getCfgFileViewerByBid(String bid) {
        CfgFileViewer cfgFileViewer = cfgFileViewerService.getOne(Wrappers.<CfgFileViewer>lambdaQuery().eq(CfgFileViewer::getBid, bid).eq(CfgFileViewer::getDeleteFlag, false));
        return CfgFileViewerConverter.INSTANCE.po2vo(cfgFileViewer);
    }

    /**
     * 文件类型和规则的相互选取
     * @param dto
     * @return
     */
    @Override
    public boolean saveOrDeleteFileTypeRuleRel(CfgFileTypeRuleRelDto dto) {
        if(dto != null){
            if(!StringUtils.isEmpty(dto.getRuleBid())){
                //规则下挂文件类型
                if(CollectionUtils.isNotEmpty(dto.getFileTypeBids())){
                    if("add".equals(dto.getOperateType())){
                        //新增
                        List<CfgFileTypeRuleRel> cfgFileTypeRuleRels = new ArrayList<>();
                        for(String fileTypeBid : dto.getFileTypeBids()){
                            CfgFileTypeRuleRel cfgFileTypeRuleRel = new CfgFileTypeRuleRel();
                            cfgFileTypeRuleRel.setRuleBid(dto.getRuleBid());
                            cfgFileTypeRuleRel.setFileTypeBid(fileTypeBid);
                            cfgFileTypeRuleRel.setBid(SnowflakeIdWorker.nextIdStr());
                            cfgFileTypeRuleRel.setCreatedTime(new Date());
                            cfgFileTypeRuleRel.setCreatedBy(SsoHelper.getJobNumber());
                            cfgFileTypeRuleRel.setDeleteFlag(false);
                            cfgFileTypeRuleRels.add(cfgFileTypeRuleRel);
                        }
                        return cfgFileTypeRuleRelService.saveBatch(cfgFileTypeRuleRels);
                    }else if ("delete".equals(dto.getOperateType())){
                        //删除
                        return cfgFileTypeRuleRelMapper.deleteByRuleBid(dto.getRuleBid(),dto.getFileTypeBids()) > 0;
                    }
                }
            }else if (!StringUtils.isEmpty(dto.getFileTypeBid())){
                //文件类型下挂规则
                if(CollectionUtils.isNotEmpty(dto.getRuleBids())){
                    if("add".equals(dto.getOperateType())){
                        List<CfgFileTypeRuleRel> cfgFileTypeRuleRels = new ArrayList<>();
                        for(String ruleBid : dto.getRuleBids()){
                            CfgFileTypeRuleRel cfgFileTypeRuleRel = new CfgFileTypeRuleRel();
                            cfgFileTypeRuleRel.setRuleBid(ruleBid);
                            cfgFileTypeRuleRel.setFileTypeBid(dto.getFileTypeBid());
                            cfgFileTypeRuleRel.setBid(SnowflakeIdWorker.nextIdStr());
                            cfgFileTypeRuleRel.setCreatedTime(new Date());
                            cfgFileTypeRuleRel.setCreatedBy(SsoHelper.getJobNumber());
                            cfgFileTypeRuleRel.setDeleteFlag(false);
                            cfgFileTypeRuleRels.add(cfgFileTypeRuleRel);
                        }
                        return cfgFileTypeRuleRelService.saveBatch(cfgFileTypeRuleRels);
                    }else if ("delete".equals(dto.getOperateType())){
                        //删除
                       return cfgFileTypeRuleRelMapper.deleteByFileTypeBid(dto.getFileTypeBid(),dto.getRuleBids()) > 0;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 根据规则bid找文件类型数据
     * @param ruleBid
     * @return
     */
    @Override
    public List<CfgFileTypeVo> listCfgFileTypeByRuleBid(String ruleBid) {
        List<CfgFileTypeRuleRel> cfgFileTypeRuleRels = cfgFileTypeRuleRelService.list(Wrappers.<CfgFileTypeRuleRel>lambdaQuery().in(CfgFileTypeRuleRel::getRuleBid,ruleBid).eq(CfgFileTypeRuleRel::getDeleteFlag, false));
        if(CollectionUtils.isNotEmpty(cfgFileTypeRuleRels)){
            List<String> fileTypeBids = cfgFileTypeRuleRels.stream().map(CfgFileTypeRuleRel::getFileTypeBid).collect(Collectors.toList());
            List<CfgFileType> cfgFileTypes = cfgFileTypeService.list(Wrappers.<CfgFileType>lambdaQuery().in(CfgFileType::getBid,fileTypeBids).eq(CfgFileType::getDeleteFlag,false));
            List<CfgFileTypeVo> cfgFileTypeVos = CfgFileTypeConverter.INSTANCE.pos2vos(cfgFileTypes);
            return cfgFileTypeVos;
        }
        return null;
    }

    @Override
    public List<CfgFileCopyRuleVo> listCfgFileCopyRuleByFileTypeBid(String fileTypeBid) {
        List<CfgFileTypeRuleRel> cfgFileTypeRuleRels = cfgFileTypeRuleRelService.list(Wrappers.<CfgFileTypeRuleRel>lambdaQuery().in(CfgFileTypeRuleRel::getFileTypeBid,fileTypeBid).eq(CfgFileTypeRuleRel::getDeleteFlag, false));
        if(CollectionUtils.isNotEmpty(cfgFileTypeRuleRels)){
            List<String> ruleBids = cfgFileTypeRuleRels.stream().map(CfgFileTypeRuleRel::getRuleBid).collect(Collectors.toList());
            List<CfgFileCopyRule> cfgFileCopyRules = cfgFileCopyRuleService.list(Wrappers.<CfgFileCopyRule>lambdaQuery().in(CfgFileCopyRule::getBid,ruleBids).eq(CfgFileCopyRule::getDeleteFlag,false));
            List<CfgFileCopyRuleVo> cfgFileCopyRuleVos = CfgFileCopyRuleConverter.INSTANCE.pos2vos(cfgFileCopyRules);
            return cfgFileCopyRuleVos;
        }
        return null;
    }


    /**
     * 查询默认库
     * @return
     */
    public CfgFileLibraryVo getDefaultCfgFileLibrary(){
        List<CfgFileLibrary> cfgFileLibrarys = cfgFileLibraryService.list(Wrappers.<CfgFileLibrary>lambdaQuery().eq(CfgFileLibrary::getDefaultFlag, true).eq(CfgFileLibrary::getDeleteFlag, false));
        if(CollectionUtils.isNotEmpty(cfgFileLibrarys)){
            CfgFileLibraryVo cfgFileLibraryVo = CfgFileLibraryConverter.INSTANCE.po2vo(cfgFileLibrarys.get(0));
            return cfgFileLibraryVo;
        }
        return null;
    }

    /**
     * 根据地址查文件库
     * @param address
     * @return
     */
    public CfgFileLibraryVo getCfgFileLibraryByAddress(String address){
        List<CfgFileLibrary> cfgFileLibrarys = cfgFileLibraryService.list(Wrappers.<CfgFileLibrary>lambdaQuery().eq(CfgFileLibrary::getAddress, address).eq(CfgFileLibrary::getDeleteFlag, false));
        if(CollectionUtils.isNotEmpty(cfgFileLibrarys)){
            CfgFileLibraryVo cfgFileLibraryVo = CfgFileLibraryConverter.INSTANCE.po2vo(cfgFileLibrarys.get(0));
            return cfgFileLibraryVo;
        }
        return null;
    }

    public List<CfgFileLibraryVo> getCfgFileLibraryByCodes(List<String> codes){
        List<CfgFileLibrary> cfgFileLibrarys = cfgFileLibraryService.list(Wrappers.<CfgFileLibrary>lambdaQuery().in(CfgFileLibrary::getCode, codes).eq(CfgFileLibrary::getDeleteFlag, false));
        List<CfgFileLibraryVo> cfgFileLibraryVos = CfgFileLibraryConverter.INSTANCE.pos2vos(cfgFileLibrarys);
        return cfgFileLibraryVos;
    }

    @Override
    public List<CfgFileTypeRelRuleNameVo> getCfgRuleListByName(String name) {
        List<CfgFileTypeRelRuleNameVo> cfgFileCopyRuleVoList = Lists.newArrayList();
        if (StringUtil.isBlank(name)) {
            return cfgFileCopyRuleVoList;
        }
        cfgFileCopyRuleVoList = cfgFileTypeRuleRelService.queryFileTypeCodeWithRuleNameList(name);
        return cfgFileCopyRuleVoList;
    }

    @Override
    public List<CfgFileLibraryVo> listCfgLiabrary() {
        List<CfgFileLibrary> cfgFileLibrarys = cfgFileLibraryService.list(Wrappers.<CfgFileLibrary>lambdaQuery().eq(CfgFileLibrary::getDeleteFlag, false).eq(CfgFileLibrary::getEnableFlag, 1));
        if(CollectionUtils.isNotEmpty(cfgFileLibrarys)){
            return CfgFileLibraryConverter.INSTANCE.pos2vos(cfgFileLibrarys);
        }
        return Lists.newArrayList();
    }

    @Override
    public List<CfgFileTypeVo> queryCfgFileTypeAll() {
        List<CfgFileType> list = cfgFileTypeService.list(Wrappers.<CfgFileType>lambdaQuery()
                        .eq(CfgFileType::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                .eq(CfgFileType::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE));
        return CfgFileTypeConverter.INSTANCE.pos2vos(list);
    }
}
