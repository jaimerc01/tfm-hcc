<template>
  <div class="home">
    <h1>{{$t('welcome_title')}}</h1>
    <p>{{$t('welcome_subtitle')}}</p>
    <div class="navigation">
      <router-link to="/login" class="nav-link">
        {{$t('login')}}
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
