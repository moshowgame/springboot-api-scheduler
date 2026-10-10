# 数据库增量 DDL 管理指南

> 增量脚本统一放在 `src/main/resources/sql/incremental/`，按版本号命名，由自带的
> `IncrementalDdlRunner` 执行并自动记录每次执行时间（`schema_incremental_history` 表）。
> 已部署到某个版本的库，只需执行该版本之后到现在的增量即可。

## 📁 目录与命名

```
src/main/resources/sql/
├── init.sql                        # 全量建表基线（新库初始化用，始终包含最新结构）
└── incremental/                    # 版本化增量脚本（唯一权威增量入口）
    ├── V1.0.1__add_assertions.sql  # 断言功能
    ├── V1.0.2__add_alert.sql       # 警报功能
    └── V1.1.0__add_chain_trigger.sql # 任务编排（链式触发）
```

命名规则：`V<版本号>__<说明>.sql`，版本号语义化且**必须递增**（比较按数字分段：
`1.0.2 < 1.1.0 < 1.10.0`）。基线版本 `1.0.0` 代表 init.sql 全量建表。

## 📜 历史记录表

执行器首次运行会自动创建 `schema_incremental_history`：

| 字段 | 说明 |
|---|---|
| version | 增量版本号（主键，防重复执行） |
| script_name | 脚本文件名 |
| description | 说明 |
| applied_mode | `APPLY` = 实际执行 / `BASELINE` = 补记历史（不执行） |
| executed_at | **执行时间** |
| execution_ms | 执行耗时（毫秒） |
| success | 是否成功 |

## 🚀 使用场景

```bash
# classpath 准备（Windows Git Bash 示例，注意加 MSYS_NO_PATHCONV=1）
CP='target/classes;C:/Users/<你>/.m2/repository/org/postgresql/postgresql/<版本>/postgresql-<版本>.jar'
```

### 场景一：全新部署
1. 先执行 `sql/init.sql` 建全量表结构（已包含全部增量对应的结构）
2. 补记历史基线，使历史表与实际结构一致：
   ```bash
   java -cp "$CP" com.software.dev.util.IncrementalDdlRunner --baseline <init.sql 对应的最新版本>
   ```

### 场景二：存量库首次接入（推荐做法）
已在生产运行、但从未记录过增量的库：**告诉我你部署时的版本，执行器只补记该版本及之前的历史，之后只跑新增部分**：
```bash
# 假设当前部署版本为 1.0.2（已有断言、警报，没有任务编排）
java -cp "$CP" com.software.dev.util.IncrementalDdlRunner --baseline 1.0.2
```

### 场景三：日常升级
```bash
# 直接执行：自动应用所有历史表中没有记录的增量（即从已应用版本到现在）
java -cp "$CP" com.software.dev.util.IncrementalDdlRunner

# 或显式指定只执行某版本之后的增量（不含该版本本身）
java -cp "$CP" com.software.dev.util.IncrementalDdlRunner --from 1.0.2
```

### 连接参数
默认连接 `jdbc:postgresql://localhost:5432/api_scheduler`（postgres/root123），可用参数覆盖：
`--url` / `--user` / `--password` / `--dir`（增量脚本目录）。

## ✍️ 新增增量脚本的规范

1. 在 `incremental/` 下新建 `V<下一个版本号>__<说明>.sql`，文件头注释写日期与目的；
2. 脚本必须**幂等**（`CREATE TABLE IF NOT EXISTS`、`ADD COLUMN IF NOT EXISTS`），
   保证同一脚本误跑多次无副作用；
3. 不要修改已发布的历史脚本（各环境已按版本记录执行，改动会导致结构不一致）；
4. 同步更新 `init.sql`（全量基线始终反映最新结构），并在本文件顶部目录示意中补一行；
5. 提交前本地跑一遍 `IncrementalDdlRunner` 验证（执行后如属测试请连历史记录一起清理）。

## ⚠️ 注意事项

- 执行失败会立即中止且**不记录历史**，修复脚本后可直接重跑；
- `--baseline` 与 `--from` 都不会重复执行已记录的版本；
- Git Bash 下运行请加 `MSYS_NO_PATHCONV=1`，否则 `-cp` 中含盘符的路径会被错误转换；
- 本工具定位是轻量零依赖；如后续迁移到 Flyway/Liquibase，历史表可作对照数据。
