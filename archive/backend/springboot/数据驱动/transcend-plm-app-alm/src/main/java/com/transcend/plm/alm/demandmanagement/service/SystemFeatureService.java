package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

import java.util.List;
import java.util.Set;

public interface SystemFeatureService {


    List<MObjectTree> tree(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo, boolean b);

    List<MObjectTree> getSourceData(String spaceBid, String targetSpaceAppBid, ModelMixQo modelMixQo);

    List<MObjectTree> selectSF(String spaceBid, String targetSpaceAppBid, String parentBid);

    Boolean addSFRelation(MSpaceAppData mSpaceAppData, String spaceBid, String irBid, Set<String> sfBids);

    Boolean addL2SFRelation(MSpaceAppData mSpaceAppData, String spaceBid, String irBid, String l2SfBid);

    void deleteIrRsfRelation(List<String> mObjectList);

    MObject assembleRelationObject(String modelCode, Object sourceBid, Object targetBid, String spaceBid);

}
