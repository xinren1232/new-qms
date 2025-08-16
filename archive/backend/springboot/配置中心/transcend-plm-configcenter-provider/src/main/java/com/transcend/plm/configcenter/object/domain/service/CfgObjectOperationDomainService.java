package com.transcend.plm.configcenter.object.domain.service;

import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectOperationRepository;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectOperationVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 对象操作领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/3/14 9:41
 * @since 1.0
 */
@Service
public class CfgObjectOperationDomainService {

    @Resource
    private CfgObjectOperationRepository cfgObjectOperationRepository;

    public List<CfgObjectOperationVo> listByBaseModel(String baseModel){
        return cfgObjectOperationRepository.listByBaseModel(baseModel);
    }

}
