package com.transcend.plm.configcenter.table.infrastructure.external.domain.feign;

/**  
 * @Description: TODO
 * @author zhihui.yu
 * @date 2021年6月9日

@FeignClient(name ="service-cp-uc-extend",path="/cp-uc-extend/Employee/",fallbackFactory=DemoEmployeeClientFactory.class)
public interface DemoEmployeeClient {
	@PostMapping("queryEmployeeListByIds")
	BaseResponse<List<EmployeeDto>> listEmployeeByNos(List<String> empNos);
	
	@PostMapping("queryUserInfo")
	List<EmployeeDto> listEmployeeByKeywords(@RequestBody EmployeeParam par);
	
	@GetMapping("queryUserByToken")
	BaseResponse<EmployeeDto> getEmployeeByToken(@RequestParam("token")String token);
	
	@PostMapping("queryEmployeeInfoByUid")
	BaseResponse<List<EmployeeDto>> listEmployeeByIds(List<String> userIds);
}
 */