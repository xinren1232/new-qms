package com.transcend.plm.alm.pi.service.impl;

import com.alibaba.fastjson.JSON;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.pi.dto.MemberDto;
import com.transcend.plm.alm.pi.dto.ProductDto;
import com.transcend.plm.alm.pi.dto.SyncDto;
import com.transcend.plm.alm.pi.service.SyncProductService;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyncProductServiceImpl implements SyncProductService {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;


    @Value("#{'${transcend.plm.safe.productCode:P01,P02,PO3,P04,P05,P06,P07,P08,P09,P10,P11,P12,P13,P14,P15,P16}'.split(',')}")
    private List<String> productCodes;

    /*@Value("#{'${transcend.plm.safe.productAppBid:1211971953994846220,1197899366295482380,1197848313004896268}'.split(',')}")
    private List<String> productAppBid;*/

    @Value("#{'${transcend.plm.safe.productBid:1211971953936125952,1211972323100356608,1197899366236762112,1197848312950370304,1211972465895436288,1211972613695950848,1211972729609736192,1197832176082956288,1211962812647690240,1211972858754920448,1211962812647690240,1229483513104695296,1211962812647690240,1211962812647690240,1211962812647690240,1211962812647690240}'.split(',')}")
    private List<String> productSpaceBids;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Override
    public boolean saveOrUpdate(SyncDto syncDto){
        if(!"product".equals(syncDto.getType())){
            throw new BusinessException("不支持非产品类型同步");
        }
        ProductDto productDto = JSON.parseObject(syncDto.getBody(), ProductDto.class);
        return syncProduct(productDto);
    }

    @Override
    public boolean syncProduct(ProductDto productDto) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq("cc_id", productDto.getId())
                .and()
                .eq("delete_flag", 0);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> queryList = objectModelCrudI.list(TranscendModel.DOMAIN.getCode(), queryWrappers);
        if(CollectionUtils.isNotEmpty(queryList)){
            //更新
            MObject mObjectOld = queryList.get(0);
            MObject mObject = new MObject();
            mObject.setName(productDto.getProductName());
            mObject.put("enName", productDto.getProductEnName());
            mObject.put("enAbbreviationName",productDto.getProductShortName());
            mObject.put("description", productDto.getDescription());
            mObject.put("productField",productDto.getFieldCode());
            mObject.setUpdatedBy(productDto.getUpdatedBy());
            List<MemberDto> members = productDto.getMembers();
            if(CollectionUtils.isNotEmpty(members)){
                for(MemberDto memberDto:members) {
                    if("1".equals(memberDto.getType())){
                        //产品负责人
                        mObject.put("productManager",memberDto.getEmployeeNo());
                    }else if("2".equals(memberDto.getType())){
                        //技术负责人
                        mObject.put("technologyManager",memberDto.getEmployeeNo());
                    }
                }
            }
            return objectModelCrudI.updateByBid(TranscendModel.DOMAIN.getCode(),mObjectOld.getBid(),mObject);
        }else{
            List<ApmSpaceApp> apmSpaceAppVos = apmSpaceAppService.listBySpaceBidsAndModelCode(productSpaceBids,TranscendModel.DOMAIN.getCode());
            Map<String,String> sapceAppMap = apmSpaceAppVos.stream().collect(Collectors.toMap(ApmSpaceApp::getSpaceBid,ApmSpaceApp::getBid));
            //新增
            MObject mObject = new MObject();
            mObject.put("ccId", productDto.getId());
            mObject.setName(productDto.getProductName());
            mObject.put("enName", productDto.getProductEnName());
            mObject.put("enAbbreviationName",productDto.getProductShortName());
            mObject.setLifeCycleCode("NOSTART");
            mObject.put("lcModelCode","NOSTART:"+TranscendModel.DOMAIN.getCode());
            mObject.setBid(SnowflakeIdWorker.nextIdStr());
            mObject.setDeleteFlag(false);
            mObject.put("dataBid",mObject.getBid());
            mObject.put("productField",productDto.getFieldCode());
            mObject.setLcTemplBid("1195006087243104256");
            mObject.setLcTemplVersion("V1");
            mObject.setEnableFlag(true);
            mObject.setTenantId(101L);
            mObject.setCreatedBy(productDto.getCreateBy());
            List<MemberDto> members = productDto.getMembers();
            if(CollectionUtils.isNotEmpty(members)){
                for(MemberDto memberDto:members) {
                   if("1".equals(memberDto.getType())){
                       //产品负责人
                       mObject.put("productManager",memberDto.getEmployeeNo());
                   }else if("2".equals(memberDto.getType())){
                       //技术负责人
                       mObject.put("technologyManager",memberDto.getEmployeeNo());
                   }
                }
            }
            Map<String,String> codeAndSpaceMap = new HashMap<>();
            for(int i = 0; i < productCodes.size();i++){
                codeAndSpaceMap.put(productCodes.get(i),productSpaceBids.get(i));
            }
            mObject.put("spaceBid",codeAndSpaceMap.get(productDto.getFieldCode()));
            mObject.put("spaceAppBid",sapceAppMap.get(mObject.get("spaceBid")+""));
            mObject.put("permissionBid","APP:"+mObject.get("spaceAppBid"));
            MObject res = objectModelCrudI.add(TranscendModel.DOMAIN.getCode(),mObject);
            return res != null;
        }
    }
}
