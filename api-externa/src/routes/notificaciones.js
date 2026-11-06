const express = require('express');
const { enviarCorreo } = require('../mailer');

const router = express.Router();

// Historial en memoria de las notificaciones enviadas (se pierde al reiniciar)
const historial = [];
let siguienteId = 1;

const TIPOS = {
  CONFIRMACION: { asunto: 'Tu hora fue CONFIRMADA', color: '#0F6E56', titulo: 'Hora confirmada' },
  CANCELACION: { asunto: 'Tu hora fue CANCELADA', color: '#A32D2D', titulo: 'Hora cancelada' },
  MODIFICACION: { asunto: 'Tu hora fue MODIFICADA', color: '#854F0B', titulo: 'Hora modificada' },
};

const escapar = (s) =>
  String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

const emailValido = (e) => typeof e === 'string' && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e);

function armarCorreo(tipo, nombreCliente, r) {
  const t = TIPOS[tipo];
  const filas = [
    ['Mascota', r.mascota],
    ['Fecha', r.fecha],
    ['Hora', r.hora],
    ['Veterinario/a', r.veterinario],
    ['Motivo', r.motivo],
  ];
  const texto =
    `Hola ${nombreCliente},\n\n${t.titulo} (reserva #${r.id}).\n` +
    filas.map(([k, v]) => `${k}: ${v ?? '-'}`).join('\n') +
    '\n\nClínica Veterinaria PetCare - Av. Libertad 1050, Chillán';

  const html = `
  <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;border:1px solid #ddd;border-radius:10px;overflow:hidden">
    <div style="background:${t.color};color:#fff;padding:16px 20px;font-size:18px">PetCare · ${t.titulo}</div>
    <div style="padding:20px;color:#333">
      <p>Hola <strong>${escapar(nombreCliente)}</strong>,</p>
      <p>Te informamos que tu reserva <strong>#${escapar(r.id)}</strong> fue actualizada:</p>
      <table style="border-collapse:collapse;width:100%">
        ${filas
          .map(
            ([k, v]) =>
              `<tr><td style="padding:6px;border-bottom:1px solid #eee;color:#777">${k}</td>
                   <td style="padding:6px;border-bottom:1px solid #eee">${escapar(v ?? '-')}</td></tr>`
          )
          .join('')}
      </table>
      <p style="margin-top:20px;font-size:13px;color:#777">Clínica Veterinaria PetCare · Av. Libertad 1050, Chillán · +56 42 222 3344</p>
    </div>
  </div>`;

  return { asunto: `PetCare: ${t.asunto} (#${r.id})`, html, texto };
}

/**
 * POST /api/notificaciones/email
 * Body: { para, nombreCliente, tipo: CONFIRMACION|CANCELACION|MODIFICACION,
 *         reserva: { id, fecha, hora, mascota, veterinario, motivo } }
 */
router.post('/email', async (req, res, next) => {
  const { para, nombreCliente, tipo, reserva } = req.body || {};

  const errores = [];
  if (!emailValido(para)) errores.push('"para" debe ser un correo válido.');
  if (!nombreCliente) errores.push('"nombreCliente" es obligatorio.');
  if (!TIPOS[tipo]) errores.push(`"tipo" debe ser uno de: ${Object.keys(TIPOS).join(', ')}.`);
  if (!reserva || reserva.id === undefined) errores.push('"reserva" con su "id" es obligatoria.');
  if (errores.length) {
    return res.status(400).json({ error: 'DATOS_INVALIDOS', mensaje: 'Solicitud inválida.', detalles: errores });
  }

  try {
    const correo = armarCorreo(tipo, nombreCliente, reserva);
    const resultado = await enviarCorreo({ para, asunto: correo.asunto, html: correo.html, texto: correo.texto });

    const registro = {
      id: siguienteId++,
      enviada: true,
      tipo,
      para,
      asunto: correo.asunto,
      reservaId: reserva.id,
      modo: resultado.modo,
      previewUrl: resultado.previewUrl,
      fecha: new Date().toISOString(),
    };
    historial.unshift(registro);
    if (historial.length > 50) historial.pop();

    console.log(`[notificacion] ${tipo} -> ${para} ${resultado.previewUrl ? '| ' + resultado.previewUrl : ''}`);
    res.status(201).json(registro);
  } catch (err) {
    err.status = 502;
    err.codigo = 'ERROR_ENVIO_CORREO';
    next(err);
  }
});

/**
 * GET /api/notificaciones
 * Lista las últimas notificaciones enviadas (máx. 50).
 */
router.get('/', (req, res) => {
  res.json({ total: historial.length, resultados: historial });
});

module.exports = router;
