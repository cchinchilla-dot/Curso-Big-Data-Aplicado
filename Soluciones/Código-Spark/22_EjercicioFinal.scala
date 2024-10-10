import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.hadoop.fs.{FileSystem, Path}
import java.net.URL
import java.io.File
import java.nio.file.{Files, Paths}

object AnalisisTaxiNYC {
  def main(args: Array[String]): Unit = {
    // Inicializar la sesión de Spark
    val spark = SparkSession.builder()
      .appName("AnalisisTaxiNYC")
      .master("local[*]")
      .config("spark.sql.warehouse.dir", "spark-warehouse")
      .getOrCreate()

    import spark.implicits._

    try {
      // Configurar HDFS
      val hdfsUri = "hdfs://localhost:9000"
      val conf = spark.sparkContext.hadoopConfiguration
      conf.set("fs.defaultFS", hdfsUri)
      val fs = FileSystem.get(conf)

      // 1. Descargar el dataset
      val url = "https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2023-01.parquet"
      val localPath = "data/yellow_tripdata_2023-01.parquet"
      createDirectoryIfNotExists(new File(localPath).getParent)
      println(s"Iniciando descarga del archivo desde $url")
      downloadFile(url, localPath)
      println(s"Descarga completada: $localPath")

      // 2. Cargar el archivo en HDFS
      val hdfsPath = s"$hdfsUri/user/spark/yellow_tripdata_2023-01.parquet"
      fs.copyFromLocalFile(new Path(localPath), new Path(hdfsPath))
      println(s"Archivo cargado en HDFS: $hdfsPath")

      // 3. Leer el archivo Parquet
      val df = spark.read.parquet(hdfsPath)

      // 4. Procesar los datos
      val processedDf = cleanAndTransformData(df)
      processedDf.cache()

      // 5. Análisis exploratorio
      println("Estadísticas básicas:")
      val basicStats = calculateBasicStats(processedDf)
      basicStats.show()

      println("Top 10 zonas de recogida más frecuentes:")
      val topPickupLocations = findTopPickupLocations(processedDf)
      topPickupLocations.show()

      // 6. Análisis avanzado
      println("Ingresos promedio por hora del día:")
      val hourlyRevenue = calculateHourlyRevenue(processedDf)
      hourlyRevenue.show(24)

      println("Duración promedio de viaje por día de la semana:")
      val tripDurationByDay = calculateTripDurationByDay(processedDf)
      tripDurationByDay.show()

      // 7. Optimización: particionamiento por fecha
      val partitionedDf = processedDf.repartition(col("pickup_date"))
      partitionedDf.write.mode("overwrite").partitionBy("pickup_date").parquet(s"$hdfsUri/user/spark/taxi_trips_partitioned")

      // 8. Exportar datos para visualización
      val dailyStats = calculateDailyStats(processedDf)
      dailyStats.coalesce(1)
        .write.mode("overwrite")
        .option("header", "true")
        .csv(s"$hdfsUri/user/spark/daily_taxi_stats")

      println("Análisis completado. Los resultados han sido guardados en HDFS.")
    } catch {
      case e: Exception => 
        println(s"Error durante el análisis: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      // Detener la sesión de Spark
      spark.stop()
    }
  }

  def createDirectoryIfNotExists(dirPath: String): Unit = {
    val path = Paths.get(dirPath)
    if (!Files.exists(path)) {
      Files.createDirectories(path)
      println(s"Directorio creado: $dirPath")
    }
  }

  def downloadFile(url: String, localPath: String): Unit = {
    val connection = new URL(url).openConnection()
    val inputStream = connection.getInputStream
    val outputStream = Files.newOutputStream(Paths.get(localPath))
    try {
      outputStream.write(inputStream.readAllBytes())
    } finally {
      inputStream.close()
      outputStream.close()
    }
  }

  def cleanAndTransformData(df: DataFrame): DataFrame = {
    df.na.drop()
      .withColumn("pickup_date", to_date(col("tpep_pickup_datetime")))
      .withColumn("pickup_hour", hour(col("tpep_pickup_datetime")))
      .withColumn("trip_duration", (unix_timestamp(col("tpep_dropoff_datetime")) - unix_timestamp(col("tpep_pickup_datetime"))) / 60)
      .filter(col("trip_duration") > 0 && col("trip_duration") < 1440) // Filtrar viajes de más de 24 horas o negativos
  }

  def calculateBasicStats(df: DataFrame): DataFrame = {
    df.agg(
      count("*").as("TotalTrips"),
      sum("total_amount").as("TotalRevenue"),
      avg("trip_duration").as("AverageTripDuration"),
      avg("total_amount").as("AverageFare")
    )
  }

  def findTopPickupLocations(df: DataFrame): DataFrame = {
    df.groupBy("PULocationID")
      .agg(count("*").as("TripCount"))
      .orderBy(desc("TripCount"))
      .limit(10)
  }

  def calculateHourlyRevenue(df: DataFrame): DataFrame = {
    df.groupBy("pickup_hour")
      .agg(avg("total_amount").as("AverageRevenue"))
      .orderBy("pickup_hour")
  }

  def calculateTripDurationByDay(df: DataFrame): DataFrame = {
    df.withColumn("day_of_week", date_format(col("pickup_date"), "EEEE"))
      .groupBy("day_of_week")
      .agg(avg("trip_duration").as("AverageTripDuration"))
      .orderBy(
        when(col("day_of_week") === "Monday", 1)
          .when(col("day_of_week") === "Tuesday", 2)
          .when(col("day_of_week") === "Wednesday", 3)
          .when(col("day_of_week") === "Thursday", 4)
          .when(col("day_of_week") === "Friday", 5)
          .when(col("day_of_week") === "Saturday", 6)
          .when(col("day_of_week") === "Sunday", 7)
      )
  }

  def calculateDailyStats(df: DataFrame): DataFrame = {
    df.groupBy("pickup_date")
      .agg(
        count("*").as("TotalTrips"),
        sum("total_amount").as("TotalRevenue"),
        avg("trip_duration").as("AverageTripDuration")
      )
      .orderBy("pickup_date")
  }
}