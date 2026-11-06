const nodemailer = require('nodemailer');

/**
 * Modos de envío (variable MAIL_MODE en .env):
 *  - ethereal (por defecto): crea una cuenta de prueba en ethereal.email.
 *    Los correos NO llegan a una bandeja real; se ven en un link de vista previa.
 *  - smtp: usa un servidor SMTP real (SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS).
 *  - simulado: no envía nada, solo registra la notificación (útil sin internet).
 */
const MODO = (process.env.MAIL_MODE || 'ethereal').toLowerCase();

let transporter = null;
let remitente = process.env.MAIL_FROM || '"PetCare" <no-reply@petcare.cl>';

async function obtenerTransporter() {
  if (transporter) return transporter;

  if (MODO === 'smtp') {
    transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST,
      port: Number(process.env.SMTP_PORT || 587),
      secure: Number(process.env.SMTP_PORT) === 465,
      auth: { user: process.env.SMTP_USER, pass: process.env.SMTP_PASS },
    });
  } else {
    const cuenta = await nodemailer.createTestAccount();
    transporter = nodemailer.createTransport({
      host: cuenta.smtp.host,
      port: cuenta.smtp.port,
      secure: cuenta.smtp.secure,
      auth: { user: cuenta.user, pass: cuenta.pass },
    });
    console.log(`[mailer] Cuenta Ethereal creada: ${cuenta.user}`);
  }
  return transporter;
}

/**
 * Envía un correo y devuelve { modo, messageId, previewUrl }.
 */
async function enviarCorreo({ para, asunto, html, texto }) {
  if (MODO === 'simulado') {
    return { modo: 'simulado', messageId: null, previewUrl: null };
  }

  const t = await obtenerTransporter();
  const info = await t.sendMail({ from: remitente, to: para, subject: asunto, text: texto, html });
  return {
    modo: MODO,
    messageId: info.messageId,
    previewUrl: nodemailer.getTestMessageUrl(info) || null,
  };
}

module.exports = { enviarCorreo, MODO };
