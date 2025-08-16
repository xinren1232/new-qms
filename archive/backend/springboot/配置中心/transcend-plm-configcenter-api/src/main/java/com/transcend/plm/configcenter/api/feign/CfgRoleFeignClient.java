package com.transcend.plm.configcenter.api.feign;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.fallback.DemoClientFactory;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleUserVo;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

/**
 * @author yinbin
 * @version:
 * @date 2023/10/09 09:24
 */
@FeignClient(name = "${transcend.configcenter.feign.name:transcend-plm-configcenter}",
        url = "${transcend.configcenter.feign.url:}",
        fallbackFactory = DemoClientFactory.class)
public interface CfgRoleFeignClient {

    /**
     * 根据code列表查查询角色信息列表
     * @param codeList codeList
     * @return TranscendApiResponse<List<CfgRoleVo>>
     * @version: 1.0
     * @date: 2023/10/9 9:25
     * @author: bin.yin
     */
    @ApiOperation("根据code列表查查询角色信息列表")
    @PostMapping("/api/manage/cfg/role/queryByCodes")
    TranscendApiResponse<List<CfgRoleVo>> queryByCodes(@RequestBody List<String> codeList);

    /**
     * 根据用户id查询角色信息
     * @param jobNumber
     * @return
     */
    @GetMapping("/api/cfg/getRolesByUserId/{userId}")
    TranscendApiResponse<List<String>> getRolesByUserId(@PathVariable("userId") String jobNumber);

    /**
     * 根据用户id查询角色code
     * @param jobNumber
     * @return
     */
    @GetMapping("/api/cfg/getSysGlobalRolesByUserId/{userId}")
    TranscendApiResponse<List<String>> getSysGlobalRolesByUserId(@PathVariable("userId") String jobNumber);

    /**
     * 根据用户id查询角色code
     * @param roleCode
     * @return
     */
    @GetMapping("/api/cfg/listUserByRoleCodes/{roleCode}")
    TranscendApiResponse<Set<String>> listUserSetByRoleCode(@PathVariable("roleCode") String roleCode);

    /**
     * 根据角色code查询用户
     * @return
     */
    @ApiOperation("查角色树")
    @GetMapping("/api/cfg/roleTree")
    TranscendApiResponse<List<CfgRoleVo>> tree();

    /**
     * 根据角色code查询用户
     * @return
     */
    @ApiOperation("查角色列表")
    @GetMapping("/api/cfg/listGlobalRole")
    TranscendApiResponse<List<CfgRoleVo>> listGlobalRole();

    /**
     * 根据角色code查询用户
     * @param roleCode
     * @return
     */
    @ApiOperation("根据角色编码查询所属用户")
    @GetMapping("/api/cfg/listUsersByRoleCode/{roleCode}")
    TranscendApiResponse<List<CfgRoleUserVo>> listUsersByRoleCode(@PathVariable("roleCode") String roleCode);
}
