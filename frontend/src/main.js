import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createI18n } from 'vue-i18n'
import es from './locales/es.json'
import gl from './locales/gl.json'
import '@/styles/shared.css'
import '@/styles/forms.css'

const i18n = createI18n({
	legacy: false,
	locale: 'es',
	fallbackLocale: 'es',
	messages: { es, gl }
})

const app = createApp(App)
app.use(router)
app.use(i18n)
app.mount('#app')
