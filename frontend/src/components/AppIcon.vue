<template>
  <component
    :is="glyph"
    v-if="glyph"
    class="app-icon"
    :class="[`app-icon--${size}`, { 'app-icon--spin': spin }]"
    :stroke-width="strokeWidth"
    :focusable="false"
    v-bind="accessibilityAttrs"
  />
</template>

<script>
import {
  Activity,
  ArrowLeft,
  ArrowRight,
  Bell,
  Bookmark,
  ChartColumnBig,
  Check,
  CircleAlert,
  CircleCheck,
  ClipboardCheck,
  Download,
  EllipsisVertical,
  Eye,
  EyeOff,
  File,
  FileText,
  FlaskConical,
  Info,
  List,
  Lock,
  LoaderCircle,
  LogOut,
  Menu,
  NotebookText,
  Plus,
  Search,
  Send,
  SquarePen,
  TestTube,
  Trash2,
  TriangleAlert,
  Upload,
  User,
  UserMinus,
  UserPlus,
  Users,
  X
} from '@lucide/vue'

// Catálogo semántico de iconos: la clave (kebab-case) expresa la intención de uso
// en la aplicación y aísla a las plantillas del nombre concreto del icono en la
// librería, de modo que un cambio de glifo se hace en un solo sitio.
const ICONS = Object.freeze({
  'activity': Activity,
  'alert-circle': CircleAlert,
  'alert-triangle': TriangleAlert,
  'arrow-left': ArrowLeft,
  'arrow-right': ArrowRight,
  'bar-chart': ChartColumnBig,
  'bell': Bell,
  'bookmark': Bookmark,
  'check': Check,
  'check-circle': CircleCheck,
  'clipboard-check': ClipboardCheck,
  'download': Download,
  'edit': SquarePen,
  'eye': Eye,
  'eye-off': EyeOff,
  'file': File,
  'file-text': FileText,
  'flask': FlaskConical,
  'info': Info,
  'list': List,
  'lock': Lock,
  'log-out': LogOut,
  'menu': Menu,
  'more-vertical': EllipsisVertical,
  'note': NotebookText,
  'plus': Plus,
  'search': Search,
  'send': Send,
  'spinner': LoaderCircle,
  'test-tube': TestTube,
  'trash': Trash2,
  'upload': Upload,
  'user': User,
  'user-minus': UserMinus,
  'user-plus': UserPlus,
  'users': Users,
  'x': X
})

const SIZES = Object.freeze(['xs', 'sm', 'md', 'lg', 'xl', '2xl', '3xl', '4xl'])

export default {
  name: 'AppIcon',
  props: {
    // Nombre semántico del icono; debe existir en ICONS.
    name: {
      type: String,
      required: true
    },
    // Tamaño lógico, resuelto a un token --icon-* por CSS global (components.css).
    size: {
      type: String,
      default: 'lg',
      validator: value => SIZES.includes(value)
    },
    // Texto accesible ya traducido. Si se indica, el icono se anuncia como
    // imagen informativa (role="img"); si no, queda como decorativo.
    label: {
      type: String,
      default: ''
    },
    strokeWidth: {
      type: [Number, String],
      default: 2
    },
    // Aplica una animación de giro continuo (indicadores de carga).
    spin: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    glyph() {
      const icon = ICONS[this.name]
      if (!icon) {
        console.warn(`[AppIcon] Icono desconocido: "${this.name}"`)
        return null
      }
      return icon
    },
    accessibilityAttrs() {
      return this.label
        ? { role: 'img', 'aria-label': this.label }
        : { 'aria-hidden': 'true' }
    }
  }
}
</script>
