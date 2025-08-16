package com.transcend.plm.datadriven.apm.supplier.service;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 战略供应商管理服务接口
 *
 * @author yanbing.ao
 * @version: 1.0
 * @date 2023/11/15 11:26
 */
public interface SupplierService {
    /**
     * listObjectsByRelations
     *
     * @param relationObject relationObject
     * @return {@link List<MObject>}
     */
    List<MObject> listObjectsByRelations(@RequestBody RelationMObject relationObject);
}
