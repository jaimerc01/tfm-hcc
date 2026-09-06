# Diagramas del sistema HCC (Historial Clínico Compartido)

Diagramas generados a partir de una revisión del código fuente (rama `dev/version/0.0.3`).
Reflejan el estado real de la aplicación: entidades JPA en `src/main/java/com/hcc/tfm_hcc/model`,
documentos MongoDB del mismo paquete, repositorios Spring Data y la arquitectura en capas
Controller → Facade → Service → Repository.

Notas transversales:

- Todas las entidades relacionales heredan de `BaseEntity`: `id` (UUID, PK, generado por la
  aplicación con `GenerationType.UUID`), `fecha_creacion` y `fecha_ultima_modificacion`.
- Varios atributos se persisten **cifrados con AES/GCM** (`AESEncryptionConverter` en JPA;
  `FieldEncryptionService` / `ArchivoCifradoService` en MongoDB). El cifrado es *no determinista*,
  por lo que las búsquedas por igualdad se hacen contra columnas `*_hash` (**HMAC-SHA256**,
  `HmacSearchIndexService`), no contra el valor cifrado. La relación completa de atributos
  cifrados está en la tabla «Atributos cifrados en reposo» de la sección 2.
- El registro del consentimiento (`fecha_consentimiento`, `version_politica_privacidad`) **no**
  es un dato de salud y se guarda sin cifrar (RGPD art. 7.1: poder demostrar qué se consintió).
- No existen claves ajenas entre PostgreSQL y MongoDB: la relación de los documentos con
  `usuario` es una **referencia lógica** por UUID (línea discontinua en el diagrama E-R).
- Persistencia políglota: datos clínicos y de identidad en **PostgreSQL**; auditoría, logs de
  acceso, archivos clínicos y tokens efímeros (2FA, Google, reset de contraseña) en **MongoDB**.

---

## 1. Diagrama Entidad-Relación

```mermaid
erDiagram
    %% ========== PostgreSQL (relacional, claves ajenas reales) ==========
    USUARIO {
        UUID id PK
        string nombre
        string apellido1
        string apellido2
        string email
        string email_hash UK
        string password
        datetime fecha_nacimiento
        string nif
        string nif_hash UK
        string telefono
        string especialidad
        string estado_cuenta
        datetime fecha_eliminacion
        datetime last_password_change
        datetime fecha_consentimiento
        string version_politica_privacidad
        string totp_secret
        boolean totp_enabled
    }
    PERFIL {
        UUID id PK
        string rol UK
    }
    PERFIL_USUARIO {
        UUID id PK
        UUID id_usuario FK
        UUID id_perfil FK
    }
    HISTORIAL_CLINICO {
        UUID id PK
        UUID id_paciente FK
    }
    DATO_CLINICO {
        UUID id PK
        string tipo
        string tipo_hash
        string valor
        string unidad
        string observacion
        UUID id_rango FK
        UUID id_historial_clinico FK
        datetime fecha_creacion
    }
    RANGO {
        UUID id PK
        string nombre UK
        string valor_superior
        string valor_inferior
    }
    ALERGIA {
        UUID id PK
        string descripcion
        UUID id_historial_clinico FK
    }
    ANTECEDENTE_CLINICO {
        UUID id PK
        string categoria
        string descripcion
        UUID id_historial_clinico FK
    }
    ANOTACION_MEDICA {
        UUID id PK
        UUID id_profesional FK
        UUID id_paciente FK
        string mensaje
    }
    MEDICO_PACIENTE {
        UUID id PK
        UUID id_medico FK
        UUID id_paciente FK
        string estado
    }
    SOLICITUD_ASIGNACION {
        UUID id PK
        UUID id_medico FK
        UUID id_paciente FK
        string estado
    }
    PROPUESTA_CAMBIO_CLINICO {
        UUID id PK
        UUID id_medico FK
        UUID id_paciente FK
        UUID id_historial_clinico FK
        string dominio
        string operacion
        string id_recurso_objetivo
        string payload_json
        string descripcion_actual
        string motivo
        string estado
        datetime fecha_resolucion
    }
    NOTIFICACION {
        UUID id PK
        UUID id_usuario FK
        string mensaje
        boolean leida
        boolean eliminada
    }

    %% ========== MongoDB (documentos, sin FK; referencia logica por UUID) ==========
    ARCHIVO_CLINICO {
        UUID id PK
        UUID id_usuario
        string nombre_original
        string content_type
        long size_bytes
        bytes contenido
        datetime fecha_creacion
    }
    AUDITORIA_CAMBIO {
        string id PK
        string id_usuario
        string id_paciente
        string id_medico
        string tabla
        string tipo_cambio
        string valor_anterior
        string valor_nuevo
        string tipo_operacion
        string razon_cambio
        string id_recurso
        string metadatos
        datetime fecha_cambio
    }
    LOG_ACCESO {
        string id PK
        string id_usuario
        datetime timestamp
        string metodo
        string ruta
        int estado
        string ip
        string user_agent
        long duracion_ms
    }
    PASSWORD_RESET_TOKEN {
        string id PK
        string token_hash UK
        string usuario_id
        boolean usado
        instant fecha_creacion
        instant fecha_expiracion
    }
    TWO_FACTOR_CHALLENGE {
        string id PK
        string usuario_id
        int intentos
        instant fecha_expiracion
    }
    GOOGLE_LOGIN_CODE {
        string id PK
        string token
        string challenge_id
        boolean requires_two_factor
        long expiration_time
        instant fecha_expiracion
    }

    USUARIO ||--o{ PERFIL_USUARIO : tiene
    PERFIL  ||--o{ PERFIL_USUARIO : "asignado en"
    USUARIO ||--o| HISTORIAL_CLINICO : posee
    HISTORIAL_CLINICO ||--o{ DATO_CLINICO : contiene
    HISTORIAL_CLINICO ||--o{ ALERGIA : contiene
    HISTORIAL_CLINICO ||--o{ ANTECEDENTE_CLINICO : contiene
    RANGO ||--o{ DATO_CLINICO : "sirve de referencia a"
    USUARIO ||--o{ ANOTACION_MEDICA : "escribe (medico)"
    USUARIO ||--o{ ANOTACION_MEDICA : "es sujeto de (paciente)"
    USUARIO ||--o{ MEDICO_PACIENTE : "como medico"
    USUARIO ||--o{ MEDICO_PACIENTE : "como paciente"
    USUARIO ||--o{ SOLICITUD_ASIGNACION : "como medico"
    USUARIO ||--o{ SOLICITUD_ASIGNACION : "como paciente"
    USUARIO ||--o{ PROPUESTA_CAMBIO_CLINICO : "como medico"
    USUARIO ||--o{ PROPUESTA_CAMBIO_CLINICO : "como paciente"
    HISTORIAL_CLINICO ||--o{ PROPUESTA_CAMBIO_CLINICO : "afecta a"
    USUARIO ||--o{ NOTIFICACION : "recibe"

    USUARIO ||..o{ ARCHIVO_CLINICO : "propietario"
    USUARIO ||..o{ AUDITORIA_CAMBIO : "referida por"
    USUARIO ||..o{ LOG_ACCESO : "referida por"
    USUARIO ||..o{ PASSWORD_RESET_TOKEN : "referida por"
    USUARIO ||..o{ TWO_FACTOR_CHALLENGE : "referida por"
```

### Estados y enumeraciones

| Columna | Valores |
|---|---|
| `USUARIO.estado_cuenta` | `ACTIVO` · `SUSPENDIDO` (limitación del tratamiento, art. 18 RGPD) · `ELIMINADO` (borrado lógico) |
| `PERFIL.rol` | `PACIENTE` · `MEDICO` · `ADMINISTRADOR` |
| `ANTECEDENTE_CLINICO.categoria` | `PERSONAL` · `FAMILIAR` |
| `MEDICO_PACIENTE.estado` | `ACTIVA` · `REVOCADA` |
| `SOLICITUD_ASIGNACION.estado` | `PENDIENTE` · `ACEPTADA` · `RECHAZADA` · `REVOCADA` |
| `PROPUESTA_CAMBIO_CLINICO.estado` | `PENDIENTE` · `ACEPTADA` · `RECHAZADA` · `ANULADA` |
| `PROPUESTA_CAMBIO_CLINICO.dominio` | `ANTECEDENTE` · `ALERGIA` · `ANALISIS_SANGRE` · `SIGNOS_VITALES` · `ANALISIS_ORINA` |
| `PROPUESTA_CAMBIO_CLINICO.operacion` / `AUDITORIA_CAMBIO.tipo_operacion` | `CREATE` · `UPDATE` · `DELETE` |

Notas: `email_hash`, `nif_hash` y `tipo_hash` son índices de búsqueda HMAC-SHA256; `password` es un
hash BCrypt y `token_hash` un SHA-256. Los campos `fecha_expiracion` de los documentos MongoDB
llevan índice TTL. `AUDITORIA_CAMBIO.id_medico` solo se rellena cuando el cambio lo origina un
médico mediante una propuesta aceptada. En `DATO_CLINICO`, `fecha_creacion` es la fecha real del
dato clínico, no la de inserción.

### Aclaraciones de cardinalidad

| Relación | Cardinalidad | Implementación |
|---|---|---|
| `usuario` – `perfil` | N:M | Tabla puente `perfil_usuario` (`@ManyToOne` a ambos lados) |
| `usuario` – `historial_clinico` | 1:0..1 | `@ManyToOne` sobre `id_paciente`; la unicidad la garantiza la capa de servicio, no un índice único |
| `historial_clinico` – `dato_clinico` / `alergia` / `antecedente_clinico` | 1:N (composición) | `@ManyToOne` con `id_historial_clinico NOT NULL` |
| `rango` – `dato_clinico` | 0..1:N | `id_rango` nullable; se asigna por coincidencia de nombre (ignorando mayúsculas/tildes) |
| `usuario` – `medico_paciente` / `solicitud_asignacion` | 1:N (doble rol) | N:M lógico médico↔paciente materializado con estado |
| `usuario` – `propuesta_cambio_clinico` | 1:N (doble rol) | El médico crea la propuesta; el paciente la confirma o rechaza |
| `historial_clinico` – `propuesta_cambio_clinico` | 1:N | Historial sobre el que se aplicaría el cambio al aceptarse |
| `usuario` – `anotacion_medica` | 1:N (doble rol) | `id_profesional` + `id_paciente` |
| `usuario` – documentos MongoDB | 1:N lógica | Referencia por UUID; sin integridad referencial entre motores |

---

## 2. Diagrama de clases UML — modelo de dominio

Diagrama de entidades del modelo de dominio (entidades JPA de `model/`). Herencia con triángulo
hueco desde `BaseEntity`, realización de interfaz con línea discontinua, composición (rombo
relleno) para el agregado `HistorialClinico` y multiplicidades en los extremos de cada asociación.
Cada relación `@ManyToOne` se muestra **también como atributo tipado** dentro de la clase (el
campo real de la entidad), con el mismo nombre que rotula la asociación. Se omiten los métodos;
los identificadores y las marcas de tiempo se heredan de `BaseEntity`.

```mermaid
classDiagram
    direction TB

    class BaseEntity {
        <<abstract>>
        +UUID id
        +LocalDateTime fechaCreacion
        +LocalDateTime fechaUltimaModificacion
    }
    class UserDetails {
        <<interface>>
    }

    class Usuario {
        +String nombre
        +String apellido1
        +String apellido2
        +String email
        +String emailHash
        +String password
        +LocalDateTime fechaNacimiento
        +String nif
        +String nifHash
        +String telefono
        +String especialidad
        +String estadoCuenta
        +LocalDateTime fechaEliminacion
        +LocalDateTime lastPasswordChange
        +LocalDateTime fechaConsentimiento
        +String versionPoliticaPrivacidad
        +String totpSecret
        +boolean totpEnabled
    }
    class Perfil {
        +String rol
    }
    class PerfilUsuario {
        +Usuario usuario
        +Perfil perfil
    }
    class HistorialClinico {
        +Usuario usuario
    }
    class DatoClinico {
        +String tipo
        +String tipoHash
        +String valor
        +String unidad
        +String observacion
        +Rango rango
        +HistorialClinico historialClinico
    }
    class Rango {
        +String nombre
        +String valorSuperior
        +String valorInferior
    }
    class Alergia {
        +String descripcion
        +HistorialClinico historialClinico
    }
    class AntecedenteClinico {
        +Categoria categoria
        +String descripcion
        +HistorialClinico historialClinico
    }
    class Categoria {
        <<enumeration>>
        PERSONAL
        FAMILIAR
    }
    class AnotacionMedica {
        +String mensaje
        +Usuario medico
        +Usuario paciente
    }
    class MedicoPaciente {
        +String estado
        +Usuario medico
        +Usuario paciente
    }
    class SolicitudAsignacion {
        +String estado
        +Usuario medico
        +Usuario paciente
    }
    class PropuestaCambioClinico {
        +Dominio dominio
        +Operacion operacion
        +String idRecursoObjetivo
        +String payloadJson
        +String descripcionActual
        +String motivo
        +String estado
        +LocalDateTime fechaResolucion
        +Usuario medico
        +Usuario paciente
        +HistorialClinico historialClinico
    }
    class Dominio {
        <<enumeration>>
        ANTECEDENTE
        ALERGIA
        ANALISIS_SANGRE
        SIGNOS_VITALES
        ANALISIS_ORINA
    }
    class Operacion {
        <<enumeration>>
        CREATE
        UPDATE
        DELETE
    }
    class Notificacion {
        +String mensaje
        +boolean leida
        +boolean eliminada
        +Usuario usuario
    }

    BaseEntity <|-- Usuario
    BaseEntity <|-- Perfil
    BaseEntity <|-- PerfilUsuario
    BaseEntity <|-- HistorialClinico
    BaseEntity <|-- DatoClinico
    BaseEntity <|-- Rango
    BaseEntity <|-- Alergia
    BaseEntity <|-- AntecedenteClinico
    BaseEntity <|-- AnotacionMedica
    BaseEntity <|-- MedicoPaciente
    BaseEntity <|-- SolicitudAsignacion
    BaseEntity <|-- PropuestaCambioClinico
    BaseEntity <|-- Notificacion
    UserDetails <|.. Usuario

    Usuario "1" --> "0..*" PerfilUsuario : usuario
    Perfil "1" --> "0..*" PerfilUsuario : perfil
    Usuario "1" --> "0..1" HistorialClinico : usuario
    HistorialClinico "1" *-- "0..*" DatoClinico
    HistorialClinico "1" *-- "0..*" Alergia
    HistorialClinico "1" *-- "0..*" AntecedenteClinico
    DatoClinico "0..*" --> "0..1" Rango : rango
    AntecedenteClinico "0..*" --> "1" Categoria : categoria
    Usuario "1" --> "0..*" AnotacionMedica : medico
    Usuario "1" --> "0..*" AnotacionMedica : paciente
    Usuario "1" --> "0..*" MedicoPaciente : medico
    Usuario "1" --> "0..*" MedicoPaciente : paciente
    Usuario "1" --> "0..*" SolicitudAsignacion : medico
    Usuario "1" --> "0..*" SolicitudAsignacion : paciente
    Usuario "1" --> "0..*" PropuestaCambioClinico : medico
    Usuario "1" --> "0..*" PropuestaCambioClinico : paciente
    HistorialClinico "1" --> "0..*" PropuestaCambioClinico : historialClinico
    PropuestaCambioClinico "0..*" --> "1" Dominio : dominio
    PropuestaCambioClinico "0..*" --> "1" Operacion : operacion
    Usuario "1" --> "0..*" Notificacion : usuario
```

### Atributos cifrados en reposo (AES/GCM)

| Clase | Atributos |
|---|---|
| `Usuario` | `nombre`, `apellido1`, `apellido2`, `email`, `nif`, `telefono`, `fechaNacimiento`, `totpSecret` |
| `DatoClinico` | `tipo`, `valor`, `unidad`, `observacion` |
| `Alergia` | `descripcion` |
| `AntecedenteClinico` | `descripcion` |
| `AnotacionMedica` | `mensaje` |
| `PropuestaCambioClinico` | `payloadJson`, `descripcionActual`, `motivo` |
| `AuditoriaCambio` (Mongo) | `valorAnterior`, `valorNuevo` |
| `ArchivoClinico` (Mongo) | `nombreOriginal`, `contenido` |

Índices de búsqueda deterministas (HMAC-SHA256): `Usuario.emailHash` (UNIQUE), `Usuario.nifHash`
(UNIQUE), `DatoClinico.tipoHash`. Contraseña: solo hash BCrypt. `PasswordResetToken.tokenHash`:
SHA-256.

### Documentos MongoDB (fuera de la jerarquía `BaseEntity`)

```mermaid
classDiagram
    direction LR
    class ArchivoClinico {
        <<Document "archivo_clinico">>
        +UUID id
        +UUID usuarioId
        +String nombreOriginal
        +String contentType
        +Long sizeBytes
        +byte[] contenido
        +LocalDateTime fechaCreacion
        +LocalDateTime fechaUltimaModificacion
    }
    class AuditoriaCambio {
        <<Document "auditoria_cambio">>
        +String id
        +String idUsuario
        +String idPaciente
        +String idMedico
        +String tabla
        +String tipoCambio
        +String valorAnterior
        +String valorNuevo
        +TipoOperacion tipoOperacion
        +String razonCambio
        +String idRecurso
        +String metadatos
        +LocalDateTime fechaCambio
    }
    class TipoOperacion {
        <<enumeration>>
        CREATE
        UPDATE
        DELETE
    }
    class AccessLog {
        <<Document "log_acceso">>
        +String id
        +String usuarioId
        +LocalDateTime timestamp
        +String metodo
        +String ruta
        +Integer estado
        +String ip
        +String userAgent
        +Long duracionMs
    }
    class GoogleLoginCode {
        <<Document "google_login_code">>
        +String id
        +String token
        +long expirationTime
        +boolean requiresTwoFactor
        +String challengeId
        +Instant fechaExpiracion
    }
    class TwoFactorChallenge {
        <<Document "two_factor_challenge">>
        +String id
        +String usuarioId
        +int intentos
        +Instant fechaExpiracion
    }
    class PasswordResetToken {
        <<Document "password_reset_token">>
        +String id
        +String tokenHash
        +String usuarioId
        +boolean usado
        +Instant fechaCreacion
        +Instant fechaExpiracion
    }
    AuditoriaCambio --> TipoOperacion
```

`AuditoriaCambio.idMedico` se rellena cuando el cambio lo origina un médico a través de una
`PropuestaCambioClinico` aceptada por el paciente; en los cambios que hace el paciente sobre su
propio historial es nulo.

---

## 3. Diagrama de clases UML — arquitectura en capas

Patrón por *vertical slice*: cada área funcional (autenticación, historial, médico, admin,
perfil, notificaciones, rangos, relaciones, propuestas de cambio, reset de contraseña) repite la
misma pila. Interfaz + implementación en cada capa. La autorización declarativa (`@PreAuthorize`)
vive en la **fachada**.

```mermaid
classDiagram
    direction TB

    class RestController {
        <<@RestController>>
        maneja HTTP, DTOs, códigos de estado
    }
    class Facade {
        <<@Service + @PreAuthorize>>
        orquesta casos de uso, valida entrada, autoriza
    }
    class DomainService {
        <<@Service + @Transactional>>
        reglas de negocio, cifrado, auditoría
    }
    class Repository {
        <<Spring Data>>
        JpaRepository / MongoRepository
    }
    class Converter {
        <<@Component>>
        Entidad ↔ DTO
    }
    class Entity {
        <<@Entity / @Document>>
    }
    class DTO {
        <<data>>
    }

    RestController --> Facade : delega
    Facade --> DomainService : usa
    Facade --> Converter : mapea
    DomainService --> Repository : persiste / consulta
    DomainService --> Converter : mapea
    Repository --> Entity : gestiona
    Converter --> Entity
    Converter --> DTO
```

### Componentes reales por capa

```mermaid
flowchart TB
    subgraph Cliente["Frontend — Vue 3 + Pinia/axios (frontend/src)"]
        V["Views / Components"] --> SVC["services/*.js (axios)"]
        V --> RT["router/index.js — guards por rol"]
    end

    subgraph Seguridad["Filtros de seguridad (config/)"]
        JWT["JwtAuthenticationFilter"]
        ALF["AccessLogFilter"]
        WSC["WebSecurityConfig — /admin/** ADMINISTRADOR · /medico/** MEDICO · resto authenticated"]
    end

    subgraph Controllers["controller/impl"]
        C1["AutenticacionControllerImpl"]
        C2["HistorialClinicoControllerImpl"]
        C3["MedicoControllerImpl"]
        C4["AdminControllerImpl"]
        C5["UsuarioControllerImpl"]
        C6["PerfilControllerImpl"]
        C7["NotificacionControllerImpl"]
        C8["RangoControllerImpl"]
        C9["RelacionMedicoPacienteControllerImpl"]
        C10["PasswordResetControllerImpl"]
    end

    subgraph Facades["facade/impl — @PreAuthorize"]
        F1["AutenticacionFacadeImpl"]
        F2["HistorialClinicoFacadeImpl"]
        F3["MedicoFacadeImpl"]
        F4["AdminFacadeImpl"]
        F5["UsuarioFacadeImpl"]
        F6["PerfilFacadeImpl"]
        F7["NotificacionFacadeImpl"]
        F8["RangoFacadeImpl"]
        F9["RelacionMedicoPacienteFacadeImpl"]
        F10["PasswordResetFacadeImpl"]
    end

    subgraph Services["service/impl"]
        S1["AutenticacionServiceImpl"]
        S2["HistorialClinicoServiceImpl"]
        S3["MedicoServiceImpl"]
        S4["SolicitudAsignacionServiceImpl"]
        S5["RelacionMedicoPacienteServiceImpl"]
        S6["AnotacionMedicaServiceImpl"]
        S7["ArchivoClinicoServiceImpl"]
        S8["NotificacionServiceImpl"]
        S9["PerfilServiceImpl / PerfilUsuarioServiceImpl"]
        S10["RangoServiceImpl"]
        S11["UsuarioServiceImpl"]
        S12["PasswordResetServiceImpl"]
        S13["PropuestaCambioClinicoServiceImpl"]
        SX["JwtServiceImpl · TotpServiceImpl · EmailServiceImpl"]
        SC["AuditoriaCambioServiceImpl · AccessLogServiceImpl"]
        CR["FieldEncryptionServiceImpl · HmacSearchIndexServiceImpl · ArchivoCifradoServiceImpl"]
    end

    subgraph Repos["repository"]
        RPG["JpaRepository: Usuario, Perfil, PerfilUsuario, HistorialClinico,\nDatoClinico, Rango, Alergia, AntecedenteClinico, AnotacionMedica,\nMedicoPaciente, SolicitudAsignacion, PropuestaCambioClinico, Notificacion"]
        RMG["MongoRepository: ArchivoClinico, AuditoriaCambio, AccessLog,\nGoogleLoginCode, TwoFactorChallenge, PasswordResetToken"]
    end

    DB1[("PostgreSQL")]
    DB2[("MongoDB")]
    EXT["Google OAuth2 / OIDC · SMTP"]

    SVC -->|"HTTPS + Bearer JWT"| JWT --> ALF --> Controllers
    WSC -.protege.- Controllers
    Controllers --> Facades --> Services
    Services --> Repos
    RPG --> DB1
    RMG --> DB2
    S1 --> EXT
    SX --> EXT
    Services -.->|convierten con| Conv["converter/* · mapper/*"]
    Services -.->|cifran / indexan con| CR
    Services -.->|auditan con| SC

    subgraph Converters["converter/ · mapper/ (Entidad ↔ DTO)"]
        Conv
    end
```

### Capa transversal de protección de datos (RGPD / LOPDGDD)

```mermaid
flowchart LR
    A["Campo de entidad\n(@Convert)"] --> AES["AESEncryptionConverter\nAES/GCM no determinista"]
    AES --> COL[("columna cifrada")]
    B["Valor buscable\n(NIF, email, tipo)"] --> HMAC["HmacSearchIndexService\nHMAC-SHA256"]
    HMAC --> IDX[("columna *_hash (UNIQUE / índice)")]
    C["Documento MongoDB\n(nombre archivo, valores auditoría)"] --> FES["FieldEncryptionService /\nArchivoCifradoService"]
    FES --> DOC[("campo cifrado en Mongo")]
    D["Clave"] --> EKP["EncryptionKeyProvider\nTFM_HCC_ENCRYPTION_KEY"]
    EKP --> AES
    EKP --> HMAC
    EKP --> FES
```

---

## 4. Casos de uso y roles (contexto)

| Rol | Vías de acceso | Capacidades principales |
|---|---|---|
| **PACIENTE** | NIF + contraseña (+ TOTP opcional), Google OAuth2 | Consentimiento explícito al registrarse (art. 9.2.a RGPD); ver y editar su historial (antecedentes, alergias, análisis de sangre/orina, signos vitales), incluida la edición individual de un dato; confirmar o rechazar los cambios propuestos por un médico; subir archivos clínicos; aceptar/rechazar solicitudes de médicos y revocarlos; ver logs de acceso; exportar sus datos; limitar/reanudar el tratamiento (art. 18 RGPD); borrado lógico de cuenta |
| **MEDICO** | NIF + contraseña (+ TOTP) | Buscar pacientes, enviar solicitudes de asignación, consultar el historial de pacientes asignados (`estado = ACTIVA` y cuenta no `SUSPENDIDO`), crear anotaciones médicas, ver/subir archivos del paciente, **proponer** altas/correcciones/borrados sobre el historial (con motivo obligatorio; solo se aplican tras la confirmación del paciente) |
| **ADMINISTRADOR** | NIF + contraseña (+ TOTP) | Alta/baja de médicos, otorgar/retirar el perfil médico, buscar usuarios por NIF. Sin acceso a datos clínicos |

Autenticación: JWT sin estado (`Authorization: Bearer`, 1 hora), invalidado por `lastPasswordChange`.
Segundo factor TOTP (RFC 6238) opcional, con un máximo de 5 intentos por reto. Login con Google
mediante código de un solo uso persistido en MongoDB con TTL.

---

_Para regenerar: revisar `model/`, `repository/`, `controller/impl/`, `facade/impl/`,
`service/impl/` y `config/WebSecurityConfig.java`._
