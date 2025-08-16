import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { analyticsAPI, recommendationAPI, collaborationAPI, integrationAPI } from '@/api/advanced-features'

export const useAdvancedFeaturesStore = defineStore('advancedFeatures', () => {
  // 状态
  const analytics = ref({
    topics: [],
    behavior: {},
    sentiment: {},
    loading: false
  })

  const recommendations = ref({
    personalized: [],
    popular: [],
    loading: false
  })

  const collaboration = ref({
    teamStats: {},
    sharedConversations: [],
    loading: false
  })

  const integration = ref({
    stats: {},
    moduleHealth: [],
    loading: false
  })

  // 计算属性
  const healthyModulesCount = computed(() => {
    return integration.value.moduleHealth.filter(module => module.healthy).length
  })

  const totalRecommendations = computed(() => {
    return recommendations.value.personalized.length + recommendations.value.popular.length
  })

  // 操作
  const loadAnalytics = async () => {
    analytics.value.loading = true
    try {
      const [topicsRes, behaviorRes, sentimentRes] = await Promise.all([
        analyticsAPI.getTopics(),
        analyticsAPI.getBehavior(),
        analyticsAPI.getSentiment()
      ])

      if (topicsRes.success) analytics.value.topics = topicsRes.data.topics || []
      if (behaviorRes.success) analytics.value.behavior = behaviorRes.data
      if (sentimentRes.success) analytics.value.sentiment = sentimentRes.data
    } catch (error) {
      console.error('加载分析数据失败:', error)
    } finally {
      analytics.value.loading = false
    }
  }

  const loadRecommendations = async () => {
    recommendations.value.loading = true
    try {
      const [personalizedRes, popularRes] = await Promise.all([
        recommendationAPI.getPersonalized(),
        recommendationAPI.getPopular()
      ])

      if (personalizedRes.success) recommendations.value.personalized = personalizedRes.data
      if (popularRes.success) recommendations.value.popular = popularRes.data
    } catch (error) {
      console.error('加载推荐数据失败:', error)
    } finally {
      recommendations.value.loading = false
    }
  }

  return {
    // 状态
    analytics,
    recommendations,
    collaboration,
    integration,

    // 计算属性
    healthyModulesCount,
    totalRecommendations,

    // 操作
    loadAnalytics,
    loadRecommendations
  }
})