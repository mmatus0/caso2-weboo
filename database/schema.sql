-- =====================================================================
-- PetCare - Sistema de Gestión de Atención Veterinaria
-- Script de creación de la base de datos (MySQL 8)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS petcare
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE petcare;

DROP TABLE IF EXISTS receta;
DROP TABLE IF EXISTS atencion;
DROP TABLE IF EXISTS reserva;
DROP TABLE IF EXISTS mascota;
DROP TABLE IF EXISTS veterinario;
DROP TABLE IF EXISTS cliente;

-- ---------------------------------------------------------------------
-- CLIENTE: dueño de una o más mascotas
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  rut         VARCHAR(12)  NOT NULL,
  nombre      VARCHAR(100) NOT NULL,
  email       VARCHAR(120) NOT NULL,
  telefono    VARCHAR(20)  NOT NULL,
  direccion   VARCHAR(200),
  creado_en   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_cliente PRIMARY KEY (id),
  CONSTRAINT uk_cliente_rut UNIQUE (rut),
  CONSTRAINT uk_cliente_email UNIQUE (email)
);

-- ---------------------------------------------------------------------
-- MASCOTA: pertenece a un cliente (1 cliente -> N mascotas)
-- Si se elimina el cliente, se eliminan sus mascotas.
-- ---------------------------------------------------------------------
CREATE TABLE mascota (
  id                BIGINT       NOT NULL AUTO_INCREMENT,
  nombre            VARCHAR(60)  NOT NULL,
  especie           VARCHAR(30)  NOT NULL,
  raza              VARCHAR(60),
  fecha_nacimiento  DATE,
  peso              DECIMAL(5,2),
  cliente_id        BIGINT       NOT NULL,
  CONSTRAINT pk_mascota PRIMARY KEY (id),
  CONSTRAINT fk_mascota_cliente FOREIGN KEY (cliente_id)
    REFERENCES cliente (id) ON DELETE CASCADE,
  CONSTRAINT ck_mascota_peso CHECK (peso IS NULL OR peso > 0)
);

-- ---------------------------------------------------------------------
-- VETERINARIO: profesional que atiende las horas médicas
-- ---------------------------------------------------------------------
CREATE TABLE veterinario (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(100) NOT NULL,
  especialidad  VARCHAR(80)  NOT NULL,
  email         VARCHAR(120) NOT NULL,
  activo        BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT pk_veterinario PRIMARY KEY (id),
  CONSTRAINT uk_veterinario_email UNIQUE (email)
);

-- ---------------------------------------------------------------------
-- RESERVA: hora médica (bloques de 1 hora) de una mascota con un veterinario
--
-- Regla anti-duplicados: un veterinario no puede tener dos reservas
-- ACTIVAS (no canceladas) el mismo día a la misma hora.
-- MySQL no tiene índices únicos parciales, así que usamos una columna
-- generada: vale 1 si la reserva está activa y NULL si está cancelada.
-- Como UNIQUE permite varios NULL, una hora cancelada vuelve a quedar libre.
-- ---------------------------------------------------------------------
CREATE TABLE reserva (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  fecha           DATE         NOT NULL,
  hora            TIME         NOT NULL,
  motivo          VARCHAR(200) NOT NULL,
  estado          VARCHAR(15)  NOT NULL DEFAULT 'PENDIENTE',
  mascota_id      BIGINT       NOT NULL,
  veterinario_id  BIGINT       NOT NULL,
  creado_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  slot_activo     TINYINT GENERATED ALWAYS AS (
                    CASE WHEN estado <> 'CANCELADA' THEN 1 ELSE NULL END
                  ) STORED,
  CONSTRAINT pk_reserva PRIMARY KEY (id),
  CONSTRAINT fk_reserva_mascota FOREIGN KEY (mascota_id)
    REFERENCES mascota (id) ON DELETE CASCADE,
  CONSTRAINT fk_reserva_veterinario FOREIGN KEY (veterinario_id)
    REFERENCES veterinario (id),
  CONSTRAINT ck_reserva_estado
    CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'ATENDIDA')),
  CONSTRAINT uk_reserva_vet_fecha_hora
    UNIQUE (veterinario_id, fecha, hora, slot_activo)
);

-- ---------------------------------------------------------------------
-- ATENCION: registro médico generado al atender una reserva (1 a 1)
-- Conforma el historial médico de la mascota.
-- ---------------------------------------------------------------------
CREATE TABLE atencion (
  id           BIGINT   NOT NULL AUTO_INCREMENT,
  reserva_id   BIGINT   NOT NULL,
  diagnostico  TEXT     NOT NULL,
  tratamiento  TEXT     NOT NULL,
  observaciones TEXT,
  fecha        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_atencion PRIMARY KEY (id),
  CONSTRAINT uk_atencion_reserva UNIQUE (reserva_id),
  CONSTRAINT fk_atencion_reserva FOREIGN KEY (reserva_id)
    REFERENCES reserva (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- RECETA: medicamento prescrito en una atención (1 atención -> N recetas)
-- El nombre del medicamento se consulta en la API externa de medicamentos.
-- ---------------------------------------------------------------------
CREATE TABLE receta (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  atencion_id  BIGINT       NOT NULL,
  medicamento  VARCHAR(100) NOT NULL,
  dosis        VARCHAR(60)  NOT NULL,
  frecuencia   VARCHAR(60)  NOT NULL,
  dias         INT          NOT NULL,
  CONSTRAINT pk_receta PRIMARY KEY (id),
  CONSTRAINT fk_receta_atencion FOREIGN KEY (atencion_id)
    REFERENCES atencion (id) ON DELETE CASCADE,
  CONSTRAINT ck_receta_dias CHECK (dias > 0)
);

CREATE INDEX idx_reserva_fecha ON reserva (fecha);
CREATE INDEX idx_reserva_estado ON reserva (estado);
