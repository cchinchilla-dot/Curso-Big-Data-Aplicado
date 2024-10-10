import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

object GraphXExample {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("GraphX Example").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Crear los datos de los vértices (nodos)
    val vertices: RDD[(VertexId, String)] = sc.parallelize(Array(
      (1L, "Alice"),
      (2L, "Bob"),
      (3L, "Charlie"),
      (4L, "David"),
      (5L, "Eve")
    ))

    // Crear los datos de las aristas (conexiones)
    val edges: RDD[Edge[String]] = sc.parallelize(Array(
      Edge(1L, 2L, "Amigos"),
      Edge(2L, 3L, "Colegas"),
      Edge(3L, 4L, "Familia"),
      Edge(4L, 5L, "Amigos"),
      Edge(5L, 1L, "Amigos")
    ))

    // Crear el grafo
    val graph: Graph[String, String] = Graph(vertices, edges)

    // Mostrar los datos de vértices y aristas
    println("Vertices:")
    graph.vertices.collect().foreach(println)
    println("Aristas:")
    graph.edges.collect().foreach(println)

    sc.stop()
  }
}
