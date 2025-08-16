package com.transcend.plm.configcenter.objectview.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.transcend.plm.configcenter.objectview.infrastructure.repository.mapper.CfgViewConfigAttrMapper;
import com.transcend.plm.configcenter.objectview.infrastructure.repository.po.CfgViewConfigAttr;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 */
@Service
public class CfgViewConfigAttrServiceImpl extends ServiceImpl<CfgViewConfigAttrMapper, CfgViewConfigAttr>
    implements CfgViewConfigAttrService{

    @Resource
    private CfgViewConfigAttrMapper cfgViewConfigAttrMapper;


    @Override
    public int saveConfigAttrs(List<CfgViewConfigAttr> viewConfigAttrList) {
        return 0;//cfgViewConfigAttrMapper.bulkInsertConfigAttr(viewConfigAttrList);
    }
}




