//package com.transcend.plm.configcenter.application.service.manager;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import com.transcend.plm.configcenter.pojo.dto.NotifyEventBusDto;
//import com.transcend.plm.configcenter.infrastructure.repository.po.Sample;
//import com.transcend.plm.configcenter.config.eventbus.NotifyEventBus;
//import com.transcend.plm.configcenter.application.service.ISampleService;
//import com.transcend.plm.configcenter.wrapper.SampleWrapper;
//import org.springframework.stereotype.Service;
//
//import com.baomidou.dynamic.datasource.annotation.Slave;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.baomidou.mybatisplus.extension.AbstractManagerImpl;
//import com.baomidou.mybatisplus.extension.WrapperBuilder;
//import com.transsion.framework.dto.BaseRequest;
//import com.transsion.framework.tool.locate.AssertBuilder;
//import com.transcend.plm.configcenter.pojo.vo.SampleVo;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Service
//public class SampleManager extends AbstractManagerImpl<ISampleService, Sample,SampleVo>{
//	/*
//	 * 分页查询
//	 */
//	public IPage<SampleVo> page(BaseRequest<SampleVo> request) {
//		IPage<Sample> page = WrapperBuilder.getPage(request);
//		Sample param=this.voEntity(request.getParam());
//		QueryWrapper<Sample> qw=WrapperBuilder.getQueryWrapper(param);
//		page=this.baseService.page(page,qw);
//		return this.pageVO(page);
//	}
//
//	/**
//	 * 查询列表
//	 * @param request
//	 * @return
//	 */
//	public List<SampleVo> list(SampleVo request) {
//		SampleWrapper sampleWrapper=SampleWrapper.builder();
//		Sample param=sampleWrapper.voEntity(request);
//		QueryWrapper<Sample> qw=WrapperBuilder.getQueryWrapper(param);
//
//		List<Sample> list=this.baseService.list(qw);
//		return this.listVO(list);
//	}
//
//	@Slave
//	public List<SampleVo> findByKey(String key) {
//		//参数验证
//		AssertBuilder.ctor(key)
//		.code("500")
//		.notEmpty();
//
//		//List<Sample> list= this.baseService.findByKey(key);
//		List<Sample> list= this.baseService.findListByKey(key);
//		return this.listVO(list);
//	}
//
//	public List<SampleVo> findByStatus(Integer status) {
//		List<Sample> list= this.baseService.findByStatus(status);
//		return this.listVO(list);
//	}
//
//	public Sample saveEntity(SampleVo vo) {
//		SampleWrapper sampleWrapper=SampleWrapper.builder(this.baseService);
//		Sample entity=sampleWrapper.voEntity(vo);
//		this.baseService.saveEntity(entity);
//    	return entity;
//	}
//
//	public Boolean updateEntity(Sample entity) {
//		return this.baseService.updateEntityById(entity);
//	}
//
//
//	public LocalDateTime asyncExecutor(String key) {
//		this.baseService.asyncExecutor(key);
//
//		log.info("异步执行后的操作---{}",key);
//		return LocalDateTime.now();
//	}
//
//	public LocalDateTime eventBusExecutor(String key) {
//		NotifyEventBus.publishEvent(NotifyEventBusDto.builder()
//				.key(key)
//				.build());
//
//		log.info("异步执行后的操作---{}",key);
//		return LocalDateTime.now();
//	}
//
//	public Boolean update2(String id, String name) {
//		UpdateWrapper<Sample> updateWrapper=new UpdateWrapper<>();
//		updateWrapper.eq("id", id);
//		updateWrapper.set("name", name);
//		updateWrapper.set("status", 1);
//		return this.baseService.update(updateWrapper);
//	}
//
//	public Sample getById2(String id) {
//		return this.baseService.getById2(id);
//	}
//
//	public Boolean removeById2(String id) {
//		return this.baseService.removeById2(id);
//	}
//
//	public Sample updateById2(Sample sample) {
//		return this.baseService.updateById2(sample);
//	}
//}
