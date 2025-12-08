# useChart Composable

Composable de Vue 3 para crear gráficos interactivos con D3.js de forma reutilizable.

## Tipos de Gráficos Disponibles

1. **Línea Temporal Simple** (`drawTimeSeriesChart`)
2. **Línea Temporal Interactiva con Zoom** (`drawInteractiveTimeSeriesChart`)
3. **Barras** (`drawBarChart`)
4. **Gauge/Medidor** (`drawGaugeChart`)

Cada gráfico incluye interactividad, tooltips, rangos recomendados y accesibilidad.

## Instalación

El composable ya incluye D3.js como dependencia. Asegúrate de que D3.js esté instalado en el proyecto:

```bash
npm install d3
```

## Uso

```javascript
import { useChart } from '@/composables/useChart'

export default {
  setup() {
    const { drawTimeSeriesChart, prepareChartData } = useChart()
    return { drawTimeSeriesChart, prepareChartData }
  },
  
  methods: {
    drawMyChart() {
      const container = this.$refs.myChart
      
      // Preparar datos
      const data = this.prepareChartData(this.rawData, 'glucosa')
      
      // Dibujar gráfico
      this.drawTimeSeriesChart(container, data, {
        label: 'Glucosa',
        color: '#c73333',
        recommendedMin: 70,
        recommendedMax: 140,
        ariaLabel: 'Histórico de Glucosa'
      })
    }
  }
}
```

## Guía Rápida por Tipo de Dato Médico

| Dato Médico | Gráfico Recomendado | Razón |
|-------------|---------------------|-------|
| **Glucosa** | Línea Interactiva | Mediciones frecuentes, necesitas ver tendencias y hacer zoom |
| **Hemoglobina** | Barras + Gauge | Mediciones menos frecuentes, comparación temporal |
| **Colesterol** | Barras + Gauge | Mediciones esporádicas, importante ver último valor |
| **Triglicéridos** | Barras + Gauge | Similar a colesterol |
| **Creatinina** | Barras + Gauge | Mediciones periódicas |
| **Hematocrito** | Línea Simple + Gauge | Ver tendencia general y valor actual |

## API Completa

### `drawTimeSeriesChart(container, data, options)`

Dibuja un gráfico de línea temporal en el contenedor especificado.

**Parámetros:**

- `container` (HTMLElement): Elemento DOM donde se dibujará el gráfico
- `data` (Array): Array de objetos con estructura `{ date: Date, value: number }`
- `options` (Object): Opciones de configuración
  - `label` (string): Etiqueta del parámetro (ej: 'Glucosa')
  - `color` (string): Color de la línea en formato hexadecimal (default: '#c73333')
  - `recommendedMin` (number, opcional): Valor mínimo del rango recomendado
  - `recommendedMax` (number, opcional): Valor máximo del rango recomendado
  - `width` (number, opcional): Ancho del gráfico (se ajusta al contenedor por defecto)
  - `height` (number): Alto del gráfico (default: 200)
  - `ariaLabel` (string): Etiqueta aria para accesibilidad (default: 'Gráfico de serie temporal')

**Características:**

- Limpia automáticamente el contenedor antes de dibujar
- Muestra mensaje amigable cuando no hay datos
- Dibuja banda de rango recomendado si se proporcionan `recommendedMin` y `recommendedMax`
- Ejes X (tiempo) e Y (valores) con formato automático
- Puntos interactivos con tooltips que muestran fecha y valor
- Línea suavizada con curva monotónica
- Responsive: se ajusta al ancho del contenedor

### `prepareChartData(entries, paramKey)`

Prepara y filtra datos para el gráfico a partir de entradas brutas.

**Parámetros:**

- `entries` (Array): Array de entradas brutas del servidor
- `paramKey` (string): Clave del parámetro a filtrar (ej: 'glucosa', 'hemoglobina')

**Retorna:**

Array de objetos `{ date: Date, value: number }` ordenados por fecha, excluyendo valores inválidos.

**Transformaciones:**

- Filtra entradas por `key` o `label` que coincidan con `paramKey`
- Convierte `createdAt` a objetos `Date`
- Normaliza valores numéricos (reemplaza comas por puntos)
- Elimina entradas con fechas o valores inválidos
- Ordena por fecha ascendente

## Estilos

Los estilos para los gráficos están en `@/styles/charts.css`. Importa este archivo en tu componente:

```css
@import '@/styles/charts.css';
```

### Clases CSS disponibles:

- `.chart-section`: Contenedor principal del gráfico
- `.chart-header`: Cabecera con título y controles
- `.chart-controls`: Controles del gráfico (selectores, botones, etc.)
- `.chart-container`: Contenedor del SVG
- `.chart-empty`: Mensaje cuando no hay datos

## Ejemplo Completo

```vue
<template>
  <div class="chart-section">
    <div class="chart-header">
      <h4>Evolución Histórica</h4>
      <div class="chart-controls">
        <label for="param-select">Parámetro:</label>
        <select id="param-select" v-model="selectedParam" @change="drawChart">
          <option value="glucosa">Glucosa</option>
          <option value="hemoglobina">Hemoglobina</option>
        </select>
      </div>
    </div>
    <div class="chart-container" ref="myChart"></div>
  </div>
</template>

<script>
import { useChart } from '@/composables/useChart'

export default {
  setup() {
    const { drawTimeSeriesChart, prepareChartData } = useChart()
    return { drawTimeSeriesChart, prepareChartData }
  },
  
  data() {
    return {
      selectedParam: 'glucosa',
      entries: []
    }
  },
  
  mounted() {
    this.loadData()
  },
  
  methods: {
    async loadData() {
      // Cargar datos del servidor
      const response = await fetch('/api/data')
      this.entries = await response.json()
      this.drawChart()
    },
    
    drawChart() {
      const container = this.$refs.myChart
      if (!container) return
      
      // Preparar datos
      const data = this.prepareChartData(this.entries, this.selectedParam)
      
      // Dibujar gráfico
      this.drawTimeSeriesChart(container, data, {
        label: this.selectedParam,
        color: '#c73333',
        recommendedMin: 70,
        recommendedMax: 140,
        ariaLabel: `Histórico de ${this.selectedParam}`
      })
    }
  }
}
</script>

<style scoped>
@import '@/styles/charts.css';
</style>
```

## Características de Accesibilidad

- Atributos `role="img"` y `aria-label` en el SVG
- Tooltips en los puntos de datos
- Formato de fecha legible en ejes
- Colores con suficiente contraste

### `drawInteractiveTimeSeriesChart(container, data, options)`

Similar a `drawTimeSeriesChart` pero con características adicionales:

**Nuevas características:**

- **Brush/Zoom**: Selector de rango temporal en la parte inferior
- **Tooltips interactivos**: Hover sobre puntos muestra información detallada
- **Mayor altura**: Default 300px vs 200px
- **Puntos ampliables**: Los puntos crecen al hacer hover

**Cuándo usar**: Para datos con muchas mediciones (glucosa, presión arterial) donde el usuario necesita explorar diferentes períodos temporales.

### `drawBarChart(container, data, options)`

Dibuja un gráfico de barras con animación y tooltips.

**Parámetros adicionales específicos:**

- Los mismos que `drawTimeSeriesChart` pero optimizado para datos discretos

**Características:**

- Animación de entrada de barras (800ms)
- Tooltips al hacer hover
- Barras se oscurecen ligeramente al pasar el ratón
- Etiquetas rotadas en eje X para mejor legibilidad
- Banda de rango recomendado

**Cuándo usar**: Para datos con mediciones poco frecuentes (análisis mensuales, trimestrales) donde quieres comparar valores discretos.

### `drawGaugeChart(container, value, options)`

Dibuja un medidor circular (gauge) para mostrar un único valor.

**Parámetros:**

- `container` (HTMLElement): Elemento DOM
- `value` (number): Valor actual a mostrar
- `options` (Object):
  - `label` (string): Etiqueta del medidor
  - `min` (number): Valor mínimo de la escala (default: 0)
  - `max` (number): Valor máximo de la escala (default: 100)
  - `recommendedMin` (number, opcional): Límite inferior del rango normal
  - `recommendedMax` (number, opcional): Límite superior del rango normal
  - `unit` (string): Unidad de medida ('mg/dL', '%', etc.)
  - `width` (number, opcional): Ancho del medidor (default: 300px)
  - `height` (number): Alto del medidor (default: 200)

**Características:**

- Animación de 0 al valor real (1000ms)
- Color automático según rango:
  - 🟢 Verde si está dentro del rango recomendado
  - 🔴 Rojo si está fuera del rango
  - 🔵 Azul si no hay rango definido
- Marcas visuales para límites del rango recomendado
- Valor grande y centrado con unidad
- Responsive

**Cuándo usar**: Para dashboards, vistas resumidas, o cuando solo interesa el valor más reciente en contexto de su rango normal.

## Ejemplos de Uso por Métrica

### Ejemplo 1: Glucosa con Zoom Interactivo

```javascript
const glucosaData = this.prepareChartData(this.entries, 'glucosa')

this.drawInteractiveTimeSeriesChart(this.$refs.glucosaChart, glucosaData, {
  label: 'Glucosa',
  color: '#c73333',
  recommendedMin: 70,
  recommendedMax: 140,
  height: 400,
  ariaLabel: 'Histórico de glucosa con zoom'
})
```

### Ejemplo 2: Creatinina con Barras

```javascript
const creatininaData = this.prepareChartData(this.entries, 'creatinina')

this.drawBarChart(this.$refs.creatininaChart, creatininaData, {
  label: 'Creatinina',
  color: '#0284c7',
  recommendedMin: 0.6,
  recommendedMax: 1.2,
  ariaLabel: 'Histórico de creatinina'
})
```

### Ejemplo 3: Panel de Gauges

```javascript
// Obtener último valor de cada métrica
const metrics = ['glucosa', 'hemoglobina', 'colesterol', 'trigliceridos', 'creatinina', 'hematocrito']

metrics.forEach(metric => {
  const latestEntry = this.entries
    .filter(e => e.key === metric)
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0]
  
  if (latestEntry) {
    const value = parseFloat(latestEntry.value)
    const config = this.metricsConfig[metric] // Configuración de rangos
    
    this.drawGaugeChart(this.$refs[`gauge_${metric}`], value, {
      label: config.label,
      unit: config.unit,
      min: config.gaugeMin,
      max: config.gaugeMax,
      recommendedMin: config.recommendedMin,
      recommendedMax: config.recommendedMax
    })
  }
})
```

### Ejemplo 4: Comparación Multi-Gráfico

```vue
<template>
  <div class="metrics-comparison">
    <!-- Gauges para vista rápida -->
    <div class="gauges-grid">
      <div ref="gauge1"></div>
      <div ref="gauge2"></div>
      <div ref="gauge3"></div>
    </div>
    
    <!-- Selector de tipo de vista -->
    <select v-model="viewType" @change="updateView">
      <option value="line">Línea simple</option>
      <option value="interactive">Línea con zoom</option>
      <option value="bar">Barras</option>
    </select>
    
    <!-- Gráfico dinámico -->
    <div ref="mainChart"></div>
  </div>
</template>

<script>
methods: {
  updateView() {
    const data = this.prepareChartData(this.entries, this.selectedMetric)
    const options = { /* ... */ }
    
    switch(this.viewType) {
      case 'line':
        this.drawTimeSeriesChart(this.$refs.mainChart, data, options)
        break
      case 'interactive':
        this.drawInteractiveTimeSeriesChart(this.$refs.mainChart, data, options)
        break
      case 'bar':
        this.drawBarChart(this.$refs.mainChart, data, options)
        break
    }
  }
}
</script>
```

## Notas

- El gráfico se limpia automáticamente antes de redibujar
- Los datos se ordenan por fecha automáticamente
- Valores con coma se convierten a punto decimal
- Se filtran automáticamente valores inválidos (NaN, null, undefined)
- El ancho se ajusta al contenedor
- Todos los gráficos incluyen atributos ARIA para accesibilidad
- Los tooltips se posicionan automáticamente y no interfieren con la interacción
- Las animaciones mejoran la experiencia pero no bloquean la UI
