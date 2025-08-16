<template>
  <div class="analytics-panel">
    <div class="panel-header">
      <h3>📊 智能分析</h3>
      <span class="phase-badge">Phase 1</span>
    </div>
    
    <div class="analytics-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        :class="['tab-button', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.icon }} {{ tab.label }}
      </button>
    </div>

    <div class="analytics-content">
      <!-- 主题分析 -->
      <div v-if="activeTab === 'topics'" class="analysis-section">
        <div class="loading" v-if="loading.topics">
          <div class="spinner"></div>
          <span>正在分析对话主题...</span>
        </div>
        
        <div v-else-if="analytics.topics" class="topics-analysis">
          <div class="stats-summary">
            <div class="stat-item">
              <span class="stat-value">{{ analytics.topics.total_analyzed }}</span>
              <span class="stat-label">已分析对话</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ analytics.topics.topics.length }}</span>
              <span class="stat-label">识别主题</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ analytics.topics.keywords.length }}</span>
              <span class="stat-label">关键词</span>
            </div>
          </div>

          <div class="topics-grid">
            <div class="topics-chart">
              <h4>🏷️ 主题分布</h4>
              <div class="topic-list">
                <div 
                  v-for="(topic, index) in analytics.topics.topics.slice(0, 8)" 
                  :key="topic.topic"
                  class="topic-item"
                >
                  <span class="topic-name">{{ topic.topic }}</span>
                  <div class="topic-bar">
                    <div 
                      class="topic-fill" 
                      :style="{ width: (topic.count / analytics.topics.topics[0].count * 100) + '%' }"
                    ></div>
                  </div>
                  <span class="topic-count">{{ topic.count }}</span>
                </div>
              </div>
            </div>

            <div class="keywords-cloud">
              <h4>🔍 热门关键词</h4>
              <div class="keyword-list">
                <span 
                  v-for="keyword in analytics.topics.keywords.slice(0, 12)"
                  :key="keyword.word"
                  class="keyword-tag"
                  :style="{ fontSize: Math.max(12, Math.min(20, keyword.count * 2)) + 'px' }"
                >
                  {{ keyword.word }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 行为分析 -->
      <div v-if="activeTab === 'behavior'" class="analysis-section">
        <div class="loading" v-if="loading.behavior">
          <div class="spinner"></div>
          <span>正在分析用户行为...</span>
        </div>
        
        <div v-else-if="analytics.behavior" class="behavior-analysis">
          <div class="behavior-grid">
            <div class="behavior-stats">
              <h4>👤 活动统计</h4>
              <div class="stat-grid">
                <div class="stat-card">
                  <div class="stat-icon">💬</div>
                  <div class="stat-info">
                    <span class="stat-number">{{ analytics.behavior.total_conversations }}</span>
                    <span class="stat-text">总对话数</span>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">📝</div>
                  <div class="stat-info">
                    <span class="stat-number">{{ analytics.behavior.total_messages }}</span>
                    <span class="stat-text">总消息数</span>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">⭐</div>
                  <div class="stat-info">
                    <span class="stat-number">{{ analytics.behavior.rating_patterns?.avg_rating?.toFixed(1) || 'N/A' }}</span>
                    <span class="stat-text">平均评分</span>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">🕐</div>
                  <div class="stat-info">
                    <span class="stat-number">{{ analytics.behavior.peak_hour }}:00</span>
                    <span class="stat-text">活跃时间</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="interests-analysis">
              <h4>🎯 兴趣分析</h4>
              <div class="interest-list">
                <div 
                  v-for="(count, topic) in analytics.behavior.topics_interest"
                  :key="topic"
                  class="interest-item"
                >
                  <span class="interest-topic">{{ topic }}</span>
                  <div class="interest-bar">
                    <div 
                      class="interest-fill"
                      :style="{ width: (count / Math.max(...Object.values(analytics.behavior.topics_interest)) * 100) + '%' }"
                    ></div>
                  </div>
                  <span class="interest-count">{{ count }}次</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 情感分析 -->
      <div v-if="activeTab === 'sentiment'" class="analysis-section">
        <div class="loading" v-if="loading.sentiment">
          <div class="spinner"></div>
          <span>正在分析情感倾向...</span>
        </div>
        
        <div v-else-if="analytics.sentiment" class="sentiment-analysis">
          <div class="sentiment-overview">
            <h4>😊 情感分布</h4>
            <div class="sentiment-chart">
              <div class="sentiment-item positive">
                <div class="sentiment-icon">😊</div>
                <div class="sentiment-info">
                  <span class="sentiment-label">积极</span>
                  <span class="sentiment-percent">{{ analytics.sentiment.positive }}%</span>
                </div>
                <div class="sentiment-bar">
                  <div 
                    class="sentiment-fill positive-fill"
                    :style="{ width: analytics.sentiment.positive + '%' }"
                  ></div>
                </div>
              </div>
              
              <div class="sentiment-item neutral">
                <div class="sentiment-icon">😐</div>
                <div class="sentiment-info">
                  <span class="sentiment-label">中性</span>
                  <span class="sentiment-percent">{{ analytics.sentiment.neutral }}%</span>
                </div>
                <div class="sentiment-bar">
                  <div 
                    class="sentiment-fill neutral-fill"
                    :style="{ width: analytics.sentiment.neutral + '%' }"
                  ></div>
                </div>
              </div>
              
              <div class="sentiment-item negative">
                <div class="sentiment-icon">😔</div>
                <div class="sentiment-info">
                  <span class="sentiment-label">消极</span>
                  <span class="sentiment-percent">{{ analytics.sentiment.negative }}%</span>
                </div>
                <div class="sentiment-bar">
                  <div 
                    class="sentiment-fill negative-fill"
                    :style="{ width: analytics.sentiment.negative + '%' }"
                  ></div>
                </div>
              </div>
            </div>
            
            <div class="sentiment-summary">
              <p class="sentiment-insight">
                基于 {{ analytics.sentiment.total }} 条消息的分析，
                您的整体情感倾向为
                <strong>{{ getSentimentTrend() }}</strong>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="panel-footer">
      <button @click="refreshAnalytics" :disabled="isRefreshing" class="refresh-btn">
        <span v-if="isRefreshing">🔄 刷新中...</span>
        <span v-else>🔄 刷新数据</span>
      </button>
      <span class="last-update">
        最后更新: {{ lastUpdate }}
      </span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AnalyticsPanel',
  data() {
    return {
      activeTab: 'topics',
      tabs: [
        { key: 'topics', label: '主题分析', icon: '🏷️' },
        { key: 'behavior', label: '行为分析', icon: '👤' },
        { key: 'sentiment', label: '情感分析', icon: '😊' }
      ],
      analytics: {
        topics: null,
        behavior: null,
        sentiment: null
      },
      loading: {
        topics: false,
        behavior: false,
        sentiment: false
      },
      isRefreshing: false,
      lastUpdate: ''
    }
  },
  mounted() {
    this.loadAnalytics();
  },
  methods: {
    async loadAnalytics() {
      await Promise.all([
        this.loadTopicsAnalysis(),
        this.loadBehaviorAnalysis(),
        this.loadSentimentAnalysis()
      ]);
      this.lastUpdate = new Date().toLocaleString();
    },
    
    async loadTopicsAnalysis() {
      this.loading.topics = true;
      try {
        const response = await fetch('/api/analytics/topics', {
          headers: this.getAuthHeaders()
        });
        const result = await response.json();
        if (result.success) {
          this.analytics.topics = result.data;
        }
      } catch (error) {
        console.error('加载主题分析失败:', error);
      } finally {
        this.loading.topics = false;
      }
    },
    
    async loadBehaviorAnalysis() {
      this.loading.behavior = true;
      try {
        const response = await fetch('/api/analytics/behavior', {
          headers: this.getAuthHeaders()
        });
        const result = await response.json();
        if (result.success) {
          this.analytics.behavior = result.data;
        }
      } catch (error) {
        console.error('加载行为分析失败:', error);
      } finally {
        this.loading.behavior = false;
      }
    },
    
    async loadSentimentAnalysis() {
      this.loading.sentiment = true;
      try {
        const response = await fetch('/api/analytics/sentiment', {
          headers: this.getAuthHeaders()
        });
        const result = await response.json();
        if (result.success) {
          this.analytics.sentiment = result.data;
        }
      } catch (error) {
        console.error('加载情感分析失败:', error);
      } finally {
        this.loading.sentiment = false;
      }
    },
    
    async refreshAnalytics() {
      this.isRefreshing = true;
      await this.loadAnalytics();
      this.isRefreshing = false;
    },
    
    getSentimentTrend() {
      if (!this.analytics.sentiment) return '未知';
      
      const { positive, neutral, negative } = this.analytics.sentiment;
      const maxValue = Math.max(parseFloat(positive), parseFloat(neutral), parseFloat(negative));
      
      if (maxValue === parseFloat(positive)) return '积极正面';
      if (maxValue === parseFloat(neutral)) return '中性平和';
      return '需要关注';
    },
    
    getAuthHeaders() {
      return {
        'user-id': this.$store?.state?.user?.id || 'demo_user',
        'user-name': this.$store?.state?.user?.name || 'demo_user',
        'user-role': this.$store?.state?.user?.role || 'user'
      };
    }
  }
}
</script>

<style scoped>
.analytics-panel {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.panel-header h3 {
  margin: 0;
  font-size: 1.4em;
}

.phase-badge {
  background: rgba(255,255,255,0.2);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8em;
  font-weight: bold;
}

.analytics-tabs {
  display: flex;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.tab-button {
  flex: 1;
  padding: 15px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.tab-button:hover {
  background: #e9ecef;
}

.tab-button.active {
  background: white;
  border-bottom: 3px solid #667eea;
  color: #667eea;
  font-weight: bold;
}

.analytics-content {
  padding: 20px;
  min-height: 400px;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #6c757d;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 15px;
  margin-bottom: 25px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-value {
  display: block;
  font-size: 2em;
  font-weight: bold;
  color: #667eea;
}

.stat-label {
  font-size: 0.9em;
  color: #6c757d;
}

.topics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;
}

.topic-item, .interest-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.topic-name, .interest-topic {
  width: 100px;
  font-size: 0.9em;
  color: #495057;
}

.topic-bar, .interest-bar {
  flex: 1;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  margin: 0 10px;
  overflow: hidden;
}

.topic-fill, .interest-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.topic-count, .interest-count {
  font-size: 0.8em;
  color: #6c757d;
  min-width: 30px;
  text-align: right;
}

.keyword-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: bold;
  transition: transform 0.2s ease;
}

.keyword-tag:hover {
  transform: scale(1.05);
}

.behavior-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;
}

.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-icon {
  font-size: 2em;
  margin-right: 15px;
}

.stat-number {
  display: block;
  font-size: 1.5em;
  font-weight: bold;
  color: #667eea;
}

.stat-text {
  font-size: 0.8em;
  color: #6c757d;
}

.sentiment-chart {
  margin-bottom: 20px;
}

.sentiment-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.sentiment-icon {
  font-size: 1.5em;
  margin-right: 15px;
}

.sentiment-info {
  width: 80px;
  margin-right: 15px;
}

.sentiment-label {
  display: block;
  font-size: 0.9em;
  color: #495057;
}

.sentiment-percent {
  display: block;
  font-weight: bold;
  color: #667eea;
}

.sentiment-bar {
  flex: 1;
  height: 12px;
  background: #e9ecef;
  border-radius: 6px;
  overflow: hidden;
}

.sentiment-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.5s ease;
}

.positive-fill { background: #28a745; }
.neutral-fill { background: #6c757d; }
.negative-fill { background: #dc3545; }

.sentiment-summary {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

.sentiment-insight {
  margin: 0;
  color: #495057;
  line-height: 1.6;
}

.panel-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.refresh-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-2px);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.last-update {
  font-size: 0.8em;
  color: #6c757d;
}
</style>
