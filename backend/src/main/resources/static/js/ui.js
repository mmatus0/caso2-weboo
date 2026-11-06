// Utilidades de interfaz compartidas por todas las secciones.

export const $ = (sel, raiz = document) => raiz.querySelector(sel);

/** Escapa texto para insertarlo en HTML de forma segura. */
export const esc = (v) =>
  String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

export const fechaCL = (iso) => (iso ? iso.slice(0, 10).split('-').reverse().join('-') : '—');

export const hoyISO = () => {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 10);
};

export function toast(mensaje, tipo = '', ms = 4500) {
  const el = document.createElement('div');
  el.className = `toast ${tipo}`;
  el.innerHTML = mensaje;
  $('#toasts').appendChild(el);
  setTimeout(() => el.remove(), ms);
}

/** Muestra el error del backend, incluyendo el detalle de cada campo inválido. */
export function toastError(err) {
  const detalles = err.detalles?.length ? '<br>• ' + err.detalles.map(esc).join('<br>• ') : '';
  toast(esc(err.message) + detalles, 'error', 7000);
}

/** Muestra el resultado de la notificación por correo que devuelve Spring. */
export function toastNotificacion(n) {
  if (!n) return;
  if (n.enviada) {
    const link = n.previewUrl ? ` · <a href="${esc(n.previewUrl)}" target="_blank" rel="noopener">ver correo</a>` : '';
    toast(`✉️ ${esc(n.detalle)}${link}`, 'ok', 9000);
  } else {
    toast(`⚠️ La reserva se actualizó, pero el correo no se envió: ${esc(n.detalle)}`, 'aviso', 9000);
  }
}

export function abrirModal(titulo, html) {
  $('#modal-titulo').textContent = titulo;
  $('#modal-cuerpo').innerHTML = html;
  $('#modal').classList.add('abierto');
}

export function cerrarModal() {
  $('#modal').classList.remove('abierto');
  $('#modal-cuerpo').innerHTML = '';
}

/** Lee un formulario como objeto; los campos vacíos quedan en null. */
export function leerForm(form) {
  const datos = {};
  new FormData(form).forEach((v, k) => {
    datos[k] = typeof v === 'string' && v.trim() === '' ? null : v;
  });
  return datos;
}

export function llenarForm(form, datos) {
  Object.entries(datos).forEach(([k, v]) => {
    const campo = form.elements.namedItem(k);
    if (campo) campo.value = v ?? '';
  });
}

export function opciones(select, items, valor, texto, placeholder) {
  const previo = select.value;
  select.innerHTML =
    (placeholder ? `<option value="">${esc(placeholder)}</option>` : '') +
    items.map((i) => `<option value="${esc(valor(i))}">${esc(texto(i))}</option>`).join('');
  if ([...select.options].some((o) => o.value === previo)) select.value = previo;
}

export const estadoBadge = (e) => `<span class="estado ${esc(e)}">${esc(e)}</span>`;

export const filaVacia = (cols, texto = 'Sin registros') =>
  `<tr><td colspan="${cols}" class="vacio">${esc(texto)}</td></tr>`;
