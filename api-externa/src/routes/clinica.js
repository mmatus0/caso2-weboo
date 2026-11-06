const express = require('express');

const router = express.Router();

// Datos de la clínica (ficticia) ubicada en el centro de Chillán.
const CLINICA = {
  nombre: 'Clínica Veterinaria PetCare',
  direccion: 'Av. Libertad 1050',
  ciudad: 'Chillán',
  region: 'Región de Ñuble',
  pais: 'Chile',
  telefono: '+56 42 222 3344',
  email: 'contacto@petcare.cl',
  horario: 'Lunes a sábado, 09:00 a 18:00',
  latitud: -36.6085,
  longitud: -72.0985,
};

// Distancia en km entre dos coordenadas (fórmula de Haversine)
function distanciaKm(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const rad = (g) => (g * Math.PI) / 180;
  const dLat = rad(lat2 - lat1);
  const dLon = rad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

/**
 * GET /api/clinica/ubicacion
 * Devuelve la ubicación de la clínica y enlaces de mapa listos para usar.
 */
router.get('/ubicacion', (req, res) => {
  const { latitud, longitud } = CLINICA;
  res.json({
    ...CLINICA,
    mapa: {
      proveedor: 'OpenStreetMap',
      zoomSugerido: 16,
      tilesUrl: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
      verEnOpenStreetMap: `https://www.openstreetmap.org/?mlat=${latitud}&mlon=${longitud}#map=17/${latitud}/${longitud}`,
      verEnGoogleMaps: `https://www.google.com/maps/search/?api=1&query=${latitud},${longitud}`,
      comoLlegar: `https://www.google.com/maps/dir/?api=1&destination=${latitud},${longitud}`,
    },
  });
});

/**
 * GET /api/clinica/distancia?lat=-36.61&lng=-72.10
 * Calcula a cuántos km está el usuario de la clínica.
 */
router.get('/distancia', (req, res) => {
  const lat = Number(req.query.lat);
  const lng = Number(req.query.lng);

  if (!Number.isFinite(lat) || !Number.isFinite(lng) || Math.abs(lat) > 90 || Math.abs(lng) > 180) {
    return res.status(400).json({
      error: 'PARAMETRO_INVALIDO',
      mensaje: 'Debe enviar "lat" y "lng" numéricos válidos.',
    });
  }

  const km = distanciaKm(lat, lng, CLINICA.latitud, CLINICA.longitud);
  res.json({
    origen: { latitud: lat, longitud: lng },
    destino: { nombre: CLINICA.nombre, latitud: CLINICA.latitud, longitud: CLINICA.longitud },
    distanciaKm: Math.round(km * 100) / 100,
  });
});

module.exports = router;
