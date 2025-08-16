package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmAppTabHeaderVO;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppTabHeaderDto;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppTabHeader;

import java.util.Collection;
import java.util.List;

/**
 * @author unknown
 */
public interface ApmAppTabHeaderService extends IService<ApmAppTabHeader> {
    /**
     * 保存或更新 ApmAppTabHeaderDto 对象。
     *
     * @param apmAppTabHeaderDto 要保存或更新的 ApmAppTabHeaderDto 对象。
     * @return 如果保存或更新成功，则返回true；否则返回false。
     */
    boolean saveOrUpdate(ApmAppTabHeaderDto apmAppTabHeaderDto);
    /**
     * 获取指定业务bid的ApmAppTabHeader对象。
     *
     * @param bizBid 业务bid
     * @return 指定业务bid的ApmAppTabHeader对象
     */
    ApmAppTabHeader getApmAppTabHeader(String bizBid);

    /**
     * 从数据库中获取指定业务bid的ApmAppTabHeader对象列表。
     *
     * @param bizBids 业务bid的集合
     * @return 指定业务bid的ApmAppTabHeader对象列表
     */
    List<ApmAppTabHeader> getApmAppTabHeaders(Collection<String> bizBids);

    /**
     * 从数据库中获取指定业务bid和code的ApmAppTabHeaderVO对象。
     *
     * @param bizBid 业务bid
     * @param code   视图模式code
     * @return 指定业务bid和code的ApmAppTabHeaderVO对象
     */
    ApmAppTabHeaderVO getApmAppTabHeaderVO(String bizBid, String code);
}
