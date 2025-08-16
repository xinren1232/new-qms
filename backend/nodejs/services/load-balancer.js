
// 简单的负载均衡器
class LoadBalancer {
  constructor(servers) {
    this.servers = servers;
    this.currentIndex = 0;
  }
  
  // 轮询算法
  getNextServer() {
    const server = this.servers[this.currentIndex];
    this.currentIndex = (this.currentIndex + 1) % this.servers.length;
    return server;
  }
  
  // 健康检查
  async healthCheck() {
    const healthyServers = [];
    
    for (const server of this.servers) {
      try {
        const response = await fetch(`${server}/health`);
        if (response.ok) {
          healthyServers.push(server);
        }
      } catch (error) {
        console.warn(`服务器 ${server} 健康检查失败`);
      }
    }
    
    this.servers = healthyServers;
    return healthyServers;
  }
}

module.exports = LoadBalancer;
