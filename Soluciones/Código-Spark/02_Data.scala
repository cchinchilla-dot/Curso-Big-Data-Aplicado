import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.sql.expressions.Window
import java.sql.Date

// Definir un objeto principal
object DataExample {
  // Definir un caso de clase para el Dataset
  case class Venta(fecha: java.sql.Date, producto: String, cantidad: Int, precio: Double)

  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("DataExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Crear una secuencia de datos simulando las ventas
    val ventas = Seq(
      Venta(Date.valueOf("2024-09-01"), "ProductoA", 10, 20.5),
      Venta(Date.valueOf("2024-09-01"), "ProductoB", 5, 10.0),
      Venta(Date.valueOf("2024-09-02"), "ProductoA", 7, 20.5),
      Venta(Date.valueOf("2024-09-02"), "ProductoB", 8, 10.0),
      Venta(Date.valueOf("2024-09-03"), "ProductoA", 15, 20.5),
      Venta(Date.valueOf("2024-09-03"), "ProductoB", 6, 10.0),
      Venta(Date.valueOf("2024-09-04"), "ProductoA", 14, 20.5),
      Venta(Date.valueOf("2024-09-04"), "ProductoB", 10, 10.0),
      Venta(Date.valueOf("2024-09-05"), "ProductoA", 12, 20.5),
      Venta(Date.valueOf("2024-09-05"), "ProductoB", 9, 10.0),
      Venta(Date.valueOf("2024-09-06"), "ProductoA", 11, 20.5),
      Venta(Date.valueOf("2024-09-06"), "ProductoB", 7, 10.0),
      Venta(Date.valueOf("2024-09-07"), "ProductoA", 9, 20.5),
      Venta(Date.valueOf("2024-09-07"), "ProductoB", 5, 10.0)
    )

    // Crear un Dataset a partir de la secuencia de datos
    val ventasDS = ventas.toDS()

    // Realizar operaciones complejas
    val resultadoDF = ventasDS
      .groupBy($"fecha", $"producto")
      .agg(F.sum($"cantidad").as("total_cantidad"), F.sum($"precio" * $"cantidad").as("total_ventas"))
      .withColumn("promedio_7_dias", 
        F.avg($"total_ventas").over(Window.partitionBy($"producto")
          .orderBy($"fecha")
          .rowsBetween(-6, 0))) // Cambiado a -6 para incluir 7 días en total
      .filter($"total_ventas" > $"promedio_7_dias")

    // Mostrar el resultado
    resultadoDF.show()

    // Detener la sesión de Spark
    spark.stop()
  }
}
