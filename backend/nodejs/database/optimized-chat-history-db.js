
// 数据库连接优化
class OptimizedChatHistoryDB extends ChatHistoryDB {
  constructor() {
    super();
    this.connectionPool = new Map();
    this.maxConnections = 10;
  }
  
  async getConnection() {
    // 连接池实现
    if (this.connectionPool.size < this.maxConnections) {
      const conn = new sqlite3.Database(this.dbPath);
      this.connectionPool.set(Date.now(), conn);
      return conn;
    }
    
    // 复用现有连接
    const connections = Array.from(this.connectionPool.values());
    return connections[Math.floor(Math.random() * connections.length)];
  }
  
  async closeConnections() {
    for (const conn of this.connectionPool.values()) {
      conn.close();
    }
    this.connectionPool.clear();
  }
}
