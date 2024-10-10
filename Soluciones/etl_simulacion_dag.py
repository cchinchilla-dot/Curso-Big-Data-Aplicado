from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python import PythonOperator, BranchPythonOperator
from airflow.operators.bash import BashOperator
from airflow.operators.dummy import DummyOperator
from airflow.sensors.filesystem import FileSensor
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
