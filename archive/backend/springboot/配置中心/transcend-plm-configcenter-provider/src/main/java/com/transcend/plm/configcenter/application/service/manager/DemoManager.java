//package com.transcend.plm.configcenter.application.service.manager;
//
//import java.util.Arrays;
//import java.util.List;
//
//import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
//import com.transcend.plm.configcenter.api.model.object.vo.SampleVo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.transsion.framework.sdk.open.dto.OpenResponse;
//import com.transsion.framework.tool.locate.AssertBuilder;
//import com.transsion.framework.tool.locate.AssertUtil;
//import com.transsion.framework.tool.locate.II18nService;
//import com.transsion.framework.uac.model.dto.UserDTO;
//import com.transsion.framework.uac.service.IUacUserService;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Service
//public class DemoManager {
//    @Autowired
//    II18nService i18nService;
//
//    @Autowired
//    IUacUserService uacUserService;
//
//	/*
//	 * 获取多语言值
//	 */
//	public String getMessage(String key, String... args) {
//		return i18nService.getMessage(key, args);
//	}
//
//
//	public List<SampleVo> findByKey(String key) {
//		//参数验证
//		AssertBuilder.ctor(key).code("6001").notEmpty();
//
//		SampleVo s=new SampleVo();
//		s.setName("测试数据");
//
//		return Arrays.asList(s);
//	}
//
//
//	public String testParam(String key) {
//		/*
//    	if(ObjectUtils.isEmpty(key)) {
//    		throw new BusinessException(MessageConst.USER_NAME_NOT_NULL);
//    	}
//    	*/
//    	AssertBuilder.ctor(key, ErrorMsgEnum.USER_NAME_NOT_NULL)
//    			.addArg("param 0")
//    			.addArg("param 1")
//    			.notEmpty();
//
//    	return key;
//	}
//
//	public String testParam2(String key) {
//		AssertUtil.notEmpty(key, "[{tr.query.key.message}] - {tr.validation.notEmpty.message}");
//    	return key;
//	}
//
//
//	public UserDTO getUserByCode(String empNo) {
//		OpenResponse<UserDTO> res=uacUserService.queryDetail(empNo);
//		return res.getData();
//	}
//}
