# 主题切换功能说明

## 功能介绍

本项目现在支持两种视觉主题，用户可以根据个人喜好进行切换：

### 1. 现代化主题 (默认)
- **特点**：简洁、专业、现代感强
- **配色方案**：
  - 主色调：紫色渐变 (#6366f1 → #8b5cf6)
  - 成功色：绿色 (#10b981)
  - 警告色：橙色 (#f59e0b)
  - 错误色：红色 (#ef4444)
- **设计元素**：
  - 圆角卡片设计
  - 渐变背景
  - 浮动阴影效果
  - 平滑过渡动画

### 2. 马里奥像素风主题
- **特点**：复古游戏风格，趣味性强
- **配色方案**：
  - 主色调：马里奥红 (#e52521)
  - 辅助色：金币黄 (#fbd000)
  - 成功色：绿色 (#43b244)
  - 警告色：橙色 (#ff7f27)
- **设计元素**：
  - 像素化边框
  - 砖块纹理背景
  - 按钮按下效果
  - 复古字体风格

## 使用方法

### 切换主题
1. 在页面右上角找到主题切换器
2. 点击「🖥️ 现代化」或「🍄 马里奥」按钮
3. 页面会立即切换到对应主题
4. 用户偏好会被保存在浏览器本地存储中

### 技术实现
- 使用CSS自定义属性（CSS Variables）管理主题颜色
- 通过`data-theme`属性动态切换主题
- 利用localStorage持久化用户偏好设置
- Vue.js响应式更新确保UI一致性

## 文件结构

```
src/main/resources/
├── static/
│   └── css/
│       └── themes.css          # 主题样式文件
└── templates/
    └── index.html              # 主页面（包含主题切换逻辑）
```

## 自定义主题

如需添加新主题：

1. 在`themes.css`中添加新的主题变量定义：
```css
:root[data-theme="your-theme"] {
    --primary-color: #your-color;
    --bg-primary: #your-bg-color;
    /* 更多变量... */
}
```

2. 在HTML中添加对应的切换按钮：
```html
<button class="theme-btn" 
        :class="{ 'active': currentTheme === 'your-theme' }"
        @click="switchTheme('your-theme')">
    🎨 你的主题名
</button>
```

3. 在Vue组件中初始化主题：
```javascript
data() {
    return {
        currentTheme: 'your-theme', // 设置默认主题
        // 其他数据...
    }
}
```

## 注意事项

- 主题切换是实时的，无需刷新页面
- 用户偏好会在浏览器关闭后仍然保持
- 所有Bootstrap组件都会相应地更新样式
- 移动端也完全适配两种主题
