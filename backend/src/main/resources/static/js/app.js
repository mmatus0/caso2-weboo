// Punto de entrada: navegación entre secciones.
import { $, cerrarModal } from './ui.js';
import * as inicio from './inicio.js';
import * as clientes from './clientes.js';
import * as mascotas from './mascotas.js';
import * as veterinarios from './veterinarios.js';
import * as reservas from './reservas.js';
import * as ubicacion from './ubicacion.js';

const secciones = { inicio, clientes, mascotas, veterinarios, reservas, ubicacion };

function mostrar(nombre) {
  document.querySelectorAll('.seccion').forEach((s) => s.classList.toggle('activa', s.id === nombre));
  document.querySelectorAll('nav button').forEach((b) => b.classList.toggle('activo', b.dataset.seccion === nombre));
  secciones[nombre].cargar();
  history.replaceState(null, '', '#' + nombre);
}

document.querySelectorAll('nav button').forEach((b) =>
  b.addEventListener('click', () => mostrar(b.dataset.seccion))
);

$('#modal-cerrar').addEventListener('click', cerrarModal);
$('#modal').addEventListener('click', (e) => { if (e.target.id === 'modal') cerrarModal(); });
document.addEventListener('keydown', (e) => { if (e.key === 'Escape') cerrarModal(); });

Object.values(secciones).forEach((s) => s.init?.());

const inicial = location.hash.slice(1);
mostrar(secciones[inicial] ? inicial : 'inicio');
