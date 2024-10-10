import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object ComplexDataExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("ComplexDataExample").master("local[*]").getOrCreate()
    import spark.implicits._

    // Crear un DataFrame con datos complejos
    val data = Seq(
      (1, Array("apple", "banana"), Map("a" -> 1, "b" -> 2)),
      (2, Array("orange", "grape"), Map("x" -> 3, "y" -> 4))
    ).toDF("id", "fruits", "scores")

    // Operaciones con arrays
    val withArrayLength = data.withColumn("fruit_count", size($"fruits"))

    // Operaciones con mapas
    val withMapValue = data.withColumn("score_a", $"scores".getItem("a"))

    // Explotar (explode) un array
    val explodedFruits = data.select($"id", explode($"fruits").as("fruit"))

    withArrayLength.show()
    withMapValue.show()
    explodedFruits.show()

    spark.stop()
  }
}
