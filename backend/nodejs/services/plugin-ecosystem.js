/**
 * 插件生态系统管理器
 * 集成LangChain Tools、Semantic Kernel、Haystack等开源插件框架
 */

const { v4: uuidv4 } = require('uuid');
const fs = require('fs').promises;
const path = require('path');

class PluginEcosystemManager {
  constructor(aiModelCaller, dbAdapter, smartModelSelector) {
    this.aiModelCaller = aiModelCaller;
    this.dbAdapter = dbAdapter;
    this.smartModelSelector = smartModelSelector;

    // 插件注册表
    this.pluginRegistry = new Map();
    this.activePlugins = new Map();

    // 重构后的插件类型定义
    this.pluginTypes = {
      'document_processor': {
        name: '文档处理器',
        description: '处理各种格式的文档和数据文件',
        framework: 'unified',
        interface: 'processor',
        execution_method: 'executeDocumentProcessor',
        result_type: 'document_result'
      },
      'data_analyzer': {
        name: '数据分析器',
        description: '执行数据分析、统计和可视化',
        framework: 'unified',
        interface: 'analyzer',
        execution_method: 'executeDataAnalyzer',
        result_type: 'analysis_result'
      },
      'quality_tool': {
        name: '质量工具',
        description: '质量管理专用分析工具',
        framework: 'unified',
        interface: 'tool',
        execution_method: 'executeQualityTool',
        result_type: 'quality_result'
      },
      'external_connector': {
        name: '外部连接器',
        description: '连接外部系统和服务',
        framework: 'unified',
        interface: 'connector',
        execution_method: 'executeExternalConnector',
        result_type: 'connector_result'
      },
      'ai_processor': {
        name: 'AI处理器',
        description: '基于AI的智能处理工具',
        framework: 'unified',
        interface: 'ai',
        execution_method: 'executeAIProcessor',
        result_type: 'ai_result'
      }
    };

    // 预置插件库
    this.builtinPlugins = {
      // 文档处理插件 - 统一使用 document_processor 类型
      'pdf_parser': {
        id: 'pdf_parser',
        name: 'PDF解析器',
        description: '解析PDF文档并提取文本内容',
        type: 'document_processor',
        category: 'document_processing',
        capabilities: ['pdf_parsing', 'text_extraction', 'metadata_extraction'],
        config_schema: {
          type: 'object',
          properties: {
            extract_images: { type: 'boolean', default: false },
            extract_tables: { type: 'boolean', default: true },
            language: { type: 'string', default: 'auto' }
          }
        }
      },
      'excel_analyzer': {
        id: 'excel_analyzer',
        name: 'Excel分析器',
        description: '分析Excel文件中的数据和图表',
        type: 'document_processor',
        category: 'document_processing',
        capabilities: ['excel_parsing', 'data_analysis', 'chart_extraction'],
        config_schema: {
          type: 'object',
          properties: {
            sheet_names: { type: 'array', items: { type: 'string' } },
            include_formulas: { type: 'boolean', default: true },
            max_rows: { type: 'number', default: 10000 }
          }
        }
      },
      'csv_parser': {
        id: 'csv_parser',
        name: 'CSV解析器',
        description: '解析CSV文本，输出表格与统计摘要',
        type: 'document_processor',
        category: 'document_processing',
        capabilities: ['csv_parsing', 'table_preview', 'summary'],
        config_schema: {
          type: 'object',
          properties: {
            delimiter: { type: 'string', default: ',' },
            encoding: { type: 'string', default: 'utf-8' },
            has_header: { type: 'boolean', default: true }
          }
        }
      },
      'xlsx_parser': {
        id: 'xlsx_parser',
        name: 'XLSX解析器',
        description: '解析XLSX/Excel文件，输出表格与统计摘要',
        type: 'document_processor',
        category: 'document_processing',
        capabilities: ['xlsx_parsing', 'table_preview', 'summary'],
        config_schema: {
          type: 'object',
          properties: {
            sheet_names: { type: 'array', items: { type: 'string' } },
            max_rows: { type: 'number', default: 10000 }
          }
        }
      },
      'json_parser': {
        id: 'json_parser',
        name: 'JSON解析器',
        description: '解析JSON文本或Base64，结构化输出',
        type: 'document_processor',
        category: 'document_processing',
        capabilities: ['json_parsing', 'structure_inspect'],
        config_schema: {
          type: 'object',
          properties: {
            validate_schema: { type: 'boolean', default: false },
            max_depth: { type: 'number', default: 10 }
          }
        }
      },
      'xml_parser': {
        id: 'xml_parser',
        name: 'XML解析器',
        description: '解析XML文本或Base64，结构化输出',
        type: 'document_processor',
        category: 'document_processing',
        capabilities: ['xml_parsing', 'structure_inspect'],
        config_schema: {
          type: 'object',
          properties: {
            validate_dtd: { type: 'boolean', default: false },
            preserve_whitespace: { type: 'boolean', default: false }
          }
        }
      },
      'docx_parser': {
        id: 'docx_parser',
        name: 'DOCX解析器',
        description: '解析DOCX文档，提取正文文本',
        type: 'document_processor',
        category: 'document_processing',
        capabilities: ['docx_text', 'style_extraction'],
        config_schema: {
          type: 'object',
          properties: {
            extract_styles: { type: 'boolean', default: false },
            extract_images: { type: 'boolean', default: false }
          }
        }
      }
,

      // AI处理插件 - 图像分析和智能识别
      'defect_detector': {
        id: 'defect_detector',
        name: '缺陷检测器',
        description: '基于AI的产品缺陷自动检测',
        type: 'ai_processor',
        category: 'image_analysis',
        capabilities: ['defect_detection', 'image_classification', 'quality_assessment'],
        config_schema: {
          type: 'object',
          properties: {
            confidence_threshold: { type: 'number', default: 0.8, minimum: 0.1, maximum: 1.0 },
            defect_types: { type: 'array', items: { type: 'string' } },
            image_preprocessing: { type: 'boolean', default: true },
            model_type: { type: 'string', enum: ['yolo', 'rcnn', 'ssd'], default: 'yolo' }
          }
        }
      },
      'ocr_reader': {
        id: 'ocr_reader',
        name: 'OCR文字识别',
        description: '从图像中识别和提取文字',
        type: 'ai_processor',
        category: 'image_analysis',
        capabilities: ['ocr', 'text_recognition', 'multilingual'],
        config_schema: {
          type: 'object',
          properties: {
            languages: { type: 'array', items: { type: 'string' }, default: ['zh', 'en'] },
            confidence_threshold: { type: 'number', default: 0.7 },
            output_format: { type: 'string', enum: ['text', 'json', 'structured'], default: 'text' },
            engine: { type: 'string', enum: ['tesseract', 'paddleocr', 'easyocr'], default: 'tesseract' }
          }
        }
      },
      'image_parser': {
        id: 'image_parser',
        name: '图像解析器',
        description: '解析图像文件并提取元数据和内容',
        type: 'document_processor',
        category: 'document_processing',
        capabilities: ['image_parsing', 'metadata_extraction', 'format_detection'],
        config_schema: {
          type: 'object',
          properties: {
            extract_metadata: { type: 'boolean', default: true },
            auto_ocr: { type: 'boolean', default: true },
            supported_formats: { type: 'array', items: { type: 'string' }, default: ['png', 'jpg', 'jpeg', 'bmp', 'gif'] }
          }
        }
      },
      'text_parser': {
        id: 'text_parser',
        name: '文本解析器',
        description: '解析纯文本文件并进行编码检测和内容分析',
        type: 'document_processor',
        category: 'document_processing',
        capabilities: ['text_parsing', 'encoding_detection', 'content_analysis'],
        config_schema: {
          type: 'object',
          properties: {
            detect_encoding: { type: 'boolean', default: true },
            analyze_structure: { type: 'boolean', default: true },
            extract_keywords: { type: 'boolean', default: false }
          }
        }
      },

      // 数据分析插件 - 统一使用 data_analyzer 类型
      'statistical_analyzer': {
        id: 'statistical_analyzer',
        name: '统计分析器',
        description: '执行各种统计分析和计算',
        type: 'data_analyzer',
        category: 'data_analysis',
        capabilities: ['statistics', 'trend_analysis', 'correlation', 'distribution_analysis'],
        config_schema: {
          type: 'object',
          properties: {
            analysis_types: { type: 'array', items: { type: 'string' } },
            confidence_level: { type: 'number', default: 0.95 },
            include_visualization: { type: 'boolean', default: true },
            outlier_detection: { type: 'boolean', default: true }
          }
        }
      },
      'spc_controller': {
        id: 'spc_controller',
        name: 'SPC统计过程控制',
        description: '统计过程控制分析和监控',
        type: 'data_analyzer',
        category: 'quality_control',
        capabilities: ['spc_analysis', 'control_charts', 'process_monitoring'],
        config_schema: {
          type: 'object',
          properties: {
            chart_types: { type: 'array', items: { type: 'string' }, default: ['xbar_r', 'p_chart'] },
            control_limits: { type: 'number', default: 3 },
            sample_size: { type: 'number', default: 5 },
            subgroup_size: { type: 'number', default: 1 }
          }
        }
      },
      'data_cleaner': {
        id: 'data_cleaner',
        name: '数据清洗器',
        description: '去除缺失/重复、类型转换、简单归一化',
        type: 'data_analyzer',
        category: 'data_processing',
        capabilities: ['missing_removal', 'deduplicate', 'normalize', 'type_conversion'],
        config_schema: {
          type: 'object',
          properties: {
            dedupe_key: { type: 'string' },
            normalize_field: { type: 'string' },
            fill_missing: { type: 'string', enum: ['mean', 'median', 'mode', 'drop'], default: 'drop' }
          }
        }
      },
      'anomaly_detector': {
        id: 'anomaly_detector',
        name: '异常检测器',
        description: '基于IQR/3σ的简单异常检测',
        type: 'data_analyzer',
        category: 'data_analysis',
        capabilities: ['iqr', 'sigma3', 'isolation_forest'],
        config_schema: {
          type: 'object',
          properties: {
            method: { type: 'string', enum: ['iqr', 'sigma3', 'isolation_forest'], default: 'iqr' },
            field: { type: 'string' },
            contamination: { type: 'number', default: 0.1, minimum: 0.01, maximum: 0.5 }
          }
        }
      },
      'text_summarizer': {
        id: 'text_summarizer',
        name: '文本摘要器',
        description: '基于频次/规则的离线摘要',
        type: 'ai_processor',
        category: 'nlp',
        capabilities: ['extractive_summary', 'keyword_extraction'],
        config_schema: {
          type: 'object',
          properties: {
            max_sentences: { type: 'number', default: 3 },
            algorithm: { type: 'string', enum: ['frequency', 'textrank', 'lsa'], default: 'frequency' }
          }
        }
      },

      // 外部集成插件 - 统一使用 external_connector 类型
      'api_connector': {
        id: 'api_connector',
        name: 'API连接器',
        description: '连接外部API服务',
        type: 'external_connector',
        category: 'external_integration',
        capabilities: ['api_calls', 'data_fetching', 'webhook', 'rest_api'],
        config_schema: {
          type: 'object',
          properties: {
            base_url: { type: 'string' },
            auth_type: { type: 'string', enum: ['none', 'api_key', 'oauth', 'basic'], default: 'none' },
            timeout: { type: 'number', default: 30 },
            retry_count: { type: 'number', default: 3 },
            headers: { type: 'object', default: {} }
          }
        }
      },
      'database_query': {
        id: 'database_query',
        name: '数据库查询器',
        description: '查询各种数据库系统',
        type: 'external_connector',
        category: 'external_integration',
        capabilities: ['sql_query', 'data_retrieval', 'database_connection'],
        config_schema: {
          type: 'object',
          properties: {
            db_type: { type: 'string', enum: ['mysql', 'postgresql', 'sqlite', 'mongodb'] },
            connection_string: { type: 'string' },
            query_timeout: { type: 'number', default: 60 },
            max_rows: { type: 'number', default: 1000 },
            pool_size: { type: 'number', default: 5 }
          }
        }
      },

      // 质量管理专用插件 - 统一使用 quality_tool 类型
      'fmea_analyzer': {
        id: 'fmea_analyzer',
        name: 'FMEA失效模式分析',
        description: '失效模式与影响分析工具',
        type: 'quality_tool',
        category: 'quality_tools',
        capabilities: ['fmea_analysis', 'risk_assessment', 'failure_prediction', 'rpn_calculation'],
        config_schema: {
          type: 'object',
          properties: {
            severity_scale: { type: 'number', default: 10 },
            occurrence_scale: { type: 'number', default: 10 },
            detection_scale: { type: 'number', default: 10 },
            rpn_threshold: { type: 'number', default: 100 },
            analysis_type: { type: 'string', enum: ['design', 'process', 'system'], default: 'process' }
          }
        }
      },
      'msa_calculator': {
        id: 'msa_calculator',
        name: 'MSA测量系统分析',
        description: '测量系统分析和计算工具',
        type: 'quality_tool',
        category: 'quality_tools',
        capabilities: ['msa_analysis', 'gage_rr', 'measurement_uncertainty', 'bias_linearity'],
        config_schema: {
          type: 'object',
          properties: {
            operators: { type: 'number', default: 3 },
            parts: { type: 'number', default: 10 },
            trials: { type: 'number', default: 2 },
            tolerance: { type: 'number' },
            study_type: { type: 'string', enum: ['gage_rr', 'bias_linearity', 'stability'], default: 'gage_rr' }
          }
        }
      }
    };

    // 初始化插件注册表
    this.initializeBuiltinPlugins();
  }

  /**
   * 初始化内置插件
   */
  async initializeBuiltinPlugins() {
    console.log('🔌 初始化内置插件库...');

    try {
      for (const [pluginId, pluginConfig] of Object.entries(this.builtinPlugins)) {
        await this.registerPlugin({
          ...pluginConfig,
          version: '1.0.0',
          author: 'QMS Team',
          status: 'active',
          builtin: true,
          created_at: new Date().toISOString()
        });
      }

      console.log(`✅ 内置插件初始化完成: ${Object.keys(this.builtinPlugins).length}个插件`);
    } catch (error) {
      console.error('❌ 内置插件初始化失败:', error);
    }
  }

  /**
   * 注册插件
   */
  async registerPlugin(pluginConfig) {
    const pluginId = pluginConfig.id || uuidv4();

    try {
      // 验证插件配置
      this.validatePluginConfig(pluginConfig);

      // 创建插件实例
      const plugin = {
        id: pluginId,
        name: pluginConfig.name,
        description: pluginConfig.description,
        type: pluginConfig.type,
        category: pluginConfig.category,
        version: pluginConfig.version || '1.0.0',
        author: pluginConfig.author || 'Unknown',
        capabilities: pluginConfig.capabilities || [],
        config_schema: pluginConfig.config_schema || {},
        status: pluginConfig.status || 'inactive',
        builtin: pluginConfig.builtin || false,
        created_at: pluginConfig.created_at || new Date().toISOString(),
        updated_at: new Date().toISOString(),
        usage_count: 0,
        last_used: null
      };

      // 注册到内存
      this.pluginRegistry.set(pluginId, plugin);

      // 保存到数据库
      await this.savePluginToDatabase(plugin);

      console.log(`✅ 插件注册成功: ${plugin.name} (${pluginId})`);
      return plugin;

    } catch (error) {
      console.error(`❌ 插件注册失败: ${pluginConfig.name}`, error);
      throw error;
    }
  }

  /**
   * 安装插件
   */
  async installPlugin(pluginId, config = {}) {
    console.log(`📦 安装插件: ${pluginId}`);

    try {
      const plugin = this.pluginRegistry.get(pluginId);
      if (!plugin) {
        throw new Error(`插件不存在: ${pluginId}`);
      }

      // 验证配置
      const validatedConfig = this.validatePluginConfig(config, plugin.config_schema);

      // 根据新的插件类型执行不同的安装逻辑
      let installResult;
      switch (plugin.type) {
        case 'document_processor':
          installResult = await this.installDocumentProcessor(plugin, validatedConfig);
          break;
        case 'data_analyzer':
          installResult = await this.installDataAnalyzer(plugin, validatedConfig);
          break;
        case 'quality_tool':
          installResult = await this.installQualityTool(plugin, validatedConfig);
          break;
        case 'external_connector':
          installResult = await this.installExternalConnector(plugin, validatedConfig);
          break;
        case 'ai_processor':
          installResult = await this.installAIProcessor(plugin, validatedConfig);
          break;
        // 兼容旧类型（逐步迁移）
        case 'langchain_tool':
          installResult = await this.installLangChainTool(plugin, validatedConfig);
          break;
        case 'semantic_kernel':
          installResult = await this.installSemanticKernelPlugin(plugin, validatedConfig);
          break;
        case 'haystack_component':
          installResult = await this.installHaystackComponent(plugin, validatedConfig);
          break;
        case 'custom_plugin':
          installResult = await this.installCustomPlugin(plugin, validatedConfig);
          break;
        default:
          throw new Error(`不支持的插件类型: ${plugin.type}`);
      }

      // 更新插件状态
      plugin.status = 'active';
      plugin.config = validatedConfig;
      plugin.install_result = installResult;
      plugin.updated_at = new Date().toISOString();

      // 添加到活跃插件
      this.activePlugins.set(pluginId, plugin);

      // 更新数据库
      await this.updatePluginInDatabase(plugin);

      console.log(`✅ 插件安装成功: ${plugin.name}`);
      return {
        plugin_id: pluginId,
        status: 'installed',
        config: validatedConfig,
        install_result: installResult
      };

    } catch (error) {
      console.error(`❌ 插件安装失败: ${pluginId}`, error);
      throw error;
    }
  }

  /**
   * 执行插件
   */
  async executePlugin(pluginId, input, options = {}) {
    console.log(`🔄 执行插件: ${pluginId}`);
    console.log('📥 插件输入数据:', JSON.stringify(input, null, 2));
    console.log('⚙️ 插件选项:', JSON.stringify(options, null, 2));

    try {
      let plugin = this.activePlugins.get(pluginId);
      if (!plugin) {
        // 若未激活但在注册表中存在，则尝试自动安装
        const registered = this.pluginRegistry.get(pluginId);
        if (registered) {
          console.log(`ℹ️ 插件未激活，尝试自动安装: ${pluginId}`);
          try { await this.installPlugin(pluginId, registered.config || {}); } catch (e) { console.warn(`自动安装失败: ${pluginId}: ${e.message}`); }
          plugin = this.activePlugins.get(pluginId);
        }
      }
      if (!plugin) {
        throw new Error(`插件未安装或未激活: ${pluginId}`);
      }

      // 记录使用统计
      plugin.usage_count++;
      plugin.last_used = new Date().toISOString();

      // 根据新的插件类型执行不同的逻辑
      let result;
      switch (plugin.type) {
        case 'document_processor':
          result = await this.executeDocumentProcessor(plugin, input, options);
          break;
        case 'data_analyzer':
          result = await this.executeDataAnalyzer(plugin, input, options);
          break;
        case 'quality_tool':
          result = await this.executeQualityTool(plugin, input, options);
          break;
        case 'external_connector':
          result = await this.executeExternalConnector(plugin, input, options);
          break;
        case 'ai_processor':
          result = await this.executeAIProcessor(plugin, input, options);
          break;
        // 兼容旧类型（逐步迁移）
        case 'langchain_tool':
          result = await this.executeLangChainTool(plugin, input, options);
          break;
        case 'semantic_kernel':
          result = await this.executeSemanticKernelPlugin(plugin, input, options);
          break;
        case 'haystack_component':
          result = await this.executeHaystackComponent(plugin, input, options);
          break;
        case 'custom_plugin':
          result = await this.executeCustomPlugin(plugin, input, options);
          break;
        default:
          throw new Error(`不支持的插件类型: ${plugin.type}`);
      }

      // 更新统计信息
      await this.updatePluginStats(plugin);

      console.log(`✅ 插件执行完成: ${plugin.name}`);
      return {
        plugin_id: pluginId,
        plugin_name: plugin.name,
        execution_time: new Date().toISOString(),
        result: result,
        success: true
      };

    } catch (error) {
      console.error(`❌ 插件执行失败: ${pluginId}`, error);
      return {
        plugin_id: pluginId,
        execution_time: new Date().toISOString(),
        error: error.message,
        success: false
      };
    }
  }

  /**
   * 安装文档处理器
   */
  async installDocumentProcessor(plugin, config) {
    console.log(`📄 安装文档处理器: ${plugin.name}`);

    // 统一的文档处理器安装
    const processorConfig = {
      plugin_id: plugin.id,
      plugin_name: plugin.name,
      capabilities: plugin.capabilities,
      config: config,
      supported_formats: this.getSupportedFormats(plugin.id)
    };

    return {
      type: 'document_processor',
      processor_config: processorConfig,
      installed_at: new Date().toISOString()
    };
  }

  /**
   * 安装数据分析器
   */
  async installDataAnalyzer(plugin, config) {
    console.log(`📊 安装数据分析器: ${plugin.name}`);

    const analyzerConfig = {
      plugin_id: plugin.id,
      plugin_name: plugin.name,
      capabilities: plugin.capabilities,
      config: config,
      analysis_types: this.getAnalysisTypes(plugin.id)
    };

    return {
      type: 'data_analyzer',
      analyzer_config: analyzerConfig,
      installed_at: new Date().toISOString()
    };
  }

  /**
   * 安装质量工具
   */
  async installQualityTool(plugin, config) {
    console.log(`🔧 安装质量工具: ${plugin.name}`);

    const toolConfig = {
      plugin_id: plugin.id,
      plugin_name: plugin.name,
      capabilities: plugin.capabilities,
      config: config,
      quality_standards: this.getQualityStandards(plugin.id)
    };

    return {
      type: 'quality_tool',
      tool_config: toolConfig,
      installed_at: new Date().toISOString()
    };
  }

  /**
   * 安装外部连接器
   */
  async installExternalConnector(plugin, config) {
    console.log(`🔗 安装外部连接器: ${plugin.name}`);

    const connectorConfig = {
      plugin_id: plugin.id,
      plugin_name: plugin.name,
      capabilities: plugin.capabilities,
      config: config,
      connection_types: this.getConnectionTypes(plugin.id)
    };

    return {
      type: 'external_connector',
      connector_config: connectorConfig,
      installed_at: new Date().toISOString()
    };
  }

  /**
   * 安装AI处理器
   */
  async installAIProcessor(plugin, config) {
    console.log(`🤖 安装AI处理器: ${plugin.name}`);

    const processorConfig = {
      plugin_id: plugin.id,
      plugin_name: plugin.name,
      capabilities: plugin.capabilities,
      config: config,
      ai_models: this.getAIModels(plugin.id)
    };

    return {
      type: 'ai_processor',
      processor_config: processorConfig,
      installed_at: new Date().toISOString()
    };
  }

  // 辅助方法
  getSupportedFormats(pluginId) {
    const formatMap = {
      'pdf_parser': ['pdf'],
      'excel_analyzer': ['xlsx', 'xls'],
      'csv_parser': ['csv'],
      'xlsx_parser': ['xlsx', 'xls'],
      'json_parser': ['json'],
      'xml_parser': ['xml'],
      'docx_parser': ['docx', 'doc']
    };
    return formatMap[pluginId] || [];
  }

  getAnalysisTypes(pluginId) {
    const typeMap = {
      'statistical_analyzer': ['descriptive', 'correlation', 'regression'],
      'spc_controller': ['control_charts', 'process_capability'],
      'data_cleaner': ['missing_data', 'duplicates', 'outliers'],
      'anomaly_detector': ['statistical', 'isolation_forest', 'clustering']
    };
    return typeMap[pluginId] || [];
  }

  getQualityStandards(pluginId) {
    const standardMap = {
      'fmea_analyzer': ['ISO14971', 'AIAG-VDA', 'IEC60812'],
      'msa_calculator': ['AIAG-MSA', 'ISO5725', 'VDA5']
    };
    return standardMap[pluginId] || [];
  }

  getConnectionTypes(pluginId) {
    const connectionMap = {
      'api_connector': ['REST', 'GraphQL', 'SOAP'],
      'database_query': ['SQL', 'NoSQL', 'Graph']
    };
    return connectionMap[pluginId] || [];
  }

  getAIModels(pluginId) {
    const modelMap = {
      'defect_detector': ['YOLO', 'R-CNN', 'SSD'],
      'ocr_reader': ['Tesseract', 'PaddleOCR', 'EasyOCR'],
      'text_summarizer': ['TextRank', 'LSA', 'Frequency']
    };
    return modelMap[pluginId] || [];
  }

  /**
   * 安装LangChain工具 - 兼容旧版本
   */
  async installLangChainTool(plugin, config) {
    console.log(`🔧 安装LangChain工具: ${plugin.name}`);

    // 模拟LangChain工具安装
    const toolConfig = {
      name: plugin.id,
      description: plugin.description,
      func: this.createLangChainToolFunction(plugin, config),
      args_schema: this.generateArgsSchema(plugin.config_schema)
    };

    return {
      type: 'langchain_tool',
      tool_config: toolConfig,
      installed_at: new Date().toISOString()
    };
  }

  /**
   * 安装Semantic Kernel插件
   */
  async installSemanticKernelPlugin(plugin, config) {
    console.log(`🧠 安装Semantic Kernel插件: ${plugin.name}`);

    // 模拟Semantic Kernel插件安装
    const skillConfig = {
      skill_name: plugin.id,
      description: plugin.description,
      functions: this.createSemanticKernelFunctions(plugin, config)
    };

    return {
      type: 'semantic_kernel',
      skill_config: skillConfig,
      installed_at: new Date().toISOString()
    };
  }

  /**
   * 安装Haystack组件
   */
  async installHaystackComponent(plugin, config) {
    console.log(`📄 安装Haystack组件: ${plugin.name}`);

    // 模拟Haystack组件安装
    const componentConfig = {
      component_name: plugin.id,
      component_type: plugin.category,
      pipeline_config: this.createHaystackPipeline(plugin, config)
    };

    return {
      type: 'haystack_component',
      component_config: componentConfig,
      installed_at: new Date().toISOString()
    };
  }

  /**
   * 安装自定义插件
   */
  async installCustomPlugin(plugin, config) {
    console.log(`⚙️ 安装自定义插件: ${plugin.name}`);

    // 模拟自定义插件安装
    const customConfig = {
      plugin_name: plugin.id,
      api_endpoint: `/plugins/${plugin.id}`,
      config: config,
      capabilities: plugin.capabilities
    };

    return {
      type: 'custom_plugin',
      custom_config: customConfig,
      installed_at: new Date().toISOString()
    };
  }

  /**
   * 执行LangChain工具
   */
  async executeLangChainTool(plugin, input, options) {
    console.log(`🔧 执行LangChain工具: ${plugin.name}`);

    // 根据插件ID执行不同的逻辑
    switch (plugin.id) {
      case 'ocr_reader':
        return await this.executeOCRReader(input, plugin.config);
      case 'api_connector':
        return await this.executeAPIConnector(input, plugin.config);
      case 'database_query':
        return await this.executeDatabaseQuery(input, plugin.config);
      default:
        return await this.executeGenericLangChainTool(plugin, input, options);
    }
  }

  /**
   * 执行文档处理器 - 统一的文档处理接口
   */
  async executeDocumentProcessor(plugin, input, options) {
    console.log(`📄 执行文档处理器: ${plugin.name}`);

    try {
      let result;
      switch (plugin.id) {
        case 'pdf_parser':
          result = await this.executePDFParser(input, plugin.config);
          break;
        case 'excel_analyzer':
          result = await this.executeExcelAnalyzer(input, plugin.config);
          break;
        case 'csv_parser':
          result = await this.executeCSVParser(input, plugin.config);
          break;
        case 'xlsx_parser':
          result = await this.executeXLSXParser(input, plugin.config);
          break;
        case 'json_parser':
          result = await this.executeJSONParser(input, plugin.config);
          break;
        case 'xml_parser':
          result = await this.executeXMLParser(input, plugin.config);
          break;
        case 'docx_parser':
          result = await this.executeDOCXParser(input, plugin.config);
          break;
        case 'image_parser':
          result = await this.executeImageParser(input, plugin.config);
          break;
        case 'text_parser':
          result = await this.executeTextParser(input, plugin.config);
          break;
        default:
          throw new Error(`未实现的文档处理器: ${plugin.id}`);
      }

      // 标准化返回格式
      return this.standardizeResult(result, 'document_result', plugin);
    } catch (error) {
      return this.createErrorResult(error, plugin);
    }
  }

  /**
   * 执行数据分析器 - 统一的数据分析接口
   */
  async executeDataAnalyzer(plugin, input, options) {
    console.log(`📊 执行数据分析器: ${plugin.name}`);

    try {
      let result;
      switch (plugin.id) {
        case 'statistical_analyzer':
          result = await this.executeStatisticalAnalyzer(input, plugin.config);
          break;
        case 'spc_controller':
          result = await this.executeSPCController(input, plugin.config);
          break;
        case 'data_cleaner':
          result = await this.executeDataCleaner(input, plugin.config);
          break;
        case 'anomaly_detector':
          result = await this.executeAnomalyDetector(input, plugin.config);
          break;
        default:
          throw new Error(`未实现的数据分析器: ${plugin.id}`);
      }

      return this.standardizeResult(result, 'analysis_result', plugin);
    } catch (error) {
      return this.createErrorResult(error, plugin);
    }
  }

  /**
   * 执行质量工具 - 统一的质量管理接口
   */
  async executeQualityTool(plugin, input, options) {
    console.log(`🔧 执行质量工具: ${plugin.name}`);

    try {
      let result;
      switch (plugin.id) {
        case 'fmea_analyzer':
          result = await this.executeFMEAAnalyzer(input, plugin.config);
          break;
        case 'msa_calculator':
          result = await this.executeMSACalculator(input, plugin.config);
          break;
        default:
          throw new Error(`未实现的质量工具: ${plugin.id}`);
      }

      return this.standardizeResult(result, 'quality_result', plugin);
    } catch (error) {
      return this.createErrorResult(error, plugin);
    }
  }

  /**
   * 执行外部连接器 - 统一的外部集成接口
   */
  async executeExternalConnector(plugin, input, options) {
    console.log(`🔗 执行外部连接器: ${plugin.name}`);

    try {
      let result;
      switch (plugin.id) {
        case 'api_connector':
          result = await this.executeAPIConnector(input, plugin.config);
          break;
        case 'database_query':
          result = await this.executeDatabaseQuery(input, plugin.config);
          break;
        default:
          throw new Error(`未实现的外部连接器: ${plugin.id}`);
      }

      return this.standardizeResult(result, 'connector_result', plugin);
    } catch (error) {
      return this.createErrorResult(error, plugin);
    }
  }

  /**
   * 执行AI处理器 - 统一的AI处理接口
   */
  async executeAIProcessor(plugin, input, options) {
    console.log(`🤖 执行AI处理器: ${plugin.name}`);

    try {
      let result;
      switch (plugin.id) {
        case 'defect_detector':
          result = await this.executeDefectDetector(input, plugin.config);
          break;
        case 'ocr_reader':
          result = await this.executeOCRReader(input, plugin.config);
          break;
        case 'text_summarizer':
          result = await this.executeTextSummarizer(input, plugin.config);
          break;
        default:
          throw new Error(`未实现的AI处理器: ${plugin.id}`);
      }

      return this.standardizeResult(result, 'ai_result', plugin);
    } catch (error) {
      return this.createErrorResult(error, plugin);
    }
  }

  /**
   * 标准化结果格式
   */
  standardizeResult(result, resultType, plugin) {
    // 文档处理器：如果结果有preview、columns、text等前端直接需要的字段，直接展开
    if ((result.preview || result.columns || result.text) && plugin.type === 'document_processor') {
      return {
        success: true,
        type: resultType,
        ...result,  // 直接展开结果，不额外包装
        metadata: {
          plugin_id: plugin.id,
          plugin_name: plugin.name,
          plugin_type: plugin.type,
          execution_time: new Date().toISOString(),
          ...result.metadata
        },
        statistics: result.statistics || {},
        warnings: result.warnings || [],
        plugin_info: {
          version: plugin.version,
          capabilities: plugin.capabilities
        }
      };
    }

    // 其他插件：使用原来的逻辑，保持数据在data字段中
    return {
      success: true,
      type: resultType,
      data: result.data || result,
      metadata: {
        plugin_id: plugin.id,
        plugin_name: plugin.name,
        plugin_type: plugin.type,
        execution_time: new Date().toISOString(),
        ...result.metadata
      },
      statistics: result.statistics || {},
      warnings: result.warnings || [],
      plugin_info: {
        version: plugin.version,
        capabilities: plugin.capabilities
      }
    };
  }

  /**
   * 创建错误结果
   */
  createErrorResult(error, plugin) {
    return {
      success: false,
      type: 'error_result',
      error: error.message,
      metadata: {
        plugin_id: plugin.id,
        plugin_name: plugin.name,
        plugin_type: plugin.type,
        execution_time: new Date().toISOString()
      },
      warnings: [`插件执行失败: ${error.message}`]
    };
  }

  /**
   * 执行Haystack组件 - 兼容旧版本
   */
  async executeHaystackComponent(plugin, input, options) {
    console.log(`🏗️ 执行Haystack组件: ${plugin.name}`);

    // 根据插件ID执行不同的逻辑
    switch (plugin.id) {
      case 'pdf_parser':
        return await this.executePDFParser(input, plugin.config);
      default:
        return await this.executeGenericHaystackComponent(plugin, input, options);
    }
  }

  /**
   * 执行通用Haystack组件
   */
  async executeGenericHaystackComponent(plugin, input, options) {
    console.log(`🔧 执行通用Haystack组件: ${plugin.name}`);

    // 模拟Haystack组件执行
    return {
      success: true,
      type: 'haystack_result',
      component_id: plugin.id,
      result: `Haystack组件 ${plugin.name} 执行结果`,
      input: input,
      execution_time: new Date().toISOString()
    };
  }

  /**
   * 执行自定义插件
   */
  async executeCustomPlugin(plugin, input, options) {
    console.log(`⚙️ 执行自定义插件: ${plugin.name}`);

    // 根据插件ID执行不同的逻辑
    switch (plugin.id) {
      case 'excel_analyzer':
        return await this.executeExcelAnalyzer(input, plugin.config);
      case 'xlsx_parser':
        return await this.executeXLSXParser(input, plugin.config);
      case 'csv_parser':
        return await this.executeCSVParser(input, plugin.config);
      case 'json_parser':
        return await this.executeJSONParser(input, plugin.config);
      case 'xml_parser':
        return await this.executeXMLParser(input, plugin.config);
      case 'docx_parser':
        return await this.executeDOCXParser(input, plugin.config);
      case 'ocr_reader':
        return await this.executeOCRReader(input, plugin.config);
      case 'defect_detector':
        return await this.executeDefectDetector(input, plugin.config);
      case 'statistical_analyzer':
        return await this.executeStatisticalAnalyzer(input, plugin.config);
      case 'spc_controller':
        return await this.executeSPCController(input, plugin.config);
      case 'api_connector':
        return await this.executeAPIConnector(input, plugin.config);
      case 'database_query':
        return await this.executeDatabaseQuery(input, plugin.config);
      case 'fmea_analyzer':
        return await this.executeFMEAAnalyzer(input, plugin.config);
      case 'msa_calculator':
        return await this.executeMSACalculator(input, plugin.config);
      case 'data_cleaner':
        return await this.executeDataCleaner(input, plugin.config);
      case 'anomaly_detector':
        return await this.executeAnomalyDetector(input, plugin.config);
      case 'text_summarizer':
        return await this.executeTextSummarizer(input, plugin.config);
      default:
        return await this.executeGenericCustomPlugin(plugin, input, options);
    }
  }

  // 具体插件执行方法（示例实现）
  async executeOCRReader(input, config) {
    const startTime = Date.now();

    try {
      // 检查是否有真实的OCR库可用
      let hasRealOCR = false;
      try {
        require('tesseract.js');
        hasRealOCR = true;
      } catch (e) {
        console.log('📝 Tesseract.js未安装，使用增强模拟模式');
      }

      if (hasRealOCR) {
        return await this.executeRealOCR(input, config, startTime);
      } else {
        return await this.executeEnhancedOCRSimulation(input, config, startTime);
      }
    } catch (error) {
      return {
        success: false,
        type: 'ocr_error',
        error: error.message,
        processing_time: Date.now() - startTime,
        warnings: ['OCR处理失败，请检查输入图像格式']
      };
    }
  }

  async executeRealOCR(input, config, startTime) {
    const Tesseract = require('tesseract.js');

    const imagePath = input.image_path || input.image_url || input.image;
    const languages = config.languages || ['chi_sim', 'eng'];
    const confidence_threshold = config.confidence_threshold || 0.7;

    console.log(`🔍 开始真实OCR识别: ${imagePath}`);

    const { data } = await Tesseract.recognize(imagePath, languages.join('+'), {
      logger: m => {
        if (m.status === 'recognizing text') {
          console.log(`OCR进度: ${Math.round(m.progress * 100)}%`);
        }
      }
    });

    // 过滤低置信度文字
    const filteredWords = data.words.filter(w => w.confidence >= confidence_threshold * 100);

    return {
      success: true,
      type: 'ocr_result',
      text: data.text,
      confidence: data.confidence / 100,
      words: filteredWords.map(w => ({
        text: w.text,
        confidence: w.confidence / 100,
        bbox: {
          x0: w.bbox.x0, y0: w.bbox.y0,
          x1: w.bbox.x1, y1: w.bbox.y1
        }
      })),
      statistics: {
        total_words: data.words.length,
        high_confidence_words: filteredWords.length,
        average_confidence: data.confidence / 100,
        processing_time: Date.now() - startTime
      },
      metadata: {
        languages: languages,
        engine: 'tesseract.js',
        image_info: {
          width: data.width,
          height: data.height
        }
      }
    };
  }

  async executeEnhancedOCRSimulation(input, config, startTime) {
    const imagePath = input.image_path || input.image_url || input.image;
    const languages = config.languages || ['chi_sim', 'eng'];

    // 基于文件名和配置生成更真实的模拟结果
    const simulatedTexts = {
      'invoice': '发票号码: INV-2025-001\n开票日期: 2025-01-15\n金额: ¥1,250.00\n税率: 13%\n备注: 质量管理系统服务费',
      'report': '质量检测报告\n\n产品名称: 精密零件A-001\n检测日期: 2025-01-15\n检测结果: 合格\n\n主要指标:\n- 尺寸精度: ±0.01mm\n- 表面粗糙度: Ra 0.8\n- 硬度: HRC 45-50',
      'certificate': '质量认证证书\n\nISO 9001:2015\n质量管理体系认证\n\n证书编号: QMS-2025-001\n有效期至: 2028-01-15\n认证机构: 中国质量认证中心',
      'default': '这是OCR识别的示例文本内容。\n包含中文和English混合文本。\n数字识别：12345.67\n特殊符号：@#$%^&*()'
    };

    // 根据图像路径推断内容类型
    let contentType = 'default';
    if (imagePath) {
      const path = imagePath.toLowerCase();
      if (path.includes('invoice') || path.includes('发票')) contentType = 'invoice';
      else if (path.includes('report') || path.includes('报告')) contentType = 'report';
      else if (path.includes('certificate') || path.includes('证书')) contentType = 'certificate';
    }

    const simulatedText = simulatedTexts[contentType];
    const words = simulatedText.split(/\s+/);

    // 生成模拟的词级别识别结果
    const simulatedWords = words.map((word, index) => ({
      text: word,
      confidence: 0.75 + Math.random() * 0.2, // 0.75-0.95之间的置信度
      bbox: {
        x0: 50 + (index % 10) * 80,
        y0: 50 + Math.floor(index / 10) * 30,
        x1: 50 + (index % 10) * 80 + word.length * 8,
        y1: 50 + Math.floor(index / 10) * 30 + 25
      }
    }));

    const averageConfidence = simulatedWords.reduce((sum, w) => sum + w.confidence, 0) / simulatedWords.length;

    return {
      success: true,
      type: 'ocr_result',
      text: simulatedText,
      confidence: averageConfidence,
      words: simulatedWords,
      statistics: {
        total_words: simulatedWords.length,
        high_confidence_words: simulatedWords.filter(w => w.confidence > 0.8).length,
        average_confidence: averageConfidence,
        processing_time: Date.now() - startTime
      },
      metadata: {
        languages: languages,
        engine: 'enhanced_simulation',
        content_type: contentType,
        image_info: {
          width: 800,
          height: 600
        }
      },
      warnings: ['当前为增强模拟模式，如需真实OCR功能请安装tesseract.js']
    };
  }

  // 图像解析器实现
  async executeImageParser(input = {}, config = {}) {
    const startTime = Date.now();

    try {
      let imageBuffer = null;
      let imageInfo = {};

      // 处理不同输入类型
      if (input.base64) {
        imageBuffer = Buffer.from(input.base64, 'base64');
      } else if (input.buffer) {
        imageBuffer = input.buffer;
      } else if (input.filePath) {
        const fs = require('fs');
        imageBuffer = fs.readFileSync(input.filePath);
      }

      if (!imageBuffer) {
        throw new Error('需要提供base64、buffer或filePath数据');
      }

      // 检测图像格式
      const format = this.detectImageFormat(imageBuffer);

      // 提取图像元数据
      if (config.extract_metadata) {
        imageInfo = await this.extractImageMetadata(imageBuffer, format);
      }

      let ocrResult = null;

      // 自动OCR识别
      if (config.auto_ocr) {
        try {
          ocrResult = await this.executeOCRReader({
            base64: input.base64,
            buffer: imageBuffer
          }, {
            languages: ['zh', 'en'],
            confidence_threshold: 0.7
          });
        } catch (ocrError) {
          console.warn('OCR识别失败:', ocrError.message);
        }
      }

      return {
        success: true,
        type: 'image_content',
        format: format,
        text: ocrResult?.text || '',
        metadata: {
          ...imageInfo,
          file_size: imageBuffer.length,
          processing_time: Date.now() - startTime
        },
        ocr_result: ocrResult,
        image_analysis: {
          format: format,
          size: imageBuffer.length,
          estimated_dimensions: imageInfo.dimensions || { width: 'unknown', height: 'unknown' }
        },
        warnings: ocrResult?.warnings || []
      };

    } catch (error) {
      return {
        success: false,
        type: 'image_error',
        error: error.message,
        processing_time: Date.now() - startTime,
        warnings: ['图像解析失败，请检查输入格式']
      };
    }
  }

  // 文本解析器实现
  async executeTextParser(input = {}, config = {}) {
    const startTime = Date.now();

    try {
      let textContent = '';
      let encoding = 'utf-8';

      // 处理不同输入类型
      if (input.text) {
        textContent = input.text;
      } else if (input.base64) {
        const buffer = Buffer.from(input.base64, 'base64');

        // 编码检测
        if (config.detect_encoding) {
          encoding = this.detectTextEncoding(buffer);
        }

        textContent = buffer.toString(encoding);
      } else if (input.buffer) {
        if (config.detect_encoding) {
          encoding = this.detectTextEncoding(input.buffer);
        }
        textContent = input.buffer.toString(encoding);
      }

      if (!textContent) {
        throw new Error('无法提取文本内容');
      }

      // 内容分析
      let analysis = {};
      if (config.analyze_structure) {
        analysis = this.analyzeTextStructure(textContent);
      }

      // 关键词提取
      let keywords = [];
      if (config.extract_keywords) {
        keywords = this.extractKeywords(textContent);
      }

      return {
        success: true,
        type: 'text_content',
        text: textContent,
        encoding: encoding,
        metadata: {
          character_count: textContent.length,
          line_count: textContent.split('\n').length,
          word_count: textContent.split(/\s+/).filter(w => w.length > 0).length,
          processing_time: Date.now() - startTime
        },
        analysis: analysis,
        keywords: keywords,
        warnings: []
      };

    } catch (error) {
      return {
        success: false,
        type: 'text_error',
        error: error.message,
        processing_time: Date.now() - startTime,
        warnings: ['文本解析失败，请检查输入格式和编码']
      };
    }
  }

  // 图像格式检测
  detectImageFormat(buffer) {
    const signatures = {
      'png': [0x89, 0x50, 0x4E, 0x47],
      'jpg': [0xFF, 0xD8, 0xFF],
      'jpeg': [0xFF, 0xD8, 0xFF],
      'gif': [0x47, 0x49, 0x46],
      'bmp': [0x42, 0x4D],
      'webp': [0x52, 0x49, 0x46, 0x46]
    };

    for (const [format, signature] of Object.entries(signatures)) {
      if (signature.every((byte, index) => buffer[index] === byte)) {
        return format;
      }
    }

    return 'unknown';
  }

  // 提取图像元数据
  async extractImageMetadata(buffer, format) {
    const metadata = {
      format: format,
      size: buffer.length,
      created_at: new Date().toISOString()
    };

    // 简单的尺寸检测（针对常见格式）
    try {
      if (format === 'png' && buffer.length > 24) {
        const width = buffer.readUInt32BE(16);
        const height = buffer.readUInt32BE(20);
        metadata.dimensions = { width, height };
      } else if ((format === 'jpg' || format === 'jpeg') && buffer.length > 10) {
        // JPEG尺寸检测较复杂，这里提供估算
        metadata.dimensions = { width: 'estimated', height: 'estimated' };
      }
    } catch (e) {
      metadata.dimensions = { width: 'unknown', height: 'unknown' };
    }

    return metadata;
  }

  // 文本编码检测
  detectTextEncoding(buffer) {
    // 简单的编码检测
    const sample = buffer.slice(0, Math.min(1024, buffer.length));

    // 检测BOM
    if (sample.length >= 3 && sample[0] === 0xEF && sample[1] === 0xBB && sample[2] === 0xBF) {
      return 'utf-8';
    }
    if (sample.length >= 2 && sample[0] === 0xFF && sample[1] === 0xFE) {
      return 'utf-16le';
    }
    if (sample.length >= 2 && sample[0] === 0xFE && sample[1] === 0xFF) {
      return 'utf-16be';
    }

    // 检测中文字符（简单启发式）
    const text = sample.toString('utf-8');
    const chineseRegex = /[\u4e00-\u9fff]/;
    if (chineseRegex.test(text)) {
      return 'utf-8';
    }

    return 'utf-8'; // 默认UTF-8
  }

  // 文本结构分析
  analyzeTextStructure(text) {
    const lines = text.split('\n');
    const words = text.split(/\s+/).filter(w => w.length > 0);

    return {
      line_count: lines.length,
      word_count: words.length,
      character_count: text.length,
      paragraph_count: text.split(/\n\s*\n/).length,
      has_headers: /^#+\s/.test(text), // Markdown headers
      has_lists: /^\s*[-*+]\s/.test(text), // Lists
      has_numbers: /\d+/.test(text),
      has_chinese: /[\u4e00-\u9fff]/.test(text),
      has_english: /[a-zA-Z]/.test(text)
    };
  }

  // 关键词提取（简单实现）
  extractKeywords(text, maxKeywords = 10) {
    // 简单的关键词提取
    const words = text.toLowerCase()
      .replace(/[^\w\s\u4e00-\u9fff]/g, ' ')
      .split(/\s+/)
      .filter(w => w.length > 2);

    // 词频统计
    const frequency = {};
    words.forEach(word => {
      frequency[word] = (frequency[word] || 0) + 1;
    });

    // 排序并返回前N个
    return Object.entries(frequency)
      .sort(([,a], [,b]) => b - a)
      .slice(0, maxKeywords)
      .map(([word, count]) => ({ word, count }));
  }

  async executePDFParser(input = {}, config = {}) {
    // 优先使用开源 pdf-parse；无依赖或失败则返回友好提示
    const result = {
      success: true,
      type: 'pdf_content',
      text: '',
      pages: input.pages || null,
      metadata: {
        title: (input.metadata && input.metadata.title) || '未命名文档',
        author: (input.metadata && input.metadata.author) || '未知',
        created_date: (input.metadata && input.metadata.created_date) || new Date().toISOString().slice(0,10)
      },
      warnings: []
    };

    if (typeof input === 'string') {
      result.text = input;
      return result;
    }

    if (input && input.text) {
      result.text = input.text;
      return result;
    }

    // 尝试使用 pdf-parse 从 base64/文件读取文本
    try {
      const pdfParse = require('pdf-parse');
      let buffer = null;
      if (input.base64) buffer = Buffer.from(input.base64, 'base64');
      else if (input.buffer) buffer = Buffer.isBuffer(input.buffer) ? input.buffer : Buffer.from(input.buffer);
      else if (input.filePath) buffer = await require('fs').promises.readFile(input.filePath);

      if (buffer) {
        const data = await pdfParse(buffer);
        result.text = data.text || '';
        result.pages = data.numpages || result.pages;
        if (data.info) {
          result.metadata.title = data.info.Title || result.metadata.title;
          result.metadata.author = data.info.Author || result.metadata.author;
        }
        return result;
      }
    } catch (e) {
      result.warnings.push('pdf-parse 未安装或解析失败: ' + e.message);
    }

    // 未提供可直接使用的文本，返回提示
    result.text = '当前为轻量解析模式：未安装/启用 pdf-parse。请提供 text 或安装依赖以自动提取。';
    result.warnings.push('未启用pdf自动解析');
    return result;
  }

  async executeExcelAnalyzer(input = {}, config = {}) {
    // 实现：优先 filePath 读取 .xlsx；否则兼容 CSV 文本或 dataset 数组
    let rows = [];

    // 1) filePath 读取 .xlsx
    // 0) base64 buffer
    if (input.base64 && !rows.length) {
      try {
        const XLSX = require('xlsx');
        const buf = Buffer.from(input.base64, 'base64');
        const workbook = XLSX.read(buf, {
          type: 'buffer',
          cellText: false,
          cellDates: true,
          raw: false,
          codepage: 65001 // UTF-8编码
        });
        const sheetName = workbook.SheetNames[0];
        const sheet = workbook.Sheets[sheetName];
        rows = XLSX.utils.sheet_to_json(sheet, {
          defval: '',
          raw: false,
          dateNF: 'yyyy-mm-dd'
        });
      } catch (e) { console.warn('xlsx base64读取失败，回退', e.message); }
    }



    if (input.filePath) {
      try {
        const XLSX = require('xlsx');
        const workbook = XLSX.readFile(input.filePath, {
          cellText: false,
          cellDates: true,
          raw: false,
          codepage: 65001 // UTF-8编码
        });
        const sheetName = workbook.SheetNames[0];
        const sheet = workbook.Sheets[sheetName];
        rows = XLSX.utils.sheet_to_json(sheet, {
          defval: '',
          raw: false,
          dateNF: 'yyyy-mm-dd'
        });
      } catch (e) {
        console.warn('xlsx读取失败，回退到CSV/数组', e.message);
      }
    }

    // 2) CSV 文本（使用 PapaParse 提升鲁棒性）
    if (!rows.length && typeof input.csv === 'string' && input.csv.trim()) {
      try {
        const Papa = require('papaparse');
        const parsed = Papa.parse(input.csv, { header: true, skipEmptyLines: true, transformHeader: h => h.trim() });
        if (!parsed.errors?.length) {
          rows = parsed.data;
        } else {
          console.warn('PapaParse 解析存在错误，回退到简易解析:', parsed.errors?.[0]?.message);
        }
      } catch (e) {
        console.warn('未安装 papaparse 或解析失败，回退到简易解析:', e.message);
      }
      if (!rows.length) {
        const lines = input.csv.split(/\r?\n/).filter(l => l.trim().length);
        const header = lines[0].split(',').map(s=>s.trim());
        for (let i=1;i<lines.length;i++) {
          const parts = lines[i].split(',');
          const row = {};
          header.forEach((h,idx)=> row[h] = parts[idx] !== undefined ? parts[idx].trim() : '');
          rows.push(row);
        }
      }
    }

    // 3) dataset 数组
    if (!rows.length && Array.isArray(input.dataset)) {
      rows = input.dataset;
    }

    // 构造列
    const columns = {};
    if (rows.length) {
      const headers = Object.keys(rows[0]);
      headers.forEach(h => {
        const arr = rows.map(r => r[h]);
        // 尝试转数值列
        const nums = arr.map(v => Number(v)).filter(v => !isNaN(v));
        columns[h] = nums.length === arr.length ? nums : arr;
      });
    }

    // 统计
    const summary = {};
    for (const [k, arr] of Object.entries(columns)) {
      if (Array.isArray(arr) && typeof arr[0] === 'number') {
        const sorted = [...arr].sort((a,b)=>a-b);
        const mean = arr.reduce((s,v)=>s+v,0)/arr.length;
        const variance = arr.reduce((s,v)=>s+Math.pow(v-mean,2),0)/(arr.length-1 || 1);
        summary[k] = { count: arr.length, min: sorted[0], max: sorted[sorted.length-1], mean, std_dev: Math.sqrt(variance) };
      } else {
        // 类别列
        const freq = {};
        arr.forEach(v => freq[v] = (freq[v]||0)+1);
        const top = Object.entries(freq).sort((a,b)=>b[1]-a[1]).slice(0,5);
        summary[k] = { count: arr.length, top_values: top };
      }
    }

    return {
      success: true,
      type: 'excel_analysis',
      preview: rows.slice(0, Math.min(rows.length, 20)),
      columns,
      summary
    };
  }

  async executeSPCController(input = {}, config = {}) {
    // 输入：{ series: number[] } 或 { dataset:[{x:..}], valueField:'x' }
    let series = [];
    if (Array.isArray(input.series)) series = input.series.filter(v=>typeof v==='number');
    if (!series.length && Array.isArray(input.dataset) && input.valueField) {
      series = input.dataset.map(r=>Number(r[input.valueField])).filter(v=>!isNaN(v));
    }

    if (!series.length) {
      return { success: true, type: 'spc', message: '未提供有效的数值序列' };
    }

    const mean = series.reduce((s,v)=>s+v,0)/series.length;
    const variance = series.reduce((s,v)=>s+Math.pow(v-mean,2),0)/(series.length-1 || 1);
    const std = Math.sqrt(variance);
    const ucl = mean + 3*std;
    const lcl = mean - 3*std;
    const points = series.map((v,idx)=>({ index: idx+1, value: v, out_of_control: v>ucl || v<lcl }));

    return {
      success: true,
      type: 'spc',
      mean,
      ucl,
      lcl,
      points,
      chart: { mean, ucl, lcl, points },
      summary: { count: series.length, std, mean },
      recommendations: points.some(p=>p.out_of_control) ? ['存在异常点，建议复核过程波动源'] : ['过程稳定']
    };
  }


  async executeDataCleaner(input = {}, config = {}) {
    // 支持 dataset: Array<object> 或 data: Array<number>
    let dataset = []
    if (Array.isArray(input.dataset)) dataset = input.dataset
    if (!dataset.length && Array.isArray(input.data)) dataset = input.data.map((v)=>({ value: v }))

    // 去除缺失
    dataset = dataset.filter(r => Object.values(r).every(v => v !== null && v !== undefined && `${v}`.trim() !== ''))

    // 去重
    if (config.dedupe_key) {
      const seen = new Set()
      dataset = dataset.filter(r => (key => !seen.has(key) && seen.add(key))(r[config.dedupe_key]))
    }

    // 归一化
    if (config.normalize_field) {
      const vals = dataset.map(r => Number(r[config.normalize_field])).filter(v => !isNaN(v))
      const min = Math.min(...vals), max = Math.max(...vals)
      if (vals.length && max > min) {
        dataset = dataset.map(r => ({ ...r, [config.normalize_field]: (Number(r[config.normalize_field]) - min) / (max - min) }))
      }
    }

    return { success: true, cleaned: dataset.slice(0, 100), total: dataset.length }
  }

  async executeAnomalyDetector(input = {}, config = {}) {
    // 输入：{ dataset:[{x:..}], field:'x' } 或 { series:[..] }
    let series = []
    const field = config.field || input.field || 'value'
    if (Array.isArray(input.series)) series = input.series.filter(v=>typeof v==='number')
    if (!series.length && Array.isArray(input.dataset)) series = input.dataset.map(r=>Number(r[field])).filter(v=>!isNaN(v))

    if (!series.length) return { success: true, anomalies: [], message: '无有效数值' }

    const method = config.method || 'iqr'
    let anomalies = []
    if (method === 'sigma3') {
      const mean = series.reduce((a,b)=>a+b,0)/series.length
      const sd = Math.sqrt(series.map(x=>Math.pow(x-mean,2)).reduce((a,b)=>a+b,0)/series.length)
      const lower = mean - 3*sd, upper = mean + 3*sd
      anomalies = series.map((v,i)=>({index:i,value:v})).filter(p=>p.value<lower||p.value>upper)
      return { success:true, method:'sigma3', mean, sd, bounds:{lower,upper}, anomalies }
    } else {
      // IQR
      const sorted = [...series].sort((a,b)=>a-b)
      const q1 = sorted[Math.floor(sorted.length*0.25)]
      const q3 = sorted[Math.floor(sorted.length*0.75)]
      const iqr = q3 - q1
      const lower = q1 - 1.5*iqr, upper = q3 + 1.5*iqr
      anomalies = series.map((v,i)=>({index:i,value:v})).filter(p=>p.value<lower||p.value>upper)
      return { success:true, method:'iqr', q1, q3, iqr, bounds:{lower,upper}, anomalies }
    }
  }

  async executeTextSummarizer(input = {}, config = {}) {
    // 输入：{ text:string }
    const text = (input.text||'').trim()
    if (!text) return { success:true, summary:'', sentences:[] }

    const sentences = text.split(/[。.!?\n]/).map(s=>s.trim()).filter(Boolean)
    const max = Math.max(1, Math.min(config.max_sentences||3, sentences.length))

    // 简单抽取式：按句子长度与关键词频次评分
    const tokens = text.toLowerCase().replace(/[^a-z0-9\u4e00-\u9fa5\s]/g,' ').split(/\s+/).filter(Boolean)
    const freq = {}
    tokens.forEach(t=>freq[t]=(freq[t]||0)+1)

    const scored = sentences.map(s=>{
      const words = s.toLowerCase().split(/\s+/).filter(Boolean)
      const score = words.reduce((sum,w)=>sum+(freq[w]||0), 0) + s.length*0.01
      return { s, score }
    })
    scored.sort((a,b)=>b.score-a.score)
    const top = scored.slice(0, max).map(o=>o.s)
    return { success:true, summary: top.join('。'), sentences: top }
  }


	  // 新增：格式专用解析器实现
	  async executeCSVParser(input = {}, config = {}) {
	    const text = (input.csv || input.text || '').toString();
	    if (!text.trim()) return { success: true, type: 'csv', preview: [], columns: {}, summary: {}, warnings: ['空CSV'] };
	    let rows = [];
	    try {
	      const Papa = require('papaparse');
	      const parsed = Papa.parse(text, { header: true, skipEmptyLines: true, delimiter: config.delimiter || ',' });
	      rows = parsed.data || [];
	    } catch (e) {
	      return { success: false, type: 'csv', message: 'CSV解析失败: ' + e.message };
	    }
	    return this._tabularSummary(rows, 'csv');
	  }

	  async executeXLSXParser(input = {}, config = {}) {
	    try {
	      const XLSX = require('xlsx');
	      let rows = [];
      let sheetNames = [];

	      // 优先处理 XLSX 文件
	      if (input.base64) {
	        const buf = Buffer.from(input.base64, 'base64');
	        const wb = XLSX.read(buf, {
	          type: 'buffer',
	          cellText: false,
	          cellDates: true,
	          raw: false,
	          codepage: 65001 // UTF-8编码
	        });
	        rows = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]], {
	          defval: '',
	          raw: false,
	          dateNF: 'yyyy-mm-dd'
	        });
	      } else if (input.filePath) {
	        const wb = XLSX.readFile(input.filePath, {
	          cellText: false,
	          cellDates: true,
	          raw: false,
	          codepage: 65001 // UTF-8编码
	        });
	        rows = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]], {
	          defval: '',
	          raw: false,
	          dateNF: 'yyyy-mm-dd'
	        });
	      } else if (input.csv || input.text) {
	        // 回退：处理 CSV 数据
	        const text = (input.csv || input.text || '').toString();
	        if (text.trim()) {
	          try {
	            const Papa = require('papaparse');
	            const parsed = Papa.parse(text, { header: true, skipEmptyLines: true });
	            rows = parsed.data || [];
	          } catch (e) {
	            // 简单 CSV 解析回退
	            const lines = text.split(/\r?\n/).filter(l => l.trim().length);
	            if (lines.length > 1) {
	              const header = lines[0].split(',').map(s => s.trim());
	              for (let i = 1; i < lines.length; i++) {
	                const parts = lines[i].split(',');
	                const row = {};
	                header.forEach((h, idx) => row[h] = parts[idx] !== undefined ? parts[idx].trim() : '');
	                rows.push(row);
	              }
	            }
	          }
	        }
	      }


		      // 尝试补齐 sheet 名称信息
		      if (sheetNames.length === 0) {
		        try {
		          if (input.base64) {
		            const wb0 = XLSX.read(Buffer.from(input.base64, 'base64'), { type: 'buffer' });
		            sheetNames = wb0.SheetNames || [];
		          } else if (input.filePath) {
		            const wb0 = XLSX.readFile(input.filePath);
		            sheetNames = wb0.SheetNames || [];
		          }
		        } catch (e) { /* ignore */ }
		      }

	      const base = this._tabularSummary(rows, 'xlsx');
      return { ...base, sheet_names: sheetNames, row_count: rows.length, columns_list: rows[0] ? Object.keys(rows[0]) : [] };
	    } catch (e) {
	      return { success: false, type: 'xlsx', message: 'XLSX解析失败: ' + e.message };
	    }
	  }

	  async executeJSONParser(input = {}, config = {}) {
	    try {
	      let text = input.text || '';
	      if (!text && input.base64) text = Buffer.from(input.base64, 'base64').toString('utf-8');
	      const obj = typeof input.json === 'string' ? JSON.parse(input.json) : (input.json || JSON.parse(text));
	      const rows = Array.isArray(obj) ? obj : (Array.isArray(obj?.data) ? obj.data : []);
	      return this._tabularSummary(rows, 'json');
	    } catch (e) {
	      return { success: false, type: 'json', message: 'JSON解析失败: ' + e.message };
	    }
	  }

	  async executeXMLParser(input = {}, config = {}) {
	    try {
	      const xml2js = require('xml2js');
	      let text = input.text || '';
	      if (!text && input.base64) text = Buffer.from(input.base64, 'base64').toString('utf-8');
	      const parsed = await xml2js.parseStringPromise(text, { explicitArray: false, mergeAttrs: true });
	      // 将常见形态转为数组
	      const rows = Array.isArray(parsed?.root?.row) ? parsed.root.row : (Array.isArray(parsed?.rows?.row) ? parsed.rows.row : []);
	      return this._tabularSummary(rows, 'xml');
	    } catch (e) {
	      return { success: false, type: 'xml', message: 'XML解析失败: ' + e.message };
	    }
	  }

	  async executeDOCXParser(input = {}, config = {}) {
	    const startTime = Date.now();

	    try {
	      // 检查是否有mammoth库可用
	      let hasMammoth = false;
	      try {
	        require('mammoth');
	        hasMammoth = true;
	      } catch (e) {
	        console.log('📄 mammoth未安装，尝试使用docx库');
	      }

	      if (hasMammoth) {
	        return await this.executeRealDOCXParser(input, config, startTime);
	      } else {
	        // 尝试使用docx库进行解析
	        try {
	          const result = await this.executeDocxLibParser(input, config, startTime);
	          // 检查是否成功提取到内容
	          if (result.text && result.text.trim() &&
	              !result.text.includes('文档解析成功，但未能提取到可读文本内容')) {
	            return result;
	          } else {
	            console.log('📄 docx库未能提取到有效内容，使用增强模拟模式');
	            return await this.executeEnhancedDOCXSimulation(input, config, startTime);
	          }
	        } catch (docxError) {
	          console.log('📄 docx库解析失败，使用增强模拟模式:', docxError.message);
	          return await this.executeEnhancedDOCXSimulation(input, config, startTime);
	        }
	      }
	    } catch (error) {
	      return {
	        success: false,
	        type: 'docx_error',
	        error: error.message,
	        processing_time: Date.now() - startTime
	      };
	    }
	  }

	  // 共享：表格预览与统计
	  _tabularSummary(rows, type) {
	    rows = Array.isArray(rows) ? rows : [];
	    const preview = rows.slice(0, Math.min(rows.length, 20));
	    const columns = {};
	    if (rows.length) {
	      const headers = Object.keys(rows[0]);
	      headers.forEach(h => {
	        const arr = rows.map(r => r[h]);
	        const nums = arr.map(v => Number(v)).filter(v => !isNaN(v));
	        columns[h] = nums.length === arr.length ? nums : arr;
	      });
	    }
	    const summary = {};
	    for (const [k, arr] of Object.entries(columns)) {
	      if (Array.isArray(arr) && typeof arr[0] === 'number') {
	        const sorted = [...arr].sort((a,b)=>a-b);
	        const mean = arr.reduce((s,v)=>s+v,0)/arr.length;
	        const variance = arr.reduce((s,v)=>s+Math.pow(v-mean,2),0)/(arr.length-1 || 1);
	        summary[k] = { count: arr.length, min: sorted[0], max: sorted[sorted.length-1], mean, std_dev: Math.sqrt(variance) };
	      } else {
	        const freq = {};
	        arr.forEach(v => freq[v] = (freq[v]||0)+1);
	        summary[k] = { count: arr.length, top_values: Object.entries(freq).sort((a,b)=>b[1]-a[1]).slice(0,5) };
	      }
	    }
	    return { success: true, type, preview, columns, summary };
	  }



  async executeDefectDetector(input, config) {
    // 模拟缺陷检测
    return {
      success: true,
      type: 'defect_detection',
      defects_found: [
        {
          type: '划痕',
          confidence: 0.92,
          location: { x: 100, y: 150, width: 50, height: 20 },
          severity: 'medium'
        },
        {
          type: '色差',
          confidence: 0.87,
          location: { x: 200, y: 300, width: 80, height: 60 },
          severity: 'low'
        }
      ],
      overall_quality: 'acceptable',
      quality_score: 0.85
    };
  }

  async executeStatisticalAnalyzer(input = {}, config = {}) {
    // 通用统计：支持单列数组或多列数据集（columns/dataset）
    const datasets = {};

    if (Array.isArray(input.data)) {
      datasets.series = input.data;
    } else if (input.columns && typeof input.columns === 'object') {
      Object.assign(datasets, input.columns);
    } else if (Array.isArray(input.dataset)) {
      // dataset: Array<Record<string, any>>
      const rows = input.dataset;
      if (rows.length) {
        const cols = Object.keys(rows[0]);
        cols.forEach(c => {
          const arr = rows.map(r => Number(r[c])).filter(v => !isNaN(v));
          if (arr.length) datasets[c] = arr;
        });
      }
    }

    const stats = {};
    for (const [key, arr] of Object.entries(datasets)) {
      if (!Array.isArray(arr) || !arr.length) continue;
      const sorted = [...arr].sort((a,b)=>a-b);
      const mean = arr.reduce((s,v)=>s+v,0)/arr.length;
      const median = sorted.length % 2 ? sorted[(sorted.length-1)/2] : (sorted[sorted.length/2-1]+sorted[sorted.length/2])/2;
      const variance = arr.reduce((s,v)=>s+Math.pow(v-mean,2),0)/(arr.length-1 || 1);
      const std_dev = Math.sqrt(variance);
      const min = sorted[0];
      const max = sorted[sorted.length-1];
      const ucl = mean + 3*std_dev;
      const lcl = mean - 3*std_dev;
      const outliers = arr.filter(v => v>ucl || v<lcl);
      stats[key] = { count: arr.length, mean, median, std_dev, min, max, ucl, lcl, outliers };
    }

    return {
      success: true,
      type: 'statistical_analysis',
      stats,
      recommendations: Object.keys(stats).length ? ['已计算3σ控制限与异常点，请结合SPC进一步分析'] : ['未检测到数值列']
    };
  }

  // 辅助方法
  validatePluginConfig(config, schema = {}) {
    // 简化的配置验证
    return config;
  }

  createLangChainToolFunction(plugin, config) {
    return async (input) => {
      return await this.executePlugin(plugin.id, input);
    };
  }

  generateArgsSchema(configSchema) {
    // 生成LangChain工具的参数模式
    return {
      input: { type: 'string', description: '输入数据' }
    };
  }

  createSemanticKernelFunctions(plugin, config) {
    return [
      {
        name: 'execute',
        description: plugin.description,
        parameters: []
      }
    ];
  }

  createHaystackPipeline(plugin, config) {
    return {
      nodes: [
        {
          name: plugin.id,
          component: plugin.category,
          inputs: ['input'],
          outputs: ['output']
        }
      ]
    };
  }

  async executeGenericLangChainTool(plugin, input, options) {
    return {
      type: 'generic_result',
      message: `LangChain工具 ${plugin.name} 执行完成`,
      input: input,
      timestamp: new Date().toISOString()
    };
  }

  async executeFMEAAnalyzer(input = {}, config = {}) {
    // FMEA失效模式分析
    const components = input.components || [
      { name: '组件A', failure_modes: ['磨损', '断裂'], severity: 8, occurrence: 3, detection: 4 },
      { name: '组件B', failure_modes: ['腐蚀', '变形'], severity: 6, occurrence: 2, detection: 5 }
    ];

    const analysis = components.map(comp => {
      const rpn = comp.severity * comp.occurrence * comp.detection;
      return {
        component: comp.name,
        failure_modes: comp.failure_modes,
        severity: comp.severity,
        occurrence: comp.occurrence,
        detection: comp.detection,
        rpn: rpn,
        risk_level: rpn > 100 ? 'high' : rpn > 50 ? 'medium' : 'low',
        recommendations: rpn > 100 ? ['立即采取纠正措施', '增加检测频率'] : ['定期监控']
      };
    });

    return {
      success: true,
      type: 'fmea_analysis',
      analysis: analysis,
      components: analysis,
      summary: {
        total_components: components.length,
        high_risk_count: analysis.filter(a => a.risk_level === 'high').length,
        average_rpn: analysis.reduce((sum, a) => sum + a.rpn, 0) / analysis.length
      }
    };
  }

  async executeMSACalculator(input = {}, config = {}) {
    // MSA测量系统分析
    const measurements = input.measurements || [
      [10.1, 10.2, 10.0], [9.9, 10.1, 10.0], [10.0, 9.8, 10.1]
    ];

    const operators = input.operators || ['操作员A', '操作员B', '操作员C'];
    const parts = input.parts || ['零件1', '零件2', '零件3'];

    // 计算重复性和再现性
    const repeatability = this.calculateRepeatability(measurements);
    const reproducibility = this.calculateReproducibility(measurements);
    const gage_rr = Math.sqrt(repeatability ** 2 + reproducibility ** 2);

    // 计算%R&R
    const total_variation = this.calculateTotalVariation(measurements);
    const percent_rr = (gage_rr / total_variation) * 100;

    return {
      success: true,
      type: 'msa_analysis',
      repeatability: repeatability,
      reproducibility: reproducibility,
      gage_rr: gage_rr,
      percent_rr: percent_rr,
      evaluation: percent_rr < 10 ? 'acceptable' : percent_rr < 30 ? 'marginal' : 'unacceptable',
      recommendations: percent_rr > 30 ? ['改进测量系统', '培训操作员'] : ['系统可接受']
    };
  }

  async executeAPIConnector(input = {}, config = {}) {
    const axios = require('axios');
    const startTime = Date.now();

    try {
      const {
        url,
        method = 'GET',
        headers = {},
        data,
        params,
        timeout = config.timeout || 30000,
        auth_type = config.auth_type || 'none',
        api_key = config.api_key
      } = input;

      if (!url) {
        throw new Error('URL参数是必需的');
      }

      // 处理认证
      const requestHeaders = { ...headers };
      if (auth_type === 'api_key' && api_key) {
        requestHeaders['Authorization'] = `Bearer ${api_key}`;
      } else if (auth_type === 'basic' && input.username && input.password) {
        const auth = Buffer.from(`${input.username}:${input.password}`).toString('base64');
        requestHeaders['Authorization'] = `Basic ${auth}`;
      }

      // 添加默认headers
      if (!requestHeaders['User-Agent']) {
        requestHeaders['User-Agent'] = 'QMS-AI-System/1.0';
      }

      console.log(`🌐 发起API请求: ${method.toUpperCase()} ${url}`);

      const response = await axios({
        url,
        method: method.toUpperCase(),
        headers: requestHeaders,
        data,
        params,
        timeout,
        validateStatus: () => true, // 接受所有状态码
        maxRedirects: 5,
        maxContentLength: 50 * 1024 * 1024, // 50MB限制
        maxBodyLength: 50 * 1024 * 1024
      });

      const duration = Date.now() - startTime;
      const isSuccess = response.status >= 200 && response.status < 300;

      // 分析响应内容类型
      const contentType = response.headers['content-type'] || '';
      let responseSize = 0;
      let dataPreview = response.data;

      if (typeof response.data === 'string') {
        responseSize = response.data.length;
        if (responseSize > 1000) {
          dataPreview = response.data.substring(0, 1000) + '...(truncated)';
        }
      } else if (typeof response.data === 'object') {
        const jsonStr = JSON.stringify(response.data);
        responseSize = jsonStr.length;
        if (responseSize > 1000) {
          dataPreview = JSON.stringify(response.data, null, 2).substring(0, 1000) + '...(truncated)';
        }
      }

      return {
        success: isSuccess,
        type: 'api_response',
        status: response.status,
        status_code: response.status,
        statusText: response.statusText,
        headers: response.headers,
        data: response.data,
        response_data: response.data,
        data_preview: dataPreview,
        url: response.config.url,
        method: response.config.method.toUpperCase(),
        duration,
        statistics: {
          response_size: responseSize,
          response_time: duration,
          status_code: response.status,
          content_type: contentType,
          is_json: contentType.includes('application/json'),
          is_html: contentType.includes('text/html'),
          is_xml: contentType.includes('xml')
        },
        metadata: {
          request_headers: requestHeaders,
          response_headers: response.headers,
          final_url: response.request?.res?.responseUrl || url,
          redirects: response.request?._redirectCount || 0
        },
        warnings: isSuccess ? [] : [`HTTP ${response.status}: ${response.statusText}`]
      };
    } catch (error) {
      const duration = Date.now() - startTime;

      // 分析错误类型
      let errorType = 'unknown_error';
      let errorDetails = error.message;

      if (error.code === 'ECONNREFUSED') {
        errorType = 'connection_refused';
        errorDetails = '连接被拒绝，请检查URL和网络连接';
      } else if (error.code === 'ENOTFOUND') {
        errorType = 'dns_error';
        errorDetails = 'DNS解析失败，请检查域名是否正确';
      } else if (error.code === 'ECONNABORTED') {
        errorType = 'timeout';
        errorDetails = '请求超时，请检查网络连接或增加超时时间';
      } else if (error.response) {
        errorType = 'http_error';
        errorDetails = `HTTP ${error.response.status}: ${error.response.statusText}`;
      }

      return {
        success: false,
        type: 'api_error',
        error: errorDetails,
        error_type: errorType,
        code: error.code,
        duration,
        url: input.url,
        method: input.method || 'GET',
        statistics: {
          response_time: duration,
          error_code: error.code
        },
        metadata: {
          original_error: error.message
        }
      };
    }
  }

  async executeDatabaseQuery(input = {}, config = {}) {
    // 数据库查询器 - 模拟数据库查询
    const query = input.query || config.query || 'SELECT * FROM products LIMIT 10';
    const database = input.database || config.database || 'default';

    try {
      // 模拟数据库查询结果
      const mockResults = [
        { id: 1, name: '产品A', price: 100, category: '电子产品' },
        { id: 2, name: '产品B', price: 200, category: '家居用品' },
        { id: 3, name: '产品C', price: 150, category: '办公用品' }
      ];

      return {
        success: true,
        type: 'database_result',
        query: query,
        database: database,
        results: mockResults,
        count: mockResults.length,
        execution_time: Math.random() * 100 + 10 // 模拟执行时间
      };
    } catch (error) {
      return {
        success: false,
        type: 'database_error',
        message: `数据库查询失败: ${error.message}`,
        query: query,
        database: database
      };
    }
  }

  // MSA计算辅助方法
  calculateRepeatability(measurements) {
    // 简化的重复性计算
    let sumSquares = 0;
    let count = 0;
    measurements.forEach(group => {
      const mean = group.reduce((a, b) => a + b, 0) / group.length;
      group.forEach(val => {
        sumSquares += (val - mean) ** 2;
        count++;
      });
    });
    return Math.sqrt(sumSquares / (count - measurements.length));
  }

  calculateReproducibility(measurements) {
    // 简化的再现性计算
    const groupMeans = measurements.map(group =>
      group.reduce((a, b) => a + b, 0) / group.length
    );
    const overallMean = groupMeans.reduce((a, b) => a + b, 0) / groupMeans.length;
    const variance = groupMeans.reduce((sum, mean) => sum + (mean - overallMean) ** 2, 0) / (groupMeans.length - 1);
    return Math.sqrt(variance);
  }

  calculateTotalVariation(measurements) {
    // 简化的总变异计算
    const allValues = measurements.flat();
    const mean = allValues.reduce((a, b) => a + b, 0) / allValues.length;
    const variance = allValues.reduce((sum, val) => sum + (val - mean) ** 2, 0) / (allValues.length - 1);
    return Math.sqrt(variance);
  }

  async executeGenericCustomPlugin(plugin, input, options) {
    return {
      success: true,
      type: 'generic_result',
      message: `自定义插件 ${plugin.name} 执行完成`,
      input: input,
      timestamp: new Date().toISOString()
    };
  }

  // 数据库操作方法
  async savePluginToDatabase(plugin) {
    try {
      await this.dbAdapter.query(`
        INSERT OR REPLACE INTO plugins (
          id, name, description, type, category, version, author,
          capabilities, config_schema, status, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, [
        plugin.id,
        plugin.name,
        plugin.description,
        plugin.type,
        plugin.category,
        plugin.version,
        plugin.author,
        JSON.stringify(plugin.capabilities),
        JSON.stringify(plugin.config_schema),
        plugin.status,
        plugin.created_at,
        plugin.updated_at
      ]);
    } catch (error) {
      console.warn('保存插件到数据库失败:', error.message);
    }
  }

  async updatePluginInDatabase(plugin) {
    try {
      await this.dbAdapter.query(`
        UPDATE plugins
        SET status = ?, config = ?, usage_count = ?, last_used = ?, updated_at = ?
        WHERE id = ?
      `, [
        plugin.status,
        JSON.stringify(plugin.config || {}),
        plugin.usage_count,
        plugin.last_used,
        plugin.updated_at,
        plugin.id
      ]);
    } catch (error) {
      console.warn('更新插件数据库失败:', error.message);
    }
  }

  async updatePluginStats(plugin) {
    try {
      await this.dbAdapter.query(`
        UPDATE plugins
        SET usage_count = ?, last_used = ?
        WHERE id = ?
      `, [
        plugin.usage_count,
        plugin.last_used,
        plugin.id
      ]);
    } catch (error) {
      console.warn('更新插件统计失败:', error.message);
    }
  }

  /**
   * 获取插件列表
   */
  getPluginList(filters = {}) {
    const plugins = Array.from(this.pluginRegistry.values());

    // 应用过滤器
    let filteredPlugins = plugins;

    if (filters.type) {
      filteredPlugins = filteredPlugins.filter(p => p.type === filters.type);
    }

    if (filters.category) {
      filteredPlugins = filteredPlugins.filter(p => p.category === filters.category);
    }

    if (filters.status) {
      filteredPlugins = filteredPlugins.filter(p => p.status === filters.status);
    }

    return filteredPlugins.map(plugin => ({
      id: plugin.id,
      name: plugin.name,
      description: plugin.description,
      type: plugin.type,
      category: plugin.category,
      version: plugin.version,
      author: plugin.author,
      capabilities: plugin.capabilities,
      status: plugin.status,
      usage_count: plugin.usage_count,
      last_used: plugin.last_used
    }));
  }

  /**
   * 获取插件详情
   */
  getPluginDetails(pluginId) {
    return this.pluginRegistry.get(pluginId) || null;
  }

  /**
   * 卸载插件
   */
  async uninstallPlugin(pluginId) {
    console.log(`🗑️ 卸载插件: ${pluginId}`);

    try {
      const plugin = this.activePlugins.get(pluginId);
      if (!plugin) {
        throw new Error(`插件未安装: ${pluginId}`);
      }

      // 从活跃插件中移除
      this.activePlugins.delete(pluginId);

      // 更新状态
      plugin.status = 'inactive';
      plugin.updated_at = new Date().toISOString();

      // 更新数据库
      await this.updatePluginInDatabase(plugin);

      console.log(`✅ 插件卸载成功: ${plugin.name}`);
      return true;

    } catch (error) {
      console.error(`❌ 插件卸载失败: ${pluginId}`, error);
      throw error;
    }
  }

  /**
   * 获取插件统计信息
   */
  getPluginStats() {
    const allPlugins = Array.from(this.pluginRegistry.values());
    const activePlugins = Array.from(this.activePlugins.values());

    const stats = {
      total_plugins: allPlugins.length,
      active_plugins: activePlugins.length,
      inactive_plugins: allPlugins.length - activePlugins.length,
      by_type: {},
      by_category: {},
      most_used: null
    };

    // 按类型统计
    allPlugins.forEach(plugin => {
      stats.by_type[plugin.type] = (stats.by_type[plugin.type] || 0) + 1;
    });

    // 按分类统计
    allPlugins.forEach(plugin => {
      stats.by_category[plugin.category] = (stats.by_category[plugin.category] || 0) + 1;
    });

    // 最常用插件
    const sortedByUsage = allPlugins.sort((a, b) => (b.usage_count || 0) - (a.usage_count || 0));
    if (sortedByUsage.length > 0) {
      stats.most_used = {
        id: sortedByUsage[0].id,
        name: sortedByUsage[0].name,
        usage_count: sortedByUsage[0].usage_count || 0
      };
    }

    return stats;
  }

  // Excel分析器增强方法
  async analyzeWorksheet(worksheet, sheetName, config = {}) {
    const XLSX = require('xlsx');
    const range = XLSX.utils.decode_range(worksheet['!ref'] || 'A1:A1');
    const data = XLSX.utils.sheet_to_json(worksheet, { header: 1 });

    const analysis = {
      name: sheetName,
      range: worksheet['!ref'],
      cell_count: (range.e.r - range.s.r + 1) * (range.e.c - range.s.c + 1),
      data_rows: data.length,
      columns: range.e.c - range.s.c + 1,
      formulas: [],
      data_preview: data.slice(0, 5), // 前5行预览
      column_types: {},
      data_quality: {}
    };

    // 查找公式
    for (let R = range.s.r; R <= range.e.r; ++R) {
      for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: R, c: C });
        const cell = worksheet[cellAddress];

        if (cell && cell.f) {
          analysis.formulas.push({
            address: cellAddress,
            formula: cell.f,
            value: cell.v,
            type: cell.t
          });
        }
      }
    }

    // 分析列数据类型和质量
    if (data.length > 1) {
      const headers = data[0] || [];
      headers.forEach((header, index) => {
        const columnData = data.slice(1).map(row => row[index]).filter(val => val !== undefined);
        analysis.column_types[header || `Column_${index}`] = this.detectColumnType(columnData);
        analysis.data_quality[header || `Column_${index}`] = this.analyzeColumnQuality(columnData);
      });
    }

    return analysis;
  }

  detectColumnType(columnData) {
    if (columnData.length === 0) return { primary_type: 'empty', confidence: 1.0 };

    const types = {
      number: 0,
      date: 0,
      text: 0,
      boolean: 0,
      formula: 0
    };

    columnData.forEach(value => {
      if (typeof value === 'number') {
        types.number++;
      } else if (value instanceof Date || /^\d{4}-\d{2}-\d{2}/.test(value)) {
        types.date++;
      } else if (typeof value === 'boolean') {
        types.boolean++;
      } else if (typeof value === 'string' && value.startsWith('=')) {
        types.formula++;
      } else {
        types.text++;
      }
    });

    const total = columnData.length;
    const maxType = Object.keys(types).reduce((a, b) => types[a] > types[b] ? a : b);
    const confidence = types[maxType] / total;

    return {
      primary_type: maxType,
      confidence: confidence,
      distribution: Object.fromEntries(
        Object.entries(types).map(([k, v]) => [k, (v / total * 100).toFixed(1) + '%'])
      ),
      sample_values: columnData.slice(0, 3)
    };
  }

  analyzeColumnQuality(columnData) {
    const total = columnData.length;
    const nonEmpty = columnData.filter(val => val !== null && val !== undefined && val !== '').length;
    const unique = new Set(columnData).size;

    // 检测异常值（仅对数值列）
    let outliers = [];
    const numbers = columnData.filter(val => typeof val === 'number');
    if (numbers.length > 0) {
      const sorted = [...numbers].sort((a, b) => a - b);
      const q1 = sorted[Math.floor(sorted.length * 0.25)];
      const q3 = sorted[Math.floor(sorted.length * 0.75)];
      const iqr = q3 - q1;
      const lowerBound = q1 - 1.5 * iqr;
      const upperBound = q3 + 1.5 * iqr;
      outliers = numbers.filter(val => val < lowerBound || val > upperBound);
    }

    const completeness = nonEmpty / total;
    const uniqueness = unique / total;

    // 计算质量分数
    let qualityScore = completeness * 0.4 + uniqueness * 0.3;
    if (outliers.length / total < 0.05) qualityScore += 0.3; // 异常值少于5%

    return {
      completeness: (completeness * 100).toFixed(1) + '%',
      uniqueness: (uniqueness * 100).toFixed(1) + '%',
      missing_count: total - nonEmpty,
      duplicate_count: total - unique,
      outlier_count: outliers.length,
      quality_score: (qualityScore * 100).toFixed(1),
      issues: this.identifyDataIssues(columnData, outliers)
    };
  }

  identifyDataIssues(columnData, outliers) {
    const issues = [];
    const total = columnData.length;
    const missing = columnData.filter(val => val === null || val === undefined || val === '').length;

    if (missing / total > 0.1) issues.push('缺失值过多');
    if (outliers.length / total > 0.05) issues.push('异常值较多');
    if (new Set(columnData).size === 1) issues.push('所有值相同');
    if (columnData.some(val => typeof val === 'string' && val.length > 100)) issues.push('包含超长文本');

    return issues;
  }

  analyzeDataQuality(sheets) {
    const allQualities = [];

    Object.values(sheets).forEach(sheet => {
      Object.values(sheet.data_quality || {}).forEach(quality => {
        if (quality.quality_score) {
          allQualities.push(parseFloat(quality.quality_score));
        }
      });
    });

    const overallScore = allQualities.length > 0
      ? allQualities.reduce((sum, score) => sum + score, 0) / allQualities.length
      : 0;

    return {
      overall_score: overallScore.toFixed(1),
      sheet_count: Object.keys(sheets).length,
      total_columns: Object.values(sheets).reduce((sum, sheet) => sum + Object.keys(sheet.column_types || {}).length, 0),
      quality_distribution: {
        excellent: allQualities.filter(s => s >= 80).length,
        good: allQualities.filter(s => s >= 60 && s < 80).length,
        poor: allQualities.filter(s => s < 60).length
      }
    };
  }

  analyzeDataTypes(sheets) {
    const typeCount = {};

    Object.values(sheets).forEach(sheet => {
      Object.values(sheet.column_types || {}).forEach(typeInfo => {
        const type = typeInfo.primary_type;
        typeCount[type] = (typeCount[type] || 0) + 1;
      });
    });

    return typeCount;
  }

  generateExcelRecommendations(analysis) {
    const recommendations = [];

    // 基于数据质量生成建议
    if (analysis.data_quality.overall_score < 60) {
      recommendations.push('数据质量较低，建议进行数据清洗');
    }

    // 基于公式数量生成建议
    if (analysis.formulas.length > 50) {
      recommendations.push('包含大量公式，建议检查计算逻辑');
    }

    // 基于工作表数量生成建议
    if (analysis.workbook_info.sheet_count > 10) {
      recommendations.push('工作表较多，建议整理文档结构');
    }

    // 基于数据类型分布生成建议
    const typeCount = Object.keys(analysis.data_types).length;
    if (typeCount > 5) {
      recommendations.push('数据类型多样，建议标准化数据格式');
    }

    return recommendations.length > 0 ? recommendations : ['数据结构良好，无特殊建议'];
  }

  // 兼容方法：将CSV作为Excel分析
  async analyzeCSVAsExcel(csvText, config, startTime) {
    try {
      const Papa = require('papaparse');
      const parsed = Papa.parse(csvText, { header: true, skipEmptyLines: true });

      const analysis = {
        workbook_info: {
          sheet_names: ['CSV_Data'],
          sheet_count: 1,
          source: { type: 'csv', size: csvText.length }
        },
        sheets: {
          'CSV_Data': {
            name: 'CSV_Data',
            data_rows: parsed.data.length,
            data_preview: parsed.data.slice(0, 5),
            column_types: {},
            data_quality: {}
          }
        },
        summary: {
          total_cells: parsed.data.length * Object.keys(parsed.data[0] || {}).length,
          processing_time: Date.now() - startTime
        }
      };

      return {
        success: true,
        type: 'excel_analysis',
        data: analysis,
        statistics: analysis.summary,
        metadata: { file_type: 'csv_as_excel' }
      };
    } catch (error) {
      return {
        success: false,
        type: 'excel_error',
        error: error.message,
        processing_time: Date.now() - startTime
      };
    }
  }

  // 兼容方法：将数组作为Excel分析
  async analyzeArrayAsExcel(dataArray, config, startTime) {
    const analysis = {
      workbook_info: {
        sheet_names: ['Array_Data'],
        sheet_count: 1,
        source: { type: 'array', size: dataArray.length }
      },
      sheets: {
        'Array_Data': {
          name: 'Array_Data',
          data_rows: dataArray.length,
          data_preview: dataArray.slice(0, 5),
          column_types: {},
          data_quality: {}
        }
      },
      summary: {
        total_cells: dataArray.length * Object.keys(dataArray[0] || {}).length,
        processing_time: Date.now() - startTime
      }
    };

    return {
      success: true,
      type: 'excel_analysis',
      data: analysis,
      statistics: analysis.summary,
      metadata: { file_type: 'array_as_excel' }
    };
  }

  // DOCX解析器辅助方法
  async executeRealDOCXParser(input, config, startTime) {
    const mammoth = require('mammoth');

    let buffer;

    // 处理不同输入类型
    if (input.filePath) {
      buffer = require('fs').readFileSync(input.filePath);
    } else if (input.buffer) {
      buffer = input.buffer;
    } else if (input.base64) {
      buffer = Buffer.from(input.base64, 'base64');
    } else {
      throw new Error('需要提供filePath、buffer或base64数据');
    }

    console.log('📄 开始解析DOCX文档...');

    // 提取纯文本
    const textResult = await mammoth.extractRawText({ buffer });

    // 提取HTML（保留格式）
    const htmlResult = await mammoth.convertToHtml({ buffer });

    // 提取样式信息
    const styleMap = config.extract_styles ? await this.extractDOCXStyles(buffer) : null;

    const text = textResult.value;
    const html = htmlResult.value;

    return {
      success: true,
      type: 'docx_content',
      text: text,
      html: html,
      styles: styleMap,
      data: {
        text: text,
        html: html,
        content: text,
        preview: text ? [{ content: text.substring(0, 200) + '...' }] : []
      },
      statistics: {
        character_count: text.length,
        word_count: text.split(/\s+/).filter(word => word.length > 0).length,
        paragraph_count: text.split(/\n\s*\n/).length,
        processing_time: Date.now() - startTime
      },
      warnings: [
        ...textResult.messages.map(m => m.message),
        ...htmlResult.messages.map(m => m.message)
      ],
      metadata: {
        extraction_method: 'mammoth',
        has_styles: !!styleMap,
        format: 'docx'
      }
    };
  }

  async executeEnhancedDOCXSimulation(input, config, startTime) {
    // 如果提供了文本，直接处理
    if (input.text) {
      const text = input.text;
      return {
        success: true,
        type: 'docx_content',
        text: text,
        html: `<p>${text.replace(/\n/g, '</p><p>')}</p>`,
        data: {
          text: text,
          html: `<p>${text.replace(/\n/g, '</p><p>')}</p>`,
          content: text,
          preview: text ? [{ content: text.substring(0, 200) + '...' }] : []
        },
        statistics: {
          character_count: text.length,
          word_count: text.split(/\s+/).filter(word => word.length > 0).length,
          paragraph_count: text.split(/\n\s*\n/).length,
          processing_time: Date.now() - startTime
        },
        metadata: {
          extraction_method: 'text_input',
          format: 'text'
        }
      };
    }

    // 生成模拟的DOCX内容
    const simulatedContent = this.generateSimulatedDOCXContent(input);

    return {
      success: true,
      type: 'docx_content',
      text: simulatedContent.text,
      html: simulatedContent.html,
      data: {
        text: simulatedContent.text,
        html: simulatedContent.html,
        content: simulatedContent.text,
        preview: simulatedContent.text ? [{ content: simulatedContent.text.substring(0, 200) + '...' }] : []
      },
      statistics: {
        character_count: simulatedContent.text.length,
        word_count: simulatedContent.text.split(/\s+/).filter(word => word.length > 0).length,
        paragraph_count: simulatedContent.text.split(/\n\s*\n/).length,
        processing_time: Date.now() - startTime
      },
      metadata: {
        extraction_method: 'enhanced_simulation',
        format: 'docx_simulation'
      },
      warnings: ['当前为增强模拟模式，如需真实DOCX解析请安装mammoth库']
    };
  }

  generateSimulatedDOCXContent(input) {
    // 基于输入参数生成不同类型的模拟文档
    const documentTypes = {
      'report': {
        text: `X6532项目达成全包包装等手机屏幕印刷报告

项目概述
项目名称：X6532项目达成全包包装等手机屏幕印刷
项目编号：QMS-2025-001
报告日期：2025年1月15日
负责部门：重庆工厂质量管理部

一、项目背景
本项目旨在实现X6532手机屏幕的全包装印刷工艺，确保产品质量符合行业标准。

二、质量控制要点
1. 印刷精度控制：±0.05mm
2. 色彩一致性：ΔE≤2.0
3. 附着力测试：≥5B级
4. 耐磨性测试：≥4H

三、检测结果
经过严格的质量检测，产品各项指标均达到预期要求：
- 印刷精度：±0.03mm（符合要求）
- 色彩偏差：ΔE=1.2（符合要求）
- 附着力：5B级（符合要求）
- 耐磨性：4H（符合要求）

四、质量改进建议
1. 优化印刷工艺参数
2. 加强过程监控
3. 完善质量追溯体系

五、结论
X6532项目达成全包包装等手机屏幕印刷工艺已成功实施，产品质量稳定可靠，建议正式投产。`,
        html: ''
      },
      'manual': {
        text: `质量管理系统操作手册

第一章 系统概述
本系统是一个质量管理系统，旨在帮助企业提升产品质量和管理效率。

第二章 功能介绍
2.1 数据分析功能
系统提供强大的数据分析能力，包括统计分析、趋势分析等。

2.2 质量控制功能
支持SPC控制图、FMEA分析等质量管理工具。

第三章 操作指南
3.1 登录系统
用户可通过用户名和密码登录系统。

3.2 数据导入
支持Excel、CSV等多种格式的数据导入。`,
        html: `<h1>操作手册</h1>
<h2>第一章 系统概述</h2>
<p>本系统是一个质量管理系统，旨在帮助企业提升产品质量和管理效率。</p>
<h2>第二章 功能介绍</h2>
<h3>2.1 数据分析功能</h3>
<p>系统提供强大的数据分析能力，包括统计分析、趋势分析等。</p>
<h3>2.2 质量控制功能</h3>
<p>支持SPC控制图、FMEA分析等质量管理工具。</p>
<h2>第三章 操作指南</h2>
<h3>3.1 登录系统</h3>
<p>用户可通过用户名和密码登录系统。</p>
<h3>3.2 数据导入</h3>
<p>支持Excel、CSV等多种格式的数据导入。</p>`
      },
      'report': {
        text: `质量分析报告

报告编号：QR-2025-001
报告日期：2025年1月15日
分析人员：质量部

一、概述
本报告对2024年第四季度的产品质量数据进行了全面分析。

二、数据来源
数据来源于生产线质量检测系统，包含以下指标：
- 产品合格率
- 缺陷率
- 客户投诉率

三、分析结果
3.1 总体质量水平
第四季度产品合格率达到99.2%，较上季度提升0.3%。

3.2 主要问题
发现以下质量问题需要关注：
1. 外观缺陷率略有上升
2. 尺寸精度波动较大

四、改进建议
建议采取以下措施：
1. 加强员工培训
2. 优化工艺参数
3. 增加检测频次`,
        html: `<h1>质量分析报告</h1>
<p><strong>报告编号：</strong>QR-2025-001<br>
<strong>报告日期：</strong>2025年1月15日<br>
<strong>分析人员：</strong>质量部</p>
<h2>一、概述</h2>
<p>本报告对2024年第四季度的产品质量数据进行了全面分析。</p>
<h2>二、数据来源</h2>
<p>数据来源于生产线质量检测系统，包含以下指标：</p>
<ul>
<li>产品合格率</li>
<li>缺陷率</li>
<li>客户投诉率</li>
</ul>
<h2>三、分析结果</h2>
<h3>3.1 总体质量水平</h3>
<p>第四季度产品合格率达到99.2%，较上季度提升0.3%。</p>
<h3>3.2 主要问题</h3>
<p>发现以下质量问题需要关注：</p>
<ol>
<li>外观缺陷率略有上升</li>
<li>尺寸精度波动较大</li>
</ol>
<h2>四、改进建议</h2>
<p>建议采取以下措施：</p>
<ol>
<li>加强员工培训</li>
<li>优化工艺参数</li>
<li>增加检测频次</li>
</ol>`
      }
    };

    // 根据输入推断文档类型
    let docType = 'manual';
    if (input.filePath) {
      const path = input.filePath.toLowerCase();
      if (path.includes('report') || path.includes('报告')) docType = 'report';
    }

    return documentTypes[docType];
  }

  // 提取DOCX样式信息
  async extractDOCXStyles(buffer) {
    const mammoth = require('mammoth');

    try {
      const result = await mammoth.convertToHtml({
        buffer,
        styleMap: [
          "p[style-name='Heading 1'] => h1:fresh",
          "p[style-name='Heading 2'] => h2:fresh",
          "p[style-name='Heading 3'] => h3:fresh",
          "p[style-name='Title'] => h1.title:fresh",
          "p[style-name='Subtitle'] => h2.subtitle:fresh"
        ]
      });

      return {
        html_with_styles: result.value,
        style_messages: result.messages.map(m => m.message)
      };
    } catch (error) {
      return null;
    }
  }

  // 使用高级方法解析DOCX文件
  async executeDocxLibParser(input, config, startTime) {
    const fs = require('fs');
    const path = require('path');
    const zlib = require('zlib');

    let buffer;

    // 处理不同输入类型
    if (input.filePath) {
      buffer = fs.readFileSync(input.filePath);
    } else if (input.buffer) {
      buffer = input.buffer;
    } else if (input.base64) {
      buffer = Buffer.from(input.base64, 'base64');
    } else {
      throw new Error('需要提供filePath、buffer或base64数据');
    }

    console.log('📄 使用高级方法开始解析DOCX文档...');

    try {
      // 使用更强大的DOCX解析方法
      const extractedText = await this.extractDocxContent(buffer);

      if (!extractedText || extractedText.trim().length === 0) {
        throw new Error('无法从DOCX文件中提取文本内容');
      }

      // 清理和格式化文本
      const cleanText = this.cleanDocxText(extractedText);

      // 生成HTML格式
      const html = this.generateDocxHtml(cleanText);

      return {
        success: true,
        type: 'docx_content',
        text: cleanText,
        html: html,
        data: {
          text: cleanText,
          html: html,
          content: cleanText,
          preview: cleanText ? [{ content: cleanText.substring(0, 200) + '...' }] : []
        },
        statistics: {
          character_count: cleanText.length,
          word_count: cleanText.split(/\s+/).filter(word => word.length > 0).length,
          paragraph_count: Math.max(1, cleanText.split(/\n\s*\n/).length),
          processing_time: Date.now() - startTime
        },
        metadata: {
          extraction_method: 'advanced_zip_parsing',
          format: 'docx',
          file_size: buffer.length
        },
        warnings: []
      };

    } catch (error) {
      throw new Error(`DOCX解析失败: ${error.message}`);
    }
  }

  // 提取DOCX内容的核心方法
  async extractDocxContent(buffer) {
    try {
      // 尝试使用yauzl库解析ZIP（如果可用）
      try {
        const yauzl = require('yauzl');
        return await this.extractWithYauzl(buffer);
      } catch (e) {
        console.log('📄 yauzl不可用，使用内置ZIP解析');
      }

      // 使用内置方法解析ZIP
      return await this.extractWithBuiltinZip(buffer);
    } catch (error) {
      console.log('📄 ZIP解析失败，尝试直接文本提取');
      return this.extractWithDirectParsing(buffer);
    }
  }

  // 使用内置方法解析ZIP
  async extractWithBuiltinZip(buffer) {
    console.log('📄 开始内置ZIP解析，文件大小:', buffer.length);

    // 查找document.xml文件
    let documentXml = '';

    // 简化的ZIP解析 - 查找document.xml的内容
    // 使用binary编码查找文件结构，但用utf8读取XML内容
    const bufferStr = buffer.toString('binary');

    // 查找word/document.xml文件的开始位置
    const docXmlStart = bufferStr.indexOf('word/document.xml');
    console.log('📄 查找word/document.xml位置:', docXmlStart);

    if (docXmlStart !== -1) {
      // 查找XML内容的开始
      const xmlStart = bufferStr.indexOf('<?xml', docXmlStart);
      console.log('📄 查找XML开始位置:', xmlStart);

      if (xmlStart !== -1) {
        // 查找XML内容的结束
        const xmlEnd = bufferStr.indexOf('</w:document>', xmlStart);
        console.log('📄 查找XML结束位置:', xmlEnd);

        if (xmlEnd !== -1) {
          // 提取XML部分的buffer并用UTF-8解码
          const xmlBuffer = buffer.slice(xmlStart, xmlEnd + 13);
          documentXml = xmlBuffer.toString('utf8');
          console.log('📄 提取到XML长度:', documentXml.length);
        }
      }
    }

    if (!documentXml) {
      console.log('📄 未找到直接XML，尝试压缩数据解析');
      // 尝试另一种方法：查找压缩的XML数据
      return this.extractCompressedXml(buffer);
    }

    // 解析XML并提取文本
    const text = this.parseDocumentXml(documentXml);
    console.log('📄 解析得到文本长度:', text.length);
    return text;
  }

  // 提取压缩的XML数据
  extractCompressedXml(buffer) {
    const zlib = require('zlib');
    let extractedText = '';
    let foundFiles = [];

    try {
      // 查找ZIP文件条目
      const localFileSignature = Buffer.from([0x50, 0x4b, 0x03, 0x04]);
      let offset = 0;

      console.log('📄 开始查找ZIP文件条目...');

      while (offset < buffer.length - 30) {
        const signatureIndex = buffer.indexOf(localFileSignature, offset);
        if (signatureIndex === -1) break;

        // 读取文件头信息
        const fileNameLength = buffer.readUInt16LE(signatureIndex + 26);
        const extraFieldLength = buffer.readUInt16LE(signatureIndex + 28);
        const compressionMethod = buffer.readUInt16LE(signatureIndex + 8);
        const compressedSize = buffer.readUInt32LE(signatureIndex + 18);
        const uncompressedSize = buffer.readUInt32LE(signatureIndex + 22);

        // 获取文件名
        const fileNameStart = signatureIndex + 30;
        const fileName = buffer.toString('utf8', fileNameStart, fileNameStart + fileNameLength);
        foundFiles.push(fileName);

        console.log(`📄 找到文件: ${fileName}, 压缩方法: ${compressionMethod}, 压缩大小: ${compressedSize}, 原始大小: ${uncompressedSize}`);

        // 如果是document.xml文件（不是rels文件）
        if (fileName === 'word/document.xml') {
          const dataStart = fileNameStart + fileNameLength + extraFieldLength;
          const compressedData = buffer.slice(dataStart, dataStart + compressedSize);

          console.log('📄 开始解压document.xml...');

          try {
            let xmlData;
            if (compressionMethod === 8) {
              // Deflate压缩
              xmlData = zlib.inflateRawSync(compressedData);
            } else if (compressionMethod === 0) {
              // 无压缩
              xmlData = compressedData;
            } else {
              console.log('📄 不支持的压缩方法:', compressionMethod);
              offset = signatureIndex + 30 + fileNameLength + extraFieldLength + compressedSize;
              continue;
            }

            const xmlString = xmlData.toString('utf8');
            console.log('📄 解压得到XML长度:', xmlString.length);
            extractedText = this.parseDocumentXml(xmlString);
            console.log('📄 解析得到文本长度:', extractedText.length);
            break;
          } catch (e) {
            console.log('📄 解压缩失败:', e.message);
            // 尝试其他解压方法
            try {
              const xmlData = zlib.inflateSync(compressedData);
              const xmlString = xmlData.toString('utf8');
              extractedText = this.parseDocumentXml(xmlString);
              console.log('📄 使用备用解压方法成功');
              break;
            } catch (e2) {
              console.log('📄 备用解压方法也失败:', e2.message);
            }
          }
        }

        // 移动到下一个文件条目
        offset = signatureIndex + 30 + fileNameLength + extraFieldLength + compressedSize;
      }

      console.log('📄 ZIP文件中找到的所有文件:', foundFiles);
    } catch (error) {
      console.log('📄 压缩XML提取失败:', error.message);
    }

    return extractedText;
  }

  // 解析document.xml并提取文本 - 增强版
  parseDocumentXml(xmlString) {
    let text = '';

    try {
      console.log('📄 开始解析XML，长度:', xmlString.length);

      // 增强的文本提取 - 处理多种文本元素
      text = this.extractTextFromDocumentXml(xmlString);

      console.log('📄 提取到文本长度:', text.length);
      console.log('📄 文本预览:', text.substring(0, 200));

      return text;

    } catch (error) {
      console.error('📄 XML解析失败:', error);
      // 降级到简单文本提取
      return this.extractSimpleText(xmlString);
    }
  }

  // 增强的文本提取方法
  extractTextFromDocumentXml(xmlString) {
    let text = '';
    let paragraphs = [];

    // 1. 提取段落 <w:p>
    const paragraphPattern = /<w:p[^>]*>(.*?)<\/w:p>/gs;
    let paragraphMatch;

    while ((paragraphMatch = paragraphPattern.exec(xmlString)) !== null) {
      const paragraphXml = paragraphMatch[1];
      const paragraphText = this.extractParagraphText(paragraphXml);

      if (paragraphText.trim()) {
        paragraphs.push(paragraphText.trim());
      }
    }

    // 2. 如果没有找到段落，尝试直接提取文本
    if (paragraphs.length === 0) {
      const directText = this.extractDirectText(xmlString);
      if (directText.trim()) {
        paragraphs.push(directText.trim());
      }
    }

    // 3. 组合段落
    text = paragraphs.join('\n\n');

    // 4. 清理和格式化
    text = this.cleanExtractedText(text);

    return text;
  }

  // 提取段落中的文本
  extractParagraphText(paragraphXml) {
    let paragraphText = '';

    // 提取运行 <w:r> 中的文本
    const runPattern = /<w:r[^>]*>(.*?)<\/w:r>/gs;
    let runMatch;

    while ((runMatch = runPattern.exec(paragraphXml)) !== null) {
      const runXml = runMatch[1];
      const runText = this.extractRunText(runXml);
      paragraphText += runText;
    }

    // 如果没有找到运行，直接提取文本节点
    if (!paragraphText.trim()) {
      paragraphText = this.extractDirectText(paragraphXml);
    }

    return paragraphText;
  }

  // 提取运行中的文本
  extractRunText(runXml) {
    let runText = '';

    // 提取文本节点 <w:t>
    const textPattern = /<w:t[^>]*>(.*?)<\/w:t>/gs;
    let textMatch;

    while ((textMatch = textPattern.exec(runXml)) !== null) {
      if (textMatch[1]) {
        // 解码XML实体并修复编码
        let textContent = this.decodeXmlEntities(textMatch[1]);
        textContent = this.fixTextEncoding(textContent);
        runText += textContent;
      }
    }

    // 处理制表符 <w:tab/>
    runText = runText.replace(/<w:tab\s*\/>/g, '\t');

    // 处理换行 <w:br/>
    runText = runText.replace(/<w:br\s*\/>/g, '\n');

    return runText;
  }

  // 直接提取文本（降级方法）
  extractDirectText(xmlString) {
    let text = '';

    // 提取所有 <w:t> 标签中的文本
    const textPattern = /<w:t[^>]*>(.*?)<\/w:t>/gs;
    let match;

    while ((match = textPattern.exec(xmlString)) !== null) {
      if (match[1]) {
        // 解码XML实体并修复编码
        let textContent = this.decodeXmlEntities(match[1]);
        textContent = this.fixTextEncoding(textContent);
        text += textContent + ' ';
      }
    }

    // 如果没有找到w:t标签，尝试更宽泛的提取
    if (!text.trim()) {
      console.log('📄 未找到w:t标签，尝试宽泛提取');
      const allTextMatches = xmlString.match(/>([^<]+)</g);
      if (allTextMatches) {
        for (const textMatch of allTextMatches) {
          const content = textMatch.replace(/^>/, '').replace(/<$/, '').trim();
          if (content && content.length > 1 && !content.match(/^[a-zA-Z]+$/)) {
            let textContent = this.decodeXmlEntities(content);
            textContent = this.fixTextEncoding(textContent);
            text += textContent + ' ';
          }
        }
      }
    }

    return text;
  }

  // 清理提取的文本
  cleanExtractedText(text) {
    if (!text) return '';

    console.log('📄 开始清理文本，原始长度:', text.length);

    // 1. 处理编码问题 - 增强的编码修复
    try {
      text = this.fixTextEncoding(text);
    } catch (e) {
      console.log('📄 编码转换失败，使用原始文本:', e.message);
    }

    // 2. 解码XML实体
    text = text
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&amp;/g, '&')
      .replace(/&quot;/g, '"')
      .replace(/&apos;/g, "'");

    // 3. 清理多余的空白
    text = text
      .replace(/\s+/g, ' ')  // 多个空格合并为一个
      .replace(/\n\s+/g, '\n')  // 清理行首空格
      .replace(/\s+\n/g, '\n')  // 清理行尾空格
      .replace(/\n{3,}/g, '\n\n');  // 多个换行合并为两个

    // 4. 清理首尾空白
    text = text.trim();

    return text;
  }

  // 简单文本提取（最后的降级方法）
  extractSimpleText(xmlString) {
    const textPattern = /<w:t[^>]*>([^<]*)<\/w:t>/g;
    let match;
    let text = '';
    let textCount = 0;

    try {
      while ((match = textPattern.exec(xmlString)) !== null) {
        if (match[1]) {
          text += match[1] + ' ';
          textCount++;
        }
      }

      console.log('📄 找到w:t标签数量:', textCount);

      // 如果没有找到w:t标签，尝试其他模式
      if (!text.trim()) {
        console.log('📄 未找到w:t标签，尝试其他模式');

        // 尝试更宽泛的文本提取模式
        const alternativePatterns = [
          /<w:t>([^<]+)<\/w:t>/g,
          /<text[^>]*>([^<]+)<\/text>/g,
          /<w:t[^>]*>([^<]+)<\/w:t>/g
        ];

        for (const pattern of alternativePatterns) {
          const matches = xmlString.match(pattern);
          if (matches) {
            console.log('📄 找到匹配模式，数量:', matches.length);
            matches.forEach(match => {
              const content = match.replace(/<[^>]*>/g, '').trim();
              if (content && content.length > 0) {
                text += content + ' ';
              }
            });
            if (text.trim()) break;
          }
        }
      }

      // 如果仍然没有文本，尝试直接提取所有可能的文本内容
      if (!text.trim()) {
        console.log('📄 尝试直接文本提取');

        // 移除所有XML标签，提取纯文本
        let cleanText = xmlString.replace(/<[^>]*>/g, ' ');

        // 提取可能的文本片段
        const textFragments = cleanText.match(/[\u4e00-\u9fa5a-zA-Z0-9\s]{3,}/g);
        if (textFragments) {
          textFragments.forEach(fragment => {
            const cleaned = fragment.trim();
            if (cleaned.length > 2 && !cleaned.match(/^[0-9\s\-_\.]+$/)) {
              text += cleaned + ' ';
            }
          });
        }
      }

      // 处理段落分隔
      text = text.replace(/\s+/g, ' ').trim();

      console.log('📄 最终提取文本长度:', text.length);
      console.log('📄 文本预览:', text.substring(0, 100));

    } catch (error) {
      console.log('📄 简单文本提取失败:', error.message);
    }

    return text;
  }

  // 按段落重新组织文本
  reorganizeTextByParagraphs(xmlString) {
    const paragraphs = [];

    console.log('📄 开始段落重组');

    // 提取每个段落的文本
    const paragraphPattern = /<w:p[^>]*>(.*?)<\/w:p>/gs;
    let match;
    let paragraphCount = 0;

    while ((match = paragraphPattern.exec(xmlString)) !== null) {
      const paragraphXml = match[1];
      paragraphCount++;

      // 尝试多种文本提取模式
      const textPatterns = [
        /<w:t[^>]*>([^<]*)<\/w:t>/g,
        /<w:t>([^<]+)<\/w:t>/g
      ];

      let paragraphText = '';

      for (const textPattern of textPatterns) {
        let textMatch;
        while ((textMatch = textPattern.exec(paragraphXml)) !== null) {
          if (textMatch[1]) {
            paragraphText += textMatch[1];
          }
        }
        if (paragraphText.trim()) break;
      }

      // 如果没有找到文本，尝试直接提取
      if (!paragraphText.trim()) {
        const cleanText = paragraphXml.replace(/<[^>]*>/g, ' ');
        const textFragments = cleanText.match(/[\u4e00-\u9fa5a-zA-Z0-9\s]{2,}/g);
        if (textFragments) {
          paragraphText = textFragments.join(' ').trim();
        }
      }

      if (paragraphText.trim()) {
        paragraphs.push(paragraphText.trim());
      }
    }

    console.log('📄 处理段落数量:', paragraphCount, '有效段落:', paragraphs.length);

    return paragraphs.join('\n\n');
  }

  // 直接解析方法（最后的回退）
  extractWithDirectParsing(buffer) {
    const bufferStr = buffer.toString('binary');
    let text = '';

    // 查找所有可能的文本内容
    const patterns = [
      /w:t[^>]*>([^<]+)</g,
      /<w:t>([^<]+)<\/w:t>/g,
      /<text[^>]*>([^<]+)<\/text>/g,
      />([^<]{4,})</g
    ];

    for (const pattern of patterns) {
      const matches = bufferStr.match(pattern);
      if (matches) {
        matches.forEach(match => {
          const content = match.replace(/<[^>]*>/g, '').replace(/^>/, '').trim();
          if (content && content.length > 3) {
            // 过滤掉明显的非文本内容
            if (!content.match(/^[0-9\s\-_\.#]+$/) &&
                !content.match(/^[A-Z]{2,}$/) &&
                (content.match(/[\u4e00-\u9fa5]/) || content.match(/[a-zA-Z]{3,}/))) {
              text += content + ' ';
            }
          }
        });
        if (text.trim()) break;
      }
    }

    return text.replace(/\s+/g, ' ').trim();
  }

  // 增强的文本编码修复方法
  fixTextEncoding(text) {
    if (!text) return '';

    // 检测常见的编码问题模式
    const encodingIssues = [
      { pattern: /Ã¢â‚¬â„¢/g, replacement: "'" },  // 右单引号
      { pattern: /Ã¢â‚¬Å"/g, replacement: '"' },    // 左双引号
      { pattern: /Ã¢â‚¬\u009d/g, replacement: '"' }, // 右双引号
      { pattern: /Ã¢â‚¬â€œ/g, replacement: '—' },   // 长破折号
      { pattern: /Ã¢â‚¬â€/g, replacement: '–' },    // 短破折号
      { pattern: /Ã¯Â¿Â½/g, replacement: '' },      // 替换字符
      { pattern: /Â/g, replacement: '' },           // 非断行空格问题
      { pattern: /â€™/g, replacement: "'" },        // 右单引号
      { pattern: /â€œ/g, replacement: '"' },        // 左双引号
      { pattern: /â€\u009d/g, replacement: '"' },   // 右双引号
      { pattern: /â€"/g, replacement: '—' },        // 长破折号
      { pattern: /â€"/g, replacement: '–' }         // 短破折号
    ];

    let fixedText = text;

    // 应用编码修复规则
    for (const issue of encodingIssues) {
      if (fixedText.match(issue.pattern)) {
        console.log('📄 修复编码问题:', issue.pattern);
        fixedText = fixedText.replace(issue.pattern, issue.replacement);
      }
    }

    // 如果检测到可能的Latin-1编码问题
    if (fixedText.includes('Ã') || fixedText.includes('â') || fixedText.includes('é')) {
      try {
        console.log('📄 尝试Latin-1到UTF-8转换');
        const buffer = Buffer.from(fixedText, 'latin1');
        const utf8Text = buffer.toString('utf8');

        // 验证转换结果是否更好
        if (utf8Text && !utf8Text.includes('\uFFFD') && utf8Text.length > 0) {
          // 检查是否减少了乱码字符
          const originalBadChars = (fixedText.match(/[ÃâéÂ]/g) || []).length;
          const convertedBadChars = (utf8Text.match(/[ÃâéÂ]/g) || []).length;

          if (convertedBadChars < originalBadChars) {
            console.log('📄 Latin-1转换成功，乱码字符减少:', originalBadChars, '->', convertedBadChars);
            fixedText = utf8Text;
          }
        }
      } catch (e) {
        console.warn('📄 Latin-1转换失败:', e.message);
      }
    }

    // 解码XML/HTML实体
    fixedText = this.decodeXmlEntities(fixedText);

    return fixedText;
  }

  // 解码XML/HTML实体
  decodeXmlEntities(text) {
    if (!text) return '';

    return text
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&amp;/g, '&')
      .replace(/&quot;/g, '"')
      .replace(/&apos;/g, "'")
      .replace(/&#(\d+);/g, (match, dec) => {
        try {
          return String.fromCharCode(parseInt(dec, 10));
        } catch (e) {
          return match;
        }
      })
      .replace(/&#x([0-9a-fA-F]+);/g, (match, hex) => {
        try {
          return String.fromCharCode(parseInt(hex, 16));
        } catch (e) {
          return match;
        }
      });
  }

  // 清理DOCX文本 - 增强版
  cleanDocxText(text) {
    if (!text) return '';

    console.log('📄 开始清理文本，原始长度:', text.length);

    // 1. 移除XML残留标签和属性
    text = text
      .replace(/<[^>]*>/g, ' ')  // 移除所有XML标签
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&amp;/g, '&')
      .replace(/&quot;/g, '"')
      .replace(/&apos;/g, "'");

    // 2. 清理特殊字符和控制字符
    text = text
      .replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, '')  // 移除控制字符
      .replace(/\uFEFF/g, '')  // 移除BOM
      .replace(/\u200B/g, '');  // 移除零宽空格

    // 3. 处理空白字符
    text = text
      .replace(/[ \t]+/g, ' ')  // 合并空格和制表符
      .replace(/\r\n/g, '\n')   // 统一换行符
      .replace(/\r/g, '\n')     // 统一换行符
      .replace(/\n[ \t]+/g, '\n')  // 清理行首空白
      .replace(/[ \t]+\n/g, '\n')  // 清理行尾空白
      .replace(/\n{3,}/g, '\n\n'); // 合并多个换行为双换行

    // 4. 清理无意义的文本片段
    const lines = text.split('\n');
    const cleanedLines = lines.filter(line => {
      const trimmed = line.trim();

      // 过滤掉过短的行（可能是XML残留）
      if (trimmed.length < 2) return false;

      // 过滤掉纯数字或特殊字符的行
      if (/^[\d\s\-_\.#]+$/.test(trimmed)) return false;

      // 过滤掉看起来像XML属性的行
      if (/^[A-Z]{2,}$/.test(trimmed) && trimmed.length < 10) return false;

      // 保留包含中文或有意义英文的行
      if (/[\u4e00-\u9fa5]/.test(trimmed) || /[a-zA-Z]{3,}/.test(trimmed)) {
        return true;
      }

      return false;
    });

    // 5. 重新组合文本
    text = cleanedLines.join('\n');

    // 6. 最终清理
    text = text
      .replace(/\n\s*\n/g, '\n\n')  // 规范化段落分隔
      .trim();

    console.log('📄 清理完成，最终长度:', text.length);
    console.log('📄 清理后预览:', text.substring(0, 200));

    return text;
  }

  // 生成DOCX的HTML格式
  generateDocxHtml(text) {
    if (!text) return '<div class="docx-content">无内容</div>';

    // 将段落转换为HTML
    const paragraphs = text.split(/\n\s*\n/);
    const htmlParagraphs = paragraphs.map(p => {
      const trimmed = p.trim();
      if (!trimmed) return '';

      // 检测标题（简单的启发式方法）
      if (trimmed.length < 50 && (
        trimmed.match(/^第[一二三四五六七八九十\d]+[章节条]/) ||
        trimmed.match(/^\d+\./) ||
        trimmed.match(/^[A-Z\u4e00-\u9fa5]{2,20}$/)
      )) {
        return `<h3>${trimmed}</h3>`;
      }

      return `<p>${trimmed}</p>`;
    }).filter(p => p);

    return `<div class="docx-content">${htmlParagraphs.join('\n')}</div>`;
  }
}

module.exports = PluginEcosystemManager;
