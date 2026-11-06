import { api } from './api.js';
import { $, esc, fechaCL, toast, toastError, leerForm, llenarForm, filaVacia, opciones, abrirModal } from './ui.js';

const form = () => $('#mascota-form');
let cache = [];

export function init() {
  form().addEventListener('submit', guardar);
  $('#mascota-cancelar').addEventListener('click', limpiar);
  $('#mascota-filtro-especie').addEventListener('change', cargar);

  $('#tabla-mascotas').addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-accion]');
    if (!btn) return;
    const m = cache.find((x) => String(x.id) === btn.dataset.id);
    if (btn.dataset.accion === 'editar') editar(m);
    if (btn.dataset.accion === 'historial') verHistorial(m);
    if (btn.dataset.accion === 'eliminar') eliminar(m);
  });
}

export async function cargar() {
  try {
    const [clientes, mascotas] = await Promise.all([api.clientes(), api.mascotas($('#mascota-filtro-especie').value)]);
    opciones(form().elements.clienteId, clientes, (c) => c.id, (c) => `${c.nombre} (${c.rut})`, 'Selecciona un dueño');
    cache = mascotas;

    $('#tabla-mascotas').innerHTML = mascotas.length
      ? mascotas.map((m) => `<tr>
          <td><b>${esc(m.nombre)}</b></td><td>${esc(m.especie)}</td><td>${esc(m.raza ?? '—')}</td>
          <td>${esc(m.edad ?? '—')}</td><td>${m.peso ? esc(m.peso) + ' kg' : '—'}</td>
          <td>${esc(m.clienteNombre)}</td>
          <td><div class="acciones">
            <button class="btn mini" data-accion="historial" data-id="${m.id}">Historial</button>
            <button class="btn mini sec" data-accion="editar" data-id="${m.id}">Editar</button>
            <button class="btn mini peligro" data-accion="eliminar" data-id="${m.id}">Eliminar</button>
          </div></td></tr>`).join('')
      : filaVacia(7);
  } catch (e) {
    toastError(e);
  }
}

async function guardar(e) {
  e.preventDefault();
  const { id, ...datos } = leerForm(form());
  try {
    if (id) {
      await api.editarMascota(id, datos);
      toast('Mascota actualizada', 'ok');
    } else {
      await api.crearMascota(datos);
      toast('Mascota registrada', 'ok');
    }
    limpiar();
    cargar();
  } catch (err) {
    toastError(err);
  }
}

function editar(m) {
  llenarForm(form(), m);
  $('#mascota-form-titulo').textContent = `Editando a ${m.nombre}`;
  form().scrollIntoView({ behavior: 'smooth' });
}

async function eliminar(m) {
  if (!confirm(`¿Eliminar a ${m.nombre}? Se borrará también su historial médico.`)) return;
  try {
    await api.eliminarMascota(m.id);
    toast('Mascota eliminada', 'ok');
    cargar();
  } catch (err) {
    toastError(err);
  }
}

/** Historial médico: atenciones con diagnóstico, tratamiento y recetas. */
export async function verHistorial(m) {
  try {
    const atenciones = await api.historial(m.id);
    const html = atenciones.length
      ? atenciones.map((a) => `
        <div class="atencion">
          <div class="fecha">${fechaCL(a.fecha)} · ${esc(a.veterinarioNombre)} · Reserva #${a.reservaId}</div>
          <p><b>Motivo:</b> ${esc(a.motivo)}</p>
          <p><b>Diagnóstico:</b> ${esc(a.diagnostico)}</p>
          <p><b>Tratamiento:</b> ${esc(a.tratamiento)}</p>
          ${a.observaciones ? `<p><b>Observaciones:</b> ${esc(a.observaciones)}</p>` : ''}
          <div>${a.recetas.map((r) =>
            `<span class="receta-chip">💊 ${esc(r.medicamento)} · ${esc(r.dosis)} · ${esc(r.frecuencia)} · ${r.dias} días</span>`).join('')
            || '<span class="muted">Sin medicamentos recetados</span>'}</div>
        </div>`).join('')
      : '<p class="muted">Esta mascota aún no registra atenciones.</p>';
    abrirModal(`Historial médico de ${m.nombre ?? m.mascotaNombre}`, html);
  } catch (err) {
    toastError(err);
  }
}

function limpiar() {
  form().reset();
  form().elements.id.value = '';
  $('#mascota-form-titulo').textContent = 'Nueva mascota';
}
