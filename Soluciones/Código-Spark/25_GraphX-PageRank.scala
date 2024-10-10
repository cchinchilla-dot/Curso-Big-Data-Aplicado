import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import scala.util.Random
import org.apache.log4j.{Level, Logger}

object GraphXPageRankExample {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Número de vértices al usuario
    val numVertices = 10

    // Crear la configuración de Spark y el contexto
    val conf = new SparkConf()
      .setAppName("GraphX PageRank Example")
      .setMaster("local[*]")
      .set("spark.executor.memory", "1g")
      .set("spark.driver.memory", "1g")

    val sc = new SparkContext(conf)

    try {
      // Calcula el número de aristas basado en el número de vértices
      val numEdges = numVertices * 5  // Aproximadamente 5 aristas por vértice en promedio

      // Genera vértices
      val vertices: RDD[(VertexId, String)] = sc.parallelize(
        (0L until numVertices).map(id => (id, s"User_$id"))
      )

      // Genera aristas aleatorias
      val edges: RDD[Edge[String]] = sc.parallelize(
        (0L until numEdges).map { _ =>
          val src = Random.nextInt(numVertices)
          val dst = Random.nextInt(numVertices)
          Edge(src.toLong, dst.toLong, "connection")
        }
      )

      // Crea el grafo
      val graph: Graph[String, String] = Graph(vertices, edges)

      // Ejecuta PageRank
      val ranks = graph.pageRank(0.0001).vertices

      // Muestra los 5 vértices con mayor PageRank
      println(s"Top 5 vertices by PageRank (out of $numVertices):")
      ranks.join(vertices).sortBy(_._2._1, ascending = false).take(5).foreach {
        case (id, (rank, name)) => println(f"Vertex $id ($name) has rank $rank%.5f")
      }

      // Calcula algunas estadísticas básicas
      val numComponents = graph.connectedComponents().vertices.map(_._2).distinct().count()
      println(s"\nNumber of connected components: $numComponents")

      val maxDegree = graph.degrees.map(_._2).max()
      println(s"Maximum degree: $maxDegree")

      val avgDegree = graph.degrees.map(_._2.toDouble).mean()
      println(f"Average degree: $avgDegree%.2f")

    } catch {
      case e: Exception =>
        println(s"An error occurred: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      // Detener el contexto de Spark
      sc.stop()
    }
  }
}