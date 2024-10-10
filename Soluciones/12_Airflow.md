# Soluciones de ejercicios de Apache Airflow

## Ejercicio 1: DAG "procesamiento_logs"

### Paso 1: Crear el archivo DAG

Crea un nuevo archivo en tu carpeta de `Soluciones` llamado `procesamiento_logs_dag.py`.

### Paso 2: Escribir el código del DAG

Copia y pega el siguiente código en `procesamiento_logs_dag.py`:

```python
from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python_operator import PythonOperator, BranchPythonOperator
from airflow.operators.bash_operator import BashOperator
from airflow.sensors.filesystem import FileSensor
from airflow.utils.trigger_rule import TriggerRule

# Argumentos por defecto para el DAG
default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime(2023, 1, 1),
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 3,
    'retry_delay': timedelta(minutes=5),
}

# Definición del DAG
dag = DAG(
    'procesamiento_logs',
    default_args=default_args,
    description='DAG para procesar archivos de logs',
    schedule_interval=timedelta(days=1),
    catchup=False,
)

# Función para contar líneas
def contar_lineas(**kwargs):
    with open('/opt/airflow/logs/logs.txt', 'r') as file:
        num_lineas = sum(1 for line in file)
    kwargs['ti'].xcom_push(key='num_lineas', value=num_lineas)
    print(f"Número de líneas en el archivo: {num_lineas}")

# Función para decidir la ruta
def decidir_analisis(**kwargs):
    ti = kwargs['ti']
    num_lineas = ti.xcom_pull(key='num_lineas', task_ids='contar_lineas')
    if num_lineas > 100:
        return 'analisis_detallado'
    else:
        return 'analisis_rapido'

# Tareas
esperar_archivo = FileSensor(
    task_id='esperar_archivo',
    filepath='/opt/airflow/logs/logs.txt',
    poke_interval=30,
    timeout=300,
    mode='poke',
    dag=dag,
)

contar_lineas = PythonOperator(
    task_id='contar_lineas',
    python_callable=contar_lineas,
    provide_context=True,
    dag=dag,
)

decidir_analisis = BranchPythonOperator(
    task_id='decidir_analisis',
    python_callable=decidir_analisis,
    provide_context=True,
    dag=dag,
)

analisis_detallado = BashOperator(
    task_id='analisis_detallado',
    bash_command='echo "Realizando análisis detallado..."',
    dag=dag,
)

analisis_rapido = BashOperator(
    task_id='analisis_rapido',
    bash_command='echo "Realizando análisis rápido..."',
    dag=dag,
)

archivar_logs = BashOperator(
    task_id='archivar_logs',
    bash_command='mv /opt/airflow/logs/logs.txt /opt/airflow/logs/processed/logs_$(date +%Y%m%d).txt',
    trigger_rule=TriggerRule.ONE_SUCCESS,
    dag=dag,
)

# Definir el orden de las tareas
esperar_archivo >> contar_lineas >> decidir_analisis >> [analisis_detallado, analisis_rapido] >> archivar_logs
```

### Paso 3: Configurar el entorno

1. Asegúrate de que existe el directorio para los logs procesados:

```bash
mkdir -p /opt/airflow/logs/processed
```

2. Crea un archivo de logs de prueba:

```bash
echo "Log de prueba" > /opt/airflow/logs/logs.txt
```

3. Agrega más líneas al archivo de logs para probar diferentes escenarios:

```bash
for i in {1..150}; do echo "Línea de log $i" >> /opt/airflow/logs/logs.txt; done
```

### Paso 4: Activar y ejecutar el DAG

Sigue los pasos habituales para activar y ejecutar el DAG en la interfaz web de Airflow.

## Ejercicio 2: DAG "datos_meteorologicos"

### Paso 1: Crear el archivo DAG

Crea un nuevo archivo en tu carpeta de `Soluciones` llamado `datos_meteorologicos_dag.py`.

### Paso 2: Escribir el código del DAG

Copia y pega el siguiente código en `datos_meteorologicos_dag.py`:

```python
from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python_operator import PythonOperator, BranchPythonOperator
from airflow.operators.bash_operator import BashOperator
from airflow.models import Variable
import random

# Argumentos por defecto para el DAG
default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime(2023, 1, 1),
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

# Definición del DAG
dag = DAG(
    'datos_meteorologicos',
    default_args=default_args,
    description='DAG para procesar datos meteorológicos',
    schedule_interval=timedelta(days=1),
    catchup=False,
)

# Función para generar datos meteorológicos
def generar_datos(**kwargs):
    ciudades = ['Madrid', 'Barcelona', 'Valencia', 'Sevilla', 'Bilbao']
    datos = {ciudad: {'temperatura': random.uniform(0, 40),
                      'humedad': random.uniform(0, 100),
                      'presion': random.uniform(900, 1100)}
             for ciudad in ciudades}
    kwargs['ti'].xcom_push(key='datos_meteo', value=datos)
    print(f"Datos generados: {datos}")

# Función para transformar datos
def transformar_datos(**kwargs):
    ti = kwargs['ti']
    datos = ti.xcom_pull(key='datos_meteo', task_ids='generar_datos')
    umbral_caliente = float(Variable.get("umbral_temperatura_caliente", default_var=30))
    
    for ciudad, mediciones in datos.items():
        if mediciones['temperatura'] < 10:
            mediciones['clasificacion'] = 'fría'
        elif mediciones['temperatura'] > umbral_caliente:
            mediciones['clasificacion'] = 'caliente'
        else:
            mediciones['clasificacion'] = 'templada'
    
    ti.xcom_push(key='datos_transformados', value=datos)
    print(f"Datos transformados: {datos}")

# Función para decidir si emitir alerta
def decidir_alerta(**kwargs):
    ti = kwargs['ti']
    datos = ti.xcom_pull(key='datos_transformados', task_ids='transformar_datos')
    
    if any(mediciones['clasificacion'] == 'caliente' for mediciones in datos.values()):
        return 'emitir_alerta'
    else:
        return 'sin_alerta'

# Función para generar informe
def generar_informe(**kwargs):
    ti = kwargs['ti']
    datos = ti.xcom_pull(key='datos_transformados', task_ids='transformar_datos')
    
    with open('/opt/airflow/data/informe_meteorologico.txt', 'w') as f:
        for ciudad, mediciones in datos.items():
            f.write(f"Ciudad: {ciudad}\n")
            f.write(f"Temperatura: {mediciones['temperatura']:.2f}°C\n")
            f.write(f"Humedad: {mediciones['humedad']:.2f}%\n")
            f.write(f"Presión: {mediciones['presion']:.2f} hPa\n")
            f.write(f"Clasificación: {mediciones['clasificacion']}\n\n")

# Tareas
generar_datos = PythonOperator(
    task_id='generar_datos',
    python_callable=generar_datos,
    provide_context=True,
    dag=dag,
)

transformar_datos = PythonOperator(
    task_id='transformar_datos',
    python_callable=transformar_datos,
    provide_context=True,
    dag=dag,
)

decidir_alerta = BranchPythonOperator(
    task_id='decidir_alerta',
    python_callable=decidir_alerta,
    provide_context=True,
    dag=dag,
)

emitir_alerta = BashOperator(
    task_id='emitir_alerta',
    bash_command='echo "¡ALERTA! Temperatura alta detectada."',
    dag=dag,
)

sin_alerta = BashOperator(
    task_id='sin_alerta',
    bash_command='echo "Sin alertas de temperatura."',
    dag=dag,
)

generar_informe = PythonOperator(
    task_id='generar_informe',
    python_callable=generar_informe,
    provide_context=True,
    dag=dag,
)

# Definir el orden de las tareas
generar_datos >> transformar_datos >> decidir_alerta >> [emitir_alerta, sin_alerta] >> generar_informe
```

### Paso 3: Configurar variables de Airflow

Configura la variable para el umbral de temperatura:

```bash
airflow variables set umbral_temperatura_caliente 30
```

### Paso 4: Configurar el entorno

Asegúrate de que existe el directorio para los informes:

```bash
mkdir -p /opt/airflow/data
```

### Paso 5: Activar y ejecutar el DAG

Sigue los pasos habituales para activar y ejecutar el DAG en la interfaz web de Airflow.

## Ejercicio 3: DAG "analisis_tweets"

### Paso 1: Crear el archivo DAG

Crea un nuevo archivo en tu carpeta de `Soluciones` llamado `analisis_tweets_dag.py`.

### Paso 2: Escribir el código del DAG

Copia y pega el siguiente código en `analisis_tweets_dag.py`:

```python
from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python_operator import PythonOperator, BranchPythonOperator
from airflow.operators.bash_operator import BashOperator
from airflow.sensors.filesystem import FileSensor
from airflow.models import BaseOperator
from airflow.utils.decorators import apply_defaults
import random

# Argumentos por defecto para el DAG
default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime(2023, 1, 1),
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
    'pool': 'analisis_pool',
}

# Definición del DAG
dag = DAG(
    'analisis_tweets',
    default_args=default_args,
    description='DAG para análisis de sentimientos de tweets',
    schedule_interval=timedelta(days=1),
    catchup=False,
)

# Operador personalizado para análisis de sentimientos
class SentimentAnalyzer(BaseOperator):
    @apply_defaults
    def __init__(self, *args, **kwargs):
        super(SentimentAnalyzer, self).__init__(*args, **kwargs)

    def execute(self, context):
        ti = context['ti']
        tweets = ti.xcom_pull(key='tweets', task_ids='generar_tweets')
        sentimientos = ['positivo', 'negativo', 'neutral']
        analisis = {tweet: random.choice(sentimientos) for tweet in tweets}
        ti.xcom_push(key='analisis', value=analisis)
        print(f"Análisis de sentimientos: {analisis}")

# Función para generar tweets
def generar_tweets(**kwargs):
    tweets = [f"Tweet de prueba número {i}" for i in range(1, 21)]
    kwargs['ti'].xcom_push(key='tweets', value=tweets)
    print(f"Tweets generados: {tweets}")

# Función para clasificar tendencia
def clasificar_tendencia(**kwargs):
    ti = kwargs['ti']
    analisis = ti.xcom_pull(key='analisis', task_ids='analizar_sentimientos')
    positivos = sum(1 for sentiment in analisis.values() if sentiment == 'positivo')
    negativos = sum(1 for sentiment in analisis.values() if sentiment == 'negativo')
    
    if positivos > negativos:
        return 'tendencia_positiva'
    elif negativos > positivos:
        return 'tendencia_negativa'
    else:
        return 'tendencia_neutral'

# Función para generar informe
def generar_informe(**kwargs):
    ti = kwargs['ti']
    analisis = ti.xcom_pull(key='analisis', task_ids='analizar_sentimientos')
    tendencia = ti.xcom_pull(key='tendencia', task_ids=['tendencia_positiva', 'tendencia_negativa', 'tendencia_neutral'])
    
    positivos = sum(1 for sentiment in analisis.values() if sentiment == 'positivo')
    negativos = sum(1 for sentiment in analisis.values() if sentiment == 'negativo')
    neutrales = sum(1 for sentiment in analisis.values() if sentiment == 'neutral')
    
    with open('/opt/airflow/data/informe_tweets.txt', 'w') as f:
        f.write(f"Análisis de sentimientos de tweets\n\n")
        f.write(f"Tweets positivos: {positivos}\n")
        f.write(f"Tweets negativos: {negativos}\n")
        f.write(f"Tweets neutrales: {neutrales}\n")
        f.write(f"\nTendencia general: {tendencia}\n")

# Tareas
generar_tweets = PythonOperator(
    task_id='generar_tweets',
    python_callable=generar_tweets,
    provide_context=True,
    dag=dag,
)

analizar_sentimientos = SentimentAnalyzer(
    task_id='analizar_sentimientos',
    dag=dag,
)

clasificar_tendencia = BranchPythonOperator(
    task_id='clasificar_tendencia',
    python_callable=clasificar_tendencia,
    provide_context=True,
    dag=dag,
)

tendencia_positiva = BashOperator(
    task_id='tendencia_positiva',
    bash_command='echo "Tendencia positiva" > /tmp/tendencia.txt',
    dag=dag,
)

tendencia_negativa = BashOperator(
    task_id='tendencia_negativa',
    bash_command='echo "Tendencia negativa" > /tmp/tendencia.txt',
    dag=dag,
)

tendencia_neutral = BashOperator(
    task_id='tendencia_neutral',
    bash_command='echo "Tendencia neutral" > /tmp/tendencia.txt',
    dag=dag,
)

generar_informe = PythonOperator(
    task_id='generar_informe',
    python_callable=generar_informe,
    provide_context=True,
    dag=dag,
)

esperar_archivo = FileSensor(
    task_id='esperar_archivo',
    filepath='/opt/airflow/data/publicar_informe.txt',
    poke_interval=30,
    timeout=300,
    mode='poke',
    dag=dag,
)

publicar_informe = BashOperator(
    task_id='publicar_informe',
    bash_command='cat /opt/airflow/data/informe_tweets.txt >> /opt/airflow/logs/informe_tweets_publicado.log',
    dag=dag,
)

# Definir el orden de las tareas
generar_tweets >> analizar_sentimientos >> clasificar_tendencia >> [tendencia_positiva, tendencia_negativa, tendencia_neutral] >> generar_informe >> esperar_archivo >> publicar_informe
```

### Paso 3: Configurar el Pool

Crea un nuevo pool llamado "analisis_pool" en la interfaz web de Airflow:

1. Ve a Admin > Pools en la interfaz web de Airflow.
2. Haz clic en "Create".
3. Nombre del pool: "analisis_pool"
4. Número de slots: 5 (o el número que desees para limitar las tareas concurrentes)
5. Descripción: "Pool para tareas de análisis de tweets"
6. Haz clic en "Save".

### Paso 4: Configurar el entorno

1. Asegúrate de que existe el directorio para los informes:

   ```bash
   mkdir -p /opt/airflow/data
   ```

2. Crea un archivo de prueba para simular datos meteorológicos:

  ```bash
  echo "Datos meteorológicos de prueba" > /opt/airflow/data/datos_meteorologicos.txt
  echo "Madrid,25.5,60,1013" >> /opt/airflow/data/datos_meteorologicos.txt
  echo "Barcelona,22.0,70,1015" >> /opt/airflow/data/datos_meteorologicos.txt
  echo "Valencia,28.0,55,1012" >> /opt/airflow/data/datos_meteorologicos.txt
  echo "Sevilla,32.5,45,1010" >> /opt/airflow/data/datos_meteorologicos.txt
  echo "Bilbao,18.5,80,1014" >> /opt/airflow/data/datos_meteorologicos.txt
  ```

3. Crea un archivo de trigger para la publicación del informe:

   ```bash
   touch /opt/airflow/data/publicar_informe.txt
   ```

### Paso 5: Activar y ejecutar el DAG

1. Copia el archivo DAG a la carpeta de DAGs de Airflow:

   ```bash
   cp /workspace/soluciones/analisis_tweets_dag.py /opt/airflow/dags/
   ```

2. En la interfaz web de Airflow, busca el DAG "analisis_tweets" y actívalo.

3. Ejecuta el DAG manualmente o espera a que se ejecute según el schedule_interval definido.

### Paso 6: Monitorear la ejecución

1. En la interfaz web de Airflow, ve a la vista de gráfico del DAG "analisis_tweets".
2. Observa cómo las tareas se ejecutan en el orden definido.
3. Verifica los logs de las tareas para ver los resultados de cada paso.
4. Comprueba el archivo de informe generado en `/opt/airflow/data/informe_tweets.txt`.
5. Verifica el log de informes publicados en `/opt/airflow/logs/informe_tweets_publicado.log`.

Este DAG demuestra el uso de un operador personalizado (SentimentAnalyzer), el uso de XComs para pasar datos entre tareas, el uso de un BranchPythonOperator para flujos condicionales, y la implementación de un FileSensor para esperar una condición externa antes de publicar el informe. Además, utiliza un Pool para limitar el número de tareas de análisis que pueden ejecutarse simultáneamente.
