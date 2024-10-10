import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.sql.expressions.Window

object OptimizerExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("OptimizerExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._
    import org.apache.spark.sql.functions._  // Importar las funciones necesarias de Spark SQL

    // Crear un DataFrame simulado para `df1`
    val df1 = Seq(
      (1, "2023-02-01", "Electronica", 1000.0),
      (2, "2023-03-01", "Hogar", 500.0),
      (3, "2022-12-01", "Electronica", 700.0),
      (4, "2023-05-01", "Ropa", 1500.0)
    ).toDF("id", "fecha", "categoria", "ventas")

    // Crear un DataFrame simulado para `df2`
    val df2 = Seq(
      (1, "Descuento"),
      (2, "Promoción"),
      (4, "Descuento"),
      (5, "Ofertas")
    ).toDF("id", "tipo_promocion")

    // Realizar el join entre `df1` y `df2` usando broadcast para `df2`
    val resultado = df1.join(broadcast(df2), Seq("id"))  // Hacer un join en la columna "id" y usar `broadcast` para optimizar si `df2` es pequeño
      .filter($"fecha" > "2023-01-01")  // Filtrar las filas donde la fecha sea posterior al 1 de enero de 2023
      .groupBy($"categoria")  // Agrupar por la columna "categoria"
      .agg(sum($"ventas").as("total_ventas"))  // Sumar las ventas dentro de cada grupo y almacenar en "total_ventas"
      .orderBy($"total_ventas".desc)  // Ordenar los resultados por "total_ventas" en orden descendente

    // Analizar el plan de ejecución
    resultado.explain(true)  // Mostrar el plan de ejecución detallado para entender cómo Spark procesará la consulta

    // Materializar el resultado (en un entorno real, escribiría a un archivo)
    resultado.show()  // En lugar de escribir a un archivo, mostramos el resultado en pantalla

    // Detener la sesión de Spark
    spark.stop()
  }
}
