// Cliente HTTP del front. Todas las llamadas van a Spring Boot (mismo origen).
// Spring, a su vez, consume la API externa de Node cuando corresponde.

const BASE = '/api';

async function request(metodo, ruta, body) {
  const opciones = { method: metodo, headers: {} };
  if (body !== undefined) {
    opciones.headers['Content-Type'] = 'application/json';
    opciones.body = JSON.stringify(body);
  }

  const resp = await fetch(BASE + ruta, opciones);
  if (resp.status === 204) return null;

  const datos = await resp.json().catch(() => null);
  if (!resp.ok) {
    // El backend siempre responde { mensaje, detalles[] } en los errores
    const error = new Error(datos?.mensaje || `Error ${resp.status}`);
    error.detalles = datos?.detalles || [];
    error.status = resp.status;
    throw error;
  }
  return datos;
}

const qs = (params) => {
  const limpio = Object.entries(params).filter(([, v]) => v !== '' && v !== null && v !== undefined);
  return limpio.length ? '?' + new URLSearchParams(limpio).toString() : '';
};

export const api = {
  // Clientes
  clientes: (buscar) => request('GET', '/clientes' + qs({ buscar })),
  cliente: (id) => request('GET', `/clientes/${id}`),
  crearCliente: (d) => request('POST', '/clientes', d),
  editarCliente: (id, d) => request('PUT', `/clientes/${id}`, d),
  eliminarCliente: (id) => request('DELETE', `/clientes/${id}`),
  mascotasDeCliente: (id) => request('GET', `/clientes/${id}/mascotas`),

  // Mascotas
  mascotas: (especie) => request('GET', '/mascotas' + qs({ especie })),
  crearMascota: (d) => request('POST', '/mascotas', d),
  editarMascota: (id, d) => request('PUT', `/mascotas/${id}`, d),
  eliminarMascota: (id) => request('DELETE', `/mascotas/${id}`),
  historial: (id) => request('GET', `/mascotas/${id}/historial`),

  // Veterinarios
  veterinarios: (soloActivos = false) => request('GET', '/veterinarios' + qs({ soloActivos })),
  crearVet: (d) => request('POST', '/veterinarios', d),
  editarVet: (id, d) => request('PUT', `/veterinarios/${id}`, d),
  eliminarVet: (id) => request('DELETE', `/veterinarios/${id}`),
  disponibilidad: (id, fecha) => request('GET', `/veterinarios/${id}/disponibilidad` + qs({ fecha })),

  // Reservas
  reservas: (filtros = {}) => request('GET', '/reservas' + qs(filtros)),
  crearReserva: (d) => request('POST', '/reservas', d),
  editarReserva: (id, d) => request('PUT', `/reservas/${id}`, d),
  confirmar: (id) => request('PATCH', `/reservas/${id}/confirmar`),
  cancelar: (id) => request('PATCH', `/reservas/${id}/cancelar`),
  atender: (id, d) => request('POST', `/reservas/${id}/atencion`, d),

  // Integración externa (Spring -> Node)
  medicamentos: (nombre) => request('GET', '/externo/medicamentos' + qs({ nombre })),
  clinica: () => request('GET', '/externo/clinica'),
  distancia: (lat, lng) => request('GET', '/externo/clinica/distancia' + qs({ lat, lng })),
};
