# Guía avanzada de Apache Spark

En relación con los contenidos del curso, se corresponde con:

- Módulo 1:
  - Procesamiento de datos.
  - Analítica de Big Data en los ecosistemas de almacenamiento.

- Módulo 2:
  - Sustituye MapReduce como herramienta de procesamiento.
  - Complementa Consultas Pig y Hive con SparkSQL.

## 1. Introducción a Apache Spark

Apache Spark es un motor de procesamiento de datos distribuido diseñado para ser rápido y versátil. Ofrece APIs en Scala, Java, Python y R, y soporta diversos tipos de procesamiento de datos, incluyendo procesamiento por lotes, análisis interactivo, procesamiento de streaming y aprendizaje automático.

### Características clave

- Procesamiento en memoria para mayor velocidad
- Soporte para computación distribuida
- APIs de alto nivel para facilitar el desarrollo
- Integración con diversos sistemas de almacenamiento (HDFS, S3, Cassandra, etc.)
- Ecosistema rico que incluye herramientas para SQL, ML, grafos y streaming

### Arquitectura

- **Driver Program**: Coordina la ejecución de la aplicación Spark.
- **Cluster Manager**: Gestiona los recursos del clúster (puede ser Standalone, YARN, Mesos o Kubernetes).
- **Executors**: Procesos en los nodos trabajadores que ejecutan tareas.
- **Tasks**: Unidades de trabajo enviadas a los executors.
- **Stages**: Conjuntos de tareas que pueden ejecutarse en paralelo.
- **DAG Scheduler**: Optimiza el plan de ejecución creando un grafo acíclico dirigido (DAG) de stages.

## 2. Ejecución de Spark

### 2.1 Instalar VSCode

Si aún no tienes VSCode instalado, descárgalo e instálalo desde <https://code.visualstudio.com>.

### 2.2 Instalar extensiones necesarias

Abre VSCode y instala las siguientes extensiones:

- Scala (Metals): Proporciona soporte para Scala.
- Scala Syntax (official): Mejora el resaltado de sintaxis para Scala.
- sbt: Ofrece soporte para el sistema de construcción sbt.

Para instalar estas extensiones:

- Ve a la vista de extensiones (icono de cuadrados en la barra lateral izquierda o Ctrl+Shift+X).
- Busca cada extensión por su nombre.
- Haz clic en "Install" para cada una.

### 2.3 Crear un proyecto Spark

#### 2.3.1 Crear un nuevo directorio

Crea un nuevo directorio desde el terminal conectado al contenedor de Docker.

#### 2.3.2 Inicializar un proyecto sbt

En la terminal, ejecuta:

```bash
sbt new scala/hello-world.g8
```

Sigue las instrucciones para nombrar tu proyecto.

Accede a la carpeta creada:

```bash
cd miproyectospark
```

#### 2.3.3 Configurar build.sbt

Abre el archivo ```build.sbt``` en la raíz del proyecto y modifícalo para incluir las dependencias de Spark:

```scala
scalaVersion := "2.12.19"
name := "MiProyectoSpark"
version := "0.1"
val sparkVersion = "3.2.0"
val hadoopVersion = "3.4.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
  "org.apache.logging.log4j" % "log4j-api" % "2.20.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.20.0",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.20.0"
)

fork := true

javaOptions ++= Seq(
  "-Dlog4j.configurationFile=log4j2.xml",
  "--add-opens=java.base/java.lang=ALL-UNNAMED",
  "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
  "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
  "--add-opens=java.base/java.io=ALL-UNNAMED",
  "--add-opens=java.base/java.net=ALL-UNNAMED",
  "--add-opens=java.base/java.nio=ALL-UNNAMED",
  "--add-opens=java.base/java.util=ALL-UNNAMED",
  "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
  "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED",
  "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
  "--add-opens=java.base/sun.nio.cs=ALL-UNNAMED",
  "--add-opens=java.base/sun.security.action=ALL-UNNAMED",
  "--add-opens=java.base/sun.util.calendar=ALL-UNNAMED",
  "-Dio.netty.tryReflectionSetAccessible=true"
)

run / javaOptions ++= Seq(
  "-Dlog4j.configurationFile=log4j2.xml"
)
```

#### 2.3.4 Configurar Log4j2

Crea el directorio ```src/main/resources/```, en el genera un archivo llamado ```log4j2.xml``` con el siguiente contenido:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="OFF">
    <Appenders>
        <Console name="Console" target="SYSTEM_ERR">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="OFF">
            <AppenderRef ref="Console"/>
        </Root>
        <Logger name="org.apache.spark" level="OFF" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>
        <Logger name="org.apache.hadoop" level="OFF" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>
        <Logger name="org.spark-project" level="OFF" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>
    </Loggers>
</Configuration>
```

En el mismo drectorio genera un archivo llamado ```log4j.properties``` con el siguiente contenido:

```bash
# Set root logger level to OFF
rootLogger.level = OFF

# Console appender configuration
appender.console.type = Console
appender.console.name = console
appender.console.layout.type = PatternLayout
appender.console.layout.pattern = %d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n

# Root logger referring to console appender
rootLogger.appenderRef.stdout.ref = console

# Set all loggers to OFF
logger.spark.name = org.apache.spark
logger.spark.level = OFF
logger.hadoop.name = org.apache.hadoop
logger.hadoop.level = OFF
logger.netty.name = io.netty
logger.netty.level = OFF
logger.zookeeper.name = org.apache.zookeeper
logger.zookeeper.level = OFF
```

#### 2.3.4 Escribir código Spark

Crear un archivo Scala

- En el explorador de archivos, navega a ```src/main/scala```.
- Crea un nuevo archivo llamado ```MiAppSpark.scala```.
- Añade el siguiente código a ```MiAppSpark.scala```:

```scala
import org.apache.spark.sql.SparkSession
import org.apache.log4j.{Level, Logger}

object MiAppSpark {
    def main(args: Array[String]): Unit = {
        // Configurar el nivel de log globalmente
        Logger.getLogger("org").setLevel(Level.OFF)
        Logger.getLogger("akka").setLevel(Level.OFF)
        Logger.getRootLogger.setLevel(Level.OFF)

        // Desactivar el logging de Spark
        System.setProperty("spark.logging.initialized", "true")

        // Crear una sesión de Spark con configuración optimizada
        val spark = SparkSession.builder()
            .appName("MiAppSpark")
            .master("local[*]")
            .config("spark.driver.memory", "2g")
            .config("spark.executor.memory", "2g")
            .config("spark.log.level", "OFF")
            .config("spark.sql.legacy.createHiveTableByDefault", "false")
            .config("spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version", "2")
            .config("spark.ui.showConsoleProgress", "false")
            .config("spark.sql.catalogImplementation", "in-memory")
            .config("spark.driver.extraJavaOptions", "-Dlog4j.configuration=log4j2.properties")
            .config("spark.executor.extraJavaOptions", "-Dlog4j.configuration=log4j2.properties")
            .getOrCreate()

        // Configurar el nivel de log para Spark
        spark.sparkContext.setLogLevel("OFF")

        // Desactivar el logging de Hadoop
        val hadoopConf = spark.sparkContext.hadoopConfiguration
        hadoopConf.set("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false")

        // Crear un DataFrame de ejemplo
        val df = spark.createDataFrame(Seq(
        (1, "Alice", 25),
        (2, "Bob", 30),
        (3, "Charlie", 35)
        )).toDF("id", "name", "age")

        // Mostrar el contenido del DataFrame
        df.show()

        // Realizar una operación simple
        val resultDF = df.filter(df("age") > 28)
        resultDF.show()

        // Detener la sesión de Spark
        spark.stop()
    }
}
```

#### 2.3.5 Compilar el proyecto

Ejecuta el comando:

```bash
sbt compile
```

Este comando compilará tu proyecto y descargará todas las dependencias necesarias.

- Opción 1: Usar sbt run

Ejecuta el siguiente comando especifico para ejecutar un archivo concreto:

```bash
sbt "runMain MiAppSpark"
```

Donde ```MiAppSpark``` es el nombre de la clase principal que contiene el método ```main``` que deseas ejecutar.

Para ver y seleccionar entre múltiples clases ejecutables:

```bash
sbt run
```

Si tu proyecto contiene múltiples clases con métodos ```main```, sbt te proporcionará un listado numerado de todas las clases ejecutables disponibles. Por ejemplo:

```bash
Multiple main classes detected. Select one to run:
 [1] com.example.MiAppSpark
 [2] com.example.OtraAppSpark
 [3] com.example.TerceraAppSpark

Enter number:
```

Simplemente ingresa el número correspondiente a la clase que deseas ejecutar y presiona Enter.

- Opción 2: Compilar y ejecutar en un solo paso

Si deseas compilar y ejecutar en un solo comando, puedes usar:

```bash
sbt compile run
```

Este comando compilará tu proyecto y luego te presentará la lista de clases ejecutables si hay más de una, igual que con ```sbt run```.

- Opción 3: Usar spark-submit

Para usar ```spark-submit```, primero debes empaquetar tu aplicación:

```bash
sbt package
```

Luego, usa ```spark-submit``` para ejecutar la aplicación:

```bash
spark-submit \
  --class MiAppSpark \
  --master local[*] \
  target/scala-2.12/miproyectospark_2.12-0.1.jar
```

Asegúrate de que la ruta al ```jar``` y el nombre de la clase sean correctos según tu configuración.

#### 2.3.5 Limpiar la compilación

Para limpiar los archivos compilados:

```bash
sbt clean
```

#### 2.3.6 Notas adicionales

- La configuración proporcionada está diseñada para minimizar los mensajes de log y evitar errores comunes.
- Si necesitas ver más información de log, puedes ajustar los niveles en el archivo log4j2.xml.
- Asegúrate de que todos los archivos de configuración (build.sbt, log4j2.xml) estén en las ubicaciones correctas.

## 3. Conceptos fundamentales de Spark

### 3.1 RDD (Resilient Distributed Dataset)

RDD es la estructura de datos fundamental en Spark. Es una colección distribuida e inmutable de objetos que puede ser procesada en paralelo.

Características clave:

- Inmutabilidad: Los RDDs no pueden ser modificados una vez creados, lo que facilita la recuperación en caso de fallos.
- Distribución: Los datos se distribuyen automáticamente a través del clúster.
- Tolerancia a fallos: Spark puede reconstruir partes perdidas de un RDD utilizando su linaje.
- Evaluación perezosa (lazy evaluation): Las transformaciones no se ejecutan hasta que se invoca una acción.

#### Ejemplo de creación y uso de RDD

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.HashPartitioner

object RDDExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("RDDExample")
      .master("local[*]")
      .getOrCreate()

    // Crear un RDD a partir de una colección
    val rdd = spark.sparkContext.parallelize(1 to 1000000, 100)

    // Aplicar transformaciones complejas
    val resultRDD = rdd
      .map(x => (x % 10, x))
      .repartitionAndSortWithinPartitions(new HashPartitioner(10))
      .mapValues(x => x * x)
      .reduceByKey(_ + _)

    // Acción para materializar el resultado
    val result = resultRDD.collect()

    // Imprimir los primeros 10 elementos del resultado
    result.take(10).foreach(println)

    // Detener la sesión de Spark
    spark.stop()
  }
}
```

### 3.2 DataFrame y Dataset

DataFrame y Dataset son APIs estructuradas que proporcionan una abstracción más rica y optimizada que los RDDs.

- DataFrame: Colección distribuida de datos organizados en columnas nombradas.
- Dataset: Extensión tipada de DataFrame que proporciona una interfaz orientada a objetos.

#### Ejemplo de uso de DataFrame y Dataset

```scala
import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.sql.expressions.Window
import java.sql.Date

// Definir un objeto principal
object DataExample {
  // Definir un caso de clase para el Dataset
  case class Venta(fecha: java.sql.Date, producto: String, cantidad: Int, precio: Double)

  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("DataExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Crear una secuencia de datos simulando las ventas
    val ventas = Seq(
      Venta(Date.valueOf("2024-09-01"), "ProductoA", 10, 20.5),
      Venta(Date.valueOf("2024-09-01"), "ProductoB", 5, 10.0),
      Venta(Date.valueOf("2024-09-02"), "ProductoA", 7, 20.5),
      Venta(Date.valueOf("2024-09-02"), "ProductoB", 8, 10.0),
      Venta(Date.valueOf("2024-09-03"), "ProductoA", 15, 20.5),
      Venta(Date.valueOf("2024-09-03"), "ProductoB", 6, 10.0),
      Venta(Date.valueOf("2024-09-04"), "ProductoA", 14, 20.5),
      Venta(Date.valueOf("2024-09-04"), "ProductoB", 10, 10.0),
      Venta(Date.valueOf("2024-09-05"), "ProductoA", 12, 20.5),
      Venta(Date.valueOf("2024-09-05"), "ProductoB", 9, 10.0),
      Venta(Date.valueOf("2024-09-06"), "ProductoA", 11, 20.5),
      Venta(Date.valueOf("2024-09-06"), "ProductoB", 7, 10.0),
      Venta(Date.valueOf("2024-09-07"), "ProductoA", 9, 20.5),
      Venta(Date.valueOf("2024-09-07"), "ProductoB", 5, 10.0)
    )

    // Crear un Dataset a partir de la secuencia de datos
    val ventasDS = ventas.toDS()

    // Realizar operaciones complejas
    val resultadoDF = ventasDS
      .groupBy($"fecha", $"producto")
      .agg(F.sum($"cantidad").as("total_cantidad"), F.sum($"precio" * $"cantidad").as("total_ventas"))
      .withColumn("promedio_7_dias", 
        F.avg($"total_ventas").over(Window.partitionBy($"producto")
          .orderBy($"fecha")
          .rowsBetween(-6, 0))) // Cambiado a -6 para incluir 7 días en total
      .filter($"total_ventas" > $"promedio_7_dias")

    // Mostrar el resultado
    resultadoDF.show()

    // Detener la sesión de Spark
    spark.stop()
  }
}

```

### 3.3 SparkSQL

SparkSQL permite ejecutar consultas SQL sobre los datos en Spark, proporcionando una interfaz familiar para los analistas de datos.

#### Ejemplo de uso de SparkSQL

```scala
// Registrar el DataFrame como una vista temporal
resultadoDF.createOrReplaceTempView("ventas_resumen")

// Ejecutar una consulta SQL compleja
val resultadoSQL = spark.sql("""
  WITH ventas_ranking AS (
    SELECT 
      fecha,
      producto,
      total_ventas,
      RANK() OVER (PARTITION BY producto ORDER BY total_ventas DESC) as rank
    FROM ventas_resumen
  )
  SELECT 
    v.fecha,
    v.producto,
    v.total_ventas,
    vr.rank
  FROM ventas_resumen v
  JOIN ventas_ranking vr ON v.fecha = vr.fecha AND v.producto = vr.producto
  WHERE vr.rank <= 3
  ORDER BY v.producto, vr.rank
""")

resultadoSQL.show()
```

## 4. Procesamiento de datos con Spark

### 4.1 Transformaciones y acciones

Spark ofrece una rica API de transformaciones y acciones para manipular datos.

#### Ejemplo de transformaciones

```scala
import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import java.sql.Timestamp

// Definir un objeto principal
object FunctionsExample {
  // Definir un caso de clase para el Dataset
  case class Evento(timestamp: Timestamp, usuario_id: String, tipo_evento: String, monto: Double)

  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("FunctionsExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Crear una secuencia de datos simulando los eventos
    val eventos = Seq(
      Evento(Timestamp.valueOf("2024-09-01 10:00:00"), "user1", "compra", 100.0),
      Evento(Timestamp.valueOf("2024-09-01 12:30:00"), "user2", "compra", 200.0),
      Evento(Timestamp.valueOf("2024-09-02 09:15:00"), "user1", "visita", 0.0),
      Evento(Timestamp.valueOf("2024-09-03 15:45:00"), "user3", "compra", 150.0),
      Evento(Timestamp.valueOf("2024-09-04 16:30:00"), "user4", "visita", 0.0),
      Evento(Timestamp.valueOf("2024-09-05 14:00:00"), "user2", "compra", 300.0),
      Evento(Timestamp.valueOf("2024-09-06 18:00:00"), "user5", "visita", 0.0),
      Evento(Timestamp.valueOf("2024-09-07 20:00:00"), "user1", "compra", 400.0),
      Evento(Timestamp.valueOf("2024-09-07 21:30:00"), "user3", "compra", 250.0)
    )

    // Crear un Dataset a partir de la secuencia de datos
    val df = eventos.toDF()

    // Procesar el DataFrame
    val dfProcesado = df
      .withColumn("fecha", to_date($"timestamp"))  // Crear una nueva columna "fecha" convirtiendo el "timestamp" a solo la fecha (sin la hora)
      .withColumn("hora", hour($"timestamp"))  // Crear una nueva columna "hora" extrayendo la hora del "timestamp"
      .withColumn("es_fin_semana", when(dayofweek($"fecha").isin(1, 7), true).otherwise(false))  // Crear una columna booleana "es_fin_semana" que es true si "fecha" es sábado o domingo
      .groupBy($"fecha", $"es_fin_semana")  // Agrupar los datos por "fecha" y si es fin de semana o no
      .agg(
        countDistinct($"usuario_id").as("usuarios_unicos"),  // Contar el número de usuarios únicos ("usuario_id") en cada grupo y almacenarlo en "usuarios_unicos"
        sum(when($"tipo_evento" === "compra", $"monto").otherwise(0)).as("total_ventas")  // Sumar los "monto" de eventos donde "tipo_evento" es "compra" y almacenar en "total_ventas"
      )
      .withColumn("promedio_movil_ventas", 
        avg($"total_ventas").over(Window.partitionBy($"es_fin_semana")  // Calcular el promedio móvil de "total_ventas" para cada grupo de "es_fin_semana"
          .orderBy($"fecha")  // Ordenar por "fecha" para calcular el promedio móvil en orden cronológico
          .rowsBetween(-6, 0)))  // Definir la ventana de promedio móvil como los últimos 7 días (6 días anteriores más el día actual)

    // Mostrar el resultado
    dfProcesado.show()

    // Detener la sesión de Spark
    spark.stop()
  }
}
```

### 4.2 Optimización de consultas

Catalyst es el motor de optimización de consultas que Apache Spark utiliza para transformar y optimizar las consultas que se realizan sobre DataFrames y Datasets. Introducido con Spark SQL, Catalyst es fundamental para asegurar que las consultas sean ejecutadas de la manera más eficiente posible sobre el clúster de Spark. A continuación, se explica en mayor detalle cómo Catalyst funciona y cómo se pueden aprovechar sus capacidades.

#### ¿Qué es Catalyst?

Catalyst es un framework extensible para la optimización de consultas dentro de Spark SQL. Está diseñado para trabajar de manera efectiva con consultas SQL, DataFrames, y Datasets. Su objetivo principal es traducir las consultas de alto nivel en un plan de ejecución que sea lo más eficiente posible, teniendo en cuenta la estructura del clúster y la distribución de los datos.

Catalyst se compone de varios componentes clave que juntos permiten optimizar las consultas de manera extensiva:

1. Árboles de expresiones (Expression Trees):
   - Catalyst representa las consultas como árboles de expresiones. Cada nodo en el árbol representa una operación, como un filtro, un join, o una agregación. Estos árboles permiten a Catalyst aplicar reglas de optimización de manera flexible y extensible.
2. Transformaciones basadas en reglas (Rule-based Optimizations):
   - Catalyst aplica una serie de reglas de optimización a los árboles de expresiones. Estas reglas pueden incluir, por ejemplo, la simplificación de expresiones (eliminación de operaciones redundantes), la reordenación de joins para minimizar los datos procesados, y la poda de predicados (aplicar filtros lo antes posible en la consulta).
   - Estas reglas son claves para mejorar el rendimiento de las consultas y son aplicadas de forma iterativa hasta que no se puedan hacer más optimizaciones.
3. Planificación de la consulta (Query Planning):
   - Catalyst genera diferentes planes de ejecución posibles (plan lógico, plan físico) para una consulta dada y elige el que se espera que sea más eficiente.
   - Plan lógico: Es una representación abstracta de las operaciones necesarias para ejecutar la consulta, sin tener en cuenta los detalles físicos como la distribución de datos o las operaciones de I/O.-
   - Plan físico: Este es un plan más detallado que tiene en cuenta la distribución de los datos, las particiones y las características del hardware. El plan físico se basa en el plan lógico pero incluye consideraciones prácticas para la ejecución.
4. Optimización basada en costes (Cost-based Optimizations, CBO):
   - Catalyst también puede realizar optimizaciones basadas en costes, donde se estima el coste (en términos de tiempo y recursos) de diferentes planes de ejecución. Luego, selecciona el plan con el menor coste estimado.\
   - CBO se basa en estadísticas sobre los datos (como el tamaño de las tablas, la cardinalidad de las columnas) para tomar decisiones más informadas sobre el plan de ejecución.

#### ¿Cómo funciona Catalyst?

Catalyst sigue un flujo de trabajo en etapas para transformar una consulta de alto nivel en un plan de ejecución optimizado:

1. Análisis (Analysis):
   - Se analizan las consultas y se validan para asegurarse de que son sintáctica y semánticamente correctas. En esta etapa, se resuelven referencias a columnas, tipos de datos, etc.
2. Optimización lógica (Logical Optimization):
   - Catalyst aplica una serie de reglas de optimización a la versión lógica de la consulta. Esta optimización incluye la eliminación de operaciones redundantes, la combinación de operaciones similares, y la reordenación de las operaciones para maximizar la eficiencia.
3. Optimización física (Physical Planning):
   - En esta etapa, Catalyst convierte el plan lógico optimizado en un plan físico. Aquí, se eligen las estrategias físicas para cada operación, como la implementación de joins utilizando broadcast cuando es apropiado, o la elección de un tipo específico de shuffle.
4. Generación del código (Code Generation):
   - Finalmente, Catalyst genera el código de ejecución (generalmente en bytecode JVM) que será ejecutado en los nodos del clúster. Este código incluye todas las optimizaciones previamente decididas.

#### Ejemplo de cómo Catalyst optimiza una consulta

Tomemos el ejemplo de una consulta simple como un join entre dos DataFrames, seguido de un filter y una aggregation.

```scala
val resultado = df1.join(broadcast(df2), Seq("id"))
  .filter($"fecha" > "2023-01-01")
  .groupBy($"categoria")
  .agg(sum($"ventas").as("total_ventas"))
  .orderBy($"total_ventas".desc)
  ```

En este ejemplo:

- Broadcast Join:
  - Detección y decisión automática: Catalyst identifica que df2 es un DataFrame pequeño cuando se utiliza la función broadcast(df2). Esta función le indica a Catalyst que puede replicar df2 a todos los nodos del clúster en lugar de distribuirlo mediante un shuffle, que es la estrategia utilizada en joins convencionales. En un join típico, los datos deben redistribuirse entre los nodos para que las filas correspondientes se encuentren en el mismo lugar, lo que requiere mover datos a través de la red y puede ser muy costoso en términos de tiempo y recursos.
  - Eficiencia del Broadcast Join: El shuffle es particularmente lento porque implica transferir grandes volúmenes de datos entre los nodos, lo que aumenta la latencia y consume un considerable ancho de banda de la red. Además, el shuffle requiere escribir los datos temporalmente en disco en cada nodo, agregando una carga adicional de I/O y procesamiento. En contraste, el broadcast join evita estos problemas al replicar el DataFrame pequeño (df2) en todos los nodos del clúster. Esto permite que cada nodo realice el join localmente con los datos que ya tiene, eliminando la necesidad de mover datos entre nodos. Como resultado, se reduce drásticamente la latencia y el uso de recursos de red, haciendo que la operación sea mucho más rápida y eficiente. Esta optimización es crucial para mejorar el rendimiento en consultas distribuidas sobre grandes volúmenes de datos.
- Reordenación de operaciones:
  - Catalyst puede reordenar la aplicación del filter (filter($"fecha" > "2023-01-01")) antes de la agregación para reducir la cantidad de datos que necesitan ser agrupados y sumados.
- Eliminación de operaciones innecesarias:
  - Si detecta operaciones que no afectan el resultado final (como columnas que no se utilizan en ningún cálculo posterior), Catalyst las eliminará del plan de ejecución.
- Aplicación de particionamiento:
  - Catalyst también considera cómo están distribuidos los datos. Si los datos están bien particionados, Catalyst puede reducir la cantidad de shuffles y aumentar la paralelización.
- Planificación de la ejecución:
  - Finalmente, Catalyst generará el plan físico óptimo para ejecutar esta consulta en un entorno distribuido, utilizando la planificación basada en costes si es necesario.

#### Ejemplo de optimización

```scala
import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.sql.expressions.Window

object OptimizerExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("OptimizerExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._
    import org.apache.spark.sql.functions._  // Importar las funciones necesarias de Spark SQL

    // Crear un DataFrame simulado para `df1`
    val df1 = Seq(
      (1, "2023-02-01", "Electronica", 1000.0),
      (2, "2023-03-01", "Hogar", 500.0),
      (3, "2022-12-01", "Electronica", 700.0),
      (4, "2023-05-01", "Ropa", 1500.0)
    ).toDF("id", "fecha", "categoria", "ventas")

    // Crear un DataFrame simulado para `df2`
    val df2 = Seq(
      (1, "Descuento"),
      (2, "Promoción"),
      (4, "Descuento"),
      (5, "Ofertas")
    ).toDF("id", "tipo_promocion")

    // Realizar el join entre `df1` y `df2` usando broadcast para `df2`
    val resultado = df1.join(broadcast(df2), Seq("id"))  // Hacer un join en la columna "id" y usar `broadcast` para optimizar si `df2` es pequeño
      .filter($"fecha" > "2023-01-01")  // Filtrar las filas donde la fecha sea posterior al 1 de enero de 2023
      .groupBy($"categoria")  // Agrupar por la columna "categoria"
      .agg(sum($"ventas").as("total_ventas"))  // Sumar las ventas dentro de cada grupo y almacenar en "total_ventas"
      .orderBy($"total_ventas".desc)  // Ordenar los resultados por "total_ventas" en orden descendente

    // Analizar el plan de ejecución
    resultado.explain(true)  // Mostrar el plan de ejecución detallado para entender cómo Spark procesará la consulta

    // Materializar el resultado (en un entorno real, escribiría a un archivo)
    resultado.show()  // En lugar de escribir a un archivo, mostramos el resultado en pantalla

    // Detener la sesión de Spark
    spark.stop()
  }
}
```

En este ejemplo específico, Catalyst realiza varias optimizaciones:

- Optimización del join: Al detectar el uso de broadcast(df2), Catalyst optimiza el join distribuyendo la pequeña tabla df2 a todos los nodos del clúster, evitando movimientos de datos (shuffle).
- Reordenación de operaciones: Catalyst puede reordenar las operaciones de filtrado, agrupación, y agregación para minimizar la cantidad de datos procesados en cada etapa.
- Generación del plan de ejecución: Cuando se llama a resultado.explain(true), Catalyst genera y muestra el plan de ejecución físico, que incluye las decisiones optimizadas sobre cómo se procesarán las consultas en el clúster.

### 4.3 Persistencia y Caching

Spark permite almacenar en caché los DataFrames/Datasets para un acceso más rápido en operaciones repetidas.

```scala
import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.storage.StorageLevel

object CachingExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("CachingExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._
    import org.apache.spark.sql.functions._  // Importar las funciones necesarias de Spark SQL

    // Crear un DataFrame simulado para `df`
    val df = Seq(
      (1, "Electronica", 1000.0),
      (2, "Hogar", 500.0),
      (3, "Electronica", 700.0),
      (4, "Ropa", 1500.0)
    ).toDF("id", "categoria", "ventas")

    // Cachear el DataFrame en memoria y disco
    df.persist(StorageLevel.MEMORY_AND_DISK_SER)  // Persistir el DataFrame usando almacenamiento en memoria y disco serializado

    // Realizar múltiples operaciones sobre el DataFrame cacheado
    val resultado1 = df.groupBy("categoria").agg(sum("ventas").as("total_ventas"))  // Agrupar por categoría y sumar ventas
    val resultado2 = df.join(df, Seq("id"))  // Realizar un join consigo mismo basado en la columna "id"

    // Mostrar los resultados de las operaciones
    resultado1.show()
    resultado2.show()

    // Liberar el caché cuando ya no sea necesario
    df.unpersist()  // Liberar la memoria y espacio en disco ocupados por el caché

    // Detener la sesión de Spark
    spark.stop()
  }
}
```

## 5. Spark Streaming

Spark Streaming permite procesar datos en tiempo real, lo que es esencial para aplicaciones que requieren análisis continuo de flujos de datos, como análisis de logs, monitoreo de sistemas, o procesamiento de eventos de IoT. A partir de Spark 2.0, se introdujo Structured Streaming, que se basa en el modelo de DataFrame/Dataset, proporcionando una API de alto nivel para construir pipelines de procesamiento de datos en tiempo real de manera más simple y robusta. Structured Streaming ofrece una semántica similar a las consultas en batch, pero aplicadas a datos que llegan en tiempo real, permitiendo un procesamiento más natural y optimizado.

Ejemplo de Structured Streaming:

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

object StructuredStreamingExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("StructuredStreamingExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Crear un DataFrame de streaming usando la fuente `rate`
    val streamingDF = spark.readStream
      .format("rate")
      .option("rowsPerSecond", "10")  // Genera 10 filas por segundo
      .load()
      .selectExpr("CAST(timestamp AS STRING)", "value AS id")  // Selecciona y renombra columnas para simular datos

    // Agregar una columna que simule tipos de eventos aleatorios
    val simulatedEventsDF = streamingDF
      .withColumn("tipo_evento", expr("CASE WHEN rand() < 0.5 THEN 'click' ELSE 'view' END"))
      .withColumn("timestamp", $"timestamp".cast("timestamp"))  // Convertir la columna `timestamp` a tipo `timestamp`

    // Procesar el stream
    val procesadoDF = simulatedEventsDF
      .withWatermark("timestamp", "10 minutes")  // Configurar watermark para manejar datos retrasados
      .groupBy(window($"timestamp", "5 minutes"), $"tipo_evento")  // Agrupar por ventanas de tiempo y tipo de evento
      .agg(count("*").as("total_eventos"))  // Contar el número de eventos en cada grupo

    // Especificar el lugar y la forma de salida del stream
    val query = procesadoDF.writeStream
      .outputMode("update")  // Modo de salida: solo mostrar los resultados nuevos o actualizados
      .format("console")  // Salida en la consola para visualización
      .trigger(Trigger.ProcessingTime("10 seconds"))  // Configurar el trigger para procesar los datos cada 10 segundos
      .start()

    // Mantener la aplicación en ejecución hasta que se detenga manualmente
    query.awaitTermination()

    // Detener la sesión de Spark
    spark.stop()
  }
}
```

### Explicación del código

- Fuente rate:
  - Spark tiene una fuente de datos llamada rate que genera datos automáticamente a una velocidad específica (rowsPerSecond). En este ejemplo, estamos generando 10 filas por segundo, cada una con un timestamp y un valor incremental (value).
- Simulación de datos:
  - Se simulan tipos de eventos aleatorios (click o view) utilizando una columna adicional tipo_evento generada con una expresión CASE basada en un valor aleatorio.
- Procesamiento del stream:
  - El DataFrame de streaming (streamingDF) se procesa con una ventana de tiempo de 5 minutos y se cuenta el número de eventos por tipo en cada ventana. Los datos se agregan utilizando groupBy y agg.
    - La ventana de tiempo es un concepto clave en el procesamiento de streams, que permite agrupar y analizar eventos que ocurren dentro de un período específico, en lugar de tratar cada evento de forma aislada. En el contexto de este ejemplo, aunque los datos llegan en tiempo real (cada pocos segundos), la ventana de tiempo permite que Spark agrupe todos esos eventos dentro de intervalos de 5 minutos para su análisis conjunto.
- Salida del stream:
  - Los resultados se escriben en la consola en modo update, y el trigger se establece para procesar los datos cada 10 segundos.
- Ejecución continua:
  - La consulta se ejecuta indefinidamente hasta que se detiene manualmente, permitiendo observar continuamente el procesamiento de los datos generados automáticamente.

## 6. Optimización y ajuste de rendimiento

### 6.1 Particionamiento

El particionamiento adecuado de los datos permite distribuir las operaciones de manera más equilibrada entre los nodos del clúster.

```scala
import org.apache.spark.sql.SparkSession

object PartitioningExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("PartitioningExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Crear un DataFrame simulado
    val df = Seq(
      (1, "2023-02-01", "Electronica", 1000.0),
      (2, "2023-03-01", "Hogar", 500.0),
      (3, "2023-02-01", "Electronica", 700.0),
      (4, "2023-05-01", "Ropa", 1500.0)
    ).toDF("id", "fecha", "categoria", "ventas")

    // Particionamiento por una columna
    val dfParticionado = df.repartition($"fecha")  // Repartir las particiones del DataFrame basado en la columna "fecha"

    // Particionamiento por múltiples columnas con un número específico de particiones
    val dfMultiParticionado = df.repartition(200, $"fecha", $"categoria")  // Repartir en 200 particiones basadas en "fecha" y "categoria"

    // Escritura particionada
    dfParticionado.write
      .partitionBy("fecha", "categoria")  // Escribir los datos particionados por "fecha" y "categoria"
      .bucketBy(10, "id")  // Distribuir los datos en 10 "buckets" basados en la columna "id"
      .saveAsTable("tabla_optimizada")  // Guardar como una tabla particionada y bucketizada
  }
}

```

### 6.3 Ajuste de configuración

Ajustar la configuración de Spark puede tener un impacto significativo en el rendimiento. Aquí te muestro algunos ajustes comunes que puedes aplicar:

```scala
import org.apache.spark.sql.SparkSession

object ConfigurationTuningExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("ConfigurationTuningExample")
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", "200")  // Ajustar el número de particiones para operaciones de shuffle
      .config("spark.executor.memory", "10g")  // Configurar la memoria del executor
      .config("spark.driver.memory", "4g")  // Configurar la memoria del driver
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")  // Usar KryoSerializer para optimizar la serialización
      .config("spark.speculation", "true")  // Activar la especulación para manejar tareas lentas
      .getOrCreate()

    // Aquí se incluirían operaciones de Spark que se beneficien de la configuración ajustada

    // Detener la sesión de Spark
    spark.stop()
  }
}
```

## 7. Integración con sistemas externos

### 7.1 Lectura y escritura de datos CSV

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import scala.io.Source
import java.io.PrintWriter
import java.io.File

object IrisDatasetExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("IrisDatasetExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // URL del dataset de Iris
    val url = "https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data"

    // Descargar el dataset de Iris
    val irisData = Source.fromURL(url).mkString

    // Guardar el dataset en un archivo temporal
    val tempFile = new File("iris.csv")
    new PrintWriter(tempFile) { write(irisData); close() }

    // Especificar los nombres de las columnas ya que el dataset original no tiene cabecera
    val irisSchema = Seq("sepal_length", "sepal_width", "petal_length", "petal_width", "species")

    // Leer el CSV usando Spark
    val dfCSV = spark.read
      .option("header", "false")  // El archivo no tiene cabecera
      .option("inferSchema", "true")  // Inferir el esquema automáticamente
      .option("dateFormat", "yyyy-MM-dd")  // Configurar el formato de fecha (aunque no aplica en este dataset)
      .csv(tempFile.getAbsolutePath)  // Leer el archivo CSV

    // Asignar los nombres de las columnas
    val dfIris = dfCSV.toDF(irisSchema: _*)

    // Modificar el DataFrame: Añadir una columna que calcule la relación longitud/ancho de sépalo
    val dfModificado = dfIris.withColumn("sepal_ratio", $"sepal_length" / $"sepal_width")

    // Mostrar la modificación realizada
    dfModificado.show(10)

    // Detener la sesión de Spark
    spark.stop()
  }
}
```

### 7.2 HDFS

HDFS es un sistema de archivos distribuido diseñado para almacenar grandes volúmenes de datos en clústeres de computadoras.

```scala
import org.apache.spark.sql.SparkSession
import org.apache.hadoop.fs.{FileSystem, Path, FSDataOutputStream}
import java.io.{BufferedReader, InputStreamReader}
import scala.io.{Source, Codec}
import org.apache.hadoop.conf.Configuration

object HDFSFileHandlingExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("HDFSFileHandlingExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // URL del archivo de texto a descargar
    val url = "https://www.gutenberg.org/files/11/11-0.txt"  // "Alice's Adventures in Wonderland" de Lewis Carroll

    // Descargar el archivo de texto con la codificación UTF-8
    implicit val codec: Codec = Codec.UTF8
    val textData = Source.fromURL(url).reader()  // Obtener un Reader directamente desde la fuente

    // Configurar HDFS
    val hdfsUri = "hdfs://localhost:9000"  // Reemplaza con la URI de tu HDFS
    val hdfsInputPath = s"$hdfsUri/curso/alice.txt"
    val hdfsOutputPath = s"$hdfsUri/curso/alice_processed.txt"

    // Obtener el sistema de archivos HDFS
    val hadoopConf = new Configuration()
    hadoopConf.set("fs.defaultFS", hdfsUri)
    val hdfs = FileSystem.get(hadoopConf)

    // Crear un archivo en HDFS para escribir en él
    val outputStream: FSDataOutputStream = hdfs.create(new Path(hdfsInputPath))

    // Leer y subir el archivo línea por línea para evitar transferencias grandes
    val bufferedReader = new BufferedReader(textData)  // Utilizar BufferedReader para leer eficientemente
    var line: String = null
    while ({ line = bufferedReader.readLine(); line != null }) {
      outputStream.writeBytes(line + "\n")
    }

    bufferedReader.close()
    outputStream.close()
    println(s"Archivo subido a HDFS: $hdfsInputPath")

    // Lectura del archivo de texto desde HDFS
    val dfHDFS = spark.read.textFile(hdfsInputPath)

    // Mostrar algunas líneas del archivo leído desde HDFS
    dfHDFS.show(10, truncate = false)

    // Escritura del DataFrame en un archivo de texto en HDFS
    dfHDFS.write.text(hdfsOutputPath)
    println(s"Archivo de texto procesado guardado en HDFS: $hdfsOutputPath")

    // Detener la sesión de Spark
    spark.stop()
  }
}
```

Este código realiza una operación completa de manejo de archivos utilizando Spark y HDFS. Primero, descarga un archivo de texto desde una URL (en este caso, "Alice's Adventures in Wonderland" de Lewis Carroll) usando Scala. El archivo descargado se lee línea por línea para evitar transferencias grandes que puedan causar problemas de rendimiento o errores en HDFS. A medida que se lee cada línea, se escribe directamente en un archivo en HDFS utilizando un FSDataOutputStream, lo que permite que el archivo se almacene en el sistema de archivos distribuido de Hadoop sin sobrecargar la red ni los recursos del sistema.

Una vez que el archivo ha sido subido a HDFS, el código utiliza Spark para leer el archivo desde HDFS y cargarlo en un DataFrame. Este DataFrame es luego mostrado parcialmente (las primeras 10 líneas) para confirmar que la lectura se realizó correctamente. Finalmente, el DataFrame se guarda nuevamente en HDFS en un nuevo archivo.

### 7.3 Parquet

Parquet es un formato de almacenamiento columnar que ofrece una compresión eficiente y un rendimiento de consulta rápido para Big Data.

#### ¿Qué es Parquet?

- Formato de archivo columnar para Hadoop.
- Diseñado para un rendimiento y compresión eficientes.
- Soporta esquemas complejos y anidados.
- Altamente eficiente para consultas que solo acceden a ciertas columnas.

#### Cómo usar Parquet en Spark

```scala
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.Configuration
import java.net.URL
import java.io.{File, FileOutputStream}

object ParquetHandlingExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("ParquetHandlingExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // URL del archivo Parquet a descargar (yellow taxi trip records)
    val url = "https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2022-01.parquet"
    
    // Descargar el archivo Parquet
    val localFilePath = "yellow_tripdata_2022-01.parquet"
    downloadFile(url, localFilePath)
    println(s"Archivo Parquet descargado en: $localFilePath")

    // Leer el archivo Parquet local
    val dfParquetLocal = spark.read.parquet(localFilePath)
    println("DataFrame leido del archivo Parquet local:")
    dfParquetLocal.show(5)
    println(s"Numero de filas: ${dfParquetLocal.count()}")
    println("Esquema del DataFrame:")
    dfParquetLocal.printSchema()

    // Configurar HDFS
    val hdfsUri = "hdfs://localhost:9000"  // Reemplaza con la URI de tu HDFS
    val hdfsInputPath = s"$hdfsUri/curso/yellow_taxi_2022-01.parquet"
    val hdfsOutputPath = s"$hdfsUri/curso/yellow_taxi_2022-01_processed.parquet"

    // Obtener el sistema de archivos HDFS
    val hadoopConf = new Configuration()
    hadoopConf.set("fs.defaultFS", hdfsUri)
    val hdfs = FileSystem.get(hadoopConf)

    // Copiar el archivo Parquet a HDFS
    hdfs.copyFromLocalFile(new Path(localFilePath), new Path(hdfsInputPath))
    println(s"Archivo Parquet copiado a HDFS: $hdfsInputPath")

    // Lectura del archivo Parquet desde HDFS
    val dfParquetHDFS = spark.read.parquet(hdfsInputPath)
    println("DataFrame leido desde HDFS (Parquet):")
    dfParquetHDFS.show(5)

    // Realizar una operación simple: filtrar las filas donde trip_distance > 5.0
    val dfFiltered = dfParquetHDFS.filter($"trip_distance" > 5.0)
    println("DataFrame filtrado (viajes con distancia > 5.0 millas):")
    dfFiltered.show(5)

    // Guardar el DataFrame filtrado en un nuevo archivo Parquet en HDFS
    dfFiltered.write.parquet(hdfsOutputPath)
    println(s"DataFrame filtrado guardado en HDFS: $hdfsOutputPath")

    // Detener la sesión de Spark
    spark.stop()
  }

  def downloadFile(url: String, localFilePath: String): Unit = {
    val connection = new URL(url).openConnection()
    val inputStream = connection.getInputStream
    val outputStream = new FileOutputStream(new File(localFilePath))

    try {
      val buffer = new Array[Byte](4096)
      var bytesRead = inputStream.read(buffer)
      while (bytesRead != -1) {
        outputStream.write(buffer, 0, bytesRead)
        bytesRead = inputStream.read(buffer)
      }
    } finally {
      inputStream.close()
      outputStream.close()
    }
  }
}
```

#### Diferencias entre Parquet y HDFS

- Naturaleza:
  - Parquet: Es un formato de archivo.
  - HDFS: Es un sistema de archivos distribuido.
- Estructura:
  - Parquet: Almacenamiento columnar.
  - HDFS: Puede almacenar cualquier tipo de archivo, incluyendo Parquet.
- Compresión:
  - Parquet: Ofrece compresión eficiente incorporada.
  - HDFS: La compresión depende del formato de archivo almacenado.
- Rendimiento de consulta:
  - Parquet: Optimizado para consultas que acceden a subconjuntos de columnas.
  - HDFS: El rendimiento depende del formato de archivo y la implementación.
- Uso en Spark:
  - Parquet: Formato preferido para almacenamiento en Spark debido a su eficiencia.
  - HDFS: Se usa como sistema de almacenamiento subyacente, sobre el cual se pueden almacenar archivos Parquet.

En la práctica, a menudo se usa Parquet como formato de archivo dentro de HDFS, combinando las ventajas de ambos: el almacenamiento distribuido de HDFS con la eficiencia de consulta y compresión de Parquet.

## 8. Gestión de Datos y Operaciones Avanzadas

### 8.1 Window Functions

Las funciones de ventana en Spark permiten realizar cálculos avanzados como totales acumulados, rankings y medias móviles, sobre un conjunto de filas relacionadas, definidas por una partición y un orden específico. Estas funciones son especialmente poderosas porque eliminan la necesidad de operaciones complejas como uniones y agrupaciones que suelen ser costosas en términos de rendimiento. Al realizar cálculos directamente sobre los datos en una sola pasada, las funciones de ventana minimizan la latencia y mejoran la eficiencia del procesamiento. Además de su eficiencia, las funciones de ventana hacen que el código sea más legible y fácil de mantener.

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

object WindowFunctionExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("WindowFunctionExample").master("local[*]").getOrCreate()
    import spark.implicits._

    // Crear un DataFrame de ejemplo
    val data = Seq(
      ("Alice", "Sales", 3000),
      ("Bob", "Sales", 4000),
      ("Charlie", "Marketing", 4500),
      ("David", "Sales", 3500),
      ("Eve", "Marketing", 3800)
    ).toDF("name", "department", "salary")

    // Definir una ventana particionada por departamento y ordenada por salario
    val windowSpec = Window.partitionBy("department").orderBy("salary")

    // Calcular el rango y el salario acumulado dentro de cada departamento
    val result = data.withColumn("rank", rank().over(windowSpec))
                     .withColumn("cumulative_salary", sum("salary").over(windowSpec))

    result.show()

    spark.stop()
  }
}
```

### 8.2 UDFs (User-Defined Functions)

Las UDFs permiten extender las capacidades de Spark SQL con funciones personalizadas.

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object UDFExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("UDFExample").master("local[*]").getOrCreate()
    import spark.implicits._

    // Definir una UDF para categorizar salarios
    val categorizeSalary = udf((salary: Int) => {
      if (salary < 3500) "Low"
      else if (salary < 4500) "Medium"
      else "High"
    })

    // Registrar la UDF para uso en SQL
    spark.udf.register("categorizeSalary", categorizeSalary)

    // Crear un DataFrame de ejemplo
    val data = Seq(
      ("Alice", 3000),
      ("Bob", 4000),
      ("Charlie", 5000)
    ).toDF("name", "salary")

    // Usar la UDF en una transformación de DataFrame
    val result = data.withColumn("salary_category", categorizeSalary($"salary"))

    result.show()

    // Usar la UDF en una consulta SQL
    data.createOrReplaceTempView("employees")
    spark.sql("SELECT name, salary, categorizeSalary(salary) as salary_category FROM employees").show()

    spark.stop()
  }
}
```

### 8.3 Manejo de datos complejos

Spark puede manejar estructuras de datos complejas como arrays y mapas.

En Spark, arrays son colecciones ordenadas de elementos del mismo tipo que pueden ser accedidos por su índice, mientras que mapas (o maps) son estructuras de datos que almacenan pares clave-valor, donde cada clave única se asocia con un valor específico.

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object ComplexDataExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("ComplexDataExample").master("local[*]").getOrCreate()
    import spark.implicits._

    // Crear un DataFrame con datos complejos
    val data = Seq(
      (1, Array("apple", "banana"), Map("a" -> 1, "b" -> 2)),
      (2, Array("orange", "grape"), Map("x" -> 3, "y" -> 4))
    ).toDF("id", "fruits", "scores")

    // Operaciones con arrays
    val withArrayLength = data.withColumn("fruit_count", size($"fruits"))

    // Operaciones con mapas
    val withMapValue = data.withColumn("score_a", $"scores".getItem("a"))

    // Explotar (explode) un array
    val explodedFruits = data.select($"id", explode($"fruits").as("fruit"))

    withArrayLength.show()
    withMapValue.show()
    explodedFruits.show()

    spark.stop()
  }
}
```

## 9. Ejemplo completo I

El código en Scala realiza un procesamiento completo de un conjunto de datos en formato Parquet, utilizando Apache Spark. El archivo Parquet contiene registros de viajes en taxi de la ciudad de Nueva York. El código realiza las siguientes operaciones:

- Descarga el archivo Parquet desde una URL específica.
- Lee el archivo descargado en un DataFrame de Spark.
- Realiza varias transformaciones y consultas en los datos, incluyendo la adición de nuevas columnas y el análisis de los datos por día de la semana.
- Utiliza tanto la API de DataFrame como SQL para realizar consultas y análisis.
- Guarda los resultados procesados en nuevos archivos Parquet.
- Libera los recursos y finaliza la sesión de Spark.

```scala
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel
import java.net.URL
import java.io.{File, FileOutputStream}

object CompleteExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("CompleteExample")
      .master("local[*]")
      .config("spark.sql.warehouse.dir", "spark-warehouse")
      .config("spark.executor.memory", "2g")
      .config("spark.driver.memory", "2g")
      .config("spark.sql.shuffle.partitions", "8")
      .getOrCreate()

    import spark.implicits._

    // URL del archivo Parquet a descargar (yellow taxi trip records)
    val url = "https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2022-01.parquet"
    
    // Descargar el archivo Parquet
    val localFilePath = "yellow_tripdata_2022-01.parquet"
    downloadFile(url, localFilePath)
    println(s"Archivo Parquet descargado en: $localFilePath")

    // Leer el archivo Parquet
    val dfTaxi = spark.read.parquet(localFilePath)
    
    // Mostrar el esquema y algunas estadísticas básicas
    dfTaxi.printSchema()
    dfTaxi.describe().show()

    // Cachear el DataFrame para mejorar el rendimiento de operaciones subsecuentes
    dfTaxi.persist(StorageLevel.MEMORY_AND_DISK)

    // Realizar algunas transformaciones y consultas
    val dfProcessed = dfTaxi
      .withColumn("trip_duration_minutes", 
        (unix_timestamp($"tpep_dropoff_datetime") - unix_timestamp($"tpep_pickup_datetime")) / 60)
      .withColumn("price_per_mile", when($"trip_distance" > 0, $"total_amount" / $"trip_distance").otherwise(0))
      .withColumn("day_of_week", date_format($"tpep_pickup_datetime", "EEEE"))

    // Crear una vista temporal para usar SQL
    dfProcessed.createOrReplaceTempView("taxi_trips")

    // Realizar una consulta SQL compleja
    val resultSQL = spark.sql("""
      SELECT 
        day_of_week,
        AVG(trip_duration_minutes) as avg_duration,
        AVG(trip_distance) as avg_distance,
        AVG(total_amount) as avg_amount,
        AVG(price_per_mile) as avg_price_per_mile
      FROM taxi_trips
      WHERE trip_distance > 0 AND trip_duration_minutes BETWEEN 5 AND 120
      GROUP BY day_of_week
      ORDER BY avg_amount DESC
    """)

    println("Resumen por dia de la semana:")
    resultSQL.show()

    // Análisis adicional usando la API de DataFrame
    val topPickupLocations = dfProcessed
      .groupBy("PULocationID")
      .agg(
        count("*").as("total_pickups"),
        avg("total_amount").as("avg_fare")
      )
      .orderBy(desc("total_pickups"))
      .limit(5)

    println("Top 5 ubicaciones de recogida:")
    topPickupLocations.show()

    // Guardar resultados en formato Parquet
    resultSQL.write.mode("overwrite").parquet("taxi_summary_by_day.parquet")
    topPickupLocations.write.mode("overwrite").parquet("top_pickup_locations.parquet")

    // Liberar el caché
    dfTaxi.unpersist()

    // Detener la sesión de Spark
    spark.stop()
  }

  def downloadFile(url: String, localFilePath: String): Unit = {
    val connection = new URL(url).openConnection()
    val inputStream = connection.getInputStream
    val outputStream = new FileOutputStream(new File(localFilePath))

    try {
      val buffer = new Array[Byte](4096)
      var bytesRead = inputStream.read(buffer)
      while (bytesRead != -1) {
        outputStream.write(buffer, 0, bytesRead)
        bytesRead = inputStream.read(buffer)
      }
    } finally {
      inputStream.close()
      outputStream.close()
    }
  }
}
```

## 10. Ejemplo completo II

Este código en Scala, utilizando Apache Spark, realiza un procesamiento avanzado de un conjunto de datos de rutas aéreas descargado desde internet en formato CSV. El flujo de trabajo incluye la descarga del archivo, la lectura de los datos en un DataFrame de Spark, la aplicación de transformaciones complejas, la ejecución de consultas SQL, el uso de funciones de ventana para análisis avanzado, y la persistencia de los resultados en formato Parquet. Este ejemplo muestra cómo manejar grandes volúmenes de datos y realizar análisis detallados sobre rutas aéreas, aerolíneas y conectividad de aeropuertos, optimizando tanto el rendimiento como la escalabilidad del procesamiento.

```scala
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.storage.StorageLevel
import java.net.URL
import java.io.{File, FileOutputStream}

object CompleteExample2 {
  def main(args: Array[String]): Unit = {
    // Inicializar la sesión de Spark con configuraciones optimizadas
    val spark = SparkSession.builder()
      .appName("CompleteExample2")
      .master("local[*]")
      .config("spark.sql.warehouse.dir", "spark-warehouse")
      .config("spark.executor.memory", "2g")
      .config("spark.driver.memory", "2g")
      .config("spark.sql.shuffle.partitions", "8")
      .getOrCreate()

    import spark.implicits._

    // URL del conjunto de datos de vuelos (sustituye con una URL real si esta no funciona)
    val url = "https://raw.githubusercontent.com/jpatokal/openflights/master/data/routes.dat"
    
    // Descargar el archivo CSV
    val localFilePath = "routes.csv"
    downloadFile(url, localFilePath)
    println(s"Archivo CSV descargado en: $localFilePath")

    // Leer el archivo CSV
    val dfRoutes = spark.read
      .option("header", "false") // El archivo no tiene encabezado
      .option("inferSchema", "true")
      .csv(localFilePath)
      .toDF("airline", "airline_id", "source_airport", "source_airport_id", 
            "destination_airport", "destination_airport_id", "codeshare", 
            "stops", "equipment")

    // Mostrar el esquema y algunas filas de muestra
    dfRoutes.printSchema()
    dfRoutes.show(5)

    // Persistir el DataFrame en memoria y disco para mejorar el rendimiento
    dfRoutes.persist(StorageLevel.MEMORY_AND_DISK)

    // Definir una UDF para categorizar las rutas basadas en el número de paradas
    val categorizeRoute = udf((stops: Int) => {
      if (stops == 0) "Direct"
      else if (stops == 1) "One Stop"
      else "Multiple Stops"
    })

    // Aplicar la UDF y realizar algunas transformaciones
    val dfProcessed = dfRoutes
      .withColumn("route_type", categorizeRoute($"stops"))
      .withColumn("is_international", 
                  when($"source_airport".substr(1, 2) =!= $"destination_airport".substr(1, 2), true)
                  .otherwise(false))

    // Crear una vista temporal para usar SQL
    dfProcessed.createOrReplaceTempView("flight_routes")

    // Realizar un análisis complejo usando SQL
    val routeAnalysis = spark.sql("""
      SELECT 
        airline,
        route_type,
        COUNT(*) as route_count,
        SUM(CASE WHEN is_international THEN 1 ELSE 0 END) as international_routes
      FROM flight_routes
      GROUP BY airline, route_type
      ORDER BY route_count DESC
    """)

    println("Análisis de rutas por aerolínea:")
    routeAnalysis.show()

    // Utilizar funciones de ventana para análisis más avanzado
    val windowSpec = Window.partitionBy("airline").orderBy($"route_count".desc)

    val topRoutesByAirline = routeAnalysis
      .withColumn("rank", dense_rank().over(windowSpec))
      .filter($"rank" <= 3) // Obtener las top 3 tipos de rutas para cada aerolínea
      .orderBy($"airline", $"rank")

    println("Top 3 tipos de rutas por aerolínea:")
    topRoutesByAirline.show()

    // Realizar un análisis de conectividad de aeropuertos
    val airportConnectivity = dfProcessed
      .groupBy("source_airport")
      .agg(
        countDistinct("destination_airport").as("destinations"),
        sum(when($"is_international", 1).otherwise(0)).as("international_connections")
      )
      .orderBy($"destinations".desc)

    println("Análisis de conectividad de aeropuertos:")
    airportConnectivity.show()

    // Guardar los resultados en formato Parquet
    routeAnalysis.write.mode("overwrite").parquet("route_analysis.parquet")
    topRoutesByAirline.write.mode("overwrite").parquet("top_routes_by_airline.parquet")
    airportConnectivity.write.mode("overwrite").parquet("airport_connectivity.parquet")

    // Liberar el caché y detener la sesión de Spark
    dfRoutes.unpersist()
    spark.stop()
  }

  // Función auxiliar para descargar el archivo
  def downloadFile(url: String, localFilePath: String): Unit = {
    val connection = new URL(url).openConnection()
    val inputStream = connection.getInputStream
    val outputStream = new FileOutputStream(new File(localFilePath))

    try {
      val buffer = new Array[Byte](4096)
      var bytesRead = inputStream.read(buffer)
      while (bytesRead != -1) {
        outputStream.write(buffer, 0, bytesRead)
        bytesRead = inputStream.read(buffer)
      }
    } finally {
      inputStream.close()
      outputStream.close()
    }
  }
}
```

## 11. Ejercicios prácticos

### Ejercicio 1

- Objetivo: Analizar tendencias de ventas por categoría de producto utilizando funciones de ventana en Spark SQL.
- Planteamiento: Una empresa de comercio electrónico quiere analizar sus datos de ventas para entender mejor las tendencias por categoría de producto. Necesitan calcular las ventas acumuladas y el promedio móvil de ventas para cada categoría de producto a lo largo del tiempo.

- Crea un DataFrame con las siguientes columnas: fecha, categoria, ventas.
- Utiliza funciones de ventana para calcular:
  - Ventas acumuladas por categoría.
  - Promedio móvil de ventas de 3 días por categoría.
  - Muestra los resultados ordenados por categoría y fecha.
- Ayuda:
  - Utiliza SparkSession para crear el DataFrame.
  - La función Window.partitionBy() te ayudará a definir la ventana por categoría.
  - Las funciones sum() y avg() pueden usarse con over() para cálculos de ventana.
  - Para el promedio móvil, considera usar windowSpec.rowsBetween().

### Ejercicio 2

- Objetivo: Procesar y analizar logs de servidor utilizando User-Defined Functions (UDFs) y operaciones de texto en Spark.
Planteamiento: Un equipo de operaciones de TI necesita analizar los logs de sus servidores para identificar patrones y problemas. Los logs contienen información como timestamp, nivel de log (INFO, ERROR, WARN), y mensaje.
- Crea un DataFrame simulando logs de servidor.
- Implementa una UDF para extraer el nivel de log de cada entrada.
- Utiliza funciones de procesamiento de texto para extraer el timestamp y el mensaje.
- Agrupa los logs por nivel y calcula la frecuencia de cada tipo.
- Ayuda:
  - Usa spark.udf.register() para crear una UDF que extraiga el nivel de log.
  - Las funciones substring() y regexp_extract() pueden ser útiles para procesar el texto de los logs.
  - Considera usar withColumn() para añadir nuevas columnas con la información extraída.
  - groupBy() y count() te ayudarán a calcular las frecuencias de los niveles de log.

### Ejercicio 3

- Objetivo: Implementar un sistema de procesamiento de datos de sensores en tiempo real utilizando Spark Structured Streaming.
- Planteamiento: Una fábrica inteligente ha instalado sensores de temperatura en diferentes áreas de la planta. Necesitan un sistema que pueda procesar estos datos en tiempo real y proporcionar estadísticas actualizadas constantemente.
- Configura un StreamingQuery que simule la entrada de datos de sensores.
- Procesa el stream para calcular:
  - Temperatura promedio por sensor en los últimos 5 minutos.
  - Temperatura máxima por sensor desde el inicio del stream.
- Muestra los resultados actualizados cada minuto.
- Ayuda:
  - Utiliza spark.readStream.format("rate") para simular un stream de entrada.
  - La función window() te ayudará a crear ventanas de tiempo para los cálculos.
  - Usa groupBy() con las funciones de agregación avg() y max() para los cálculos requeridos.
  - Configura el outputMode y el trigger en writeStream para controlar cómo y cuándo se actualizan los resultados.

### Ejercicio 4

- Objetivo: Realizar operaciones de ETL (Extract, Transform, Load) en datos almacenados en formato Parquet y optimizar consultas.
- Planteamiento: Un equipo de análisis de datos necesita preparar un gran conjunto de datos de ventas para su análisis. Los datos están almacenados en formato Parquet y necesitan ser procesados, transformados y optimizados para consultas eficientes.
- Lee un conjunto de datos de ventas desde archivos Parquet.
- Realiza transformaciones como:
  - Convertir fechas a un formato estándar.
  - Calcular el total de ventas por transacción.
- Particiona los datos por fecha para optimizar las consultas.
- Escribe los datos transformados de vuelta en formato Parquet.
- Realiza y optimiza una consulta para obtener las ventas totales por mes.
- Ayuda:
  - Usa spark.read.parquet() para leer los datos Parquet.
  - Las funciones to_date() y date_format() pueden ser útiles para el manejo de fechas.
  - Considera usar partitionBy() al escribir los datos para optimizar futuras consultas.
  - Utiliza explain() para ver el plan de ejecución de tu consulta y buscar oportunidades de optimización.
- Dataset: "<https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2023-01.parquet>"

### Ejercicio 5

- Objetivo: Analizar la evolución de casos de COVID-19 por país utilizando un conjunto de datos real descargado de internet.
- Planteamiento: Una organización de salud global necesita analizar la propagación del COVID-19 en diferentes países para informar sus políticas y recomendaciones. Utilizarán el conjunto de datos de casos confirmados de COVID-19 proporcionado por el Centro de Ciencia e Ingeniería de Sistemas de la Universidad Johns Hopkins. El dataset contiene información diaria sobre los casos confirmados de COVID-19 para diferentes países y regiones.
- Se requiere:
  - Descargar y procesar el conjunto de datos más reciente.
  - Transformar los datos de formato ancho a largo para facilitar el análisis temporal.
  - Calcular los nuevos casos diarios por país.
  - Identificar los 10 países con más casos acumulados.
  - Calcular la media móvil de 7 días de nuevos casos para los top 10 países.
  - Almacenar los resultados en un formato eficiente para futuras consultas.
- Ayuda:
  - Utiliza las funciones de Spark SQL para realizar transformaciones y agregaciones.
  - Considera usar Window functions para cálculos como la media móvil.
  - Aprovecha las capacidades de particionamiento de Spark al escribir los resultados.
- Dataset: "<https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv>

## Anotaciones

### Descarga de ficheros en una carpeta concreta

Antes de descargar o guardar cualquier archivo, es una buena práctica asegurarse de que existe una carpeta dedicada para los datos. En este caso, utilizaremos una carpeta llamada 'data'.

Implementación en scala:

```scala
import java.nio.file.{Files, Paths}

def createDirectoryIfNotExists(dirPath: String): Unit = {
  val path = Paths.get(dirPath)
  if (!Files.exists(path)) {
    try {
      Files.createDirectories(path)
      println(s"Directorio creado: $dirPath")
    } catch {
      case e: Exception => println(s"Error al crear el directorio $dirPath: ${e.getMessage}")
    }
  }
}

val dataDir = "data"
createDirectoryIfNotExists(dataDir)
```

Una vez que tenemos la carpeta 'data', podemos implementar una función para descargar archivos y guardarlos en esta carpeta.

```scala
import java.net.URL
import java.io.{File, FileOutputStream}

def downloadFile(url: String, localFilePath: String): Unit = {
  val connection = new URL(url).openConnection()
  val inputStream = connection.getInputStream
  val outputStream = new FileOutputStream(new File(localFilePath))

  try {
    val buffer = new Array[Byte](4096)
    var bytesRead = inputStream.read(buffer)
    while (bytesRead != -1) {
      outputStream.write(buffer, 0, bytesRead)
      bytesRead = inputStream.read(buffer)
    }
    println(s"Archivo descargado exitosamente: $localFilePath")
  } catch {
    case e: Exception => println(s"Error al descargar el archivo: ${e.getMessage}")
  } finally {
    inputStream.close()
    outputStream.close()
  }
}

val url = "https://example.com/data.csv"
val localFilePath = s"$dataDir/data.csv"
downloadFile(url, localFilePath)
```

## Ejercicio final

### Objetivo

Realizar un análisis completo de datos utilizando Apache Spark y HDFS.

### Instrucciones

1. **Datos**
   - Busca y descarga un conjunto de datos de interés (>1GB) de una fuente en línea.
   - Documenta la fuente y describe brevemente el contenido.

2. **HDFS**
   - Carga los datos en HDFS.

3. **Spark**
   - Procesa los datos con Spark:
     - Limpieza y transformación.
     - Análisis exploratorio con Spark SQL y DataFrames.
     - Aplica al menos dos técnicas avanzadas (ej. Window Functions, UDFs).

4. **Análisis**
   - Extrae al menos tres informaciones significativos.

5. **Visualización**
   - Crea al menos una visualización significativa de tus resultados.
   - Exporta los resultados de tu análisis a un archivo CSV utilizando Spark (df.write.csv("ruta/al/archivo.csv")) y luego impórtalos en una herramienta externa como Excel, Google Sheets o Google Looker Studio para crear la visualización.
   - Incluye una captura de pantalla o imagen de tu visualización en el informe.
