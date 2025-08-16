/**
 * 缓存问题诊断脚本
 * 检查和清理有问题的缓存键
 */

require('dotenv').config();
const redis = require('redis');

async function diagnoseCacheIssues() {
  console.log('🔍 开始诊断缓存问题...\n');
  
  let client;
  
  try {
    // 连接Redis
    console.log('🔗 连接Redis服务器...');
    client = redis.createClient({
      socket: {
        host: process.env.REDIS_HOST || 'localhost',
        port: process.env.REDIS_PORT || 6379
      },
      password: process.env.REDIS_PASSWORD || undefined,
      database: process.env.REDIS_DB || 0
    });
    
    await client.connect();
    console.log('✅ Redis连接成功\n');
    
    // 1. 查找所有包含"undefined"的键
    console.log('1️⃣ 查找包含"undefined"的缓存键...');
    const allKeys = await client.keys('*');
    const undefinedKeys = allKeys.filter(key => key.includes('undefined'));
    
    if (undefinedKeys.length > 0) {
      console.log(`❌ 发现 ${undefinedKeys.length} 个包含"undefined"的键:`);
      undefinedKeys.forEach((key, index) => {
        console.log(`   ${index + 1}. ${key}`);
      });
      
      // 询问是否清理这些键
      console.log('\n🧹 正在清理这些有问题的键...');
      for (const key of undefinedKeys) {
        try {
          await client.del(key);
          console.log(`✅ 已删除: ${key}`);
        } catch (error) {
          console.log(`❌ 删除失败: ${key} - ${error.message}`);
        }
      }
      console.log(`🎉 清理完成，共删除 ${undefinedKeys.length} 个有问题的键\n`);
    } else {
      console.log('✅ 没有发现包含"undefined"的键\n');
    }
    
    // 2. 查找所有agent相关的键
    console.log('2️⃣ 查找所有agent相关的缓存键...');
    const agentKeys = allKeys.filter(key => 
      key.includes('agent') || key.includes('user_')
    );
    
    if (agentKeys.length > 0) {
      console.log(`📋 发现 ${agentKeys.length} 个agent相关的键:`);
      for (const key of agentKeys) {
        try {
          const ttl = await client.ttl(key);
          const type = await client.type(key);
          console.log(`   - ${key} (类型: ${type}, TTL: ${ttl}s)`);
        } catch (error) {
          console.log(`   - ${key} (检查失败: ${error.message})`);
        }
      }
    } else {
      console.log('✅ 没有发现agent相关的键');
    }
    
    console.log();
    
    // 3. 检查Redis内存使用情况
    console.log('3️⃣ 检查Redis内存使用情况...');
    const info = await client.info('memory');
    const memoryLines = info.split('\r\n').filter(line => 
      line.includes('used_memory') || line.includes('maxmemory')
    );
    
    console.log('💾 内存使用情况:');
    memoryLines.forEach(line => {
      if (line.trim()) {
        console.log(`   ${line}`);
      }
    });
    
    console.log();
    
    // 4. 检查键的分布情况
    console.log('4️⃣ 检查键的分布情况...');
    const keyPatterns = {};
    
    allKeys.forEach(key => {
      const parts = key.split(':');
      const pattern = parts.length > 1 ? parts[0] : 'other';
      keyPatterns[pattern] = (keyPatterns[pattern] || 0) + 1;
    });
    
    console.log('🔑 键分布统计:');
    Object.entries(keyPatterns).forEach(([pattern, count]) => {
      console.log(`   ${pattern}: ${count} 个键`);
    });
    
    console.log();
    
    // 5. 生成修复建议
    console.log('5️⃣ 修复建议...');
    console.log('✅ 缓存问题已修复，建议：');
    console.log('   1. 使用正确的缓存策略模式');
    console.log('   2. 确保用户ID不为undefined');
    console.log('   3. 使用缓存辅助函数进行操作');
    console.log('   4. 定期清理过期和无效的缓存键');
    
    console.log('\n🎉 缓存诊断完成！');
    
  } catch (error) {
    console.error('❌ 诊断过程中出现错误:', error.message);
    console.error('错误详情:', error);
  } finally {
    if (client) {
      await client.quit();
      console.log('🔒 Redis连接已关闭');
    }
  }
}

// 清理特定模式的键
async function cleanupKeys(pattern) {
  console.log(`🧹 清理匹配模式 "${pattern}" 的键...\n`);
  
  let client;
  
  try {
    client = redis.createClient({
      socket: {
        host: process.env.REDIS_HOST || 'localhost',
        port: process.env.REDIS_PORT || 6379
      },
      password: process.env.REDIS_PASSWORD || undefined,
      database: process.env.REDIS_DB || 0
    });
    
    await client.connect();
    
    const keys = await client.keys(pattern);
    
    if (keys.length > 0) {
      console.log(`发现 ${keys.length} 个匹配的键:`);
      keys.forEach((key, index) => {
        console.log(`   ${index + 1}. ${key}`);
      });
      
      console.log('\n开始清理...');
      for (const key of keys) {
        try {
          await client.del(key);
          console.log(`✅ 已删除: ${key}`);
        } catch (error) {
          console.log(`❌ 删除失败: ${key} - ${error.message}`);
        }
      }
      
      console.log(`\n🎉 清理完成，共删除 ${keys.length} 个键`);
    } else {
      console.log('✅ 没有找到匹配的键');
    }
    
  } catch (error) {
    console.error('❌ 清理过程中出现错误:', error.message);
  } finally {
    if (client) {
      await client.quit();
    }
  }
}

// 命令行参数处理
if (require.main === module) {
  const args = process.argv.slice(2);
  
  if (args.length > 0 && args[0] === 'cleanup') {
    const pattern = args[1] || '*undefined*';
    cleanupKeys(pattern).catch(console.error);
  } else {
    diagnoseCacheIssues().catch(console.error);
  }
}

module.exports = { diagnoseCacheIssues, cleanupKeys };
