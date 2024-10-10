# Guía de prueba de Apache Airflow (Ejemplos de ejecución)

## DAG de inicio

### Paso 0: Crear el directorio de DAG

- Ejecuta: `mkdir /opt/airflow/dags`

### Paso 1: Crear el archivo DAG

- Crea un nuevo archivo en tu carpeta de `Soluciones` llamado `prueba_airflow_dag.py`.

### Paso 2: Escribir el código del DAG

Copia y pega el siguiente código en `prueba_airflow_dag.py`:

```python
from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from airflow.operators.bash_operator import BashOperator
from airflow.sensors.filesystem import FileSensor

# Argumentos por defecto para el DAG
default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime(2023, 1, 1),
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

# Definición del DAG
dag = DAG(
    'prueba_airflow',
    default_args=default_args,
    description='Un DAG de prueba para Airflow',
    schedule_interval=timedelta(days=1),
    catchup=False,
)

# Tarea 1: Ejecutar un comando bash
tarea_bash = BashOperator(
    task_id='imprimir_fecha',
    bash_command='date',
    dag=dag,
)

# Tarea 2: Ejecutar una función Python
def saludar():
    print("¡Hola desde Airflow!")

tarea_python = PythonOperator(
    task_id='saludar',
    python_callable=saludar,
    dag=dag,
)

# Tarea 3: Sensor de archivo
sensor_archivo = FileSensor(
    task_id='esperar_archivo',
    filepath='/opt/airflow/dags/archivo_prueba.txt',
    poke_interval=30,
    timeout=300,
    mode='poke',
    dag=dag,
)

# Tarea 4: Tarea condicional
def decidir_ruta(**kwargs):
    import random
    return 'ruta_a' if random.random() < 0.5 else 'ruta_b'

decidir = PythonOperator(
    task_id='decidir_ruta',
    python_callable=decidir_ruta,
    provide_context=True,
    dag=dag,
)

ruta_a = BashOperator(
    task_id='ruta_a',
    bash_command='echo "Tomando ruta A"',
    dag=dag,
)

ruta_b = BashOperator(
    task_id='ruta_b',
    bash_command='echo "Tomando ruta B"',
    dag=dag,
)

# Definir el orden de las tareas
tarea_bash >> tarea_python >> sensor_archivo >> decidir
decidir >> [ruta_a, ruta_b]
```

Copia el fichero a la ruta de DAGs:

```bash
cp -f /workspace/soluciones/prueba_airflow_dag.py /opt/airflow/dags
```

### Paso 3: Crear el archivo de prueba

1. Crea un archivo de texto llamado `archivo_prueba.txt` en el directorio de DAGs:

   ```bash
   touch /opt/airflow/dags/archivo_prueba.txt
   ```

2. Añade algún contenido al archivo:

   ```bash
   echo "Este es un archivo de prueba para el DAG de Airflow" > /opt/airflow/dags/archivo_prueba.txt
   ```

### Paso 4: Activar el DAG

1. Asegúrate de que el webserver y el scheduler de Airflow estén en ejecución.
2. Abre la interfaz web de Airflow en tu navegador (por defecto: <http://localhost:8080>).
3. Busca el DAG llamado 'prueba_airflow' en la lista de DAGs.
4. Activa el DAG usando el interruptor en la interfaz web.

### Paso 5: Ejecutar el DAG

1. Una vez activado, el DAG comenzará a ejecutarse según el `schedule_interval` definido (diariamente en este caso).
2. Para ejecutar el DAG manualmente:
   - En la interfaz web, haz clic en el botón de "play" junto al DAG.
   - O usa el comando CLI:
  
     ```bash
     airflow dags trigger prueba_airflow
     ```

### Paso 6: Monitorear la ejecución

1. En la interfaz web de Airflow, haz clic en el nombre del DAG 'prueba_airflow'.
2. Navega a la vista "Graph" para ver el progreso de las tareas.
3. Haz clic en las tareas individuales para ver sus logs y estados.

### Explicación del DAG

Este DAG de prueba incluye varias tareas que demuestran algunas funcionalidades básicas de Airflow:

1. **tarea_bash**: Ejecuta un comando bash simple para imprimir la fecha actual.
2. **tarea_python**: Ejecuta una función Python simple que imprime un saludo.
3. **sensor_archivo**: Espera la existencia del archivo 'archivo_prueba.txt' antes de continuar.
4. **decidir**: Elige aleatoriamente entre dos rutas de ejecución.
5. **ruta_a** y **ruta_b**: Dos tareas simples que se ejecutan dependiendo de la decisión tomada en la tarea anterior.

El orden de las tareas está definido al final del script, mostrando cómo se pueden establecer dependencias entre tareas en Airflow.

## DAG de simulación ETL

### Paso 1: Crear el archivo DAG

- Crea un nuevo archivo en tu carpeta de `Soluciones` llamado `etl_simulacion_dag.py`.

### Paso 2: Escribir el código del DAG

Copia y pega el siguiente código en `etl_simulacion_dag.py`:

```python
from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python_operator import PythonOperator, BranchPythonOperator
from airflow.operators.bash_operator import BashOperator
from airflow.operators.dummy_operator import DummyOperator
from airflow.sensors.filesystem import FileSensor
from airflow.hooks.postgres_hook import PostgresHook
from airflow.models import Variable
from airflow.utils.trigger_rule import TriggerRule

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
    'etl_simulacion',
    default_args=default_args,
    description='Un DAG de simulación ETL más avanzado',
    schedule_interval=timedelta(days=1),
    catchup=False,
)

# Función para extraer datos
def extraer_datos(**kwargs):
    # Simulamos la extracción de datos
    datos = [
        {'id': 1, 'nombre': 'Juan', 'edad': 30},
        {'id': 2, 'nombre': 'María', 'edad': 25},
        {'id': 3, 'nombre': 'Pedro', 'edad': 35}
    ]
    kwargs['ti'].xcom_push(key='datos_extraidos', value=datos)
    print(f"Datos extraídos: {datos}")

# Función para transformar datos
def transformar_datos(**kwargs):
    ti = kwargs['ti']
    datos = ti.xcom_pull(key='datos_extraidos', task_ids='extraer_datos')
    
    # Simulamos una transformación simple
    datos_transformados = [
        {**dato, 'edad_en_meses': dato['edad'] * 12}
        for dato in datos
    ]
    ti.xcom_push(key='datos_transformados', value=datos_transformados)
    print(f"Datos transformados: {datos_transformados}")

# Función para cargar datos
def cargar_datos(**kwargs):
    ti = kwargs['ti']
    datos = ti.xcom_pull(key='datos_transformados', task_ids='transformar_datos')
    
    # Simulamos la carga de datos en una base de datos
    # En un escenario real, usaríamos un hook de base de datos aquí
    print(f"Cargando datos en la base de datos: {datos}")
    # Simulamos el conteo de registros cargados
    Variable.set("registros_cargados", len(datos))

# Función para verificar la carga
def verificar_carga(**kwargs):
    registros_cargados = int(Variable.get("registros_cargados", default_var=0))
    if registros_cargados > 2:
        return 'carga_exitosa'
    else:
        return 'carga_fallida'

# Tareas
inicio = DummyOperator(task_id='inicio', dag=dag)

esperar_archivo = FileSensor(
    task_id='esperar_archivo',
    filepath='/opt/airflow/dags/datos_entrada.csv',
    poke_interval=30,
    timeout=300,
    mode='poke',
    dag=dag,
)

extraer = PythonOperator(
    task_id='extraer_datos',
    python_callable=extraer_datos,
    provide_context=True,
    dag=dag,
)

transformar = PythonOperator(
    task_id='transformar_datos',
    python_callable=transformar_datos,
    provide_context=True,
    dag=dag,
)

cargar = PythonOperator(
    task_id='cargar_datos',
    python_callable=cargar_datos,
    provide_context=True,
    dag=dag,
)

verificar = BranchPythonOperator(
    task_id='verificar_carga',
    python_callable=verificar_carga,
    provide_context=True,
    dag=dag,
)

carga_exitosa = BashOperator(
    task_id='carga_exitosa',
    bash_command='echo "Carga exitosa, enviando notificación..."',
    dag=dag,
)

carga_fallida = BashOperator(
    task_id='carga_fallida',
    bash_command='echo "Carga fallida, iniciando proceso de recuperación..."',
    dag=dag,
)

fin = DummyOperator(
    task_id='fin',
    trigger_rule=TriggerRule.ONE_SUCCESS,
    dag=dag,
)

# Definir el orden de las tareas
inicio >> esperar_archivo >> extraer >> transformar >> cargar >> verificar
verificar >> [carga_exitosa, carga_fallida] >> fin
```

Copia el fichero a la ruta de DAGs:

```bash
cp -f /workspace/soluciones/etl_simulacion_dag.py /opt/airflow/dags
```

### Paso 3: Crear archivos auxiliares

1. Crea un archivo CSV vacío llamado `datos_entrada.csv` en el directorio de DAGs:

   ```bash
   touch /opt/airflow/dags/datos_entrada.csv
   ```

2. Añade algún contenido al archivo:

   ```bash
   echo "id,nombre,edad" > /opt/airflow/dags/datos_entrada.csv
   echo "1,Juan,30" >> /opt/airflow/dags/datos_entrada.csv
   echo "2,María,25" >> /opt/airflow/dags/datos_entrada.csv
   echo "3,Pedro,35" >> /opt/airflow/dags/datos_entrada.csv
   ```

### Paso 4: Configurar variables de Airflow

Airflow utiliza variables para almacenar y recuperar valores. Vamos a configurar una variable que usaremos en nuestro DAG:

```bash
airflow variables set registros_cargados 0
```

### Paso 5: Activar el DAG

1. Asegúrate de que el webserver y el scheduler de Airflow estén en ejecución.
2. Abre la interfaz web de Airflow en tu navegador (por defecto: <http://localhost:8080>).
3. Busca el DAG llamado 'etl_simulacion' en la lista de DAGs.
4. Activa el DAG usando el interruptor en la interfaz web.

### Paso 6: Ejecutar el DAG

1. Una vez activado, el DAG comenzará a ejecutarse según el `schedule_interval` definido (diariamente en este caso).
2. Para ejecutar el DAG manualmente:
   - En la interfaz web, haz clic en el botón de "play" junto al DAG.
   - O usa el comando CLI:
  
     ```bash
     airflow dags trigger etl_simulacion
     ```

### Paso 7: Monitorear la ejecución

1. En la interfaz web de Airflow, haz clic en el nombre del DAG 'etl_simulacion'.
2. Navega a la vista "Graph" para ver el progreso de las tareas.
3. Haz clic en las tareas individuales para ver sus logs y estados.

### Explicación del DAG

Este DAG de simulación ETL incluye varias tareas que demuestran funcionalidades más avanzadas de Airflow:

1. **inicio**: Una tarea dummy que marca el inicio del flujo de trabajo.
2. **esperar_archivo**: Un sensor que espera la existencia del archivo 'datos_entrada.csv'.
3. **extraer_datos**: Simula la extracción de datos y los almacena usando XComs.
4. **transformar_datos**: Realiza una transformación simple en los datos extraídos.
5. **cargar_datos**: Simula la carga de datos y establece una variable de Airflow.
6. **verificar_carga**: Utiliza branching para decidir el siguiente paso basado en el número de registros cargados.
7. **carga_exitosa** y **carga_fallida**: Tareas que se ejecutan dependiendo del resultado de la verificación.
8. **fin**: Una tarea dummy que marca el final del flujo de trabajo.

El orden de las tareas está definido al final del script, mostrando cómo se pueden establecer dependencias complejas entre tareas en Airflow.

Este DAG demuestra el uso de XComs para pasar datos entre tareas, variables de Airflow para almacenar estado, branching para flujos de trabajo condicionales, y sensores para esperar condiciones externas.
