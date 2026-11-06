import { api } from './api.js';
import { $, esc, toast, toastError, leerForm, llenarForm, filaVacia, abrirModal } from './ui.js';

const form = () => $('#cliente-form');
let temporizador;

export function init() {
  form().addEventListener('submit', guardar);
  $('#cliente-cancelar').addEventListener('click', limpiar);

  $('#cliente-buscar').addEventListener('input', () => {
    clearTimeout(temporizador);
    temporizador = setTimeout(cargar, 300);
  });

  $('#tabla-clientes').addEventListener('click', async (e) => {
    const btn = e.target.closest('button[data-accion]');
    if (!btn) return;
    const id = btn.dataset.id;
    if (btn.dataset.accion === 'editar') editar(id);
    if (btn.dataset.accion === 'mascotas') verMascotas(id, btn.dataset.nombre);
    if (btn.dataset.accion === 'eliminar') eliminar(id, btn.dataset.nombre);
  });
}

export async function cargar() {
  try {
    const lista = await api.clientes($('#cliente-buscar').value.trim());
    $('#tabla-clientes').innerHTML = lista.length
      ? lista.map((c) => `<tr>
          <td>${esc(c.rut)}</td>
          <td><b>${esc(c.nombre)}</b><div class="muted">${esc(c.direccion)}</div></td>
          <td>${esc(c.email)}</td>
          <td>${esc(c.telefono)}</td>
          <td><div class="acciones">
            <button class="btn mini sec" data-accion="mascotas" data-id="${c.id}" data-nombre="${esc(c.nombre)}">Mascotas</button>
            <button class="btn mini sec" data-accion="editar" data-id="${c.id}">Editar</button>
            <button class="btn mini peligro" data-accion="eliminar" data-id="${c.id}" data-nombre="${esc(c.nombre)}">Eliminar</button>
          </div></td></tr>`).join('')
      : filaVacia(5);
  } catch (e) {
    toastError(e);
  }
}

async function guardar(e) {
  e.preventDefault();
  const { id, ...datos } = leerForm(form());
  try {
    if (id) {
      await api.editarCliente(id, datos);
      toast('Cliente actualizado', 'ok');
    } else {
      await api.crearCliente(datos);
      toast('Cliente registrado', 'ok');
    }
    limpiar();
    cargar();
  } catch (err) {
    toastError(err);
  }
}

async function editar(id) {
  try {
    const c = await api.cliente(id);
    llenarForm(form(), c);
    $('#cliente-form-titulo').textContent = `Editando a ${c.nombre}`;
    form().scrollIntoView({ behavior: 'smooth' });
  } catch (err) {
    toastError(err);
  }
}

async function eliminar(id, nombre) {
  if (!confirm(`¿Eliminar a ${nombre}? También se eliminarán sus mascotas y su historial.`)) return;
  try {
    await api.eliminarCliente(id);
    toast('Cliente eliminado', 'ok');
    cargar();
  } catch (err) {
    toastError(err);
  }
}

async function verMascotas(id, nombre) {
  try {
    const lista = await api.mascotasDeCliente(id);
    const html = lista.length
      ? `<table><thead><tr><th>Nombre</th><th>Especie</th><th>Raza</th><th>Edad</th><th>Peso</th></tr></thead><tbody>
          ${lista.map((m) => `<tr><td><b>${esc(m.nombre)}</b></td><td>${esc(m.especie)}</td><td>${esc(m.raza)}</td>
            <td>${esc(m.edad ?? '—')}</td><td>${m.peso ? esc(m.peso) + ' kg' : '—'}</td></tr>`).join('')}
         </tbody></table>`
      : '<p class="muted">Este cliente aún no tiene mascotas registradas.</p>';
    abrirModal(`Mascotas de ${nombre}`, html);
  } catch (err) {
    toastError(err);
  }
}

function limpiar() {
  form().reset();
  form().elements.id.value = '';
  $('#cliente-form-titulo').textContent = 'Nuevo cliente';
}
