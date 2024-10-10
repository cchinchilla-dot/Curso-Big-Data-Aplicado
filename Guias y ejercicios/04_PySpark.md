# Guía avanzada de PySpark

- Módulo 1:
  - Procesamiento de datos.
  - Analítica de Big Data en los ecosistemas de almacenamiento.

- Modulo 2:
  - Otras herramientas: Como complemento a Spark, destacando su curva de aprendizaje sencilla y su conexión con los temas del Módulo 5.

Se incluye PySpark en "Otras herramientas" por su facilidad de uso y su amplia adopción en la industria, impulsada por la popularidad de Python en ciencia de datos. Además, su creciente demanda en el mercado laboral hace que los perfiles con conocimientos en PySpark sean altamente valorados, dado su papel clave en proyectos de Big Data.

## 1. Introducción a PySpark

PySpark es la interfaz de Python para Apache Spark, un motor de procesamiento de datos distribuido diseñado para ser rápido y versátil. PySpark permite a los desarrolladores y científicos de datos utilizar las capacidades de Spark con la facilidad y flexibilidad de Python.

### Características clave

- Procesamiento en memoria para mayor velocidad
- Soporte para computación distribuida
- APIs de alto nivel para facilitar el desarrollo
- Integración con diversos sistemas de almacenamiento (HDFS, S3, Cassandra, etc.)
- Ecosistema rico que incluye herramientas para SQL, ML, grafos y streaming
- Compatibilidad con bibliotecas populares de Python como NumPy y Pandas

### Arquitectura

- **Driver Program**: Coordina la ejecución de la aplicación Spark.
- **Cluster Manager**: Gestiona los recursos del clúster (puede ser Standalone, YARN, Mesos o Kubernetes).
- **Executors**: Procesos en los nodos trabajadores que ejecutan tareas.
- **Tasks**: Unidades de trabajo enviadas a los executors.
- **Stages**: Conjuntos de tareas que pueden ejecutarse en paralelo.
- **DAG Scheduler**: Optimiza el plan de ejecución creando un grafo acíclico dirigido (DAG) de stages.

## 2. Configuración del entorno PySpark

### 2.1 Instalación de PySpark

PySpark se puede instalar fácilmente utilizando pip, el gestor de paquetes de Python.

```bash
pip install pyspark
pip install mysql-connector-python
```

Para versiones específicas de Spark, puedes especificar la versión:

```bash
pip install pyspark==3.2.0
```

### 2.2 Integración con Jupyter Notebook

Jupyter Notebook es un entorno interactivo de desarrollo que permite crear y compartir documentos que contienen código en vivo, ecuaciones, visualizaciones y texto narrativo. Es una herramienta muy popular para el análisis de datos, aprendizaje automático y programación en general.

Para usar PySpark en Jupyter Notebook:

1. Asegúrate de que Jupyter Notebook esté instalado. Si no lo está, puedes instalarlo con:

   ```bash
   pip install jupyter
   ```

2. Inicia Jupyter Notebook si aún no está en ejecución:

   ```bash
   jupyter notebook
   ```

   Esto abrirá una nueva pestaña en tu navegador con la interfaz de Jupyter Notebook.

3. En la interfaz de Jupyter Notebook (<http://localhost:8888/tree>), haz clic en "New" y selecciona "Python 3" para crear un nuevo notebook.

4. En la primera celda del notebook, importa PySpark y crea una sesión de Spark:

   ```python
   from pyspark.sql import SparkSession

   spark = SparkSession.builder \
       .appName("PySpark en Jupyter") \
       .getOrCreate()
   ```

5. Ejecuta la celda presionando Shift+Enter. Si todo está configurado correctamente, esto creará una sesión de Spark sin errores.

## 3. Conceptos fundamentales de PySpark

### 3.1 RDD (Resilient Distributed Dataset)

RDD es la estructura de datos fundamental en Spark. Es una colección distribuida e inmutable de objetos que puede ser procesada en paralelo.

Características clave:

- Inmutabilidad: Los RDDs no pueden ser modificados una vez creados.
- Distribución: Los datos se distribuyen automáticamente a través del clúster.
- Tolerancia a fallos: Spark puede reconstruir partes perdidas de un RDD utilizando su linaje.
- Evaluación perezosa (lazy evaluation): Las transformaciones no se ejecutan hasta que se invoca una acción.

#### Ejemplo de creación y uso de RDD

```python
from pyspark.sql import SparkSession
from pyspark import SparkContext

# Crear una sesión de Spark
spark = SparkSession.builder \
    .appName("RDDExample") \
    .master("local[*]") \
    .getOrCreate()

sc = spark.sparkContext

# Crear un RDD a partir de una colección
rdd = sc.parallelize(range(1, 1000001), 100)

# Aplicar transformaciones complejas
resultRDD = rdd \
    .map(lambda x: (x % 10, x)) \
    .reduceByKey(lambda a, b: a + b) \
    .mapValues(lambda x: x * x)

# Acción para materializar el resultado
result = resultRDD.collect()

# Imprimir los primeros 10 elementos del resultado
for item in result[:10]:
    print(item)

# Detener la sesión de Spark
spark.stop()
```

### 3.2 DataFrame y Dataset

DataFrame y Dataset son APIs estructuradas que proporcionan una abstracción más rica y optimizada que los RDDs.

- DataFrame: Colección distribuida de datos organizados en columnas nombradas.
- Dataset: Extensión tipada de DataFrame que proporciona una interfaz orientada a objetos (no disponible en Python, solo en Scala y Java).

#### Ejemplo de uso de DataFrame

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, avg, dense_rank, sum
from pyspark.sql.window import Window
from datetime import date

# Crear una sesión de Spark
spark = SparkSession.builder \
    .appName("DataFrameExample") \
    .master("local[*]") \
    .getOrCreate()

# Crear un DataFrame a partir de una lista de tuplas
data = [
    (date(2024, 9, 1), "ProductoA", 10, 20.5),
    (date(2024, 9, 1), "ProductoB", 5, 10.0),
    (date(2024, 9, 2), "ProductoA", 7, 20.5),
    (date(2024, 9, 2), "ProductoB", 8, 10.0),
    (date(2024, 9, 3), "ProductoA", 15, 20.5),
    (date(2024, 9, 3), "ProductoB", 6, 10.0),
    (date(2024, 9, 4), "ProductoA", 14, 20.5),
    (date(2024, 9, 4), "ProductoB", 10, 10.0),
    (date(2024, 9, 5), "ProductoA", 12, 20.5),
    (date(2024, 9, 5), "ProductoB", 9, 10.0),
    (date(2024, 9, 6), "ProductoA", 11, 20.5),
    (date(2024, 9, 6), "ProductoB", 7, 10.0),
    (date(2024, 9, 7), "ProductoA", 9, 20.5),
    (date(2024, 9, 7), "ProductoB", 5, 10.0)
]

columns = ["fecha", "producto", "cantidad", "precio"]
df = spark.createDataFrame(data, columns)

# Realizar operaciones complejas
windowSpec = Window.partitionBy("producto").orderBy("fecha").rowsBetween(-6, 0)

resultadoDF = df \
    .groupBy("fecha", "producto") \
    .agg(
        sum("cantidad").alias("total_cantidad"),
        sum(col("precio") * col("cantidad")).alias("total_ventas")
    ) \
    .withColumn("promedio_7_dias", 
                avg("total_ventas").over(windowSpec)) \
    .filter(col("total_ventas") > col("promedio_7_dias"))

# Mostrar el resultado
resultadoDF.show()
```

### 3.3 SparkSQL

SparkSQL permite ejecutar consultas SQL sobre los datos en Spark, proporcionando una interfaz familiar para los analistas de datos.

#### Ejemplo de uso de SparkSQL

```python
# Continuando con el DataFrame anterior

# Registrar el DataFrame como una vista temporal
df.createOrReplaceTempView("ventas")

# Ejecutar una consulta SQL compleja
resultadoSQL = spark.sql("""
    WITH ventas_ranking AS (
        SELECT 
            fecha,
            producto,
            total_ventas,
            RANK() OVER (PARTITION BY producto ORDER BY total_ventas DESC) as rank
        FROM (
            SELECT 
                fecha,
                producto,
                SUM(cantidad * precio) as total_ventas
            FROM ventas
            GROUP BY fecha, producto
        )
    )
    SELECT 
        v.fecha,
        v.producto,
        v.total_ventas,
        vr.rank
    FROM ventas_ranking vr
    JOIN (
        SELECT 
            fecha,
            producto,
            SUM(cantidad * precio) as total_ventas
        FROM ventas
        GROUP BY fecha, producto
    ) v ON v.fecha = vr.fecha AND v.producto = vr.producto
    WHERE vr.rank <= 3
    ORDER BY v.producto, vr.rank
""")

resultadoSQL.show()
```

## 4. Manejo de archivos

PySpark proporciona una API unificada para leer diferentes formatos de archivos. Vamos a explorar cómo trabajar con archivos CSV, Parquet y HDFS, incluyendo la descarga de archivos de internet y la escritura/lectura en HDFS.

### 4.1 Archivos CSV

Primero, descargaremos un archivo CSV de internet y luego lo leeremos con PySpark.

```python
import requests
from pyspark.sql import SparkSession

# Crear una sesión de Spark
spark = SparkSession.builder \
    .appName("CSV File Reading") \
    .getOrCreate()

# URL del archivo CSV (ejemplo: dataset de Iris)
csv_url = "https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data"

# Descargar el archivo CSV
response = requests.get(csv_url)
with open("/tmp/iris.csv", "wb") as f:
    f.write(response.content)

# Leer el archivo CSV descargado
iris_df = spark.read.csv("/tmp/iris.csv", 
                         header=False, 
                         inferSchema=True)

# Asignar nombres a las columnas
column_names = ["sepal_length", "sepal_width", "petal_length", "petal_width", "class"]
iris_df = iris_df.toDF(*column_names)

# Mostrar el esquema y los primeros registros
iris_df.printSchema()
iris_df.show(5)

# Escritura de CSV
iris_df.write.csv("hdfs://localhost:9000/user/hdfs/iris", header=True, mode="overwrite")
```

### 4.2 Archivo Parquet

Parquet es un formato de almacenamiento columnar que ofrece una compresión eficiente y un rendimiento de consulta rápido para Big Data.

#### ¿Qué es Parquet?

- Formato de archivo columnar para Hadoop.
- Diseñado para un rendimiento y compresión eficientes.
- Soporta esquemas complejos y anidados.
- Altamente eficiente para consultas que solo acceden a ciertas columnas.

Ahora, descargaremos un archivo Parquet de internet y lo leeremos con PySpark.

```python
import requests

# URL del archivo Parquet (ejemplo: datos de vuelos)
parquet_url = "https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2022-01.parquet"

# Descargar el archivo Parquet
response = requests.get(parquet_url)
with open("/tmp/flights.parquet", "wb") as f:
    f.write(response.content)

# Leer el archivo Parquet descargado
flights_df = spark.read.parquet("/tmp/flights.parquet")

# Mostrar el esquema y los primeros registros
flights_df.printSchema()
flights_df.show(5)

# Escritura de Parquetx
flights_df.write.parquet("hdfs://localhost:9000/user/hdfs/salida_parquet", mode="overwrite")
```

## 5. Integración con Bases de Datos

PySpark ofrece una potente integración con sistemas de bases de datos relacionales. En esta sección, nos conectaremos al servidor MySQL configurado en nuestro Dockerfile, crearemos una nueva base de datos de prueba y realizaremos operaciones sobre ella.

### 5.1 Conexión al servidor MySQL y creación de una nueva base de datos

Debemos instalar el conector de MySQL introduciendo el siguiente comando en el terminal:

```python
pip install mysql-connector-python
```

Nos conectaremos al servidor MySQL y crearemos una nueva base de datos para nuestras pruebas.

```python
from pyspark.sql import SparkSession
import mysql.connector

# Crear una sesión de Spark con soporte para JDBC
spark = SparkSession.builder \
    .appName("MySQL Integration") \
    .getOrCreate()

# Parámetros de conexión
mysql_host = "localhost"
mysql_port = 3306
mysql_user = "hive"
mysql_password = "hive_password" 

# Conectar a MySQL y crear una nueva base de datos
connection = mysql.connector.connect(
    host=mysql_host,
    port=mysql_port,
    user=mysql_user,
    password=mysql_password
)
cursor = connection.cursor()

# Crear una nueva base de datos
new_database = "pyspark_test_db"
cursor.execute(f"CREATE DATABASE IF NOT EXISTS {new_database}")
print(f"Base de datos '{new_database}' creada correctamente.")

# Cerrar la conexión
cursor.close()
connection.close()

# Configurar la URL JDBC para la nueva base de datos
jdbc_url = f"jdbc:mysql://{mysql_host}:{mysql_port}/{new_database}"
connection_properties = {
    "user": mysql_user,
    "password": mysql_password,
    "driver": "com.mysql.cj.jdbc.Driver"
}

print(f"Conexión JDBC configurada para la base de datos: {new_database}")
```

### 5.2 Crear una tabla de prueba en PySpark y escribirla en MySQL

Ahora, crearemos un DataFrame de prueba en PySpark y lo escribiremos en nuestra nueva base de datos MySQL.

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import rand, randn, when, col
import mysql.connector

# Crear una sesión de Spark
spark = SparkSession.builder \
    .appName("MySQL Integration") \
    .getOrCreate()

# Parámetros de conexión
mysql_host = "localhost"
mysql_port = 3306
mysql_user = "hive"
mysql_password = "hive_password" 
new_database = "pyspark_test_db"

# Crear un DataFrame de prueba
test_data = spark.range(0, 10000) \
    .withColumn("random_value", rand()) \
    .withColumn("normal_distribution", randn()) \
    .withColumn("category", when(col("random_value") < 0.3, "Low")
                            .when(col("random_value") < 0.7, "Medium")
                            .otherwise("High"))

# Mostrar los primeros registros
print("Muestra de los datos generados:")
test_data.show(5)

# Conectar a MySQL
try:
    conn = mysql.connector.connect(
        host=mysql_host,
        port=mysql_port,
        user=mysql_user,
        password=mysql_password,
        database=new_database
    )
    cursor = conn.cursor()

    # Crear la tabla en MySQL
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS test_data (
        id BIGINT PRIMARY KEY,
        random_value DOUBLE,
        normal_distribution DOUBLE,
        category VARCHAR(10)
    )
    """)

    # Insertar datos en MySQL
    data = test_data.collect()
    for row in data:
        cursor.execute("""
        INSERT INTO test_data (id, random_value, normal_distribution, category)
        VALUES (%s, %s, %s, %s)
        """, (row.id, row.random_value, row.normal_distribution, row.category))

    conn.commit()
    print("Datos de prueba escritos en la tabla 'test_data' de MySQL.")

    # Verificar la escritura leyendo los datos de vuelta
    cursor.execute("SELECT * FROM test_data LIMIT 5")
    result = cursor.fetchall()
    print("\nVerificación de los datos escritos en MySQL:")
    for row in result:
        print(row)

    # Comparar el número de filas
    cursor.execute("SELECT COUNT(*) FROM test_data")
    mysql_count = cursor.fetchone()[0]
    original_count = test_data.count()
    print(f"Número de filas en el DataFrame original: {original_count}")
    print(f"Número de filas en la tabla MySQL: {mysql_count}")
    assert original_count == mysql_count, "El número de filas no coincide"

except mysql.connector.Error as e:
    print(f"Error de MySQL: {e}")
finally:
    if 'conn' in locals() and conn.is_connected():
        cursor.close()
        conn.close()
        print("Conexión a MySQL cerrada.")
```

### 5.3 Realizar operaciones analíticas utilizando PySpark y MySQL

Ahora que tenemos datos en nuestra base de datos MySQL, podemos realizar algunas operaciones analíticas utilizando PySpark.

```python
import mysql.connector
from pyspark.sql import SparkSession
from pyspark.sql.types import StructType, StructField, StringType, DoubleType, LongType

# Crear una sesión de Spark
spark = SparkSession.builder \
    .appName("MySQL to PySpark") \
    .getOrCreate()

# Parámetros de conexión MySQL
mysql_config = {
    'user': 'hive',
    'password': 'hive_password',
    'host': 'localhost',
    'database': 'pyspark_test_db',
    'raise_on_warnings': True
}

#  Leer datos
try:
    conn = mysql.connector.connect(**mysql_config)
    cursor = conn.cursor()
    
    # Ejecutar una consulta
    query = "SELECT * FROM test_data LIMIT 100"
    cursor.execute(query)
    
    # Obtener los resultados
    results = cursor.fetchall()
    
    # Obtener los nombres de las columnas
    column_names = [desc[0] for desc in cursor.description]
    
    print(f"Datos leídos de MySQL: {len(results)} filas")
    
except mysql.connector.Error as err:
    print(f"Error: {err}")
finally:
    if 'conn' in locals() and conn.is_connected():
        cursor.close()
        conn.close()
        print("Conexión MySQL cerrada.")

# Definir el esquema para el DataFrame de Spark
# Ajusta los tipos de datos según tu esquema real
schema = StructType([
    StructField("id", LongType(), True),
    StructField("random_value", DoubleType(), True),
    StructField("normal_distribution", DoubleType(), True),
    StructField("category", StringType(), True)
])

# Crear un DataFrame de Spark a partir de los datos de MySQL
if 'results' in locals():
    df = spark.createDataFrame(results, schema=schema)
    
    print("DataFrame de Spark creado. Mostrando las primeras 5 filas:")
    df.show(5)
    
    print("Realizando algunas operaciones con PySpark:")
    
    # Ejemplo de operación: contar registros por categoría
    df.groupBy("category").count().show()
    
    # Ejemplo de operación: calcular estadísticas básicas
    df.describe().show()
else:
    print("No se pudieron cargar datos en el DataFrame de Spark.")

# Detener la sesión de Spark
spark.stop()
```

### 5.4 Realizar una consulta compleja combinando datos de PySpark y MySQL

Por último, realizaremos una consulta más compleja que combina datos de PySpark y MySQL.

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, rand, when, avg, count, sum, round, datediff, current_date
from pyspark.sql.types import StructType, StructField, IntegerType, StringType, DateType, DoubleType
import mysql.connector
from datetime import datetime, date

# Crear una sesión de Spark
spark = SparkSession.builder \
    .appName("Complex MySQL PySpark Query") \
    .getOrCreate()

# Parámetros de conexión MySQL
mysql_config = {
    'user': 'hive',
    'password': 'hive_password',
    'host': 'localhost',
    'database': 'pyspark_test_db',
    'raise_on_warnings': True
}

# Función para leer datos de MySQL usando el conector SQL de Python
def read_mysql_data():
    try:
        conn = mysql.connector.connect(**mysql_config)
        cursor = conn.cursor()
        
        query = "SELECT * FROM test_data"
        cursor.execute(query)
        
        # Obtener los resultados y los nombres de las columnas
        results = cursor.fetchall()
        column_names = [desc[0] for desc in cursor.description]
        
        print(f"Datos leídos de MySQL: {len(results)} filas")
        return results, column_names
    
    except mysql.connector.Error as err:
        print(f"Error: {err}")
        return None, None
    finally:
        if 'conn' in locals() and conn.is_connected():
            cursor.close()
            conn.close()
            print("Conexión MySQL cerrada.")

# Leer datos de MySQL
mysql_data, column_names = read_mysql_data()

if mysql_data and column_names:
    # Definir el esquema para el DataFrame de Spark
    schema = StructType([
        StructField("id", IntegerType(), True),
        StructField("random_value", DoubleType(), True),
        StructField("normal_distribution", DoubleType(), True),
        StructField("category", StringType(), True)
    ])

    # Crear un DataFrame de Spark a partir de los datos de MySQL
    mysql_df = spark.createDataFrame(mysql_data, schema=schema)

    # Mostrar una muestra de los datos de MySQL
    print("Muestra de datos de MySQL:")
    mysql_df.show(5)

    # Crear un DataFrame sintético en PySpark para simular datos adicionales
    synthetic_schema = StructType([
        StructField("user_id", IntegerType(), False),
        StructField("product", StringType(), False),
        StructField("purchase_date", DateType(), False),
        StructField("amount", IntegerType(), False)
    ])

    # Función para crear una fecha válida
    def create_date(i):
        year = 2023
        month = (i % 12) + 1
        day = (i % 28) + 1
        return date(year, month, day)

    # Generar datos sintéticos
    # Creación de una lista llamada 'synthetic_data' utilizando una comprensión de listas.
    synthetic_data = [
        (
            i % 1000,                             # Toma el valor de 'i' y calcula su módulo 1000, lo que limita el resultado entre 0 y 999.
            ["ProductA", "ProductB", "ProductC"][i % 3],  # Selecciona un producto de la lista ["ProductA", "ProductB", "ProductC"] usando 'i % 3' como índice.
            create_date(i),                       # Llama a la función 'create_date' pasando 'i' como argumento; esta función debe retornar un objeto de tipo 'date'.
            (i % 5 + 1) * 100                     # Calcula el módulo de 'i' con 5, suma 1 (para obtener un valor entre 1 y 5), y luego lo multiplica por 100.
        ) 
        for i in range(10000)                     # Itera sobre el rango de 0 a 9999, generando 10,000 tuplas en total.
    ]

    synthetic_df = spark.createDataFrame(synthetic_data, schema=synthetic_schema)

    # Mostrar una muestra de los datos sintéticos
    print("Muestra de datos sintéticos:")
    synthetic_df.show(5)

    # Realizar una consulta compleja combinando ambos DataFrames

    # Se realiza un 'join' (unión) entre dos DataFrames: 'mysql_df' y 'synthetic_df'.
    complex_query_result = mysql_df.join(
        synthetic_df,
        mysql_df.id == synthetic_df.user_id,  # Condición de unión: la columna 'id' de 'mysql_df' debe coincidir con la columna 'user_id' de 'synthetic_df'.
        "inner"  # Tipo de unión: inner join, solo mantiene las filas donde hay coincidencia en ambas tablas.
    ).select(
        mysql_df.id.alias("user_id"),  # Selecciona y renombra la columna 'id' de 'mysql_df' a 'user_id'.
        mysql_df.category,             # Selecciona la columna 'category' del DataFrame 'mysql_df'.
        synthetic_df.product,          # Selecciona la columna 'product' del DataFrame 'synthetic_df'.
        synthetic_df.purchase_date,    # Selecciona la columna 'purchase_date' del DataFrame 'synthetic_df'.
        synthetic_df.amount,           # Selecciona la columna 'amount' del DataFrame 'synthetic_df'.
        mysql_df.random_value,         # Selecciona la columna 'random_value' del DataFrame 'mysql_df'.
        mysql_df.normal_distribution   # Selecciona la columna 'normal_distribution' del DataFrame 'mysql_df'.
    ).withColumn(
        "days_since_purchase", datediff(current_date(), col("purchase_date")) 
        # Crea una nueva columna llamada 'days_since_purchase' que calcula el número de días transcurridos desde 'purchase_date' hasta la fecha actual.
    ).withColumn(
        "value_segment", when(col("random_value") < 0.3, "Low")  
                        .when(col("random_value") < 0.7, "Medium")
                        .otherwise("High")
        # Crea una nueva columna llamada 'value_segment' que categoriza 'random_value' en "Low", "Medium", o "High".
        # Si 'random_value' es menor a 0.3, se asigna "Low". Si está entre 0.3 y 0.7, se asigna "Medium". Para valores mayores o iguales a 0.7, se asigna "High".
    ).groupBy(
        "category", "product", "value_segment"
        # Agrupa los datos resultantes por las columnas 'category', 'product', y 'value_segment'.
    ).agg(
        count("user_id").alias("num_purchases"), 
        # Cuenta el número de compras en cada grupo (cada combinación de 'category', 'product', y 'value_segment') y lo nombra como 'num_purchases'.
        round(avg("amount"), 2).alias("avg_purchase_amount"),  
        # Calcula el promedio de la columna 'amount' en cada grupo, redondeado a 2 decimales, y lo nombra como 'avg_purchase_amount'.
        round(avg("days_since_purchase"), 2).alias("avg_days_since_purchase"), 
        # Calcula el promedio de la columna 'days_since_purchase', redondeado a 2 decimales, y lo nombra como 'avg_days_since_purchase'.
        round(sum("amount"), 2).alias("total_revenue"),  
        # Calcula la suma total de la columna 'amount' en cada grupo, redondeado a 2 decimales, y lo nombra como 'total_revenue'.
        round(avg("normal_distribution"), 2).alias("avg_normal_dist")  
        # Calcula el promedio de la columna 'normal_distribution' en cada grupo, redondeado a 2 decimales, y lo nombra como 'avg_normal_dist'.
    ).orderBy(
        "category", "product", "value_segment"  
        # Ordena el resultado final por 'category', 'product', y 'value_segment'.
    )
    
    # Mostrar los resultados de la consulta compleja
    print("Resultados de la consulta compleja:")
    complex_query_result.show(20, truncate=False)

    # Realizar un análisis adicional
    print("Análisis adicional - Top 5 combinaciones de categoría y producto por ingresos totales:")
    complex_query_result.orderBy(col("total_revenue").desc()).select(
        "category", "product", "total_revenue"
    ).show(5)

    # Calcular métricas globales
    print("Métricas globales:")
    global_metrics = complex_query_result.agg(
        round(sum("total_revenue"), 2).alias("global_revenue"),
        round(avg("avg_purchase_amount"), 2).alias("global_avg_purchase"),
        round(avg("avg_days_since_purchase"), 2).alias("global_avg_days_since_purchase")
    )
    global_metrics.show()
else:
    print("No se pudieron cargar los datos de MySQL.")

# Detener la sesión de Spark
spark.stop()
```

## 6. Gestión de Datos y Operaciones Avanzadas

### 6.1 Window Functions

Las funciones de ventana en PySpark permiten realizar cálculos avanzados como totales acumulados, rankings y medias móviles, sobre un conjunto de filas relacionadas.

```python
from pyspark.sql import SparkSession
from pyspark.sql.window import Window
from pyspark.sql.functions import rank, sum

spark = SparkSession.builder.appName("WindowFunctionExample").getOrCreate()

# Crear un DataFrame de ejemplo
data = [("Alice", "Sales", 3000),
        ("Bob", "Sales", 4000),
        ("Charlie", "Marketing", 4500),
        ("David", "Sales", 3500),
        ("Eve", "Marketing", 3800)]
df = spark.createDataFrame(data, ["name", "department", "salary"])

# Definir una ventana particionada por departamento y ordenada por salario
windowSpec = Window.partitionBy("department").orderBy("salary")

# Calcular el rango y el salario acumulado dentro de cada departamento
result = df.withColumn("rank", rank().over(windowSpec)) \
           .withColumn("cumulative_salary", sum("salary").over(windowSpec))

result.show()

spark.stop()
```

### 6.2 UDFs (User-Defined Functions)

Las UDFs permiten extender las capacidades de PySpark SQL con funciones personalizadas.

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import udf
from pyspark.sql.types import StringType

spark = SparkSession.builder.appName("UDFExample").getOrCreate()

# Definir una UDF para categorizar salarios
@udf(returnType=StringType())
def categorize_salary(salary):
    if salary < 3500:
        return "Low"
    elif salary < 4500:
        return "Medium"
    else:
        return "High"

# Crear un DataFrame de ejemplo
data = [("Alice", 3000),
        ("Bob", 4000),
        ("Charlie", 5000)]
df = spark.createDataFrame(data, ["name", "salary"])

# Usar la UDF en una transformación de DataFrame
result = df.withColumn("salary_category", categorize_salary(df.salary))

result.show()

spark.stop()
```

### 6.3 Manejo de datos complejos

PySpark puede manejar estructuras de datos complejas como arrays y mapas.

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import explode, size

spark = SparkSession.builder.appName("ComplexDataExample").getOrCreate()

# Crear un DataFrame con datos complejos
data = [(1, ["apple", "banana"], {"a": 1, "b": 2}),
        (2, ["orange", "grape"], {"x": 3, "y": 4})]
df = spark.createDataFrame(data, ["id", "fruits", "scores"])

# Operaciones con arrays
with_array_length = df.withColumn("fruit_count", size(df.fruits))

# Operaciones con mapas
with_map_value = df.withColumn("score_a", df.scores.getItem("a"))

# Explotar (explode) un array
exploded_fruits = df.select(df.id, explode(df.fruits).alias("fruit"))

with_array_length.show()
with_map_value.show()
exploded_fruits.show()

spark.stop()
```

## 7. Funciones clave de PySpark

### Operaciones de RDD

1. `parallelize()`: Crea un RDD a partir de una colección local.
2. `map()`: Aplica una función a cada elemento del RDD.
3. `flatMap()`: Similar a map, pero cada entrada puede generar 0 o más salidas.
4. `filter()`: Selecciona elementos que cumplen una condición.
5. `reduce()`: Agrega los elementos del RDD usando una función asociativa.
6. `collect()`: Retorna todos los elementos del RDD al driver.
7. `count()`: Retorna el número de elementos en el RDD.
8. `take(n)`: Retorna los primeros n elementos del RDD.

### Operaciones de Pares Clave-Valor

9. `reduceByKey()`: Combina valores para cada clave usando una función asociativa.
10. `groupByKey()`: Agrupa valores para cada clave.
11. `sortByKey()`: Ordena un RDD de pares (K,V) basado en la clave.
12. `join()`: Realiza un join interno entre dos RDDs de pares.
13. `cogroup()`: Agrupa datos de varios RDDs por clave.

### Transformaciones de DataFrames

14. `select()`: Selecciona una lista de columnas.
15. `filter()` / `where()`: Filtra filas usando la condición dada.
16. `groupBy()`: Agrupa el DataFrame usando las columnas especificadas.
17. `agg()`: Realiza agregaciones después de un groupBy.
18. `orderBy()` / `sort()`: Ordena el DataFrame por las columnas especificadas.
19. `drop()`: Elimina columnas especificadas.
20. `withColumn()`: Añade una nueva columna o reemplaza una existente.
21. `withColumnRenamed()`: Renombra una columna existente.
22. `join()`: Realiza un join entre DataFrames.

### Funciones de ventana

23. `Window.partitionBy()`: Define una partición para operaciones de ventana.
24. `Window.orderBy()`: Ordena dentro de una partición para operaciones de ventana.
25. `Window.rowsBetween()`: Define el frame de la ventana basado en filas.
26. `lead()`: Accede a un valor en una fila subsiguiente.
27. `lag()`: Accede a un valor en una fila anterior.

### Funciones de agregación

28. `sum()`: Calcula la suma de los valores en un grupo.
29. `avg()`: Calcula el promedio de los valores en un grupo.
30. `max()`: Encuentra el valor máximo en un grupo.
31. `min()`: Encuentra el valor mínimo en un grupo.
32. `count()`: Cuenta el número de filas en un grupo.
33. `countDistinct()`: Cuenta el número de valores distintos en un grupo.

### Funciones de fecha y tiempo

34. `to_date()`: Convierte una columna a tipo fecha.
35. `date_format()`: Formatea una fecha según un patrón específico.
36. `datediff()`: Calcula la diferencia entre dos fechas.
37. `months_between()`: Calcula el número de meses entre dos fechas.
38. `unix_timestamp()`: Convierte una fecha/hora a timestamp Unix.

### Funciones de cadena

39. `concat()`: Concatena dos o más cadenas.
40. `substring()`: Extrae una subcadena.
41. `lower()`: Convierte una cadena a minúsculas.
42. `upper()`: Convierte una cadena a mayúsculas.
43. `trim()`: Elimina espacios en blanco al inicio y final de una cadena.
44. `regexp_extract()`: Extrae una parte de la cadena que coincide con un patrón regex.

### UDFs (User-Defined Functions)

45. `udf()`: Crea una función definida por el usuario.
46. `spark.udf.register()`: Registra una UDF para su uso en consultas SQL.

### Operaciones de E/S

47. `read.parquet()`: Lee archivos en formato Parquet.
48. `write.parquet()`: Escribe DataFrames en formato Parquet.
49. `read.csv()`: Lee archivos CSV.
50. `write.csv()`: Escribe DataFrames en formato CSV.
51. `read.json()`: Lee archivos JSON.
52. `write.json()`: Escribe DataFrames en formato JSON.

### Operaciones de Streaming

53. `readStream`: Inicia la definición de un streaming query.
54. `writeStream`: Especifica el destino de salida de un streaming query.
55. `trigger()`: Define cuándo se debe activar el procesamiento de datos en streaming.
56. `outputMode()`: Especifica cómo se deben escribir los resultados del streaming.

### Optimización y rendimiento

57. `cache()`: Almacena en caché el DataFrame.
58. `persist()`: Almacena en caché el DataFrame con un nivel de almacenamiento específico.
59. `unpersist()`: Elimina el DataFrame de la caché.
60. `explain()`: Muestra el plan de ejecución de una consulta.
61. `coalesce()`: Reduce el número de particiones.
62. `repartition()`: Aumenta o disminuye el número de particiones.

### Otras funciones

63. `broadcast()`: Crea una variable de transmisión para optimizar joins.
64. `explode()`: Crea una nueva fila para cada elemento en una colección.
65. `lit()`: Crea una columna con un valor literal.
66. `when()`: Añade una cláusula condicional a una expresión.
67. `over()`: Especifica una ventana para una función de ventana.
68. `partitionBy()`: Especifica las columnas de partición al escribir datos.

## 8. Ejercicios práctivos

### Ejercicio 1

**Objetivo**: Utilizar PySpark para analizar los datos globales de casos confirmados de COVID-19.

**Dataset**: <https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv>

**Instrucciones**:

1. Carga el dataset en un DataFrame de PySpark.

2. Realiza las siguientes operaciones:
   a) Muestra los 10 países con más casos confirmados en la última fecha disponible.
   b) Calcula el total de casos globales para la última fecha disponible.

3. Muestra los resultados de cada operación.

### Ejercicio 2

**Objetivo**: Utilizar PySpark para analizar los datos de viajes en taxi amarillo de Nueva York.

**Dataset**: <https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2023-01.parquet>

**Instrucciones**:

1. Carga el dataset en un DataFrame de PySpark.

2. Realiza las siguientes operaciones:
   a) Calcula el número total de viajes y el ingreso total para todo el mes.
   b) Encuentra la hora del día con más viajes.
   c) Calcula la tarifa promedio por viaje.

3. Muestra los resultados de cada operación.

## Ejercicio final

### Objetivo

Realizar un análisis completo de datos utilizando PySpark y MySQL.

### Instrucciones

1. **Datos**
   - Busca y descarga un conjunto de datos de interés de una fuente en línea.
   - Documenta la fuente y describe brevemente el contenido.

2. **MySQL**
   - Carga los datos en MySQL.

3. **PySpark**
   - Procesa los datos con PySpark:
     - Limpieza y transformación.
     - Análisis exploratorio con DataFrames.
     - Aplica al menos dos técnicas avanzadas (ej. Window Functions, UDFs).

4. **Análisis**
   - Extrae al menos tres informaciones significativos.

5. **Visualización**
   - Crea al menos una visualización significativa de tus resultados.
   - Exporta los resultados de tu análisis a un archivo CSV y luego impórtalos en una herramienta externa como Excel, Google Sheets o Google Looker Studio para crear la visualización.
   - Incluye una captura de pantalla o imagen de tu visualización en el informe.
