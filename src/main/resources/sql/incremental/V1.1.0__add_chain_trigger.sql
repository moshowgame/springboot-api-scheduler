-- 链式触发（简易任务编排）相关字段
-- trigger_type: 任务触发类型，CRON=定时调度（默认）/ CHAIN=被链式调用（不参与定时调度）
-- next_task_id: 本任务执行完成后要触发的下游任务 id
-- trigger_condition: 触发下游任务的条件，ALWAYS=任意 / ASSERTION_PASS=断言成功 / ASSERTION_FAIL=断言失败

ALTER TABLE api_task ADD COLUMN IF NOT EXISTS trigger_type VARCHAR(20) NOT NULL DEFAULT 'CRON';
ALTER TABLE api_task ADD COLUMN IF NOT EXISTS next_task_id VARCHAR(50) NULL;
ALTER TABLE api_task ADD COLUMN IF NOT EXISTS trigger_condition VARCHAR(20) NOT NULL DEFAULT 'ALWAYS';

COMMENT ON COLUMN api_task.trigger_type IS '任务触发类型: CRON=定时调度 / CHAIN=被链式调用';
COMMENT ON COLUMN api_task.next_task_id IS '执行完成后触发的下游任务id';
COMMENT ON COLUMN api_task.trigger_condition IS '触发下游任务的条件: ALWAYS=任意 / ASSERTION_PASS=断言成功 / ASSERTION_FAIL=断言失败';
