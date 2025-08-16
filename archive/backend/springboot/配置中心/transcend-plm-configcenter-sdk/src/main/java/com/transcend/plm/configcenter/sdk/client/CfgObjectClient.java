//package com.transcend.plm.configcenter.sdk.client;
//
///**
// * TODO 描述
// *
// * @author jie.luo <jie.luo1@transsion.com>
// * @version V1.0.0
// * @date 2023/5/11 16:07
// * @since 1.0
// */
//
//import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
//import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//
//import javax.annotation.Resource;
//
///**
// * @author jie.luo1
// * @Description: 配置对象feign
// * @date 2023年5月9日
// */
//public class CfgObjectClient{
//    private CfgObjectFeignClient cfgObjectFeignClient;
//
//    public static CfgObjectClient getInstance() {
//
//        return new CfgObjectClient();
//    }
//
//    public static void   set() {
//
//        return new CfgObjectClient();
//    }
//
//    /**
//     * 获取对象包括属性信息
//     * @param modelCode 对象类型
//     * @return
//     */
//    CfgObjectVo getByModelCode(@PathVariable String modelCode){
//        P
//    }
//
//    /**
//     * 获取对象包括属性信息
//     * @param baseModel 基类
//     * @return
//     */
//    @PostMapping("/getByBaseModel/{baseModel}")
//    @PermissionLimit(limit = false)
//    CfgObjectVo getByBaseModel(@PathVariable String baseModel);
//
//
//
//
//
//
//}
