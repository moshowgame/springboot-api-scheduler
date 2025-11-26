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
    last_execute_time TIMESTAMP,
    assertions TEXT
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
    execute_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assertion_result TEXT,
    all_assertions_passed BOOLEAN
);

-- 创建断言表
CREATE TABLE IF NOT EXISTS api_assertion (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL UNIQUE,
    response_id VARCHAR(50),
    assertion_type VARCHAR(20) NOT NULL,
    expected_value TEXT NOT NULL,
    actual_value TEXT,
    passed BOOLEAN,
    error_message TEXT,
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_api_response_task_id ON api_response(task_id);
CREATE INDEX IF NOT EXISTS idx_api_response_execute_time ON api_response(execute_time);
CREATE INDEX IF NOT EXISTS idx_api_assertion_task_id ON api_assertion(task_id);
CREATE INDEX IF NOT EXISTS idx_api_assertion_response_id ON api_assertion(response_id);

-- 插入默认用户数据 (密码: admin123, MD5加密后)
INSERT INTO sys_user (id, username, password, enabled) VALUES
('admin-001', 'admin', '0192023a7bbd73250516f069df18b500', true);

-- 插入示例数据
INSERT INTO api_task (id, task_name, url, method, timeout, headers, parameters, cron_expression, status, description) VALUES
('550e8400-e29b-41d4-a716-446655440001', '示例GET任务', 'http://localhost:8080/demo/test', 'GET', 30000, '{"User-Agent": "API-Scheduler"}', '{"param1": "value1"}', '0 */5 * * * ?', 'PAUSED', '每5分钟执行一次的GET请求示例'),
('550e8400-e29b-41d4-a716-446655440002', '示例POST任务', 'http://localhost:8080/demo/test', 'POST', 30000, '{"Content-Type": "application/json"}', '{"key": "value"}', '0 0/10 * * * ?', 'PAUSED', '每10分钟执行一次的POST请求示例');

---------------------------------------------
-- 添加断言功能相关字段
-- 执行时间: 2025-11-23
---------------------------------------------

-- 为api_task表添加assertions字段
ALTER TABLE api_task ADD COLUMN IF NOT EXISTS assertions TEXT;

-- 为api_response表添加断言结果字段
ALTER TABLE api_response ADD COLUMN IF NOT EXISTS assertion_result TEXT;
ALTER TABLE api_response ADD COLUMN IF NOT EXISTS all_assertions_passed BOOLEAN;

-- 创建断言表
CREATE TABLE IF NOT EXISTS api_assertion (
                                             id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL,
    response_id VARCHAR(50),
    assertion_type VARCHAR(20) NOT NULL,
    expected_value TEXT NOT NULL,
    actual_value TEXT,
    passed BOOLEAN NOT NULL,
    error_message TEXT,
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_api_assertion_task_id ON api_assertion(task_id);
CREATE INDEX IF NOT EXISTS idx_api_assertion_response_id ON api_assertion(response_id);

-- 添加注释
COMMENT ON COLUMN api_task.assertions IS '断言配置JSON';
COMMENT ON COLUMN api_response.assertion_result IS '断言结果汇总';
COMMENT ON COLUMN api_response.all_assertions_passed IS '所有断言是否通过';
COMMENT ON TABLE api_assertion IS 'API断言表';

-- 修改 api_assertion 表，支持一对一关系和允许 passed 字段为空
-- 执行前请备份数据库

-- 删除现有的唯一约束（如果存在）
DROP INDEX IF EXISTS idx_api_assertion_task_id;

-- 添加 task_id 唯一约束，确保一对一关系
ALTER TABLE api_assertion ADD CONSTRAINT uk_api_assertion_task_id UNIQUE (task_id);

-- 修改 passed 字段允许为 NULL（配置时不需要值）
ALTER TABLE api_assertion ALTER COLUMN passed DROP NOT NULL;

-- 添加注释
COMMENT ON CONSTRAINT uk_api_assertion_task_id ON api_assertion IS '确保每个任务只能有一个断言配置';
COMMENT ON COLUMN api_assertion.passed IS '断言执行结果，配置时可为空';

---------------------------------------------
-- 添加警报功能相关表
-- 执行时间: 2025-11-24
---------------------------------------------

-- 创建警报配置表
CREATE TABLE IF NOT EXISTS alert_config (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) UNIQUE,
    failure_rate_threshold INT NOT NULL DEFAULT 50,
    check_interval INT NOT NULL DEFAULT 30,
    api_url TEXT NOT NULL,
    http_method VARCHAR(10) NOT NULL DEFAULT 'POST',
    headers TEXT,
    body TEXT,
    enabled BOOLEAN DEFAULT false,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_check_time TIMESTAMP
    );

-- 创建警报记录表
CREATE TABLE IF NOT EXISTS alert_record (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    failure_rate INT NOT NULL,
    failure_count INT NOT NULL,
    total_count INT NOT NULL,
    alert_message TEXT,
    api_url TEXT,
    response TEXT,
    alert_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 为api_task表添加警报启用字段
ALTER TABLE api_task ADD COLUMN IF NOT EXISTS alert_enabled BOOLEAN DEFAULT false;

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_alert_config_task_id ON alert_config(task_id);
CREATE INDEX IF NOT EXISTS idx_alert_record_task_id ON alert_record(task_id);
CREATE INDEX IF NOT EXISTS idx_alert_record_alert_time ON alert_record(alert_time);

-- 创建更新时间触发器
CREATE TRIGGER update_alert_config_update_time
    BEFORE UPDATE ON alert_config
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();