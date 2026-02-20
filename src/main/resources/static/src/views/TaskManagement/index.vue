<template>
  <div class="task-management">
    <div class="page-header">
      <h1 class="page-title">任务管理</h1>
      <p class="page-description">管理和监控API调度任务</p>
    </div>

    <!-- 工具栏 -->
    <Card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <Button
            type="primary"
            @click="showCreateModal"
          >
            <Icon type="md-add" />
            新建任务
          </Button>
          
          <Button
            type="default"
            :disabled="!hasSelection"
            @click="handleBatchStart"
          >
            <Icon type="ios-play" />
            批量启动
          </Button>
          
          <Button
            type="default"
            :disabled="!hasSelection"
            @click="handleBatchPause"
          >
            <Icon type="ios-pause" />
            批量暂停
          </Button>
          
          <Button
            type="error"
            :disabled="!hasSelection"
            @click="handleBatchDelete"
          >
            <Icon type="ios-trash" />
            批量删除
          </Button>
        </div>
        
        <div class="toolbar-right">
          <Input
            v-model="searchKeyword"
            search
            placeholder="搜索任务名称或URL"
            style="width: 300px"
            @on-search="handleSearch"
          />
          
          <Button
            type="text"
            @click="refreshData"
          >
            <Icon type="ios-refresh" :class="{ 'spin': loading }" />
            刷新
          </Button>
        </div>
      </div>
    </Card>

    <!-- 任务列表 -->
    <Card class="table-card">
      <Table
        ref="table"
        :data="tasks"
        :columns="columns"
        :loading="loading"
        stripe
        @on-selection-change="handleSelectionChange"
      >
        <template #status="{ row }">
          <Tag :color="getStatusColor(row.status)">
            {{ getStatusText(row.status) }}
          </Tag>
        </template>
        
        <template #cronExpression="{ row }">
          <Tooltip :content="row.cronExpression" placement="top">
            <span class="cron-text">{{ row.cronExpression }}</span>
          </Tooltip>
        </template>
        
        <template #lastExecuteTime="{ row }">
          <span v-if="row.lastExecuteTime">
            {{ formatDateTime(row.lastExecuteTime) }}
          </span>
          <span v-else class="text-muted">未执行</span>
        </template>
        
        <template #alertEnabled="{ row }">
          <Tag :color="row.alertEnabled ? 'success' : 'default'">
            {{ row.alertEnabled ? '已启用' : '已禁用' }}
          </Tag>
        </template>
        
        <template #action="{ row }">
          <div class="action-buttons">
            <Button
              v-if="row.status === 'PAUSED'"
              type="success"
              size="small"
              @click="handleStart(row)"
            >
              启动
            </Button>
            
            <Button
              v-if="row.status === 'RUNNING'"
              type="warning"
              size="small"
              @click="handlePause(row)"
            >
              暂停
            </Button>
            
            <Button
              type="info"
              size="small"
              @click="handleExecute(row)"
            >
              执行
            </Button>
            
            <Button
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </Button>
            
            <Button
              type="default"
              size="small"
              @click="handleCopy(row)"
            >
              复制
            </Button>
            
            <Dropdown trigger="click" @on-click="(name) => handleMoreAction(name, row)">
              <Button size="small" type="text">
                更多
                <Icon type="ios-arrow-down" />
              </Button>
              <template #dropdown>
                <DropdownMenu>
                  <DropdownItem name="assertion">断言配置</DropdownItem>
                  <DropdownItem name="alert">警报配置</DropdownItem>
                  <DropdownItem name="logs">执行日志</DropdownItem>
                  <DropdownItem name="delete" divided>删除</DropdownItem>
                </DropdownMenu>
              </template>
            </Dropdown>
          </div>
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
          @on-change="handlePageChange"
          @on-page-size-change="handlePageSizeChange"
        />
      </div>
    </Card>

    <!-- 创建/编辑任务模态框 -->
    <Modal
      v-model="taskModal.visible"
      :title="taskModal.isEdit ? '编辑任务' : '新建任务'"
      width="800"
      @on-ok="handleTaskSubmit"
      @on-cancel="handleTaskCancel"
    >
      <Form
        ref="taskForm"
        :model="taskModal.form"
        :rules="taskModal.rules"
        label-position="top"
      >
        <Row :gutter="16">
          <Col :span="12">
            <FormItem label="任务名称" prop="taskName">
              <Input
                v-model="taskModal.form.taskName"
                placeholder="请输入任务名称"
              />
            </FormItem>
          </Col>
          
          <Col :span="12">
            <FormItem label="HTTP方法" prop="method">
              <Select v-model="taskModal.form.method" placeholder="请选择HTTP方法">
                <Option value="GET">GET</Option>
                <Option value="POST">POST</Option>
                <Option value="PUT">PUT</Option>
                <Option value="DELETE">DELETE</Option>
                <Option value="PATCH">PATCH</Option>
              </Select>
            </FormItem>
          </Col>
        </Row>
        
        <FormItem label="请求URL" prop="url">
          <Input
            v-model="taskModal.form.url"
            type="url"
            placeholder="请输入请求URL，如: https://api.example.com/endpoint"
          />
        </FormItem>
        
        <Row :gutter="16">
          <Col :span="12">
            <FormItem label="超时时间(ms)" prop="timeout">
              <InputNumber
                v-model="taskModal.form.timeout"
                :min="1000"
                :max="300000"
                style="width: 100%"
                placeholder="请求超时时间，单位毫秒"
              />
            </FormItem>
          </Col>
          
          <Col :span="12">
            <FormItem label="Cron表达式" prop="cronExpression">
              <Input
                v-model="taskModal.form.cronExpression"
                placeholder="如: 0 */5 * * * ? (每5分钟执行)"
              />
            </FormItem>
          </Col>
        </Row>
        
        <FormItem label="请求头 (JSON格式)">
          <Input
            v-model="taskModal.form.headers"
            type="textarea"
            :rows="3"
            placeholder='{"Content-Type": "application/json", "Authorization": "Bearer token"}'
          />
        </FormItem>
        
        <FormItem label="请求参数 (JSON格式)">
          <Input
            v-model="taskModal.form.parameters"
            type="textarea"
            :rows="3"
            placeholder='{"key1": "value1", "key2": "value2"}'
          />
        </FormItem>
        
        <FormItem label="任务描述">
          <Input
            v-model="taskModal.form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入任务描述"
          />
        </FormItem>
        
        <FormItem>
          <Checkbox v-model="taskModal.form.alertEnabled">启用警报</Checkbox>
        </FormItem>
      </Form>
    </Modal>

    <!-- Cron表达式选择器 -->
    <Modal v-model="cronModal.visible" title="Cron表达式助手" width="600">
      <div class="cron-helper">
        <div class="cron-presets">
          <h4>常用预设</h4>
          <div class="preset-buttons">
            <Button
              v-for="preset in cronPresets"
              :key="preset.value"
              type="default"
              size="small"
              @click="selectCronPreset(preset)"
            >
              {{ preset.label }}
            </Button>
          </div>
        </div>
        
        <div class="cron-preview">
          <h4>预览</h4>
          <Input
            v-model="cronPreview"
            readonly
            placeholder="选择预设或输入表达式后查看"
          />
        </div>
      </div>
      
      <template #footer>
        <Button @click="cronModal.visible = false">取消</Button>
        <Button type="primary" @click="applyCronExpression">应用</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Modal } from 'view-ui-plus'
import { taskApi } from '@/api/tasks'
import dayjs from 'dayjs'

const router = useRouter()

// 响应式数据
const table = ref(null)
const taskForm = ref(null)
const loading = ref(false)
const tasks = ref([])
const selectedRows = ref([])
const searchKeyword = ref('')

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20
})
const total = ref(0)

// 任务模态框配置
const taskModal = reactive({
  visible: false,
  isEdit: false,
  form: {
    id: null,
    taskName: '',
    url: '',
    method: 'GET',
    timeout: 30000,
    cronExpression: '',
    headers: '',
    parameters: '',
    description: '',
    alertEnabled: false
  },
  rules: {
    taskName: [
      { required: true, message: '请输入任务名称', trigger: 'blur' },
      { max: 100, message: '任务名称不能超过100个字符', trigger: 'blur' }
    ],
    url: [
      { required: true, message: '请输入请求URL', trigger: 'blur' },
      { type: 'url', message: '请输入有效的URL', trigger: 'blur' }
    ],
    method: [
      { required: true, message: '请选择HTTP方法', trigger: 'change' }
    ],
    timeout: [
      { required: true, message: '请输入超时时间', trigger: 'blur' },
      { type: 'number', min: 1000, max: 300000, message: '超时时间必须在1000-300000ms之间', trigger: 'blur' }
    ],
    cronExpression: [
      { required: true, message: '请输入Cron表达式', trigger: 'blur' }
    ]
  }
})

// Cron模态框配置
const cronModal = reactive({
  visible: false,
  targetField: null
})
const cronPreview = ref('')

// Cron预设选项
const cronPresets = [
  { label: '每分钟', value: '0 * * * * ?' },
  { label: '每5分钟', value: '0 */5 * * * ?' },
  { label: '每10分钟', value: '0 */10 * * * ?' },
  { label: '每30分钟', value: '0 */30 * * * ?' },
  { label: '每小时', value: '0 0 * * * ?' },
  { label: '每天0点', value: '0 0 0 * * ?' },
  { label: '工作日9点', value: '0 0 9 ? * MON-FRI' },
  { label: '周一9点', value: '0 0 9 ? * MON' }
]

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
    title: 'URL',
    key: 'url',
    minWidth: 200,
    ellipsis: true,
    tooltip: true
  },
  {
    title: '方法',
    key: 'method',
    width: 80
  },
  {
    title: 'Cron表达式',
    slot: 'cronExpression',
    width: 150
  },
  {
    title: '状态',
    slot: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '最后执行',
    slot: 'lastExecuteTime',
    width: 150
  },
  {
    title: '警报',
    slot: 'alertEnabled',
    width: 80,
    align: 'center'
  },
  {
    title: '操作',
    slot: 'action',
    width: 280,
    align: 'center'
  }
]

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'RUNNING': 'success',
    'PAUSED': 'warning',
    'ERROR': 'error',
    'STOPPED': 'default'
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'RUNNING': '运行中',
    'PAUSED': '已暂停',
    'ERROR': '错误',
    'STOPPED': '已停止'
  }
  return textMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 获取任务列表
const fetchTasks = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      size: pagination.pageSize,
      keyword: searchKeyword.value
    }
    
    const response = await taskApi.getAll()
    // TODO: 根据实际API响应结构调整
    tasks.value = response.data || []
    total.value = response.total || 0
  } catch (error) {
    console.error('Failed to fetch tasks:', error)
    Message.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  fetchTasks()
}

// 搜索处理
const handleSearch = () => {
  pagination.current = 1
  fetchTasks()
}

// 分页处理
const handlePageChange = (page) => {
  pagination.current = page
  fetchTasks()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  fetchTasks()
}

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 显示创建任务模态框
const showCreateModal = () => {
  taskModal.visible = true
  taskModal.isEdit = false
  resetTaskForm()
}

// 重置任务表单
const resetTaskForm = () => {
  taskModal.form = {
    id: null,
    taskName: '',
    url: '',
    method: 'GET',
    timeout: 30000,
    cronExpression: '',
    headers: '',
    parameters: '',
    description: '',
    alertEnabled: false
  }
}

// 处理任务提交
const handleTaskSubmit = async () => {
  try {
    const valid = await taskForm.value.validate()
    if (!valid) return
    
    const formData = { ...taskModal.form }
    
    // 处理JSON字段
    try {
      if (formData.headers) {
        formData.headers = JSON.parse(formData.headers)
      }
      if (formData.parameters) {
        formData.parameters = JSON.parse(formData.parameters)
      }
    } catch (error) {
      Message.error('请求头或参数格式不正确，请输入有效的JSON格式')
      return
    }
    
    let response
    if (taskModal.isEdit) {
      response = await taskApi.update(formData.id, formData)
    } else {
      response = await taskApi.create(formData)
    }
    
    if (response.success) {
      Message.success(taskModal.isEdit ? '任务更新成功' : '任务创建成功')
      taskModal.visible = false
      fetchTasks()
    } else {
      Message.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('Task submit error:', error)
    Message.error('操作失败，请稍后重试')
  }
}

// 处理任务取消
const handleTaskCancel = () => {
  taskModal.visible = false
  resetTaskForm()
}

// 处理编辑任务
const handleEdit = (row) => {
  taskModal.visible = true
  taskModal.isEdit = true
  
  // 填充表单数据
  taskModal.form = {
    ...row,
    headers: row.headers ? JSON.stringify(row.headers, null, 2) : '',
    parameters: row.parameters ? JSON.stringify(row.parameters, null, 2) : ''
  }
}

// 处理复制任务
const handleCopy = async (row) => {
  try {
    const response = await taskApi.copy(row.id)
    if (response.success) {
      Message.success('任务复制成功')
      fetchTasks()
    } else {
      Message.error(response.message || '复制失败')
    }
  } catch (error) {
    console.error('Copy task error:', error)
    Message.error('复制失败')
  }
}

// 处理任务启动
const handleStart = async (row) => {
  try {
    const response = await taskApi.start(row.id)
    if (response.success) {
      Message.success('任务启动成功')
      fetchTasks()
    } else {
      Message.error(response.message || '启动失败')
    }
  } catch (error) {
    console.error('Start task error:', error)
    Message.error('启动失败')
  }
}

// 处理任务暂停
const handlePause = async (row) => {
  try {
    const response = await taskApi.pause(row.id)
    if (response.success) {
      Message.success('任务暂停成功')
      fetchTasks()
    } else {
      Message.error(response.message || '暂停失败')
    }
  } catch (error) {
    console.error('Pause task error:', error)
    Message.error('暂停失败')
  }
}

// 处理任务立即执行
const handleExecute = async (row) => {
  try {
    const response = await taskApi.execute(row.id)
    if (response.success) {
      Message.success('任务执行成功')
    } else {
      Message.error(response.message || '执行失败')
    }
  } catch (error) {
    console.error('Execute task error:', error)
    Message.error('执行失败')
  }
}

// 批量操作处理
const handleBatchStart = async () => {
  const ids = selectedRows.value.map(row => row.id)
  try {
    const response = await taskApi.batchStart(ids)
    if (response.success) {
      Message.success(`成功启动 ${ids.length} 个任务`)
      fetchTasks()
    } else {
      Message.error(response.message || '批量启动失败')
    }
  } catch (error) {
    console.error('Batch start error:', error)
    Message.error('批量启动失败')
  }
}

const handleBatchPause = async () => {
  const ids = selectedRows.value.map(row => row.id)
  try {
    const response = await taskApi.batchPause(ids)
    if (response.success) {
      Message.success(`成功暂停 ${ids.length} 个任务`)
      fetchTasks()
    } else {
      Message.error(response.message || '批量暂停失败')
    }
  } catch (error) {
    console.error('Batch pause error:', error)
    Message.error('批量暂停失败')
  }
}

const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRows.value.length} 个任务吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      const ids = selectedRows.value.map(row => row.id)
      try {
        const response = await taskApi.batchDelete(ids)
        if (response.success) {
          Message.success(`成功删除 ${ids.length} 个任务`)
          fetchTasks()
        } else {
          Message.error(response.message || '批量删除失败')
        }
      } catch (error) {
        console.error('Batch delete error:', error)
        Message.error('批量删除失败')
      }
    }
  })
}

// 更多操作处理
const handleMoreAction = (action, row) => {
  switch (action) {
    case 'assertion':
      router.push({ name: 'AssertionConfig', params: { taskId: row.id } })
      break
    case 'alert':
      router.push({ name: 'AlertManagement', params: { taskId: row.id } })
      break
    case 'logs':
      router.push({ name: 'ResponseMonitoring', query: { taskId: row.id } })
      break
    case 'delete':
      Modal.confirm({
        title: '确认删除',
        content: `确定要删除任务"${row.taskName}"吗？此操作不可恢复。`,
        okText: '确定',
        cancelText: '取消',
        onOk: async () => {
          try {
            const response = await taskApi.delete(row.id)
            if (response.success) {
              Message.success('任务删除成功')
              fetchTasks()
            } else {
              Message.error(response.message || '删除失败')
            }
          } catch (error) {
            console.error('Delete task error:', error)
            Message.error('删除失败')
          }
        }
      })
      break
  }
}

// Cron表达式相关
const selectCronPreset = (preset) => {
  cronPreview.value = preset.value
}

const applyCronExpression = () => {
  if (cronModal.targetField && cronPreview.value) {
    taskModal.form[cronModal.targetField] = cronPreview.value
  }
  cronModal.visible = false
}

// 初始化
onMounted(() => {
  fetchTasks()
})
</script>

<style lang="scss" scoped>
.task-management {
  .toolbar-card {
    margin-bottom: 16px;
  }
  
  .toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;
    
    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
    }
  }
  
  .table-card {
    .table-footer {
      margin-top: 16px;
      text-align: right;
    }
  }
  
  .action-buttons {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-wrap: wrap;
  }
  
  .cron-text {
    font-family: 'Courier New', monospace;
    font-size: 12px;
    background: #f5f7fa;
    padding: 2px 6px;
    border-radius: 3px;
  }
  
  .text-muted {
    color: #9ca3af;
    font-style: italic;
  }
  
  .spin {
    animation: spin 1s linear infinite;
  }
  
  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
}

// Cron助手模态框
.cron-helper {
  .cron-presets {
    margin-bottom: 24px;
    
    h4 {
      margin-bottom: 12px;
      color: #374151;
    }
    
    .preset-buttons {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }
  
  .cron-preview {
    h4 {
      margin-bottom: 12px;
      color: #374151;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .task-management {
    .toolbar {
      flex-direction: column;
      align-items: stretch;
      
      .toolbar-left,
      .toolbar-right {
        flex-direction: column;
        width: 100%;
      }
      
      .toolbar-right {
        .ivu-input-wrapper {
          width: 100% !important;
        }
      }
    }
    
    .action-buttons {
      flex-direction: column;
      align-items: stretch;
      gap: 4px;
    }
  }
}
</style>