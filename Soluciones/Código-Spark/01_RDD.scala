import org.apache.spark.sql.SparkSession
import org.apache.spark.HashPartitioner

object RDDExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("RDDExample")
      .master("local[*]")
      .getOrCreate()

    // Crear un RDD a partir de una colección
    val rdd = spark.sparkContext.parallelize(1 to 1000000, 100)

    // Aplicar transformaciones complejas
    val resultRDD = rdd
      .map(x => (x % 10, x))
      .repartitionAndSortWithinPartitions(new HashPartitioner(10))
      .mapValues(x => x * x)
      .reduceByKey(_ + _)

    // Acción para materializar el resultado
    val result = resultRDD.collect()

    // Imprimir los primeros 10 elementos del resultado
    result.take(10).foreach(println)

    // Detener la sesión de Spark
    spark.stop()
  }
}