# Redis本地安装指南（无Docker环境）

## 🎯 方案概述

由于公司环境不支持Docker，我们提供多种Redis替代方案，确保QMS-AI系统的缓存功能正常运行。

## 📋 方案对比

| 方案 | 优势 | 劣势 | 推荐度 |
|------|------|------|--------|
| **本地Redis** | 性能最佳，功能完整 | 需要安装配置 | ⭐⭐⭐⭐⭐ |
| **内存缓存增强** | 零依赖，简单 | 重启丢失数据 | ⭐⭐⭐⭐ |
| **文件缓存** | 数据持久化 | 性能较低 | ⭐⭐⭐ |
| **云Redis服务** | 免维护 | 需要网络，可能有成本 | ⭐⭐⭐⭐ |

---

## 🚀 方案一：Windows本地Redis安装

### 1. 下载Redis for Windows

```bash
# 方式1：使用Chocolatey（推荐）
choco install redis-64

# 方式2：使用Scoop
scoop install redis

# 方式3：手动下载
# 访问: https://github.com/tporadowski/redis/releases
# 下载: Redis-x64-5.0.14.1.msi
```

### 2. 启动Redis服务

```bash
# 启动Redis服务器
redis-server

# 或者指定配置文件
redis-server redis.conf

# 后台运行（Windows服务）
redis-server --service-install
redis-server --service-start
```

### 3. 验证Redis安装

```bash
# 连接Redis客户端
redis-cli

# 测试连接
127.0.0.1:6379> ping
PONG

# 设置测试数据
127.0.0.1:6379> set test "Hello Redis"
OK

# 获取测试数据
127.0.0.1:6379> get test
"Hello Redis"
```

---

## 🔧 方案二：增强内存缓存（零依赖）

### 特点
- ✅ 无需外部依赖
- ✅ 启动速度快
- ✅ 配置简单
- ❌ 重启后数据丢失
- ❌ 无法跨进程共享

### 实现方案
```javascript
// 增强版内存缓存，支持持久化
class EnhancedMemoryCache {
  constructor(options = {}) {
    this.cache = new Map();
    this.ttlMap = new Map();
    this.persistFile = options.persistFile || './cache/memory-cache.json';
    this.autoSave = options.autoSave !== false;
    this.saveInterval = options.saveInterval || 60000; // 1分钟
    
    this.loadFromFile();
    if (this.autoSave) {
      this.startAutoSave();
    }
  }
  
  // 支持TTL的设置
  set(key, value, ttl = 0) {
    this.cache.set(key, value);
    if (ttl > 0) {
      this.ttlMap.set(key, Date.now() + ttl * 1000);
    }
    return true;
  }
  
  // 获取数据，自动检查过期
  get(key) {
    if (this.ttlMap.has(key)) {
      if (Date.now() > this.ttlMap.get(key)) {
        this.delete(key);
        return null;
      }
    }
    return this.cache.get(key) || null;
  }
  
  // 持久化到文件
  saveToFile() {
    const data = {
      cache: Array.from(this.cache.entries()),
      ttl: Array.from(this.ttlMap.entries()),
      timestamp: Date.now()
    };
    fs.writeFileSync(this.persistFile, JSON.stringify(data, null, 2));
  }
  
  // 从文件加载
  loadFromFile() {
    try {
      if (fs.existsSync(this.persistFile)) {
        const data = JSON.parse(fs.readFileSync(this.persistFile, 'utf8'));
        this.cache = new Map(data.cache);
        this.ttlMap = new Map(data.ttl);
        console.log('✅ 内存缓存已从文件恢复');
      }
    } catch (error) {
      console.warn('⚠️ 缓存文件加载失败:', error.message);
    }
  }
}
```

---

## 🌐 方案三：云Redis服务

### 阿里云Redis
```javascript
const redis = require('redis');
const client = redis.createClient({
  host: 'your-redis-instance.redis.rds.aliyuncs.com',
  port: 6379,
  password: 'your-password'
});
```

### 腾讯云Redis
```javascript
const client = redis.createClient({
  host: 'your-redis-instance.tencentcloudapi.com',
  port: 6379,
  password: 'your-password'
});
```

---

## 📁 方案四：文件缓存系统

### 特点
- ✅ 数据持久化
- ✅ 无需外部服务
- ✅ 支持TTL
- ❌ 性能相对较低
- ❌ 并发访问需要锁机制

### 实现示例
```javascript
class FileCacheService {
  constructor(cacheDir = './cache') {
    this.cacheDir = cacheDir;
    this.ensureCacheDir();
  }
  
  set(key, value, ttl = 0) {
    const filePath = path.join(this.cacheDir, `${key}.json`);
    const data = {
      value,
      timestamp: Date.now(),
      ttl: ttl > 0 ? Date.now() + ttl * 1000 : 0
    };
    fs.writeFileSync(filePath, JSON.stringify(data));
  }
  
  get(key) {
    const filePath = path.join(this.cacheDir, `${key}.json`);
    if (!fs.existsSync(filePath)) return null;
    
    const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    if (data.ttl > 0 && Date.now() > data.ttl) {
      fs.unlinkSync(filePath);
      return null;
    }
    return data.value;
  }
}
```

---

## 🎯 推荐实施方案

### 阶段一：立即实施（内存缓存增强）
1. 实现增强版内存缓存
2. 支持数据持久化到文件
3. 保持Redis接口兼容性

### 阶段二：中期优化（本地Redis）
1. 安装本地Redis服务
2. 迁移到Redis缓存
3. 性能和功能全面提升

### 阶段三：长期规划（云服务）
1. 评估云Redis服务
2. 生产环境使用云缓存
3. 实现高可用架构

---

## 📊 性能对比

| 操作 | 内存缓存 | 文件缓存 | 本地Redis | 云Redis |
|------|----------|----------|-----------|---------|
| **读取** | < 1ms | 5-10ms | 1-2ms | 10-50ms |
| **写入** | < 1ms | 10-20ms | 1-2ms | 10-50ms |
| **持久化** | 定期保存 | 实时 | 实时 | 实时 |
| **内存使用** | 高 | 低 | 中 | 无 |

---

## 🔧 配置建议

### 开发环境
```env
# 使用增强内存缓存
CACHE_TYPE=memory
CACHE_PERSIST_FILE=./cache/dev-cache.json
CACHE_AUTO_SAVE=true
```

### 测试环境
```env
# 使用本地Redis
CACHE_TYPE=redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

### 生产环境
```env
# 使用云Redis或本地Redis集群
CACHE_TYPE=redis
REDIS_HOST=your-redis-cluster
REDIS_PORT=6379
REDIS_PASSWORD=your-secure-password
```

这样的方案既解决了Docker限制问题，又提供了灵活的缓存选择。
