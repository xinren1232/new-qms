package com.transcend.plm.datadriven.apm.supplier.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.apm.supplier.service.SupplierService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 战略供应商管理-控制器
 *
 * @author yanbing.ao
 * @version: 1.0
 * @date 2023/11/15 10:57
 */
@RestController
public class SupplierController {

    @Resource
    private SupplierService supplierService;
    @PostMapping("/apm/app/supplier/listMObjectsByRelations")
    @ApiOperation("查询对象关系实例数据被关联的对象实例数据")
    TranscendApiResponse<List<MObject>> listObjectsByRelations(@RequestBody RelationMObject relationObject){
        return TranscendApiResponse.success(supplierService.listObjectsByRelations(relationObject));
    }
}
