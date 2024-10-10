import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.graphx.lib.ShortestPaths
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object GraphXShortestPathsExample {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Crear la configuración de Spark y el contexto
    val conf = new SparkConf()
      .setAppName("GraphX Shortest Paths Example")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)

    try {
      // Crear un grafo de ejemplo
      val vertices: RDD[(VertexId, String)] = sc.parallelize(Array(
        (1L, "A"), (2L, "B"), (3L, "C"), (4L, "D"),
        (5L, "E"), (6L, "F"), (7L, "G")
      ))

      // Definir las aristas en ambas direcciones para crear un grafo no dirigido
      val edges: RDD[Edge[Int]] = sc.parallelize(Array(
        Edge(1L, 2L, 5), Edge(2L, 1L, 5),
        Edge(1L, 3L, 2), Edge(3L, 1L, 2),
        Edge(2L, 4L, 1), Edge(4L, 2L, 1),
        Edge(3L, 4L, 6), Edge(4L, 3L, 6),
        Edge(3L, 5L, 4), Edge(5L, 3L, 4),
        Edge(4L, 5L, 3), Edge(5L, 4L, 3),
        Edge(4L, 6L, 8), Edge(6L, 4L, 8),
        Edge(5L, 7L, 2), Edge(7L, 5L, 2),
        Edge(6L, 7L, 1), Edge(7L, 6L, 1)
      ))

      val graph: Graph[String, Int] = Graph(vertices, edges)

      // Definir el nodo fuente
      val sourceId: VertexId = 1L

      // Ejecutar el algoritmo de Shortest Paths
      val shortestPaths = ShortestPaths.run(graph, Seq(sourceId))

      // Mostrar los resultados
      println(s"Shortest paths from vertex $sourceId:")
      shortestPaths.vertices.collect().sortBy(_._1).foreach { case (id, spMap) =>
        val vertexName = graph.vertices.filter(_._1 == id).first()._2
        val distance = spMap.getOrElse(sourceId, Int.MaxValue)
        println(s"To vertex $id ($vertexName): ${if(distance == Int.MaxValue) "Unreachable" else distance}")
      }

      // Encontrar el nodo más lejano
      val farthestNode = shortestPaths.vertices.map { case (id, spMap) =>
        (id, spMap.getOrElse(sourceId, Int.MaxValue))
      }.reduce((a, b) => if (a._2 > b._2) a else b)

      println(s"\nFarthest reachable node from $sourceId: " +
        s"${farthestNode._1} (${graph.vertices.filter(_._1 == farthestNode._1).first()._2}) " +
        s"with distance ${farthestNode._2}")

      // Calcular la distancia promedio desde el nodo fuente
      val avgDistance = shortestPaths.vertices.map { case (_, spMap) =>
        spMap.getOrElse(sourceId, Int.MaxValue).toDouble
      }.filter(_ != Int.MaxValue).mean()

      println(f"\nAverage distance from source node: $avgDistance%.2f")

    } catch {
      case e: Exception =>
        println(s"An error occurred: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      sc.stop()
    }
  }
}