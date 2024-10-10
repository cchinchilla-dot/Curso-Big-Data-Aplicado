# Guía de monitorización con Ganglia

En relación con los contenidos del curso, esta guía se corresponde con:

- Módulo 4:
  - Monitorización del clúster: Ganglia, entre otros.

## 1. Introducción a la monitorización de clusters en Big Data

En el ecosistema de Big Data, la monitorización eficaz de clusters es crucial para garantizar el rendimiento, la disponibilidad y la eficiencia de los sistemas. Esta guía se centra en el uso de Ganglia para monitorizar clusters de Big Data, integrándolo con herramientas como Hadoop, Hive, Spark y Airflow.

### 1.1 Importancia de la Monitorización en Big Data

- **Rendimiento**: Identificar cuellos de botella y optimizar el procesamiento de datos.
- **Disponibilidad**: Asegurar que los servicios críticos estén siempre operativos.
- **Planificación de capacidad**: Prever necesidades futuras de recursos.
- **Resolución proactiva de problemas**: Detectar y abordar problemas antes de que afecten a los usuarios.

### 1.2 Desafíos específicos de Big Data

- **Escala**: Manejar clusters con cientos o miles de nodos.
- **Diversidad de datos**: Monitorizar diferentes tipos de cargas de trabajo (batch, streaming, interactivas).
- **Complejidad**: Integrar múltiples herramientas y frameworks.

## 2. Arquitectura de monitorización para Big Data

### 2.1 Componentes Clave

1. **Ganglia**: Sistema base de monitorización distribuida.
2. **Colectores de métricas específicas**: Para Hadoop, Spark, Hive, etc.
3. **Almacenamiento de métricas**: Bases de datos de series temporales (ej. MySQL).
4. **Visualización**: Dashboards (ej. Herramientas de visualización de datos).
5. **Alertas**: Sistemas de notificación (ej. Nagios, PagerDuty).

### 2.2 Flujo de Datos de Monitorización

```mermaid
graph TD
    A[Nodos del Cluster] -->|Métricas| B[Ganglia gmond]
    B -->|Agregación| C[Ganglia gmetad]
    C -->|Almacenamiento| D[RRD Files]
    C -->|Exportación| E [MySQL]
    E -->|Visualización| F[Herramientas de visualización de datos]
    E -->|Alertas| G[Sistema de Alertas]
```

## 3. Configuración de Ganglia para Big Data

### 3.1 Configuración de gmond para Hadoop

Edita `/etc/ganglia/gmond.conf` en cada nodo Hadoop:

```conf
cluster {
  name = "Hadoop Cluster"
  owner = "Big Data Team"
  latlong = "unspecified"
  url = "unspecified"
}

udp_send_channel {
  host = gmetad_host
  port = 8649
  ttl = 1
}

udp_recv_channel {
  port = 8649
}

modules {
  module {
    name = "hadoop_metrics"
    path = "/usr/lib/ganglia/modpython.so"
    params = "/etc/ganglia/hadoop_metrics.pyconf"
  }
}
```

### 3.2 Configuración de gmetad

Edita `/etc/ganglia/gmetad.conf`:

```conf
data_source "Hadoop Cluster" 10 localhost
gridname "Big Data Grid"
```

## 4. Integración con herramientas de Big Data

### 4.1 Monitorización de Hadoop

#### 4.1.1 Métricas clave de Hadoop

- Utilización de HDFS (espacio usado, bloques, nodos activos)
- Métricas de YARN (recursos asignados, contenedores activos)
- Job metrics (jobs completados, fallidos, en ejecución)

#### 4.1.2 Configuración de Hadoop para exportar métricas

Edita `hadoop-metrics2.properties`:

```properties
*.sink.ganglia.class=org.apache.hadoop.metrics2.sink.ganglia.GangliaSink31
*.sink.ganglia.period=10
namenode.sink.ganglia.servers=gmond_host:8649
```

### 4.2 Monitorización de Spark

#### 4.2.1 Métricas clave de Spark

- Ejecutores activos
- Tareas completadas/fallidas
- Uso de memoria y CPU por ejecutor
- Tiempo de procesamiento por etapa

#### 4.2.2 Configuración de Spark para métricas

En `spark-defaults.conf`:

```conf
spark.metrics.conf.*.sink.ganglia.class=org.apache.spark.metrics.sink.GangliaSink
spark.metrics.conf.*.sink.ganglia.host=gmond_host
spark.metrics.conf.*.sink.ganglia.port=8649
```

### 4.3 Monitorización de Hive

#### 4.3.1 Métricas clave de Hive

- Tiempo de ejecución de consultas
- Número de consultas concurrentes
- Uso de recursos por consulta
- Estadísticas de tablas y particiones

#### 4.3.2 Configuración de Hive para métricas

En `hive-site.xml`:

```xml
<property>
  <name>hive.service.metrics.ganglia.class</name>
  <value>org.apache.hadoop.hive.common.metrics.metrics2.GangliaReporter</value>
</property>
<property>
  <name>hive.service.metrics.ganglia.host</name>
  <value>gmond_host</value>
</property>
```

### 4.4 Monitorización de Airflow

#### 4.4.1 Métricas clave de Airflow

- DAGs activos/pausados
- Tareas exitosas/fallidas
- Tiempo de ejecución de DAGs
- Uso de recursos por tarea

#### 4.4.2 Integración de Airflow con Ganglia

Utiliza la librería `statsd` en Airflow para enviar métricas a Ganglia:

```python
from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from statsd import StatsClient

statsd = StatsClient(host='gmond_host', port=8125, prefix='airflow')

def task_function(**context):
    with statsd.timer('task_duration'):
        # Lógica de la tarea
        pass

dag = DAG('example_dag')
task = PythonOperator(
    task_id='example_task',
    python_callable=task_function,
    dag=dag
)
```

## 5. Visualización y análisis

### 5.1 Dashboards

Crea dashboards específicos para cada componente:

1. **Dashboard general del cluster**:
   - Uso de CPU, memoria, disco y red a nivel de cluster
   - Número de nodos activos/inactivos

2. **Dashboard de Hadoop**:
   - Uso de HDFS
   - Métricas de YARN
   - Estadísticas de jobs

3. **Dashboard de Spark**:
   - Ejecutores activos
   - Estadísticas de tareas y etapas
   - Uso de recursos por aplicación

4. **Dashboard de Hive**:
   - Tiempos de ejecución de consultas
   - Uso de recursos por consulta
   - Estadísticas de tablas

5. **Dashboard de Airflow**:
   - Estado de DAGs
   - Estadísticas de tareas
   - Tiempos de ejecución

### 5.2 Análisis de tendencias

Utiliza las capacidades de Grafana para análisis a largo plazo:

- Comparación de rendimiento mes a mes
- Identificación de patrones de uso estacionales
- Proyecciones de crecimiento de datos y uso de recursos

## 6. Alertas

Configura alertas en un sistema dedicado como Nagios:

1. **Alertas de recursos del cluster**:
   - CPU > 80% durante más de 15 minutos
   - Memoria libre < 10%
   - Espacio en disco < 20%

2. **Alertas específicas de Hadoop**:
   - Nodos DataNode inactivos > 1
   - Uso de HDFS > 80%

3. **Alertas de Spark**:
   - Ejecutores fallidos > 5 en 1 hora
   - Memoria usada por ejecutor > 90%

4. **Alertas de Hive**:
   - Tiempo de ejecución de consulta > 1 hora
   - Consultas fallidas > 10 en 1 día

5. **Alertas de Airflow**:
   - DAGs fallidos > 2 en 24 horas
   - Tareas atascadas > 5

## 7. Optimización y Ajuste

### 7.1 Ajuste de la Recolección de Métricas

Optimiza la frecuencia de recolección según la importancia de las métricas:

- Métricas críticas: cada 10-30 segundos
- Métricas importantes: cada 1-5 minutos
- Métricas de largo plazo: cada 15-30 minutos

### 7.2 Almacenamiento Eficiente de Métricas

Utiliza políticas de retención para manejar el crecimiento de datos:

- Datos de alta resolución: retención de 24 horas
- Datos agregados por hora: retención de 30 días
- Datos agregados por día: retención de 1 año

### 7.3 Optimización de Consultas y Dashboards

- Utiliza consultas eficientes en Grafana
- Implementa caching para dashboards frecuentemente accedidos
- Considera el uso de vistas materializadas para cálculos complejos

## 8. Casos de uso avanzados

### 8.1 Análisis Predictivo de Fallos

Utiliza machine learning para predecir fallos basándose en patrones históricos:

```python
from sklearn.ensemble import RandomForestClassifier
import pandas as pd

# Cargar datos históricos
data = pd.read_csv('cluster_metrics.csv')

# Preparar features y target
X = data[['cpu_usage', 'memory_usage', 'disk_io', 'network_traffic']]
y = data['failure_occurred']

# Entrenar modelo
model = RandomForestClassifier()
model.fit(X, y)

# Predecir probabilidad de fallo
current_metrics = [[current_cpu, current_memory, current_disk, current_network]]
failure_probability = model.predict_proba(current_metrics)[0][1]

if failure_probability > 0.7:
    send_alert("Alto riesgo de fallo del cluster")
```

### 8.2 Optimización automática de recursos

Implementa un sistema que ajuste automáticamente la asignación de recursos basándose en la carga del cluster:

```python
from yarn_api_client import ResourceManager

rm = ResourceManager(address='yarn_resource_manager_host', port=8088)

def adjust_yarn_resources():
    cluster_metrics = rm.cluster_metrics().data
    
    if cluster_metrics['totalMB'] - cluster_metrics['allocatedMB'] < 1024:  # Menos de 1GB libre
        increase_yarn_capacity()
    elif (cluster_metrics['totalMB'] - cluster_metrics['allocatedMB']) / cluster_metrics['totalMB'] > 0.3:  # Más del 30% libre
        decrease_yarn_capacity()

def increase_yarn_capacity():
    # Lógica para aumentar la capacidad de YARN
    pass

def decrease_yarn_capacity():
    # Lógica para disminuir la capacidad de YARN
    pass

# Ejecutar periódicamente
import schedule
schedule.every(5).minutes.do(adjust_yarn_resources)
```

## 9. Mejores Prácticas y Conclusiones

1. **Monitorización holística**: Integra métricas de todas las capas del stack de Big Data.
2. **Automatización**: Implementa respuestas automáticas para problemas comunes.
3. **Escalabilidad**: Diseña tu sistema de monitorización para crecer con tu cluster.
4. **Seguridad**: Asegura que tu sistema de monitorización no introduzca vulnerabilidades.
5. **Capacitación**: Entrena a tu equipo en la interpretación de métricas y respuesta a incidentes.
6. **Mejora continua**: Revisa y ajusta regularmente tu estrategia de monitorización.
