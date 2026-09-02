<template>
  <div class="page-container-narrow">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
      </div>
      <div>
        <h1>{{ $t('my_personal_data') }}</h1>
        <p class="subtitle">{{ $t('manage_personal_info') }}</p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>{{ $t('loading_info') }}</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-danger" role="alert">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      {{ error }}
    </div>

    <!-- No Data State -->
    <div v-else-if="!user" class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
        <circle cx="12" cy="7" r="4"></circle>
      </svg>
      <h3>{{ $t('no_data_loaded') }}</h3>
      <p>{{ $t('reload_page') }}</p>
    </div>

    <!-- Data Content -->
    <div v-else class="user-data-content">
      <!-- View Mode -->
      <template v-if="!editMode">
        <div class="panel-card">
          <div class="panel-header">
            <h2>{{ $t('personal_info') }}</h2>
            <p class="panel-subtitle">{{ $t('review_update_personal') }}</p>
          </div>

          <div class="data-grid">
            <div class="data-item">
              <div class="data-label">{{ $t('name') }}</div>
              <div class="data-value">{{ user.nombre }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">{{ $t('first_surname') }}</div>
              <div class="data-value">{{ user.apellido1 }}</div>
            </div>
            <div class="data-item" v-if="user.apellido2">
              <div class="data-label">{{ $t('second_surname') }}</div>
              <div class="data-value">{{ user.apellido2 }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">{{ $t('nif') }}</div>
              <div class="data-value">{{ user.nif }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">{{ $t('email') }}</div>
              <div class="data-value">{{ user.email }}</div>
            </div>
            <div class="data-item" v-if="user.telefono">
              <div class="data-label">{{ $t('phone') }}</div>
              <div class="data-value">{{ user.telefono }}</div>
            </div>
            <div class="data-item" v-if="user.fechaNacimiento">
              <div class="data-label">{{ $t('birth_date') }}</div>
              <div class="data-value">{{ formatDate(user.fechaNacimiento) }}</div>
            </div>
            <div class="data-item" v-if="user.fechaCreacion">
              <div class="data-label">{{ $t('registration_date') }}</div>
              <div class="data-value">{{ formatDateTime(user.fechaCreacion) }}</div>
            </div>
            <div class="data-item" v-if="user.fechaUltimaModificacion">
              <div class="data-label">{{ $t('last_modification') }}</div>
              <div class="data-value">{{ formatDateTime(user.fechaUltimaModificacion) }}</div>
            </div>
          </div>

          <div class="form-group reauth-group">
            <label class="form-label" for="reauth-password">{{ $t('current_password_confirm_label') }}</label>
            <div class="password-input-wrap">
              <input
                id="reauth-password"
                :type="showReauthPassword ? 'text' : 'password'"
                v-model="reauth.password"
                class="form-input"
                autocomplete="current-password"
              />
              <button
                type="button"
                class="password-visibility-btn"
                :aria-pressed="showReauthPassword ? 'true' : 'false'"
                :aria-label="showReauthPassword ? $t('hide_password') : $t('show_password')"
                @click="showReauthPassword = !showReauthPassword"
              >
                {{ showReauthPassword ? $t('hide_password') : $t('show_password') }}
              </button>
            </div>
            <p class="field-hint">{{ $t('current_password_confirm_help') }}</p>
          </div>

          <div class="form-actions">
            <button
              type="button"
              class="btn-primary"
              @click="startEdit"
              :aria-label="$t('aria_edit_personal_data')">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
              </svg>
              {{ $t('edit_data') }}
            </button>
            <button
              type="button"
              class="btn-secondary"
              @click="exportData"
              :disabled="exportLoading"
              :aria-label="exportLoading ? $t('aria_export_personal_data_loading') : $t('aria_export_personal_data')">
              <svg v-if="!exportLoading" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
              <div v-else class="spinner-small"></div>
              {{ exportLoading ? $t('exporting') : $t('download_my_data') }}
            </button>
            <button
              type="button"
              class="btn-danger"
              @click="openDelete($event)"
              :aria-label="$t('aria_open_delete_account_modal')"
            >
              {{ $t('delete_account') }}
            </button>
          </div>

          <div v-if="exportError" class="alert alert-danger" role="alert">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ exportError }}
          </div>

          <div v-if="exportSuccess" class="alert alert-success" role="status" aria-live="polite">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
            {{ exportSuccess }}
          </div>
        </div>
      </template>
      <!-- Edit Mode -->
      <template v-else>
        <div class="panel-card">
          <div class="panel-header">
            <h2>{{ $t('edit_personal_info') }}</h2>
            <p class="panel-subtitle">{{ $t('update_personal_data') }}</p>
          </div>

          <form @submit.prevent="submitEdit">
            <div class="form-grid">
              <div class="form-group">
                <label class="form-label" for="edit-name">{{ $t('name') }} <span class="required">*</span></label>
                <input id="edit-name" type="text" v-model="form.nombre" class="form-input" autocomplete="given-name" required />
              </div>
              <div class="form-group">
                <label class="form-label" for="edit-first-surname">{{ $t('first_surname') }} <span class="required">*</span></label>
                <input id="edit-first-surname" type="text" v-model="form.apellido1" class="form-input" autocomplete="family-name" required />
              </div>
              <div class="form-group">
                <label class="form-label" for="edit-second-surname">{{ $t('second_surname') }}</label>
                <input id="edit-second-surname" type="text" v-model="form.apellido2" class="form-input" autocomplete="additional-name" />
              </div>
              <div class="form-group">
                <label class="form-label" for="edit-nif">{{ $t('nif') }} <span class="required">*</span></label>
                <input id="edit-nif" type="text" v-model="form.nif" class="form-input" autocomplete="off" required />
              </div>
              <div class="form-group">
                <label class="form-label" for="edit-email">{{ $t('email') }} <span class="required">*</span></label>
                <input id="edit-email" type="email" v-model="form.email" class="form-input" autocomplete="email" required />
              </div>
              <div class="form-group">
                <label class="form-label" for="edit-phone">{{ $t('phone') }}</label>
                <input id="edit-phone" type="text" v-model="form.telefono" class="form-input" autocomplete="tel" />
              </div>
              <div class="form-group full-width">
                <label class="form-label" for="edit-birth-date">{{ $t('birth_date') }}</label>
                <input id="edit-birth-date" type="date" v-model="form.fechaNacimiento" class="form-input" autocomplete="bday" />
              </div>
            </div>

            <div v-if="editError" class="alert alert-danger" role="alert">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {{ editError }}
            </div>

            <div v-if="editSuccess" class="alert alert-success" role="status" aria-live="polite">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              {{ $t('edit_success') }}
            </div>

            <div v-if="nifChanged" class="alert alert-warning" role="alert">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
              {{ $t('nif_changed') }}
            </div>

            <div class="form-actions form-actions--right">
              <button 
                type="button" 
                class="btn-secondary"
                @click="cancelEdit" 
                :disabled="editLoading"
                :aria-label="$t('cancel_edit')">
                {{ $t('cancel') }}
              </button>
              <button 
                type="submit" 
                class="btn-primary"
                :disabled="editLoading"
                :aria-label="editLoading ? $t('aria_save_personal_data_loading') : $t('aria_save_personal_data')">
                <div v-if="editLoading" class="spinner-small"></div>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                {{ editLoading ? $t('saving') : $t('save_changes') }}
              </button>
            </div>
          </form>
        </div>
      </template>
      <!-- Password Section -->
      <div class="section">
        <div class="panel-card">
          <div class="panel-header">
            <h2>{{ $t('change_password') }}</h2>
            <p class="panel-subtitle">{{ $t('update_password') }}</p>
          </div>

          <button 
            v-if="!showPwForm" 
            type="button"
            class="btn-secondary" 
            @click="showPwForm=true"
            :aria-label="$t('show_pw_form')">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect width="18" height="11" x="3" y="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            {{ $t('change_password') }}
          </button>

          <form v-else @submit.prevent="submitPw">
            <div class="info-box">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M12 16v-4"></path>
                <path d="M12 8h.01"></path>
              </svg>
              <span>{{ $t('password_update_help') }}</span>
            </div>

            <div class="form-grid">
              <div class="form-group full-width">
                <label class="form-label" for="pw-current">{{ $t('current_password') }} <span class="required">*</span></label>
                <div class="password-input-wrap">
                  <input id="pw-current" :type="showPwCurrent ? 'text' : 'password'" v-model="pw.current" class="form-input" autocomplete="current-password" required />
                  <button
                    type="button"
                    class="password-visibility-btn"
                    :aria-pressed="showPwCurrent ? 'true' : 'false'"
                    :aria-label="showPwCurrent ? $t('hide_password') : $t('show_password')"
                    @click="showPwCurrent = !showPwCurrent"
                  >
                    {{ showPwCurrent ? $t('hide_password') : $t('show_password') }}
                  </button>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label" for="pw-new1">{{ $t('new_password') }} <span class="required">*</span></label>
                <div class="password-input-wrap">
                  <input id="pw-new1" :type="showPwNew1 ? 'text' : 'password'" v-model="pw.new1" class="form-input" autocomplete="new-password" minlength="6" required />
                  <button
                    type="button"
                    class="password-visibility-btn"
                    :aria-pressed="showPwNew1 ? 'true' : 'false'"
                    :aria-label="showPwNew1 ? $t('hide_password') : $t('show_password')"
                    @click="showPwNew1 = !showPwNew1"
                  >
                    {{ showPwNew1 ? $t('hide_password') : $t('show_password') }}
                  </button>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label" for="pw-new2">{{ $t('repeat_new_password') }} <span class="required">*</span></label>
                <div class="password-input-wrap">
                  <input id="pw-new2" :type="showPwNew2 ? 'text' : 'password'" v-model="pw.new2" class="form-input" autocomplete="new-password" minlength="6" required />
                  <button
                    type="button"
                    class="password-visibility-btn"
                    :aria-pressed="showPwNew2 ? 'true' : 'false'"
                    :aria-label="showPwNew2 ? $t('hide_password') : $t('show_password')"
                    @click="showPwNew2 = !showPwNew2"
                  >
                    {{ showPwNew2 ? $t('hide_password') : $t('show_password') }}
                  </button>
                </div>
              </div>
            </div>

            <div v-if="pwError" class="alert alert-danger" role="alert">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {{ pwError }}
            </div>

            <div v-if="pwSuccess" class="alert alert-success" role="status" aria-live="polite">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              {{ $t('password_changed') }}
            </div>

            <div class="form-actions form-actions--right">
              <button 
                type="button" 
                class="btn-secondary"
                @click="cancelPw" 
                :disabled="pwLoading"
                :aria-label="$t('cancel_pw')">
                {{ $t('cancel') }}
              </button>
              <button 
                type="submit" 
                class="btn-primary"
                :disabled="pwLoading"
                :aria-label="pwLoading ? $t('aria_save_new_password_loading') : $t('save_new_password')">
                <div v-if="pwLoading" class="spinner-small"></div>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                {{ pwLoading ? $t('saving') : $t('save_new_password') }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Two-Factor Authentication Section -->
      <div class="section">
        <div class="panel-card">
          <div class="panel-header">
            <h2>{{ $t('two_factor_section_title') }}</h2>
            <p class="panel-subtitle">{{ $t('two_factor_section_subtitle') }}</p>
          </div>

          <div v-if="totpStatusLoading" class="loading-container">
            <div class="spinner"></div>
          </div>

          <template v-else>
            <!-- Estado: sin configuración de segundo factor activa ni en curso -->
            <template v-if="!totpEnabled && !showTotpSetup">
              <p class="two-factor-status">
                <span class="status-badge status-badge--off">{{ $t('two_factor_disabled_status') }}</span>
              </p>
              <button
                type="button"
                class="btn-secondary"
                @click="startTotpSetup"
                :disabled="totpSetupLoading"
                :aria-label="$t('two_factor_enable_button')"
              >
                <div v-if="totpSetupLoading" class="spinner-small"></div>
                {{ totpSetupLoading ? $t('loading_info') : $t('two_factor_enable_button') }}
              </button>

              <div v-if="totpSetupError" class="alert alert-danger" role="alert">
                {{ totpSetupError }}
              </div>
            </template>

            <!-- Configuración en curso: mostrar QR + secreto + código de confirmación -->
            <template v-else-if="showTotpSetup">
              <div class="info-box">
                <span>{{ $t('two_factor_setup_instructions') }}</span>
              </div>

              <div class="totp-qr-wrap">
                <img v-if="totpQrDataUrl" :src="totpQrDataUrl" :alt="$t('two_factor_qr_alt')" class="totp-qr" />
                <p class="totp-secret">
                  <span class="data-label">{{ $t('two_factor_secret_label') }}</span>
                  <code>{{ totpSetupData?.secret }}</code>
                </p>
              </div>

              <form @submit.prevent="confirmTotpSetup">
                <div class="form-group">
                  <label class="form-label" for="totp-confirm-code">{{ $t('two_factor_code_label') }} <span class="required">*</span></label>
                  <input
                    id="totp-confirm-code"
                    v-model="totpConfirmCode"
                    type="text"
                    inputmode="numeric"
                    maxlength="6"
                    class="form-input"
                    autocomplete="one-time-code"
                    :placeholder="$t('two_factor_code_placeholder')"
                    required
                  />
                </div>

                <div v-if="totpSetupError" class="alert alert-danger" role="alert">
                  {{ totpSetupError }}
                </div>

                <div class="form-actions form-actions--right">
                  <button type="button" class="btn-secondary" @click="cancelTotpSetup" :disabled="totpSetupLoading">
                    {{ $t('cancel') }}
                  </button>
                  <button type="submit" class="btn-primary" :disabled="totpSetupLoading">
                    <div v-if="totpSetupLoading" class="spinner-small"></div>
                    {{ totpSetupLoading ? $t('saving') : $t('two_factor_confirm_button') }}
                  </button>
                </div>
              </form>
            </template>

            <!-- Segundo factor activo: permitir desactivarlo -->
            <template v-else>
              <p class="two-factor-status">
                <span class="status-badge status-badge--on">{{ $t('two_factor_enabled_status') }}</span>
              </p>

              <button
                v-if="!showTotpDisable"
                type="button"
                class="btn-danger"
                @click="showTotpDisable = true"
                :aria-label="$t('two_factor_disable_button')"
              >
                {{ $t('two_factor_disable_button') }}
              </button>

              <form v-else @submit.prevent="confirmTotpDisable">
                <div class="info-box">
                  <span>{{ $t('two_factor_disable_help') }}</span>
                </div>

                <div class="form-group">
                  <label class="form-label" for="totp-disable-code">{{ $t('two_factor_code_label') }} <span class="required">*</span></label>
                  <input
                    id="totp-disable-code"
                    v-model="totpDisableCode"
                    type="text"
                    inputmode="numeric"
                    maxlength="6"
                    class="form-input"
                    autocomplete="one-time-code"
                    :placeholder="$t('two_factor_code_placeholder')"
                    required
                  />
                </div>

                <div v-if="totpDisableError" class="alert alert-danger" role="alert">
                  {{ totpDisableError }}
                </div>

                <div class="form-actions form-actions--right">
                  <button type="button" class="btn-secondary" @click="cancelTotpDisable" :disabled="totpDisableLoading">
                    {{ $t('cancel') }}
                  </button>
                  <button type="submit" class="btn-danger" :disabled="totpDisableLoading">
                    <div v-if="totpDisableLoading" class="spinner-small"></div>
                    {{ totpDisableLoading ? $t('saving') : $t('two_factor_disable_button') }}
                  </button>
                </div>
              </form>
            </template>
          </template>
        </div>
      </div>

      <!-- Processing Restriction Section (GDPR Art. 18) -->
      <div class="section">
        <div class="panel-card">
          <div class="panel-header">
            <h2>{{ $t('processing_restriction_section_title') }}</h2>
            <p class="panel-subtitle">{{ $t('processing_restriction_section_subtitle') }}</p>
          </div>

          <div class="info-box">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <path d="M12 16v-4"></path>
              <path d="M12 8h.01"></path>
            </svg>
            <span>{{ $t('processing_restriction_help') }}</span>
          </div>

          <p class="two-factor-status">
            <span :class="['status-badge', user.estadoCuenta === ESTADO_CUENTA_SUSPENDIDO ? 'status-badge--on' : 'status-badge--off']">
              {{ user.estadoCuenta === ESTADO_CUENTA_SUSPENDIDO ? $t('processing_restriction_active_status') : $t('processing_restriction_inactive_status') }}
            </span>
          </p>

          <button
            v-if="user.estadoCuenta !== ESTADO_CUENTA_SUSPENDIDO"
            type="button"
            class="btn-secondary"
            @click="enableProcessingRestriction"
            :disabled="processingRestrictionLoading"
            :aria-label="processingRestrictionLoading ? $t('aria_enable_processing_restriction_loading') : $t('aria_enable_processing_restriction')">
            <div v-if="processingRestrictionLoading" class="spinner-small"></div>
            {{ processingRestrictionLoading ? $t('saving') : $t('processing_restriction_enable_button') }}
          </button>
          <button
            v-else
            type="button"
            class="btn-primary"
            @click="disableProcessingRestriction"
            :disabled="processingRestrictionLoading"
            :aria-label="processingRestrictionLoading ? $t('aria_disable_processing_restriction_loading') : $t('aria_disable_processing_restriction')">
            <div v-if="processingRestrictionLoading" class="spinner-small"></div>
            {{ processingRestrictionLoading ? $t('saving') : $t('processing_restriction_disable_button') }}
          </button>

          <div v-if="processingRestrictionError" class="alert alert-danger" role="alert">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ processingRestrictionError }}
          </div>
        </div>
      </div>

      <!-- Access Log Section (derecho de acceso / trazabilidad, RGPD) -->
      <div class="section">
        <div class="panel-card">
          <div class="panel-header">
            <h2>{{ $t('access_log_section_title') }}</h2>
            <p class="panel-subtitle">{{ $t('access_log_section_subtitle') }}</p>
          </div>
          <AccessLogSection />
        </div>
      </div>

      <!-- Delete Account Modal -->
      <div v-if="showDelete" class="modal-overlay" role="dialog" aria-modal="true" @click.self="closeDelete">
        <div
          ref="deleteModalRef"
          class="modal"
          tabindex="-1"
          aria-labelledby="delete-account-title"
          aria-describedby="delete-account-desc"
          @keydown="onDeleteModalKeydown"
        >
          <div class="modal-header">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--danger-color)">
              <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
              <line x1="12" y1="9" x2="12" y2="13"></line>
              <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
            <h2 id="delete-account-title">{{ $t('delete_account') }}</h2>
          </div>
          <p id="delete-account-desc" class="modal-text">{{ $t('delete_account_confirm') }}</p>

          <div class="form-group">
            <label class="form-label" for="delete-reauth-password">{{ $t('current_password_confirm_label') }}</label>
            <div class="password-input-wrap">
              <input
                id="delete-reauth-password"
                :type="showReauthPassword ? 'text' : 'password'"
                v-model="reauth.password"
                class="form-input"
                autocomplete="current-password"
                required
              />
              <button
                type="button"
                class="password-visibility-btn"
                :aria-pressed="showReauthPassword ? 'true' : 'false'"
                :aria-label="showReauthPassword ? $t('hide_password') : $t('show_password')"
                @click="showReauthPassword = !showReauthPassword"
              >
                {{ showReauthPassword ? $t('hide_password') : $t('show_password') }}
              </button>
            </div>
          </div>

          <div v-if="deleteError" class="alert alert-danger" role="alert">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ deleteError }}
          </div>

          <div class="modal-actions">
            <button 
              ref="deleteCancelRef"
              type="button"
              class="btn-secondary"
              :disabled="deleteLoading" 
              @click="closeDelete" 
              :aria-label="$t('cancel_delete')">
              {{ $t('cancel') }}
            </button>
            <button 
              ref="deleteConfirmRef"
              type="button"
              class="btn-danger"
              :disabled="deleteLoading" 
              @click="confirmDelete"
              :aria-label="deleteLoading ? $t('aria_delete_account_loading') : $t('aria_delete_account_confirm')">
              <div v-if="deleteLoading" class="spinner-small"></div>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
              {{ deleteLoading ? $t('delete_loading') : $t('delete_confirm') }}
            </button>
          </div>
        </div>
      </div>
      <transition name="toast-fade">
        <div
          v-if="appToast.visible"
          :class="['app-toast', `app-toast--${appToast.type}`]"
          role="status"
          aria-live="polite"
          aria-atomic="true"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
          <span>{{ appToast.message }}</span>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import QRCode from 'qrcode'
import authService from '@/services/authService'
import { validateNIF } from '@/utils/validateNIF'
import AccessLogSection from '@/components/AccessLogSection.vue'


const ESTADO_CUENTA_SUSPENDIDO = 'SUSPENDIDO'
const ESTADO_CUENTA_ACTIVO = 'ACTIVO'

export default {
  name: 'DatosUsuarioView',
  components: { AccessLogSection },
  setup() {
    const router = useRouter()
    const { t } = useI18n()
    const user = ref(null)
    const editMode = ref(false)
    const showDelete = ref(false)
    const deleteLoading = ref(false)
    const deleteError = ref('')
    const form = ref({ nombre:'', apellido1:'', apellido2:'', nif:'', email:'', telefono:'', fechaNacimiento:'' })
    const editLoading = ref(false)
    const editError = ref('')
    const editSuccess = ref(false)
    const nifChanged = ref(false)
    const loading = ref(true)
    const error = ref('')
    const showPwForm = ref(false)
    const showPwCurrent = ref(false)
    const showPwNew1 = ref(false)
    const showPwNew2 = ref(false)
    const pw = ref({ current:'', new1:'', new2:'' })
    const pwLoading = ref(false)
    const pwError = ref('')
    const pwSuccess = ref(false)
    const exportLoading = ref(false)
    const exportError = ref('')
    const exportSuccess = ref('')
    const reauth = ref({ password: '' })
    const showReauthPassword = ref(false)
    const deleteModalRef = ref(null)
    const deleteCancelRef = ref(null)
    const deleteConfirmRef = ref(null)
    const deleteTriggerEl = ref(null)
    const appToast = ref({ visible: false, message: '', type: 'success' })
    let toastTimer = null

    // Segundo factor (TOTP)
    const totpEnabled = ref(false)
    const totpStatusLoading = ref(true)
    const showTotpSetup = ref(false)
    const totpSetupData = ref(null)
    const totpQrDataUrl = ref('')
    const totpConfirmCode = ref('')
    const totpSetupLoading = ref(false)
    const totpSetupError = ref('')
    const showTotpDisable = ref(false)
    const totpDisableCode = ref('')
    const totpDisableLoading = ref(false)
    const totpDisableError = ref('')

    // Limitación del tratamiento (derecho de limitación, art. 18 RGPD)
    const processingRestrictionLoading = ref(false)
    const processingRestrictionError = ref('')

      const showAppToast = (message, type = 'success', duration = 2600) => {
        if (!message) return
        if (toastTimer) clearTimeout(toastTimer)
        appToast.value = { visible: true, message, type }
        toastTimer = setTimeout(() => {
          appToast.value.visible = false
          toastTimer = null
        }, duration)
      }

      const load = async () => {
        loading.value = true
        error.value = ''
        try {
          user.value = await authService.fetchMyData()
        } catch (e) {
          error.value = e.message || t('error_loading_data')
        } finally {
          loading.value = false
        }
      }

      const formatDate = (d) => {
        if (!d) return ''
        const date = new Date(d)
        return date.toLocaleDateString()
      }
      const formatDateTime = (d) => {
        if (!d) return ''
        const date = new Date(d)
        return date.toLocaleString()
      }

      const submitPw = async () => {
        pwError.value = ''
        pwSuccess.value = false
        if (pw.value.new1 !== pw.value.new2) { pwError.value = t('passwords_no_match'); return }
        pwLoading.value = true
        try {
    await authService.changePassword(pw.value.current, pw.value.new1)
    pwSuccess.value = true
      showAppToast(t('password_changed'))
    pw.value = { current:'', new1:'', new2:'' }
    // Redirigir a login tras breve notificación
    setTimeout(() => { router.push({ name: 'Login' }) }, 800)
        } catch (e) {
          pwError.value = e.message || t('error_changing_password')
        } finally {
          pwLoading.value = false
        }
      }
      const cancelPw = () => {
        showPwForm.value = false
        showPwCurrent.value = false
        showPwNew1.value = false
        showPwNew2.value = false
        pw.value = { current:'', new1:'', new2:'' }
        pwError.value = ''
        pwSuccess.value = false
      }

      const loadTotpStatus = async () => {
        totpStatusLoading.value = true
        try {
          totpEnabled.value = await authService.getTotpStatus()
        } catch (e) {
          // No se bloquea el resto de la pantalla por esto: se deja como desactivado
          // y el usuario puede reintentarlo al pulsar "activar".
          totpEnabled.value = false
        } finally {
          totpStatusLoading.value = false
        }
      }

      const startTotpSetup = async () => {
        totpSetupError.value = ''
        totpSetupLoading.value = true
        try {
          const data = await authService.setupTotp()
          totpSetupData.value = data
          totpConfirmCode.value = ''
          totpQrDataUrl.value = data?.otpauthUri ? await QRCode.toDataURL(data.otpauthUri) : ''
          showTotpSetup.value = true
        } catch (e) {
          totpSetupError.value = e.message || t('two_factor_setup_error')
        } finally {
          totpSetupLoading.value = false
        }
      }

      const cancelTotpSetup = () => {
        showTotpSetup.value = false
        totpSetupData.value = null
        totpQrDataUrl.value = ''
        totpConfirmCode.value = ''
        totpSetupError.value = ''
      }

      const confirmTotpSetup = async () => {
        totpSetupError.value = ''
        totpSetupLoading.value = true
        try {
          await authService.confirmTotp(totpConfirmCode.value)
          totpEnabled.value = true
          cancelTotpSetup()
          showAppToast(t('two_factor_enabled_success'))
        } catch (e) {
          totpSetupError.value = e.message || t('two_factor_setup_error')
        } finally {
          totpSetupLoading.value = false
        }
      }

      const cancelTotpDisable = () => {
        showTotpDisable.value = false
        totpDisableCode.value = ''
        totpDisableError.value = ''
      }

      const confirmTotpDisable = async () => {
        totpDisableError.value = ''
        totpDisableLoading.value = true
        try {
          await authService.disableTotp(totpDisableCode.value)
          totpEnabled.value = false
          cancelTotpDisable()
          showAppToast(t('two_factor_disabled_success'))
        } catch (e) {
          totpDisableError.value = e.message || t('two_factor_setup_error')
        } finally {
          totpDisableLoading.value = false
        }
      }

      const enableProcessingRestriction = async () => {
        processingRestrictionError.value = ''
        processingRestrictionLoading.value = true
        try {
          await authService.limitarTratamiento()
          if (user.value) user.value.estadoCuenta = ESTADO_CUENTA_SUSPENDIDO
          showAppToast(t('processing_restriction_enable_success'))
        } catch (e) {
          processingRestrictionError.value = e.message || t('error_processing_restriction')
        } finally {
          processingRestrictionLoading.value = false
        }
      }

      const disableProcessingRestriction = async () => {
        processingRestrictionError.value = ''
        processingRestrictionLoading.value = true
        try {
          await authService.reanudarTratamiento()
          if (user.value) user.value.estadoCuenta = ESTADO_CUENTA_ACTIVO
          showAppToast(t('processing_restriction_disable_success'))
        } catch (e) {
          processingRestrictionError.value = e.message || t('error_processing_resume')
        } finally {
          processingRestrictionLoading.value = false
        }
      }

      const exportData = async () => {
        exportLoading.value = true
        exportError.value = ''
        exportSuccess.value = ''
        try {
          const json = await authService.exportMyData(reauth.value.password)
          const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' })
          const a = document.createElement('a')
          a.href = URL.createObjectURL(blob)
          a.download = `${t('user_export_filename_prefix')}_${new Date().toISOString().slice(0,10)}.json`
          a.click()
          URL.revokeObjectURL(a.href)
          exportSuccess.value = t('export_success')
          showAppToast(t('export_success'))
        } catch (e) {
          exportError.value = e.message || t('error_exporting_data')
        } finally {
          exportLoading.value = false
        }
      }

      const openDelete = async (event) => {
        deleteError.value = ''
        deleteTriggerEl.value = event?.currentTarget || null
        showDelete.value = true
        await nextTick()
        if (deleteCancelRef.value) {
          deleteCancelRef.value.focus()
        } else if (deleteModalRef.value) {
          deleteModalRef.value.focus()
        }
      }
      const closeDelete = () => {
        if (!deleteLoading.value) {
          showDelete.value = false
          nextTick(() => {
            if (deleteTriggerEl.value && typeof deleteTriggerEl.value.focus === 'function') {
              deleteTriggerEl.value.focus()
            }
            deleteTriggerEl.value = null
          })
        }
      }
      const onDeleteModalKeydown = (event) => {
        if (event.key === 'Escape') {
          event.preventDefault()
          closeDelete()
          return
        }
        if (event.key !== 'Tab') return

        const focusable = [deleteCancelRef.value, deleteConfirmRef.value].filter(Boolean)
        if (!focusable.length) return

        const first = focusable[0]
        const last = focusable[focusable.length - 1]
        const active = document.activeElement

        if (event.shiftKey && active === first) {
          event.preventDefault()
          last.focus()
        } else if (!event.shiftKey && active === last) {
          event.preventDefault()
          first.focus()
        }
      }
      const confirmDelete = async () => {
        deleteError.value=''
        deleteLoading.value = true
        try {
          await authService.deleteAccount(reauth.value.password)
          router.push({ name: 'Login' })
        } catch (e) {
          deleteError.value = e.message || t('error_deleting_account')
        } finally {
          deleteLoading.value = false
        }
      }

      const startEdit = () => {
        if (!user.value) return
        editMode.value = true
        editError.value = ''
        editSuccess.value = false
        nifChanged.value = false
        form.value = {
          nombre: user.value.nombre || '',
          apellido1: user.value.apellido1 || '',
          apellido2: user.value.apellido2 || '',
          nif: user.value.nif || '',
          email: user.value.email || '',
          telefono: user.value.telefono || '',
          fechaNacimiento: user.value.fechaNacimiento ? user.value.fechaNacimiento.slice(0, 10) : ''
        }
      }
      const cancelEdit = () => {
        editMode.value = false
        editLoading.value = false
        editError.value = ''
        editSuccess.value = false
      }
      const submitEdit = async () => {
        editError.value = ''
        editSuccess.value = false
        nifChanged.value = false
        editLoading.value = true
        try {
          // Validaciones frontend
          if (!form.value.nombre?.trim()) throw new Error(t('first_name_required'))
          if (!form.value.apellido1?.trim()) throw new Error(t('first_surname_required'))
          if (!validateNIF(form.value.nif)) throw new Error(t('invalid_nif'))
          if (!/^([^@\n]+)@([^@\n]+)\.[^@\n]+$/.test(form.value.email)) throw new Error(t('invalid_email_format'))
          if (form.value.telefono && !/^[0-9+\-() ]{0,20}$/.test(form.value.telefono)) throw new Error(t('invalid_phone_format'))
          const payload = { ...form.value }
          // El backend espera un LocalDateTime; el <input type="date"> solo da la parte de fecha (YYYY-MM-DD)
          payload.fechaNacimiento = payload.fechaNacimiento ? `${payload.fechaNacimiento}T00:00:00` : null
          const updated = await authService.updateMyData(payload)
          user.value = updated
          editSuccess.value = true
          showAppToast(t('edit_success'))
          const currentTokenUser = authService.getCurrentUser()?.sub
          if (updated && updated.nif && updated.nif !== currentTokenUser) {
            nifChanged.value = true
            // Forzar logout y redirigir
            authService._clearAuth?.()
            setTimeout(() => { router.push({ name: 'Login' }) }, 900)
          } else {
            setTimeout(() => { editMode.value = false; editSuccess.value = false }, 1200)
          }
        } catch (e) {
          editError.value = e.message || t('error_updating_data')
        } finally {
          editLoading.value = false
        }
      }

      onMounted(() => {
        load()
        loadTotpStatus()
      })
      onBeforeUnmount(() => {
        if (toastTimer) {
          clearTimeout(toastTimer)
          toastTimer = null
        }
      })

    return { user, loading, error, formatDate, formatDateTime, showPwForm, showPwCurrent, showPwNew1, showPwNew2, pw, pwLoading, pwError, pwSuccess, submitPw, cancelPw, editMode, form, startEdit, cancelEdit, submitEdit, editLoading, editError, editSuccess, nifChanged, showDelete, openDelete, closeDelete, confirmDelete, deleteLoading, deleteError, exportData, exportLoading, exportError, exportSuccess, reauth, showReauthPassword, deleteModalRef, deleteCancelRef, deleteConfirmRef, onDeleteModalKeydown, appToast,
      totpEnabled, totpStatusLoading, showTotpSetup, totpSetupData, totpQrDataUrl, totpConfirmCode, totpSetupLoading, totpSetupError, startTotpSetup, cancelTotpSetup, confirmTotpSetup, showTotpDisable, totpDisableCode, totpDisableLoading, totpDisableError, cancelTotpDisable, confirmTotpDisable,
      ESTADO_CUENTA_SUSPENDIDO, processingRestrictionLoading, processingRestrictionError, enableProcessingRestriction, disableProcessingRestriction }
  }
}
</script>

<style scoped>
.reauth-group {
  margin-bottom: 1.5rem;
}

.field-hint {
  margin: 0.4rem 0 0 0;
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.password-input-wrap {
  position: relative;
}

.password-input-wrap .form-input {
  padding-right: 5.75rem;
}

.password-visibility-btn {
  position: absolute;
  top: 50%;
  right: 0.45rem;
  transform: translateY(-50%);
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--card-bg);
  color: var(--text-secondary);
  font-size: 0.75rem;
  font-weight: 600;
  line-height: 1;
  padding: 0.35rem 0.45rem;
  cursor: pointer;
}

.password-visibility-btn:hover {
  color: var(--text-primary);
}

.password-visibility-btn:focus-visible {
  outline: 2px solid var(--focus-color);
  outline-offset: 1px;
}

/* Two-Factor Authentication section */
.two-factor-status {
  margin: 0 0 1rem 0;
}

.status-badge {
  display: inline-block;
  padding: 0.3rem 0.75rem;
  border-radius: 999px;
  font-size: 0.8125rem;
  font-weight: 600;
}

.status-badge--on {
  background: var(--badge-success-bg);
  color: var(--badge-success-text);
}

.status-badge--off {
  background: var(--bg-light);
  color: var(--text-secondary);
}

.totp-qr-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  margin: 1rem 0;
  text-align: center;
}

.totp-qr {
  width: 200px;
  height: 200px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-light);
}

.totp-secret {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  align-items: center;
}

.totp-secret code {
  font-size: 1rem;
  letter-spacing: 0.05em;
  padding: 0.4rem 0.6rem;
  background: var(--bg-light);
  border-radius: 6px;
  word-break: break-all;
}

/* Data Grid - specific to this view */
.data-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

@media (min-width: 640px) {
  .data-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.data-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  background: var(--bg-light);
  border-radius: 8px;
  border: 1px solid var(--border);
}

.data-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-value {
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--text-primary);
  word-break: break-word;
}

.app-toast {
  position: fixed;
  right: 1.5rem;
  bottom: 1.5rem;
  z-index: 10020;
  display: flex;
  align-items: center;
  gap: 0.625rem;
  min-width: 240px;
  max-width: min(420px, calc(100vw - 2rem));
  padding: 0.875rem 1rem;
  border-radius: 10px;
  box-shadow: var(--shadow-toast);
  color: var(--text-inverse);
}

.app-toast--success {
  background: var(--success-color);
}

.app-toast--error {
  background: var(--danger-color);
}

.app-toast svg {
  flex-shrink: 0;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

/* Responsive adjustments */
@media (max-width: 640px) {
  .data-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions button {
    width: 100%;
  }

  .app-toast {
    left: 1rem;
    right: 1rem;
    bottom: 1rem;
    max-width: none;
  }
}
</style>
