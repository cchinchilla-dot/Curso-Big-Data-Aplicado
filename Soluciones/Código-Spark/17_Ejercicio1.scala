import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

object Ejercicio1 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Ejercicio1")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Crear DataFrame de ventas
    val ventas = Seq(
      ("2023-01-01", "Electronica", 1000),
      ("2023-01-02", "Ropa", 500),
      ("2023-01-03", "Electronica", 1500),
      ("2023-01-04", "Hogar", 800),
      ("2023-01-05", "Ropa", 600),
      ("2023-01-06", "Electronica", 2000)
    ).toDF("fecha", "categoria", "ventas")

    // Definir ventana por categoría ordenada por fecha
    val windowSpec = Window.partitionBy("categoria").orderBy("fecha")

    // Calcular ventas acumuladas y promedio móvil
    val resultados = ventas
      .withColumn("fecha", to_date($"fecha"))
      .withColumn("ventas_acumuladas", sum("ventas").over(windowSpec))
      .withColumn("promedio_movil", avg("ventas").over(windowSpec.rowsBetween(-1, 1)))

    resultados.orderBy("categoria", "fecha").show()

    spark.stop()
  }
}
