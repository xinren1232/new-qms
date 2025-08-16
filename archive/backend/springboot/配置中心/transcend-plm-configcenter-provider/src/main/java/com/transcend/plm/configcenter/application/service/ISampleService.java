//package com.transcend.plm.configcenter.application.service;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.transcend.plm.configcenter.infrastructure.repository.po.Sample;
//import com.transsion.framework.exception.BusinessException;
//import com.transcend.plm.configcenter.pojo.dto.NotifyEventBusDto;
//
///**
// *
// * Device 表数据服务层接口
// *
// */
//public interface ISampleService extends IService<Sample> {
//
//	//List<Sample> findByKey(String key);
//
//	List<Sample> findListByKey(String key);
//
//	List<Sample> findByStatus(Integer status);
//
//	Boolean saveEntity(Sample entity);
//
//	Boolean updateEntityById(Sample sample);
//
//	void testLog(String instanceId);
//
//	LocalDateTime asyncExecutor(String key);
//
//	void eventBusTest(NotifyEventBusDto notifyEventBusDto) throws BusinessException;
//
//
//	Sample getById2(String id);
//
//	Sample updateById2(Sample sample);
//
//	Boolean removeById2(String id);
//
//}