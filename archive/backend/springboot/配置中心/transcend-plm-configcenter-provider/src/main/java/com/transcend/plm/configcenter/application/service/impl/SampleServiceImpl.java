//package com.transcend.plm.configcenter.application.service.impl;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Objects;
//
//import com.transcend.plm.configcenter.application.service.ISampleService;
//import com.transcend.plm.configcenter.infrastructure.repository.mapper.SampleMapper;
//import com.transcend.plm.configcenter.infrastructure.repository.po.Sample;
//import org.springframework.cache.annotation.CacheConfig;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.CachePut;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.cache.annotation.Caching;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.transsion.framework.cache.annotation.CacheExtends;
//import com.transsion.framework.cache.support.CacheMode;
//import com.transsion.framework.exception.BusinessException;
//import com.transsion.framework.log.annotation.TracerLog;
//import com.transsion.framework.tool.locate.AssertBuilder;
//import com.transcend.plm.configcenter.common.constant.CacheConstant;
//import com.transcend.plm.configcenter.pojo.dto.NotifyEventBusDto;
//
//import lombok.extern.slf4j.Slf4j;
//
///**
// * Cacheable,CacheConfig,CacheEvict说明
// * https://blog.csdn.net/weixin_30000283/article/details/112143253
// * Sample表数据服务层接口实现类
// * ,keyGenerator = "customizedKeyGenerator"
// */
//@CacheConfig(cacheNames=CacheConstant.CACHENAME_SAMPLE)
//@Slf4j
//@Service
//public class SampleServiceImpl extends ServiceImpl<SampleMapper, Sample> implements ISampleService {
//
//	/*
//	@Cacheable(key="'sample_key_' + #key")
//	@Override
//    public List<Sample> findByKey(String key){
//		Sample sample=new Sample();
//		sample.setName(key);
//		QueryWrapper<Sample> qw=WrapperBuilder.getQueryWrapper(sample);
//        return this.baseMapper.selectList(qw);
//    }
//	*/
//
//	@CacheExtends(mode=CacheMode.BOTH)
//	@Cacheable(key="'sample_key_' + #key")
//	@Override
//    public List<Sample> findListByKey(String key){
//		log.info("从数据库查询...");
//        return this.baseMapper.findList(key);
//    }
//
//	/*
//	 * 标记在方法上，方法执行完毕之后根据条件或key删除对应的缓存
//	 */
//	//@CacheEvict(key="'sample_status_'+#entity.status"
//	//@CacheEvict(allEntries=true)
//	@CacheExtends(mode=CacheMode.BOTH)
//	@Caching(evict= {
//			@CacheEvict(key="'sample_key_*'"),
//			@CacheEvict(key="'sample_status_'+#entity.status")
//	})
//	@Override
//	public Boolean saveEntity(Sample entity) {
//		entity.setTenantId(100L);
//		return this.baseMapper.insert(entity)>0;
//	}
//	/*
//	 * 标记在方法或者类上，标识该方法或类支持缓存
//	 */
//	@Cacheable(key="'sample_status_' + #status")
//	@Override
//	public List<Sample> findByStatus(Integer status) {
//		LambdaQueryWrapper<Sample> lqw=new LambdaQueryWrapper<>();
//		lqw.eq(Sample::getStatus, status);
//		return this.baseMapper.selectList(lqw);
//	}
//	/*
//	 * 根据ID更新实体
//	 	*/
//	@Caching(evict= {
//		@CacheEvict(key="'sample_key_*'"),
//		@CacheEvict(key="'sample_status_'+#entity.status")
//	})
//	@Override
//	public Boolean updateEntityById(Sample entity) {
//		return this.baseMapper.updateById(entity)>0;
//	}
//
//
//
//	/*
//	 * 根据ID查询
//	 * 	*/
//	@Override
//	@CacheExtends(mode=CacheMode.BOTH)
//	@Cacheable(key = "'sample_id_' + #id")
//	public Sample getById2(String id) {
//		return this.baseMapper.selectById(id);
//	}
//
//	/*
//	 * 根据ID更新实体
//	 * */
//	@Override
//	@CacheExtends(mode=CacheMode.BOTH)
//	@CachePut(key = "'sample_id_' + #sample.id")
//	public Sample updateById2(Sample sample) {
//		int count=this.baseMapper.updateById(sample);
//		//int count=0;
//		AssertBuilder.ctor(count>0)
//			.message("根据ID更新实体失败 id={0}")
//			.args(sample.getId())
//			.isTrue();
//		return sample;
//	}
//
//	/*
//	 * 根据ID删除实体
//	 * */
//	@Override
//	@CacheExtends(mode=CacheMode.BOTH)
//	@CacheEvict(key = "'sample_id_' + #id")
//	public Boolean removeById2(String id) {
//		return this.baseMapper.deleteById(id)>0;
//	}
//
//
//    @TracerLog(action="10001",
//    		title="测试流程",
//    		requestId="#instanceId")
//	@Override
//	public void testLog(String instanceId) {
//		log.info("执行server");
//	}
//
//    @Async
//	@Override
//	public LocalDateTime asyncExecutor(String key) {
//		log.info("异步执行开始---{}",key);
//
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//		}
//		log.info("异步执行结束---{}",key);
//
//		if(Objects.equals(key, "throw")) {
//			throw BusinessException.builder("异步过程抛出异常");
//		}
//
//		return LocalDateTime.now();
//	}
//
//	@Override
//	public void eventBusTest(NotifyEventBusDto notifyEventBusDto) throws BusinessException {
//		String key=notifyEventBusDto.getKey();
//		log.info("eventBus异步执行开始---{}",key);
//
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//		}
//		log.info("eventBus异步执行结束---{}",key);
//
//		if(Objects.equals(key, "throw")) {
//			throw BusinessException.builder("eventBus异步过程抛出异常");
//		}
//	}
//}