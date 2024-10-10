import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object UDFExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("UDFExample").master("local[*]").getOrCreate()
    import spark.implicits._

    // Definir una UDF para categorizar salarios
    val categorizeSalary = udf((salary: Int) => {
      if (salary < 3500) "Low"
      else if (salary < 4500) "Medium"
      else "High"
    })

    // Registrar la UDF para uso en SQL
    spark.udf.register("categorizeSalary", categorizeSalary)

    // Crear un DataFrame de ejemplo
    val data = Seq(
      ("Alice", 3000),
      ("Bob", 4000),
      ("Charlie", 5000)
    ).toDF("name", "salary")

    // Usar la UDF en una transformación de DataFrame
    val result = data.withColumn("salary_category", categorizeSalary($"salary"))

    result.show()

    // Usar la UDF en una consulta SQL
    data.createOrReplaceTempView("employees")
    spark.sql("SELECT name, salary, categorizeSalary(salary) as salary_category FROM employees").show()

    spark.stop()
  }
}
