import { api } from './api.js';
import {
  $, esc, fechaCL, hoyISO, toast, toastError, toastNotificacion, leerForm, filaVacia,
  opciones, estadoBadge, abrirModal, cerrarModal,
} from './ui.js';
import { verHistorial } from './mascotas.js';

const form = () => $('#reserva-form');
let cache = [];
let edicion = null; // reserva que se está modificando (para permitir su propio bloque)

export function init() {
  const f = form();
  f.elements.fecha.min = hoyISO();
  f.addEventListener('submit', guardar);
  $('#reserva-cancelar-edicion').addEventListener('click', limpiar);

  $('#reserva-cliente').addEventListener('change', () => cargarMascotasDe($('#reserva-cliente').value));
  f.elements.veterinarioId.addEventListener('change', cargarBloques);
  f.elements.fecha.addEventListener('change', cargarBloques);

  $('#reserva-bloques').addEventListener('click', (e) => {
    const b = e.target.closest('button.bloque:not(.ocupado)');
    if (!b) return;
    document.querySelectorAll('#reserva-bloques .bloque').forEach((x) => x.classList.remove('elegido'));
    b.classList.add('elegido');
    f.elements.hora.value = b.dataset.hora;
  });

  ['#filtro-fecha', '#filtro-estado', '#filtro-vet'].forEach((s) => $(s).addEventListener('change', cargarTabla));
  $('#filtro-limpiar').addEventListener('click', () => {
    $('#filtro-fecha').value = '';
    $('#filtro-estado').value = '';
    $('#filtro-vet').value = '';
    cargarTabla();
  });

  $('#tabla-reservas').addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-accion]');
    if (!btn) return;
    const r = cache.find((x) => String(x.id) === btn.dataset.id);
    ({ confirmar, cancelar, editar, atender, historial })[btn.dataset.accion](r);
  });
}

export async function cargar() {
  try {
    const [clientes, vets] = await Promise.all([api.clientes(), api.veterinarios()]);
    opciones($('#reserva-cliente'), clientes, (c) => c.id, (c) => c.nombre, 'Selecciona un cliente');
    opciones(form().elements.veterinarioId, vets.filter((v) => v.activo), (v) => v.id,
      (v) => `${v.nombre} · ${v.especialidad}`, 'Selecciona veterinario/a');
    opciones($('#filtro-vet'), vets, (v) => v.id, (v) => v.nombre, 'Todos');
    if ($('#reserva-cliente').value) await cargarMascotasDe($('#reserva-cliente').value);
    await cargarTabla();
  } catch (e) {
    toastError(e);
  }
}

async function cargarTabla() {
  try {
    cache = await api.reservas({
      fecha: $('#filtro-fecha').value,
      estado: $('#filtro-estado').value,
      veterinarioId: $('#filtro-vet').value,
    });
    $('#tabla-reservas').innerHTML = cache.length ? cache.map(fila).join('') : filaVacia(9);
  } catch (e) {
    toastError(e);
  }
}

function fila(r) {
  const b = (accion, texto, clase = 'sec') =>
    `<button class="btn mini ${clase}" data-accion="${accion}" data-id="${r.id}">${texto}</button>`;

  // Los botones dependen del estado (máquina de estados de la reserva)
  const botones = {
    PENDIENTE: b('confirmar', 'Confirmar', '') + b('editar', 'Modificar') + b('cancelar', 'Cancelar', 'peligro'),
    CONFIRMADA: b('atender', 'Atender', '') + b('editar', 'Modificar') + b('cancelar', 'Cancelar', 'peligro'),
    ATENDIDA: b('historial', 'Ver atención'),
    CANCELADA: '',
  }[r.estado];

  return `<tr>
    <td class="muted">${r.id}</td><td class="nowrap">${fechaCL(r.fecha)}</td><td><b>${esc(r.hora)}</b></td>
    <td>${esc(r.mascotaNombre)} <div class="muted">${esc(r.mascotaEspecie)}</div></td>
    <td>${esc(r.clienteNombre)}</td><td>${esc(r.veterinarioNombre)}</td>
    <td>${esc(r.motivo)}</td><td>${estadoBadge(r.estado)}</td>
    <td><div class="acciones">${botones}</div></td></tr>`;
}

async function cargarMascotasDe(clienteId) {
  const select = form().elements.mascotaId;
  if (!clienteId) {
    opciones(select, [], () => '', () => '', 'Primero elige un cliente');
    return;
  }
  const mascotas = await api.mascotasDeCliente(clienteId);
  opciones(select, mascotas, (m) => m.id, (m) => `${m.nombre} (${m.especie})`, 'Selecciona una mascota');
}

/** Pinta los bloques de 1 hora del veterinario en la fecha elegida. */
async function cargarBloques() {
  const f = form();
  const vetId = f.elements.veterinarioId.value;
  const fecha = f.elements.fecha.value;
  f.elements.hora.value = '';
  if (!vetId || !fecha) return;

  try {
    const d = await api.disponibilidad(vetId, fecha);
    const libres = new Set(d.bloquesLibres);

    // Al modificar, el bloque actual de la propia reserva también es elegible
    const propio = edicion && String(edicion.veterinarioId) === vetId && edicion.fecha === fecha ? edicion.hora : null;
    if (propio) libres.add(propio);

    const todos = [...new Set([...d.bloquesLibres, ...d.bloquesOcupados, ...(propio ? [propio] : [])])].sort();
    $('#reserva-bloques').innerHTML = todos.length
      ? todos.map((h) => `<button type="button" class="bloque ${libres.has(h) ? '' : 'ocupado'} ${h === propio ? 'elegido' : ''}"
            data-hora="${esc(h)}" ${libres.has(h) ? '' : 'disabled'}>${esc(h)}</button>`).join('')
      : '<span class="muted">No quedan bloques disponibles en esta fecha.</span>';
    if (propio) f.elements.hora.value = propio;
  } catch (e) {
    toastError(e);
  }
}

async function guardar(e) {
  e.preventDefault();
  const { id, ...datos } = leerForm(form());
  if (!datos.hora) {
    toast('Selecciona un bloque horario', 'aviso');
    return;
  }
  datos.mascotaId = Number(datos.mascotaId) || null;
  datos.veterinarioId = Number(datos.veterinarioId) || null;

  try {
    if (id) {
      const res = await api.editarReserva(id, datos);
      toast(`Reserva #${id} modificada`, 'ok');
      toastNotificacion(res.notificacion);
    } else {
      const r = await api.crearReserva(datos);
      toast(`Reserva #${r.id} creada como PENDIENTE`, 'ok');
    }
    limpiar();
    cargarTabla();
  } catch (err) {
    toastError(err);
    if (err.status === 409) cargarBloques(); // alguien tomó ese bloque: refrescar
  }
}

async function confirmar(r) {
  try {
    const res = await api.confirmar(r.id);
    toast(`Reserva #${r.id} CONFIRMADA`, 'ok');
    toastNotificacion(res.notificacion);
    cargarTabla();
  } catch (e) {
    toastError(e);
  }
}

async function cancelar(r) {
  if (!confirm(`¿Cancelar la hora de ${r.mascotaNombre} del ${fechaCL(r.fecha)} a las ${r.hora}?`)) return;
  try {
    const res = await api.cancelar(r.id);
    toast(`Reserva #${r.id} CANCELADA`, 'ok');
    toastNotificacion(res.notificacion);
    cargarTabla();
  } catch (e) {
    toastError(e);
  }
}

async function editar(r) {
  edicion = r;
  const f = form();
  $('#reserva-form-titulo').textContent = `Modificando reserva #${r.id}`;
  f.elements.id.value = r.id;
  $('#reserva-cliente').value = r.clienteId;
  await cargarMascotasDe(r.clienteId);
  f.elements.mascotaId.value = r.mascotaId;
  f.elements.veterinarioId.value = r.veterinarioId;
  f.elements.fecha.value = r.fecha;
  f.elements.motivo.value = r.motivo;
  await cargarBloques();
  f.scrollIntoView({ behavior: 'smooth' });
}

function historial(r) {
  verHistorial({ id: r.mascotaId, nombre: r.mascotaNombre });
}

function limpiar() {
  edicion = null;
  form().reset();
  form().elements.id.value = '';
  form().elements.hora.value = '';
  $('#reserva-form-titulo').textContent = 'Solicitar hora';
  $('#reserva-bloques').innerHTML = '<span class="muted">Selecciona veterinario y fecha.</span>';
}

// ------------------------------------------------------------------ atención médica

/** Formulario de atención con buscador de medicamentos (API externa). */
function atender(r) {
  abrirModal(`Atender a ${r.mascotaNombre} · Reserva #${r.id}`, `
    <form id="atencion-form" novalidate>
      <div class="grid-form">
        <label class="ancho">Diagnóstico <textarea name="diagnostico" required></textarea></label>
        <label class="ancho">Tratamiento <textarea name="tratamiento" required></textarea></label>
        <label class="ancho">Observaciones <input name="observaciones"></label>
      </div>

      <h3 style="margin:20px 0 8px">Medicamentos</h3>
      <div class="barra">
        <label style="flex:1">Buscar en catálogo externo <input id="med-buscar" placeholder="Ej: amoxi, meloxicam..."></label>
        <button type="button" class="btn sec" id="med-btn">Buscar</button>
      </div>
      <div id="med-resultados" class="med-resultados"></div>

      <p class="muted" style="margin:14px 0 6px">Receta:</p>
      <div id="recetas"><p class="muted" id="sin-recetas">Sin medicamentos agregados.</p></div>

      <div class="form-acciones">
        <button type="button" class="btn sec" id="atencion-cancelar">Cancelar</button>
        <button type="submit" class="btn">Registrar atención</button>
      </div>
    </form>`);

  const buscar = async () => {
    const nombre = $('#med-buscar').value.trim();
    if (nombre.length < 2) return toast('Escribe al menos 2 letras', 'aviso');
    try {
      const res = await api.medicamentos(nombre);
      $('#med-resultados').innerHTML = res.resultados.length
        ? res.resultados.map((m) => `
          <div class="med">
            <div>
              <b>${esc(m.nombre)}</b> <span class="muted">· ${esc(m.categoria)} · ${esc(m.especies.join(', '))}</span>
              <div>${esc(m.uso)}</div>
              <div class="advertencia">⚠ ${esc(m.advertencias)}</div>
              ${m.requiereReceta ? '<div class="muted">Requiere receta</div>' : ''}
            </div>
            <button type="button" class="btn mini" data-med="${esc(m.nombre)}">Agregar</button>
          </div>`).join('')
        : '<p class="muted">No se encontraron medicamentos con ese nombre.</p>';
    } catch (e) {
      toastError(e);
    }
  };

  $('#med-btn').addEventListener('click', buscar);
  $('#med-buscar').addEventListener('keydown', (e) => { if (e.key === 'Enter') { e.preventDefault(); buscar(); } });
  $('#med-resultados').addEventListener('click', (e) => {
    const b = e.target.closest('button[data-med]');
    if (b) agregarReceta(b.dataset.med);
  });
  $('#recetas').addEventListener('click', (e) => {
    if (e.target.closest('.quitar')) e.target.closest('.receta-fila').remove();
  });
  $('#atencion-cancelar').addEventListener('click', cerrarModal);
  $('#atencion-form').addEventListener('submit', (e) => registrarAtencion(e, r));
}

function agregarReceta(medicamento) {
  $('#sin-recetas')?.remove();
  const div = document.createElement('div');
  div.className = 'receta-fila';
  div.innerHTML = `
    <label>Medicamento <input name="medicamento" value="${esc(medicamento)}" readonly></label>
    <label>Dosis <input name="dosis" placeholder="1 comprimido"></label>
    <label>Frecuencia <input name="frecuencia" placeholder="Cada 12 horas"></label>
    <label>Días <input name="dias" type="number" min="1" value="5"></label>
    <button type="button" class="btn mini peligro quitar" title="Quitar">×</button>`;
  $('#recetas').appendChild(div);
}

async function registrarAtencion(e, r) {
  e.preventDefault();
  const f = e.target;
  const recetas = [...f.querySelectorAll('.receta-fila')].map((fila) => ({
    medicamento: fila.querySelector('[name=medicamento]').value,
    dosis: fila.querySelector('[name=dosis]').value.trim(),
    frecuencia: fila.querySelector('[name=frecuencia]').value.trim(),
    dias: Number(fila.querySelector('[name=dias]').value),
  }));

  try {
    await api.atender(r.id, {
      diagnostico: f.elements.diagnostico.value.trim(),
      tratamiento: f.elements.tratamiento.value.trim(),
      observaciones: f.elements.observaciones.value.trim() || null,
      recetas,
    });
    cerrarModal();
    toast(`Atención registrada. Reserva #${r.id} ATENDIDA`, 'ok');
    cargarTabla();
  } catch (err) {
    toastError(err);
  }
}
