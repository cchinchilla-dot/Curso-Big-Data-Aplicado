import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import scala.io.Source
import java.io.PrintWriter
import java.io.File

object CSVDatasetExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("CSVDatasetExample")
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
