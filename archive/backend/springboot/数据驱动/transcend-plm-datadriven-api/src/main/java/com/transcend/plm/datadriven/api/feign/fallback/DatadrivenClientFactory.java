package com.transcend.plm.datadriven.api.feign.fallback;

import com.transcend.plm.datadriven.api.feign.ObjectModelFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/11 16:10
 * @since 1.0
 */
@Slf4j
@Component
public class DatadrivenClientFactory implements FallbackFactory<ObjectModelFeignClient> {

    @Override
    public ObjectModelFeignClient create(Throwable cause) {
        log.error("调用EmployeeClient发生错误",cause);
        return null;
        // TODO Auto-generated method stub
//        return new DemoEmployeeClient() {
//
//            @Override
//            public BaseResponse<List<EmployeeDto>> listEmployeeByNos(List<String> empNos) {
//                // TODO Auto-generated method stub
//                return BaseResponse.fail("listEmployeeByNos服务降级");
//            }
//
//            @Override
//            public List<EmployeeDto> listEmployeeByKeywords(EmployeeParam par) {
//                return null;
//            }
//
//            @Override
//            public BaseResponse<EmployeeDto> getEmployeeByToken(String token) {
//                return BaseResponse.fail("getEmployeeByToken服务降级");
//            }
//
//            @Override
//            public BaseResponse<List<EmployeeDto>> listEmployeeByIds(List<String> userIds) {
//                return BaseResponse.fail("listEmployeeByIds服务降级");
//            }
//        };
    }

}