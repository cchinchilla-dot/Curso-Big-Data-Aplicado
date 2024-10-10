# Guía avanzada de Apache Spark

## 1. Ejercicios prácticos

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

## 2. Soluciones

### Ejercicio 1

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

object Ejercicio1 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Ejercicio1")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Crear DataFrame de ventas
    val ventas = Seq(
      ("2023-01-01", "Electrónica", 1000),
      ("2023-01-02", "Ropa", 500),
      ("2023-01-03", "Electrónica", 1500),
      ("2023-01-04", "Hogar", 800),
      ("2023-01-05", "Ropa", 600),
      ("2023-01-06", "Electrónica", 2000)
    ).toDF("fecha", "categoria", "ventas")

    // Definir ventana por categoría ordenada por fecha
    val windowSpec = Window.partitionBy("categoria").orderBy("fecha")

    // Calcular ventas acumuladas y promedio móvil
    val resultados = ventas
      .withColumn("fecha", to_date($"fecha"))
      .withColumn("ventas_acumuladas", sum("ventas").over(windowSpec))
      .withColumn("promedio_movil", avg("ventas").over(windowSpec.rowsBetween(-1, 1)))

    resultados.orderBy("categoria", "fecha").show()

    spark.stop()
  }
}
```

### Ejercicio 2

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Ejercicio2 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Ejercicio2")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Simular logs
    val logs = Seq(
      "2023-05-01 10:00:15 INFO [User:123] Login successful",
      "2023-05-01 10:05:20 ERROR [User:456] Failed login attempt",
      "2023-05-01 10:10:30 WARN [System] High CPU usage detected"
    ).toDF("log")

    // UDF para extraer nivel de log
    val extractLevel = udf((log: String) => {
      val levels = Seq("INFO", "ERROR", "WARN")
      levels.find(log.contains).getOrElse("UNKNOWN")
    })

    // Procesar logs
    val processedLogs = logs
      .withColumn("timestamp", to_timestamp(substring($"log", 1, 19), "yyyy-MM-dd HH:mm:ss"))
      .withColumn("level", extractLevel($"log"))
      .withColumn("message", regexp_extract($"log", "\\] (.+)$", 1))

    processedLogs.show(false)

    // Calcular frecuencia de niveles de log
    val logFrequency = processedLogs.groupBy("level").count()
    logFrequency.show()

    spark.stop()
  }
}
```

### Ejercicio 3

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

object Ejercicio3 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Ejercicio3")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Simular stream de datos de sensores
    val sensorData = spark.readStream
      .format("rate")
      .option("rowsPerSecond", 5)
      .load()
      .withColumn("sensorId", (rand() * 3).cast("int"))
      .withColumn("temperature", (rand() * 100).cast("double"))

    // Procesar stream
    val processedData = sensorData
      .withWatermark("timestamp", "5 minutes")
      .groupBy($"sensorId", window($"timestamp", "5 minutes"))
      .agg(
        avg("temperature").as("avg_temp"),
        max("temperature").as("max_temp")
      )

    // Escribir resultados en consola
    val query = processedData.writeStream
      .outputMode("complete")
      .format("console")
      .trigger(Trigger.ProcessingTime("1 minute"))
      .start()

    query.awaitTermination(300000) // Ejecutar por 5 minutos
    query.stop()

    spark.stop()
  }
}
```

### Ejercicio 4

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.{Files, Paths, StandardCopyOption}

object Ejercicio4 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Ejercicio4")
      .master("local[*]")
      .config("spark.sql.parquet.compression.codec", "snappy")
      .getOrCreate()

    import spark.implicits._

    // URL del archivo Parquet
    val url = "https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2023-01.parquet"
    
    // Descargar el archivo
    val file = Paths.get("yellow_tripdata_2023-01.parquet")
    if (!Files.exists(file)) {
      println("Descargando archivo...")
      val connection = new URL(url).openConnection()
      val in = connection.getInputStream
      Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING)
      in.close()
      println("Archivo descargado.")
    }

    // Leer el archivo Parquet
    val trips = spark.read.parquet(file.toString)

    // Realizar transformaciones
    val processedTrips = trips
      .withColumn("trip_date", to_date($"tpep_pickup_datetime"))
      .withColumn("total_amount", $"total_amount".cast("double"))
      .withColumn("trip_duration", 
        (unix_timestamp($"tpep_dropoff_datetime") - unix_timestamp($"tpep_pickup_datetime")) / 60)

    // Particionar los datos por fecha para optimizar las consultas
    processedTrips.write
      .partitionBy("trip_date")
      .mode("overwrite")
      .parquet("processed_trips")

    // Realizar y optimizar una consulta para obtener las ventas totales por mes
    val monthlyStats = spark.read.parquet("processed_trips")
      .groupBy(month($"trip_date").as("month"))
      .agg(
        sum("total_amount").as("total_sales"),
        avg("trip_duration").as("avg_duration"),
        count("*").as("trip_count")
      )
      .orderBy("month")

    // Mostrar el plan de ejecución
    monthlyStats.explain()

    // Mostrar resultados
    monthlyStats.show()

    spark.stop()
  }
}
```

### Ejercicio 5

```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import java.net.URL
import java.nio.file.{Files, Paths, StandardCopyOption}

object Ejercicio5 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Ejercicio5")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // URL del dataset
    val url = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv"
    
    // Descargar el archivo
    val file = Paths.get("covid_data.csv")
    if (!Files.exists(file)) {
      println("Descargando archivo...")
      val connection = new URL(url).openConnection()
      val in = connection.getInputStream
      Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING)
      in.close()
      println("Archivo descargado.")
    }

    // Leer el CSV
    val covidData = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(file.toString)

    // Transformar los datos de formato ancho a largo
    val dateCols = covidData.columns.drop(4)
    val longFormatData = covidData
      .select(
        col("Country/Region"),
        explode(array(dateCols.map(c => struct(lit(c).as("date"), col(c).as("confirmed_cases"))): _*)).as("tmp")
      )
      .select(
        col("Country/Region").as("country"),
        to_date(col("tmp.date"), "M/d/yy").as("date"),
        col("tmp.confirmed_cases").cast("int").as("confirmed_cases")
      )

    // Calcular nuevos casos diarios
    val dailyCases = longFormatData
      .withColumn("previous_day_cases", 
                  lag("confirmed_cases", 1).over(Window.partitionBy("country").orderBy("date")))
      .withColumn("new_cases", 
                  when(col("confirmed_cases") - col("previous_day_cases") < 0, 0)
                  .otherwise(col("confirmed_cases") - col("previous_day_cases")))

    // Analizar los 10 países con más casos acumulados
    val topCountries = dailyCases
      .groupBy("country")
      .agg(max("confirmed_cases").as("total_cases"))
      .orderBy(col("total_cases").desc)
      .limit(10)

    println("Top 10 países con más casos acumulados:")
    topCountries.show()

    // Calcular la media móvil de 7 días de nuevos casos para los top 10 países
    val movingAverage = dailyCases
      .join(topCountries, Seq("country"))
      .withColumn("7_day_average", 
                  avg("new_cases").over(Window.partitionBy("country").orderBy("date").rowsBetween(-6, 0)))
      .select("country", "date", "new_cases", "7_day_average")
      .orderBy("country", "date")

    println("Media móvil de 7 días de nuevos casos para los top 10 países:")
    movingAverage.show()

    // Guardar resultados en formato Parquet
    movingAverage.write.mode("overwrite").partitionBy("country").parquet("covid_analysis_results")

    spark.stop()
  }
}
```
