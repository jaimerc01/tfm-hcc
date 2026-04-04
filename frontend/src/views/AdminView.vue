<template>
  <div class="page">
    <h1>{{$t('admin_zone')}}</h1>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else>{{$t('admin_access')}}</p>
  </div>
</template>

<script>
export default {
  name: 'AdminView',
    data() { return { error: '' } }
}
</script>

<style scoped>
</style>
