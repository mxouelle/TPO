-- ============================================================================
-- TenisSkillGames - esquema relacional PostgreSQL 15
-- Uso: crear la base y ejecutar este script; luego SPRING_PROFILES_ACTIVE=prod
--      (spring.jpa.hibernate.ddl-auto=validate en application-prod.yml)
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ---------------------------------------------------------------- USUARIOS
CREATE TABLE usuarios (
    id                    UUID PRIMARY KEY,
    nombre                VARCHAR(80)  NOT NULL,
    email                 VARCHAR(120) NOT NULL UNIQUE,
    password_hash         VARCHAR(100) NOT NULL,
    nivel_ntrp            DOUBLE PRECISION NOT NULL,
    mano_habil            VARCHAR(15),
    golpe_preferido       VARCHAR(15),
    lado_preferido        VARCHAR(15),
    superficie_favorita   VARCHAR(25),
    zona                  VARCHAR(60),
    latitud               DOUBLE PRECISION,
    longitud              DOUBLE PRECISION,
    strikes               INTEGER NOT NULL DEFAULT 0,
    verificado            VARCHAR(15),
    partidos_jugados      INTEGER NOT NULL DEFAULT 0,
    ausencias             INTEGER NOT NULL DEFAULT 0,
    fair_play_promedio    DOUBLE PRECISION NOT NULL DEFAULT 5,
    puntualidad_promedio  DOUBLE PRECISION NOT NULL DEFAULT 5,
    fecha_registro        TIMESTAMP,
    suspendido_hasta      TIMESTAMP,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,
    telefono              VARCHAR(30),
    push_token            VARCHAR(200)
);

CREATE TABLE usuario_roles (
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    rol        VARCHAR(20) NOT NULL,
    PRIMARY KEY (usuario_id, rol)
);

CREATE TABLE usuario_canales (
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    canal      VARCHAR(15) NOT NULL,
    PRIMARY KEY (usuario_id, canal)
);

CREATE TABLE usuario_franjas (
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    franja     VARCHAR(40) NOT NULL
);

CREATE INDEX idx_usuarios_zona ON usuarios (zona);
CREATE INDEX idx_usuarios_ntrp ON usuarios (nivel_ntrp);

-- ---------------------------------------------------------------- PARTIDOS
CREATE TABLE partidos (
    id                    UUID PRIMARY KEY,
    organizador_id        UUID NOT NULL REFERENCES usuarios (id),
    formato               VARCHAR(20),
    superficie            VARCHAR(25),
    club_sede             VARCHAR(100),
    direccion             VARCHAR(160),
    latitud               DOUBLE PRECISION,
    longitud              DOUBLE PRECISION,
    ntrp_min              DOUBLE PRECISION NOT NULL,
    ntrp_max              DOUBLE PRECISION NOT NULL,
    radio_max_km          INTEGER NOT NULL DEFAULT 10,
    fecha_hora            TIMESTAMP NOT NULL,
    duracion_minutos      INTEGER NOT NULL DEFAULT 90,
    modalidad             VARCHAR(30),
    cupo_maximo           INTEGER NOT NULL,
    estado_actual         VARCHAR(25) NOT NULL,
    estrategia            VARCHAR(20),
    motivo_cancelacion    VARCHAR(200),
    fecha_creacion        TIMESTAMP,
    observaciones         VARCHAR(500),
    recordatorio_24_enviado BOOLEAN NOT NULL DEFAULT FALSE,
    recordatorio_2_enviado  BOOLEAN NOT NULL DEFAULT FALSE
);

-- Indice compuesto exigido por la consigna
CREATE INDEX idx_partidos_estado_fecha_superficie
    ON partidos (estado_actual, fecha_hora, superficie);

-- ---------------------------------------------------------- POSTULACIONES
CREATE TABLE postulaciones (
    id                 UUID PRIMARY KEY,
    usuario_id         UUID NOT NULL REFERENCES usuarios (id),
    partido_id         UUID NOT NULL REFERENCES partidos (id) ON DELETE CASCADE,
    lado_asignado      VARCHAR(15),
    confirmado         BOOLEAN,
    fecha_confirmacion TIMESTAMP,
    estado             VARCHAR(20) NOT NULL,
    fecha_postulacion  TIMESTAMP,
    motivo_rechazo     VARCHAR(200)
);

CREATE INDEX idx_postulaciones_partido ON postulaciones (partido_id);
CREATE INDEX idx_postulaciones_usuario ON postulaciones (usuario_id);

-- ---------------------------------------------------------------- PAREJAS
CREATE TABLE parejas (
    id         UUID PRIMARY KEY,
    partido_id UUID NOT NULL REFERENCES partidos (id) ON DELETE CASCADE,
    lado       VARCHAR(15)
);

CREATE TABLE pareja_jugadores (
    pareja_id  UUID NOT NULL REFERENCES parejas (id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios (id),
    orden      INTEGER NOT NULL,
    PRIMARY KEY (pareja_id, orden)
);

-- ------------------------------------------------------ RESULTADOS_PARTIDO
CREATE TABLE resultados_partido (
    id             UUID PRIMARY KEY,
    partido_id     UUID NOT NULL UNIQUE REFERENCES partidos (id) ON DELETE CASCADE,
    ganador_lado   VARCHAR(5),
    marcador_sets  VARCHAR(60),
    duracion_min   INTEGER NOT NULL,
    observaciones  VARCHAR(500),
    validado       BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_carga    TIMESTAMP
);

-- ---------------------------------------------------------- NOTIFICACIONES
CREATE TABLE notificaciones (
    id             UUID PRIMARY KEY,
    usuario_id     UUID REFERENCES usuarios (id) ON DELETE SET NULL,
    partido_id     UUID REFERENCES partidos (id) ON DELETE SET NULL,
    tipo_evento    VARCHAR(30),
    canal          VARCHAR(20),
    asunto         VARCHAR(160),
    contenido      VARCHAR(1000),
    estado_envio   VARCHAR(15),
    intentos       INTEGER NOT NULL DEFAULT 0,
    ultimo_error   VARCHAR(300),
    fecha_creacion TIMESTAMP,
    fecha_envio    TIMESTAMP
);

CREATE INDEX idx_notificaciones_usuario ON notificaciones (usuario_id);
CREATE INDEX idx_notificaciones_partido ON notificaciones (partido_id);

-- ------------------------------------------------------------ EVALUACIONES
CREATE TABLE evaluaciones (
    id                UUID PRIMARY KEY,
    partido_id        UUID NOT NULL REFERENCES partidos (id) ON DELETE CASCADE,
    evaluador_id      UUID NOT NULL REFERENCES usuarios (id),
    evaluado_id       UUID NOT NULL REFERENCES usuarios (id),
    fair_play         SMALLINT NOT NULL CHECK (fair_play BETWEEN 1 AND 5),
    puntualidad       SMALLINT NOT NULL CHECK (puntualidad BETWEEN 1 AND 5),
    comentario        VARCHAR(500),
    moderacion        VARCHAR(15),
    fecha_evaluacion  TIMESTAMP
);

CREATE INDEX idx_evaluaciones_evaluado ON evaluaciones (evaluado_id);

-- ------------------------------------------------------ REPORTES_CONDUCTA
CREATE TABLE reportes_conducta (
    id            UUID PRIMARY KEY,
    partido_id    UUID REFERENCES partidos (id) ON DELETE SET NULL,
    reportante_id UUID NOT NULL REFERENCES usuarios (id),
    reportado_id  UUID NOT NULL REFERENCES usuarios (id),
    motivo        VARCHAR(30),
    descripcion   VARCHAR(500),
    estado        VARCHAR(15),
    resolucion    VARCHAR(300),
    fecha_reporte TIMESTAMP
);

CREATE INDEX idx_reportes_reportado ON reportes_conducta (reportado_id);

-- ---------------------------------------------------- TRANSICIONES_ESTADO
CREATE TABLE transiciones_estado (
    id              UUID PRIMARY KEY,
    partido_id      UUID NOT NULL REFERENCES partidos (id) ON DELETE CASCADE,
    estado_anterior VARCHAR(25),
    estado_nuevo    VARCHAR(25),
    fecha           TIMESTAMP,
    motivo          VARCHAR(200)
);

CREATE INDEX idx_transiciones_partido ON transiciones_estado (partido_id);

-- ----------------------------------------------------- BUSQUEDAS_FAVORITAS
CREATE TABLE busquedas_favoritas (
    id         UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    nombre     VARCHAR(80),
    formato    VARCHAR(20),
    superficie VARCHAR(25),
    ntrp_min   DOUBLE PRECISION NOT NULL,
    ntrp_max   DOUBLE PRECISION NOT NULL,
    radio_km   INTEGER NOT NULL DEFAULT 10,
    zona       VARCHAR(60),
    activa     BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_busquedas_usuario ON busquedas_favoritas (usuario_id);
