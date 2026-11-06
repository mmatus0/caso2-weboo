const express = require('express');
const catalogo = require('../data/medicamentos.json');

const router = express.Router();

// Quita tildes y pasa a minúsculas para que "amoxicilina" encuentre "Amoxicilina"
// y "meloxicam" encuentre "Meloxicam" aunque el usuario escriba con o sin tildes.
const normalizar = (texto) =>
  String(texto)
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .trim();

/**
 * GET /api/medicamentos?nombre=amoxi
 * Busca medicamentos cuyo nombre contenga el texto indicado.
 * Sin parámetro "nombre" devuelve el catálogo completo.
 */
router.get('/', (req, res) => {
  const { nombre } = req.query;

  if (nombre === undefined) {
    return res.json({ total: catalogo.length, resultados: catalogo });
  }

  const busqueda = normalizar(nombre);
  if (busqueda.length < 2) {
    return res.status(400).json({
      error: 'PARAMETRO_INVALIDO',
      mensaje: 'El parámetro "nombre" debe tener al menos 2 caracteres.',
    });
  }

  const resultados = catalogo.filter((m) => normalizar(m.nombre).includes(busqueda));
  res.json({ busqueda: nombre, total: resultados.length, resultados });
});

/**
 * GET /api/medicamentos/:id
 * Devuelve el detalle de un medicamento por su id.
 */
router.get('/:id', (req, res) => {
  const id = Number(req.params.id);
  const medicamento = catalogo.find((m) => m.id === id);

  if (!medicamento) {
    return res.status(404).json({
      error: 'NO_ENCONTRADO',
      mensaje: `No existe un medicamento con id ${req.params.id}.`,
    });
  }
  res.json(medicamento);
});

module.exports = router;
