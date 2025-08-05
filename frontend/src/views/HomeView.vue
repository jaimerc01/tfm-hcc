<template>
  <div class="home">
    <h1>Bienvenido a Historia Clínica Común</h1>
    <p>Sistema de gestión de historias clínicas</p>
    
    <div class="navigation">
      <router-link to="/login" class="nav-link">
        Iniciar Sesión
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView'
}
</script>

<style scoped>
.home {
  text-align: center;
  padding: 2rem;
}

.navigation {
  margin-top: 2rem;
}

.nav-link {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  background-color: #42b983;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: #369870;
}
</style>
