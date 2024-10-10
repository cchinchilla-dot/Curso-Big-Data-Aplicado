import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object Ejercicio3ScientificCollaborationNetwork {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Crear la configuración de Spark y el contexto
    val conf = new SparkConf()
      .setAppName("Ejercicio 3 Scientific Collaboration Network Analysis")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)

    try {
      // Crear un grafo que representa una red de colaboración científica
      val vertices: RDD[(VertexId, String)] = sc.parallelize(Array(
        (1L, "Dr. Smith"), (2L, "Dr. Johnson"), (3L, "Dr. Williams"),
        (4L, "Dr. Brown"), (5L, "Dr. Jones"), (6L, "Dr. Garcia"),
        (7L, "Dr. Miller"), (8L, "Dr. Davis"), (9L, "Dr. Rodriguez"),
        (10L, "Dr. Martinez")
      ))

      val edges: RDD[Edge[Int]] = sc.parallelize(Array(
        Edge(1L, 2L, 3), Edge(1L, 3L, 2), Edge(2L, 3L, 1),
        Edge(2L, 4L, 2), Edge(3L, 4L, 3), Edge(3L, 5L, 1),
        Edge(4L, 5L, 2), Edge(4L, 6L, 1), Edge(5L, 6L, 3),
        Edge(6L, 7L, 2), Edge(7L, 8L, 1), Edge(7L, 9L, 3),
        Edge(8L, 9L, 2), Edge(8L, 10L, 1), Edge(9L, 10L, 2)
      ))

      val graph: Graph[String, Int] = Graph(vertices, edges)

      // 1. Calcular el grado de colaboración de cada investigador
      val degrees = graph.degrees.collect().sortBy(-_._2)
      println("Grados de colaboración (Top 5):")
      degrees.take(5).foreach { case (id, degree) =>
        val name = graph.vertices.filter(_._1 == id).first()._2
        println(s"$name: $degree colaboraciones")
      }

      // 2. Identificar las comunidades de investigación usando Connected Components
      val cc = graph.connectedComponents()
      val communityGroups = cc.vertices.map { case (id, label) =>
        (label, id)
      }.groupByKey().collect()

      println("\nComunidades de investigación identificadas:")
      communityGroups.sortBy(_._2.size)(Ordering[Int].reverse).take(3).foreach { case (label, members) =>
        println(s"Comunidad $label:")
        members.foreach { id =>
          val name = graph.vertices.filter(_._1 == id).first()._2
          println(s"  - $name")
        }
      }

      // 3. Calcular la centralidad de los investigadores usando PageRank
      val pageRanks = graph.pageRank(0.001).vertices.collect().sortBy(-_._2)
      println("\nCentralidad de los investigadores (Top 5):")
      pageRanks.take(5).foreach { case (id, rank) =>
        val name = graph.vertices.filter(_._1 == id).first()._2
        println(f"$name: $rank%.4f")
      }

      // 4. Encontrar el camino más corto entre dos investigadores usando Pregel
      val sourceId = 1L // Dr. Smith
      val targetId = 10L // Dr. Martinez
      
      val initialGraph = graph.mapVertices((id, _) => if (id == sourceId) 0.0 else Double.PositiveInfinity)
      
      val sssp = initialGraph.pregel(Double.PositiveInfinity)(
        (id, dist, newDist) => math.min(dist, newDist),
        triplet => {
          if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
            Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
          } else {
            Iterator.empty
          }
        },
        (a, b) => math.min(a, b)
      )

      val shortestDistance = sssp.vertices.filter(_._1 == targetId).first()._2

      println(s"\nCamino más corto entre Dr. Smith y Dr. Martinez:")
      println(s"Longitud del camino: $shortestDistance")

      // 5. Calcular la densidad del grafo
      val numVertices = graph.vertices.count()
      val numEdges = graph.edges.count()
      val maxPossibleEdges = numVertices * (numVertices - 1) / 2
      val density = numEdges.toDouble / maxPossibleEdges
      println(f"\nDensidad del grafo de colaboración: $density%.4f")

    } catch {
      case e: Exception =>
        println(s"An error occurred: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      sc.stop()
    }
  }
}