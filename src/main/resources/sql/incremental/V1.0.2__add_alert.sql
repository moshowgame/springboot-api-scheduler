-- 添加警报功能相关表

-- 创建警报配置表
CREATE TABLE IF NOT EXISTS alert_config (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50)  UNIQUE,
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