<template>
  <div class="page">
    <h1>{{ $t('cookies_title') }}</h1>

    <section>
      <h2>{{ $t('cookies_what_are') }}</h2>
      <p>{{ $t('cookies_what_are_text') }}</p>
    </section>

    <section>
      <h2>{{ $t('cookies_what_use') }}</h2>
      <p>{{ $t('cookies_what_use_text') }}</p>
    </section>

    <section>
      <h2>{{ $t('cookies_manage') }}</h2>
      <p>{{ $t('cookies_manage_text') }}</p>
    </section>

    <p>{{ $t('cookies_last_update') }}</p>
  </div>
</template>

<script>
export default { name: 'PoliticaCookiesView' }
</script>
