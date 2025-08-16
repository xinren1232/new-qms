package com.transcend.plm.configcenter.api.model.view.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * @Describe 视图查询参数
 * @Author yuhao.qiu
 * @Date 2024/9/3
 */
@Data(staticConstructor = "of")
@Accessors(chain = true)
public class ViewQueryParam {

    /**
     * 视图所属bid
     */
    private String viewBelongBid;

    /**
     * 视图所属bid列表
     */
    private List<String> viewBelongBids;

    /**
     * 视图类型
     */
    private String viewType;

    /**
     * 视图作用域
     */
    private String viewScope;

    /**
     * 角色编码列表
     */
    private List<String> roleCodes;

    /**
     * 标签列表
     */
    private String tag;

    /**
     * 实例数据
     */
    private Map<String, Object> instance;
}
