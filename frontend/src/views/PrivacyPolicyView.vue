<template>
  <div class="page">
    <h1>{{ $t('privacy_title') }}</h1>
    <section>
      <h2>{{ $t('privacy_responsible') }}</h2>
      <p>{{ $t('privacy_responsible_text') }}</p>
    </section>
    <section>
      <h2>{{ $t('privacy_purpose') }}</h2>
      <p>{{ $t('privacy_purpose_text') }}</p>
    </section>
    <section>
      <h2>{{ $t('privacy_legal_basis') }}</h2>
      <p>{{ $t('privacy_legal_basis_text') }}</p>
    </section>
    <section>
      <h2>{{ $t('privacy_recipients') }}</h2>
      <p>{{ $t('privacy_recipients_text') }}</p>
    </section>
    <section>
      <h2>{{ $t('privacy_rights') }}</h2>
      <p>{{ $t('privacy_rights_text') }}</p>
    </section>
    <section>
      <h2>{{ $t('privacy_security') }}</h2>
      <p>{{ $t('privacy_security_text') }}</p>
    </section>
    <section>
      <h2>{{ $t('privacy_retention') }}</h2>
      <p>{{ $t('privacy_retention_text') }}</p>
    </section>
    <section>
      <h2>{{ $t('privacy_transfers') }}</h2>
      <p>{{ $t('privacy_transfers_text') }}</p>
    </section>
    <section>
      <h2>{{ $t('privacy_more_info') }}</h2>
      <p>{{ $t('privacy_more_info_text') }}</p>
    </section>
    <section>
      <h2>{{ $t('privacy_consent') }}</h2>
      <p>{{ $t('privacy_consent_text') }}</p>
    </section>
  </div>
</template>

<script>
export default { name: 'PrivacyPolicyView' }
</script>
