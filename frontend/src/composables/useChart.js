// Import modular chart functions
import { drawTimeSeriesChart } from './charts/timeSeriesChart'
import { drawInteractiveTimeSeriesChart } from './charts/interactiveTimeSeriesChart'
import { drawBarChart } from './charts/barChart'
import { drawGaugeChart } from './charts/gaugeChart'
import { prepareChartData } from './charts/chartUtils'

/**
 * Composable para gráficos de datos clínicos
 * Este composable agrega y re-exporta todas las funciones de gráficos desde módulos separados
 * 
 * Funciones disponibles:
 * - drawTimeSeriesChart: Gráfico de línea temporal simple
 * - drawInteractiveTimeSeriesChart: Gráfico de línea temporal con zoom y brush
 * - drawBarChart: Gráfico de barras con animaciones
 * - drawGaugeChart: Medidor circular (gauge) con indicadores de rango
 * - prepareChartData: Utilidad para preparar datos de análisis para visualización
 */
export function useChart() {
  return {
    drawTimeSeriesChart,
    drawInteractiveTimeSeriesChart,
    drawBarChart,
    drawGaugeChart,
    prepareChartData
  }
}
