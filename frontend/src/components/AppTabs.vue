<template>
  <div class="tabs-nav-wrap">
    <button
      v-if="canScrollLeft"
      type="button"
      class="btn-icon tabs-scroll-btn"
      @click="scrollLeft"
      :aria-label="$t('tabs_scroll_left')"
    >
      <AppIcon name="arrow-left" size="sm" />
    </button>

    <div class="tabs-nav" ref="tabsNav" role="tablist" :aria-label="ariaLabel" @scroll="updateScrollState">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        :id="`tab-${tab.id}`"
        :ref="el => setTabRef(tab.id, el)"
        type="button"
        role="tab"
        :class="['tab-btn', { active: tab.id === modelValue }]"
        :aria-selected="tab.id === modelValue"
        :aria-controls="`tabpanel-${tab.id}`"
        :tabindex="tab.id === modelValue ? 0 : -1"
        @click="selectTab(tab.id)"
        @keydown="onTabKeydown($event, tab.id)"
      >
        {{ tab.label }}
      </button>
    </div>

    <button
      v-if="canScrollRight"
      type="button"
      class="btn-icon tabs-scroll-btn"
      @click="scrollRight"
      :aria-label="$t('tabs_scroll_right')"
    >
      <AppIcon name="arrow-right" size="sm" />
    </button>
  </div>
</template>

<script>
import AppIcon from './AppIcon.vue'

const SCROLL_STEP = 200

export default {
  name: 'AppTabs',
  components: { AppIcon },
  props: {
    // [{ id: string, label: string }]
    tabs: { type: Array, required: true },
    modelValue: { type: String, required: true },
    ariaLabel: { type: String, required: true }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      canScrollLeft: false,
      canScrollRight: false,
      tabRefs: {}
    }
  },
  watch: {
    tabs() {
      this.$nextTick(this.updateScrollState)
    }
  },
  mounted() {
    this.updateScrollState()
    window.addEventListener('resize', this.updateScrollState)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.updateScrollState)
  },
  methods: {
    setTabRef(id, el) {
      if (el) this.tabRefs[id] = el
    },
    selectTab(id) {
      this.$emit('update:modelValue', id)
    },
    updateScrollState() {
      const el = this.$refs.tabsNav
      if (!el) return
      this.canScrollLeft = el.scrollLeft > 0
      this.canScrollRight = el.scrollLeft + el.clientWidth < el.scrollWidth - 1
    },
    scrollLeft() {
      this.$refs.tabsNav?.scrollBy({ left: -SCROLL_STEP, behavior: 'smooth' })
    },
    scrollRight() {
      this.$refs.tabsNav?.scrollBy({ left: SCROLL_STEP, behavior: 'smooth' })
    },
    onTabKeydown(event, currentId) {
      const ids = this.tabs.map(t => t.id)
      const idx = ids.indexOf(currentId)
      if (idx < 0) return

      let targetIdx = idx
      if (event.key === 'ArrowRight') targetIdx = (idx + 1) % ids.length
      else if (event.key === 'ArrowLeft') targetIdx = (idx - 1 + ids.length) % ids.length
      else if (event.key === 'Home') targetIdx = 0
      else if (event.key === 'End') targetIdx = ids.length - 1
      else return

      event.preventDefault()
      const targetId = ids[targetIdx]
      this.selectTab(targetId)
      this.$nextTick(() => {
        this.tabRefs[targetId]?.focus()
      })
    }
  }
}
</script>

<style scoped>
.tabs-nav-wrap {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  border-bottom: 2px solid var(--border);
  margin-bottom: 1.5rem;
}

.tabs-scroll-btn {
  flex-shrink: 0;
}

.tabs-nav {
  display: flex;
  flex: 1;
  min-width: 0;
  gap: 0.5rem;
  overflow-x: auto;
  scrollbar-width: none;
}

.tabs-nav::-webkit-scrollbar {
  display: none;
}

.tab-btn {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 1.25rem;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tab-btn:hover {
  color: var(--primary-color);
  background: var(--bg-light);
}

.tab-btn:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 2px;
}

.tab-btn.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}
</style>
