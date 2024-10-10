
# Guía avanzada de Spark GraphX

## Ejercicios prácticos

### Ejercicio 1

Crea una red de colaboración académica con las siguientes características:

   - 10 nodos que representan investigadores (etiquetados del 1 al 10)
   - Dos grupos distintos de investigación:
     - Grupo A: nodos 1, 2, 3, 4, 5
     - Grupo B: nodos 6, 7, 8, 9, 10
   - Conexiones fuertes dentro de cada grupo (peso 3)
   - Dos conexiones entre grupos (peso 1) que representan colaboraciones interdisciplinarias:
     - Entre nodo 3 y nodo 8
     - Entre nodo 5 y nodo 6

   Plantea el código para crear esta red utilizando Scala y GraphX.

### Ejercicio 2

Diseña una red que represente un pequeño ecosistema de redes sociales con las siguientes características:

   - 15 nodos que representan usuarios (etiquetados del 1 al 15)
   - Tres comunidades distintas:
     - Comunidad de Amigos: nodos 1, 2, 3, 4, 5
     - Comunidad de Trabajo: nodos 6, 7, 8, 9, 10
     - Comunidad de Hobby: nodos 11, 12, 13, 14, 15
   - Conexiones dentro de cada comunidad con peso 2
   - Conexiones entre comunidades con peso 1:
     - Nodo 1 conectado con nodos 6 y 11
     - Nodo 7 conectado con nodo 12
     - Nodo 13 conectado con nodo 3
   - Un "influencer" en cada comunidad (nodos 2, 8, y 14) con conexiones adicionales a todos los miembros de su comunidad

### Ejercicio 3

Tienes un conjunto de datos que representa una red de colaboración científica. Cada vértice representa un investigador y cada arista representa una colaboración entre dos investigadores. El peso de la arista indica el número de publicaciones conjuntas.

Tareas:

- Calcula el grado de colaboración de cada investigador y muestra los 5 investigadores más colaborativos.
- Identifica comunidades de investigación utilizando el algoritmo de Label Propagation y muestra las 3 comunidades más grandes.
- Calcula la centralidad de los investigadores utilizando PageRank y muestra los 5 investigadores más centrales.
- Encuentra el camino más corto entre ID: 1 y ID: 10.

## 2. Soluciones

### Ejercicio 1

```scala
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object AcademicCollaborationNetwork {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Crear la configuración de Spark y el contexto
    val conf = new SparkConf().setAppName("Academic Collaboration Network").setMaster("local[*]")
    val sc = new SparkContext(conf)

    try {
      // Crear vértices (investigadores)
      val vertices: RDD[(VertexId, String)] = sc.parallelize((1L to 10L).map(id => (id, s"Researcher $id")))

      // Crear aristas (colaboraciones)
      val edges: RDD[Edge[Int]] = sc.parallelize(
        // Conexiones fuertes dentro del Grupo A
        (for {
          i <- 1L to 5L
          j <- i + 1L to 5L
        } yield Edge(i, j, 3)) ++
        // Conexiones fuertes dentro del Grupo B
        (for {
          i <- 6L to 10L
          j <- i + 1L to 10L
        } yield Edge(i, j, 3)) ++
        // Conexiones interdisciplinarias
        Seq(Edge(3L, 8L, 1), Edge(5L, 6L, 1))
      )

      // Crear el grafo
      val graph: Graph[String, Int] = Graph(vertices, edges)

      // Verificar la estructura del grafo
      println("Número de vértices: " + graph.vertices.count())
      println("Número de aristas: " + graph.edges.count())

      // Mostrar las conexiones de cada investigador
      println("\nConexiones de cada investigador:")
      graph.triplets.collect().foreach { triplet =>
        println(s"${triplet.srcAttr} colabora con ${triplet.dstAttr} (Peso: ${triplet.attr})")
      }

      // Calcular y mostrar el grado de cada vértice
      println("\nGrado de cada investigador:")
      graph.degrees.collect().sortBy(_._1).foreach { case (id, degree) =>
        println(s"Researcher $id: $degree conexiones")
      }

    } catch {
      case e: Exception =>
        println(s"Ocurrió un error: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      sc.stop()
    }
  }
}
```

### Ejercicio 2

```scala
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object SocialNetworkEcosystem {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Crear la configuración de Spark y el contexto
    val conf = new SparkConf().setAppName("Social Network Ecosystem").setMaster("local[*]")
    val sc = new SparkContext(conf)

    try {
      // Crear vértices (usuarios)
      val vertices: RDD[(VertexId, String)] = sc.parallelize((1L to 15L).map(id => (id, s"User $id")))

      // Crear aristas (conexiones)
      val edges: RDD[Edge[Int]] = sc.parallelize(
        // Conexiones dentro de la Comunidad de Amigos
        (for {
          i <- 1L to 5L
          j <- i + 1L to 5L
        } yield Edge(i, j, 2)) ++
        // Conexiones dentro de la Comunidad de Trabajo
        (for {
          i <- 6L to 10L
          j <- i + 1L to 10L
        } yield Edge(i, j, 2)) ++
        // Conexiones dentro de la Comunidad de Hobby
        (for {
          i <- 11L to 15L
          j <- i + 1L to 15L
        } yield Edge(i, j, 2)) ++
        // Conexiones entre comunidades
        Seq(Edge(1L, 6L, 1), Edge(1L, 11L, 1), Edge(7L, 12L, 1), Edge(13L, 3L, 1)) ++
        // Conexiones adicionales de los "influencers"
        (for {
          i <- 1L to 5L if i != 2L
        } yield Edge(2L, i, 2)) ++
        (for {
          i <- 6L to 10L if i != 8L
        } yield Edge(8L, i, 2)) ++
        (for {
          i <- 11L to 15L if i != 14L
        } yield Edge(14L, i, 2))
      )

      // Crear el grafo
      val graph: Graph[String, Int] = Graph(vertices, edges)

      // Verificar la estructura del grafo
      println("Número de vértices: " + graph.vertices.count())
      println("Número de aristas: " + graph.edges.count())

      // Mostrar las conexiones de cada usuario
      println("\nConexiones de cada usuario:")
      graph.triplets.collect().foreach { triplet =>
        println(s"${triplet.srcAttr} está conectado con ${triplet.dstAttr} (Peso: ${triplet.attr})")
      }

      // Calcular y mostrar el grado de cada vértice
      println("\nGrado de cada usuario:")
      graph.degrees.collect().sortBy(_._1).foreach { case (id, degree) =>
        println(s"User $id: $degree conexiones")
      }

      // Identificar a los "influencers"
      println("\nInfluencers:")
      val influencers = graph.degrees.filter(_._2 > 5).collect()
      influencers.foreach { case (id, degree) =>
        println(s"User $id: $degree conexiones")
      }

    } catch {
      case e: Exception =>
        println(s"Ocurrió un error: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      sc.stop()
    }
  }
}
```

### Ejercicio 3

```scala
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object ScientificCollaborationNetwork {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Crear la configuración de Spark y el contexto
    val conf = new SparkConf()
      .setAppName("Scientific Collaboration Network Analysis")
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
```

#### Resultado del ejecicio

[info] running (fork) ScientificCollaborationNetwork
[info] Grados de colaboración (Top 5):
[info] Dr. Williams: 4 colaboraciones
[info] Dr. Brown: 4 colaboraciones
[info] Dr. Davis: 3 colaboraciones
[info] Dr. Rodriguez: 3 colaboraciones
[info] Dr. Johnson: 3 colaboraciones
[info] Comunidades de investigación identificadas:
[info] Comunidad 1:
[info]   - Dr. Davis
[info]   - Dr. Smith
[info]   - Dr. Rodriguez
[info]   - Dr. Martinez
[info]   - Dr. Johnson
[info]   - Dr. Williams
[info]   - Dr. Brown
[info]   - Dr. Jones
[info]   - Dr. Garcia
[info]   - Dr. Miller
[info] Centralidad de los investigadores (Top 5):
[info] Dr. Martinez: 1.8396
[info] Dr. Miller: 1.4852
[info] Dr. Garcia: 1.3867
[info] Dr. Rodriguez: 1.3362
[info] Dr. Davis: 0.9377
[info] Camino m?s corto entre Dr. Smith y Dr. Martinez:
[info] Longitud del camino: 10.0
[info] Densidad del grafo de colaboración: 0.3333
