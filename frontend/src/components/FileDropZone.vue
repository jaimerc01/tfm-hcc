<template>
  <div
    class="file-drop-zone"
    :class="{ 'drag-over': isDragging }"
    role="button"
    tabindex="0"
    :aria-label="$t('upload_here')"
    @dragover="onDragOver"
    @dragleave="onDragLeave"
    @drop="onDrop"
    @keydown.enter.prevent="openFilePicker"
    @keydown.space.prevent="openFilePicker"
  >
    <input
      type="file"
      :id="inputId"
      ref="inputRef"
      :accept="accept"
      class="file-input-hidden"
      @change="onFileChange"
    />
    <label :for="inputId" class="file-drop-label" tabindex="-1">
      <AppIcon name="upload" size="3xl" class="upload-icon" :stroke-width="1.5" />
      <div class="upload-text">
        <span v-if="!modelValue" class="upload-primary">{{ $t('upload_here') }}</span>
        <span v-else class="file-selected-name">
          <AppIcon name="file" size="sm" />
          {{ modelValue.name }}
        </span>
        <span class="upload-secondary">{{ $t('file_types') }}</span>
      </div>
    </label>
  </div>
</template>

<script>
import AppIcon from './AppIcon.vue'

// Extensiones/tipos MIME por defecto para el selector nativo de archivos, alineadas
// con la lista blanca del backend (app.uploads.allowed-extensions en application.yml).
// Solo restringen lo que ofrece el diálogo del sistema operativo: la validación real
// (y la que decide si el archivo se acepta) la sigue haciendo el backend.
const DEFAULT_ACCEPT = '.pdf,.doc,.docx,.xls,.xlsx,.txt,.png,.jpg,.jpeg,.webp,.gif'

export default {
  name: 'FileDropZone',
  components: { AppIcon },
  props: {
    // Archivo actualmente seleccionado (o null). Se usa con v-model.
    modelValue: { type: File, default: null },
    // Id del <input> nativo subyacente; debe ser único si hay varias instancias en la página.
    inputId: { type: String, default: 'file-drop-input' },
    accept: { type: String, default: DEFAULT_ACCEPT }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      isDragging: false
    }
  },
  methods: {
    openFilePicker() {
      const input = this.$refs.inputRef
      if (input && typeof input.click === 'function') input.click()
    },
    onFileChange(e) {
      const file = e.target.files && e.target.files[0] ? e.target.files[0] : null
      this.$emit('update:modelValue', file)
    },
    onDragOver(e) {
      e.preventDefault()
      e.stopPropagation()
      this.isDragging = true
    },
    onDragLeave(e) {
      e.preventDefault()
      e.stopPropagation()
      this.isDragging = false
    },
    onDrop(e) {
      e.preventDefault()
      e.stopPropagation()
      this.isDragging = false

      const files = e.dataTransfer.files
      if (files && files[0]) {
        this.$emit('update:modelValue', files[0])
      }
    },
    // El <input type="file"> nativo no se vacía solo al poner modelValue a null desde
    // el padre (no es un elemento controlable por value); el padre debe llamar a este
    // método tras una subida correcta para poder volver a seleccionar el mismo archivo.
    reset() {
      if (this.$refs.inputRef) this.$refs.inputRef.value = ''
    }
  }
}
</script>

<style scoped>
.file-drop-zone {
  border: 2px dashed var(--border);
  border-radius: 12px;
  padding: 3rem 2rem;
  text-align: center;
  transition: all 0.2s ease;
  background: var(--bg-light);
}

.file-drop-zone:hover,
.file-drop-zone.drag-over {
  border-color: var(--primary-color);
  background: var(--surface-info-bg);
}

.file-input-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
  border: 0;
}

.file-drop-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
}

.upload-icon {
  color: var(--primary-color);
}

.upload-text {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.upload-primary {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.file-selected-name {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--success-color);
}

.file-selected-name svg {
  color: var(--success-color);
}

.upload-secondary {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.file-drop-zone:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 2px;
}

@media (max-width: 768px) {
  .file-drop-zone {
    padding: 2rem 1rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .file-drop-zone {
    transition: none;
  }
}
</style>
