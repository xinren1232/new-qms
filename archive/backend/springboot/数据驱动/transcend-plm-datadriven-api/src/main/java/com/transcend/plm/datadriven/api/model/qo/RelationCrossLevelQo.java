package com.transcend.plm.datadriven.api.model.qo;

import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * 关系跨层级查询对象
 * <p>
 * <p>
 * 场景：
 * <p>
 * <p>
 * 例：如
 * <p>
 * <p>
 * 项目->版本
 * <p>
 * <p>
 * 版本->迭代
 * <p>
 * <p>
 * 迭代->任务
 * <p>
 * <p>
 * <p>
 * 项目->迭代
 * <p>
 * <p>
 * 迭代->任务
 * <p>
 * <p>
 * 在“迭代”详情中，在TAB“任务”中，新建“任务”时，“任务”新增页面的所属“项目”需要带出“迭代”所从属的“项目”
 *
 * @author yss
 * @date 2022/06/01 20:16
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Api(value = "关系跨层级查询对象Qo",tags = "关系跨层级查询对象Qo")
public class RelationCrossLevelQo {

    /**
     * 源对象的modelCode
     * 如:项目的modelCode
     */
    String sourceModelCode;
    /**
     * 跨层级关系modelCode
     * 如：项目->版本 的modelCode
     * 如：版本->迭代 的modelCode
     */
    @ApiModelProperty(value = "跨层级关系modelCode以#分隔")
    private String crossLevelRelationModelCodes;

    /**
     * 跨层级关系的源对象实例id
     * 如：迭代->任务 的源对象实例id为迭代id
     */
    @ApiModelProperty(value = "跨层级关系modelCode")
    private String crossLevelSourceBid;

    /**
     * 分页查询
     */
    BaseRequest<ModelMixQo> pageQo;


    /**
     * @return {@link RelationCrossLevelQo }
     */
    public static RelationCrossLevelQo of() {
        return new RelationCrossLevelQo();
    }





}
