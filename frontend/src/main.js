import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import './style.css'
import App from './App.vue'
import router from './router'

// 挂载顺序：路由 → UI 组件库 → 根组件
createApp(App)
  .use(router)
  .use(ElementPlus)
  .mount('#app')
