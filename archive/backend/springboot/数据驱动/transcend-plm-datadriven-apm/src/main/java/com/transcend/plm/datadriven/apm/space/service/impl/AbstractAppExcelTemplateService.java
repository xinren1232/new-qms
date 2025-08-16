package com.transcend.plm.datadriven.apm.space.service.impl;

import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.plm.configcenter.api.model.view.dto.ViewProperty;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.space.service.IAppExcelTemplateService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description Excel模板管理
 * @createTime 2023-12-05 10:54:00
 */
@SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
public abstract class AbstractAppExcelTemplateService implements IAppExcelTemplateService {
    @Override
    public final ByteArrayOutputStream handleExcelTemplate(String spaceAppBid, String type) {
        List<ViewProperty> viewProperties =getSpaceAppViewConfig(spaceAppBid);
        ByteArrayOutputStream outputStream;
        try {
            outputStream = handleExcelOutputStream(spaceAppBid, type, viewProperties);
        } catch (IOException e) {
            throw new TranscendBizException("生成Excel模板失败", e);
        }
        return outputStream;
    }

    @Override
    public final ByteArrayOutputStream exportExcel(String spaceAppBid, String type, List<MObject> mObjects) {
        //生成模板
        try (ByteArrayOutputStream excelTemplate = handleExcelTemplate(spaceAppBid, type)) {
            //填充数据
            return handleExcelData(excelTemplate, mObjects,spaceAppBid,type);
        } catch (IOException e) {
            throw new TranscendBizException("导出Excel失败", e);
        }
    }

    /**
     * 处理Excel数据的抽象方法。
     *
     * @param excelTemplate Excel模板的字节数组流
     * @param mObjects      数据对象的列表
     * @param spaceAppBid   空间应用的bid
     * @param type          类型
     * @return 处理后的Excel数据的字节数组流
     * @throws IOException 如果处理Excel数据时发生IO异常
     */
    public abstract ByteArrayOutputStream handleExcelData(ByteArrayOutputStream excelTemplate, List<MObject> mObjects,String spaceAppBid ,String type) throws IOException;

    /**
     * 处理 Excel 输出流的抽象方法
     *
     * @param spaceAppBid 空间应用bid
     * @param type 类型
     * @param viewProperties 视图属性列表
     * @return 处理后的 ByteArrayOutputStream 对象
     * @throws IOException 如果处理过程中发生 I/O 异常
     */
    public abstract ByteArrayOutputStream handleExcelOutputStream(String spaceAppBid, String type, List<ViewProperty> viewProperties) throws IOException;

    /**
     * 获取应用的视图配置
     *
     * @param spaceAppBid 空间应用bid
     */
    public abstract List<ViewProperty> getSpaceAppViewConfig(String spaceAppBid);
}
