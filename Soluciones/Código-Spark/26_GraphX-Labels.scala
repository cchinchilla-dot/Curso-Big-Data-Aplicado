import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.graphx.lib.LabelPropagation
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object GraphXLabelPropagationExample {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Crear la configuración de Spark y el contexto
    val conf = new SparkConf()
      .setAppName("GraphXLabelPropagationExample")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)

    try {
      // Crear un grafo de ejemplo
      val vertices: RDD[(VertexId, String)] = sc.parallelize(Array(
        (1L, "A"), (2L, "B"), (3L, "C"), (4L, "D"),
        (5L, "E"), (6L, "F"), (7L, "G"), (8L, "H")
      ))

      val edges: RDD[Edge[Int]] = sc.parallelize(Array(
        Edge(1L, 2L, 1), Edge(2L, 3L, 1), Edge(3L, 4L, 1),
        Edge(4L, 1L, 1), Edge(5L, 6L, 1), Edge(6L, 7L, 1),
        Edge(7L, 8L, 1), Edge(8L, 5L, 1), Edge(1L, 5L, 1)
      ))

      val graph: Graph[String, Int] = Graph(vertices, edges)

      // Ejecutar el algoritmo de Label Propagation
      val maxIterations = 5
      val labels = LabelPropagation.run(graph, maxIterations)

      // Mostrar las comunidades detectadas
      println("Detected communities:")
      labels.vertices.collect().sortBy(_._1).foreach { case (id, label) =>
        println(s"Vertex $id (${graph.vertices.filter(_._1 == id).first()._2}) belongs to community $label")
      }

      // Contar el número de comunidades únicas
      val uniqueCommunities = labels.vertices.map(_._2).distinct().count()
      println(s"\nNumber of unique communities: $uniqueCommunities")

      // Calcular el tamaño de cada comunidad
      val communitySizes = labels.vertices.map(v => (v._2, 1)).reduceByKey(_ + _)
      println("\nCommunity sizes:")
      communitySizes.collect().sortBy(_._1).foreach { case (community, size) =>
        println(s"Community $community: $size members")
      }

    } catch {
      case e: Exception =>
        println(s"An error occurred: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      sc.stop()
    }
  }
}