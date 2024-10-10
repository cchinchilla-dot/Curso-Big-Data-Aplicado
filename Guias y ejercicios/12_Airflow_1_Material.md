# Guía avanzada de Apache Airflow

En relación con los contenidos del curso, esta guía se corresponde con:

- Módulo 2:
  - Otras herramientas: Como alternativa a Oozie para la aplicación práctica.
  - Automatización de Jobs.
  
## 1. Introducción a Apache Airflow

Apache Airflow es una plataforma de código abierto para programar, monitorear y orquestar flujos de trabajo complejos. Desarrollado originalmente por Airbnb, Airflow permite a los usuarios definir sus flujos de trabajo como código, siguiendo el principio de "Configuración como Código".

### Conceptos clave

1. **DAG (Directed Acyclic Graph)**: Representa un flujo de trabajo en Airflow.
2. **Operadores**: Definen una unidad de trabajo dentro de un DAG.
3. **Tasks**: Instancias parametrizadas de operadores.
4. **Workflows**: Secuencia de tareas organizadas para lograr un resultado.

### Características principales

- Escalabilidad: Puede manejar un gran número de DAGs y tareas.
- Dinamismo: Los DAGs pueden ser generados dinámicamente.
- Extensibilidad: Fácil de extender con plugins y librerías personalizadas.
- UI intuitiva: Interfaz web para monitoreo y administración.
- Múltiples integraciones: Se conecta fácilmente con diversas bases de datos y sistemas.

## 2. Ejecución de Airflow

Airflow puede ejecutarse de varias maneras, dependiendo de las necesidades y el entorno. Aquí se describen los métodos más comunes:

### 2.1 Ejecución local

Para ejecutar Airflow localmente, sigue estos pasos:

1. Instala Airflow:

   ```bash
   pip install apache-airflow
   ```

2. Inicializa la base de datos de Airflow:

   ```bash
   airflow db init
   ```

3. Crea un usuario (solo la primera vez):

   ```bash
   airflow users create --username admin --firstname FIRST_NAME --lastname LAST_NAME --role Admin --email admin@example.com
   ```

4. Inicia el webserver:

   ```bash
   airflow webserver --port 8080
   ```

5. En una nueva terminal, inicia el scheduler:

   ```bash
   airflow scheduler
   ```

Ahora puedes acceder a la interfaz web de Airflow en `http://localhost:8080`.

### 2.2 Ejecución con Docker

Para entornos más complejos o para desarrollo, puedes usar Docker:

1. Crea un archivo `docker-compose.yaml` con la configuración de Airflow.

2. Inicia los servicios:

   ```bash
   docker-compose up -d
   ```

3. Accede a la interfaz web en `http://localhost:8080`.

**Nota importante:** En el entorno de pruebas de este curso, Airflow ya está instalado, inicializado, con un usuario creado, el webserver en funcionamiento y un scheduler de prueba configurado y activo.

### 2.3 Ejecución en un cluster

Para producción, Airflow puede ejecutarse en un cluster de Kubernetes:

1. Usa Helm para instalar Airflow en tu cluster:

   ```bash
   helm repo add apache-airflow https://airflow.apache.org
   helm install airflow apache-airflow/airflow
   ```

2. Configura el acceso a la interfaz web según tu configuración de red.

## 3. Uso de comandos en Airflow

Airflow proporciona una CLI (Interfaz de Línea de Comandos) para diversas operaciones:

### 3.1 Gestión de DAGs

- Listar DAGs:
  
  ```bash
  airflow dags list
  ```

- Pausar un DAG:
  
  ```bash
  airflow dags pause <dag_id>
  ```

- Reanudar un DAG:
  
  ```bash
  airflow dags unpause <dag_id>
  ```

### 3.2 Ejecución de tareas

- Ejecutar una tarea:
  
  ```bash
  airflow tasks run <dag_id> <task_id> <execution_date>
  ```

- Probar una tarea:
  
  ```bash
  airflow tasks test <dag_id> <task_id> <execution_date>
  ```

### 3.3 Administración

- Inicializar la base de datos:

  ```bash
  airflow db init
  ```

- Actualizar la base de datos:
  
  ```bash
  airflow db upgrade
  ```

## 4. Interfaz gráfica de Airflow

UI / Screenshots: <https://airflow.apache.org/docs/apache-airflow/stable/ui.html>

Airflow proporciona una interfaz web intuitiva para gestionar y monitorear flujos de trabajo. A continuación, se describen las principales vistas y características de la interfaz de usuario de Airflow:

### 4.1 DAGs View

![DAGs View](https://airflow.apache.org/docs/apache-airflow/stable/_images/dags.png)

- Muestra una lista de todos los DAGs en tu entorno.
- Proporciona accesos directos a páginas útiles.
- Ofrece una vista rápida del estado de las tareas: cuántas han tenido éxito, han fallado o están en ejecución.
- Permite filtrar DAGs por etiquetas, útil para organizar por equipos o tipos de flujos de trabajo.

### 4.2 Cluster Activity View

![Cluster Activity View](https://airflow.apache.org/docs/apache-airflow/stable/_images/cluster_activity.png)

- Página de dashboard nativa de Airflow que recopila métricas útiles para monitorear tu clúster.
- Proporciona una visión general del estado y rendimiento del sistema Airflow.

### 4.3 Datasets View

![Datasets View](https://airflow.apache.org/docs/apache-airflow/stable/_images/datasets.png)

- Muestra una lista combinada de los datasets actuales y un gráfico que ilustra cómo son producidos y consumidos por los DAGs.
- Permite hacer clic en cualquier dataset para resaltarlo y sus relaciones.
- Filtra la lista para mostrar el historial reciente de instancias de tareas que han actualizado ese dataset.

### 4.4 Grid View

![Grid View](https://airflow.apache.org/docs/apache-airflow/stable/_images/grid.png)

- Presenta un gráfico de barras y una representación en cuadrícula del DAG a lo largo del tiempo.
- La fila superior muestra un gráfico de ejecuciones de DAG por duración.
- Debajo se muestran las instancias de tareas.
- Permite identificar rápidamente cuellos de botella en caso de retrasos en el pipeline.
- Ofrece paneles de detalles al seleccionar ejecuciones de DAG o instancias de tareas.
- Indica ejecuciones manuales con un icono de reproducción y ejecuciones activadas por datasets con un icono de base de datos.
- Muestra grupos de tareas que pueden expandirse o contraerse.
- Indica tareas mapeadas y proporciona detalles en un panel separado.

![Graph View](https://airflow.apache.org/docs/apache-airflow/stable/_images/grid.png)

- Visualiza las dependencias del DAG y su estado actual para una ejecución específica.
- Proporciona una representación visual completa de la estructura y el flujo del DAG.

El panel de detalles se actualizará al seleccionar una ejecución DAG haciendo clic en una barra de duración:

![Graph View](https://airflow.apache.org/docs/apache-airflow/stable/_images/grid_run_details.png)

O seleccionando una instancia de tarea haciendo clic en un cuadro de estado:

![Graph View](https://airflow.apache.org/docs/apache-airflow/stable/_images/grid_instance_details.png)

O seleccionando una tarea en todas las ejecuciones haciendo clic en task_id:

![Graph View](https://airflow.apache.org/docs/apache-airflow/stable/_images/grid_task_details.png)

Las ejecuciones manuales se indican con un ícono de reproducción (al igual que el botón Trigger DAG). Las ejecuciones activadas por el conjunto de datos se indican mediante un icono de base de datos:

![Graph View](https://airflow.apache.org/docs/apache-airflow/stable/_images/run_types.png)

Los grupos de tareas se indican con un signo de intercalación y se pueden abrir o cerrar:

![Graph View](https://airflow.apache.org/docs/apache-airflow/stable/_images/grid_task_group.png)

Las tareas asignadas se indican entre corchetes y mostrarán una tabla de cada instancia de tarea asignada en el panel Tareas asignadas:

![Graph View](https://airflow.apache.org/docs/apache-airflow/stable/_images/grid_mapped_task.png)

### 4.5 Graph View

![Graph View](https://airflow.apache.org/docs/apache-airflow/stable/_images/graph.png)

- Visualiza las dependencias del DAG y su estado actual para una ejecución específica.
- Proporciona una representación visual completa de la estructura y el flujo del DAG.

### 4.6 Calendar View

![Calendar View](https://airflow.apache.org/docs/apache-airflow/stable/_images/calendar.png)

- Ofrece una visión general del historial completo del DAG a lo largo de meses o años.
- Permite identificar tendencias en la tasa de éxito/fracaso de las ejecuciones a lo largo del tiempo.

### 4.7 Variable View

![Variable View](https://airflow.apache.org/docs/apache-airflow/stable/_images/variable_hidden.png)

- Permite listar, crear, editar o eliminar pares clave-valor de variables utilizadas durante los trabajos.
- Oculta por defecto los valores de variables que contienen palabras clave sensibles como "password" o "api_key".

### 4.8 Gantt Chart

![Gantt Chart](https://airflow.apache.org/docs/apache-airflow/stable/_images/gantt.png)

- Permite analizar la duración y superposición de tareas.
- Ayuda a identificar cuellos de botella y dónde se invierte la mayor parte del tiempo en ejecuciones específicas de DAG.

### 4.9 Task Duration

![Task Duration](https://airflow.apache.org/docs/apache-airflow/stable/_images/duration.png)

- Muestra la duración de las diferentes tareas a lo largo de las últimas N ejecuciones.
- Facilita la identificación de valores atípicos y la comprensión de dónde se invierte el tiempo en el DAG a lo largo de múltiples ejecuciones.

### 4.10 Landing Times

![Landing Times](https://airflow.apache.org/docs/apache-airflow/stable/_images/landing_times.png)

- Visualiza el tiempo de aterrizaje para las instancias de tareas.
- El tiempo de aterrizaje es la diferencia entre el final del intervalo de datos de la ejecución del DAG y el tiempo de finalización de la ejecución.

### 4.11 Code View

![Code View](https://airflow.apache.org/docs/apache-airflow/stable/_images/code.png)

- Proporciona acceso rápido al código que genera el DAG.
- Ofrece contexto adicional y transparencia sobre la definición del flujo de trabajo.

### 4.12 Trigger Form

![Trigger Form](https://airflow.apache.org/docs/apache-airflow/stable/_images/trigger-dag-tutorial-form.png)

- Se muestra al activar una ejecución manual de DAG.
- La visualización del formulario se basa en los Parámetros del DAG.

### 4.13 Audit Log

![Audit Log](https://airflow.apache.org/docs/apache-airflow/stable/_images/audit_log.png)

- Permite ver todos los eventos relacionados con un DAG.
- Ofrece la posibilidad de filtrar eventos cambiando la selección de Tarea y Ejecución de DAG.
- Permite incluir/excluir diferentes nombres de eventos.

## 5. Creación de DAGs

Los DAGs (Directed Acyclic Graphs) son el núcleo de Airflow. Representan un flujo de trabajo completo y definen las tareas y sus dependencias. Vamos a profundizar en su creación, estructura y funcionalidades avanzadas.

### 5.1 Estructura básica de un DAG

Un DAG en Airflow se define utilizando Python. La estructura básica de un DAG incluye su definición, las tareas que lo componen y las dependencias entre estas tareas. Veamos un ejemplo detallado:

```python
from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from airflow.operators.bash_operator import BashOperator
from datetime import datetime, timedelta

# Argumentos por defecto para el DAG
default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime(2023, 1, 1),
    'email': ['airflow@example.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

# Definición del DAG
dag = DAG(
    'ejemplo_complejo',
    default_args=default_args,
    description='Un DAG de ejemplo más complejo',
    schedule_interval=timedelta(days=1),
    catchup=False,
    tags=['ejemplo', 'tutorial'],
)

# Definición de funciones para tareas
def tarea_python():
    print("Esta es una tarea de Python")
    return "Tarea completada"

# Definición de tareas
tarea_1 = BashOperator(
    task_id='tarea_bash',
    bash_command='echo "Hola desde Bash"',
    dag=dag,
)

tarea_2 = PythonOperator(
    task_id='tarea_python',
    python_callable=tarea_python,
    dag=dag,
)

tarea_3 = BashOperator(
    task_id='tarea_final',
    bash_command='echo "Tarea final ejecutada"',
    dag=dag,
)

# Definición de dependencias
tarea_1 >> tarea_2 >> tarea_3
```

Explicación detallada:

1. **Importaciones**: Importamos las clases y funciones necesarias de Airflow y otros módulos Python.

2. **Argumentos por defecto**: Definimos un diccionario `default_args` que contiene configuraciones comunes para todas las tareas del DAG. Esto incluye:
   - `owner`: El propietario del DAG.
   - `depends_on_past`: Si las ejecuciones del DAG dependen del éxito de ejecuciones anteriores.
   - `start_date`: La fecha de inicio desde la cual el DAG comenzará a ejecutarse.
   - `email`: Direcciones de correo electrónico para notificaciones.
   - `retries`: Número de intentos si una tarea falla.
   - `retry_delay`: Tiempo de espera entre reintentos.

3. **Definición del DAG**: Creamos una instancia de DAG con los siguientes parámetros:
   - `dag_id`: Un identificador único para el DAG.
   - `default_args`: Los argumentos por defecto definidos anteriormente.
   - `description`: Una descripción del propósito del DAG.
   - `schedule_interval`: Con qué frecuencia se ejecutará el DAG.
   - `catchup`: Si Airflow debe ejecutar las ejecuciones atrasadas del DAG.
   - `tags`: Etiquetas para categorizar el DAG.

4. **Definición de funciones**: Definimos funciones Python que serán utilizadas por las tareas.

5. **Definición de tareas**: Creamos instancias de operadores que representan las tareas individuales del DAG. En este ejemplo, usamos:
   - `BashOperator`: Para ejecutar comandos Bash.
   - `PythonOperator`: Para ejecutar funciones Python.

6. **Definición de dependencias**: Establecemos el orden de ejecución de las tareas utilizando el operador de bit shift (`>>`).

### 5.2 Operadores comunes

Airflow proporciona una amplia variedad de operadores para diferentes tipos de tareas. Cada operador está diseñado para realizar un tipo específico de operación. Aquí tienes una explicación de algunos operadores comunes:

1. **PythonOperator**:
   - Ejecuta una función Python.
   - Útil para tareas personalizadas y lógica compleja.
  
     ```python
     def mi_funcion():
         return "Hola desde Python"

     tarea_python = PythonOperator(
         task_id='tarea_python',
         python_callable=mi_funcion,
         dag=dag,
     )
     ```

2. **BashOperator**:
   - Ejecuta un comando Bash.
   - Ideal para scripts de shell o comandos del sistema.
  
     ```python
     tarea_bash = BashOperator(
         task_id='tarea_bash',
         bash_command='echo "Hola desde Bash"',
         dag=dag,
     )
     ```

3. **SqlOperator**:
   - Ejecuta consultas SQL en una base de datos.
   - Requiere una conexión de base de datos configurada en Airflow.
  
     ```python
     tarea_sql = SqlOperator(
         task_id='tarea_sql',
         sql='SELECT * FROM mi_tabla',
         conn_id='mi_conexion_db',
         dag=dag,
     )
     ```

4. **EmailOperator**:
   - Envía correos electrónicos.
   - Útil para notificaciones y alertas.
  
     ```python
     tarea_email = EmailOperator(
         task_id='tarea_email',
         to='destinatario@ejemplo.com',
         subject='Asunto del correo',
         html_content='<p>Contenido del correo</p>',
         dag=dag,
     )
     ```

5. **HTTPOperator**:
   - Realiza solicitudes HTTP.
   - Útil para interactuar con APIs REST.
  
     ```python
     tarea_http = HTTPOperator(
         task_id='tarea_http',
         http_conn_id='mi_conexion_api',
         endpoint='/api/data',
         method='GET',
         dag=dag,
     )
     ```

6. **DockerOperator**:
   - Ejecuta un contenedor Docker.
   - Ideal para tareas que requieren un entorno aislado.
  
     ```python
     tarea_docker = DockerOperator(
         task_id='tarea_docker',
         image='mi_imagen:latest',
         command='/scripts/mi_script.sh',
         dag=dag,
     )
     ```

7. **SparkSubmitOperator**:
   - Envía trabajos a un cluster Spark.
   - Útil para procesamiento de big data.
  
     ```python
     tarea_spark = SparkSubmitOperator(
         task_id='tarea_spark',
         application='/ruta/a/mi_job_spark.py',
         conn_id='spark_default',
         dag=dag,
     )
     ```

### 5.3 Dependencias y control de flujo

Las dependencias entre tareas definen el orden de ejecución en un DAG. Airflow ofrece varias formas de establecer estas dependencias:

1. **Usando operadores bitwise**:
   - El operador `>>` indica que la tarea de la izquierda debe ejecutarse antes que la de la derecha.
   - El operador `<<` indica lo contrario.
  
     ```python
     tarea_1 >> tarea_2 >> tarea_3
     # Equivalente a:
     tarea_3 << tarea_2 << tarea_1
     ```

2. **Usando métodos set_upstream/set_downstream**:
   - `set_downstream()` establece que una tarea debe ejecutarse después de la actual.
   - `set_upstream()` establece que una tarea debe ejecutarse antes de la actual.

     ```python
     tarea_1.set_downstream(tarea_2)
     tarea_3.set_upstream(tarea_2)
     ```

3. **Usando listas**:
   - Permite establecer dependencias múltiples de manera concisa.

     ```python
     [tarea_1, tarea_2] >> tarea_3 >> [tarea_4, tarea_5]
     ```

### 5.4 Branching y control condicional

El branching en Airflow permite implementar lógica condicional en los DAGs, permitiendo que diferentes caminos de ejecución se tomen basados en condiciones específicas.

```python
from airflow.operators.python_operator import BranchPythonOperator

def branch_func(**kwargs):
    if kwargs['execution_date'].day == 1:
        return 'tarea_principio_mes'
    else:
        return 'tarea_resto_mes'

branch_operator = BranchPythonOperator(
    task_id='branch_task',
    python_callable=branch_func,
    provide_context=True,
    dag=dag,
)

tarea_principio_mes = BashOperator(
    task_id='tarea_principio_mes',
    bash_command='echo "Es principio de mes"',
    dag=dag,
)

tarea_resto_mes = BashOperator(
    task_id='tarea_resto_mes',
    bash_command='echo "No es principio de mes"',
    dag=dag,
)

branch_operator >> [tarea_principio_mes, tarea_resto_mes]
```

En este ejemplo:

1. Definimos una función `branch_func` que decide qué tarea ejecutar basándose en la fecha de ejecución.
2. Usamos `BranchPythonOperator` para implementar la lógica de branching.
3. Definimos dos tareas diferentes para cada camino posible.
4. Establecemos las dependencias de manera que el `branch_operator` decida cuál de las dos tareas se ejecutará.

Este patrón es útil para crear flujos de trabajo dinámicos que se adaptan a diferentes condiciones o escenarios.

### 5.5 Templating con Jinja

Airflow utiliza el motor de plantillas Jinja2 para permitir la parametrización dinámica de configuraciones y comandos en las tareas. Esto es especialmente útil para incluir información de tiempo de ejecución o variables en tus tareas.

```python
templated_command = """
    echo "La fecha de ejecución es {{ ds }}"
    echo "El siguiente parámetro es {{ params.my_param }}"
    echo "El año actual es {{ macros.datetime.now().year }}"
"""

tarea_templated = BashOperator(
    task_id='tarea_templated',
    bash_command=templated_command,
    params={'my_param': 'valor_parametro'},
    dag=dag,
)
```

En este ejemplo:

1. Usamos delimitadores `{{ }}` para incluir expresiones Jinja en nuestro comando.
2. `{{ ds }}` es una variable de contexto que representa la fecha de ejecución.
3. `{{ params.my_param }}` accede a un parámetro personalizado definido en la tarea.
4. `{{ macros.datetime.now().year }}` utiliza las macros de Airflow para obtener el año actual.

Airflow proporciona numerosas variables de contexto y macros que puedes utilizar en tus plantillas, permitiendo una gran flexibilidad en la definición de tus tareas.

### 5.6 XComs (Cross-communication)

XComs (Cross-communication) en Airflow permite que las tareas intercambien pequeñas cantidades de datos. Es útil cuando necesitas pasar resultados o información entre diferentes tareas de un DAG.

```python
def push_xcom(**kwargs):
    kwargs['ti'].xcom_push(key='mi_valor', value=42)

def pull_xcom(**kwargs):
    valor = kwargs['ti'].xcom_pull(key='mi_valor', task_ids='tarea_push')
    print(f"El valor recuperado es: {valor}")

tarea_push = PythonOperator(
    task_id='tarea_push',
    python_callable=push_xcom,
    provide_context=True,
    dag=dag,
)

tarea_pull = PythonOperator(
    task_id='tarea_pull',
    python_callable=pull_xcom,
    provide_context=True,
    dag=dag,
)

tarea_push >> tarea_pull
```

Explicación detallada:

1. **Push XCom**:
   - La función `push_xcom` utiliza `xcom_push` para almacenar un valor en XCom.
   - `ti` es la instancia de tarea (Task Instance) proporcionada por Airflow.
   - Guardamos el valor 42 con la clave 'mi_valor'.

2. **Pull XCom**:
   - La función `pull_xcom` utiliza `xcom_pull` para recuperar el valor almacenado.
   - Especificamos la clave y el ID de la tarea de la que queremos recuperar el valor.

3. **Configuración de tareas**:
   - Creamos dos `PythonOperator`, uno para push y otro para pull.
   - `provide_context=True` asegura que Airflow pase el contexto de ejecución a nuestras funciones.

4. **Dependencias**:
   - Establecemos que `tarea_pull` se ejecute después de `tarea_push`.

XComs es especialmente útil para escenarios donde necesitas pasar información entre tareas, como resultados de cálculos, nombres de archivos generados, o cualquier otro dato pequeño. Sin embargo, es importante no abusar de XComs para grandes cantidades de datos, ya que está diseñado para pequeñas comunicaciones entre tareas.

## 6. Automatización de Jobs

La automatización de jobs es una de las funcionalidades principales y más poderosas de Apache Airflow. Esta capacidad permite a los usuarios definir, programar y ejecutar flujos de trabajo complejos de manera eficiente y confiable. En esta sección, exploraremos en detalle las características avanzadas de automatización de jobs en Airflow.

### 6.1 Programación de DAGs

Airflow ofrece múltiples métodos para programar la ejecución de DAGs, permitiendo una gran flexibilidad en la definición de cuándo y con qué frecuencia se deben ejecutar los flujos de trabajo.

#### 6.1.1 Intervalo fijo

El método más simple es usar un intervalo fijo mediante el parámetro `schedule_interval` en la definición del DAG.

```python
from datetime import timedelta

dag = DAG(
    'dag_intervalo_fijo',
    schedule_interval=timedelta(days=1),  # Se ejecuta diariamente
    start_date=datetime(2023, 1, 1),
    catchup=False,
)
```

En este ejemplo, el DAG se ejecutará cada día a partir de la `start_date` especificada.

#### 6.1.2 Expresiones Cron

Para programaciones más complejas, Airflow soporta expresiones cron, que ofrecen una gran flexibilidad en la definición de horarios.

```python
dag = DAG(
    'dag_programado_cron',
    schedule_interval='0 0 * * MON-FRI',  # Se ejecuta a las 00:00 de lunes a viernes
    start_date=datetime(2023, 1, 1),
    catchup=False,
)
```

Explicación de la expresión cron '0 0 ** MON-FRI':

- `0`: Minuto (0-59)
- `0`: Hora (0-23)
- `*`: Día del mes (1-31)
- `*`: Mes (1-12)
- `MON-FRI`: Día de la semana (0-7, donde tanto 0 como 7 representan el domingo)

#### 6.1.3 Programación dinámica

La programación dinámica en Airflow permite implementar lógicas de programación personalizadas y complejas que van más allá de los intervalos fijos o las expresiones cron. Esta característica es útil cuando necesitas programar DAGs basándote en reglas de negocio específicas o en condiciones que cambian dinámicamente.

```python
from airflow.timetables.base import DagRunInfo, DataInterval, TimeRestriction
from airflow.timetables.simple import DataIntervalTimetable
from pendulum import DateTime, Duration

class CustomTimetable(DataIntervalTimetable):
    def next_dagrun_info(
        self,
        *,
        last_automated_data_interval: DataInterval | None,
        restriction: TimeRestriction,
    ) -> DagRunInfo | None:
        if last_automated_data_interval is None:
            next_start = restriction.earliest
        else:
            next_start = last_automated_data_interval.end

        # Lógica personalizada para determinar el próximo tiempo de ejecución
        if next_start.day_of_week == 6:  # Si es sábado
            next_start = next_start.add(days=2)  # Salta al lunes
        elif next_start.day_of_week == 0:  # Si es domingo
            next_start = next_start.add(days=1)  # Salta al lunes

        return DagRunInfo(
            data_interval=DataInterval(start=next_start, end=next_start + Duration(days=1)),
            run_after=next_start + Duration(days=1),
        )

dag = DAG(
    'dag_programacion_dinamica',
    timetable=CustomTimetable(),
    start_date=datetime(2023, 1, 1),
    catchup=False,
)
```

En este ejemplo, hemos creado una programación personalizada que evita ejecutar el DAG los fines de semana.

Explicación detallada:

```python
from airflow.timetables.base import DagRunInfo, DataInterval, TimeRestriction
from airflow.timetables.simple import DataIntervalTimetable
from pendulum import DateTime, Duration
```

Estas líneas importan las clases necesarias de Airflow y la biblioteca Pendulum para el manejo de fechas y tiempos.

```python
class CustomTimetable(DataIntervalTimetable):
```

Aquí se define una clase personalizada `CustomTimetable` que hereda de `DataIntervalTimetable`. Esta clase implementará nuestra lógica de programación personalizada.

```python
    def next_dagrun_info(
        self,
        *,
        last_automated_data_interval: DataInterval | None,
        restriction: TimeRestriction,
    ) -> DagRunInfo | None:
```

Este método es el corazón de nuestra programación personalizada. Se llama para determinar cuándo debe ocurrir la próxima ejecución del DAG. Los parámetros son:

- `last_automated_data_interval`: El intervalo de datos de la última ejecución automatizada del DAG.
- `restriction`: Restricciones de tiempo para la programación del DAG.

```python
        if last_automated_data_interval is None:
            next_start = restriction.earliest
        else:
            next_start = last_automated_data_interval.end
```

Aquí se determina el punto de inicio para calcular la próxima ejecución. Si es la primera ejecución (`last_automated_data_interval` es None), se usa la fecha más temprana permitida. De lo contrario, se usa el final del último intervalo de datos.

```python
        # Lógica personalizada para determinar el próximo tiempo de ejecución
        if next_start.day_of_week == 6:  # Si es sábado
            next_start = next_start.add(days=2)  # Salta al lunes
        elif next_start.day_of_week == 0:  # Si es domingo
            next_start = next_start.add(days=1)  # Salta al lunes
```

Esta es la lógica personalizada que evita las ejecuciones en fin de semana. Si el próximo inicio cae en sábado, se mueve dos días adelante (al lunes). Si cae en domingo, se mueve un día adelante (también al lunes).

```python
        return DagRunInfo(
            data_interval=DataInterval(start=next_start, end=next_start + Duration(days=1)),
            run_after=next_start + Duration(days=1),
        )
```

Finalmente, se devuelve un objeto `DagRunInfo` que especifica:

- El intervalo de datos para la próxima ejecución (`data_interval`).
- Cuándo debe ejecutarse el DAG (`run_after`), que en este caso es un día después del inicio del intervalo de datos.

```python
dag = DAG(
    'dag_programacion_dinamica',
    timetable=CustomTimetable(),
    start_date=datetime(2023, 1, 1),
    catchup=False,
)
```

Aquí se crea el DAG utilizando nuestra programación personalizada. Se especifica:

- El ID del DAG.
- Se usa `CustomTimetable()` como el programador del DAG.
- Una fecha de inicio.
- `catchup=False` para evitar que Airflow intente ejecutar el DAG para intervalos pasados.

Esta implementación de programación dinámica permite una gran flexibilidad en la definición de cuándo se deben ejecutar los DAGs. Puedes adaptar la lógica dentro de `next_dagrun_info` para implementar casi cualquier regla de programación que necesites, como:

- Ejecutar en días específicos del mes.
- Ajustar la programación basándose en feriados o eventos especiales.
- Implementar intervalos de ejecución variables.
- Coordinar la ejecución con eventos externos o la disponibilidad de datos.

### 6.2 Sensores

Los sensores son un tipo especial de operador en Airflow que permiten a un DAG esperar a que ocurra una condición específica antes de continuar con la ejecución. Son particularmente útiles para manejar dependencias externas o para implementar lógica de espera en los flujos de trabajo.

#### 6.2.1 FileSensor

El `FileSensor` es uno de los sensores más comunes y se utiliza para esperar la aparición de un archivo en un sistema de archivos.

```python
from airflow.sensors.filesystem import FileSensor

esperar_archivo = FileSensor(
    task_id='esperar_archivo',
    filepath='/ruta/al/archivo',
    poke_interval=300,  # Verifica cada 5 minutos
    timeout=60 * 60 * 24,  # Timeout después de 24 horas
    mode='poke',
    dag=dag,
)
```

Explicación detallada:

- `task_id`: Identificador único para esta tarea en el DAG.
- `filepath`: Ruta al archivo que el sensor está esperando.
- `poke_interval`: Frecuencia con la que el sensor verifica la condición (en segundos).
- `timeout`: Tiempo máximo que el sensor esperará antes de fallar (en segundos).
- `mode`: Modo de operación del sensor. 'poke' significa que el sensor ocupará un slot de worker mientras espera.

#### 6.2.2 ExternalTaskSensor

El `ExternalTaskSensor` permite a un DAG esperar la finalización de una tarea en otro DAG.

```python
from airflow.sensors.external_task import ExternalTaskSensor
from datetime import timedelta

esperar_tarea_externa = ExternalTaskSensor(
    task_id='esperar_tarea_externa',
    external_dag_id='otro_dag',
    external_task_id='tarea_a_esperar',
    execution_delta=timedelta(hours=1),
    mode='reschedule',
    dag=dag,
)
```

Explicación:

- `external_dag_id`: ID del DAG externo.
- `external_task_id`: ID de la tarea externa a esperar.
- `execution_delta`: Diferencia de tiempo entre la ejecución de este DAG y el DAG externo.
- `mode`: 'reschedule' libera el slot de worker entre verificaciones, útil para esperas largas.

#### 6.2.3 SqlSensor

El `SqlSensor` espera hasta que una consulta SQL devuelva un resultado específico.

```python
from airflow.sensors.sql import SqlSensor

esperar_datos = SqlSensor(
    task_id='esperar_datos',
    conn_id='mi_conexion_db',
    sql="SELECT COUNT(*) FROM mi_tabla WHERE fecha = '{{ ds }}'",
    poke_interval=600,  # Verifica cada 10 minutos
    timeout=60 * 60 * 12,  # Timeout después de 12 horas
    dag=dag,
)
```

Este sensor esperará hasta que la consulta SQL devuelva un resultado distinto de cero.

### 6.3 Triggers externos

Los triggers externos permiten activar DAGs basados en eventos que ocurren fuera del sistema de Airflow. Esto es útil para integrar Airflow con sistemas externos o para iniciar flujos de trabajo manualmente.

#### 6.3.1 Configuración de un DAG para triggers externos

```python
dag = DAG(
    'dag_trigger_externo',
    schedule_interval=None,  # No se programa automáticamente
    start_date=datetime(2023, 1, 1),
    catchup=False,
)

def tarea_activada(**context):
    print(f"DAG activado externamente. Execution date: {context['execution_date']}")

tarea = PythonOperator(
    task_id='tarea_activada',
    python_callable=tarea_activada,
    provide_context=True,
    dag=dag,
)
```

En este ejemplo, el DAG no tiene un `schedule_interval`, lo que significa que no se ejecutará automáticamente.

#### 6.3.2 Activación del DAG

Para activar este DAG externamente, puedes usar el comando CLI de Airflow:

```bash
airflow dags trigger dag_trigger_externo
```

También puedes activar el DAG con parámetros:

```bash
airflow dags trigger -c '{"param1": "valor1", "param2": "valor2"}' dag_trigger_externo
```

Estos parámetros estarán disponibles en el contexto de las tareas del DAG.

#### 6.3.3 API REST

Airflow también proporciona una API REST que permite activar DAGs programáticamente:

```python
import requests

response = requests.post(
    'http://localhost:8080/api/v1/dags/dag_trigger_externo/dagRuns',
    json={'conf': {}},
    auth=('usuario', 'contraseña')
)

print(response.json())
```

### 6.4 Backfilling y Catchup

Backfilling y catchup son características fundamentales de Airflow que permiten ejecutar DAGs para intervalos de tiempo pasados.

#### Catchup

El catchup es una característica automática de Airflow que se encarga de ejecutar todas las ejecuciones programadas de un DAG que no se hayan realizado entre la `start_date` del DAG y la fecha actual.

**¿Qué hace el catchup?**
Cuando se habilita el catchup (configurando `catchup=True` en la definición del DAG), Airflow automáticamente programa y ejecuta todas las ejecuciones del DAG que deberían haber ocurrido desde la `start_date` hasta el presente, basándose en el `schedule_interval` del DAG.

**Situaciones de uso:**

1. **Introducción de un nuevo DAG:** Cuando agregas un nuevo DAG a Airflow con una `start_date` en el pasado, el catchup asegura que todos los intervalos históricos se procesen.
2. **Recuperación después de un tiempo de inactividad:** Si Airflow estuvo inactivo durante un período, el catchup ayuda a ponerse al día con todas las ejecuciones perdidas una vez que el sistema vuelve a funcionar.
3. **Procesamiento continuo de datos:** En escenarios donde necesitas procesar datos de manera continua, el catchup garantiza que no se pierdan intervalos, incluso si hay retrasos o interrupciones.

**Ejemplo de configuración:**

```python
dag = DAG(
    'dag_con_catchup',
    start_date=datetime(2023, 1, 1),
    schedule_interval='@daily',
    catchup=True,  # Habilita el catchup
)
```

**Consideraciones:**

- El catchup puede generar una gran carga de trabajo repentina si hay muchas ejecuciones atrasadas.
- Es crucial que las tareas del DAG sean idempotentes cuando se usa catchup.

#### Backfilling

El backfilling es un proceso manual que permite a los usuarios ejecutar un DAG para un rango de fechas específico en el pasado, independientemente de la configuración de catchup del DAG.

**¿Qué hace el backfilling?**
El backfilling ejecuta el DAG para cada intervalo de programación dentro de un rango de fechas especificado, permitiendo procesar o reprocesar datos históricos de manera controlada.

**Situaciones de uso:**

1. **Reprocesamiento de datos:** Cuando necesitas volver a ejecutar un DAG para un período específico debido a errores en los datos o cambios en la lógica de procesamiento.
2. **Llenado de datos históricos:** Al introducir un nuevo pipeline de datos que necesita procesar datos antiguos.
3. **Pruebas y depuración:** Para probar un DAG con datos históricos antes de ponerlo en producción.
4. **Recuperación selectiva:** Cuando solo necesitas ejecutar el DAG para un período específico en el pasado, sin afectar otras ejecuciones.

**Ejemplo de uso:**

```bash
airflow dags backfill dag_con_backfill --start-date 2023-01-01 --end-date 2023-01-31
```

**Consideraciones:**

- El backfilling es un proceso controlado manualmente, lo que permite una mayor flexibilidad y control sobre qué períodos se procesan.
- Puedes especificar opciones adicionales como el número máximo de ejecuciones en paralelo.
- Al igual que con el catchup, las tareas deben ser idempotentes para un backfilling seguro.

#### Diferencias clave entre Catchup y Backfilling

1. Automatización:
   - Catchup es un proceso automático controlado por la configuración del DAG.
   - Backfilling es un proceso manual iniciado por el usuario.
2. Alcance:
   - Catchup siempre comienza desde la start_date del DAG hasta el presente.
   - Backfilling permite especificar un rango de fechas arbitrario.
3. Flexibilidad:
    - Catchup sigue estrictamente la configuración del DAG.
    - Backfilling ofrece más opciones de configuración al momento de la ejecución.
4. Uso típico:
    - Catchup se usa para mantener la consistencia en la ejecución regular de DAGs.
    - Backfilling se usa para casos específicos de reprocesamiento o recuperación de datos.

## 7. Funcionalidades Avanzadas

### 7.1 Pools

Los pools en Airflow son una herramienta poderosa para controlar la concurrencia y gestionar recursos compartidos. Permiten limitar el número de tareas que pueden ejecutarse simultáneamente en un grupo específico de trabajos.

#### ¿Qué son los Pools?

Un pool es esencialmente un grupo de slots de ejecución. Cada tarea asignada a un pool ocupa un slot cuando se ejecuta. Una vez que todos los slots están ocupados, las tareas adicionales deben esperar hasta que un slot se libere.

#### ¿Cuándo usar Pools?

Los pools son útiles en varios escenarios:

1. Limitar el acceso a recursos compartidos (por ejemplo, bases de datos, APIs con límites de tasa).
2. Controlar la carga en sistemas externos.
3. Priorizar ciertos tipos de tareas sobre otros.

#### Ejemplo detallado de uso de Pools

```python
from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from airflow.models.pool import Pool
from datetime import datetime, timedelta

# Crear un pool (normalmente se hace a través de la UI, pero aquí lo hacemos programáticamente)
pool = Pool(
    pool='recurso_limitado',
    slots=3  # Solo 3 tareas pueden ejecutarse simultáneamente en este pool
)

# Añadir el pool a la sesión de Airflow
from airflow.settings import Session
session = Session()
session.add(pool)
session.commit()

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2023, 1, 1),
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG('ejemplo_pool', default_args=default_args, schedule_interval=timedelta(days=1))

def tarea_ejemplo(task_number):
    print(f"Ejecutando tarea {task_number}")
    # Simular trabajo
    import time
    time.sleep(10)

# Crear 5 tareas, pero solo 3 pueden ejecutarse simultáneamente
for i in range(5):
    tarea = PythonOperator(
        task_id=f'tarea_{i}',
        python_callable=tarea_ejemplo,
        op_kwargs={'task_number': i},
        pool='recurso_limitado',
        dag=dag,
    )

```

En este ejemplo:

1. Creamos un pool llamado 'recurso_limitado' con 3 slots.
2. Definimos un DAG con 5 tareas.
3. Todas las tareas están asignadas al pool 'recurso_limitado'.
4. Aunque hay 5 tareas, solo 3 podrán ejecutarse simultáneamente debido a la limitación del pool.

#### Consideraciones al usar Pools

- Los pools son una herramienta de control de concurrencia a nivel de todo el sistema Airflow, no solo de un DAG específico.
- Si una tarea está asignada a un pool lleno, esperará hasta que un slot se libere.
- Puedes cambiar dinámicamente el número de slots en un pool a través de la UI de Airflow o programáticamente.

### 7.2 SLAs (Service Level Agreements)

Los SLAs en Airflow permiten definir expectativas de tiempo de ejecución para las tareas y recibir alertas cuando estas expectativas no se cumplen.

#### ¿Qué son los SLAs en Airflow?

Un SLA (Service Level Agreement) en Airflow es un tiempo máximo esperado para que una tarea complete su ejecución. Si una tarea supera este tiempo, Airflow puede generar alertas y registrar el incumplimiento del SLA.

#### ¿Cuándo usar SLAs?

Los SLAs son útiles para:

1. Monitorear el rendimiento de tareas críticas.
2. Identificar cuellos de botella en los flujos de trabajo.
3. Garantizar que los datos estén disponibles dentro de plazos específicos.

#### Ejemplo detallado de uso de SLAs

```python
from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import datetime, timedelta

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2023, 1, 1),
    'email': ['alert@example.com'],
    'email_on_failure': True,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG('ejemplo_sla', default_args=default_args, schedule_interval=timedelta(days=1))

def tarea_lenta():
    import time
    time.sleep(3600)  # Dormir por 1 hora
    print("Tarea lenta completada")

tarea_con_sla = PythonOperator(
    task_id='tarea_con_sla',
    python_callable=tarea_lenta,
    sla=timedelta(minutes=30),  # SLA de 30 minutos
    dag=dag,
)

def sla_miss_callback(dag, task_list, blocking_task_list, slas, blocking_tis):
    print(f"SLA perdido para las tareas: {task_list}")
    # Aquí puedes añadir lógica adicional, como enviar una alerta personalizada

dag.sla_miss_callback = sla_miss_callback
```

En este ejemplo:

1. Definimos una tarea que intencionalmente toma 1 hora en completarse.
2. Establecemos un SLA de 30 minutos para esta tarea.
3. Definimos un callback personalizado que se ejecutará cuando se incumpla el SLA.

#### Consideraciones al usar SLAs

- Los SLAs se evalúan en relación con el inicio programado de la tarea, no su inicio real.
- Las alertas de SLA no detienen la ejecución de la tarea; solo notifican del incumplimiento.
- Puedes configurar acciones personalizadas en caso de incumplimiento de SLA usando `sla_miss_callback`.

### 7.3 Callbacks

Los callbacks en Airflow permiten ejecutar código personalizado en respuesta a eventos específicos durante la ejecución de tareas y DAGs.

#### Tipos de Callbacks

Airflow soporta varios tipos de callbacks:

1. **on_success_callback**: Se ejecuta cuando una tarea se completa con éxito.
2. **on_failure_callback**: Se ejecuta cuando una tarea falla.
3. **on_retry_callback**: Se ejecuta cuando una tarea se reintenta.
4. **sla_miss_callback**: Se ejecuta cuando se incumple un SLA (a nivel de DAG).

#### ¿Cuándo usar Callbacks?

Los callbacks son útiles para:

1. Notificaciones personalizadas.
2. Logging avanzado.
3. Desencadenar acciones basadas en el resultado de las tareas.
4. Integración con sistemas externos de monitoreo.

#### Ejemplo detallado de uso de Callbacks

```python
from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import datetime, timedelta

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2023, 1, 1),
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG('ejemplo_callbacks', default_args=default_args, schedule_interval=timedelta(days=1))

def tarea_ejemplo():
    import random
    if random.random() < 0.5:
        raise Exception("Tarea falló aleatoriamente")
    print("Tarea completada con éxito")

def on_success_callback(context):
    task_instance = context['task_instance']
    print(f"Tarea {task_instance.task_id} completada con éxito")
    # Aquí puedes añadir lógica adicional, como enviar una notificación

def on_failure_callback(context):
    task_instance = context['task_instance']
    print(f"Tarea {task_instance.task_id} falló")
    # Aquí puedes añadir lógica adicional, como enviar una alerta

def on_retry_callback(context):
    task_instance = context['task_instance']
    print(f"Reintentando tarea {task_instance.task_id}")
    # Aquí puedes añadir lógica adicional, como logging específico para reintentos

tarea_con_callbacks = PythonOperator(
    task_id='tarea_con_callbacks',
    python_callable=tarea_ejemplo,
    on_success_callback=on_success_callback,
    on_failure_callback=on_failure_callback,
    on_retry_callback=on_retry_callback,
    dag=dag,
)
```

En este ejemplo:

1. Definimos una tarea que falla aleatoriamente el 50% de las veces.
2. Implementamos callbacks para éxito, fallo y reintento.
3. Cada callback recibe un contexto que incluye información sobre la ejecución de la tarea.

#### Consideraciones al usar Callbacks

- Los callbacks deben ser funciones ligeras y rápidas para evitar bloquear el scheduler de Airflow.
- El contexto proporcionado a los callbacks contiene información valiosa sobre la ejecución de la tarea y el DAG.
- Puedes usar callbacks para integrar Airflow con sistemas externos de notificación o monitoreo.

## 8. Mejores prácticas

1. Usa variables de Airflow para parametrizar tus DAGs.
2. Implementa manejo de errores robusto en tus tareas.
3. Utiliza pools para limitar la concurrencia de tareas que acceden a recursos compartidos.
4. Aprovecha los sensores para manejar dependencias externas.
5. Usa XComs para pasar pequeñas cantidades de datos entre tareas.
6. Mantén tus DAGs idempotentes y deterministas.
7. Monitorea regularmente el rendimiento y los logs de Airflow.

## 9. Integración con otras herramientas

Airflow se integra fácilmente con muchas herramientas del ecosistema de big data:

- **Hadoop**: Usa `HDFSOperator` para interactuar con HDFS.
- **Hive**: `HiveOperator` para ejecutar consultas Hive.
- **Spark**: `SparkSubmitOperator` para enviar trabajos Spark.
- **Python**: `PythonOperator` para ejecutar cualquier función Python.
- **Bases de datos**: Operadores para MySQL, Postgres, etc.
- **Cloud services**: Operadores para AWS, GCP, Azure, etc.

## 10. Ejercicios prácticos

### Ejercicio 1

Crea un DAG llamado "procesamiento_logs" que realice las siguientes tareas:

1. Utilice un FileSensor para esperar la existencia de un archivo llamado "logs.txt" en el directorio `/opt/airflow/logs/`.
2. Lea el contenido del archivo "logs.txt" y cuente el número de líneas que contiene usando un PythonOperator.
3. Basándose en el número de líneas, use un BranchPythonOperator para decidir entre dos rutas:
   - Si hay más de 100 líneas, ejecute una tarea que simule un "análisis detallado".
   - Si hay 100 líneas o menos, ejecute una tarea que simule un "análisis rápido".
4. Finalmente, archive el archivo de logs moviéndolo a un directorio de archivos procesados.

El DAG debe ejecutarse diariamente y tener un mecanismo de reintento para las tareas que puedan fallar.

### Ejercicio 2

Implementa un DAG llamado "datos_meteorologicos" que simule el procesamiento de datos meteorológicos:

1. Genera datos meteorológicos ficticios (temperatura, humedad, presión) para 5 ciudades usando un PythonOperator.
2. Transforma los datos calculando la temperatura promedio y clasificando cada ciudad como "fría", "templada" o "caliente".
3. Utiliza un BranchPythonOperator para decidir qué ciudades necesitan una "alerta por temperatura":
   - Si alguna ciudad es clasificada como "caliente", ejecuta una tarea de "emitir alerta".
   - Si no hay ciudades "calientes", ejecuta una tarea de "sin alerta".
4. Genera un informe resumen con los datos procesados y guárdalo en un archivo de texto.

Usa XComs para pasar los datos entre las tareas y una variable de Airflow para almacenar el umbral de temperatura para la clasificación "caliente".

### Ejercicio 3

Crea un DAG llamado "analisis_tweets" que simule un análisis de sentimientos de tweets:

1. Utiliza un PythonOperator para generar una lista de 20 tweets ficticios.
2. Implementa un operador personalizado llamado "SentimentAnalyzer" que asigne aleatoriamente un sentimiento (positivo, negativo, neutral) a cada tweet.
3. Usa un BranchPythonOperator para clasificar el resultado general:
   - Si hay más tweets positivos que negativos, ejecuta una tarea "tendencia_positiva".
   - Si hay más tweets negativos que positivos, ejecuta una tarea "tendencia_negativa".
   - Si están empatados, ejecuta una tarea "tendencia_neutral".
4. Genera un informe con el recuento de sentimientos y la tendencia general.
5. Utiliza un FileSensor para esperar la existencia de un archivo "publicar_informe.txt" antes de "publicar" el informe (simula la publicación escribiendo en un archivo de log).

Implementa el uso de un Pool llamado "analisis_pool" para limitar el número de tareas de análisis que pueden ejecutarse simultáneamente.
