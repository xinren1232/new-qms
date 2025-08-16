package com.transcend.plm.datadriven.apm.space.utils;

import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmMultiTreeDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeModelMixQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.MultiTreeConfigVo;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author jie.luo1
 */
@NoArgsConstructor
public class MultiTreeUtils {

    /**
     * 递归加载配置平铺集合
     *
     * @return 配置平铺集合
     */
    public static List<MultiTreeConfigVo> getConfigFlatList(MultiTreeConfigVo multiTreeConfigVo) {
        List<MultiTreeConfigVo> list = new ArrayList<>();
        list.add(multiTreeConfigVo);
        if (multiTreeConfigVo.getTargetModelCode() != null) {
            list.addAll(getConfigFlatList(multiTreeConfigVo.getTargetModelCode()));
        }
        return list;
    }

    /**
     * 获取实例模型编码集合
     *
     * @return 实例模型编码集合
     */
    public static List<String> getInstanceFlatModelCodes(MultiTreeConfigVo multiTreeConfigVo) {
        List<String> list = new ArrayList<>();

        if (multiTreeConfigVo == null) {
            // 如果输入对象为空，直接返回空列表
            return list;
        }

        // 添加当前节点的 sourceModelCode
        if (multiTreeConfigVo.getSourceModelCode() != null) {
            list.add(multiTreeConfigVo.getSourceModelCode());
        }

        // 递归收集 targetModelCode 下的所有代码
        if (multiTreeConfigVo.getTargetModelCode() != null) {
            list.addAll(getInstanceFlatModelCodes(multiTreeConfigVo.getTargetModelCode()));
        }

        return list;
    }


    /**
     * 获取关系模型编码集合
     *
     * @return 关系模型编码集合
     */
    public static List<String> getRelationFlatModelCodes(MultiTreeConfigVo multiTreeConfigVo) {
        List<String> list = new ArrayList<>();

        if (multiTreeConfigVo == null) {
            // 如果输入对象为空，直接返回空列表
            return list;
        }

        // 添加当前节点的 relationModelCode
        if (multiTreeConfigVo.getRelationModelCode() != null) {
            list.add(multiTreeConfigVo.getRelationModelCode());
        }

        // 递归收集 targetModelCode 下的所有关系模型编码
        if (multiTreeConfigVo.getTargetModelCode() != null) {
            list.addAll(getRelationFlatModelCodes(multiTreeConfigVo.getTargetModelCode()));
        }

        return list;
    }


    /**
     * 获取所需配置
     *
     * @param multiTreeConfigVo 配置
     * @param needModelCodes    需要的模型集合
     * @return 配置
     */
    public static MultiTreeConfigVo getNeedConfigList(MultiTreeConfigVo multiTreeConfigVo, Set<String> needModelCodes) {
        if (needModelCodes == null) {
            // 过滤 为空，返回原对象
            return multiTreeConfigVo;
        }
        if (multiTreeConfigVo == null) {
            // 如果模型集合或输入对象为空，直接返回null
            return null;
        }

        // 检查当前对象的模型代码是否在需要的集合中
        if (needModelCodes.contains(multiTreeConfigVo.getSourceModelCode())) {
            MultiTreeConfigVo config = new MultiTreeConfigVo();
            BeanUtils.copyProperties(multiTreeConfigVo, config, "targetModelCode", "relationModelCode");

            // 递归终止条件：如果没有目标模型代码，则无需进一步处理
            if (multiTreeConfigVo.getTargetModelCode() == null) {
                return config;
            }

            // 递归调用自身以获得目标模型代码的配置
            MultiTreeConfigVo targetConfig = getNeedConfigList(multiTreeConfigVo.getTargetModelCode(), needModelCodes);
            config.setTargetModelCode(targetConfig);

            // 只有在目标配置不为空时设置关系模型代码
            if (targetConfig != null
                    && Objects.equals(targetConfig.getSourceModelCode(),
                    multiTreeConfigVo.getTargetModelCode().getSourceModelCode())) {
                config.setRelationModelCode(multiTreeConfigVo.getRelationModelCode());
            }
            return config;
        }

        return getNeedConfigList(multiTreeConfigVo.getTargetModelCode(), needModelCodes);
    }

    /**
     * 将分组转换为条件
     *
     * @param dto 多对象树条件
     */
    public static void group2Condition(ApmMultiTreeDto dto) {
        if (dto == null) {
            return;
        }
        String groupProperty = dto.getGroupProperty();
        String groupPropertyValue = dto.getGroupPropertyValue();
        if (groupProperty == null || groupPropertyValue == null) {
            return;
        }
        dto.setGroupProperty(null);
        dto.setGroupPropertyValue(null);

        if (dto.getModelMixQo() == null) {
            dto.setModelMixQo(new ApmMultiTreeModelMixQo());
        }

        ModelMixQo modelMixQo = dto.getModelMixQo();
        modelMixQo.setGroupProperty(groupProperty);
        modelMixQo.setGroupPropertyValue(groupPropertyValue);
    }
}
