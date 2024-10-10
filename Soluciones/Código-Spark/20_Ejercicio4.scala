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