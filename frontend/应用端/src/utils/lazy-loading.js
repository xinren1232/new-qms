// 懒加载工具函数
import { defineAsyncComponent } from 'vue'
import LoadingComponent from '@/components/Loading.vue'
import ErrorComponent from '@/components/Error.vue'

// 创建懒加载组件
export function createLazyComponent(loader) {
  return defineAsyncComponent({
    loader,
    loadingComponent: LoadingComponent,
    errorComponent: ErrorComponent,
    delay: 200,
    timeout: 3000
  })
}

// 预加载组件
export function preloadComponent(loader) {
  return new Promise((resolve, reject) => {
    loader()
      .then(resolve)
      .catch(reject)
  })
}

// 路由懒加载
export const lazyRoutes = {
  Dashboard: () => import('@/views/dashboard/ModernDashboard.vue'),
  Analytics: () => import('@/views/analytics/AnalyticsDashboard.vue'),
  Recommendations: () => import('@/views/recommendations/RecommendationPanel.vue'),
  Collaboration: () => import('@/views/collaboration/CollaborationCenter.vue'),
  Integration: () => import('@/views/integration/IntegrationStatus.vue')
}