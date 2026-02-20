<template>
  <div class="dashboard">
    <div class="page-header">
      <h1 class="page-title">仪表板</h1>
      <p class="page-description">API监控系统概览</p>
    </div>

    <!-- 统计卡片 -->
    <Row :gutter="24" class="stats-cards">
      <Col :xs="24" :sm="12" :md="6">
        <Card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon success">
              <Icon type="ios-list-box" :size="32" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalTasks }}</div>
              <div class="stat-label">总任务数</div>
            </div>
          </div>
        </Card>
      </Col>
      
      <Col :xs="24" :sm="12" :md="6">
        <Card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon running">
              <Icon type="ios-play-circle" :size="32" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.runningTasks }}</div>
              <div class="stat-label">运行中</div>
            </div>
          </div>
        </Card>
      </Col>
      
      <Col :xs="24" :sm="12" :md="6">
        <Card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon warning">
              <Icon type="ios-warning" :size="32" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.failedTasks }}</div>
              <div class="stat-label">失败任务</div>
            </div>
          </div>
        </Card>
      </Col>
      
      <Col :xs="24" :sm="12" :md="6">
        <Card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon info">
              <Icon type="ios-notifications" :size="32" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.alertCount }}</div>
              <div class="stat-label">警报数量</div>
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <Row :gutter="24" class="content-cards">
      <!-- 任务状态分布 -->
      <Col :xs="24" :lg="12">
        <Card title="任务状态分布" class="chart-card">
          <div class="chart-container">
            <div ref="statusChart" class="status-chart"></div>
          </div>
        </Card>
      </Col>

      <!-- 执行成功率趋势 -->
      <Col :xs="24" :lg="12">
        <Card title="执行成功率趋势" class="chart-card">
          <div class="chart-container">
            <div ref="trendChart" class="trend-chart"></div>
          </div>
        </Card>
      </Col>
    </Row>

    <Row :gutter="24" class="content-cards">
      <!-- 最近任务执行记录 -->
      <Col :xs="24" :lg="14">
        <Card title="最近执行记录" class="table-card">
          <template #extra>
            <Button type="text" @click="refreshRecentLogs">
              <Icon type="ios-refresh" />
              刷新
            </Button>
          </template>
          
          <Table
            :data="recentLogs"
            :columns="logColumns"
            :loading="loading.recentLogs"
            stripe
            size="small"
          />
          
          <div class="table-footer">
            <Button type="text" @click="viewAllLogs">
              查看全部记录
              <Icon type="ios-arrow-forward" />
            </Button>
          </div>
        </Card>
      </Col>

      <!-- 系统警报 -->
      <Col :xs="24" :lg="10">
        <Card title="系统警报" class="alert-card">
          <template #extra>
            <Badge :count="unreadAlerts" dot>
              <Button type="text" @click="refreshAlerts">
                <Icon type="ios-refresh" />
              </Button>
            </Badge>
          </template>
          
          <div class="alert-list">
            <div v-if="alerts.length === 0" class="empty-state">
              <Icon type="ios-checkmark-circle" :size="48" color="#16a34a" />
              <p>暂无警报</p>
            </div>
            
            <div v-else class="alert-items">
              <div
                v-for="alert in alerts"
                :key="alert.id"
                class="alert-item"
                :class="[alert.severity]"
                @click="handleAlertClick(alert)"
              >
                <div class="alert-icon">
                  <Icon :type="getAlertIcon(alert.severity)" />
                </div>
                <div class="alert-content">
                  <div class="alert-title">{{ alert.title }}</div>
                  <div class="alert-time">{{ formatTime(alert.createTime) }}</div>
                </div>
                <div class="alert-status" :class="{ 'unread': !alert.read }"></div>
              </div>
            </div>
          </div>
          
          <div class="alert-footer">
            <Button type="text" @click="viewAllAlerts">
              查看全部警报
              <Icon type="ios-arrow-forward" />
            </Button>
          </div>
        </Card>
      </Col>
    </Row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const router = useRouter()

// 响应式数据
const statusChart = ref(null)
const trendChart = ref(null)
const loading = reactive({
  recentLogs: false,
  statistics: false
})

// 统计数据
const statistics = reactive({
  totalTasks: 0,
  runningTasks: 0,
  pausedTasks: 0,
  failedTasks: 0,
  alertCount: 0
})

// 最近执行记录
const recentLogs = ref([])
const unreadAlerts = ref(0)
const alerts = ref([])

// 表格列配置
const logColumns = [
  {
    title: '任务',
    key: 'taskName',
    width: 150,
    ellipsis: true
  },
  {
    title: 'URL',
    key: 'url',
    ellipsis: true,
    tooltip: true
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (h, params) => {
      const status = params.row.status
      const statusClass = status === 'SUCCESS' ? 'success' : 'error'
      return h('Tag', {
        class: `status-tag ${statusClass}`,
        props: { color: statusClass }
      }, status === 'SUCCESS' ? '成功' : '失败')
    }
  },
  {
    title: '响应时间',
    key: 'responseTime',
    width: 100,
    render: (h, params) => {
      return h('span', `${params.row.responseTime}ms`)
    }
  },
  {
    title: '执行时间',
    key: 'executeTime',
    width: 150,
    render: (h, params) => {
      return h('span', formatTime(params.row.executeTime))
    }
  }
]

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('MM-DD HH:mm:ss')
}

// 获取警报图标
const getAlertIcon = (severity) => {
  const iconMap = {
    'error': 'ios-close-circle',
    'warning': 'ios-warning',
    'info': 'ios-information-circle'
  }
  return iconMap[severity] || 'ios-information-circle'
}

// 初始化状态分布图表
const initStatusChart = () => {
  if (!statusChart.value) return
  
  const chart = echarts.init(statusChart.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        name: '任务状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: statistics.runningTasks, name: '运行中', itemStyle: { color: '#16a34a' } },
          { value: statistics.pausedTasks, name: '已暂停', itemStyle: { color: '#6b7280' } },
          { value: statistics.failedTasks, name: '失败', itemStyle: { color: '#dc2626' } }
        ]
      }
    ]
  }
  
  chart.setOption(option)
  
  // 响应式
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

// 初始化趋势图表
const initTrendChart = () => {
  if (!trendChart.value) return
  
  const chart = echarts.init(trendChart.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '成功率',
        type: 'line',
        smooth: true,
        lineStyle: {
          color: '#2563eb',
          width: 3
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37, 99, 235, 0.3)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.05)' }
          ])
        },
        data: [95, 98, 92, 97, 96, 99, 98]
      }
    ]
  }
  
  chart.setOption(option)
  
  // 响应式
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    loading.statistics = true
    // TODO: 调用API获取统计数据
    // const response = await taskApi.getStatistics()
    // Object.assign(statistics, response.data)
    
    // 模拟数据
    Object.assign(statistics, {
      totalTasks: 156,
      runningTasks: 120,
      pausedTasks: 24,
      failedTasks: 12,
      alertCount: 8
    })
  } catch (error) {
    console.error('Failed to fetch statistics:', error)
  } finally {
    loading.statistics = false
  }
}

// 获取最近执行记录
const fetchRecentLogs = async () => {
  try {
    loading.recentLogs = true
    // TODO: 调用API获取最近记录
    // const response = await responseApi.getRecent({ limit: 10 })
    // recentLogs.value = response.data
    
    // 模拟数据
    recentLogs.value = [
      {
        id: 1,
        taskName: '用户登录API',
        url: 'https://api.example.com/auth/login',
        status: 'SUCCESS',
        responseTime: 245,
        executeTime: new Date().toISOString()
      },
      {
        id: 2,
        taskName: '数据同步任务',
        url: 'https://api.example.com/sync/data',
        status: 'ERROR',
        responseTime: 5020,
        executeTime: new Date(Date.now() - 300000).toISOString()
      }
    ]
  } catch (error) {
    console.error('Failed to fetch recent logs:', error)
  } finally {
    loading.recentLogs = false
  }
}

// 获取警报数据
const fetchAlerts = async () => {
  try {
    // TODO: 调用API获取警报
    // const response = await alertApi.getRecent({ limit: 5 })
    // alerts.value = response.data
    // unreadAlerts.value = response.data.filter(alert => !alert.read).length
    
    // 模拟数据
    alerts.value = [
      {
        id: 1,
        title: '用户登录API失败率过高',
        severity: 'error',
        read: false,
        createTime: new Date(Date.now() - 60000).toISOString()
      },
      {
        id: 2,
        title: '数据同步任务响应时间过长',
        severity: 'warning',
        read: true,
        createTime: new Date(Date.now() - 180000).toISOString()
      }
    ]
    unreadAlerts.value = alerts.value.filter(alert => !alert.read).length
  } catch (error) {
    console.error('Failed to fetch alerts:', error)
  }
}

// 刷新数据
const refreshRecentLogs = () => {
  fetchRecentLogs()
}

const refreshAlerts = () => {
  fetchAlerts()
}

// 跳转页面
const viewAllLogs = () => {
  router.push({ name: 'ResponseMonitoring' })
}

const viewAllAlerts = () => {
  router.push({ name: 'AlertManagement' })
}

const handleAlertClick = (alert) => {
  // TODO: 标记警报为已读并查看详情
  console.log('Alert clicked:', alert)
}

// 初始化
onMounted(async () => {
  await fetchStatistics()
  await fetchRecentLogs()
  await fetchAlerts()
  
  // 初始化图表
  await nextTick()
  initStatusChart()
  initTrendChart()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stats-cards {
    margin-bottom: 24px;
  }
  
  .content-cards {
    margin-bottom: 24px;
  }
}

.stat-card {
  height: 120px;
  
  .stat-content {
    display: flex;
    align-items: center;
    height: 100%;
    
    .stat-icon {
      width: 64px;
      height: 64px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      
      &.success {
        background: rgba(22, 163, 74, 0.1);
        color: #16a34a;
      }
      
      &.running {
        background: rgba(37, 99, 235, 0.1);
        color: #2563eb;
      }
      
      &.warning {
        background: rgba(217, 119, 6, 0.1);
        color: #d97706;
      }
      
      &.info {
        background: rgba(8, 145, 178, 0.1);
        color: #0891b2;
      }
    }
    
    .stat-info {
      flex: 1;
      
      .stat-value {
        font-size: 28px;
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

.chart-card {
  min-height: 400px;
  
  .chart-container {
    height: 320px;
    
    .status-chart,
    .trend-chart {
      height: 100%;
    }
  }
}

.table-card {
  .table-footer {
    margin-top: 16px;
    text-align: center;
  }
}

.alert-card {
  .alert-list {
    min-height: 300px;
    
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 300px;
      color: #9ca3af;
      
      p {
        margin-top: 12px;
        font-size: 14px;
      }
    }
    
    .alert-items {
      .alert-item {
        display: flex;
        align-items: center;
        padding: 12px;
        margin-bottom: 8px;
        border-radius: 8px;
        cursor: pointer;
        transition: background-color 0.3s;
        
        &:hover {
          background-color: #f9fafb;
        }
        
        .alert-icon {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;
        }
        
        &.error .alert-icon {
          background: rgba(220, 38, 38, 0.1);
          color: #dc2626;
        }
        
        &.warning .alert-icon {
          background: rgba(217, 119, 6, 0.1);
          color: #d97706;
        }
        
        &.info .alert-icon {
          background: rgba(8, 145, 178, 0.1);
          color: #0891b2;
        }
        
        .alert-content {
          flex: 1;
          
          .alert-title {
            font-size: 14px;
            color: #374151;
            margin-bottom: 4px;
            line-height: 1.4;
          }
          
          .alert-time {
            font-size: 12px;
            color: #9ca3af;
          }
        }
        
        .alert-status {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background-color: transparent;
          
          &.unread {
            background-color: #2563eb;
          }
        }
      }
    }
  }
  
  .alert-footer {
    margin-top: 16px;
    text-align: center;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .stats-cards {
    :deep(.ivu-col) {
      margin-bottom: 16px;
    }
  }
  
  .content-cards {
    :deep(.ivu-col) {
      margin-bottom: 16px;
    }
  }
  
  .stat-card {
    height: 100px;
    
    .stat-content {
      .stat-icon {
        width: 48px;
        height: 48px;
        margin-right: 12px;
        
        :deep(.ivu-icon) {
          font-size: 24px !important;
        }
      }
      
      .stat-info {
        .stat-value {
          font-size: 24px;
        }
      }
    }
  }
}
</style>