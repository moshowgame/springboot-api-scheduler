<template>
  <div class="response-monitoring">
    <div class="page-header">
      <h1 class="page-title">响应监控</h1>
      <p class="page-description">查看API执行记录和响应分析</p>
    </div>

    <!-- 筛选工具栏 -->
    <Card class="filter-card">
      <Form :model="filters" inline>
        <FormItem label="任务选择">
          <Select
            v-model="filters.taskId"
            clearable
            placeholder="选择任务"
            style="width: 200px"
            @on-change="handleFilterChange"
          >
            <Option v-for="task in taskOptions" :key="task.id" :value="task.id">
              {{ task.taskName }}
            </Option>
          </Select>
        </FormItem>
        
        <FormItem label="执行状态">
          <Select
            v-model="filters.status"
            clearable
            placeholder="选择状态"
            style="width: 120px"
            @on-change="handleFilterChange"
          >
            <Option value="SUCCESS">成功</Option>
            <Option value="ERROR">失败</Option>
            <Option value="TIMEOUT">超时</Option>
          </Select>
        </FormItem>
        
        <FormItem label="时间范围">
          <DatePicker
            v-model="filters.timeRange"
            type="datetimerange"
            format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择时间范围"
            style="width: 350px"
            @on-change="handleFilterChange"
          />
        </FormItem>
        
        <FormItem>
          <Button type="primary" @click="handleSearch">
            <Icon type="ios-search" />
            查询
          </Button>
          
          <Button @click="handleReset">
            <Icon type="ios-refresh" />
            重置
          </Button>
        </FormItem>
      </Form>
    </Card>

    <!-- 统计信息 -->
    <Row :gutter="16" class="stats-row">
      <Col :span="6">
        <Card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <Icon type="ios-stats" :size="24" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.total }}</div>
              <div class="stat-label">总执行次数</div>
            </div>
          </div>
        </Card>
      </Col>
      
      <Col :span="6">
        <Card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon success">
              <Icon type="ios-checkmark-circle" :size="24" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.success }}</div>
              <div class="stat-label">成功次数</div>
            </div>
          </div>
        </Card>
      </Col>
      
      <Col :span="6">
        <Card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon error">
              <Icon type="ios-close-circle" :size="24" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.error }}</div>
              <div class="stat-label">失败次数</div>
            </div>
          </div>
        </Card>
      </Col>
      
      <Col :span="6">
        <Card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon avg-time">
              <Icon type="ios-speedometer" :size="24" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.avgResponseTime }}ms</div>
              <div class="stat-label">平均响应时间</div>
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- 响应记录表格 -->
    <Card class="table-card">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <Button
            type="primary"
            :disabled="!hasSelection"
            @click="handleExport"
          >
            <Icon type="ios-download" />
            导出选中
          </Button>
          
          <Button
            type="default"
            @click="handleExportAll"
          >
            <Icon type="ios-download" />
            导出全部
          </Button>
        </div>
        
        <div class="toolbar-right">
          <Button
            type="text"
            @click="refreshData"
          >
            <Icon type="ios-refresh" :class="{ 'spin': loading }" />
            刷新
          </Button>
        </div>
      </div>
      
      <Table
        ref="table"
        :data="responses"
        :columns="columns"
        :loading="loading"
        stripe
        @on-selection-change="handleSelectionChange"
        @on-expand="handleExpand"
      >
        <template #status="{ row }">
          <Tag :color="getStatusColor(row.status)">
            {{ getStatusText(row.status) }}
          </Tag>
        </template>
        
        <template #responseTime="{ row }">
          <span :class="getResponseTimeClass(row.responseTime)">
            {{ row.responseTime }}ms
          </span>
        </template>
        
        <template #assertionResult="{ row }">
          <div v-if="row.assertionResult" class="assertion-result">
            <Tag :color="row.allAssertionsPassed ? 'success' : 'error'">
              {{ row.allAssertionsPassed ? '全部通过' : '存在失败' }}
            </Tag>
            <Tooltip :content="row.assertionResult" placement="top" max-width="300">
              <Icon type="ios-help-circle" color="#999" />
            </Tooltip>
          </div>
          <span v-else class="text-muted">无断言</span>
        </template>
        
        <template #executeTime="{ row }">
          {{ formatDateTime(row.executeTime) }}
        </template>
        
        <template #action="{ row }">
          <Button
            type="primary"
            size="small"
            @click="handleViewDetail(row)"
          >
            详情
          </Button>
        </template>
      </Table>
      
      <div class="table-footer">
        <Page
          v-model="pagination"
          :total="total"
          :page-size="pagination.pageSize"
          :page-size-opts="[20, 50, 100]"
          show-sizer
          show-total
          show-elevator
          @on-change="handlePageChange"
          @on-page-size-change="handlePageSizeChange"
        />
      </div>
    </Card>

    <!-- 详情模态框 -->
    <Modal
      v-model="detailModal.visible"
      title="执行详情"
      width="900"
      class="detail-modal"
    >
      <div v-if="detailModal.data" class="detail-content">
        <Row :gutter="16">
          <Col :span="12">
            <div class="detail-section">
              <h4>基本信息</h4>
              <div class="detail-item">
                <label>任务名称：</label>
                <span>{{ detailModal.data.taskName }}</span>
              </div>
              <div class="detail-item">
                <label>请求URL：</label>
                <span class="url-text">{{ detailModal.data.requestUrl }}</span>
              </div>
              <div class="detail-item">
                <label>请求方法：</label>
                <Tag>{{ detailModal.data.requestMethod }}</Tag>
              </div>
              <div class="detail-item">
                <label>执行状态：</label>
                <Tag :color="getStatusColor(detailModal.data.status)">
                  {{ getStatusText(detailModal.data.status) }}
                </Tag>
              </div>
              <div class="detail-item">
                <label>响应时间：</label>
                <span :class="getResponseTimeClass(detailModal.data.responseTime)">
                  {{ detailModal.data.responseTime }}ms
                </span>
              </div>
              <div class="detail-item">
                <label>执行时间：</label>
                <span>{{ formatDateTime(detailModal.data.executeTime) }}</span>
              </div>
            </div>
          </Col>
          
          <Col :span="12">
            <div class="detail-section">
              <h4>断言结果</h4>
              <div v-if="detailModal.data.assertionResult" class="assertion-detail">
                <div class="assertion-summary">
                  <Tag :color="detailModal.data.allAssertionsPassed ? 'success' : 'error'">
                    {{ detailModal.data.allAssertionsPassed ? '全部通过' : '存在失败' }}
                  </Tag>
                </div>
                <div class="assertion-content">
                  {{ detailModal.data.assertionResult }}
                </div>
              </div>
              <div v-else class="no-assertion">
                <Icon type="ios-information-circle" color="#999" />
                <span>无断言配置</span>
              </div>
            </div>
          </Col>
        </Row>
        
        <div class="detail-section">
          <h4>请求信息</h4>
          <Tabs>
            <TabPane label="请求头">
              <pre class="json-content">{{ formatJson(detailModal.data.requestHeaders) }}</pre>
            </TabPane>
            <TabPane label="请求参数">
              <pre class="json-content">{{ formatJson(detailModal.data.requestParams) }}</pre>
            </TabPane>
          </Tabs>
        </div>
        
        <div class="detail-section">
          <h4>响应信息</h4>
          <div class="response-info">
            <div class="response-status">
              <label>HTTP状态码：</label>
              <Tag :color="getResponseCodeColor(detailModal.data.responseCode)">
                {{ detailModal.data.responseCode }}
              </Tag>
            </div>
          </div>
          <div class="response-body">
            <label>响应体：</label>
            <pre class="json-content">{{ detailModal.data.responseBody }}</pre>
          </div>
        </div>
        
        <div v-if="detailModal.data.errorMessage" class="detail-section error-section">
          <h4>错误信息</h4>
          <div class="error-message">
            <pre>{{ detailModal.data.errorMessage }}</pre>
          </div>
        </div>
      </div>
      
      <template #footer>
        <Button @click="detailModal.visible = false">关闭</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Message } from 'view-ui-plus'
import { taskApi } from '@/api/tasks'
import dayjs from 'dayjs'

const route = useRoute()

// 响应式数据
const table = ref(null)
const loading = ref(false)
const responses = ref([])
const selectedRows = ref([])
const taskOptions = ref([])
const expandedRows = ref(new Set())

// 筛选条件
const filters = reactive({
  taskId: null,
  status: null,
  timeRange: null
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20
})
const total = ref(0)

// 统计数据
const statistics = reactive({
  total: 0,
  success: 0,
  error: 0,
  avgResponseTime: 0
})

// 详情模态框
const detailModal = reactive({
  visible: false,
  data: null
})

// 计算属性
const hasSelection = computed(() => selectedRows.value.length > 0)

// 表格列配置
const columns = [
  {
    type: 'selection',
    width: 60,
    align: 'center'
  },
  {
    type: 'expand',
    width: 50,
    align: 'center',
    render: (h, params) => {
      return h('div', [
        h('Icon', {
          props: {
            type: expandedRows.value.has(params.row.id) ? 'ios-arrow-down' : 'ios-arrow-forward'
          }
        })
      ])
    }
  },
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '任务名称',
    key: 'taskName',
    minWidth: 150,
    ellipsis: true,
    tooltip: true
  },
  {
    title: '请求URL',
    key: 'requestUrl',
    minWidth: 200,
    ellipsis: true,
    tooltip: true
  },
  {
    title: '方法',
    key: 'requestMethod',
    width: 80
  },
  {
    title: '状态码',
    key: 'responseCode',
    width: 80
  },
  {
    title: '状态',
    slot: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '响应时间',
    slot: 'responseTime',
    width: 100,
    align: 'center'
  },
  {
    title: '断言结果',
    slot: 'assertionResult',
    width: 120,
    align: 'center'
  },
  {
    title: '执行时间',
    slot: 'executeTime',
    width: 150
  },
  {
    title: '操作',
    slot: 'action',
    width: 80,
    align: 'center'
  }
]

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'SUCCESS': 'success',
    'ERROR': 'error',
    'TIMEOUT': 'warning'
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'SUCCESS': '成功',
    'ERROR': '失败',
    'TIMEOUT': '超时'
  }
  return textMap[status] || '未知'
}

// 获取响应时间样式
const getResponseTimeClass = (time) => {
  if (time < 1000) return 'text-success'
  if (time < 3000) return 'text-warning'
  return 'text-error'
}

// 获取响应码颜色
const getResponseCodeColor = (code) => {
  if (code >= 200 && code < 300) return 'success'
  if (code >= 400 && code < 500) return 'warning'
  if (code >= 500) return 'error'
  return 'default'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化JSON
const formatJson = (data) => {
  try {
    return JSON.stringify(data, null, 2)
  } catch {
    return data || ''
  }
}

// 获取任务选项
const fetchTaskOptions = async () => {
  try {
    const response = await taskApi.getAll()
    taskOptions.value = response.data || []
  } catch (error) {
    console.error('Failed to fetch task options:', error)
  }
}

// 获取响应记录
const fetchResponses = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      size: pagination.pageSize,
      taskId: filters.taskId,
      status: filters.status,
      startTime: filters.timeRange?.[0],
      endTime: filters.timeRange?.[1]
    }
    
    // TODO: 调用API获取响应记录
    // const response = await responseApi.getList(params)
    // responses.value = response.data
    // total.value = response.total
    
    // 模拟数据
    responses.value = [
      {
        id: 1,
        taskName: '用户登录API',
        requestUrl: 'https://api.example.com/auth/login',
        requestMethod: 'POST',
        responseCode: 200,
        status: 'SUCCESS',
        responseTime: 245,
        allAssertionsPassed: true,
        assertionResult: 'HTTP状态码: 200 (通过) | JSON包含: {"status":"success"} (通过)',
        executeTime: new Date().toISOString(),
        requestHeaders: { 'Content-Type': 'application/json' },
        requestParams: { username: 'test' },
        responseBody: '{"status":"success","token":"abc123"}'
      }
    ]
    total.value = 1
    
    // 更新统计信息
    updateStatistics()
  } catch (error) {
    console.error('Failed to fetch responses:', error)
    Message.error('获取响应记录失败')
  } finally {
    loading.value = false
  }
}

// 更新统计信息
const updateStatistics = () => {
  statistics.total = responses.value.length
  statistics.success = responses.value.filter(r => r.status === 'SUCCESS').length
  statistics.error = responses.value.filter(r => r.status !== 'SUCCESS').length
  statistics.avgResponseTime = responses.value.length > 0
    ? Math.round(responses.value.reduce((sum, r) => sum + r.responseTime, 0) / responses.value.length)
    : 0
}

// 刷新数据
const refreshData = () => {
  fetchResponses()
}

// 筛选处理
const handleFilterChange = () => {
  pagination.current = 1
  fetchResponses()
}

const handleSearch = () => {
  pagination.current = 1
  fetchResponses()
}

const handleReset = () => {
  Object.assign(filters, {
    taskId: null,
    status: null,
    timeRange: null
  })
  pagination.current = 1
  fetchResponses()
}

// 分页处理
const handlePageChange = (page) => {
  pagination.current = page
  fetchResponses()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  fetchResponses()
}

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 展开处理
const handleExpand = (row, expanded) => {
  if (expanded) {
    expandedRows.value.add(row.id)
  } else {
    expandedRows.value.delete(row.id)
  }
}

// 查看详情
const handleViewDetail = (row) => {
  detailModal.visible = true
  detailModal.data = row
}

// 导出处理
const handleExport = () => {
  if (selectedRows.value.length === 0) {
    Message.warning('请先选择要导出的记录')
    return
  }
  // TODO: 实现导出功能
  Message.info('导出功能开发中...')
}

const handleExportAll = () => {
  // TODO: 实现导出全部功能
  Message.info('导出全部功能开发中...')
}

// 初始化
onMounted(async () => {
  // 从路由参数中获取taskId
  if (route.query.taskId) {
    filters.taskId = parseInt(route.query.taskId)
  }
  
  await fetchTaskOptions()
  await fetchResponses()
})
</script>

<style lang="scss" scoped>
.response-monitoring {
  .filter-card {
    margin-bottom: 16px;
  }
  
  .stats-row {
    margin-bottom: 16px;
  }
  
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      
      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        
        &.total {
          background: rgba(37, 99, 235, 0.1);
          color: #2563eb;
        }
        
        &.success {
          background: rgba(22, 163, 74, 0.1);
          color: #16a34a;
        }
        
        &.error {
          background: rgba(220, 38, 38, 0.1);
          color: #dc2626;
        }
        
        &.avg-time {
          background: rgba(8, 145, 178, 0.1);
          color: #0891b2;
        }
      }
      
      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: 700;
          color: #1f2937;
          line-height: 1;
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #6b7280;
        }
      }
    }
  }
  
  .table-card {
    .table-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .toolbar-left,
      .toolbar-right {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
    
    .table-footer {
      margin-top: 16px;
      text-align: right;
    }
  }
  
  .assertion-result {
    display: flex;
    align-items: center;
    gap: 4px;
  }
  
  .text-muted {
    color: #9ca3af;
    font-style: italic;
  }
  
  .text-success {
    color: #16a34a;
    font-weight: 500;
  }
  
  .text-warning {
    color: #d97706;
    font-weight: 500;
  }
  
  .text-error {
    color: #dc2626;
    font-weight: 500;
  }
  
  .spin {
    animation: spin 1s linear infinite;
  }
  
  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
}

// 详情模态框样式
.detail-modal {
  :deep(.ivu-modal-body) {
    max-height: 70vh;
    overflow-y: auto;
  }
  
  .detail-content {
    .detail-section {
      margin-bottom: 24px;
      
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #374151;
        margin-bottom: 12px;
        border-bottom: 1px solid #e5e7eb;
        padding-bottom: 8px;
      }
      
      .detail-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        
        label {
          min-width: 100px;
          font-weight: 500;
          color: #6b7280;
        }
        
        .url-text {
          word-break: break-all;
          max-width: 300px;
        }
      }
      
      .assertion-detail {
        .assertion-summary {
          margin-bottom: 8px;
        }
        
        .assertion-content {
          padding: 12px;
          background: #f9fafb;
          border-radius: 6px;
          border-left: 4px solid #3b82f6;
          font-size: 14px;
          line-height: 1.5;
        }
      }
      
      .no-assertion {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #9ca3af;
        font-style: italic;
      }
      
      .json-content {
        background: #f8f9fa;
        border: 1px solid #e9ecef;
        border-radius: 6px;
        padding: 12px;
        font-family: 'Courier New', monospace;
        font-size: 12px;
        line-height: 1.4;
        max-height: 300px;
        overflow-y: auto;
        white-space: pre-wrap;
        word-break: break-all;
      }
      
      .response-info {
        margin-bottom: 16px;
        
        .response-status {
          display: flex;
          align-items: center;
          margin-bottom: 8px;
          
          label {
            min-width: 100px;
            font-weight: 500;
            color: #6b7280;
          }
        }
      }
      
      .error-section {
        .error-message {
          pre {
            background: #fef2f2;
            border: 1px solid #fecaca;
            color: #dc2626;
            border-radius: 6px;
            padding: 12px;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            line-height: 1.4;
            white-space: pre-wrap;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .response-monitoring {
    .stats-row {
      :deep(.ivu-col) {
        margin-bottom: 12px;
      }
    }
    
    .table-toolbar {
      flex-direction: column;
      align-items: stretch;
      gap: 12px;
    }
    
    .table-card {
      :deep(.ivu-table) {
        font-size: 12px;
      }
    }
  }
  
  .detail-modal {
    :deep(.ivu-modal) {
      margin: 10px;
      max-width: calc(100vw - 20px);
    }
  }
}
</style>