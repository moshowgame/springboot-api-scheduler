-- 创建数据库
CREATE DATABASE api_scheduler;

-- 连接到数据库
\c api_scheduler;

-- 创建API任务表
CREATE TABLE IF NOT EXISTS api_task (
    id VARCHAR(50) PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    method VARCHAR(10) NOT NULL DEFAULT 'GET',
    timeout INT DEFAULT 30000,
    headers TEXT,
    parameters TEXT,
    cron_expression VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'PAUSED',
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_execute_time TIMESTAMP
);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 创建更新时间触发器
CREATE TRIGGER update_api_task_update_time 
    BEFORE UPDATE ON api_task 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_time_column();

-- 创建用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建更新时间触发器函数（如果不存在）
CREATE OR REPLACE FUNCTION update_updated_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 创建用户表更新时间触发器
CREATE TRIGGER update_user_update_time
    BEFORE UPDATE ON sys_user
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建API响应日志表
CREATE TABLE IF NOT EXISTS api_response (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL,
    request_url TEXT NOT NULL,
    request_method VARCHAR(10) NOT NULL,
    request_headers TEXT,
    request_params TEXT,
    response_code INT,
    response_body TEXT,
    response_time BIGINT,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    execute_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_api_response_task_id ON api_response(task_id);
CREATE INDEX IF NOT EXISTS idx_api_response_execute_time ON api_response(execute_time);

-- 插入默认用户数据 (密码: admin123, MD5加密后)
INSERT INTO sys_user (id, username, password, enabled) VALUES
('admin-001', 'admin', '0192023a7bbd73250516f069df18b500', true);

-- 插入示例数据
INSERT INTO api_task (id, task_name, url, method, timeout, headers, parameters, cron_expression, status, description) VALUES
('550e8400-e29b-41d4-a716-446655440001', '示例GET任务', 'http://localhost:8080/demo/test', 'GET', 30000, '{"User-Agent": "API-Scheduler"}', '{"param1": "value1"}', '0 */5 * * * ?', 'PAUSED', '每5分钟执行一次的GET请求示例'),
('550e8400-e29b-41d4-a716-446655440002', '示例POST任务', 'http://localhost:8080/demo/test', 'POST', 30000, '{"Content-Type": "application/json"}', '{"key": "value"}', '0 0/10 * * * ?', 'PAUSED', '每10分钟执行一次的POST请求示例');