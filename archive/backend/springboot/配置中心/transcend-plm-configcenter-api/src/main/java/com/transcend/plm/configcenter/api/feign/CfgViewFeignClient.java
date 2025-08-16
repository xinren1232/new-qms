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
import com.transcend.plm.configcenter.api.model.object.dto.CfgViewRuleMatchDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transsion.framework.annotation.PermissionLimit;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jie.luo1
 * @Description: 配置对象feign
 * @date 2023年5月9日
 */
@FeignClient(name = "${transcend.configcenter.feign.name:transcend-plm-configcenter}",
        url = "${transcend.configcenter.feign.url:}",
        fallbackFactory = DemoClientFactory.class)
public interface CfgViewFeignClient {
    /**
     * 获取最优匹配视图
     *
     * @param ruleMatchDto 视图规则匹配入参
     * @return
     */
    @PostMapping("/api/cfg/object/getOptimalMatchView")
    @PermissionLimit(limit = false)
    TranscendApiResponse<CfgViewVo> getOptimalMatchView(@RequestBody CfgViewRuleMatchDto ruleMatchDto);

    /**
     * 获取操作日志默认视图
     *
     * @param ruleMatchDto ruleMatchDto 视图规则匹配入参
     *                     modelCode 和 tag 必传
     * @return TranscendApiResponse<CfgViewVo>
     * @version: 1.0
     * @date: 2023/10/7 16:53
     * @author: bin.yin
     */
    @PostMapping("/api/cfg/object/getLogView")
    @PermissionLimit(limit = false)
    TranscendApiResponse<CfgViewVo> getLogView(@RequestBody CfgViewRuleMatchDto ruleMatchDto);

    /**
     * 新增属性基本信息
     *
     * @param bid        业务id
     * @param cfgViewDto 查询参数
     * @return 返回结果
     */
    @ApiOperation("新增属性基本信息")
    @PostMapping("/saveOrUpdate/{bid}")
    @Deprecated
    TranscendApiResponse<CfgViewVo> saveOrUpdate(@PathVariable("bid") String bid,
                                                 @RequestBody CfgViewDto cfgViewDto);

    /**
     * 新增或更新视图
     *
     * @param cfgViewDto 参数
     * @return 返回结果
     */
    @ApiOperation("新增或更新视图")
    @PostMapping("/api/saveOrUpdateView")
    TranscendApiResponse<CfgViewVo> saveOrUpdateView(@RequestBody CfgViewDto cfgViewDto);


    /**
     * 新增属性基本信息
     *
     * @param cfgViewDto 参数
     * @return 返回结果
     */
    @ApiOperation("新增属性基本信息")
    @PostMapping("/api/copyViews")
    TranscendApiResponse<Boolean> copyViews(@RequestBody CfgViewDto cfgViewDto);

    /**
     * 通过参数获取视图
     *
     * @param param 参数
     * @return 返回结果
     */
    @ApiOperation("根据参数获取视图")
    @PostMapping("/api/getView")
    TranscendApiResponse<CfgViewVo> getView(@RequestBody ViewQueryParam param);

    /**
     * 批量获取视图（暂时支持默认视图，后续再支持条件过滤多视图）
     *
     * @param param 参数
     * @return 返回结果
     */
    @ApiOperation("批量获取视图（暂时支持默认视图，后续再支持条件过滤多视图）")
    @PostMapping("/api/getViews")
    TranscendApiResponse<Map<String, CfgViewVo>> getViews(@RequestBody ViewQueryParam param);

    /**
     * 根据所属bid查询所有视图(不过滤条件)
     *
     * @param belongBid 所属bid
     * @return 返回结果
     */
    @ApiOperation("根据所属bid查询所有视图(不过滤条件)")
    @PostMapping("/api/listView/{belongBid}")
    TranscendApiResponse<List<CfgViewVo>> listView(@PathVariable("belongBid") String belongBid);

    /**
     * 根据所属bid列表查询所有视图
     * 注意：此接口不包含视图配置信息
     *
     * @param belongBids 所属bid列表
     * @return 返回结果
     */
    @ApiOperation("根据所属bid列表查询所有视图")
    @PostMapping("/api/listView")
    TranscendApiResponse<List<CfgViewVo>> listView(@RequestBody List<String> belongBids);

    /**
     * 通过bid查看详情
     *
     * @param bid 业务id
     * @return 返回结果
     */
    @GetMapping("/api/get/{bid}")
    @ApiOperation("查看详情")
    TranscendApiResponse<CfgViewVo> getByBid(@PathVariable("bid") String bid);

    /**
     * 通过bid列表批量获取详情
     *
     * @param bids
     * @return
     */
    @PostMapping("/api/getByBids")
    @ApiOperation("查看详情")
    TranscendApiResponse<Map<String, CfgViewVo>> getByBids(@RequestBody Set<String> bids);

    /**
     * 通过bid获取视图详情
     *
     * @param bid 业务id
     * @return 返回结果
     */
    @GetMapping("/api/get/{bid}/listMetaModels")
    @ApiOperation("查看详情")
    @Deprecated
    TranscendApiResponse<List<CfgViewMetaDto>> listMetaModels(@PathVariable("bid") String bid);

    /**
     * 通过参数获取视图元数据详情
     *
     * @param param 参数
     * @return 返回结果
     */
    @PostMapping("/api/listMetaModels")
    @ApiOperation("获取视图元数据详情")
    TranscendApiResponse<List<CfgViewMetaDto>> listMetaModels(@RequestBody ViewQueryParam param);

    /**
     * 获取归属Bid视图列表 并筛选对应视图或者默认视图
     *
     * @param param 参数
     * @return 返回结果
     */
    @PostMapping("/api/listViewTypeOrDefault")
    @ApiOperation("获取视图元数据详情")
    TranscendApiResponse<List<CfgViewVo>> listTypeOrDefaultView(@RequestBody ViewQueryParam param);


}
