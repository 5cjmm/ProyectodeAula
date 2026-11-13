document.addEventListener('DOMContentLoaded', async function () {
  const productSelect = document.getElementById('productSelect');
  const startYearInput = document.getElementById('startYear');
  const yearsInput = document.getElementById('years');

  // populate products
  try {
    const resp = await fetch('/api/predict/price/products');
    if (resp.ok) {
      const prods = await resp.json();
      prods.forEach(p => {
        const opt = document.createElement('option');
        opt.value = p;
        opt.textContent = p;
        productSelect.appendChild(opt);
      });
    }
  } catch (e) {
    console.warn('No se pudo cargar la lista de productos:', e);
  }

  document.getElementById('priceForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const product = productSelect.value;
    const startYear = startYearInput.value ? parseInt(startYearInput.value, 10) : null;
    const years = yearsInput.value ? parseInt(yearsInput.value, 10) : 3;

    const payload = { product, startYear, years };

    try {
      const [predResp, histResp] = await Promise.all([
        fetch('/api/predict/price', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) }),
        fetch('/api/predict/price/history?product=' + encodeURIComponent(product))
      ]);

      if (!predResp.ok) throw new Error('Error al obtener la predicción');
      if (!histResp.ok) throw new Error('Error al obtener el historial');

      const predData = await predResp.json();
      const histData = await histResp.json();

      document.getElementById('result').style.display = 'block';
      const listDiv = document.getElementById('predictionsList');
      listDiv.innerHTML = '';


      // display list of predictions como tarjetas minimalistas con indicador de tendencia y COP al lado
      // Mostrar predicciones en tarjetas tipo dashboard financiero
      const preds = predData.predictions || predData;
      const intervals = predData.intervals || null;
      renderModelo(predData.model);
      listDiv.className = "mt-6 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5";

      const yearsArray = Object.entries(preds);
      for (let i = 0; i < yearsArray.length; i++) {
        const [year, price] = yearsArray[i];
        const prevPrice = i > 0 ? yearsArray[i - 1][1] : null;

        // Tarjeta base
        const card = document.createElement('div');
        card.className =
          "bg-white border border-gray-200 shadow-sm rounded-2xl p-5 flex flex-col justify-between hover:shadow-lg transition";

        // Encabezado: año + icono
        const header = document.createElement('div');
        header.className = "flex justify-between items-center mb-2";

        const yearEl = document.createElement('h4');
        yearEl.textContent = year;
        yearEl.className = "text-sm font-semibold text-gray-600";

        const icon = document.createElement('div');
        icon.innerHTML = `
        <svg class="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-13a1 1 0 10-2 0v.092a4.535 4.535 0 00-1.676.662C6.602 6.234 6 7.009 6 8c0 .99.602 1.765 1.324 2.246.48.32 1.054.545 1.676.662v1.941c-.391-.127-.68-.317-.843-.504a1 1 0 10-1.51 1.31c.562.649 1.413 1.076 2.353 1.253V15a1 1 0 102 0v-.092a4.535 4.535 0 001.676-.662C13.398 13.766 14 12.991 14 12c0-.99-.602-1.765-1.324-2.246A4.535 4.535 0 0011 9.092V7.151c.391.127.68.317.843.504a1 1 0 101.511-1.31c-.563-.649-1.413-1.076-2.354-1.253V5z"></path>
        </svg>
      `;

        header.appendChild(yearEl);
        header.appendChild(icon);

        // Cuerpo: precio formateado en COP
        const priceEl = document.createElement('p');
        priceEl.textContent = isFinite(price)
          ? `$${Math.round(price).toLocaleString('es-CO')} COP`
          : 'N/A';
        priceEl.className =
          "text-2xl font-extrabold text-gray-900 tracking-tight mb-1";

        // Pie: tendencia (sube o baja)
        const trendContainer = document.createElement('div');
        trendContainer.className = "flex items-center gap-1 text-sm font-medium";

        if (prevPrice !== null && isFinite(prevPrice) && isFinite(price)) {
          const diff = price - prevPrice;
          const percent = ((diff / prevPrice) * 100).toFixed(1);

          if (diff > 0) {
            trendContainer.innerHTML = `
          <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-green-500" viewBox="0 0 20 20" fill="currentColor">
            <path d="M5 10l4-4 4 4m-4-4v8"/>
          </svg>
          <span class="text-green-600">+${percent}% vs año anterior</span>
        `;
          } else if (diff < 0) {
            trendContainer.innerHTML = `
          <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-red-500" viewBox="0 0 20 20" fill="currentColor">
            <path d="M5 10l4 4 4-4m-4 4V6"/>
          </svg>
          <span class="text-red-600">${percent}% vs año anterior</span>
        `;
          } else {
            trendContainer.innerHTML = `
          <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
            <path d="M4 10h12"/>
          </svg>
          <span class="text-gray-500">Sin cambio</span>
        `;
          }
        }

        // Intervalo de confianza del 95 % (si el modelo lo calcula)
        const rango = intervals && intervals[year];
        const rangeEl = document.createElement('p');
        if (rango) {
          rangeEl.className = "text-xs text-gray-500 mt-2";
          rangeEl.textContent = `Rango 95 %: $${Math.round(rango[0]).toLocaleString('es-CO')} – $${Math.round(rango[1]).toLocaleString('es-CO')}`;
        }

        // Ensamblar tarjeta
        card.appendChild(header);
        card.appendChild(priceEl);
        card.appendChild(trendContainer);
        if (rango) card.appendChild(rangeEl);

        listDiv.appendChild(card);
      }


      // prepare and render chart using Chart.js
      renderChart(histData, preds, intervals);

    } catch (error) {
      alert('Ocurrió un error: ' + error.message);
    }
  });
});

let priceChart = null;
function renderChart(historyMap, predictionsMap, intervals) {
  // historyMap and predictionsMap are objects mapping year->price
  const yearsSet = new Set();
  for (const y of Object.keys(historyMap || {})) yearsSet.add(Number(y));
  for (const y of Object.keys(predictionsMap || {})) yearsSet.add(Number(y));
  const years = Array.from(yearsSet).sort((a, b) => a - b);

  const historyValues = years.map(y => (historyMap && historyMap[y] != null) ? Number(historyMap[y]) : null);
  const predictedValues = years.map(y => (predictionsMap && predictionsMap[y] != null) ? Number(predictionsMap[y]) : null);

  const minValues = years.map(y => (intervals && intervals[y]) ? Number(intervals[y][0]) : null);
  const maxValues = years.map(y => (intervals && intervals[y]) ? Number(intervals[y][1]) : null);
  const ctx = document.getElementById('priceChart').getContext('2d');
  const data = {
    labels: years.map(String),
    datasets: [
      {
        label: 'Histórico',
        data: historyValues,
        borderColor: 'rgba(54, 162, 235, 1)',
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        spanGaps: true,
        tension: 0.2
      },
      {
        label: 'Predicción',
        data: predictedValues,
        borderColor: 'rgba(75, 192, 192, 1)',
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        borderDash: [6, 4],
        spanGaps: true,
        tension: 0.2
      }
    ]
  };

  // Banda del intervalo de confianza del 95 %: el máximo se rellena hasta el mínimo
  if (intervals) {
    data.datasets.push(
      { label: 'Mínimo (95 %)', data: minValues, borderWidth: 0, pointRadius: 0, fill: false, spanGaps: true },
      { label: 'Máximo (95 %)', data: maxValues, borderWidth: 0, pointRadius: 0, fill: '-1',
        backgroundColor: 'rgba(75, 192, 192, 0.18)', spanGaps: true }
    );
  }

  const config = {
    type: 'line',
    data: data,
    options: {
      responsive: true,
      scales: {
        y: { beginAtZero: false }
      }
    }
  };

  if (priceChart) {
    priceChart.data = data;
    priceChart.update();
  } else {
    priceChart = new Chart(ctx, config);
  }
}


function escapeHtml(t) {
  return String(t == null ? '' : t).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

// Ficha del modelo: variable predictiva, predictoras, porcentaje predictivo y comparación de modelos
function renderModelo(model) {
  const box = document.getElementById('modeloInfo');
  if (!box) return;
  if (!model || !model.metricas) { box.innerHTML = ''; return; }

  const m = model.metricas;
  const pct = Number(m.porcentajePredictivo);
  const r2 = Number(m.r2);
  const predictoras = (model.variablesPredictoras || []).map(escapeHtml).join(', ');

  const tarjeta = (titulo, valor, nota) => `
    <div class="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
      <p class="text-xs font-medium text-gray-500">${titulo}</p>
      <p class="text-2xl font-extrabold text-gray-900 mt-1">${valor}</p>
      <p class="text-xs text-gray-400 mt-1">${nota}</p>
    </div>`;

  let tabla = '';
  if (Array.isArray(model.comparativa) && model.comparativa.length) {
    const filas = model.comparativa.map(c => {
      const elegido = c.modelo === model.modelo;
      return `<tr class="${elegido ? 'bg-blue-50 font-semibold' : ''} border-t border-gray-100">
        <td class="py-2 pr-3">${escapeHtml(c.modelo)}${elegido ? ' <span class="text-xs text-blue-600">(en uso)</span>' : ''}</td>
        <td class="py-2 px-3 text-right">${Number(c.porcentajePredictivo).toFixed(2)} %</td>
        <td class="py-2 px-3 text-right">${Number(c.r2).toFixed(3)}</td>
        <td class="py-2 px-3 text-right">${Number(c.mapePct).toFixed(2)} %</td>
        <td class="py-2 pl-3 text-right">$${Math.round(c.mae).toLocaleString('es-CO')}</td>
      </tr>`;
    }).join('');
    tabla = `
      <h4 class="text-sm font-semibold text-gray-700 mt-6 mb-2">Comparación de modelos (validación temporal)</h4>
      <div class="overflow-x-auto">
        <table class="w-full text-sm text-gray-700">
          <thead><tr class="text-xs text-gray-500">
            <th class="text-left pb-2 pr-3">Modelo</th><th class="text-right pb-2 px-3">Porcentaje predictivo</th>
            <th class="text-right pb-2 px-3">R²</th><th class="text-right pb-2 px-3">Error (MAPE)</th><th class="text-right pb-2 pl-3">Error medio (MAE)</th>
          </tr></thead>
          <tbody>${filas}</tbody>
        </table>
      </div>`;
  }

  box.innerHTML = `
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      ${tarjeta('Variable predictiva', escapeHtml(model.variablePredictiva || 'precio'), 'Lo que el modelo pronostica (COP)')}
      ${tarjeta('Variables predictoras', predictoras || '—', 'Datos con los que se predice')}
      ${tarjeta('Porcentaje predictivo', isFinite(pct) ? pct.toFixed(2) + ' %' : '—', 'Precisión = 100 − error medio (MAPE)')}
      ${tarjeta('Coeficiente R²', isFinite(r2) ? r2.toFixed(3) : '—', 'Variación de precios explicada')}
    </div>
    <p class="text-xs text-gray-500 mt-3">
      <strong>Modelo:</strong> ${escapeHtml(model.modelo)} · <strong>Validación:</strong> ${escapeHtml(model.validacion)} ·
      <strong>Datos:</strong> ${escapeHtml(model.dataset)}
    </p>
    ${tabla}`;
}
