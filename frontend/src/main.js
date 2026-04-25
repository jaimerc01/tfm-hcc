import { createApp, watch } from 'vue'
import App from './App.vue'
import router from './router'
import { createI18n } from 'vue-i18n'
import es from './locales/es.json'
import gl from './locales/gl.json'
import privacyEs from './locales/privacy_es.json'
import privacyGl from './locales/privacy_gl.json'
import '@/styles/shared.css'
import '@/styles/forms.css'

const SUPPORTED_LOCALES = ['es', 'gl']
const savedLocale = localStorage.getItem('app-locale')
const initialLocale = SUPPORTED_LOCALES.includes(savedLocale) ? savedLocale : 'es'

const i18n = createI18n({
	legacy: false,
	locale: initialLocale,
	fallbackLocale: 'es',
	messages: {
		es: { ...es, ...privacyEs },
		gl: { ...gl, ...privacyGl }
	}
})

const app = createApp(App)
app.use(router)
app.use(i18n)

document.documentElement.classList.add('js-enabled')

const jsWarning = document.getElementById('js-warning')
if (jsWarning) {
	jsWarning.remove()
}

const getLocale = () => i18n.global.locale?.value || i18n.global.locale

document.documentElement.lang = getLocale()

watch(
	() => i18n.global.locale?.value || i18n.global.locale,
	(newLocale) => {
		document.documentElement.lang = newLocale
		localStorage.setItem('app-locale', newLocale)
	}
)

app.mount('#app')
