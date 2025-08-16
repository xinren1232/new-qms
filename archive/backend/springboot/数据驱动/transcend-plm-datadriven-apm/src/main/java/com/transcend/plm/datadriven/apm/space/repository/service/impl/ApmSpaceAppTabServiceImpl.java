package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiAppConfig;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmSpaceAppTabMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppTab;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppTabService;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
public class ApmSpaceAppTabServiceImpl extends ServiceImpl<ApmSpaceAppTabMapper, ApmSpaceAppTab>
    implements ApmSpaceAppTabService {

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Override
    public boolean enableBySpaceAppBid(ApmSpaceAppTab apmSpaceAppTab){
        List<ApmSpaceAppTab> list;
        if(StringUtils.isNotEmpty(apmSpaceAppTab.getTargetSpaceAppBid())){
            list = list(Wrappers.<ApmSpaceAppTab>lambdaQuery().eq(ApmSpaceAppTab::getSpaceAppBid, apmSpaceAppTab.getSpaceAppBid()).eq(ApmSpaceAppTab::getTargetSpaceAppBid, apmSpaceAppTab.getTargetSpaceAppBid()).eq(ApmSpaceAppTab::getDeleteFlag,false));
        }else{
            list = list(Wrappers.<ApmSpaceAppTab>lambdaQuery().eq(ApmSpaceAppTab::getSpaceAppBid, apmSpaceAppTab.getSpaceAppBid()).eq(ApmSpaceAppTab::getCode, apmSpaceAppTab.getCode()).eq(ApmSpaceAppTab::getDeleteFlag,false));
        }
        if(CollectionUtils.isNotEmpty(list)){
            for(ApmSpaceAppTab apmSpaceAppTab1:list){
                apmSpaceAppTab1.setEnableFlag(false);
            }
            updateBatchById(list);
        }
        return true;
    }

    /**
     * 拷贝单个应用的tab配置
     * @param apmSpaceBidMap
     * @param apmSpaceAppBidMap
     * @param sphereMap
     * @return
     */
    @Override
    public boolean copyApmSpaceAbbTab(Map<String,String> apmSpaceBidMap, Map<String,String> apmSpaceAppBidMap, Map<String,String> sphereMap,String nowSpaceBid){
        List<ApmSpaceAppTab> list = list(Wrappers.<ApmSpaceAppTab>lambdaQuery().in(ApmSpaceAppTab::getSpaceAppBid, apmSpaceAppBidMap.keySet()).eq(ApmSpaceAppTab::getDeleteFlag,false).eq(ApmSpaceAppTab::getEnableFlag,true));
        Map<String,String> spaceAllAppBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        spaceAllAppBidMap.putAll(apmSpaceAppBidMap);
        if(CollectionUtils.isNotEmpty(list)){
            List<String> targetSpaceAppBidList = new ArrayList<>();
            for(ApmSpaceAppTab apmSpaceAppTab:list){
                if(StringUtils.isNotEmpty(apmSpaceAppTab.getTargetSpaceAppBid())){
                    targetSpaceAppBidList.add(apmSpaceAppTab.getTargetSpaceAppBid());
                }
            }
            List<ApmSpaceAppVo> apmSpaceAppVos = Lists.newArrayList();
            if(CollectionUtils.isNotEmpty(targetSpaceAppBidList)){
                apmSpaceAppVos = apmSpaceAppService.listSpaceInfo(targetSpaceAppBidList);
            }
            Map<String,String> modelCodeRelMap = apmSpaceAppVos.stream().collect(Collectors.toMap(ApmSpaceAppVo::getBid,ApmSpaceAppVo::getModelCode));
            List<ApmSpaceApp> apmSpaceApps = apmSpaceAppService.listSpaceAppWithOrder(nowSpaceBid,true);
            Map<String,String> modelCodeRelMap2 = apmSpaceApps.stream().collect(Collectors.toMap(ApmSpaceApp::getModelCode,ApmSpaceApp::getBid,(a,b)->a));
            for(int i=list.size()-1;i>=0;i--){
                ApmSpaceAppTab apmSpaceAppTab = list.get(i);
                if(StringUtils.isNotEmpty(apmSpaceAppTab.getTargetSpaceAppBid()) && !modelCodeRelMap2.containsKey(modelCodeRelMap.get(apmSpaceAppTab.getTargetSpaceAppBid()))){
                    list.remove(i);
                }else{
                    spaceAllAppBidMap.put(apmSpaceAppTab.getTargetSpaceAppBid(),modelCodeRelMap2.get(modelCodeRelMap.get(apmSpaceAppTab.getTargetSpaceAppBid())));
                }
            }
        }
        if(CollectionUtils.isNotEmpty(list)){
            for(ApmSpaceAppTab apmSpaceAppTab:list){
                apmSpaceAppTab.setId(null);
                apmSpaceAppTab.setBid(SnowflakeIdWorker.nextIdStr());
                apmSpaceAppTab.setSpaceAppBid(apmSpaceAppBidMap.get(apmSpaceAppTab.getSpaceAppBid()));
                apmSpaceAppTab.setTargetSpaceAppBid(spaceAllAppBidMap.get(apmSpaceAppTab.getTargetSpaceAppBid()));
                List<Map> multiTreeContent = apmSpaceAppTab.getMultiTreeContent();
                if(CollectionUtils.isNotEmpty(multiTreeContent)){
                    String con = JSON.toJSONString(multiTreeContent);
                    String newCon = replateBids(apmSpaceBidMap,spaceAllAppBidMap,sphereMap,con);
                    List<Map> multiTreeContentNew = JSON.parseArray(newCon,Map.class);
                    apmSpaceAppTab.setMultiTreeContent(multiTreeContentNew);
                }
                ModelMixQo configContent = apmSpaceAppTab.getConfigContent();
                if(configContent != null){
                    String con = JSON.toJSONString(configContent);
                    String newCon = replateBids(apmSpaceBidMap,spaceAllAppBidMap,sphereMap,con);
                    ModelMixQo modelMixQo = JSON.parseObject(newCon,ModelMixQo.class);
                    apmSpaceAppTab.setConfigContent(modelMixQo);
                }
                MultiAppConfig multiAppTreeContent = apmSpaceAppTab.getMultiAppTreeContent();
                if(multiAppTreeContent != null){
                    String con = JSON.toJSONString(multiAppTreeContent);
                    String newCon = replateBids(apmSpaceBidMap,spaceAllAppBidMap,sphereMap,con);
                    MultiAppConfig multiAppConfig = JSON.parseObject(newCon,MultiAppConfig.class);
                    apmSpaceAppTab.setMultiAppTreeContent(multiAppConfig);
                }

            }
            saveBatch(list);
        }
        return true;
    }

    @Override
    public boolean copyApmSpaceAbbTabs(Map<String,String> apmSpaceBidMap, Map<String,String> apmSpaceAppBidMap, Map<String,String> sphereMap){
        List<ApmSpaceAppTab> list = list(Wrappers.<ApmSpaceAppTab>lambdaQuery().in(ApmSpaceAppTab::getSpaceAppBid, apmSpaceAppBidMap.keySet()).eq(ApmSpaceAppTab::getDeleteFlag,false).eq(ApmSpaceAppTab::getEnableFlag,true));
        for(ApmSpaceAppTab apmSpaceAppTab:list){
            apmSpaceAppTab.setId(null);
            apmSpaceAppTab.setBid(SnowflakeIdWorker.nextIdStr());
            apmSpaceAppTab.setSpaceAppBid(apmSpaceAppBidMap.get(apmSpaceAppTab.getSpaceAppBid()));
            apmSpaceAppTab.setTargetSpaceAppBid(apmSpaceAppBidMap.get(apmSpaceAppTab.getTargetSpaceAppBid()));
            List<Map> multiTreeContent = apmSpaceAppTab.getMultiTreeContent();
            if(CollectionUtils.isNotEmpty(multiTreeContent)){
                String con = JSON.toJSONString(multiTreeContent);
                String newCon = replateBids(apmSpaceBidMap,apmSpaceAppBidMap,sphereMap,con);
                List<Map> multiTreeContentNew = JSON.parseArray(newCon,Map.class);
                apmSpaceAppTab.setMultiTreeContent(multiTreeContentNew);
            }
            ModelMixQo configContent = apmSpaceAppTab.getConfigContent();
            if(configContent != null){
                String con = JSON.toJSONString(configContent);
                String newCon = replateBids(apmSpaceBidMap,apmSpaceAppBidMap,sphereMap,con);
                ModelMixQo modelMixQo = JSON.parseObject(newCon,ModelMixQo.class);
                apmSpaceAppTab.setConfigContent(modelMixQo);
            }
            MultiAppConfig multiAppTreeContent = apmSpaceAppTab.getMultiAppTreeContent();
            if(multiAppTreeContent != null){
                String con = JSON.toJSONString(multiAppTreeContent);
                String newCon = replateBids(apmSpaceBidMap,apmSpaceAppBidMap,sphereMap,con);
                MultiAppConfig multiAppConfig = JSON.parseObject(newCon,MultiAppConfig.class);
                apmSpaceAppTab.setMultiAppTreeContent(multiAppConfig);
            }

        }
        saveBatch(list);
        return true;
    }

    @Override
    public ApmSpaceAppTab getByBid(String tabBid) {
        return getOne(Wrappers.<ApmSpaceAppTab>lambdaQuery().eq(ApmSpaceAppTab::getBid, tabBid));
    }

    private String replateBids(Map<String,String> apmSpaceBidMap,Map<String,String> apmSpaceAppBidMap,Map<String,String> sphereMap,String content){
        content = replateByMap(apmSpaceBidMap,content);
        content = replateByMap(apmSpaceAppBidMap,content);
        content = replateByMap(sphereMap,content);
        return content;
    }

    private String replateByMap(Map<String,String> map,String content){
        if(StringUtils.isNotEmpty(content) && CollectionUtils.isNotEmpty(map)){
            for(Map.Entry<String,String> entry:map.entrySet()){
                if(StringUtils.isNotEmpty(entry.getKey()) && StringUtils.isNotEmpty(entry.getValue())){
                    content = content.replaceAll(entry.getKey(),entry.getValue());
                }
            }
        }
        return content;
    }

    @Override
    public boolean deleteBySpaceAppBid(ApmSpaceAppTab apmSpaceAppTab) {
        List<ApmSpaceAppTab> list;
        if(StringUtils.isNotEmpty(apmSpaceAppTab.getTargetSpaceAppBid())){
            list = list(Wrappers.<ApmSpaceAppTab>lambdaQuery().eq(ApmSpaceAppTab::getSpaceAppBid, apmSpaceAppTab.getSpaceAppBid())
                    .eq(ApmSpaceAppTab::getTargetSpaceAppBid, apmSpaceAppTab.getTargetSpaceAppBid())
                    .eq(ApmSpaceAppTab::getRelationModelCode, apmSpaceAppTab.getRelationModelCode())
                    .eq(ApmSpaceAppTab::getDeleteFlag,false));
        }else{
            list = list(Wrappers.<ApmSpaceAppTab>lambdaQuery().eq(ApmSpaceAppTab::getSpaceAppBid, apmSpaceAppTab.getSpaceAppBid())
                    .eq(ApmSpaceAppTab::getCode, apmSpaceAppTab.getCode()).eq(ApmSpaceAppTab::getDeleteFlag,false));
        }
        if(CollectionUtils.isNotEmpty(list)){
            List<Long> ids = list.stream().map(ApmSpaceAppTab::getId).collect(Collectors.toList());
            return remove(Wrappers.<ApmSpaceAppTab>lambdaQuery().in(ApmSpaceAppTab::getId, ids));
        }
        return true;
    }

    @Override
    public List<ApmSpaceAppTab> listBySpaceAppBid(String spaceAppBid){
        List<ApmSpaceAppTab> list = list(Wrappers.<ApmSpaceAppTab>lambdaQuery().eq(ApmSpaceAppTab::getSpaceAppBid, spaceAppBid).eq(ApmSpaceAppTab::getDeleteFlag,false));
        return list;
    }


    @Override
    public List<ApmSpaceAppTab> listBySpaceAppBids(List<String> spaceAppBids,String relationModelCode){
        List<ApmSpaceAppTab> list = list(Wrappers.<ApmSpaceAppTab>lambdaQuery().in(ApmSpaceAppTab::getSpaceAppBid, spaceAppBids).eq(ApmSpaceAppTab::getRelationModelCode,relationModelCode).eq(ApmSpaceAppTab::getDeleteFlag,false).eq(ApmSpaceAppTab::getEnableFlag,true));
        return list;
    }

    @Override
    public ApmSpaceAppTab getBySpaceAppBidAndRelationModelCode(String spaceAppBid, String relationModelCode) {
        return getOne(Wrappers.<ApmSpaceAppTab>lambdaQuery()
                .eq(ApmSpaceAppTab::getSpaceAppBid, spaceAppBid)
                .eq(ApmSpaceAppTab::getRelationModelCode, relationModelCode)
                .eq(ApmSpaceAppTab::getDeleteFlag,false));
    }
}




