import { api } from './api.js';
import { $, esc, fechaCL, hoyISO, estadoBadge, filaVacia, toastError } from './ui.js';

export async function cargar() {
  try {
    const [clientes, mascotas, reservas] = await Promise.all([api.clientes(), api.mascotas(), api.reservas()]);
    const contar = (estado) => reservas.filter((r) => r.estado === estado).length;

    const kpis = [
      ['Clientes', clientes.length],
      ['Mascotas', mascotas.length],
      ['Horas pendientes', contar('PENDIENTE')],
      ['Horas confirmadas', contar('CONFIRMADA')],
      ['Atenciones realizadas', contar('ATENDIDA')],
    ];
    $('#kpis').innerHTML = kpis
      .map(([e, v]) => `<div class="kpi"><div class="valor">${v}</div><div class="etiqueta">${e}</div></div>`)
      .join('');

    const hoy = hoyISO();
    const proximas = reservas
      .filter((r) => (r.estado === 'PENDIENTE' || r.estado === 'CONFIRMADA') && r.fecha >= hoy)
      .slice(0, 8);

    $('#tabla-proximas').innerHTML = proximas.length
      ? proximas.map((r) => `<tr>
          <td class="nowrap">${fechaCL(r.fecha)}</td><td>${esc(r.hora)}</td>
          <td>${esc(r.mascotaNombre)} <span class="muted">${esc(r.mascotaEspecie)}</span></td>
          <td>${esc(r.clienteNombre)}</td><td>${esc(r.veterinarioNombre)}</td>
          <td>${estadoBadge(r.estado)}</td></tr>`).join('')
      : filaVacia(6, 'No hay horas próximas');
  } catch (e) {
    toastError(e);
  }
}
