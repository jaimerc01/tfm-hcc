<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal" role="dialog" aria-modal="true" :aria-label="label" ref="dialog">
      <header class="modal-header">
        <slot name="header">
          <h3>{{ label }}</h3>
        </slot>
        <button class="modal-close" @click="close" aria-label="Cerrar">×</button>
      </header>
      <section class="modal-body">
        <slot />
      </section>
      <footer class="modal-footer">
        <slot name="footer" />
      </footer>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AppModal',
  props: {
    label: { type: String, default: 'Diálogo' }
  },
  emits: ['close'],
  mounted() {
    // basic escape handler
    this._onKey = (e) => { if (e.key === 'Escape') this.close() }
    document.addEventListener('keydown', this._onKey)
    // remember opener to restore focus later
    this._opener = document.activeElement
    // trap focus inside modal: focus first focusable element
    this.$nextTick(() => {
      const focusable = Array.from(this.$el.querySelectorAll('button, [href], input, textarea, select, [tabindex]:not([tabindex="-1"])'))
        .filter(el => !el.hasAttribute('disabled'))
      if (focusable.length) {
        this._firstFocusable = focusable[0]
        this._lastFocusable = focusable[focusable.length - 1]
        this._firstFocusable.focus()
      } else {
        // fallback: focus dialog container
        this.$refs.dialog && this.$refs.dialog.focus && this.$refs.dialog.setAttribute('tabindex', '-1') && this.$refs.dialog.focus()
      }
      // add keydown handler for tab trapping
      this._onTab = (e) => {
        if (e.key === 'Tab') {
          if (e.shiftKey && document.activeElement === this._firstFocusable) {
            e.preventDefault(); this._lastFocusable && this._lastFocusable.focus()
          } else if (!e.shiftKey && document.activeElement === this._lastFocusable) {
            e.preventDefault(); this._firstFocusable && this._firstFocusable.focus()
          }
        }
      }
      document.addEventListener('keydown', this._onTab)
    })
  },
  beforeUnmount() {
    document.removeEventListener('keydown', this._onKey)
    document.removeEventListener('keydown', this._onTab)
    // restore focus to opener when modal closes
    if (this._opener && this._opener.focus) {
      try { this._opener.focus() } catch (e) { /* ignore */ }
    }
  },
  methods: {
    close() { this.$emit('close') }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  background: white;
  width: 90%;
  max-width: 640px;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.35);
  overflow: hidden;
}
.modal-header { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; border-bottom:1px solid #eee }
.modal-body { padding: 16px }
.modal-footer { padding: 12px 16px; border-top:1px solid #eee; text-align:right }
.modal-close { background: transparent; border: none; font-size:20px; cursor:pointer }
</style>
