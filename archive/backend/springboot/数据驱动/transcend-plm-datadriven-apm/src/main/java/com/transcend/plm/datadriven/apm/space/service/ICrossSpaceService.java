package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.api.model.dto.MObjectCheckDto;

import java.util.List;

/**
 * 跨空间操作
 *
 * @author unknown
 */
public interface ICrossSpaceService {
    /**
     * relationSelectListExpand
     *
     * @param spaceBid        spaceBid
     * @param modelCode       modelCode
     * @param source          source
     * @param mObjectCheckDto mObjectCheckDto
     * @return {@link List<MObject>}
     */
    List<MObject> relationSelectListExpand(String spaceBid, String modelCode, String source, MObjectCheckDto mObjectCheckDto);

    /**
     * selectTree
     *
     * @param spaceBid        spaceBid
     * @param modelCode       modelCode
     * @param relationMObject relationMObject
     * @return {@link List<MObjectTree>}
     */
    List<MObjectTree> selectTree(String spaceBid, String modelCode, RelationMObject relationMObject);
}
