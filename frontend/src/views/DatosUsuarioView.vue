<template>
  <div class="page-container-narrow">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-icon">
        <AppIcon name="user" size="2xl" />
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
      <AppIcon name="alert-circle" size="lg" />
      {{ error }}
    </div>

    <!-- No Data State -->
    <div v-else-if="!user" class="empty-state">
      <AppIcon name="user" size="4xl" />
      <h3>{{ $t('no_data_loaded') }}</h3>
      <p>{{ $t('reload_page') }}</p>
    </div>

    <!-- Data Content -->
    <div v-else class="user-data-content">
      <section class="tabs-section">
        <div class="tabs-nav-wrap">
          <button
            v-if="canScrollTabsLeft"
            type="button"
            class="btn-icon tabs-scroll-btn"
            @click="scrollTabsLeft"
            :aria-label="$t('tabs_scroll_left')"
          >
            <AppIcon name="arrow-left" size="sm" />
          </button>

          <div class="tabs-nav" ref="tabsNavRef" role="tablist" :aria-label="$t('my_personal_data')">
          <button
            type="button"
            id="personal-tab"
            ref="tabPersonal"
            :class="['tab-btn', { active: activeSection === 'personal' }]"
            @click="activeSection = 'personal'"
            @keydown="onTabKeydown($event, 'personal')"
            role="tab"
            :aria-selected="activeSection === 'personal'"
            aria-controls="personal-panel"
            :tabindex="activeSection === 'personal' ? 0 : -1"
          >
            <AppIcon name="user" size="md" />
            {{ $t('personal_info') }}
          </button>

          <button
            type="button"
            id="password-tab"
            ref="tabPassword"
            :class="['tab-btn', { active: activeSection === 'password' }]"
            @click="activeSection = 'password'"
            @keydown="onTabKeydown($event, 'password')"
            role="tab"
            :aria-selected="activeSection === 'password'"
            aria-controls="password-panel"
            :tabindex="activeSection === 'password' ? 0 : -1"
          >
            <AppIcon name="lock" size="md" />
            {{ $t('change_password') }}
          </button>

          <button
            type="button"
            id="two-factor-tab"
            ref="tabTwoFactor"
            :class="['tab-btn', { active: activeSection === 'two-factor' }]"
            @click="activeSection = 'two-factor'"
            @keydown="onTabKeydown($event, 'two-factor')"
            role="tab"
            :aria-selected="activeSection === 'two-factor'"
            aria-controls="two-factor-panel"
            :tabindex="activeSection === 'two-factor' ? 0 : -1"
          >
            <AppIcon name="check-circle" size="md" />
            {{ $t('two_factor_section_title') }}
          </button>

          <button
            type="button"
            id="processing-restriction-tab"
            ref="tabRestriction"
            :class="['tab-btn', { active: activeSection === 'processing-restriction' }]"
            @click="activeSection = 'processing-restriction'"
            @keydown="onTabKeydown($event, 'processing-restriction')"
            role="tab"
            :aria-selected="activeSection === 'processing-restriction'"
            aria-controls="processing-restriction-panel"
            :tabindex="activeSection === 'processing-restriction' ? 0 : -1"
          >
            <AppIcon name="alert-triangle" size="md" />
            {{ $t('processing_restriction_section_title') }}
          </button>

          <button
            type="button"
            id="access-log-tab"
            ref="tabAccessLog"
            :class="['tab-btn', { active: activeSection === 'access-log' }]"
            @click="activeSection = 'access-log'"
            @keydown="onTabKeydown($event, 'access-log')"
            role="tab"
            :aria-selected="activeSection === 'access-log'"
            aria-controls="access-log-panel"
            :tabindex="activeSection === 'access-log' ? 0 : -1"
          >
            <AppIcon name="list" size="md" />
            {{ $t('access_log_section_title') }}
          </button>
          </div>

          <button
            v-if="canScrollTabsRight"
            type="button"
            class="btn-icon tabs-scroll-btn"
            @click="scrollTabsRight"
            :aria-label="$t('tabs_scroll_right')"
          >
            <AppIcon name="arrow-right" size="sm" />
          </button>
        </div>

        <transition name="fade-slide" mode="out-in">
          <!-- Personal Info Tab -->
          <div
            v-if="activeSection === 'personal'"
            key="personal"
            id="personal-panel"
            class="tab-panel"
            role="tabpanel"
            aria-labelledby="personal-tab"
            tabindex="-1"
          >
            <!-- View Mode -->
            <template v-if="!editMode">
              <div class="panel-card">
                <div class="panel-header">
                  <h2>{{ $t('personal_info') }}</h2>
                  <p class="panel-subtitle">{{ $t('review_update_personal') }}</p>
                </div>

                <div class="data-grid">
                  <div class="data-item">
                    <label class="data-label" for="dato-nombre">{{ $t('name') }}</label>
                    <input id="dato-nombre" class="form-input" type="text" :value="user.nombre" readonly />
                  </div>
                  <div class="data-item">
                    <label class="data-label" for="dato-apellido1">{{ $t('first_surname') }}</label>
                    <input id="dato-apellido1" class="form-input" type="text" :value="user.apellido1" readonly />
                  </div>
                  <div class="data-item" v-if="user.apellido2">
                    <label class="data-label" for="dato-apellido2">{{ $t('second_surname') }}</label>
                    <input id="dato-apellido2" class="form-input" type="text" :value="user.apellido2" readonly />
                  </div>
                  <div class="data-item">
                    <label class="data-label" for="dato-nif">{{ $t('nif') }}</label>
                    <input id="dato-nif" class="form-input" type="text" :value="user.nif" readonly />
                  </div>
                  <div class="data-item">
                    <label class="data-label" for="dato-email">{{ $t('email') }}</label>
                    <input id="dato-email" class="form-input" type="text" :value="user.email" readonly />
                  </div>
                  <div class="data-item" v-if="user.telefono">
                    <label class="data-label" for="dato-telefono">{{ $t('phone') }}</label>
                    <input id="dato-telefono" class="form-input" type="text" :value="user.telefono" readonly />
                  </div>
                  <div class="data-item" v-if="user.fechaNacimiento">
                    <label class="data-label" for="dato-fecha-nacimiento">{{ $t('birth_date') }}</label>
                    <input id="dato-fecha-nacimiento" class="form-input" type="text" :value="formatDate(user.fechaNacimiento)" readonly />
                  </div>
                  <div class="data-item" v-if="user.fechaCreacion">
                    <label class="data-label" for="dato-fecha-creacion">{{ $t('registration_date') }}</label>
                    <input id="dato-fecha-creacion" class="form-input" type="text" :value="formatDateTime(user.fechaCreacion)" readonly />
                  </div>
                  <div class="data-item" v-if="user.fechaUltimaModificacion">
                    <label class="data-label" for="dato-fecha-modificacion">{{ $t('last_modification') }}</label>
                    <input id="dato-fecha-modificacion" class="form-input" type="text" :value="formatDateTime(user.fechaUltimaModificacion)" readonly />
                  </div>
                </div>

                <div class="form-actions">
                  <button
                    type="button"
                    class="btn-primary"
                    @click="startEdit"
                    :aria-label="$t('aria_edit_personal_data')">
                    <AppIcon name="edit" size="md" />
                    {{ $t('edit_data') }}
                  </button>
                  <button
                    type="button"
                    class="btn-secondary"
                    @click="goToPasswordTab"
                    :aria-label="$t('show_pw_form')">
                    <AppIcon name="lock" size="md" />
                    {{ $t('change_password') }}
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
                    <AppIcon name="alert-circle" size="lg" />
                    {{ editError }}
                  </div>

                  <div v-if="editSuccess" class="alert alert-success" role="status" aria-live="polite">
                    <AppIcon name="check" size="lg" />
                    {{ $t('edit_success') }}
                  </div>

                  <div v-if="nifChanged" class="alert alert-warning" role="alert">
                    <AppIcon name="alert-triangle" size="lg" />
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
                      <AppIcon v-else name="check" size="md" />
                      {{ editLoading ? $t('saving') : $t('save_changes') }}
                    </button>
                  </div>
                </form>
              </div>
            </template>
          </div>

          <!-- Change Password Tab -->
          <div
            v-else-if="activeSection === 'password'"
            key="password"
            id="password-panel"
            class="tab-panel"
            role="tabpanel"
            aria-labelledby="password-tab"
            tabindex="-1"
          >
            <div class="panel-card">
              <div class="panel-header">
                <h2>{{ $t('change_password') }}</h2>
                <p class="panel-subtitle">{{ $t('update_password') }}</p>
              </div>

              <form @submit.prevent="submitPw">
                <div class="info-box">
                  <AppIcon name="info" size="lg" />
                  <span>{{ $t('password_update_help') }}</span>
                </div>

                <div class="form-grid">
                  <div class="form-group full-width">
                    <PasswordInput
                      ref="pwCurrentRef"
                      id="pw-current"
                      v-model="pw.current"
                      :label="$t('current_password')"
                      required
                      autocomplete="current-password"
                    />
                  </div>
                  <div class="form-group">
                    <PasswordInput
                      ref="pwNew1Ref"
                      id="pw-new1"
                      v-model="pw.new1"
                      :label="$t('new_password')"
                      required
                      minlength="8"
                      autocomplete="new-password"
                    />
                  </div>
                  <div class="form-group">
                    <PasswordInput
                      ref="pwNew2Ref"
                      id="pw-new2"
                      v-model="pw.new2"
                      :label="$t('repeat_new_password')"
                      required
                      minlength="8"
                      autocomplete="new-password"
                    />
                  </div>
                </div>

                <div v-if="pwError" class="alert alert-danger" role="alert">
                  <AppIcon name="alert-circle" size="lg" />
                  {{ pwError }}
                </div>

                <div v-if="pwSuccess" class="alert alert-success" role="status" aria-live="polite">
                  <AppIcon name="check" size="lg" />
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
                    <AppIcon v-else name="check" size="md" />
                    {{ pwLoading ? $t('saving') : $t('save_new_password') }}
                  </button>
                </div>
              </form>
            </div>
          </div>

          <!-- Two-Factor Authentication Tab -->
          <div
            v-else-if="activeSection === 'two-factor'"
            key="two-factor"
            id="two-factor-panel"
            class="tab-panel"
            role="tabpanel"
            aria-labelledby="two-factor-tab"
            tabindex="-1"
          >
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

          <!-- Processing Restriction Tab (GDPR Art. 18) -->
          <div
            v-else-if="activeSection === 'processing-restriction'"
            key="processing-restriction"
            id="processing-restriction-panel"
            class="tab-panel"
            role="tabpanel"
            aria-labelledby="processing-restriction-tab"
            tabindex="-1"
          >
            <div class="panel-card">
              <div class="panel-header">
                <h2>{{ $t('processing_restriction_section_title') }}</h2>
                <p class="panel-subtitle">{{ $t('processing_restriction_section_subtitle') }}</p>
              </div>

              <div class="info-box">
                <AppIcon name="info" size="lg" />
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
                <AppIcon name="alert-circle" size="lg" />
                {{ processingRestrictionError }}
              </div>
            </div>
          </div>

          <!-- Access Log Tab (derecho de acceso / trazabilidad, RGPD) -->
          <div
            v-else-if="activeSection === 'access-log'"
            key="access-log"
            id="access-log-panel"
            class="tab-panel"
            role="tabpanel"
            aria-labelledby="access-log-tab"
            tabindex="-1"
          >
            <div class="panel-card">
              <div class="panel-header">
                <h2>{{ $t('access_log_section_title') }}</h2>
                <p class="panel-subtitle">{{ $t('access_log_section_subtitle') }}</p>
              </div>

              <div class="form-group reauth-group">
                <PasswordInput
                  id="reauth-password"
                  v-model="reauth.password"
                  :label="$t('current_password_confirm_label')"
                  :hint="$t('current_password_confirm_help')"
                  autocomplete="current-password"
                />
              </div>

              <div class="form-actions">
                <button
                  type="button"
                  class="btn-secondary"
                  @click="exportData"
                  :disabled="exportLoading"
                  :aria-label="exportLoading ? $t('aria_export_personal_data_loading') : $t('aria_export_personal_data')">
                  <AppIcon v-if="!exportLoading" name="download" size="md" />
                  <div v-else class="spinner-small"></div>
                  {{ exportLoading ? $t('exporting') : $t('download_my_data') }}
                </button>
              </div>

              <div v-if="exportError" class="alert alert-danger" role="alert">
                <AppIcon name="alert-circle" size="lg" />
                {{ exportError }}
              </div>

              <div v-if="exportSuccess" class="alert alert-success" role="status" aria-live="polite">
                <AppIcon name="check" size="lg" />
                {{ exportSuccess }}
              </div>

              <hr class="section-divider" />

              <AccessLogSection />
            </div>
          </div>
        </transition>
      </section>

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
            <AppIcon name="alert-triangle" size="3xl" style="color: var(--danger-color)" />
            <h2 id="delete-account-title">{{ $t('delete_account') }}</h2>
          </div>
          <p id="delete-account-desc" class="modal-text">{{ $t('delete_account_confirm') }}</p>

          <div class="form-group">
            <PasswordInput
              id="delete-reauth-password"
              v-model="reauth.password"
              :label="$t('current_password_confirm_label')"
              required
              autocomplete="current-password"
            />
          </div>

          <div v-if="deleteError" class="alert alert-danger" role="alert">
            <AppIcon name="alert-circle" size="lg" />
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
              <AppIcon v-else name="trash" size="md" />
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
          <AppIcon name="check" size="md" />
          <span>{{ appToast.message }}</span>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import QRCode from 'qrcode'
import authService from '@/services/authService'
import { validateNIF } from '@/utils/validateNIF'
import AccessLogSection from '@/components/AccessLogSection.vue'
import AppIcon from '@/components/AppIcon.vue'
import PasswordInput from '@/components/PasswordInput.vue'


const ESTADO_CUENTA_SUSPENDIDO = 'SUSPENDIDO'
const ESTADO_CUENTA_ACTIVO = 'ACTIVO'
const MIN_PASSWORD_LENGTH = 8

export default {
  name: 'DatosUsuarioView',
  components: { AccessLogSection, AppIcon, PasswordInput },
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
    const pw = ref({ current:'', new1:'', new2:'' })
    const pwCurrentRef = ref(null)
    const pwNew1Ref = ref(null)
    const pwNew2Ref = ref(null)
    const pwLoading = ref(false)
    const pwError = ref('')
    const pwSuccess = ref(false)
    const exportLoading = ref(false)
    const exportError = ref('')
    const exportSuccess = ref('')
    const reauth = ref({ password: '' })
    const deleteModalRef = ref(null)
    const deleteCancelRef = ref(null)
    const deleteConfirmRef = ref(null)
    const deleteTriggerEl = ref(null)
    const appToast = ref({ visible: false, message: '', type: 'success' })
    let toastTimer = null

    // Subpestañas de la pantalla, en el mismo patrón que HistoriaClinicaView.
    const tabOrder = ['personal', 'password', 'two-factor', 'processing-restriction', 'access-log']
    const activeSection = ref('personal')
    const tabPersonal = ref(null)
    const tabPassword = ref(null)
    const tabTwoFactor = ref(null)
    const tabRestriction = ref(null)
    const tabAccessLog = ref(null)
    const tabRefs = {
      personal: tabPersonal,
      password: tabPassword,
      'two-factor': tabTwoFactor,
      'processing-restriction': tabRestriction,
      'access-log': tabAccessLog
    }

    // Desplazamiento de la barra de subpestañas mediante botones (en vez de
    // la barra de scroll nativa) cuando no caben todas a la vez.
    const TABS_SCROLL_STEP = 200
    const tabsNavRef = ref(null)
    const canScrollTabsLeft = ref(false)
    const canScrollTabsRight = ref(false)

    const updateTabsScrollState = () => {
      const el = tabsNavRef.value
      if (!el) return
      canScrollTabsLeft.value = el.scrollLeft > 0
      canScrollTabsRight.value = el.scrollLeft + el.clientWidth < el.scrollWidth - 1
    }

    const scrollTabsLeft = () => {
      tabsNavRef.value?.scrollBy({ left: -TABS_SCROLL_STEP, behavior: 'smooth' })
    }
    const scrollTabsRight = () => {
      tabsNavRef.value?.scrollBy({ left: TABS_SCROLL_STEP, behavior: 'smooth' })
    }

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

      const focusTab = (section) => {
        const tabRef = tabRefs[section]
        if (tabRef?.value && typeof tabRef.value.focus === 'function') tabRef.value.focus()
      }

      const onTabKeydown = (event, currentSection) => {
        const idx = tabOrder.indexOf(currentSection)
        if (idx < 0) return

        let targetIdx = idx
        if (event.key === 'ArrowRight') targetIdx = (idx + 1) % tabOrder.length
        if (event.key === 'ArrowLeft') targetIdx = (idx - 1 + tabOrder.length) % tabOrder.length
        if (event.key === 'Home') targetIdx = 0
        if (event.key === 'End') targetIdx = tabOrder.length - 1

        if (targetIdx !== idx) {
          event.preventDefault()
          activeSection.value = tabOrder[targetIdx]
          nextTick(() => focusTab(tabOrder[targetIdx]))
        }
      }

      const goToPasswordTab = () => {
        activeSection.value = 'password'
      }

      watch(activeSection, () => {
        nextTick(() => {
          const panel = document.getElementById(`${activeSection.value}-panel`)
          if (panel) panel.focus()
        })
      })

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
        if (pw.value.new1.length < MIN_PASSWORD_LENGTH) { pwError.value = t('password_min_length'); return }
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
        pwCurrentRef.value?.reset()
        pwNew1Ref.value?.reset()
        pwNew2Ref.value?.reset()
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
        load().then(() => nextTick().then(() => {
          updateTabsScrollState()
          tabsNavRef.value?.addEventListener('scroll', updateTabsScrollState, { passive: true })
          window.addEventListener('resize', updateTabsScrollState)
        }))
        loadTotpStatus()
      })
      onBeforeUnmount(() => {
        if (toastTimer) {
          clearTimeout(toastTimer)
          toastTimer = null
        }
        tabsNavRef.value?.removeEventListener('scroll', updateTabsScrollState)
        window.removeEventListener('resize', updateTabsScrollState)
      })

    return { user, loading, error, formatDate, formatDateTime, pw, pwLoading, pwError, pwSuccess, submitPw, cancelPw, pwCurrentRef, pwNew1Ref, pwNew2Ref, editMode, form, startEdit, cancelEdit, submitEdit, editLoading, editError, editSuccess, nifChanged, showDelete, openDelete, closeDelete, confirmDelete, deleteLoading, deleteError, exportData, exportLoading, exportError, exportSuccess, reauth, deleteModalRef, deleteCancelRef, deleteConfirmRef, onDeleteModalKeydown, appToast,
      activeSection, tabPersonal, tabPassword, tabTwoFactor, tabRestriction, tabAccessLog, onTabKeydown, goToPasswordTab,
      tabsNavRef, canScrollTabsLeft, canScrollTabsRight, scrollTabsLeft, scrollTabsRight,
      totpEnabled, totpStatusLoading, showTotpSetup, totpSetupData, totpQrDataUrl, totpConfirmCode, totpSetupLoading, totpSetupError, startTotpSetup, cancelTotpSetup, confirmTotpSetup, showTotpDisable, totpDisableCode, totpDisableLoading, totpDisableError, cancelTotpDisable, confirmTotpDisable,
      ESTADO_CUENTA_SUSPENDIDO, processingRestrictionLoading, processingRestrictionError, enableProcessingRestriction, disableProcessingRestriction }
  }
}
</script>

<style scoped>
.reauth-group {
  margin-bottom: 1.5rem;
}

/* Tabs (same pattern as HistoriaClinicaView) */
.tabs-section {
  margin-bottom: 2rem;
}

.tabs-nav-wrap {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  border-bottom: 2px solid var(--border);
  margin-bottom: 2rem;
}

.tabs-scroll-btn {
  flex-shrink: 0;
}

.tabs-nav {
  display: flex;
  flex: 1;
  min-width: 0;
  gap: 0.5rem;
  overflow-x: auto;
  scrollbar-width: none;
}

.tabs-nav::-webkit-scrollbar {
  display: none;
}

.tab-btn {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 1.25rem;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tab-btn:hover {
  color: var(--primary-color);
  background: var(--bg-light);
}

.tab-btn:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 2px;
}

.tab-btn.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}

.tab-btn svg {
  flex-shrink: 0;
}

.tab-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-divider {
  border: none;
  border-top: 1px solid var(--border);
  margin: 2rem 0;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
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

  .tabs-nav {
    gap: 0.25rem;
  }

  .tab-btn {
    padding: 0.75rem 1rem;
    font-size: 0.8125rem;
  }

  .app-toast {
    left: 1rem;
    right: 1rem;
    bottom: 1rem;
    max-width: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .tab-panel {
    animation: none;
  }

  .tab-btn,
  .fade-slide-enter-active,
  .fade-slide-leave-active {
    transition: none;
  }
}
</style>
