import org.apache.spark.sql.SparkSession
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object GraphXCSVExample {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Crear la sesión de Spark
    val spark = SparkSession.builder()
      .appName("GraphX CSV Example")
      .master("local[*]")
      .getOrCreate()

    // Importar los implicits de Spark
    import spark.implicits._

    try {
      // Definir el contenido de los CSV como strings (normalmente, estos serían archivos reales)
      val verticesCSV = 
        """id,name
          |1,Alice
          |2,Bob
          |3,Charlie
          |4,David
          |5,Eve""".stripMargin

      val edgesCSV = 
        """src,dst,relationship
          |1,2,friend
          |1,3,friend
          |2,3,colleague
          |3,4,friend
          |4,5,family
          |5,1,friend""".stripMargin

      // Crear DataFrames a partir de los strings CSV
      val vertexDF = spark.read.option("header", "true").csv(verticesCSV.split("\n").toSeq.toDS())
      val edgeDF = spark.read.option("header", "true").csv(edgesCSV.split("\n").toSeq.toDS())

      // Convertir DataFrames a RDDs
      val vertices: RDD[(VertexId, String)] = vertexDF.rdd.map(row => (row.getString(0).toLong, row.getString(1)))
      val edges: RDD[Edge[String]] = edgeDF.rdd.map(row => Edge(row.getString(0).toLong, row.getString(1).toLong, row.getString(2)))

      // Crear el grafo
      val graph: Graph[String, String] = Graph(vertices, edges)

      // Mostrar los vértices y aristas cargados
      println("Vertices:")
      graph.vertices.collect().foreach(println)

      println("\nEdges:")
      graph.edges.collect().foreach(println)

      // Realizar algunos análisis básicos
      println("\nNumero de vértices: " + graph.vertices.count())
      println("Numero de aristas: " + graph.edges.count())

      // Calcular el grado de cada vértice
      println("\nGrado de cada vértice:")
      graph.degrees.collect().foreach { case (id, degree) =>
        val name = graph.vertices.filter(_._1 == id).first()._2
        println(s"$name (ID: $id): $degree")
      }

      // Encontrar el vértice con el grado más alto
      val maxDegreeVertex = graph.degrees.reduce((a, b) => if (a._2 > b._2) a else b)
      val maxDegreeName = graph.vertices.filter(_._1 == maxDegreeVertex._1).first()._2
      println(s"\nVertice con el grado más alto: $maxDegreeName (ID: ${maxDegreeVertex._1}) con grado ${maxDegreeVertex._2}")

    } catch {
      case e: Exception =>
        println(s"An error occurred: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      spark.stop()
    }
  }
}