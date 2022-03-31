## Redis 相关命令

### Redis 设置和查看密码

前提：启动 redis-cli

```bash
# 初始化 Redis 密码
requirepass 123456

# 不重启 Redis 设置密码
config set requirepass 123456

# 查询密码
config get requirepass

# 密码验证
auth 123456
```

