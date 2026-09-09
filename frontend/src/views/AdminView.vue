<template>
  <div class="admin-page">
    <div class="page-header">
      <div class="header-icon">
        <AppIcon name="lock" size="2xl" />
      </div>
      <div>
        <h1>{{ $t('admin_zone') }}</h1>
        <p class="subtitle">{{ $t('admin_access') }}</p>
      </div>
    </div>

    <AdminMedicosView />
  </div>
</template>

<script>
import AppIcon from '@/components/AppIcon.vue'
import AdminMedicosView from '@/views/AdminMedicosView.vue'

export default {
  name: 'AdminView',
  components: { AppIcon, AdminMedicosView }
}
</script>

<style scoped>
.admin-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 2rem 1rem;
}
</style>
