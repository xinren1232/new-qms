package com.transcend.plm.configcenter.language.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.language.infrastructure.repository.mapper.CfgVersionMapper;
import com.transcend.plm.configcenter.language.infrastructure.repository.po.CfgVersionPo;
import org.springframework.stereotype.Service;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Service
public class CfgVersionServiceImpl extends ServiceImpl<CfgVersionMapper, CfgVersionPo>
implements CfgVersionService {

    /**
     * 版本增加
     *
     * @param
     * @return
     */
    @Override
    public Long increment(String name) {
        return this.getBaseMapper().increment(name);
    }

    /**
     * 获取当前版本
     *
     * @param name
     * @return
     */
    @Override
    public Long getVersionByName(String name) {
        return this.getBaseMapper().getVersion(name);
    }
}
