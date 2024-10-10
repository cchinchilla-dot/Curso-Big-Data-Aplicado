
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object GraphXConnectedComponents {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Crear la configuración de Spark y el contexto
    val conf = new SparkConf()
      .setAppName("GraphX Connected Components Example")
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
        Edge(5L, 6L, 1), Edge(6L, 7L, 1), Edge(7L, 8L, 1)
      ))

      val graph: Graph[String, Int] = Graph(vertices, edges)

      // Ejecutar el algoritmo Connected Components
      val cc = graph.connectedComponents()

      // Mostrar los componentes conectados
      println("Connected Components:")
      cc.vertices.collect().sortBy(_._1).foreach { case (id, component) =>
        val vertexName = graph.vertices.filter(_._1 == id).first()._2
        println(s"Vertex $id ($vertexName) belongs to component $component")
      }

      // Contar el número de componentes conectados
      val numComponents = cc.vertices.map(_._2).distinct().count()
      println(s"\nNumber of connected components: $numComponents")

      // Calcular el tamaño de cada componente
      val componentSizes = cc.vertices.map { case (_, component) => (component, 1) }
        .reduceByKey(_ + _)
        .collect()
        .sortBy(_._1)

      println("\nSize of each component:")
      componentSizes.foreach { case (component, size) =>
        println(s"Component $component: $size vertices")
      }

      // Encontrar el componente más grande
      val largestComponent = componentSizes.maxBy(_._2)
      println(s"\nLargest component: Component ${largestComponent._1} with ${largestComponent._2} vertices")

    } catch {
      case e: Exception =>
        println(s"An error occurred: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      sc.stop()
    }
  }
}