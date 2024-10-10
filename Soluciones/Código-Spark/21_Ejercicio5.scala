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