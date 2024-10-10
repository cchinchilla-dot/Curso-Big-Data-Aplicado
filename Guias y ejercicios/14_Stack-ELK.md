# Guía avanzada del Stack ELK (Elasticsearch, Logstash, Kibana)

En relación con los contenidos del curso, esta guía se corresponde con:

- Modulo 1:
  - Procesamiento de datos.
- Módulo 2:
  - Otras herramientas.

- Módulo 4:
  - Interfaz web del Jobtracker y Namenode, entre otras.
  - Análisis de los históricos.

## 1. Introducción al Stack ELK

El Stack ELK es un conjunto de herramientas de código abierto que trabajan juntas para ingerir, procesar, almacenar y visualizar datos en tiempo real. ELK es el acrónimo de tres proyectos open source: Elasticsearch, Logstash y Kibana.

### 1.1 ¿Qué es el Stack ELK?

- **Elasticsearch**: Es un motor de búsqueda y análisis distribuido, basado en Apache Lucene. Proporciona una capa de abstracción sobre Lucene que permite realizar búsquedas y análisis de datos de manera rápida y en tiempo casi real.

- **Logstash**: Es una herramienta de procesamiento de datos del lado del servidor que ingiere datos de múltiples fuentes simultáneamente, los transforma y luego los envía a un "stash" como Elasticsearch.

- **Kibana**: Es una plataforma de visualización de datos que trabaja sobre Elasticsearch. Proporciona visualizaciones (histogramas, gráficos de líneas, gráficos circulares, etc.) de los datos indexados en Elasticsearch.

### 1.2 ¿Para qué se utiliza el Stack ELK?

El Stack ELK se utiliza principalmente para:

1. **Centralización y análisis de logs**: Permite recopilar logs de múltiples sistemas y aplicaciones en un solo lugar para su análisis.
2. **Monitorización de aplicaciones**: Ayuda a rastrear el rendimiento de las aplicaciones y a identificar cuellos de botella.
3. **Seguridad y análisis de amenazas**: Se utiliza para recopilar y analizar datos de seguridad, detectando patrones anómalos que podrían indicar amenazas.
4. **Análisis de negocio**: Permite visualizar datos de ventas, marketing, comportamiento de usuarios, etc., para obtener insights de negocio.
5. **Monitorización de infraestructura**: Ayuda a supervisar el rendimiento de servidores, redes y otros componentes de infraestructura.
6. **Internet de las cosas (IoT)**: Permite recopilar y analizar datos de dispositivos IoT en tiempo real.

### 1.3 ¿Cómo funciona el Stack ELK?

El funcionamiento general del Stack ELK sigue este flujo:

1. **Recopilación de datos**: Los datos se recopilan de diversas fuentes (logs de aplicaciones, métricas de sistemas, datos de sensores, etc.) y se envían a Logstash.
2. **Procesamiento con Logstash**: Logstash ingiere estos datos, los parsea y transforma según las reglas definidas.
3. **Indexación en Elasticsearch**: Los datos procesados se envían a Elasticsearch, donde se indexan y almacenan.
4. **Visualización con Kibana**: Kibana se conecta a Elasticsearch y proporciona una interfaz web para buscar, visualizar y analizar los datos indexados.

### 1.4 Características principales

- **Escalabilidad**: El Stack ELK puede manejar grandes volúmenes de datos y escalar horizontalmente.
- **Tiempo real**: Proporciona capacidades de búsqueda y análisis en tiempo casi real.
- **Flexibilidad**: Puede procesar y analizar diversos tipos de datos estructurados y no estructurados.
- **Potente búsqueda**: Ofrece capacidades de búsqueda de texto completo y análisis complejos.
- **Visualizaciones interactivas**: Permite crear dashboards personalizados y visualizaciones interactivas.
- **Ecosistema extenso**: Cuenta con una amplia gama de plugins y integraciones.

## 2. Instalación del Stack ELK

Para facilitar la instalación y configuración del Stack ELK, se recomienda utilizar la imagen de Docker proporcionada por `deviantony`. Esta imagen incluye el Stack ELK completo y es fácil de instalar y configurar.

### 2.1 Requisitos previos

- Docker instalado en tu sistema
- Docker Compose instalado en tu sistema
- Al menos 4GB de RAM disponible

### 2.2 Pasos para la instalación

1. Clona el repositorio de GitHub:

   ```bash
   git clone https://github.com/deviantony/docker-elk.git
   ```

2. Navega al directorio del proyecto:

   ```bash
   cd docker-elk
   ```

3. (Opcional) Ajusta la configuración:
   Puedes modificar los archivos de configuración en los directorios `elasticsearch/config`, `logstash/config`, y `kibana/config` según tus necesidades.

4. Inicia el Stack ELK:

   ```bash
   docker-compose up -d setup
   docker-compose up
   ```

Este comando descargará las imágenes necesarias y iniciará los contenedores para Elasticsearch, Logstash y Kibana.

5. Verifica que los servicios estén funcionando:

   ```bash
   docker-compose ps
   ```

Deberías ver tres contenedores en estado "Up".

### 2.3 Acceso a las interfaces

- Elasticsearch: <http://localhost:9200>

user: *elastic*
password: *changeme*

- Kibana: <http://localhost:5601>

**Nota importante**: El Stack ELK no está incluido en el entorno de pruebas del curso. Deberás instalarlo en tu propio entorno de desarrollo o producción siguiendo los pasos anteriores.

Para más detalles sobre la configuración y uso de esta implementación de Docker, consulta la documentación en el repositorio: <https://github.com/deviantony/docker-elk>

## 3. Elasticsearch

Elasticsearch es el centro del Stack ELK, proporcionando capacidades de búsqueda y análisis en tiempo real. Es un motor de búsqueda y análisis distribuido, capaz de abordar un número creciente de casos de uso.

### 3.1 Conceptos básicos

- **Nodo**: Una instancia en ejecución de Elasticsearch.
- **Cluster**: Un conjunto de uno o más nodos que juntos mantienen todos tus datos.
- **Índice**: Una colección de documentos que tienen características similares.
- **Tipo**: Una categoría/partición lógica dentro de un índice (obsoleto en versiones recientes).
- **Documento**: La unidad básica de información que puede ser indexada, en formato JSON.
- **Shards**: Subdivisiones de un índice, permiten distribuir y paralelizar operaciones.
- **Réplicas**: Copias de shards para proporcionar redundancia y mejorar la capacidad de respuesta.

### 3.2 Arquitectura

Elasticsearch utiliza una arquitectura distribuida:

1. **Distribución de datos**: Los datos se dividen en "shards" que se distribuyen entre los nodos del cluster.
2. **Replicación**: Cada shard puede tener cero o más réplicas para redundancia y rendimiento.
3. **Coordinación**: Cualquier nodo puede recibir una solicitud y coordinar la respuesta.
4. **Detección y recuperación**: Los nodos se comunican para detectar fallos y redistribuir datos si es necesario.

### 3.3 Operaciones básicas

#### Indexación de documentos

La indexación es el proceso de agregar datos a Elasticsearch. Cada documento se indexa y es totalmente buscable en tiempo casi real (dentro de 1 segundo).

```bash
curl -X POST "localhost:9200/mi_indice/_doc" -H "Content-Type: application/json" -d'
{
  "titulo": "Introducción a Elasticsearch",
  "autor": "John Doe",
  "fecha": "2023-06-15",
  "contenido": "Elasticsearch es un motor de búsqueda y análisis distribuido..."
}'
```

Este comando crea un nuevo documento en el índice "mi_indice". Si el índice no existe, Elasticsearch lo creará automáticamente.

#### Búsqueda de documentos

Elasticsearch proporciona una API REST para buscar y analizar datos.

```bash
curl -X GET "localhost:9200/mi_indice/_search?q=autor:John"
```

Esta consulta buscará todos los documentos en "mi_indice" donde el campo "autor" contiene "John".

#### Búsqueda con Query DSL

Para consultas más complejas, Elasticsearch proporciona un Query DSL (Domain Specific Language) basado en JSON.

```bash
curl -X GET "localhost:9200/mi_indice/_search" -H "Content-Type: application/json" -d'
{
  "query": {
    "bool": {
      "must": [
        { "match": { "autor": "John" } },
        { "range": { "fecha": { "gte": "2023-01-01" } } }
      ]
    }
  }
}'
```

Esta consulta busca documentos donde el autor es "John" y la fecha es posterior o igual al 1 de enero de 2023.

#### Actualización de documentos

Elasticsearch permite actualizar documentos existentes.

```bash
curl -X POST "localhost:9200/mi_indice/_update/1" -H "Content-Type: application/json" -d'
{
  "doc": {
    "titulo": "Introducción actualizada a Elasticsearch"
  }
}'
```

Este comando actualiza el campo "titulo" del documento con ID 1 en "mi_indice".

#### Eliminación de documentos

Puedes eliminar documentos individuales o consultas enteras.

```bash
curl -X DELETE "localhost:9200/mi_indice/_doc/1"
```

Este comando elimina el documento con ID 1 de "mi_indice".

### 3.4 Análisis y agregaciones

Elasticsearch no solo es potente en búsquedas, sino también en análisis de datos.

#### Agregaciones básicas

```bash
curl -X GET "localhost:9200/mi_indice/_search" -H "Content-Type: application/json" -d'
{
  "size": 0,
  "aggs": {
    "autores_populares": {
      "terms": {
        "field": "autor.keyword",
        "size": 5
      }
    }
  }
}'
```

Esta consulta devuelve los 5 autores más frecuentes en "mi_indice".

#### Agregaciones anidadas

```bash
curl -X GET "localhost:9200/mi_indice/_search" -H "Content-Type: application/json" -d'
{
  "size": 0,
  "aggs": {
    "rango_fechas": {
      "date_histogram": {
        "field": "fecha",
        "calendar_interval": "month"
      },
      "aggs": {
        "autores_top": {
          "terms": {
            "field": "autor.keyword",
            "size": 3
          }
        }
      }
    }
  }
}'
```

Esta consulta agrupa los documentos por mes y luego encuentra los 3 autores más frecuentes en cada mes.

### 3.5 Mappings y tipos de datos

Los mappings en Elasticsearch son como los esquemas en bases de datos relacionales. Definen cómo se almacenan e indexan los documentos.

#### Creación de un mapping explícito

```bash
curl -X PUT "localhost:9200/mi_indice" -H "Content-Type: application/json" -d'
{
  "mappings": {
    "properties": {
      "titulo": { "type": "text" },
      "autor": { "type": "keyword" },
      "fecha": { "type": "date" },
      "contenido": { "type": "text" }
    }
  }
}'
```

Este comando crea un nuevo índice con un mapping explícito.

#### Tipos de datos comunes

- **text**: Para texto completo, analizado y buscable.
- **keyword**: Para identificadores exactos o campos de agrupación/filtrado.
- **date**: Para fechas.
- **long**, **integer**, **short**, **byte**: Para números enteros.
- **double**, **float**: Para números decimales.
- **boolean**: Para valores verdadero/falso.
- **object**: Para objetos JSON anidados.
- **nested**: Para arrays de objetos que deben ser consultados independientemente.

### 3.6 Optimización y rendimiento

Algunas consideraciones para optimizar Elasticsearch:

1. **Dimensionamiento adecuado**: Asegúrate de que tu cluster tenga suficientes recursos.
2. **Sharding**: Elige el número correcto de shards basándote en el tamaño de tus datos y el número de nodos.
3. **Replicación**: Usa réplicas para mejorar la disponibilidad y el rendimiento de lectura.
4. **Mappings**: Diseña tus mappings cuidadosamente para optimizar el almacenamiento y la búsqueda.
5. **Bulk API**: Usa la API de operaciones en lote para indexaciones masivas.
6. **Cachés**: Utiliza y ajusta las diferentes cachés de Elasticsearch.
7. **Monitoreo**: Usa herramientas como Elastic Stack Monitoring para supervisar y ajustar el rendimiento.

## 4. Logstash

Logstash es una herramienta de procesamiento de datos del lado del servidor que ingiere datos de múltiples fuentes simultáneamente, los transforma y luego los envía a un "stash" como Elasticsearch.

### 4.1 Arquitectura de Logstash

Logstash tiene una arquitectura de pipeline que consta de tres etapas:

1. **Inputs**: Ingieren datos de diversas fuentes.
2. **Filters**: Transforman y procesan los datos.
3. **Outputs**: Envían los datos procesados a uno o más destinos.

### 4.2 Configuración de Logstash

La configuración de Logstash se realiza mediante archivos de configuración que definen el pipeline.

#### Estructura básica de un pipeline de Logstash

```ruby
input {
  # Definición de fuentes de datos
}

filter {
  # Definición de transformaciones
}

output {
  # Definición de destinos
}
```

#### Ejemplo detallado

```ruby
input {
  file {
    path => "/var/log/apache/access.log"
    start_position => "beginning"
  }
}

filter {
  grok {
    match => { "message" => "%{COMBINEDAPACHELOG}" }
  }
  date {
    match => [ "timestamp" , "dd/MMM/yyyy:HH:mm:ss Z" ]
    target => "@timestamp"
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

Este ejemplo:

1. Lee datos de un archivo de log de Apache.
2. Parsea los logs usando el patrón COMBINEDAPACHELOG.
3. Extrae y formatea la fecha del log para usar como timestamp.
4. Añade información geográfica basada en la IP del cliente.
5. Envía los datos procesados a Elasticsearch, creando un índice diario.

### 4.3 Inputs

Los inputs son responsables de ingerir datos en Logstash. Algunos inputs comunes incluyen:

- **file**: Lee datos de un archivo o directorio.
- **syslog**: Escucha mensajes de syslog en puertos específicos.
- **beats**: Recibe datos de Filebeat y otros agentes Beats.
- **jdbc**: Lee datos de una base de datos SQL.
- **http**: Recibe datos a través de peticiones HTTP/HTTPS.

Ejemplo de múltiples inputs:

```ruby
input {
  file {
    path => "/var/log/apache/access.log"
    type => "apache"
  }
  syslog {
    port => 5000
    type => "syslog"
  }
}
```

### 4.4 Filters

Los filters procesan los eventos de entrada, permitiendo parsear, transformar y enriquecer los datos. Algunos filters comunes son:

- **grok**: Parsea y estructura texto no estructurado.
- **mutate**: Realiza transformaciones generales en los campos del evento.
- **date**: Parsea fechas de campos y las usa como el timestamp del evento.
- **geoip**: Añade información geográfica basada en direcciones IP.
- **ruby**: Ejecuta código Ruby arbitrario.

Ejemplo de uso de múltiples filters:

```ruby
filter {
  if [type] == "apache" {
    grok {
      match => { "message" => "%{COMBINEDAPACHELOG}" }
    }
    date {
      match => [ "timestamp", "dd/MMM/yyyy:HH:mm:ss Z" ]
      target => "@timestamp"
    }
  }
  if [type] == "syslog" {
    grok {
      match => { "message" => "%{SYSLOGTIMESTAMP:syslog_timestamp} %{SYSLOGHOST:syslog_hostname} %{DATA:syslog_program}(?:\[%{POSINT:syslog_pid}\])?: %{GREEDYDATA:syslog_message}" }
    }
    date {
      match => [ "syslog_timestamp", "MMM  d HH:mm:ss", "MMM dd HH:mm:ss" ]
      target => "@timestamp"
    }
  }
}
```

### 4.5 Outputs

Los outputs definen dónde Logstash debe enviar los eventos procesados. Algunos outputs comunes incluyen:

- **elasticsearch**: Envía eventos a Elasticsearch.
- **file**: Escribe eventos en un archivo.
- **kafka**: Envía eventos a Apache Kafka.
- **s3**: Escribe eventos en un bucket de Amazon S3.
- **email**: Envía un email cuando se cumplen ciertas condiciones.

Ejemplo de múltiples outputs:

```ruby
output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "%{type}-%{+YYYY.MM.dd}"
  }
  file {
    path => "/var/log/logstash/%{type}-%{+YYYY.MM.dd}.log"
  }
}
```

### 4.6 Plugins

Logstash tiene un rico ecosistema de plugins que extienden su funcionalidad. Puedes instalar plugins adicionales usando el comando `logstash-plugin`:

```bash
bin/logstash-plugin install logstash-input-twitter
```

### 4.7 Rendimiento y escalabilidad

Para mejorar el rendimiento de Logstash:

1. Usa múltiples pipelines para procesar diferentes tipos de datos.
2. Ajusta el número de workers y batch size.
3. Usa persistent queues para manejar picos de tráfico.
4. Monitorea el rendimiento usando las APIs de monitoreo de Logstash.

## 5. Kibana

Kibana es la plataforma de visualización y gestión para el ecosistema Elastic. Permite a los usuarios visualizar y analizar datos almacenados en Elasticsearch a través de una interfaz web intuitiva.

### 5.1 Características principales

- Exploración y búsqueda de datos
- Creación de visualizaciones personalizadas
- Construcción de dashboards interactivos
- Análisis de series temporales
- Creación de mapas geoespaciales
- Aprendizaje automático y detección de anomalías (con X-Pack)

### 5.2 Interfaz de usuario

La interfaz de Kibana se divide en varias secciones principales:

1. **Discover**: Para explorar y buscar en tus datos.
2. **Visualize**: Para crear y editar visualizaciones.
3. **Dashboard**: Para crear paneles con múltiples visualizaciones.
4. **Management**: Para gestionar índices, usuarios, roles, etc.

### 5.3 Creación de visualizaciones

Kibana ofrece varios tipos de visualizaciones:

1. Gráficos de barras y líneas
2. Gráficos circulares y de área
3. Tablas de datos
4. Métricas y gráficos de medidor
5. Mapas de calor y mapas geográficos
6. Nubes de palabras

Proceso de creación de una visualización:

1. Ve a la pestaña "Visualize".
2. Haz clic en "Create new visualization".
3. Elige el tipo de visualización.
4. Selecciona la fuente de datos (índice de Elasticsearch).
5. Configura los ejes, métricas y opciones de la visualización.
6. Guarda la visualización.

### 5.4 Creación de dashboards

Los dashboards en Kibana permiten combinar múltiples visualizaciones en un solo panel.

Proceso de creación de un dashboard:

1. Ve a la pestaña "Dashboard".
2. Haz clic en "Create new dashboard".
3. Utiliza el botón "Add" para incluir visualizaciones existentes o crear nuevas.
4. Organiza y redimensiona los paneles según sea necesario.
5. Utiliza filtros y controles para hacer el dashboard interactivo.
6. Guarda el dashboard.

### 5.5 Kibana Query Language (KQL)

KQL es un lenguaje de consulta simplificado para filtrar datos en Kibana.

Ejemplos de consultas KQL:

- `response:200`: Encuentra documentos donde el campo "response" es exactamente 200.
- `agent:*Chrome*`: Encuentra documentos donde el campo "agent" contiene "Chrome".
- `bytes > 1000 and ip:192.168.0.*`: Encuentra documentos con más de 1000 bytes de una subred específica.

### 5.6 Temporizadores y autorefresh

Kibana permite configurar dashboards para que se actualicen automáticamente, lo que es útil para monitoreo en tiempo real.

### 5.7 Seguridad y gestión de usuarios

Con X-Pack, Kibana proporciona características de seguridad como:

- Autenticación de usuarios
- Control de acceso basado en roles
- Cifrado de comunicaciones
- Auditoría de acciones de usuarios

## 6. Beats

Beats son agentes de envío de datos ligeros que se instalan en los servidores para enviar diferentes tipos de datos operativos a Elasticsearch.

### 6.1 Tipos de Beats

- **Filebeat**: Para envío de logs y archivos
- **Metricbeat**: Para métricas del sistema y servicios
- **Packetbeat**: Para análisis de red
- **Winlogbeat**: Para logs de eventos de Windows
- **Auditbeat**: Para datos de auditoría
- **Heartbeat**: Para monitoreo de disponibilidad

### 6.2 Configuración básica de Filebeat

Ejemplo de configuración de Filebeat para enviar logs de Apache:

```yaml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/apache2/*.log

output.elasticsearch:
  hosts: ["localhost:9200"]
```

## 7. Casos de uso comunes

### 7.1 Análisis de logs

- Centralización de logs de múltiples servidores y aplicaciones
- Detección de errores y problemas en tiempo real
- Análisis de patrones de uso y comportamiento de usuarios

### 7.2 Monitorización de infraestructura

- Seguimiento de métricas de servidores (CPU, memoria, disco)
- Monitoreo de servicios y aplicaciones
- Alertas basadas en umbrales predefinidos

### 7.3 Análisis de seguridad

- Detección de intrusiones y actividades sospechosas
- Análisis de logs de firewall y sistemas de seguridad
- Visualización de patrones de tráfico anómalos

### 7.4 Business Intelligence

- Análisis de datos de ventas y marketing
- Seguimiento de KPIs en tiempo real
- Creación de dashboards para ejecutivos

## 8. Mejores prácticas

1. Diseña tus mappings en Elasticsearch para optimizar el almacenamiento y las búsquedas.
2. Utiliza índices con rotación (por ejemplo, diarios o semanales) para manejar grandes volúmenes de datos.
3. Implementa una estrategia de retención de datos para gestionar el crecimiento del almacenamiento.
4. Utiliza aliases de índices para facilitar la migración y el mantenimiento.
5. Monitorea regularmente el rendimiento del cluster y ajusta según sea necesario.
6. Implementa una estrategia de backup y recuperación.
7. Utiliza plantillas de índice para aplicar configuraciones consistentes.
8. Optimiza tus consultas y agregaciones para mejorar el rendimiento.

## 9. Integración con otras herramientas

- **Kafka**: Como buffer entre las fuentes de datos y Logstash.
- **Grafana**: Como alternativa a Kibana para visualizaciones más avanzadas.
- **Prometheus**: Para monitoreo adicional y alerting.
- **Jupyter Notebooks**: Para análisis de datos más avanzados usando Elasticsearch como fuente.

## 10. Troubleshooting común

1. **Problemas de memoria en Elasticsearch**: Ajusta la configuración de heap y monitorea el uso de memoria.
2. **Logstash consume mucha CPU**: Optimiza los filtros y considera usar múltiples instancias.
3. **Consultas lentas en Kibana**: Analiza y optimiza las consultas, considera usar índices más granulares.
4. **Pérdida de datos en Logstash**: Implementa persistent queues y monitorea los backlogs.
5. **Cluster Elasticsearch inestable**: Verifica la configuración de discovery y la comunicación entre nodos.

## 11. Ejercicios prácticos

### Ejercicio 1

1. Configura Filebeat para recolectar logs de Apache o Nginx.
2. Crea un pipeline de Logstash para procesar estos logs.
3. Indexa los datos en Elasticsearch.
4. Crea visualizaciones en Kibana para:
   - Tráfico por hora del día
   - Códigos de respuesta HTTP
   - IPs más frecuentes
   - User agents más comunes

### Ejercicio 2

1. Configura Metricbeat para recolectar métricas del sistema (CPU, memoria, disco).
2. Envía estas métricas directamente a Elasticsearch.
3. Crea un dashboard en Kibana que muestre:
   - Uso de CPU a lo largo del tiempo
   - Uso de memoria
   - Espacio en disco disponible
   - Procesos que consumen más recursos

### Ejercicio 3

1. Importa un conjunto de datos de ventas en Elasticsearch (puedes usar Logstash con un input de CSV).
2. Crea visualizaciones en Kibana para:
   - Ventas totales por mes
   - Productos más vendidos
   - Ventas por región geográfica (usa un mapa)
   - Tendencias de ventas a lo largo del tiempo
