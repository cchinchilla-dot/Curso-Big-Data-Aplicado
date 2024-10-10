# Guía avanzada del Stack ELK: Ejercicios prácticos

## 1. Ejercicios prácticos

### Ejercicio 1

Configura un pipeline de Logstash que realice las siguientes tareas:

1. Lea logs de Apache desde un archivo.
2. Parse los logs para extraer información relevante (IP, fecha, método HTTP, URL, código de respuesta).
3. Enriquezca los datos con información geográfica basada en la IP.
4. Indexe los datos procesados en Elasticsearch.

Luego, crea una visualización en Kibana que muestre:

- Un mapa de calor de las ubicaciones de los visitantes.
- Un gráfico de barras de los códigos de respuesta HTTP más comunes.
- Una tabla con las 10 URLs más visitadas.

### Ejercicio 2

Implementa un sistema de monitoreo de logs de aplicación:

1. Configura Filebeat para recoger logs de una aplicación (puedes simular estos logs si no tienes una aplicación real).
2. Usa Logstash para procesar estos logs, extrayendo información como nivel de log, mensaje, y cualquier dato estructurado en el mensaje.
3. Indexa los logs procesados en Elasticsearch.
4. Crea un dashboard en Kibana que muestre:
   - Un contador de logs por nivel (INFO, WARN, ERROR, etc.)
   - Un gráfico de líneas que muestre la frecuencia de logs a lo largo del tiempo.
   - Una tabla con los mensajes de error más recientes.

Además, configura una alerta que se dispare cuando haya un aumento significativo en el número de logs de error.

### Ejercicio 3

Desarrolla un sistema de análisis de ventas utilizando el Stack ELK:

1. Crea un índice en Elasticsearch para almacenar datos de ventas.
2. Utiliza el API de Elasticsearch para ingestar datos de ventas simulados (incluye campos como fecha, producto, cantidad, precio, cliente, región).
3. Crea un dashboard en Kibana que muestre:
   - Ingresos totales por mes
   - Top 5 productos por cantidad vendida
   - Distribución de ventas por región (usando un mapa)
   - Valor promedio de venta por cliente

4. Implementa una búsqueda en Kibana que permita a los usuarios encontrar todas las ventas de un producto específico en un rango de fechas determinado.

## 2. Soluciones

### Ejercicio 1

1. Configuración de Logstash:

Crea un archivo `apache_logs.conf` en el directorio de configuración de Logstash:

```ruby
input {
  file {
    path => "/path/to/apache/access.log"
    start_position => "beginning"
  }
}

filter {
  grok {
    match => { "message" => "%{COMBINEDAPACHELOG}" }
  }
  date {
    match => [ "timestamp" , "dd/MMM/yyyy:HH:mm:ss Z" ]
  }
  geoip {
    source => "clientip"
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "apache_logs-%{+YYYY.MM.dd}"
  }
}
```

2. Visualizaciones en Kibana:

a. Mapa de calor de ubicaciones:

   - Crea una visualización de tipo "Maps"
   - Usa el campo `geoip.location` para la ubicación
   - Ajusta la intensidad basada en el conteo de logs

b. Gráfico de barras de códigos de respuesta:

   - Crea una visualización de tipo "Vertical bar"
   - Eje X: Agregación de "Terms" en el campo "response"
   - Eje Y: "Count" de documentos

c. Tabla de URLs más visitadas:

   - Crea una visualización de tipo "Data Table"
   - Columnas:
     1. Agregación de "Terms" en el campo "request"
     2. "Count" de documentos
   - Ordena por conteo descendente y limita a 10 filas

### Ejercicio 2

1. Configuración de Filebeat:

Edita el archivo `filebeat.yml`:

```yaml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /path/to/your/application/logs/*.log

output.logstash:
  hosts: ["localhost:5044"]
```

2. Configuración de Logstash:

Crea un archivo `app_logs.conf`:

```ruby
input {
  beats {
    port => 5044
  }
}

filter {
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:log_level} %{GREEDYDATA:log_message}" }
  }
  date {
    match => [ "timestamp", "ISO8601" ]
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "app_logs-%{+YYYY.MM.dd}"
  }
}
```

3. Visualizaciones en Kibana:

a. Contador de logs por nivel:

   - Crea una visualización de tipo "Metric"
   - Agrega métricas de "Count" para cada nivel de log (INFO, WARN, ERROR)

b. Gráfico de líneas de frecuencia de logs:

   - Crea una visualización de tipo "Line"
   - Eje X: Agregación de "Date Histogram" en el campo "@timestamp"
   - Eje Y: "Count" de documentos

c. Tabla de errores recientes:

   - Crea una visualización de tipo "Data Table"
   - Filtro: log_level is ERROR
   - Columnas:
     1. Agregación de "Date" en "@timestamp"
     2. "Terms" agregación en "log_message"
   - Ordena por fecha descendente

4. Configuración de alerta:

En Kibana, ve a "Stack Management" > "Alerts and Insights" > "Rules and Connectors":

- Crea una nueva regla
- Condición: Count of documents where log_level is ERROR
- Umbral: Por ejemplo, más de 10 en la última hora
- Acción: Enviar email o integrar con un sistema de notificaciones

### Ejercicio 3

1. Creación del índice en Elasticsearch:

```bash
curl -X PUT "localhost:9200/ventas" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "properties": {
      "fecha": { "type": "date" },
      "producto": { "type": "keyword" },
      "cantidad": { "type": "integer" },
      "precio": { "type": "float" },
      "cliente": { "type": "keyword" },
      "region": { "type": "keyword" }
    }
  }
}'
```

2. Script Python para ingestar datos simulados:

```python
from datetime import datetime, timedelta
import random
import requests
import json

productos = ["Laptop", "Smartphone", "Tablet", "Smartwatch", "Headphones"]
regiones = ["Norte", "Sur", "Este", "Oeste", "Centro"]
clientes = ["Cliente" + str(i) for i in range(1, 101)]

def generar_venta():
    fecha = datetime.now() - timedelta(days=random.randint(0, 365))
    return {
        "fecha": fecha.isoformat(),
        "producto": random.choice(productos),
        "cantidad": random.randint(1, 10),
        "precio": round(random.uniform(10, 1000), 2),
        "cliente": random.choice(clientes),
        "region": random.choice(regiones)
    }

# Generar y enviar 1000 ventas simuladas
for _ in range(1000):
    venta = generar_venta()
    requests.post("http://localhost:9200/ventas/_doc", json=venta)

print("Datos de ventas simulados ingresados en Elasticsearch.")
```

3. Visualizaciones en Kibana:

a. Ingresos totales por mes:

   - Crea una visualización de tipo "Line"
   - Eje X: Agregación de "Date Histogram" en el campo "fecha" con intervalo mensual
   - Eje Y: "Sum" agregación del campo "precio" multiplicado por "cantidad"

b. Top 5 productos por cantidad vendida:

   - Crea una visualización de tipo "Pie"
   - Slices: Agregación de "Terms" en el campo "producto"
   - Size: Agregación de "Sum" en el campo "cantidad"
   - Ordena por tamaño descendente y limita a 5 slices

c. Distribución de ventas por región:

   - Crea una visualización de tipo "Region Map"
   - Usa el campo "region" para las áreas
   - Métrica: "Sum" agregación del campo "precio" multiplicado por "cantidad"

d. Valor promedio de venta por cliente:

   - Crea una visualización de tipo "Data Table"
   - Columnas:
     1. Agregación de "Terms" en el campo "cliente"
     2. Agregación de "Average" del campo "precio" multiplicado por "cantidad"
   - Ordena por valor promedio descendente

4. Implementación de búsqueda en Kibana:

En el panel de búsqueda de Kibana:

- Usa la sintaxis de consulta de Kibana (KQL) para buscar ventas específicas:
  
  ```bash
  producto: "Laptop" and fecha >= "2023-01-01" and fecha <= "2023-12-31"
  ```

- Esto buscará todas las ventas de laptops en el año 2023.
