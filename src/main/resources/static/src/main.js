import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// 引入样式
import 'view-ui-plus/dist/styles/viewuiplus.css'
import './styles/index.scss'

const app = createApp(App)
const pinia = createPinia()

// 注册全局插件
app.use(pinia)
app.use(router)

// 全局配置
app.config.globalProperties.$formatDate = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  const dayjs = require('dayjs')
  return dayjs(date).format(format)
}

app.mount('#app')