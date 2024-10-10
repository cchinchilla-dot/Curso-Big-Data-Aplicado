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