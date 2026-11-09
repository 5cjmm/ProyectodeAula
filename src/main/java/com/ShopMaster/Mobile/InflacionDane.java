package com.ShopMaster.Mobile;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Inflación anual (variación del IPC de diciembre a diciembre) publicada por el DANE.
 * Son cifras históricas ya cerradas, por eso viven en el código y no se consultan al DANE
 * en cada petición. Para agregar un año nuevo basta con una línea.
 *
 * Fuentes (consultadas en noviembre de 2026):
 *  - Nacional 2015-2025: DANE, serie anual citada por Gerencie.com
 *    (https://www.gerencie.com/ipc-historico.html). 2025 coincide con los reportes de prensa
 *    del cierre de año (5,10 %).
 *  - Cartagena 2020: El Universal, "IPC de Cartagena en el 2020: 0,35 %".
 *  - Cartagena 2021: El Universal (7-ene-2022), "Cartagena, la sexta ciudad con más bajo IPC": 5,75 %.
 *  - Cartagena 2022: El Universal, "Inflación 2022, la más alta en 23 años": 14,33 %.
 *  - Cartagena 2023: El Universal, "4 ciudades del Caribe únicas con IPC de dos dígitos": 9,91 %.
 *  - Cartagena 2024: infografía de Cartagena Cómo Vamos (enero 2025): 4,57 %.
 *  - Cartagena 2025: El Universal (10-ene-2026): 4,80 %.
 *
 * No hay cifras de Cartagena para 2015-2019: no se encontró una fuente confiable y no se
 * inventan. La app las muestra como "sin dato".
 */
final class InflacionDane {

    static final String FUENTE = "DANE · IPC, variación anual de diciembre a diciembre";

    static final SortedMap<Integer, Double> NACIONAL;
    static final SortedMap<Integer, Double> CARTAGENA;

    static {
        TreeMap<Integer, Double> nacional = new TreeMap<>();
        nacional.put(2015, 6.77);
        nacional.put(2016, 5.75);
        nacional.put(2017, 4.09);
        nacional.put(2018, 3.18);
        nacional.put(2019, 3.80);
        nacional.put(2020, 1.61);
        nacional.put(2021, 5.62);
        nacional.put(2022, 13.12);
        nacional.put(2023, 9.28);
        nacional.put(2024, 5.20);
        nacional.put(2025, 5.10);
        NACIONAL = Collections.unmodifiableSortedMap(nacional);

        TreeMap<Integer, Double> cartagena = new TreeMap<>();
        cartagena.put(2020, 0.35);
        cartagena.put(2021, 5.75);
        cartagena.put(2022, 14.33);
        cartagena.put(2023, 9.91);
        cartagena.put(2024, 4.57);
        cartagena.put(2025, 4.80);
        CARTAGENA = Collections.unmodifiableSortedMap(cartagena);
    }

    private InflacionDane() {
    }

    /** Último valor publicado en la serie, para usarlo como referencia de años futuros. */
    static Double ultimo(SortedMap<Integer, Double> serie) {
        return serie.isEmpty() ? null : serie.get(serie.lastKey());
    }

    static Integer ultimoAnio(SortedMap<Integer, Double> serie) {
        return serie.isEmpty() ? null : serie.lastKey();
    }
}
