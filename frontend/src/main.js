import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import '@/styles/shared.css'
import '@/styles/forms.css'

const app = createApp(App)
app.use(router)
app.mount('#app')
