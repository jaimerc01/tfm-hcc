<template>
  <div id="app">
    <AppNavbar v-if="showNavbar" />
    <router-view />
  </div>
</template>

<script>
import AppNavbar from '@/components/Navbar.vue'
export default {
  name: 'App',
  components: { AppNavbar },
  computed: {
    showNavbar() {
      const meta = this.$route && this.$route.meta ? this.$route.meta : {}
      // Ocultar en rutas de invitado (p. ej., login)
      return !meta.requiresGuest
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>
