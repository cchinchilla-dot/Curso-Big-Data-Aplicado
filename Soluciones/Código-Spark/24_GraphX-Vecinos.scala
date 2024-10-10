import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object GraphXExampleFilter {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    val conf = new SparkConf().setAppName("GraphX Example Filter").setMaster("local[*]")
    val sc = new SparkContext(conf)

    try {
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
      graph.vertices.collect().foreach { case (id, name) =>
        println(s"ID: $id, Nombre: $name")
      }

      println("\nAristas:")
      graph.edges.collect().foreach { case Edge(src, dst, relationship) =>
        println(s"Origen: $src, Destino: $dst, Relación: $relationship")
      }
      
      // Filtrar las aristas para encontrar las conexiones del nodo 1L (Alice).
      val nodeId: VertexId = 1L
      val neighbors = graph.edges
        .filter(e => e.srcId == nodeId || e.dstId == nodeId)   // Filtra las aristas donde el nodo de origen o destino sea el nodo 1.
        .map(e => if (e.srcId == nodeId) e.dstId else e.srcId) // Si el nodo 1 es el origen, guarda el destino, de lo contrario guarda el origen.
        .collect()                                             // Recolecta los resultados en una lista.
        .distinct                                              // Elimina nodos duplicados, en caso de que haya conexiones repetidas.

      // Mostrar los vecinos del nodo 1L (Alice).
      println(s"\nVecinos del nodo $nodeId (${graph.vertices.filter(_._1 == nodeId).first()._2}):")
      neighbors.foreach { neighborId =>
        val neighborName = graph.vertices.filter(_._1 == neighborId).first()._2  // Obtiene el nombre del vecino a partir de su ID.
        println(s"ID: $neighborId, Nombre: $neighborName")  // Muestra el ID y nombre de cada vecino de Alice.
      }

      // Calcular y mostrar el grado del nodo 1L (número de vecinos).
      val nodeDegree = neighbors.length
      println(s"\nGrado del nodo $nodeId: $nodeDegree")      // Muestra el número de conexiones (grado) que tiene el nodo 1 (Alice).


    } catch {
      case e: Exception =>
        println(s"Ocurrió un error: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      sc.stop()
    }
  }
}