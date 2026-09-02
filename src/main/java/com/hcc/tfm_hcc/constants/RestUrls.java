package com.hcc.tfm_hcc.constants;

public final class RestUrls {

    private RestUrls() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no debe ser instanciada");
    }

    public static final String AUTH_BASE = "/authentication";
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_LOGIN_2FA = "/login/2fa";
    public static final String AUTH_SIGNUP = "/signup";
    public static final String AUTH_GOOGLE_LOGIN = "/google/login";
    public static final String AUTH_GOOGLE_TOKEN = "/google/token";
    public static final String AUTH_PASSWORD_RESET_BASE = "/password-reset";
    public static final String AUTH_PASSWORD_RESET_REQUEST = "/password-reset/request";
    public static final String AUTH_PASSWORD_RESET_CONFIRM = "/password-reset/confirm";
    public static final String OAUTH2_AUTHORIZATION_GOOGLE = "/oauth2/authorization/google";

    public static final String HISTORIA_BASE = "/historia";
    public static final String HISTORIA_ARCHIVOS = "/archivos";
    public static final String HISTORIA_ARCHIVO_ID = "/archivos/{id}";
    public static final String HISTORIA_ANTECEDENTES = "/antecedentes";
    public static final String HISTORIA_ANTECEDENTE_ID = "/antecedentes/{id}";
    public static final String HISTORIA_ALERGIAS = "/alergias";
    public static final String HISTORIA_ALERGIA_ID = "/alergias/{id}";
    public static final String HISTORIA_ANALISIS_SANGRE = "/analisis-sangre";
    public static final String HISTORIA_SIGNOS_VITALES = "/signos-vitales";
    public static final String HISTORIA_ANALISIS_ORINA = "/analisis-orina";
    public static final String HISTORIA_DATOS_CLINICOS_ID = "/datos-clinicos/{id}";

    public static final String USUARIO_BASE = "/usuario";
    public static final String USUARIO_NOMBRE = "/nombre";
    public static final String USUARIO_ME = "/me";
    public static final String USUARIO_PASSWORD = "/password";
    public static final String USUARIO_SOLICITUDES = "/solicitudes";
    public static final String USUARIO_SOLICITUD_ID = "/solicitudes/{idSolicitud}";
    public static final String USUARIO_LOGS = "/logs";
    public static final String USUARIO_EXPORT = "/export";
    public static final String USUARIO_LIMITAR_TRATAMIENTO = "/me/limitar-tratamiento";
    public static final String USUARIO_REANUDAR_TRATAMIENTO = "/me/reanudar-tratamiento";
    public static final String USUARIO_2FA_SETUP = "/2fa/setup";
    public static final String USUARIO_2FA_CONFIRM = "/2fa/confirm";
    public static final String USUARIO_2FA_DISABLE = "/2fa/disable";
    public static final String USUARIO_2FA_STATUS = "/2fa/status";
    public static final String USUARIO_ANOTACIONES = "/anotaciones";

    public static final String PERFIL_BASE = "/perfil";
    public static final String PERFIL_ROL = "/rol/{rol}";

    public static final String MEDICO_BASE = "/medico";
    public static final String MEDICO_PACIENTES = "/pacientes";
    public static final String MEDICO_PACIENTES_BUSCAR = "/pacientes/buscar";
    public static final String MEDICO_SOLICITUDES_ASIGNACION = "/solicitudes-asignacion";
    public static final String MEDICO_SOLICITUDES_PENDIENTES = "/solicitudes-asignacion/pendientes";
    public static final String MEDICO_SOLICITUDES_ENVIADAS = "/solicitudes-asignacion/enviadas";
    public static final String MEDICO_PACIENTE_HISTORIAL = "/pacientes/{nif}/historial";
    public static final String MEDICO_PACIENTE_ANOTACIONES = "/pacientes/{nif}/anotaciones";
    public static final String MEDICO_PACIENTE_ARCHIVOS = "/pacientes/{nif}/archivos";
    public static final String MEDICO_PACIENTE_ARCHIVO_ID = "/pacientes/{nif}/archivos/{id}";

    public static final String ADMIN_BASE = "/admin";
    public static final String ADMIN_MEDICOS = "/medicos";
    public static final String ADMIN_MEDICO_ID = "/medicos/{id}";
    public static final String ADMIN_MEDICO_PERFIL = "/medicos/{id}/perfil-medico";
    public static final String ADMIN_USUARIOS_BY_NIF = "/usuarios/by-nif";

    public static final String RANGOS_BASE = "/rangos";

    public static final String RELACIONES_BASE = "/relaciones";
    public static final String RELACIONES_MIS_MEDICOS = "/mis-medicos";
    public static final String RELACIONES_MIS_MEDICOS_NIF = "/mis-medicos/{nif}";
    public static final String RELACIONES_MIS_PACIENTES_NIF = "/mis-pacientes/{nif}";

    public static final String NOTIFICACION_BASE = "/notificaciones";
    public static final String NOTIFICACION_MARCAR_LEIDAS = "/marcar-leidas";
    public static final String NOTIFICACION_NO_LEIDAS = "/no-leidas";
    public static final String NOTIFICACION_LEIDA = "/{id}/leida";
    public static final String NOTIFICACION_ID = "/{id}";
}