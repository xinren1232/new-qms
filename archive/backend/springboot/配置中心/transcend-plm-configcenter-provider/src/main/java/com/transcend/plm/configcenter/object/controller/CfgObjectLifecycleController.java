package com.transcend.plm.configcenter.object.controller;

import com.transcend.plm.configcenter.object.application.service.ICfgObjectLifecycleAppService;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectLifeCycleEditParam;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectLifeCycleSaveParam;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * ObjectLifecycleController
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 10:26
 */

@RestController
@Api(value = "Object lifeCycle Controller", tags = "对象管理-生命周期-控制器")
@RequestMapping(value = "/manager/cfg/object/life-cycle")
public class CfgObjectLifecycleController {

    @Resource
    private ICfgObjectLifecycleAppService lifecycleAppService;

    @ApiOperation("根据modelCode查询生命周期")
    @GetMapping("listByModelCode/{modelCode}")
    public ObjectModelLifeCycleVO findObjectLifecycleByModelCode(@PathVariable(name = "modelCode") String modelCode) {
        return lifecycleAppService.findObjectLifecycleByModelCode(modelCode);
    }

    @ApiOperation("保存生命周期对象")
    @PostMapping("saveObjectLifeCycle")
    public Boolean saveObjectLifeCycle(@RequestBody ObjectLifeCycleSaveParam param) {
        return lifecycleAppService.saveObjectLifeCycle(param);
    }

    @ApiOperation("编辑生命周期对象")
    @PostMapping("updateObjectLifeCycle")
    public Boolean updateObjectLifeCycle(@RequestBody ObjectLifeCycleEditParam param) {
        return lifecycleAppService.updateObjectLifeCycle(param);
    }

    /**
     * 上面为基础接口
     * ==================优--雅--的--分--割--线==================
     * 下面为扩展接口
     */

    @ApiOperation("批量获取生命周期对象")
    @PostMapping("listLifecycleByModelCodes")
    public Map<String, ObjectModelLifeCycleVO> listLifecycleByModelCodes(@RequestBody List<String> modelCodeList) {
        return lifecycleAppService.listLifecycleByModelCodes(modelCodeList);
    }

    /**
     * 上面入参为modelCode的接口
     * ==================优--雅--的--分--割--线==================
     * 下面入参为bid的接口
     */

    @ApiOperation("根据bid查询生命周期")
    @GetMapping("listObjectLifecycleByObjectBid/{objectBid}")
    public ObjectModelLifeCycleVO listObjectLifecycleByObjectBid(@PathVariable(name = "objectBid") String objectBid) {
        return lifecycleAppService.findObjectLifecycleByObjectBid(objectBid);
    }

    @ApiOperation("根据bid查询子生命周期")
    @GetMapping("listChildrenLifecycleByObjectBid/{objectBid}")
    public List<ObjectModelLifeCycleVO> listChildrenLifecycleByObjectBid(@PathVariable(name = "objectBid") String objectBid) {
        return lifecycleAppService.findChildrenLifecycleByObjectBid(objectBid);
    }

    @ApiOperation("根据bid集合批量获取生命周期对象")
    @PostMapping("listLifecycleListByObjectBidList")
    public Map<String, ObjectModelLifeCycleVO> listLifecycleListByObjectBids(@RequestBody List<String> objectBidList) {
        return lifecycleAppService.findLifecycleListByObjectBidList(objectBidList);
    }

}
