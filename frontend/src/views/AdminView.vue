<template>
  <div class="page">
    <h1>Zona Administración</h1>
    <p v-if="error" class="error">{{ error }}</p>
      <p v-else>Acceso de administrador.</p>
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
