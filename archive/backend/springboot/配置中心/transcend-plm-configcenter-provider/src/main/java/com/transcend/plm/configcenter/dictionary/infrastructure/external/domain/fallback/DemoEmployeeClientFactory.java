package com.transcend.plm.configcenter.dictionary.infrastructure.external.domain.fallback;

/*
@Slf4j
@Component
public class DemoEmployeeClientFactory implements FallbackFactory<DemoEmployeeClient>{

	@Override
	public DemoEmployeeClient create(Throwable cause) {
		log.error("调用EmployeeClient发生错误",cause);
		// TODO Auto-generated method stub
		return new DemoEmployeeClient() {

			@Override
			public BaseResponse<List<EmployeeDto>> listEmployeeByNos(List<String> empNos) {
				// TODO Auto-generated method stub
				return BaseResponse.fail("listEmployeeByNos服务降级");
			}

			@Override
			public List<EmployeeDto> listEmployeeByKeywords(EmployeeParam par) {
				return null;
			}

			@Override
			public BaseResponse<EmployeeDto> getEmployeeByToken(String token) {
				return BaseResponse.fail("getEmployeeByToken服务降级");
			}

			@Override
			public BaseResponse<List<EmployeeDto>> listEmployeeByIds(List<String> userIds) {
				return BaseResponse.fail("listEmployeeByIds服务降级");
			}
		};
	}

}
*/
