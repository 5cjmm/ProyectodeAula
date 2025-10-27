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

        // Ensamblar tarjeta
        card.appendChild(header);
        card.appendChild(priceEl);
        card.appendChild(trendContainer);

        listDiv.appendChild(card);
      }


      // prepare and render chart using Chart.js
      renderChart(histData, preds);

    } catch (error) {
      alert('Ocurrió un error: ' + error.message);
    }
  });
});

let priceChart = null;
function renderChart(historyMap, predictionsMap) {
  // historyMap and predictionsMap are objects mapping year->price
  const yearsSet = new Set();
  for (const y of Object.keys(historyMap || {})) yearsSet.add(Number(y));
  for (const y of Object.keys(predictionsMap || {})) yearsSet.add(Number(y));
  const years = Array.from(yearsSet).sort((a, b) => a - b);

  const historyValues = years.map(y => (historyMap && historyMap[y] != null) ? Number(historyMap[y]) : null);
  const predictedValues = years.map(y => (predictionsMap && predictionsMap[y] != null) ? Number(predictionsMap[y]) : null);

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
