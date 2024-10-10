import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

object WindowFunctionExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("WindowFunctionExample").master("local[*]").getOrCreate()
    import spark.implicits._

    // Crear un DataFrame de ejemplo
    val data = Seq(
      ("Alice", "Sales", 3000),
      ("Bob", "Sales", 4000),
      ("Charlie", "Marketing", 4500),
      ("David", "Sales", 3500),
      ("Eve", "Marketing", 3800)
    ).toDF("name", "department", "salary")

    // Definir una ventana particionada por departamento y ordenada por salario
    val windowSpec = Window.partitionBy("department").orderBy("salary")

    // Calcular el rango y el salario acumulado dentro de cada departamento
    val result = data.withColumn("rank", rank().over(windowSpec))
                     .withColumn("cumulative_salary", sum("salary").over(windowSpec))

    result.show()

    spark.stop()
  }
}