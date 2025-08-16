
// Redis缓存服务 (可选，如果没有Redis则使用内存缓存)
class CacheService {
  constructor() {
    this.memoryCache = new Map();
    this.maxSize = 1000;
    this.ttl = 3600000; // 1小时
  }
  
  async get(key) {
    const item = this.memoryCache.get(key);
    if (!item) return null;
    
    if (Date.now() > item.expiry) {
      this.memoryCache.delete(key);
      return null;
    }
    
    return item.value;
  }
  
  async set(key, value, ttl = this.ttl) {
    // 如果缓存满了，删除最旧的项
    if (this.memoryCache.size >= this.maxSize) {
      const firstKey = this.memoryCache.keys().next().value;
      this.memoryCache.delete(firstKey);
    }
    
    this.memoryCache.set(key, {
      value,
      expiry: Date.now() + ttl
    });
  }
  
  async del(key) {
    this.memoryCache.delete(key);
  }
  
  async clear() {
    this.memoryCache.clear();
  }
}

module.exports = new CacheService();
