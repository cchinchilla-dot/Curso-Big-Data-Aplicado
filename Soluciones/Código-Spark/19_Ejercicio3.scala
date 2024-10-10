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
