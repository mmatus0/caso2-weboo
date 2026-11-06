import { api } from './api.js';
import { $, esc, toast, toastError } from './ui.js';

let mapa = null;
let clinica = null;

/** Muestra la clínica en un mapa Leaflet con teselas de OpenStreetMap. */
export async function cargar() {
  if (mapa) {
    setTimeout(() => mapa.invalidateSize(), 50); // el div estaba oculto al crearse
    return;
  }
  try {
    clinica = await api.clinica();
  } catch (e) {
    $('#clinica-info').innerHTML = `<p class="muted">No se pudo obtener la ubicación: ${esc(e.message)}</p>`;
    return;
  }

  const c = clinica;
  $('#clinica-info').innerHTML = `
    <h3>${esc(c.nombre)}</h3>
    <div class="dato"><b>Dirección</b><span>${esc(c.direccion)}, ${esc(c.ciudad)}</span></div>
    <div class="dato"><b>Región</b><span>${esc(c.region)}</span></div>
    <div class="dato"><b>Teléfono</b><span>${esc(c.telefono)}</span></div>
    <div class="dato"><b>Email</b><span>${esc(c.email)}</span></div>
    <div class="dato"><b>Horario</b><span>${esc(c.horario)}</span></div>
    <div class="dato"><b>Coordenadas</b><span>${c.latitud}, ${c.longitud}</span></div>
    <div class="form-acciones" style="justify-content:flex-start">
      <button class="btn" id="btn-distancia">¿A qué distancia estoy?</button>
      <a class="btn sec" href="${esc(c.mapa.comoLlegar)}" target="_blank" rel="noopener">Cómo llegar</a>
    </div>
    <p id="distancia" class="muted"></p>`;

  $('#btn-distancia').addEventListener('click', calcularDistancia);

  if (typeof L === 'undefined') {
    $('#mapa').innerHTML = '<p class="muted" style="padding:16px">No se pudo cargar la librería de mapas (requiere internet).</p>';
    return;
  }
  mapa = L.map('mapa').setView([c.latitud, c.longitud], c.mapa.zoomSugerido || 16);
  L.tileLayer(c.mapa.tilesUrl, {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors',
  }).addTo(mapa);
  L.marker([c.latitud, c.longitud])
    .addTo(mapa)
    .bindPopup(`<b>${esc(c.nombre)}</b><br>${esc(c.direccion)}, ${esc(c.ciudad)}`)
    .openPopup();
  setTimeout(() => mapa.invalidateSize(), 50);
}

function calcularDistancia() {
  if (!navigator.geolocation) return toast('Tu navegador no permite geolocalización', 'aviso');
  $('#distancia').textContent = 'Obteniendo tu ubicación...';

  navigator.geolocation.getCurrentPosition(
    async ({ coords }) => {
      try {
        const d = await api.distancia(coords.latitude, coords.longitude);
        $('#distancia').innerHTML = `Estás a <b>${d.distanciaKm} km</b> de la clínica.`;
        L.circleMarker([coords.latitude, coords.longitude], { radius: 8 }).addTo(mapa).bindPopup('Tú estás aquí');
        mapa.fitBounds([[coords.latitude, coords.longitude], [clinica.latitud, clinica.longitud]], { padding: [40, 40] });
      } catch (e) {
        toastError(e);
      }
    },
    () => {
      $('#distancia').textContent = 'No se pudo obtener tu ubicación (permiso denegado).';
    }
  );
}
