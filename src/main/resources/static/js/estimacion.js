// Estimación de precio con la red neuronal entrenada con el dataset de Kaggle.
// GET /api/predict/estimar/opciones (selectores + ficha del modelo) y POST /api/predict/estimar.
document.addEventListener('DOMContentLoaded', async function () {
  const form = document.getElementById('estimarForm');
  if (!form) return;

  const esc = t => String(t == null ? '' : t).replace(/[&<>"']/g,
    c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const cop = n => '$' + Math.round(n).toLocaleString('es-CO');

  const subSel = document.getElementById('estSubcategoria');
  const marcaSel = document.getElementById('estMarca');
  const ventaSel = document.getElementById('estUnidadVenta');
  const medidaSel = document.getElementById('estUnidadMedida');
  const cantidadInp = document.getElementById('estCantidad');
  const resultado = document.getElementById('estResultado');
  const fichaBox = document.getElementById('estModelo');

  function llenar(select, items, valor, texto) {
    select.innerHTML = '';
    items.forEach(it => {
      const o = document.createElement('option');
      o.value = valor ? valor(it) : it;
      o.textContent = texto ? texto(it) : it;
      select.appendChild(o);
    });
  }

  function renderFicha(model) {
    if (!model || !model.metricas) { fichaBox.innerHTML = ''; return; }
    const m = model.metricas;
    const tarjeta = (titulo, valor, nota) => `
      <div class="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
        <p class="text-xs font-medium text-gray-500">${titulo}</p>
        <p class="text-xl font-extrabold text-gray-900 mt-1 break-words">${valor}</p>
        <p class="text-xs text-gray-400 mt-1">${nota}</p>
      </div>`;
    const filas = (model.comparativa || []).map(c => {
      const en = c.modelo === model.modelo;
      return `<tr class="${en ? 'bg-blue-50 font-semibold' : ''} border-t border-gray-100">
        <td class="py-2 pr-3">${esc(c.modelo)}${en ? ' <span class="text-xs text-blue-600">(en uso)</span>' : ''}</td>
        <td class="py-2 px-3 text-right">${Number(c.porcentajePredictivo).toFixed(1)} %</td>
        <td class="py-2 px-3 text-right">${Number(c.r2).toFixed(3)}</td>
        <td class="py-2 pl-3 text-right">${Number(c.errorMedianoPct).toFixed(1)} %</td>
      </tr>`;
    }).join('');
    fichaBox.innerHTML = `
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        ${tarjeta('Variable predictiva', esc(model.variablePredictiva), 'Lo que el modelo estima (COP)')}
        ${tarjeta('Variables predictoras', (model.variablesPredictoras || []).map(esc).join(', '), 'Datos con los que estima')}
        ${tarjeta('Porcentaje predictivo', Number(m.porcentajePredictivo).toFixed(1) + ' %', 'R² en productos que nunca vio')}
        ${tarjeta('Error típico', Number(m.errorMedianoPct).toFixed(1) + ' %', 'Mediana del error porcentual')}
      </div>
      <p class="text-xs text-gray-500 mt-3">
        <strong>Modelo:</strong> ${esc(model.modelo)} (${esc(model.arquitectura)}) ·
        <strong>Validación:</strong> ${esc(model.validacion)} · <strong>Datos:</strong> ${esc(model.dataset)}
      </p>
      <h4 class="text-sm font-semibold text-gray-700 mt-5 mb-2">Comparación de modelos</h4>
      <div class="overflow-x-auto">
        <table class="w-full text-sm text-gray-700">
          <thead><tr class="text-xs text-gray-500">
            <th class="text-left pb-2 pr-3">Modelo</th><th class="text-right pb-2 px-3">Porcentaje predictivo</th>
            <th class="text-right pb-2 px-3">R²</th><th class="text-right pb-2 pl-3">Error típico</th>
          </tr></thead><tbody>${filas}</tbody>
        </table>
      </div>`;
  }

  try {
    const resp = await fetch('/api/predict/estimar/opciones');
    if (!resp.ok) throw new Error('opciones');
    const data = await resp.json();
    const op = data.opciones || {};
    llenar(subSel, op.subcategorias || []);
    llenar(marcaSel, ['OTRA'].concat(op.marcas || []), null, v => v === 'OTRA' ? 'Otra / no listada' : v);
    llenar(ventaSel, op.unidadesVenta || []);
    llenar(medidaSel, op.unidadesMedida || [], u => u.valor, u => u.nombre);
    renderFicha(data.modelo);
  } catch (e) {
    fichaBox.innerHTML = '<p class="text-sm text-red-600">No se pudo cargar el modelo de estimación.</p>';
    return;
  }

  // Sin unidad de medida no tiene sentido pedir cantidad
  function sincronizar() {
    const sin = medidaSel.value === 'ninguna';
    cantidadInp.disabled = sin;
    if (sin) cantidadInp.value = '';
  }
  medidaSel.addEventListener('change', sincronizar);
  sincronizar();

  form.addEventListener('submit', async function (e) {
    e.preventDefault();
    const cantidad = cantidadInp.value ? parseFloat(cantidadInp.value) : null;
    const body = {
      subcategoria: subSel.value, marca: marcaSel.value, unidadVenta: ventaSel.value,
      cantidad: cantidad, unidadMedida: medidaSel.value
    };
    resultado.classList.remove('hidden');
    resultado.innerHTML = '<p class="text-sm text-gray-500">Calculando…</p>';
    try {
      const r = await fetch('/api/predict/estimar', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
      });
      if (!r.ok) {
        let msg = 'No se pudo estimar el precio';
        try { const j = await r.json(); if (j.message) msg = j.message; } catch (_) {}
        throw new Error(msg);
      }
      const d = await r.json();
      resultado.innerHTML = `
        <p class="text-sm text-gray-500">Precio estimado</p>
        <p class="text-4xl font-extrabold text-gray-900 tracking-tight">${cop(d.precioEstimado)} <span class="text-lg font-semibold text-gray-500">COP</span></p>
        <p class="text-sm text-gray-600 mt-2">Rango típico: <strong>${cop(d.minimo)} – ${cop(d.maximo)}</strong>
          <span class="text-gray-400">(entre el 10 % y el 90 % de los casos de validación)</span></p>
        <p class="text-xs text-gray-400 mt-2">Es un precio de referencia estimado con precios de supermercado de 2019-2020, no un precio de venta definitivo.</p>`;
      if (d.modelo) renderFicha(d.modelo);
    } catch (err) {
      resultado.innerHTML = `<p class="text-sm text-red-600">${esc(err.message)}</p>`;
    }
  });
});
