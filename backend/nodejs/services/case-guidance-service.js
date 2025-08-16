/**
 * 案例异常指导服务（最小可用版）
 * 功能：
 *  - 导入Excel案例（来料/制造/自定义分类）
 *  - 简单检索（SQLite LIKE + 关键词权重）
 *  - 生成结构化指导输出（无需模型即可返回，后续可接入模型增强）
 */

const path = require('path');
const fs = require('fs');
// const XLSX = require('xlsx'); // Excel功能暂时禁用，npm有问题
const { v4: uuidv4 } = require('uuid');

class CaseGuidanceService {
  constructor(dbAdapter, options = {}) {
    this.db = dbAdapter;
    this.options = { maxImportRows: 5000, ...options };
    this.initialized = false;
  }

  async initialize() {
    if (this.initialized) return true;
    await this.ensureSchema();
    this.initialized = true;
    return true;
  }

  async ensureSchema() {
    // 案例主表
    await this.db.run(
      `CREATE TABLE IF NOT EXISTS case_library (
        id TEXT PRIMARY KEY,
        category TEXT,
        title TEXT,
        symptom TEXT,
        cause TEXT,
        solution TEXT,
        tags TEXT,
        source TEXT,
        created_at TEXT DEFAULT (datetime('now')),
        updated_at TEXT DEFAULT (datetime('now')),
        is_deleted INTEGER DEFAULT 0
      )`
    );

    await this.db.run(
      `CREATE INDEX IF NOT EXISTS idx_case_category ON case_library(category)`
    );
    await this.db.run(
      `CREATE INDEX IF NOT EXISTS idx_case_search ON case_library(title, symptom, cause, solution, tags)`
    );
  }

  /**
   * 从Excel导入案例
   * 允许列名（模糊匹配）：
   *  - 标题/问题/主题
   *  - 现象/症状/描述
   *  - 原因/根因/分析
   *  - 解决/措施/对策/建议
   *  - 标签/工序/供应商/分类
   */
  async importFromExcel(buffer, filename, category = 'general', source = 'upload') {
    await this.initialize();

    // Excel功能暂时禁用，返回友好错误信息
    throw new Error('Excel导入功能暂时不可用，npm环境需要修复');

    // const workbook = XLSX.read(buffer, { type: 'buffer' });
    // const sheetName = workbook.SheetNames[0];
    // const sheet = workbook.Sheets[sheetName];
    // const rows = XLSX.utils.sheet_to_json(sheet, { defval: '' });

    const cases = [];
    const limit = Math.min(rows.length, this.options.maxImportRows);

    for (let i = 0; i < limit; i++) {
      const row = rows[i];

      const getByKeys = (obj, keys) => {
        for (const k of keys) {
          const hit = Object.keys(obj).find(h => h.toLowerCase().includes(k));
          if (hit && obj[hit]) return String(obj[hit]);
        }
        return '';
      };

      const title = getByKeys(row, ['标题', '问题', '主题', '简述']);
      const symptom = getByKeys(row, ['现象', '症状', '描述', '问题描述', '异常']);
      const cause = getByKeys(row, ['原因', '根因', '分析', '原因分析']);
      const solution = getByKeys(row, ['解决', '措施', '对策', '整改', '建议', '处理']);
      const tags = getByKeys(row, ['标签', '工序', '供应商', '分类', '类型']);

      // 跳过空白行
      if (!(title || symptom || cause || solution)) continue;

      cases.push({
        id: uuidv4(),
        category,
        title: title || (symptom?.slice(0, 30) || '未命名案例'),
        symptom,
        cause,
        solution,
        tags,
        source: `${source}:${filename}`,
      });
    }

    // 批量写入
    for (const item of cases) {
      await this.db.run(
        `INSERT OR REPLACE INTO case_library
         (id, category, title, symptom, cause, solution, tags, source, updated_at)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))`,
        [
          item.id,
          item.category,
          item.title,
          item.symptom,
          item.cause,
          item.solution,
          item.tags,
          item.source
        ]
      );
    }

    return { success: true, count: cases.length };
  }

  /**
   * 简单检索：LIKE + 打分
   */
  async searchCases(query, { category, limit = 8 } = {}) {
    await this.initialize();
    const q = `%${query}%`;

    const params = [q, q, q, q, q];
    let sql = `SELECT * FROM case_library 
               WHERE is_deleted = 0 AND (title LIKE ? OR symptom LIKE ? OR cause LIKE ? OR solution LIKE ? OR tags LIKE ?)`;
    if (category) {
      sql += ' AND category = ?';
    }
    sql += ' LIMIT ?';

    const rows = await this.db.query(sql, category ? [...params, category, limit * 3] : [...params, limit * 3]);

    // 简单打分：字段权重
    const words = (query || '').toLowerCase().split(/\s+|,|，|、/).filter(Boolean);
    const scoreRow = (r) => {
      const text = `${r.title}\n${r.symptom}\n${r.cause}\n${r.solution}\n${r.tags}`.toLowerCase();
      let score = 0;
      for (const w of words) {
        if (!w) continue;
        const occur = (text.match(new RegExp(w.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g')) || []).length;
        score += occur * 2;
        if (r.title?.toLowerCase().includes(w)) score += 5;
        if (r.symptom?.toLowerCase().includes(w)) score += 3;
        if (r.cause?.toLowerCase().includes(w)) score += 2;
      }
      return score;
    };

    const ranked = rows
      .map(r => ({ ...r, score: scoreRow(r) }))
      .sort((a, b) => b.score - a.score)
      .slice(0, limit);

    return ranked;
  }

  /**
   * 生成经验指导（模板化）
   */
  generateGuidance(query, cases) {
    if (!cases || cases.length === 0) {
      return {
        summary: '未检索到相关历史案例，请提供更具体的异常现象或关键词（如工序、材料、供应商、故障码等）。',
        checklist: [
          '确认异常现象和触发条件（时间/工位/批次）',
          '收集相关数据（来料批次/制造参数/不良图片）',
          '检查是否为已知问题（供应商变更/工艺调整/材料批次差异）'
        ],
        cases: []
      };
    }

    const top = cases[0];
    const distinctCauses = Array.from(new Set(cases.map(c => c.cause).filter(Boolean))).slice(0, 5);
    const distinctActions = Array.from(new Set(cases.map(c => c.solution).filter(Boolean))).slice(0, 5);

    return {
      summary: `依据历史共${cases.length}条相似案例，优先关注："${top.title}"。常见根因：${distinctCauses.map(s=>`【${s.slice(0,40)}】`).join('、')}。推荐先行措施：${distinctActions.map(s=>`【${s.slice(0,40)}】`).join('、')}。`,
      checklist: [
        '复现实验：在相同工艺/环境下复现异常，记录参数窗口',
        '逐项排查：原料 → 制程参数 → 设备 → 治具 → 人员操作',
        '数据对比：异常批次 vs 正常批次（关键CTQ、SPC趋势）',
        '快速遏制：临时性围堵/加严检验/替代材料',
        '根因验证：单点变量确认，给出验证数据'
      ],
      cases: cases.map(c => ({
        id: c.id,
        title: c.title,
        symptom: c.symptom,
        cause: c.cause,
        solution: c.solution,
        tags: c.tags
      }))
    };
  }
}

module.exports = CaseGuidanceService;

