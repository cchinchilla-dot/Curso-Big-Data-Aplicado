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
