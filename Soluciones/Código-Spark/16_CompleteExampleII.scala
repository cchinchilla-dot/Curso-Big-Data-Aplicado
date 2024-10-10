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