-- 添加断言功能相关字段
-- 执行时间: 2024-01-XX

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