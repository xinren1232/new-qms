#!/bin/bash

echo "=== QMS系统 - 使用pnpm修复npm依赖问题 ==="

# 进入项目目录
cd /opt

echo "1. 停止现有容器..."
docker-compose down

echo "2. 清理npm缓存和锁文件..."
# 清理配置服务
cd backend/nodejs
rm -f package-lock.json node_modules -rf
echo "配置服务清理完成"

# 清理聊天服务
cd ../chat-service
rm -f package-lock.json node_modules -rf
echo "聊天服务清理完成"

# 清理认证服务
cd ../auth-service
rm -f package-lock.json node_modules -rf
echo "认证服务清理完成"

# 清理API网关
cd ../api-gateway
rm -f package-lock.json node_modules -rf
echo "API网关清理完成"

# 清理前端应用端
cd ../../frontend/app
rm -f package-lock.json node_modules -rf
echo "前端应用端清理完成"

# 清理前端配置端
cd ../config
rm -f package-lock.json node_modules -rf
echo "前端配置端清理完成"

cd /opt

echo "3. 创建优化的Dockerfile文件..."

# 创建配置服务Dockerfile
cat > backend/nodejs/Dockerfile << 'EOF'
FROM node:18-alpine

WORKDIR /app

# 安装系统依赖和pnpm
RUN apk add --no-cache curl && \
    npm install -g pnpm && \
    rm -rf /var/cache/apk/*

# 复制package文件
COPY package*.json ./
COPY pnpm-lock.yaml* ./

# 安装依赖
RUN pnpm install --prod || npm install --only=production

# 复制源代码
COPY . .

# 创建非root用户
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001 && \
    chown -R nodejs:nodejs /app

USER nodejs

EXPOSE 3003

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:3003/health || exit 1

CMD ["node", "config-center-service.js"]
EOF

# 创建聊天服务Dockerfile
cat > backend/chat-service/Dockerfile << 'EOF'
FROM node:18-alpine

WORKDIR /app

# 安装系统依赖和pnpm
RUN apk add --no-cache curl && \
    npm install -g pnpm && \
    rm -rf /var/cache/apk/*

# 复制package文件
COPY package*.json ./
COPY pnpm-lock.yaml* ./

# 安装依赖
RUN pnpm install --prod || npm install --only=production

# 复制源代码
COPY . .

# 创建非root用户
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001 && \
    chown -R nodejs:nodejs /app

USER nodejs

EXPOSE 3001

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:3001/health || exit 1

CMD ["node", "chat-service.js"]
EOF

# 创建认证服务Dockerfile
cat > backend/auth-service/Dockerfile << 'EOF'
FROM node:18-alpine

WORKDIR /app

# 安装系统依赖和pnpm
RUN apk add --no-cache curl && \
    npm install -g pnpm && \
    rm -rf /var/cache/apk/*

# 复制package文件
COPY package*.json ./
COPY pnpm-lock.yaml* ./

# 安装依赖
RUN pnpm install --prod || npm install --only=production

# 复制源代码
COPY . .

# 创建非root用户
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001 && \
    chown -R nodejs:nodejs /app

USER nodejs

EXPOSE 3002

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:3002/health || exit 1

CMD ["node", "auth-service.js"]
EOF

# 创建API网关Dockerfile
cat > backend/api-gateway/Dockerfile << 'EOF'
FROM node:18-alpine

WORKDIR /app

# 安装系统依赖和pnpm
RUN apk add --no-cache curl && \
    npm install -g pnpm && \
    rm -rf /var/cache/apk/*

# 复制package文件
COPY package*.json ./
COPY pnpm-lock.yaml* ./

# 安装依赖
RUN pnpm install --prod || npm install --only=production

# 复制源代码
COPY . .

# 创建非root用户
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001 && \
    chown -R nodejs:nodejs /app

USER nodejs

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:3000/health || exit 1

CMD ["node", "gateway.js"]
EOF

echo "4. 创建前端Dockerfile..."

# 创建前端应用端Dockerfile
cat > frontend/app/Dockerfile << 'EOF'
# 构建阶段
FROM node:18-alpine as builder

WORKDIR /app

# 安装pnpm
RUN npm install -g pnpm

# 复制package文件
COPY package*.json ./
COPY pnpm-lock.yaml* ./

# 安装依赖
RUN pnpm install || npm install

# 复制源代码
COPY . .

# 构建应用
RUN pnpm build || npm run build

# 生产阶段
FROM nginx:alpine

# 复制构建结果
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
EOF

# 创建前端配置端Dockerfile
cat > frontend/config/Dockerfile << 'EOF'
# 构建阶段
FROM node:18-alpine as builder

WORKDIR /app

# 安装pnpm
RUN npm install -g pnpm

# 复制package文件
COPY package*.json ./
COPY pnpm-lock.yaml* ./

# 安装依赖
RUN pnpm install || npm install

# 复制源代码
COPY . .

# 构建应用
RUN pnpm build || npm run build

# 生产阶段
FROM nginx:alpine

# 复制构建结果
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
EOF

echo "5. 重新构建和启动系统..."
docker-compose up -d --build

echo "6. 检查服务状态..."
sleep 10
docker-compose ps

echo "=== 修复完成！==="
echo "如果还有问题，请检查日志："
echo "docker-compose logs -f"
