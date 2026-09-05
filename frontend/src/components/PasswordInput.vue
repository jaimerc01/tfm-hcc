<template>
  <label class="form-label" :for="id">{{ label }} <span v-if="required" class="required" aria-hidden="true">*</span></label>
  <div class="password-input-wrap">
    <input
      :id="id"
      ref="inputRef"
      :type="visible ? 'text' : 'password'"
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      class="form-input"
      :autocomplete="autocomplete"
      :minlength="minlength || undefined"
      :required="required"
    />
    <button
      type="button"
      class="btn-icon password-visibility-btn"
      :aria-pressed="visible ? 'true' : 'false'"
      :aria-label="visible ? $t('hide_password') : $t('show_password')"
      :title="visible ? $t('hide_password') : $t('show_password')"
      @click="visible = !visible"
    >
      <AppIcon :name="visible ? 'eye-off' : 'eye'" size="sm" />
    </button>
  </div>
  <p v-if="hint" class="field-hint">{{ hint }}</p>
</template>

<script>
import AppIcon from './AppIcon.vue'

export default {
  name: 'PasswordInput',
  components: { AppIcon },
  props: {
    modelValue: { type: String, default: '' },
    id: { type: String, required: true },
    label: { type: String, required: true },
    required: { type: Boolean, default: false },
    minlength: { type: [String, Number], default: null },
    autocomplete: { type: String, default: 'current-password' },
    hint: { type: String, default: '' }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      visible: false
    }
  },
  methods: {
    // Vuelve a ocultar el valor; lo usan las pantallas que reinician el
    // formulario (p. ej. al cancelar un cambio de contraseña) para no dejar
    // el campo en texto plano la próxima vez que se abra.
    reset() {
      this.visible = false
    }
  }
}
</script>

<style scoped>
.password-input-wrap {
  position: relative;
}

.password-input-wrap .form-input {
  padding-right: 2.75rem;
}

.password-visibility-btn {
  position: absolute;
  top: 50%;
  right: 0.4rem;
  transform: translateY(-50%);
  width: 2.25rem;
  height: 2.25rem;
  padding: 0;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--card-bg);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color var(--transition-fast), border-color var(--transition-fast), color var(--transition-fast);
}

/* El sistema de botones global (shared.css) anima `transform` en :hover/:active
   para dar un efecto de elevación; aquí ese transform ya se usa para centrar
   el botón en vertical (position: absolute + translateY(-50%)), así que hay
   que fijarlo explícitamente en cada estado o el botón "salta" al pasar el
   ratón o al pulsarlo. */
.password-visibility-btn:hover,
.password-visibility-btn:active {
  transform: translateY(-50%);
  background: var(--button-color);
  border-color: var(--button-color);
  color: var(--on-button);
}

.password-visibility-btn:focus-visible {
  transform: translateY(-50%);
  outline: 2px solid var(--focus-color);
  outline-offset: 1px;
}

.field-hint {
  margin: 0.4rem 0 0 0;
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

@media (prefers-reduced-motion: reduce) {
  .password-visibility-btn {
    transition: none;
  }
}
</style>
