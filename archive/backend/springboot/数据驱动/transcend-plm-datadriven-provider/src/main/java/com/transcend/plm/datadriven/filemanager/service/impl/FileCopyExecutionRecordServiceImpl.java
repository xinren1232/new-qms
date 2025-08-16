package com.transcend.plm.datadriven.filemanager.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.api.feign.CfgFileConfigFeignClient;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileLibraryVo;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileTypeRelRuleNameVo;
import com.transcend.plm.datadriven.api.feign.IntegrationFeignClient;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.filemanager.mapper.FileCopyExecutionRecordMapper;
import com.transcend.plm.datadriven.filemanager.mapstruct.FileCopyExecutionRecordConverter;
import com.transcend.plm.datadriven.filemanager.pojo.ao.FileCopyExecutionAo;
import com.transcend.plm.datadriven.filemanager.pojo.po.FileCopyExecutionRecordPo;
import com.transcend.plm.datadriven.filemanager.pojo.vo.FileCopyExecutionVo;
import com.transcend.plm.datadriven.filemanager.service.FileCopyExecutionRecordService;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.dto.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author bin.yin
 * @date 2024/07/24
 */
@Service
public class FileCopyExecutionRecordServiceImpl extends ServiceImpl<FileCopyExecutionRecordMapper, FileCopyExecutionRecordPo>
    implements FileCopyExecutionRecordService {
    @Resource
    private CfgFileConfigFeignClient cfgFileConfigFeignClient;
    @Resource
    private IntegrationFeignClient integrationFeignClient;

    /**
     * @param request
     * @return {@link PagedResult }<{@link FileCopyExecutionVo }>
     */
    @Override
    public PagedResult<FileCopyExecutionVo> queryPageByCondition(BaseRequest<FileCopyExecutionAo> request) {
        FileCopyExecutionAo ao = request.getParam();
        List<CfgFileTypeRelRuleNameVo> cfgFileTypeRelRuleNameVoList;
        if (StringUtils.isNotBlank(ao.getCopyRuleName())) {
            cfgFileTypeRelRuleNameVoList = cfgFileConfigFeignClient.getCfgRuleListByName(ao.getCopyRuleName()).getCheckExceptionData();
            if (CollectionUtils.isEmpty(cfgFileTypeRelRuleNameVoList)) {
                return PageResultTools.createEmpty();
            }
            ao.setFileTypeCodes(cfgFileTypeRelRuleNameVoList.stream().map(CfgFileTypeRelRuleNameVo::getFileTypeCode).collect(Collectors.toList()));
        } else {
            // 通过复制规则名称模糊查询,名称为空时,查询所有规则
            cfgFileTypeRelRuleNameVoList = cfgFileConfigFeignClient.getCfgRuleListByName("all").getCheckExceptionData();
        }
        Map<String, String> fileTypeCodeWithCopyRuleNameMap = cfgFileTypeRelRuleNameVoList.stream()
                .collect(Collectors.toMap(CfgFileTypeRelRuleNameVo::getFileTypeCode, CfgFileTypeRelRuleNameVo::getCopyRuleName, (k1, k2) -> k1));
        Page<FileCopyExecutionVo> page = PageMethod.startPage(request.getCurrent(), request.getSize());
        List<FileCopyExecutionVo> fileCopyExecutionVoList = FileCopyExecutionRecordConverter.INSTANCE.pos2Vos(baseMapper.queryPageByCondition(ao));
        if (CollectionUtils.isNotEmpty(fileCopyExecutionVoList)) {
            // 查询库名称
            Set<String> fileLibraryCodes = Sets.newHashSet();
            fileCopyExecutionVoList.forEach(vo -> {
                if (StringUtils.isNotBlank(vo.getSourceFileLibrary())) {
                    fileLibraryCodes.addAll(Lists.newArrayList(vo.getSourceFileLibrary().split(StrUtil.COMMA)));
                }
                if (StringUtils.isNotBlank(vo.getTargetFileLibrary())) {
                    fileLibraryCodes.addAll(Lists.newArrayList(vo.getTargetFileLibrary().split(StrUtil.COMMA)));
                }
            });
            Map<String, String> libraryCodeWithNameMap = cfgFileConfigFeignClient.getCfgFileLibraryByCodes(new ArrayList<>(fileLibraryCodes))
                    .getCheckExceptionData()
                    .stream().collect(Collectors.toMap(CfgFileLibraryVo::getCode, CfgFileLibraryVo::getName, (k1, k2) -> k1));
            // 字典转换
            boolean transferFlag = CollectionUtils.isNotEmpty(fileTypeCodeWithCopyRuleNameMap);
            for (FileCopyExecutionVo vo : fileCopyExecutionVoList) {
                if (transferFlag) {
                    vo.setCopyRuleName(fileTypeCodeWithCopyRuleNameMap.getOrDefault(vo.getFileType(), vo.getFileType()));
                }
                if (StringUtils.isNotBlank(vo.getSourceFileLibrary())) {
                    List<String> sourceList = Lists.newArrayList();
                    for (String source : vo.getSourceFileLibrary().split(StrUtil.COMMA)) {
                        sourceList.add(libraryCodeWithNameMap.getOrDefault(source, source));
                    }
                    vo.setSourceFileLibrary(Joiner.on(StrUtil.COMMA).join(sourceList));
                }
                if (StringUtils.isNotBlank(vo.getTargetFileLibrary())) {
                    List<String> targetList = Lists.newArrayList();
                    for (String target : vo.getTargetFileLibrary().split(StrUtil.COMMA)) {
                        targetList.add(libraryCodeWithNameMap.getOrDefault(target, target));
                    }
                    vo.setTargetFileLibrary(Joiner.on(StrUtil.COMMA).join(targetList));
                }
            }
        }
        // 由于上面po转vo时,分页信息丢失,所以这里这么写
        PageInfo<FileCopyExecutionVo> pageInfo = new PageInfo<>(page);
        return PageResultTools.create(pageInfo).setData(fileCopyExecutionVoList);
    }

    /**
     * @param recordBids
     * @return {@link Boolean }
     */
    @Override
    public Boolean batchExecute(List<String> recordBids) {
        if (CollectionUtils.isEmpty(recordBids)) {
            throw new TranscendBizException("请先勾选需要批量执行的文件");
        }
        boolean updateFlag = update(null, Wrappers.<FileCopyExecutionRecordPo>lambdaUpdate().set(FileCopyExecutionRecordPo::getExecuteState, 1).in(FileCopyExecutionRecordPo::getBid, recordBids));
        if (updateFlag) {
            BaseResponse<Boolean> baseResponse = integrationFeignClient.batchExecute(recordBids);
            if (!baseResponse.isSuccess()) {
                throw new TranscendBizException(baseResponse.getCode(), baseResponse.getMessage());
            }
            if (baseResponse.getData()) {
                return baseResponse.getData();
            } else {
                throw new TranscendBizException("integration更新执行状态失败");
            }
        } else {
            throw new TranscendBizException("更新执行状态失败");
        }
    }
}




