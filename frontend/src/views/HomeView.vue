<template>
  <div class="landing">
    <header class="hero">
      <div class="hero__inner">
        <div class="hero__content">
          <p class="hero__eyebrow">{{ $t('home_hero_eyebrow') }}</p>
          <h1 class="hero__title">{{ $t('home_hero_title') }}</h1>
          <p class="hero__subtitle">{{ $t('home_hero_subtitle') }}</p>
          <div class="hero__actions">
            <router-link to="/login" class="btn btn--primary">{{ $t('home_cta_login') }}</router-link>
            <router-link to="/register" class="btn btn--ghost">{{ $t('home_cta_register') }}</router-link>
          </div>
        </div>

        <div class="hero__art" aria-hidden="true">
          <svg viewBox="0 0 240 200" role="presentation" focusable="false">
            <rect class="art-panel" x="24" y="30" width="150" height="150" rx="14" />
            <line class="art-rule" x1="44" y1="60" x2="120" y2="60" />
            <line class="art-rule" x1="44" y1="78" x2="150" y2="78" />
            <polyline
              class="art-pulse"
              points="44,124 68,124 82,100 98,144 114,116 132,124 154,124"
            />
            <g class="art-shield">
              <path d="M176 96 l30 -12 l30 12 v22 c0 26 -18 40 -30 46 c-12 -6 -30 -20 -30 -46 z" />
              <polyline class="art-check" points="192,116 203,127 222,104" />
            </g>
          </svg>
        </div>
      </div>
    </header>

    <main class="landing__main">
      <section class="section section--what">
        <h2 class="section__title">{{ $t('home_what_title') }}</h2>
        <p class="section__lead">{{ $t('home_what_body') }}</p>
      </section>

      <section class="section">
        <h2 class="section__title">{{ $t('home_features_title') }}</h2>
        <ul class="features">
          <li v-for="feature in features" :key="feature" class="feature">
            <span class="circle-icon feature__icon" aria-hidden="true">
              <svg v-if="feature === 'history'" viewBox="0 0 24 24" focusable="false">
                <path d="M4 5h11l5 5v9a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1z" />
                <path d="M14 5v6h6" />
                <path d="M8 15h8M8 18h5" />
              </svg>
              <svg v-else-if="feature === 'control'" viewBox="0 0 24 24" focusable="false">
                <path d="M12 3l7 3v5c0 4.5-2.8 7.9-7 10-4.2-2.1-7-5.5-7-10V6z" />
                <path d="M9.5 12l1.8 1.8L15 10" />
              </svg>
              <svg v-else-if="feature === 'security'" viewBox="0 0 24 24" focusable="false">
                <rect x="5" y="10" width="14" height="10" rx="2" />
                <path d="M8 10V7a4 4 0 0 1 8 0v3" />
                <circle cx="12" cy="15" r="1.5" />
              </svg>
              <svg v-else viewBox="0 0 24 24" focusable="false">
                <circle cx="8.5" cy="9" r="3" />
                <circle cx="16" cy="10.5" r="2.5" />
                <path d="M3.5 19c0-2.8 2.2-5 5-5s5 2.2 5 5" />
                <path d="M14.5 19c0-2.2 1.4-4.1 3.3-4.7" />
              </svg>
            </span>
            <h3 class="feature__title">{{ $t(`home_feature_${feature}_title`) }}</h3>
            <p class="feature__body">{{ $t(`home_feature_${feature}_body`) }}</p>
          </li>
        </ul>
      </section>

      <section class="section section--steps">
        <h2 class="section__title">{{ $t('home_steps_title') }}</h2>
        <ol class="steps">
          <li v-for="(step, index) in steps" :key="step" class="step">
            <span class="step__num" aria-hidden="true">{{ index + 1 }}</span>
            <div class="step__text">
              <h3 class="step__title">{{ $t(`home_${step}_title`) }}</h3>
              <p class="step__body">{{ $t(`home_${step}_body`) }}</p>
            </div>
          </li>
        </ol>
      </section>

      <section class="section section--cta">
        <h2 class="section__title">{{ $t('home_final_cta_title') }}</h2>
        <p class="section__lead">{{ $t('home_final_cta_body') }}</p>
        <div class="hero__actions hero__actions--center">
          <router-link to="/login" class="btn btn--primary">{{ $t('home_cta_login') }}</router-link>
          <router-link to="/register" class="btn btn--outline">{{ $t('home_cta_register') }}</router-link>
        </div>
      </section>
    </main>

    <footer class="landing__footer">
      <p class="landing__footer-tagline">{{ $t('home_footer_tagline') }}</p>
      <nav class="landing__footer-links" :aria-label="$t('home_footer_nav_aria')">
        <router-link to="/privacidad">{{ $t('home_footer_privacy') }}</router-link>
        <router-link to="/cookies">{{ $t('home_footer_cookies') }}</router-link>
      </nav>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
    return {
      features: ['history', 'control', 'security', 'roles'],
      steps: ['step1', 'step2', 'step3']
    }
  }
}
</script>

<style scoped>
.landing {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-light);
  color: var(--text-primary);
}

/* ---------- Hero ---------- */
.hero {
  background: var(--gradient-primary);
  color: var(--text-inverse);
  /* Top padding clears the fixed language switch (App.vue) so it never overlaps the hero. */
  padding: 5.5rem 1.5rem 4rem;
}

.hero__inner {
  max-width: 1080px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 3rem;
}

.hero__content {
  flex: 1 1 60%;
}

.hero__eyebrow {
  margin: 0 0 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--white-alpha-90);
}

.hero__title {
  margin: 0 0 1rem;
  font-size: clamp(1.9rem, 4vw, 2.9rem);
  line-height: 1.15;
  font-weight: 700;
}

.hero__subtitle {
  margin: 0 0 2rem;
  max-width: 46ch;
  font-size: 1.1rem;
  line-height: 1.6;
  color: var(--white-alpha-90);
}

.hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.hero__actions--center {
  justify-content: center;
}

.hero__art {
  flex: 1 1 40%;
  display: flex;
  justify-content: center;
}

.hero__art svg {
  width: 100%;
  max-width: 320px;
  height: auto;
}

.art-panel {
  fill: var(--white-alpha-20);
  stroke: var(--white-alpha-30);
  stroke-width: 2;
}

.art-rule {
  stroke: var(--white-alpha-30);
  stroke-width: 4;
  stroke-linecap: round;
}

.art-pulse {
  fill: none;
  stroke: var(--text-inverse);
  stroke-width: 3;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.art-shield path {
  fill: var(--secondary-color);
  stroke: var(--text-inverse);
  stroke-width: 2;
  stroke-linejoin: round;
}

.art-check {
  fill: none;
  stroke: var(--text-inverse);
  stroke-width: 3;
  stroke-linecap: round;
  stroke-linejoin: round;
}

/* ---------- Buttons ---------- */
.btn {
  display: inline-block;
  padding: 0.8rem 1.6rem;
  border-radius: var(--radius);
  font-size: 1rem;
  font-weight: 600;
  text-decoration: none;
  border: 2px solid transparent;
  transition:
    background-color var(--transition-fast),
    color var(--transition-fast),
    border-color var(--transition-fast);
}

.btn--primary {
  background: var(--button-color);
  color: var(--on-button);
}

.btn--primary:hover {
  background: var(--button-hover);
}

.btn--ghost {
  color: var(--text-inverse);
  border-color: var(--white-alpha-30);
}

.btn--ghost:hover {
  background: var(--white-alpha-20);
}

.btn--outline {
  color: var(--button-color);
  border-color: var(--button-color);
}

.btn--outline:hover {
  background: var(--success-soft-bg);
}

.btn:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 3px;
}

/* ---------- Sections ---------- */
.landing__main {
  flex: 1;
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  padding: 1rem 1.5rem 3rem;
}

.section {
  padding: 3rem 0;
  border-bottom: 1px solid var(--border);
}

.section:last-child {
  border-bottom: none;
}

.section__title {
  margin: 0 0 1rem;
  font-size: clamp(1.4rem, 2.5vw, 1.9rem);
  font-weight: 700;
  color: var(--primary-color);
}

.section__lead {
  margin: 0;
  max-width: 68ch;
  font-size: 1.05rem;
  line-height: 1.7;
  color: var(--text-secondary);
}

.section--what,
.section--cta {
  text-align: center;
}

.section--what .section__lead,
.section--cta .section__lead {
  margin: 0 auto;
}

.section--cta .hero__actions {
  margin-top: 1.75rem;
}

/* ---------- Features ---------- */
.features {
  list-style: none;
  margin: 1.5rem 0 0;
  padding: 0;
  display: grid;
  gap: 1.25rem;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
}

.feature {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
}

.feature__icon {
  margin-bottom: 1rem;
}

.feature__title {
  margin: 0 0 0.5rem;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.feature__body {
  margin: 0;
  font-size: 0.98rem;
  line-height: 1.6;
  color: var(--text-secondary);
}

/* ---------- Steps ---------- */
.steps {
  list-style: none;
  counter-reset: none;
  margin: 1.5rem 0 0;
  padding: 0;
  display: grid;
  gap: 1.25rem;
}

.step {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.step__num {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 50%;
  background: var(--primary-color);
  color: var(--on-primary);
  font-weight: 700;
}

.step__title {
  margin: 0.2rem 0 0.35rem;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.step__body {
  margin: 0;
  font-size: 0.98rem;
  line-height: 1.6;
  color: var(--text-secondary);
}

/* ---------- Footer ---------- */
.landing__footer {
  background: var(--footer-bg);
  color: var(--white-alpha-90);
  text-align: center;
  padding: 2rem 1.5rem;
}

.landing__footer-tagline {
  margin: 0 0 0.75rem;
  font-size: 0.92rem;
}

.landing__footer-links {
  display: flex;
  gap: 1.5rem;
  justify-content: center;
  flex-wrap: wrap;
}

.landing__footer-links a {
  color: var(--text-inverse);
  text-decoration: none;
  font-size: 0.92rem;
  font-weight: 500;
}

.landing__footer-links a:hover {
  text-decoration: underline;
}

.landing__footer-links a:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 3px;
}

/* ---------- Responsive ---------- */
@media (max-width: 860px) {
  .hero {
    /* Extra top room: the language switch sits above the centred illustration here. */
    padding: 5.5rem 1.5rem 3rem;
  }

  .hero__inner {
    flex-direction: column;
    text-align: center;
    gap: 2.5rem;
  }

  .hero__subtitle {
    margin-left: auto;
    margin-right: auto;
  }

  .hero__actions {
    justify-content: center;
  }

  .hero__art {
    order: -1;
  }

  .hero__art svg {
    max-width: 220px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .btn {
    transition: none;
  }
}
</style>
