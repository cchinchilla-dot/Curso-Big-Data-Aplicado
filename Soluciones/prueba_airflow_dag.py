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
