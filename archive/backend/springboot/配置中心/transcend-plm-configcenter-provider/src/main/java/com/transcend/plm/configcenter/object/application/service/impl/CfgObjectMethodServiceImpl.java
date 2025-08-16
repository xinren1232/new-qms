package com.transcend.plm.configcenter.object.application.service.impl;

import com.github.pagehelper.page.PageMethod;
import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.configcenter.common.web.exception.ExceptionConstructor;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectMethodService;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgModelEventMethodPo;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.object.infrastructure.po.ModelMethodPO;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgModelEventMethodService;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectMethodRepository;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectRepository;
import com.transcend.plm.configcenter.api.model.object.dto.ModelMethodDTO;
import com.transcend.plm.configcenter.api.model.object.qo.ModelMethodQO;
import com.transcend.plm.configcenter.api.model.object.vo.ModelMethodVO;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author pingzhang.chen
 * @version: 1.0
 * @date 2021/11/29 10:19
 */
@Service
public class CfgObjectMethodServiceImpl implements ICfgObjectMethodService {

    private static final String ENABLE = "enable";
    @Resource
    private CfgObjectMethodRepository cfgObjectMethodRepository;

    @Resource
    private CfgModelEventMethodService cfgModelEventMethodService;

    @Resource
    private CfgObjectRepository cfgObjectRepository;


    @Override
    public PagedResult<ModelMethodVO> page(BaseRequest<ModelMethodQO> modelMethodQO) {
        com.github.pagehelper.Page<ModelMethodVO> page = PageMethod
                .startPage(modelMethodQO.getCurrent(), modelMethodQO.getSize());
        List<ModelMethodPO> query = cfgObjectMethodRepository.query(modelMethodQO.getParam());
        List<ModelMethodVO> dataList = BeanUtil.copy(query, ModelMethodVO.class);
        return PageResultTools.create(page, dataList);
    }

    @Override
    public Boolean deleteByBid(String bid) {
        CfgModelEventMethodPo modelEventMethod = new CfgModelEventMethodPo();
        modelEventMethod.setMethodBid(bid);
        List<CfgModelEventMethodPo> eventMethodByCondition = cfgModelEventMethodService.findByCondition(modelEventMethod);
        if (CollectionUtil.isNotEmpty(eventMethodByCondition)){
            String modelCode=eventMethodByCondition.get(0).getModelCode();
            CfgObjectPo cfgObjectPo = cfgObjectRepository.getByModelCode(modelCode);
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), String.format("该方法被%s对象的事件绑定，请移除绑定后再删除", cfgObjectPo.getName()));
        }
        return cfgObjectMethodRepository.deleteByBid(bid);
    }

    @Override
    public Boolean save(ModelMethodDTO modelMethodDTO) {
        if (StringUtil.isEmpty(modelMethodDTO.getName())) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), "方法名称不能为空");
        }
        if (Objects.nonNull(cfgObjectMethodRepository.findName(modelMethodDTO.getName()))) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), "方法名称不能重复");
        }
        ModelMethodPO modelMethodPO = BeanUtil.copy(modelMethodDTO, ModelMethodPO.class);
        modelMethodPO.setBid(SnowflakeIdWorker.nextIdStr());
        if (StringUtil.isEmpty(modelMethodPO.getMethodCode())){
            modelMethodPO.setMethodCode("");
        }
        modelMethodPO.setEnableFlag(CommonConst.ENABLE_FLAG_FORBIDDEN);
        modelMethodPO.setActionMethod("");
        return cfgObjectMethodRepository.bulkInsert(Lists.newArrayList(modelMethodPO));
    }

    @Override
    public ModelMethodVO getOne(String bid) {
        ModelMethodPO modelMethodPO = cfgObjectMethodRepository.findByBid(bid);
        return BeanUtil.copy(modelMethodPO, ModelMethodVO.class);
    }

    @Override
    public Boolean update(ModelMethodDTO modelMethodDTO) {
        ModelMethodVO methodVO = getOne(modelMethodDTO.getBid());
        if (methodVO.getEnableFlag().equals(CommonConst.ENABLE_FLAG_ENABLE)&&!modelMethodDTO.getEnableFlag().equals(methodVO.getEnableFlag())){
            CfgModelEventMethodPo cfgModelEventMethod = new CfgModelEventMethodPo();
            cfgModelEventMethod.setMethodBid(modelMethodDTO.getBid());
            List<CfgModelEventMethodPo> eventMethodByCondition = cfgModelEventMethodService.findByCondition(cfgModelEventMethod);
            if (ObjectUtils.isNotEmpty(eventMethodByCondition)){
                String modelCode=eventMethodByCondition.get(0).getModelCode();
                CfgObjectPo cfgObjectPo = cfgObjectRepository.getByModelCode(modelCode);

                throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(),
                        String.format("该方法已被%s对象的事件绑定，请解绑后禁用", cfgObjectPo.getName()));
            }
        }
        if (StringUtil.isEmpty(modelMethodDTO.getName())) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), "方法名称不能为空");
        }
        ModelMethodPO modelMethodPO = cfgObjectMethodRepository.findName(modelMethodDTO.getName());
        if (Objects.nonNull(modelMethodPO) && !org.apache.commons.lang.StringUtils.equals(modelMethodDTO.getBid(), modelMethodPO.getBid())) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), "方法名称不能重复");
        }
        ModelMethodPO methodPO = BeanUtil.copy(modelMethodDTO, ModelMethodPO.class);
        return cfgObjectMethodRepository.bulkUpdate(Lists.newArrayList(methodPO));
    }
}


