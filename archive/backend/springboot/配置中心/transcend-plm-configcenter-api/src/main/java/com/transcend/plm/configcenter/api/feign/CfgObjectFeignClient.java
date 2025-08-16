package com.transcend.plm.configcenter.api.feign;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/11 16:07
 * @since 1.0
 */

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.fallback.DemoClientFactory;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectRelationVO;
import com.transsion.framework.annotation.PermissionLimit;
import io.swagger.annotations.ApiOperation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author jie.luo1
 * @Description: 配置对象feign
 * @date 2023年5月9日
 * 本地调试加：, url = "http://127.0.0.1:8081/"
 */
@FeignClient(name = "${transcend.configcenter.feign.name:transcend-plm-configcenter}",
        url = "${transcend.configcenter.feign.url:}",
        fallbackFactory = DemoClientFactory.class)
public interface CfgObjectFeignClient {
    /**
     * 获取对象包括属性信息
     *
     * @param modelCode 对象类型
     * @return
     */
    @PostMapping("/api/cfg/object/getByModelCode/{modelCode}")
    @Cacheable(value = "OBJECT_MODEL", key = "#modelCode")
    TranscendApiResponse<CfgObjectVo> getByModelCode(@PathVariable String modelCode);

    @ApiOperation("根据modelCode查询生命周期")
    @PostMapping("/api/cfg/object/lifeCycle/get/{modelCode}")
    @Cacheable(value = "OBJECT_LIFE_CYCLE", key = "#modelCode")
    TranscendApiResponse<ObjectModelLifeCycleVO> findObjectLifecycleByModelCode(@PathVariable(name = "modelCode") String modelCode);

    /**
     * 获取对象包括属性信息
     *
     * @param baseModel 基类
     * @return
     */
    @PostMapping("/api/cfg/object/getByBaseModel/{baseModel}")
    @PermissionLimit(limit = false)
    TranscendApiResponse<CfgObjectVo> getByBaseModel(@PathVariable String baseModel);


    @PostMapping("/api/cfg/object/listAllByModelCode/{modelCode}")
    @PermissionLimit(limit = false)
    TranscendApiResponse<List<CfgObjectVo>> listAllByModelCode(@PathVariable String modelCode);

    @PostMapping("/api/cfg/object/getObjectRelationVOsBySourceModelCode/{sourceModelCode}")
    @PermissionLimit(limit = false)
    TranscendApiResponse<List<ObjectRelationVO>> getObjectRelationVOsBySourceModelCode(@PathVariable String sourceModelCode);


    /**
     * 根据modelCodeList查询特定的对象List（只有基本信息）
     *
     * @param modelCodes modelCodes
     * @return TranscendApiResponse<List < CfgObjectVo>>
     * @version: 1.0
     * @date: 2023/10/9 10:29
     * @author: bin.yin
     */
    @ApiOperation(value = "根据modelCodeList查询特定的对象List（只有基本信息）")
    @PostMapping("/api/manager/cfg/object/listByModelCodes")
    TranscendApiResponse<List<CfgObjectVo>> listByModelCodes(@RequestBody List<String> modelCodes);

    @ApiOperation(value = "根据modelCode查询其本身及其所有子对象列表（只有基本信息）")
    @GetMapping("/api/manager/cfg/object/{modelCode}/listChildrenByModelCode")
    TranscendApiResponse<List<CfgObjectVo>> listChildrenByModelCode(@PathVariable String modelCode);

    /**
     * 查询所有对象列表
     *
     * @param: @param  * @param
     * @return: TranscendApiResponse<List < CfgObjectVo>>
     * @version: 1.0
     * @date: 2024/8/5/005
     * @author: yanbing.ao
     */
    @ApiOperation(value = "查询所有对象列表")
    @GetMapping("/api/manager/cfg/object/list")
    TranscendApiResponse<List<CfgObjectVo>> list();


    /**
     * 获取对象属性列表
     *
     * @param modelCode 模型编码
     * @return 属性列表
     */
    @GetMapping("/api/cfg/object/listObjectAttribute/{modelCode}")
    @PermissionLimit(limit = false)
    TranscendApiResponse<List<CfgObjectAttributeVo>> listObjectAttribute(@PathVariable String modelCode);

}
