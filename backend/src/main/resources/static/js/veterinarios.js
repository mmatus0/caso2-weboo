import { api } from './api.js';
import { $, esc, toast, toastError, leerForm, llenarForm, filaVacia, opciones, hoyISO } from './ui.js';

const form = () => $('#vet-form');
let cache = [];

export function init() {
  form().addEventListener('submit', guardar);
  $('#vet-cancelar').addEventListener('click', limpiar);
  $('#disp-fecha').value = hoyISO();
  $('#disp-vet').addEventListener('change', verDisponibilidad);
  $('#disp-fecha').addEventListener('change', verDisponibilidad);

  $('#tabla-vets').addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-accion]');
    if (!btn) return;
    const v = cache.find((x) => String(x.id) === btn.dataset.id);
    if (btn.dataset.accion === 'editar') editar(v);
    if (btn.dataset.accion === 'eliminar') eliminar(v);
  });
}

export async function cargar() {
  try {
    cache = await api.veterinarios();
    $('#tabla-vets').innerHTML = cache.length
      ? cache.map((v) => `<tr>
          <td><b>${esc(v.nombre)}</b></td><td>${esc(v.especialidad)}</td><td>${esc(v.email)}</td>
          <td>${v.activo ? '<span class="estado ATENDIDA">Activo</span>' : '<span class="estado CANCELADA">Inactivo</span>'}</td>
          <td><div class="acciones">
            <button class="btn mini sec" data-accion="editar" data-id="${v.id}">Editar</button>
            <button class="btn mini peligro" data-accion="eliminar" data-id="${v.id}">Eliminar</button>
          </div></td></tr>`).join('')
      : filaVacia(5);

    opciones($('#disp-vet'), cache.filter((v) => v.activo), (v) => v.id, (v) => v.nombre);
    verDisponibilidad();
  } catch (e) {
    toastError(e);
  }
}

async function verDisponibilidad() {
  const id = $('#disp-vet').value;
  const fecha = $('#disp-fecha').value;
  if (!id || !fecha) return;
  try {
    const d = await api.disponibilidad(id, fecha);
    const todos = [...d.bloquesLibres.map((h) => [h, true]), ...d.bloquesOcupados.map((h) => [h, false])]
      .sort((a, b) => a[0].localeCompare(b[0]));
    $('#disp-bloques').innerHTML = todos.length
      ? todos.map(([h, libre]) => `<span class="bloque ${libre ? '' : 'ocupado'}">${esc(h)}</span>`).join('')
      : '<span class="muted">No quedan bloques disponibles en esta fecha.</span>';
  } catch (e) {
    toastError(e);
  }
}

async function guardar(e) {
  e.preventDefault();
  const { id, ...datos } = leerForm(form());
  datos.activo = datos.activo !== 'false';
  try {
    if (id) {
      await api.editarVet(id, datos);
      toast('Veterinario actualizado', 'ok');
    } else {
      await api.crearVet(datos);
      toast('Veterinario registrado', 'ok');
    }
    limpiar();
    cargar();
  } catch (err) {
    toastError(err);
  }
}

function editar(v) {
  llenarForm(form(), { ...v, activo: String(v.activo) });
  $('#vet-form-titulo').textContent = `Editando a ${v.nombre}`;
}

async function eliminar(v) {
  if (!confirm(`¿Eliminar a ${v.nombre}?`)) return;
  try {
    await api.eliminarVet(v.id);
    toast('Veterinario eliminado', 'ok');
    cargar();
  } catch (err) {
    toastError(err);
  }
}

function limpiar() {
  form().reset();
  form().elements.id.value = '';
  $('#vet-form-titulo').textContent = 'Nuevo veterinario';
}
