package com.transcend.plm.alm.demandmanagement.entity.wrapper;

import com.transcend.plm.datadriven.common.wapper.MapWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendTreeObjectWrapper;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 特性树包装类型
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/6/6 10:15
 */
@ToString(callSuper = true)
@NoArgsConstructor
public class SystemFeatureWrapper extends TranscendTreeObjectWrapper {

    public SystemFeatureWrapper(@NotNull Map<String, Object> metadata) {
        super(metadata);
    }

    public SystemFeatureWrapper(@NonNull MapWrapper wrapper) {
        super(wrapper);
    }

    // 静态常量定义
    /**
     * 私有对标竞品技术指标
     * 文本
     */
    public final static String COMPETITOR_TECH_BENCHMARK = "competitorTechBenchmark";
    /**
     * 私有所属父级
     * 文本
     */
    public final static String PARENT_OF_AFFILIATION = "parentOfAffiliation";
    /**
     * 私有层级
     * 整型
     */
    public final static String LEVEL = "level";
    /**
     * 私有版本号
     * 文本
     */
    public final static String VERSION_NUMBER = "versionNumber";
    /**
     * 私有对标竞品功能指标
     * 文本
     */
    public final static String COMPETITOR_FEATURE_BENCHMARK = "competitorFeatureBenchmark";
    /**
     * 私有对标竞品水平
     * 文本
     */
    public final static String COMPETITOR_BENCHMARK_LEVEL = "competitorBenchmarkLevel";
    /**
     * 私有行业最优标杆竞品
     * 文本
     */
    public final static String INDUS_OPL_BEN_COMPETITOR = "indusOplBenCompetitor";
    /**
     * 私有引导方式
     * 文本
     */
    public final static String GUIDE_MODE = "guideMode";
    /**
     * 私有是否引导打开
     * 下拉选项
     */
    public final static String IS_GUIDED_TO_OPEN = "isGuidedToOpen";
    /**
     * 私有开关位置
     * 文本
     */
    public final static String SWITCH_POSITION = "switchPosition";
    /**
     * 私有开关
     * 下拉选项
     */
    public final static String SWITCH = "switch";
    /**
     * 私有依赖详情
     * 下拉选项
     */
    public final static String DEPENDENCY_DETAILS = "dependencyDetails";
    /**
     * 私有关联属性
     * 文本
     */
    public final static String ASSOCIATED_PROPERTY = "associatedProperty";
    /**
     * 私有硬件依赖
     * 下拉选项
     */
    public final static String HARDWARE_DEPENDENCE = "hardwareDependence";
    /**
     * 私有适用运营商
     * 下拉选项
     */
    public final static String APPLICABLE_CARRIER = "applicableCarrier";
    /**
     * 私有适用市场
     * 下拉选项
     */
    public final static String APPLICABLE_MARKET = "applicableMarket";
    /**
     * 私有需求产品线
     * 下拉选项
     */
    public final static String REQUIREMENT_PRODUCT_LINE = "requirementProductLine";
    /**
     * 私有适用品牌
     * 下拉选项
     */
    public final static String APPLICABLE_BRANDS = "applicableBrands";
    /**
     * 私有特性差异分类
     * 下拉选项
     */
    public final static String CHA_DIFF_CLASSIFICATION = "chaDiffClassification";
    /**
     * 私有备注
     * 大文本
     */
    public final static String REMARK = "remark";
    /**
     * 私有附件
     * JSON
     */
    public final static String FILES = "files";
    /**
     * 私有特性SE
     * JSON
     */
    public final static String FEATURE_SE = "featureSE";
    /**
     * 私有归属领域
     * JSON
     */
    public final static String BELONG_DOMAIN = "belongDomain";
    /**
     * 私有所有领域
     * JSON
     */
    public final static String ALL_FIELDS = "allFields";
    /**
     * 私有特性Owner
     * JSON
     */
    public final static String FEATURE_OWNER = "featureOwner";
    /**
     * 私有需求分类
     * 下拉选项
     */
    public final static String REQUIREMENT_CLASSIFICATION = "requirementClassification";
    /**
     * 私有需求描述
     * 大文本
     */
    public final static String DEMAND_DESC = "demandDesc";

    public String getCompetitorTechBenchmark() {
        return this.getStr(COMPETITOR_TECH_BENCHMARK);
    }

    public SystemFeatureWrapper setCompetitorTechBenchmark(String competitorTechBenchmark) {
        this.put(COMPETITOR_TECH_BENCHMARK, competitorTechBenchmark);
        return this;
    }

    public String getParentOfAffiliation() {
        return this.getStr(PARENT_OF_AFFILIATION);
    }

    public SystemFeatureWrapper setParentOfAffiliation(String parentOfAffiliation) {
        this.put(PARENT_OF_AFFILIATION, parentOfAffiliation);
        return this;
    }

    public Integer getLevel() {
        return this.getInt(LEVEL);
    }

    public SystemFeatureWrapper setLevel(Integer level) {
        this.put(LEVEL, level);
        return this;
    }

    public String getVersionNumber() {
        return this.getStr(VERSION_NUMBER);
    }

    public SystemFeatureWrapper setVersionNumber(String versionNumber) {
        this.put(VERSION_NUMBER, versionNumber);
        return this;
    }

    public String getCompetitorFeatureBenchmark() {
        return this.getStr(COMPETITOR_FEATURE_BENCHMARK);
    }

    public SystemFeatureWrapper setCompetitorFeatureBenchmark(String competitorFeatureBenchmark) {
        this.put(COMPETITOR_FEATURE_BENCHMARK, competitorFeatureBenchmark);
        return this;
    }

    public String getCompetitorBenchmarkLevel() {
        return this.getStr(COMPETITOR_BENCHMARK_LEVEL);
    }

    public SystemFeatureWrapper setCompetitorBenchmarkLevel(String competitorBenchmarkLevel) {
        this.put(COMPETITOR_BENCHMARK_LEVEL, competitorBenchmarkLevel);
        return this;
    }

    public String getIndusOplBenCompetitor() {
        return this.getStr(INDUS_OPL_BEN_COMPETITOR);
    }

    public SystemFeatureWrapper setIndusOplBenCompetitor(String indusOplBenCompetitor) {
        this.put(INDUS_OPL_BEN_COMPETITOR, indusOplBenCompetitor);
        return this;
    }

    public String getGuideMode() {
        return this.getStr(GUIDE_MODE);
    }

    public SystemFeatureWrapper setGuideMode(String guideMode) {
        this.put(GUIDE_MODE, guideMode);
        return this;
    }

    public String getIsGuidedToOpen() {
        return this.getStr(IS_GUIDED_TO_OPEN);
    }

    public SystemFeatureWrapper setIsGuidedToOpen(String isGuidedToOpen) {
        this.put(IS_GUIDED_TO_OPEN, isGuidedToOpen);
        return this;
    }

    public String getSwitchPosition() {
        return this.getStr(SWITCH_POSITION);
    }

    public SystemFeatureWrapper setSwitchPosition(String switchPosition) {
        this.put(SWITCH_POSITION, switchPosition);
        return this;
    }

    public String getSwitch() {
        return this.getStr(SWITCH);
    }

    public SystemFeatureWrapper setSwitch(String switchValue) {
        this.put(SWITCH, switchValue);
        return this;
    }

    public String getDependencyDetails() {
        return this.getStr(DEPENDENCY_DETAILS);
    }

    public SystemFeatureWrapper setDependencyDetails(String dependencyDetails) {
        this.put(DEPENDENCY_DETAILS, dependencyDetails);
        return this;
    }

    public String getAssociatedProperty() {
        return this.getStr(ASSOCIATED_PROPERTY);
    }

    public SystemFeatureWrapper setAssociatedProperty(String associatedProperty) {
        this.put(ASSOCIATED_PROPERTY, associatedProperty);
        return this;
    }

    public String getHardwareDependence() {
        return this.getStr(HARDWARE_DEPENDENCE);
    }

    public SystemFeatureWrapper setHardwareDependence(String hardwareDependence) {
        this.put(HARDWARE_DEPENDENCE, hardwareDependence);
        return this;
    }

    public String getApplicableCarrier() {
        return this.getStr(APPLICABLE_CARRIER);
    }

    public SystemFeatureWrapper setApplicableCarrier(String applicableCarrier) {
        this.put(APPLICABLE_CARRIER, applicableCarrier);
        return this;
    }

    public String getApplicableMarket() {
        return this.getStr(APPLICABLE_MARKET);
    }

    public SystemFeatureWrapper setApplicableMarket(String applicableMarket) {
        this.put(APPLICABLE_MARKET, applicableMarket);
        return this;
    }

    public String getRequirementProductLine() {
        return this.getStr(REQUIREMENT_PRODUCT_LINE);
    }

    public SystemFeatureWrapper setRequirementProductLine(String requirementProductLine) {
        this.put(REQUIREMENT_PRODUCT_LINE, requirementProductLine);
        return this;
    }

    public String getApplicableBrands() {
        return this.getStr(APPLICABLE_BRANDS);
    }

    public SystemFeatureWrapper setApplicableBrands(String applicableBrands) {
        this.put(APPLICABLE_BRANDS, applicableBrands);
        return this;
    }

    public String getChaDiffClassification() {
        return this.getStr(CHA_DIFF_CLASSIFICATION);
    }

    public SystemFeatureWrapper setChaDiffClassification(String chaDiffClassification) {
        this.put(CHA_DIFF_CLASSIFICATION, chaDiffClassification);
        return this;
    }

    public String getRemark() {
        return this.getStr(REMARK);
    }

    public SystemFeatureWrapper setRemark(String remark) {
        this.put(REMARK, remark);
        return this;
    }

    public Object getFiles() {
        return this.get(FILES);
    }

    public SystemFeatureWrapper setFiles(Object files) {
        this.put(FILES, files);
        return this;
    }

    public Object getFeatureSe() {
        return this.get(FEATURE_SE);
    }

    public SystemFeatureWrapper setFeatureSe(Object featureSe) {
        this.put(FEATURE_SE, featureSe);
        return this;
    }

    public Object getBelongDomain() {
        return this.get(BELONG_DOMAIN);
    }

    public SystemFeatureWrapper setBelongDomain(Object belongDomain) {
        this.put(BELONG_DOMAIN, belongDomain);
        return this;
    }

    public Object getAllFields() {
        return this.get(ALL_FIELDS);
    }

    public SystemFeatureWrapper setAllFields(Object allFields) {
        this.put(ALL_FIELDS, allFields);
        return this;
    }

    public Object getFeatureOwner() {
        return this.get(FEATURE_OWNER);
    }

    public SystemFeatureWrapper setFeatureOwner(Object featureOwner) {
        this.put(FEATURE_OWNER, featureOwner);
        return this;
    }

    public String getRequirementClassification() {
        return this.getStr(REQUIREMENT_CLASSIFICATION);
    }

    public SystemFeatureWrapper setRequirementClassification(String requirementClassification) {
        this.put(REQUIREMENT_CLASSIFICATION, requirementClassification);
        return this;
    }

    public String getDemandDesc() {
        return this.getStr(DEMAND_DESC);
    }

    public SystemFeatureWrapper setDemandDesc(String demandDesc) {
        this.put(DEMAND_DESC, demandDesc);
        return this;
    }

}
