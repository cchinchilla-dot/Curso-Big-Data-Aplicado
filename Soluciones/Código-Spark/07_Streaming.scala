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

    // Query para mostrar los datos procesados (agregados)
    val queryProcesado = procesadoDF.writeStream
      .outputMode("update")  // Modo de salida: solo mostrar los resultados nuevos o actualizados
      .format("console")  // Salida en la consola para visualización
      .trigger(Trigger.ProcessingTime("10 seconds"))  // Configurar el trigger para procesar los datos cada 10 segundos
      .start()

    // Query para mostrar todos los datos recibidos
    val queryTodosLosDatos = simulatedEventsDF.writeStream
      .outputMode("append")  // Modo de salida: añadir nuevas filas
      .format("console")  // Salida en la consola para visualización
      .trigger(Trigger.ProcessingTime("10 seconds"))  // Configurar el trigger para procesar los datos cada 10 segundos
      .start()

    // Mantener la aplicación en ejecución hasta que se detenga manualmente
    spark.streams.awaitAnyTermination()

    // Detener la sesión de Spark
    spark.stop()
  }
}