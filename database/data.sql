-- =====================================================================
-- PetCare - Datos iniciales de prueba
-- =====================================================================

USE petcare;

-- Clientes
INSERT INTO cliente (id, rut, nombre, email, telefono, direccion) VALUES
  (1, '15432876-9', 'Camila Rojas Soto',     'camila.rojas@correo.cl',   '+56912345678', 'Av. Libertad 345, Chillán'),
  (2, '18765234-0', 'Javier Muñoz Pérez',    'javier.munoz@correo.cl',   '+56987654321', 'Calle Arauco 120, Chillán'),
  (3, '12345678-5', 'Valentina Fuentes Lagos','vale.fuentes@correo.cl',  '+56955512345', 'Av. Collín 980, Chillán'),
  (4, '20111222-2', 'Matías Contreras Vidal', 'matias.contreras@correo.cl','+56944433322','El Roble 455, Chillán'),
  (5, '17654321-3', 'Fernanda Sáez Ortiz',    'fer.saez@correo.cl',      '+56933322211', 'Av. O''Higgins 210, Chillán Viejo');

-- Mascotas
INSERT INTO mascota (id, nombre, especie, raza, fecha_nacimiento, peso, cliente_id) VALUES
  (1, 'Luna',    'Perro', 'Labrador Retriever', '2021-03-14', 28.50, 1),
  (2, 'Michi',   'Gato',  'Común europeo',      '2022-07-02',  4.20, 1),
  (3, 'Rocky',   'Perro', 'Bulldog Francés',    '2020-11-23', 12.30, 2),
  (4, 'Nala',    'Gato',  'Siamés',             '2019-05-10',  3.80, 3),
  (5, 'Toby',    'Perro', 'Poodle',             '2023-01-30',  6.10, 3),
  (6, 'Kiwi',    'Ave',   'Cotorra argentina',  '2022-09-12',  0.12, 4),
  (7, 'Simba',   'Gato',  'Maine Coon',         '2021-12-05',  7.40, 5),
  (8, 'Canela',  'Perro', 'Mestizo',            '2018-08-19', 18.00, 5);

-- Veterinarios
INSERT INTO veterinario (id, nombre, especialidad, email, activo) VALUES
  (1, 'Dra. Paula Herrera',  'Medicina general',     'paula.herrera@petcare.cl',  TRUE),
  (2, 'Dr. Andrés Navarro',  'Cirugía',              'andres.navarro@petcare.cl', TRUE),
  (3, 'Dra. Sofía Riquelme', 'Dermatología',         'sofia.riquelme@petcare.cl', TRUE);

-- Reservas (bloques de 1 hora entre 09:00 y 18:00)
INSERT INTO reserva (id, fecha, hora, motivo, estado, mascota_id, veterinario_id) VALUES
  (1,  '2026-10-20', '10:00:00', 'Control anual y vacunas',        'ATENDIDA',   1, 1),
  (2,  '2026-10-22', '11:00:00', 'Picazón y caída de pelo',        'ATENDIDA',   4, 3),
  (3,  '2026-10-28', '15:00:00', 'Vómitos desde ayer',             'ATENDIDA',   3, 1),
  (4,  '2026-11-03', '09:00:00', 'Control de peso',                'CANCELADA',  7, 1),
  (5,  '2026-11-20', '09:00:00', 'Vacuna antirrábica',             'CONFIRMADA', 2, 1),
  (6,  '2026-11-20', '10:00:00', 'Revisión de herida en pata',     'PENDIENTE',  8, 2),
  (7,  '2026-11-20', '10:00:00', 'Control dermatológico',          'PENDIENTE',  4, 3),
  (8,  '2026-11-23', '16:00:00', 'Evaluación pre quirúrgica',      'CONFIRMADA', 3, 2),
  (9,  '2026-11-24', '12:00:00', 'Corte de uñas y control',        'PENDIENTE',  6, 1),
  (10, '2026-11-25', '17:00:00', 'Desparasitación',                'PENDIENTE',  5, 1);

-- Atenciones (historial médico de las reservas ATENDIDAS)
INSERT INTO atencion (id, reserva_id, diagnostico, tratamiento, observaciones, fecha) VALUES
  (1, 1, 'Paciente sano. Peso adecuado para su talla.',
         'Vacuna óctuple anual y desparasitación interna.',
         'Próximo control en 12 meses.', '2026-10-20 10:40:00'),
  (2, 2, 'Dermatitis alérgica leve.',
         'Antihistamínico oral y shampoo medicado dos veces por semana.',
         'Evitar cambios bruscos de alimento.', '2026-10-22 11:35:00'),
  (3, 3, 'Gastroenteritis aguda.',
         'Dieta blanda por 5 días, antiemético y protector gástrico.',
         'Volver si persisten los vómitos más de 48 horas.', '2026-10-28 15:45:00');

-- Recetas (medicamentos prescritos en cada atención)
INSERT INTO receta (id, atencion_id, medicamento, dosis, frecuencia, dias) VALUES
  (1, 1, 'Praziquantel',  '1 comprimido',   'Dosis única',      1),
  (2, 2, 'Cetirizina',    '1/2 comprimido', 'Cada 24 horas',   10),
  (3, 3, 'Maropitant',    '1 comprimido',   'Cada 24 horas',    3),
  (4, 3, 'Omeprazol',     '1 cápsula',      'Cada 24 horas',    5);

-- Ajustar los AUTO_INCREMENT para que los nuevos registros sigan la numeración
ALTER TABLE cliente     AUTO_INCREMENT = 6;
ALTER TABLE mascota     AUTO_INCREMENT = 9;
ALTER TABLE veterinario AUTO_INCREMENT = 4;
ALTER TABLE reserva     AUTO_INCREMENT = 11;
ALTER TABLE atencion    AUTO_INCREMENT = 4;
ALTER TABLE receta      AUTO_INCREMENT = 5;
