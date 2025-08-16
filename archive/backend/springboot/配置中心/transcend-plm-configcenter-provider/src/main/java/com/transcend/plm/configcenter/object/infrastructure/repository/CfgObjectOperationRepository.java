package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.CfgObjectOperationMapper;
import com.transcend.plm.configcenter.object.pojo.CfgObjectOperationConverter;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectOperationVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectPermissionVo;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * CfgObjectPermissionRepository
 *
 * @author jie.luo1
 * @version: 1.0
 * @date 2023/02/18 10:45
 */
@Repository
public class CfgObjectOperationRepository {

    @Resource
    private CfgObjectOperationMapper cfgObjectOperationMapper;

    public List<CfgObjectOperationVo> listByBaseModel(String baseModel){
        return CfgObjectOperationConverter.INSTANCE.pos2vos(cfgObjectOperationMapper.listByBaseModelAndDefalut(baseModel));
    }

}
