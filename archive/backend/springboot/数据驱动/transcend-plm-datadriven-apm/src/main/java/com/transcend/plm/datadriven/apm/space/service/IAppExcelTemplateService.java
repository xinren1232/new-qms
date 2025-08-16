package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description Excel模板管理
 * @createTime 2023-12-05 10:51:00
 */
public interface IAppExcelTemplateService {
    /**
     * 下载Excel模板
     *
     * @param spaceAppBid 空间应用bid
     * @param type        type
     * @return {@link ByteArrayOutputStream}
     */
    ByteArrayOutputStream handleExcelTemplate(String spaceAppBid, String type);

    /**
     * importExcel
     *
     * @param spaceAppBid spaceAppBid
     * @param file        file
     * @param spaceBid    spaceBid
     * @return {@link List<MSpaceAppData>}
     */
    List<MSpaceAppData> importExcel(String spaceAppBid, MultipartFile file, String spaceBid);

    /**
     * importExcelAndSave
     *
     * @param spaceAppBid spaceAppBid
     * @param file        file
     * @param spaceBid    spaceBid
     * @return {@link boolean}
     */
    boolean importExcelAndSave(String spaceAppBid, MultipartFile file, String spaceBid);

    /**
     * exportExcel
     *
     * @param spaceAppBid spaceAppBid
     * @param type        type
     * @param mObjects    mObjects
     * @return {@link ByteArrayOutputStream}
     */
    ByteArrayOutputStream exportExcel(String spaceAppBid, String type, List<MObject> mObjects);
}
