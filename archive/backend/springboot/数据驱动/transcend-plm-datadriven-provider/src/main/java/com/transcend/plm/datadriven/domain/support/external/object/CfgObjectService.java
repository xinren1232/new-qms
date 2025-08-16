package com.transcend.plm.datadriven.domain.support.external.object;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;

import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.api.model.config.ObjectAttributeVo;
import com.transcend.plm.datadriven.api.model.config.ObjectVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 对象配置 转换
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/11 15:09
 */
@Component
@Slf4j
public class CfgObjectService {

    @Resource
    private CfgObjectFeignClient cfgObjectClient;

    @Resource
    private ObjectMapper objectMapper;

    public static CfgObjectService getInstance() {
        return PlmContextHolder.getBean(CfgObjectService.class);
    }

    /**
     * 获取对象定义以及定义的属性 TODO
     * @param modelCode 模型编码
     * @return  TableDefinition
     */
    public ObjectVo getByModelCode(String modelCode) {
        // 获取表信息
        log.debug("start-->获取modelCode={},的对象以及属性信息", modelCode);
        CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(modelCode).getCheckExceptionData();
        try {
            log.debug("end-->获取modelCode={},的对象以及属性信息,结果为：{}",
                    modelCode, objectMapper.writeValueAsString(cfgObjectVo));
        } catch (JsonProcessingException e) {
            log.error("获取modelCode={},的对象以及属性信息,异常信息：{}", modelCode, e.getMessage());
            throw new RuntimeException(e);
        }
        if (null == cfgObjectVo || CollectionUtils.isEmpty(cfgObjectVo.getAttrList())){
            throw new PlmBizException("",modelCode+"：找不到对象信息，或者对象属性信息");
        }

        // 以下为转换过程
        List<CfgObjectAttributeVo> attributes = cfgObjectVo.getAttrList();
        ObjectVo objectVo = ObjectPojoConverter.INSTANCE.cfg2definition(cfgObjectVo);

        List<ObjectAttributeVo> attributeVos =
                ObjectAttritubePojoConverter.INSTANCE.cfg2definitions(attributes);
        objectVo.setAttributes(attributeVos);
        return objectVo;
    }

    /**
     * 获取对象定义以及定义的属性 TODO
     * @param baseModel 基类模型
     * @return  TableDefinition
     */
    public ObjectVo getByBaseModel(String baseModel) {
        // 获取表信息
        log.debug("start-->获取baseModel={},的对象以及属性信息", baseModel);
        CfgObjectVo cfgObjectVo = cfgObjectClient.getByBaseModel(baseModel).getCheckExceptionData();
        try {
            log.debug("end-->获取baseModel={},的对象以及属性信息,结果为：{}",
                    baseModel, objectMapper.writeValueAsString(cfgObjectVo));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (null == cfgObjectVo || CollectionUtils.isEmpty(cfgObjectVo.getAttrList())){
            throw new PlmBizException("","找不到对象信息，或者对象属性信息");
        }

        // 以下为转换过程
        List<CfgObjectAttributeVo> attributes = cfgObjectVo.getAttrList();
        ObjectVo objectVo = ObjectPojoConverter.INSTANCE.cfg2definition(cfgObjectVo);

        List<ObjectAttributeVo> attributeVos =
                ObjectAttritubePojoConverter.INSTANCE.cfg2definitions(attributes);
        objectVo.setAttributes(attributeVos);
        return objectVo;
    }

}
