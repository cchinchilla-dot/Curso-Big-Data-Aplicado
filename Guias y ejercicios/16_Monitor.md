# Monitorización de Hadoop y análisis de históricos

En relación con los contenidos del curso, esta guía se corresponde con:

- Módulo 4:
  - Interfaz web del Jobtracker y Namenode, entre otras.
  - Análisis de los históricos.

## 1. Interfaces Web Nativas de Hadoop

### 1.1 Interfaz Web del JobTracker (Hadoop 1.x) / ResourceManager (Hadoop 2.x+)

El JobTracker (en versiones antiguas de Hadoop, 1.x) o el ResourceManager (en versiones más recientes, 2.x+) proporciona una interfaz web para monitorizar el estado de los trabajos y recursos del cluster.

#### Características principales

- Vista general de los trabajos en ejecución, completados y fallidos.
- Detalles de la utilización de recursos por trabajo.
- Información sobre nodos y aplicaciones.

#### Acceso

```bash
http://<jobtracker-host>:50030 # Para Hadoop 1.x
http://<resourcemanager-host>:8088 # Para Hadoop 2.x+
```

#### Ejemplo de uso

```python
import requests
import json

def get_cluster_metrics(rm_url):
    response = requests.get(f"{rm_url}/ws/v1/cluster/metrics")
    if response.status_code == 200:
        metrics = json.loads(response.text)
        print(f"Aplicaciones en ejecución: {metrics['clusterMetrics']['appsRunning']}")
        print(f"Memoria disponible: {metrics['clusterMetrics']['availableMB']} MB")
        print(f"Contenedores asignados: {metrics['clusterMetrics']['containersAllocated']}")
        print(f"Nodos activos: {metrics['clusterMetrics']['activeNodes']}")
    else:
        print("Error al obtener métricas del cluster")

get_cluster_metrics("http://resourcemanager-host:8088")
```

#### Información clave a monitorizar

1. Número de aplicaciones en ejecución, pendientes y completadas.
2. Utilización de recursos (CPU, memoria) a nivel de cluster y por aplicación.
3. Estado de los nodos (activos, perdidos, no saludables).
4. Tiempo de ejecución de los trabajos.

### 1.2 Interfaz Web del NameNode

La interfaz web del NameNode ofrece información sobre el estado del sistema de archivos HDFS.

#### Características principales

- Visión general del espacio utilizado y disponible en HDFS.
- Lista de nodos de datos y su estado.
- Información sobre bloques y replicación.

#### Acceso

```bash
http://<namenode-host>:50070
```

#### Ejemplo de uso

```python
import requests
from bs4 import BeautifulSoup

def get_hdfs_status(nn_url):
    response = requests.get(f"{nn_url}/dfshealth.html")
    if response.status_code == 200:
        soup = BeautifulSoup(response.text, 'html.parser')
        capacity = soup.find(id="capacityUsed").text
        nodes = soup.find(id="numLiveDataNodes").text
        under_replicated = soup.find(id="numBlocksUnderReplicated").text
        print(f"Capacidad utilizada HDFS: {capacity}")
        print(f"Nodos de datos activos: {nodes}")
        print(f"Bloques sub-replicados: {under_replicated}")
    else:
        print("Error al obtener estado de HDFS")

get_hdfs_status("http://namenode-host:50070")
```

#### Información clave a monitorizar

1. Espacio total, usado y disponible en HDFS.
2. Número de nodos de datos activos e inactivos.
3. Número de bloques sub-replicados o corruptos.
4. Distribución de datos entre nodos.

### 1.3 Otras Interfaces Web Relevantes

#### YARN Application Timeline Server

- Proporciona información histórica sobre aplicaciones YARN.
- Acceso: `http://<timeline-server-host>:8188`

#### Hadoop Job History Server

- Almacena información detallada sobre trabajos MapReduce completados.
- Acceso: `http://<jobhistory-host>:19888`

## 2. Análisis de históricos

El análisis de datos históricos es fundamental para identificar tendencias, optimizar recursos y prevenir problemas futuros.

### 2.1 JobHistory Server

El JobHistory Server almacena información detallada sobre trabajos completados, permitiendo un análisis retrospectivo del rendimiento del cluster.

#### Acceso

```bash
http://<jobhistory-host>:19888
```

#### Ejemplo de análisis

```python
import requests
import pandas as pd
from datetime import datetime, timedelta

def analyze_job_history(jhs_url, days_back=7):
    end_date = datetime.now()
    start_date = end_date - timedelta(days=days_back)
    
    response = requests.get(f"{jhs_url}/ws/v1/history/mapreduce/jobs?startedTimeBegin={int(start_date.timestamp()*1000)}&startedTimeEnd={int(end_date.timestamp()*1000)}")
    
    if response.status_code == 200:
        jobs = response.json()['jobs']['job']
        df = pd.DataFrame(jobs)
        
        # Análisis básico
        print(f"Total de trabajos en los últimos {days_back} días: {len(df)}")
        print(f"Trabajos exitosos: {len(df[df['state'] == 'SUCCEEDED'])}")
        print(f"Trabajos fallidos: {len(df[df['state'] == 'FAILED'])}")
        
        # Tiempo promedio de ejecución
        df['duration'] = pd.to_numeric(df['finishTime']) - pd.to_numeric(df['startTime'])
        print(f"Tiempo promedio de ejecución: {df['duration'].mean() / 1000:.2f} segundos")
        
        # Top 5 trabajos más largos
        top_5_longest = df.nlargest(5, 'duration')
        print("\nTop 5 trabajos más largos:")
        for _, job in top_5_longest.iterrows():
            print(f"ID: {job['id']}, Duración: {job['duration']/1000:.2f} segundos")
        
        # Análisis de tendencias
        df['date'] = pd.to_datetime(df['startTime'], unit='ms')
        daily_jobs = df.groupby(df['date'].dt.date).size()
        print("\nTendencia de trabajos diarios:")
        print(daily_jobs)
        
        # Tasa de éxito por usuario
        success_rate = df.groupby('user')['state'].apply(lambda x: (x == 'SUCCEEDED').mean())
        print("\nTasa de éxito por usuario:")
        print(success_rate)
    
    else:
        print("Error al obtener datos del JobHistory Server")

analyze_job_history("http://jobhistory-host:19888")
```

### 2.2 Análisis de Logs

Los logs de Hadoop proporcionan información detallada sobre el funcionamiento interno del sistema y son cruciales para el diagnóstico de problemas.

#### Ubicaciones comunes de logs

- HDFS NameNode: `/var/log/hadoop-hdfs/hadoop-hdfs-namenode-*.log`
- YARN ResourceManager: `/var/log/hadoop-yarn/yarn-yarn-resourcemanager-*.log`
- MapReduce JobHistory Server: `/var/log/hadoop-mapreduce/mapred-mapred-historyserver-*.log`

#### Ejemplo de análisis

```python
import re
from collections import Counter

def analyze_yarn_logs(log_file):
    error_pattern = re.compile(r'ERROR')
    warn_pattern = re.compile(r'WARN')
    app_pattern = re.compile(r'application_\d+_\d+')
    
    errors = []
    warnings = []
    applications = []
    
    with open(log_file, 'r') as f:
        for line in f:
            if error_pattern.search(line):
                errors.append(line)
            elif warn_pattern.search(line):
                warnings.append(line)
            
            app_match = app_pattern.search(line)
            if app_match:
                applications.append(app_match.group())
    
    print(f"Total de errores: {len(errors)}")
    print(f"Total de advertencias: {len(warnings)}")
    
    app_counts = Counter(applications)
    print("\nAplicaciones más frecuentes en los logs:")
    for app, count in app_counts.most_common(5):
        print(f"{app}: {count} ocurrencias")
    
    print("\nÚltimos 5 errores:")
    for error in errors[-5:]:
        print(error.strip())

analyze_yarn_logs("/var/log/hadoop-yarn/yarn-yarn-resourcemanager-hostname.log")
```

## 4. APIs REST y URLs de Hadoop

Hadoop proporciona varias APIs REST que permiten acceder programáticamente a la información del cluster. Estas APIs son fundamentales para la monitorización y el análisis automatizados. A continuación, se detallan las principales URLs, sus opciones y alternativas.

### 4.1 ResourceManager API

La API del ResourceManager proporciona información sobre el estado del cluster YARN y las aplicaciones en ejecución.

#### URL Base

```bash
http://<resourcemanager-host>:8088/ws/v1/
```

#### Endpoints principales

1. **Métricas del Cluster**

   ```bash
   GET /cluster/metrics
   ```

   Proporciona métricas generales del cluster como número de aplicaciones, contenedores y recursos utilizados.

2. **Aplicaciones**

   ```bash
   GET /cluster/apps
   ```

   Lista todas las aplicaciones en el cluster. Admite varios parámetros de consulta:
   - `state`: Filtra por estado (NEW, SUBMITTED, ACCEPTED, RUNNING, FINISHED, FAILED, KILLED)
   - `finalStatus`: Filtra por estado final (UNDEFINED, SUCCEEDED, FAILED, KILLED)
   - `user`: Filtra por usuario
   - `queue`: Filtra por cola
   - `limit`: Limita el número de resultados
   - `startedTimeBegin`: Filtra aplicaciones iniciadas después de esta marca de tiempo
   - `startedTimeEnd`: Filtra aplicaciones iniciadas antes de esta marca de tiempo

   Ejemplo:

   ```bash
   GET /cluster/apps?state=RUNNING&user=hadoop&limit=10
   ```

3. **Nodos**

   ```bash
   GET /cluster/nodes
   ```

   Proporciona información sobre los nodos del cluster.

4. **Colas**:

   ```bash
   GET /cluster/scheduler
   ```

   Muestra información sobre las colas del planificador.

### 4.2 NameNode API

La API del NameNode proporciona información sobre el estado del sistema de archivos HDFS.

#### URL Base

```bash
http://<namenode-host>:50070/webhdfs/v1/
```

#### Endpoints principales

1. **Estado del sistema de archivos**

   ```bash
   GET /
   ```

   Proporciona información general sobre el sistema de archivos HDFS.

2. **Listado de directorios**

   ```bash
   GET /<path>?op=LISTSTATUS
   ```

   Lista el contenido de un directorio específico.

3. **Información de un archivo**

   ```bash
   GET /<path>?op=GETFILESTATUS
   ```

   Proporciona información detallada sobre un archivo específico.

4. **Espacio utilizado**

   ```bash
   GET /?op=GETCONTENTSUMMARY
   ```

   Proporciona un resumen del espacio utilizado en HDFS.

### 4.3 JobHistory Server API

La API del JobHistory Server permite acceder a información histórica sobre trabajos MapReduce completados.

#### URL Base

```bash
http://<jobhistory-host>:19888/ws/v1/history/
```

#### Endpoints principales

1. **Listar trabajos**

   ```bash
   GET /mapreduce/jobs
   ```

   Lista todos los trabajos MapReduce. Admite parámetros de filtrado similares a la API del ResourceManager.

2. **Detalles de un trabajo específico**

   ```bash
   GET /mapreduce/jobs/<job_id>
   ```

   Proporciona información detallada sobre un trabajo específico.

3. **Contadores de un trabajo**

   ```bash
   GET /mapreduce/jobs/<job_id>/counters
   ```

   Muestra los contadores asociados a un trabajo específico.

### 4.4 Alternativas y Consideraciones

1. **YARN Timeline Server**:
   - URL Base: `http://<timelineserver-host>:8188/ws/v1/timeline/`
   - Proporciona una vista histórica más detallada de las aplicaciones YARN.
   - Útil para obtener información sobre aplicaciones que ya no están en el ResourceManager.

2. **HTTPFS**:
   - URL Base: `http://<httpfs-host>:14000/webhdfs/v1/`
   - Alternativa a WebHDFS que proporciona acceso HTTP a HDFS.
   - Útil cuando el acceso directo al NameNode no es posible por razones de seguridad.

3. **Ambari API**:
   - Si se utiliza Apache Ambari para administrar el cluster, su API proporciona una interfaz unificada para acceder a métricas y estados de varios componentes de Hadoop.
   - URL Base: `http://<ambari-server>:8080/api/v1/`

4. **Consideraciones de Seguridad**:
   - En entornos de producción, estas APIs deberían estar protegidas con autenticación y, preferiblemente, accesibles solo a través de HTTPS.
   - Kerberos es comúnmente utilizado para la autenticación en clusters Hadoop seguros.

5. **Limitaciones de Tasa**:
   - Al desarrollar herramientas de monitorización, es importante considerar las limitaciones de tasa de las APIs para evitar sobrecargar los servicios.

6. **Versionado de APIs**:
   - Las URLs proporcionadas son para versiones específicas de las APIs. Es importante verificar la documentación de la versión exacta de Hadoop en uso, ya que pueden existir diferencias entre versiones.

Al utilizar estas APIs, los administradores y desarrolladores pueden crear herramientas de monitorización personalizadas y sistemas de alerta que se integren perfectamente con la infraestructura existente de Hadoop. La combinación de estas APIs con herramientas de visualización y análisis puede proporcionar insights profundos sobre el rendimiento y la salud del cluster.

## 4. Mejores prácticas

1. **Monitorización proactiva**: Configurar alertas basadas en umbrales para métricas clave (por ejemplo, uso de HDFS > 80%, nodos inactivos > 2).
2. **Análisis regular de tendencias**: Realizar análisis semanales o mensuales de los datos históricos para identificar patrones y tendencias a largo plazo.
3. **Correlación de datos**: Combinar datos de diferentes fuentes (interfaces web, logs, métricas del sistema) para obtener una visión holística del rendimiento del cluster.
4. **Automatización**: Desarrollar scripts para recopilar y analizar datos automáticamente, generando informes periódicos.
5. **Retención de datos históricos**: Mantener un historial de métricas y logs durante un período suficiente (por ejemplo, 6 meses a 1 año) para análisis a largo plazo.
6. **Visualización de datos**: Utilizar herramientas de visualización para crear dashboards que faciliten la interpretación de los datos de monitorización e históricos.

**Nota importante:** Si utilizas entornos dockerizados, asegúrate de que el puerto esté expuesto y configurado en el puerto predeterminado.
