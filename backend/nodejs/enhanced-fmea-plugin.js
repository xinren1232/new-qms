/**
 * 增强版FMEA失效模式分析插件
 * 基于开源知识库和行业标准的专业实现
 * 参考：IEC 60812、AIAG FMEA手册第四版、ISO 14971
 */

/**
 * 手机制造业FMEA知识库（基于开源项目整理）
 */
const MOBILE_FMEA_KNOWLEDGE = {
  // 手机主要组件失效模式库
  components: {
    screen: {
      name: '显示屏',
      failure_modes: [
        { mode: '屏幕破裂', causes: ['跌落冲击', '压力过大', '材料缺陷'], effects: ['显示异常', '触摸失效', '用户体验差'] },
        { mode: '显示异常', causes: ['驱动IC故障', '连接线松动', '背光故障'], effects: ['无法正常显示', '色彩失真', '亮度不均'] },
        { mode: '触摸失效', causes: ['触摸IC故障', '静电损伤', '软件bug'], effects: ['无法操作', '误触', '响应迟缓'] },
        { mode: '亮度不均', causes: ['背光分布不均', 'LED老化', '导光板缺陷'], effects: ['视觉体验差', '眼疲劳'] }
      ],
      detection_methods: ['外观检查', '功能测试', '光学检测', '触摸测试'],
      prevention_methods: ['跌落测试', '压力测试', '老化测试', '静电防护']
    },
    
    battery: {
      name: '电池',
      failure_modes: [
        { mode: '容量衰减', causes: ['充放电循环', '高温环境', '过充过放'], effects: ['续航下降', '用户满意度降低'] },
        { mode: '充电异常', causes: ['充电IC故障', '接口损坏', '电池老化'], effects: ['无法充电', '充电缓慢', '发热'] },
        { mode: '电池膨胀', causes: ['过充', '高温', '电解液分解'], effects: ['外壳变形', '安全隐患', '功能异常'] },
        { mode: '发热异常', causes: ['内阻增大', '短路', '过流'], effects: ['安全风险', '性能下降', '用户烫伤'] }
      ],
      detection_methods: ['容量测试', '温度监控', '外观检查', '充电测试'],
      prevention_methods: ['温度控制', '充电管理', '质量筛选', '老化测试']
    },
    
    camera: {
      name: '摄像头',
      failure_modes: [
        { mode: '对焦失效', causes: ['马达故障', '镜头污染', '软件算法'], effects: ['图像模糊', '拍照质量差'] },
        { mode: '图像噪点', causes: ['传感器故障', '信号干扰', '光线不足'], effects: ['图像质量差', '用户体验差'] },
        { mode: '色彩异常', causes: ['白平衡错误', '传感器老化', '镜头污染'], effects: ['色彩失真', '拍照效果差'] },
        { mode: '无法启动', causes: ['供电故障', '连接异常', '软件故障'], effects: ['功能完全失效', '用户投诉'] }
      ],
      detection_methods: ['图像质量测试', '自动对焦测试', '色彩校准', '功能测试'],
      prevention_methods: ['清洁控制', '防尘设计', '软件优化', '供电稳定']
    },
    
    mainboard: {
      name: '主板',
      failure_modes: [
        { mode: '短路', causes: ['焊接不良', '元件故障', '水分侵入'], effects: ['设备无法启动', '功能异常', '安全隐患'] },
        { mode: '元件失效', causes: ['质量问题', '静电损伤', '过应力'], effects: ['功能缺失', '性能下降', '系统不稳定'] },
        { mode: '信号干扰', causes: ['布线不当', '屏蔽不足', '电磁干扰'], effects: ['通信异常', '音频噪声', '性能下降'] },
        { mode: '焊接不良', causes: ['工艺问题', '材料质量', '温度控制'], effects: ['连接不稳定', '间歇性故障', '可靠性差'] }
      ],
      detection_methods: ['电气测试', 'ICT测试', '功能测试', 'X-ray检测'],
      prevention_methods: ['工艺控制', '静电防护', '质量检验', '环境控制']
    }
  },
  
  // 风险评估标准（基于IEC 60812）
  risk_criteria: {
    severity: {
      1: { level: '无影响', description: '用户无感知，不影响功能' },
      2: { level: '轻微影响', description: '用户轻微不满，功能轻微影响' },
      3: { level: '小影响', description: '用户不满，功能小幅影响' },
      4: { level: '中等影响', description: '用户明显不满，功能中等影响' },
      5: { level: '显著影响', description: '用户很不满，功能显著影响' },
      6: { level: '重大影响', description: '用户极不满，功能重大影响' },
      7: { level: '严重影响', description: '功能严重受损，影响使用' },
      8: { level: '极严重影响', description: '主要功能失效，无法正常使用' },
      9: { level: '危险影响', description: '存在安全隐患，可能造成伤害' },
      10: { level: '灾难性影响', description: '严重安全问题，可能致命' }
    },
    
    occurrence: {
      1: { level: '极不可能', probability: '<0.01%', description: '理论上可能，实际几乎不发生' },
      2: { level: '很不可能', probability: '0.01-0.1%', description: '很少发生，偶尔出现' },
      3: { level: '不太可能', probability: '0.1-0.5%', description: '不常发生，偶有报告' },
      4: { level: '较不可能', probability: '0.5-1%', description: '较少发生，有时出现' },
      5: { level: '可能', probability: '1-2%', description: '可能发生，定期出现' },
      6: { level: '较可能', probability: '2-5%', description: '较常发生，经常出现' },
      7: { level: '很可能', probability: '5-10%', description: '经常发生，频繁出现' },
      8: { level: '高可能', probability: '10-20%', description: '高频发生，大量出现' },
      9: { level: '极可能', probability: '20-50%', description: '极高频率，大部分出现' },
      10: { level: '几乎确定', probability: '>50%', description: '几乎必然发生' }
    },
    
    detection: {
      1: { level: '几乎确定检出', description: '自动检测，100%发现' },
      2: { level: '很高检出率', description: '自动检测，>95%发现' },
      3: { level: '高检出率', description: '自动检测，>90%发现' },
      4: { level: '中高检出率', description: '半自动检测，>80%发现' },
      5: { level: '中等检出率', description: '人工检测，>70%发现' },
      6: { level: '中低检出率', description: '人工检测，>60%发现' },
      7: { level: '低检出率', description: '抽样检测，>40%发现' },
      8: { level: '很低检出率', description: '抽样检测，>20%发现' },
      9: { level: '极低检出率', description: '很难检测，<20%发现' },
      10: { level: '几乎无法检出', description: '无法检测，用户发现' }
    }
  },
  
  // 改进措施库
  improvement_actions: {
    design: ['设计优化', '材料升级', '结构改进', '冗余设计'],
    process: ['工艺改进', '参数优化', '设备升级', '培训加强'],
    detection: ['检测增强', '测试完善', '监控加强', '预警系统'],
    prevention: ['预防维护', '环境控制', '质量控制', '供应商管理']
  }
};

/**
 * 增强版FMEA分析器
 */
class EnhancedFMEAAnalyzer {
  constructor() {
    this.knowledge = MOBILE_FMEA_KNOWLEDGE;
  }

  /**
   * 执行FMEA分析
   */
  async execute(input = {}) {
    try {
      const { components, analysisType = 'design', industry = 'mobile' } = input;
      
      if (!Array.isArray(components) || components.length === 0) {
        return this.createErrorResult('未提供有效的组件数据');
      }

      // 执行FMEA分析
      const analysis = await this.performFMEAAnalysis(components, analysisType);
      
      // 风险评估
      const riskAssessment = this.assessOverallRisk(analysis);
      
      // 生成改进建议
      const improvements = this.generateImprovements(analysis);
      
      // 生成报告
      const report = this.generateReport(analysis, riskAssessment, improvements);

      return {
        success: true,
        type: 'fmea_analysis',
        analysisType: analysisType,
        industry: industry,
        
        // 详细分析结果
        analysis: analysis,
        
        // 风险评估
        riskAssessment: riskAssessment,
        
        // 改进建议
        improvements: improvements,
        
        // 汇总报告
        report: report,
        
        // 统计信息
        statistics: this.calculateStatistics(analysis)
      };

    } catch (error) {
      return this.createErrorResult(error.message);
    }
  }

  /**
   * 执行FMEA分析
   */
  async performFMEAAnalysis(components, analysisType) {
    const analysis = [];

    for (const component of components) {
      const componentAnalysis = await this.analyzeComponent(component, analysisType);
      analysis.push(componentAnalysis);
    }

    return analysis;
  }

  /**
   * 分析单个组件
   */
  async analyzeComponent(component, analysisType) {
    const { name, failure_modes = [] } = component;
    
    // 从知识库获取标准失效模式
    const knowledgeComponent = this.findKnowledgeComponent(name);
    const standardFailureModes = knowledgeComponent ? knowledgeComponent.failure_modes : [];
    
    // 合并用户输入和知识库数据
    const allFailureModes = this.mergeFailureModes(failure_modes, standardFailureModes);
    
    const failureModeAnalysis = allFailureModes.map(failureMode => {
      return this.analyzeFaiureMode(component, failureMode, analysisType);
    });

    return {
      component: name,
      componentType: knowledgeComponent ? knowledgeComponent.name : name,
      failureModes: failureModeAnalysis,
      overallRisk: this.calculateComponentRisk(failureModeAnalysis),
      recommendations: this.getComponentRecommendations(failureModeAnalysis, knowledgeComponent)
    };
  }

  /**
   * 分析失效模式
   */
  analyzeFaiureMode(component, failureMode, analysisType) {
    // 获取或计算RPN评分
    const severity = failureMode.severity || this.estimateSeverity(failureMode);
    const occurrence = failureMode.occurrence || this.estimateOccurrence(failureMode);
    const detection = failureMode.detection || this.estimateDetection(failureMode);
    
    const rpn = severity * occurrence * detection;
    
    // 风险等级评估
    const riskLevel = this.assessRiskLevel(rpn, severity);
    
    // 获取改进建议
    const actions = this.getImprovementActions(failureMode, riskLevel);

    return {
      failureMode: failureMode.mode || failureMode.name,
      causes: failureMode.causes || [],
      effects: failureMode.effects || [],
      currentControls: failureMode.currentControls || [],
      
      // RPN评分
      severity: {
        score: severity,
        description: this.knowledge.risk_criteria.severity[severity]
      },
      occurrence: {
        score: occurrence,
        description: this.knowledge.risk_criteria.occurrence[occurrence]
      },
      detection: {
        score: detection,
        description: this.knowledge.risk_criteria.detection[detection]
      },
      
      // 风险评估
      rpn: rpn,
      riskLevel: riskLevel,
      priority: this.calculatePriority(rpn, severity, riskLevel),
      
      // 改进措施
      recommendedActions: actions,
      
      // 目标RPN（改进后）
      targetRPN: this.calculateTargetRPN(rpn, actions)
    };
  }

  /**
   * 查找知识库组件
   */
  findKnowledgeComponent(componentName) {
    const normalizedName = componentName.toLowerCase();
    
    // 精确匹配
    for (const [key, component] of Object.entries(this.knowledge.components)) {
      if (component.name.toLowerCase() === normalizedName) {
        return component;
      }
    }
    
    // 模糊匹配
    for (const [key, component] of Object.entries(this.knowledge.components)) {
      if (normalizedName.includes(key) || key.includes(normalizedName)) {
        return component;
      }
    }
    
    return null;
  }

  /**
   * 合并失效模式
   */
  mergeFailureModes(userModes, standardModes) {
    const merged = [...userModes];
    
    // 添加知识库中的标准失效模式
    for (const standardMode of standardModes) {
      const exists = userModes.some(userMode => 
        (userMode.mode || userMode.name) === standardMode.mode
      );
      
      if (!exists) {
        merged.push({
          mode: standardMode.mode,
          causes: standardMode.causes,
          effects: standardMode.effects,
          isStandard: true
        });
      }
    }
    
    return merged;
  }

  /**
   * 估算严重度
   */
  estimateSeverity(failureMode) {
    const effects = failureMode.effects || [];
    
    // 基于影响关键词估算严重度
    const severityKeywords = {
      10: ['致命', '灾难', '安全', '伤害'],
      9: ['危险', '严重', '无法使用'],
      8: ['极严重', '主要功能失效'],
      7: ['严重', '功能受损'],
      6: ['重大', '明显影响'],
      5: ['显著', '用户不满'],
      4: ['中等', '功能影响'],
      3: ['小', '轻微影响'],
      2: ['轻微', '用户感知'],
      1: ['无', '无影响']
    };
    
    for (const [score, keywords] of Object.entries(severityKeywords)) {
      for (const keyword of keywords) {
        if (effects.some(effect => effect.includes(keyword))) {
          return parseInt(score);
        }
      }
    }
    
    return 5; // 默认中等严重度
  }

  /**
   * 估算发生率
   */
  estimateOccurrence(failureMode) {
    // 基于原因复杂度和控制措施估算
    const causes = failureMode.causes || [];
    const controls = failureMode.currentControls || [];
    
    let baseOccurrence = Math.min(causes.length, 8); // 原因越多，发生率越高
    const controlReduction = Math.min(controls.length * 2, 6); // 控制措施减少发生率
    
    return Math.max(1, baseOccurrence - controlReduction);
  }

  /**
   * 估算检出度
   */
  estimateDetection(failureMode) {
    const controls = failureMode.currentControls || [];
    
    if (controls.length === 0) return 9; // 无控制措施，很难检出
    if (controls.some(c => c.includes('自动'))) return 2; // 自动检测
    if (controls.some(c => c.includes('测试'))) return 4; // 测试检测
    if (controls.some(c => c.includes('检查'))) return 6; // 人工检查
    
    return 7; // 默认较难检出
  }

  /**
   * 评估风险等级
   */
  assessRiskLevel(rpn, severity) {
    if (rpn >= 200 || severity >= 9) return 'critical';
    if (rpn >= 100 || severity >= 7) return 'high';
    if (rpn >= 50 || severity >= 5) return 'medium';
    return 'low';
  }

  /**
   * 获取改进措施
   */
  getImprovementActions(failureMode, riskLevel) {
    const actions = [];
    
    switch (riskLevel) {
      case 'critical':
        actions.push('立即停止生产，紧急调查');
        actions.push('实施临时控制措施');
        actions.push('重新设计或更换组件');
        break;
      case 'high':
        actions.push('优先处理，加强监控');
        actions.push('改进检测方法');
        actions.push('增加预防措施');
        break;
      case 'medium':
        actions.push('定期监控');
        actions.push('改进工艺控制');
        break;
      case 'low':
        actions.push('持续监控');
        break;
    }
    
    return actions;
  }

  /**
   * 计算目标RPN
   */
  calculateTargetRPN(currentRPN, actions) {
    // 基于改进措施估算RPN降低幅度
    const reductionFactor = Math.min(actions.length * 0.3, 0.8);
    return Math.round(currentRPN * (1 - reductionFactor));
  }

  /**
   * 计算优先级
   */
  calculatePriority(rpn, severity, riskLevel) {
    const riskWeights = { critical: 4, high: 3, medium: 2, low: 1 };
    return rpn * 0.7 + severity * 10 + riskWeights[riskLevel] * 20;
  }

  /**
   * 计算组件风险
   */
  calculateComponentRisk(failureModeAnalysis) {
    const totalRPN = failureModeAnalysis.reduce((sum, fm) => sum + fm.rpn, 0);
    const maxSeverity = Math.max(...failureModeAnalysis.map(fm => fm.severity.score));
    const criticalCount = failureModeAnalysis.filter(fm => fm.riskLevel === 'critical').length;
    
    return {
      totalRPN: totalRPN,
      averageRPN: totalRPN / failureModeAnalysis.length,
      maxSeverity: maxSeverity,
      criticalFailureModes: criticalCount,
      riskLevel: criticalCount > 0 ? 'critical' : totalRPN > 500 ? 'high' : 'medium'
    };
  }

  /**
   * 获取组件建议
   */
  getComponentRecommendations(failureModeAnalysis, knowledgeComponent) {
    const recommendations = [];
    
    const highRiskModes = failureModeAnalysis.filter(fm => 
      fm.riskLevel === 'critical' || fm.riskLevel === 'high'
    );
    
    if (highRiskModes.length > 0) {
      recommendations.push('优先关注高风险失效模式');
      recommendations.push('加强质量控制和检测');
    }
    
    if (knowledgeComponent && knowledgeComponent.prevention_methods) {
      recommendations.push(...knowledgeComponent.prevention_methods.map(method => 
        `建议实施: ${method}`
      ));
    }
    
    return recommendations;
  }

  /**
   * 评估整体风险
   */
  assessOverallRisk(analysis) {
    const allFailureModes = analysis.flatMap(comp => comp.failureModes);
    const totalRPN = allFailureModes.reduce((sum, fm) => sum + fm.rpn, 0);
    const criticalCount = allFailureModes.filter(fm => fm.riskLevel === 'critical').length;
    const highCount = allFailureModes.filter(fm => fm.riskLevel === 'high').length;
    
    return {
      totalRPN: totalRPN,
      averageRPN: totalRPN / allFailureModes.length,
      criticalFailureModes: criticalCount,
      highRiskFailureModes: highCount,
      overallRiskLevel: criticalCount > 0 ? 'critical' : 
                       highCount > 3 ? 'high' : 
                       totalRPN > 1000 ? 'medium' : 'low',
      riskDistribution: {
        critical: criticalCount,
        high: highCount,
        medium: allFailureModes.filter(fm => fm.riskLevel === 'medium').length,
        low: allFailureModes.filter(fm => fm.riskLevel === 'low').length
      }
    };
  }

  /**
   * 生成改进建议
   */
  generateImprovements(analysis) {
    const improvements = {
      immediate: [],
      shortTerm: [],
      longTerm: []
    };
    
    const allFailureModes = analysis.flatMap(comp => comp.failureModes);
    
    // 立即措施
    const criticalModes = allFailureModes.filter(fm => fm.riskLevel === 'critical');
    if (criticalModes.length > 0) {
      improvements.immediate.push('立即处理所有关键风险失效模式');
      improvements.immediate.push('实施紧急控制措施');
    }
    
    // 短期措施
    const highRiskModes = allFailureModes.filter(fm => fm.riskLevel === 'high');
    if (highRiskModes.length > 0) {
      improvements.shortTerm.push('制定高风险失效模式改进计划');
      improvements.shortTerm.push('加强检测和监控');
    }
    
    // 长期措施
    improvements.longTerm.push('建立持续改进机制');
    improvements.longTerm.push('定期更新FMEA分析');
    
    return improvements;
  }

  /**
   * 生成报告
   */
  generateReport(analysis, riskAssessment, improvements) {
    return {
      summary: {
        componentsAnalyzed: analysis.length,
        totalFailureModes: analysis.reduce((sum, comp) => sum + comp.failureModes.length, 0),
        overallRiskLevel: riskAssessment.overallRiskLevel,
        priorityActions: improvements.immediate.length + improvements.shortTerm.length
      },
      keyFindings: this.extractKeyFindings(analysis, riskAssessment),
      actionPlan: improvements,
      nextSteps: [
        '实施优先改进措施',
        '建立监控机制',
        '定期评审和更新',
        '培训相关人员'
      ]
    };
  }

  /**
   * 提取关键发现
   */
  extractKeyFindings(analysis, riskAssessment) {
    const findings = [];
    
    if (riskAssessment.criticalFailureModes > 0) {
      findings.push(`发现${riskAssessment.criticalFailureModes}个关键风险失效模式，需要立即处理`);
    }
    
    if (riskAssessment.highRiskFailureModes > 0) {
      findings.push(`发现${riskAssessment.highRiskFailureModes}个高风险失效模式，需要优先关注`);
    }
    
    // 找出风险最高的组件
    const highestRiskComponent = analysis.reduce((max, comp) => 
      comp.overallRisk.totalRPN > max.overallRisk.totalRPN ? comp : max
    );
    findings.push(`${highestRiskComponent.component}组件风险最高，总RPN为${highestRiskComponent.overallRisk.totalRPN}`);
    
    return findings;
  }

  /**
   * 计算统计信息
   */
  calculateStatistics(analysis) {
    const allFailureModes = analysis.flatMap(comp => comp.failureModes);
    
    return {
      totalComponents: analysis.length,
      totalFailureModes: allFailureModes.length,
      averageFailureModesPerComponent: allFailureModes.length / analysis.length,
      rpnStatistics: {
        min: Math.min(...allFailureModes.map(fm => fm.rpn)),
        max: Math.max(...allFailureModes.map(fm => fm.rpn)),
        average: allFailureModes.reduce((sum, fm) => sum + fm.rpn, 0) / allFailureModes.length,
        total: allFailureModes.reduce((sum, fm) => sum + fm.rpn, 0)
      },
      riskDistribution: {
        critical: allFailureModes.filter(fm => fm.riskLevel === 'critical').length,
        high: allFailureModes.filter(fm => fm.riskLevel === 'high').length,
        medium: allFailureModes.filter(fm => fm.riskLevel === 'medium').length,
        low: allFailureModes.filter(fm => fm.riskLevel === 'low').length
      }
    };
  }

  createErrorResult(message) {
    return {
      success: false,
      error: message,
      type: 'fmea_error'
    };
  }
}

module.exports = { EnhancedFMEAAnalyzer, MOBILE_FMEA_KNOWLEDGE };
