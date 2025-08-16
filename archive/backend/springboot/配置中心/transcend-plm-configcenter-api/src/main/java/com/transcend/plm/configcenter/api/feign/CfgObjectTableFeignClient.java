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
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectRelationVO;
import com.transsion.framework.annotation.PermissionLimit;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author jie.luo1
 * @Description: 配置对象feign
 * @date 2023年5月9日
 */
@FeignClient(name="${transcend.configcenter.feign.name:transcend-plm-configcenter}",
        url = "${transcend.configcenter.feign.url:}",
        fallbackFactory = DemoClientFactory.class)
public interface CfgObjectTableFeignClient {
    /**
     * 获取对象包括属性信息
     * @param modelCode 对象类型
     * @return
     */
    @PostMapping("/api/cfg/object-table/getByModelCode/{modelCode}")
    TranscendApiResponse<CfgObjectVo> getByModelCode(@PathVariable String modelCode);


}
