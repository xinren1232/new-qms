<template>
  <div class="error-page">
    <div class="error-container">
      <div class="error-icon">
        <el-icon :size="120" color="#e6a23c">
          <QuestionFilled />
        </el-icon>
      </div>
      
      <div class="error-content">
        <h1 class="error-title">404</h1>
        <h2 class="error-subtitle">页面不存在</h2>
        <p class="error-description">
          抱歉，您访问的页面不存在或已被删除。请检查URL是否正确。
        </p>
        
        <div class="error-actions">
          <el-button type="primary" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回上页
          </el-button>
          <el-button @click="goHome">
            <el-icon><House /></el-icon>
            回到首页
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  padding: 20px;
}

.error-container {
  text-align: center;
  background: white;
  border-radius: 16px;
  padding: 60px 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.error-icon {
  margin-bottom: 30px;
}

.error-content {
  .error-title {
    font-size: 72px;
    font-weight: bold;
    color: #e6a23c;
    margin: 0 0 10px 0;
    line-height: 1;
  }
  
  .error-subtitle {
    font-size: 28px;
    color: #303133;
    margin: 0 0 20px 0;
    font-weight: 600;
  }
  
  .error-description {
    font-size: 16px;
    color: #606266;
    margin: 0 0 40px 0;
    line-height: 1.6;
  }
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
  
  .el-button {
    padding: 12px 24px;
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .error-container {
    padding: 40px 20px;
  }
  
  .error-content {
    .error-title {
      font-size: 56px;
    }
    
    .error-subtitle {
      font-size: 24px;
    }
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
    
    .el-button {
      width: 200px;
    }
  }
}
</style>
