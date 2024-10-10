import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.storage.StorageLevel

object CachingExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("CachingExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._
    import org.apache.spark.sql.functions._  // Importar las funciones necesarias de Spark SQL

    // Crear un DataFrame simulado para `df`
    val df = Seq(
      (1, "Electronica", 1000.0),
      (2, "Hogar", 500.0),
      (3, "Electronica", 700.0),
      (4, "Ropa", 1500.0)
    ).toDF("id", "categoria", "ventas")

    // Cachear el DataFrame en memoria y disco
    df.persist(StorageLevel.MEMORY_AND_DISK_SER)  // Persistir el DataFrame usando almacenamiento en memoria y disco serializado

    // Realizar múltiples operaciones sobre el DataFrame cacheado
    val resultado1 = df.groupBy("categoria").agg(sum("ventas").as("total_ventas"))  // Agrupar por categoría y sumar ventas
    val resultado2 = df.join(df, Seq("id"))  // Realizar un join consigo mismo basado en la columna "id"

    // Mostrar los resultados de las operaciones
    resultado1.show()
    resultado2.show()

    // Liberar el caché cuando ya no sea necesario
    df.unpersist()  // Liberar la memoria y espacio en disco ocupados por el caché

    // Detener la sesión de Spark
    spark.stop()
  }
}
