require('dotenv').config();

const os = require('os');
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');

const medicamentos = require('./routes/medicamentos');
const clinica = require('./routes/clinica');
const notificaciones = require('./routes/notificaciones');
const { MODO } = require('./mailer');

const app = express();
const PORT = Number(process.env.PORT || 3000);
const HOST = process.env.HOST || '0.0.0.0'; // 0.0.0.0 = acepta conexiones de otros equipos de la red

app.use(cors());
app.use(express.json());
app.use(morgan('dev'));

app.get('/health', (req, res) => {
  res.json({ estado: 'OK', servicio: 'petcare-api-externa', modoCorreo: MODO, hora: new Date().toISOString() });
});

app.use('/api/medicamentos', medicamentos);
app.use('/api/clinica', clinica);
app.use('/api/notificaciones', notificaciones);

app.use((req, res) => {
  res.status(404).json({ error: 'NO_ENCONTRADO', mensaje: `Ruta ${req.method} ${req.originalUrl} no existe.` });
});

// eslint-disable-next-line no-unused-vars
app.use((err, req, res, next) => {
  console.error('[error]', err.message);
  res.status(err.status || 500).json({
    error: err.codigo || 'ERROR_INTERNO',
    mensaje: err.message || 'Error inesperado.',
  });
});

app.listen(PORT, HOST, () => {
  const ips = Object.values(os.networkInterfaces())
    .flat()
    .filter((i) => i && i.family === 'IPv4' && !i.internal)
    .map((i) => i.address);

  console.log(`\nPetCare API externa escuchando en el puerto ${PORT} (modo correo: ${MODO})`);
  console.log(`  Local:      http://localhost:${PORT}/health`);
  ips.forEach((ip) => console.log(`  En la red:  http://${ip}:${PORT}/health   <- usa esta URL en Spring`));
  console.log('');
});
