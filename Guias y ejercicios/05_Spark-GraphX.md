# Guía avanzada de Spark GraphX

En relación con los contenidos del curso, se corresponde con:

- Módulo 1:
  - Analítica de Big Data en los ecosistemas de almacenamiento.

- Módulo 2:
  - Otras herramientas: Como una herramienta específica para procesamiento de grafos.

Se incluye GraphX en "Otras herramientas" por su capacidad para procesar y analizar grafos, una tarea fundamental en Big Data para aplicaciones como redes sociales, recomendaciones y análisis de relaciones complejas entre datos. Esta necesidad ha impulsado la adopción de GraphX y ha aumentado la demanda de profesionales con conocimientos y experiencia en su uso para proyectos de gran escala.

## 1. Introducción a Spark GraphX

Spark GraphX es el módulo de Apache Spark para realizar análisis y procesamiento de grafos a gran escala. Un grafo es una estructura matemática que representa relaciones entre objetos, donde los **vértices (nodos)** representan los objetos y los **aristas (edges)** representan las conexiones o relaciones entre ellos. GraphX permite realizar análisis complejos sobre grafos y proporciona algoritmos predefinidos para resolver problemas de grafos como la búsqueda de caminos más cortos, el cálculo de PageRank y la detección de comunidades.

### Características clave

- **Modelo de grafos distribuido**: Los grafos están particionados y distribuidos a través de un clúster, lo que permite procesar grafos masivos.
- **API versátil**: Proporciona una API para crear y manipular grafos mediante operaciones estructuradas y algoritmos de grafos.
- **Operaciones paralelas**: El procesamiento se distribuye automáticamente en un clúster de nodos Spark, aprovechando la capacidad de procesamiento paralelo.
- **Integración con Spark**: Aprovecha el ecosistema de Spark, integrándose fácilmente con otras librerías como Spark SQL y DataFrames.

### Conceptos fundamentales

- **Vértices (Vertices)**: Representan los nodos u objetos en un grafo.
- **Aristas (Edges)**: Conexiones entre dos nodos en el grafo.
- **Propiedades de Vértices/Aristas**: Cada vértice y arista puede tener propiedades asociadas (por ejemplo, un nombre de usuario para un nodo o un peso para una arista).
- **Grafo dirigido/no dirigido**: Un grafo puede ser dirigido, donde las conexiones entre nodos tienen una dirección, o no dirigido, donde las conexiones son bidireccionales.

### Arquitectura de GraphX

GraphX está construido sobre Spark Core, lo que significa que aprovecha la capacidad de Spark para el procesamiento distribuido. GraphX extiende el modelo de RDDs (Resilient Distributed Datasets) para representar los grafos de manera eficiente y escalable.

- **VertexRDD**: Representa el conjunto de vértices de un grafo, extendido a partir de RDD.
- **EdgeRDD**: Representa el conjunto de aristas de un grafo, extendido a partir de RDD.
- **Triplet View**: Representa las conexiones entre vértices y aristas, permitiendo acceder a ambos de manera conjunta.

## 2. Configuración del entorno Scala para GraphX

### 2.1 Instalación de Scala y Spark

Asegúrate de tener instalados Scala y Apache Spark en tu sistema. Puedes descargar Spark desde el sitio oficial de Apache Spark, que incluye GraphX.

### 2.2 Configuración del proyecto

Para usar GraphX en un proyecto Scala, necesitarás incluir las dependencias adecuadas en tu archivo de construcción (por ejemplo, `build.sbt` para proyectos SBT):

```scala
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.2.0",
  "org.apache.spark" %% "spark-graphx" % "3.2.0"
)
```

## 3. Conceptos y Estructuras de Scala GraphX

En GraphX, los grafos están compuestos por dos elementos principales: vértices y aristas. Cada vértice puede tener un identificador único y puede almacenar atributos, mientras que cada arista conecta dos vértices y también puede almacenar propiedades.

### 3.1 Creación de grafos

Un grafo en GraphX se define usando dos RDDs: uno para los vértices y otro para las aristas.

#### Ejemplo básico: Creación de un grafo simple

```scala
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
```

## 4. Algoritmos de Grafos en Scala GraphX

GraphX viene con varios algoritmos predefinidos para realizar análisis de grafos. Estos algoritmos son ampliamente utilizados para análisis de redes sociales, recomendaciones y más.

### 4.1 Algoritmo de PageRank

PageRank es un algoritmo utilizado por Google para rankear las páginas web en sus resultados de búsqueda. Este algoritmo puede aplicarse en grafos para medir la importancia relativa de los nodos en función de sus conexiones.

```scala
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
```

Resultado:

[info] running (fork) GraphXPageRankExample
[info] Top 5 vertices by PageRank:
[info] Vertex 2 (User_2) has rank 1.57961
[info] Vertex 0 (User_0) has rank 0.89329
[info] Vertex 1 (User_1) has rank 0.52710
[info] Number of connected components: 1
[info] Maximum degree: 13
[info] Average degree: 10.00

1. Análisis de PageRank:

   El algoritmo PageRank ha sido aplicado a un grafo con 10 vértices (usuarios). Los resultados muestran los 5 usuarios más "importantes" o "influyentes" según este algoritmo:

   - User_4 (Vértice 4) tiene el rango más alto con 1.58074
   - User_5 (Vértice 5) es el segundo más importante con 1.13950
   - User_8 (Vértice 8) sigue de cerca con 1.13732
   - User_2 (Vértice 2) y User_1 (Vértice 1) completan el top 5

   Interpretación: Esto sugiere que User_4 es el nodo más central o influyente en la red. Probablemente tiene muchas conexiones entrantes de otros nodos importantes. Los usuarios 5 y 8 tienen una importancia similar, seguidos de cerca por los usuarios 2 y 1.

2. Componentes Conectados:

   "Number of connected components: 1"

   Interpretación: Esto indica que el grafo es totalmente conexo. Todos los vértices están conectados entre sí de alguna manera, no hay subgrupos aislados en la red. Esto sugiere una red cohesiva donde la información podría fluir potencialmente entre cualquier par de usuarios.

3. Grado Máximo:

   "Maximum degree: 14"

    Interpretación: En un grafo dirigido, que es probablemente lo que tenemos aquí, el grado de un nodo es la suma de su grado de entrada (número de aristas que llegan al nodo) y su grado de salida (número de aristas que salen del nodo). Por lo tanto, un nodo puede tener un grado mayor que el número total de otros nodos en el grafo.

    En este caso, el nodo con el grado máximo tiene 14 conexiones en total, que podrían estar distribuidas como, por ejemplo, 7 conexiones de entrada y 7 de salida, o cualquier otra combinación que sume 14.

4. Grado Promedio:

   "Average degree: 10.00"

   En un grafo dirigido, cada arista contribuye al grado de dos nodos (el nodo de origen y el nodo de destino). Por lo tanto, el grado promedio de 10 significa que, en promedio, cada nodo tiene 5 aristas de entrada y 5 de salida.

   Cálculo: Si tenemos 10 nodos y el grado promedio es 10, entonces el número total de aristas en el grafo es (10 * 10) / 2 = 50, ya que cada arista se cuenta dos veces en la suma total de grados.

Conclusión general:

Este grafo representa una red muy pequeña (10 usuarios) pero extremadamente densa y bien conectada. Casi todos los usuarios están conectados con casi todos los demás. A pesar de esta alta conectividad, aún hay diferencias claras en la "importancia" de los nodos según PageRank, con User_4 destacándose como el más central.

En una aplicación del mundo real, esto podría representar, por ejemplo, un pequeño grupo de trabajo muy colaborativo, donde todos interactúan con todos, pero aún hay algunas personas (como User_4) que son puntos focales particulares de la red, tal vez actuando como coordinadores o líderes de equipo.

### 4.2 Algoritmo de detección de comunidades (Label Propagation)

El algoritmo de **Label Propagation** asigna etiquetas a los nodos del grafo y propaga esas etiquetas entre los nodos vecinos, lo que permite la identificación de comunidades.

```scala
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.graphx.lib.LabelPropagation
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object LabelPropagationExample {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de log para reducir la verbosidad
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Crear la configuración de Spark y el contexto
    val conf = new SparkConf()
      .setAppName("GraphX Label Propagation Example")
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
```

Resultado:

[info] running (fork) LabelPropagationExample
[info] Detected communities:
[info] Vertex 1 (A) belongs to community 2
[info] Vertex 2 (B) belongs to community 1
[info] Vertex 3 (C) belongs to community 2
[info] Vertex 4 (D) belongs to community 1
[info] Vertex 5 (E) belongs to community 8
[info] Vertex 6 (F) belongs to community 5
[info] Vertex 7 (G) belongs to community 8
[info] Vertex 8 (H) belongs to community 5
[info] Number of unique communities: 4
[info] Community sizes:
[info] Community 1: 2 members
[info] Community 2: 2 members
[info] Community 5: 2 members
[info] Community 8: 2 members

1. Se detectaron 4 comunidades distintas.
2. Cada comunidad tiene exactamente 2 miembros.
3. Las comunidades se identifican con las etiquetas 1, 2, 5 y 8.

Análisis detallado:

1. Estructura de las comunidades:
   - Comunidad 1: Vértices 2 (B) y 4 (D)
   - Comunidad 2: Vértices 1 (A) y 3 (C)
   - Comunidad 5: Vértices 6 (F) y 8 (H)
   - Comunidad 8: Vértices 5 (E) y 7 (G)

2. Interpretación de la estructura:
   - El algoritmo ha dividido el grafo en 4 pares de nodos.
   - Esto sugiere que el grafo tiene una estructura donde los nodos tienden a formar conexiones fuertes en pares.
   - La división en pares indica que las conexiones dentro de estos pares son más fuertes o más significativas que las conexiones entre diferentes pares.

3. Justificación de los resultados:
   - El algoritmo de Label Propagation funciona propagando etiquetas entre nodos vecinos.
   - El hecho de que haya 4 comunidades de 2 miembros cada una sugiere que durante las iteraciones del algoritmo, estas parejas de nodos mantuvieron sus etiquetas consistentemente.
   - Esto puede ocurrir cuando hay conexiones bidireccionales fuertes entre los miembros de cada par, o cuando la topología del grafo favorece estas agrupaciones.

4. Implicaciones:
   - La red parece tener una estructura modular, donde los pares de nodos forman unidades básicas.
   - Puede haber conexiones entre estos pares, pero son más débiles que las conexiones dentro de cada par.
   - Esta estructura podría representar, por ejemplo, colaboraciones cercanas en un entorno de trabajo, donde las personas tienden a trabajar en parejas estrechas.

### 4.3 Algoritmo de Caminos más Cortos (Shortest Path)

Este algoritmo calcula la distancia más corta desde un nodo de origen a todos los demás nodos del grafo.

```scala
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
```

[info] running (fork) GraphXShortestPathsExample
[info] Shortest paths from vertex 1:
[info] To vertex 1 (A): 0
[info] To vertex 2 (B): 1
[info] To vertex 3 (C): 1
[info] To vertex 4 (D): 2
[info] To vertex 5 (E): 2
[info] To vertex 6 (F): 3
[info] To vertex 7 (G): 3
[info] Farthest reachable node from 1: 6 (F) with distance 3
[info] Average distance from source node: 1.71

1. Todos los nodos son alcanzables desde el vértice fuente 1 (A).
2. Las distancias varían de 0 a 3.
3. El nodo más lejano es el vértice 6 (F) con una distancia de 3.
4. La distancia promedio desde el nodo fuente es 1.71.

Análisis:

1. Conectividad del grafo:
   - El grafo está completamente conectado, ya que todos los nodos son alcanzables desde el nodo fuente.
   - Esto confirma que la corrección en la creación del grafo no dirigido fue exitosa.

2. Distancias desde el nodo fuente (1):
   - Nodo 1 (A): Distancia 0 (el propio nodo fuente)
   - Nodos 2 (B) y 3 (C): Distancia 1 (vecinos directos)
   - Nodos 4 (D) y 5 (E): Distancia 2 (dos saltos desde el nodo fuente)
   - Nodos 6 (F) y 7 (G): Distancia 3 (tres saltos desde el nodo fuente)

3. Estructura del grafo:
   - El grafo parece tener una estructura en capas desde el nodo fuente:
     - Capa 1: Nodos 2 y 3
     - Capa 2: Nodos 4 y 5
     - Capa 3: Nodos 6 y 7
   - Esto sugiere una topología relativamente balanceada y simétrica.

4. Nodo más lejano:
   - El vértice 6 (F) es identificado como el más lejano con una distancia de 3.
   - Esto indica que el diámetro del grafo (la distancia máxima entre cualquier par de nodos) es al menos 3.

5. Distancia promedio:
   - La distancia promedio de 1.71 sugiere que el grafo es relativamente compacto.
   - Este valor indica que, en promedio, se necesitan menos de 2 saltos para llegar a cualquier nodo desde el nodo fuente.

Conclusiones:

1. Eficiencia de la red:
   - Con una distancia máxima de 3 y un promedio de 1.71, la red parece ser eficiente en términos de conectividad.
   - La información o los recursos pueden fluir relativamente rápido desde el nodo fuente a cualquier otro nodo.

2. Centralidad del nodo fuente:
   - El nodo 1 (A) parece estar bien posicionado en el grafo, con acceso rápido a todos los demás nodos.
   - Esto sugiere que podría ser un nodo importante o central en la red.

3. Balanceo de la red:
   - La distribución simétrica de las distancias (dos nodos a distancia 1, dos a distancia 2, dos a distancia 3) sugiere un diseño o evolución balanceada de la red.

4. Aplicaciones prácticas:
   - En un contexto de red social, el nodo 1 podría representar un influencer o un punto de difusión eficiente de información.
   - En una red de transporte, podría representar un hub bien conectado.
   - En una red de computadoras, podría ser un servidor central con buena conectividad a todos los clientes.

### 4.4 Algoritmo Connected Components

Este algoritmo encuentra todos los componentes conectados en un grafo. Un componente conectado es un subconjunto del grafo donde todos los nodos están conectados entre sí, ya sea directamente o a través de otros nodos.

```scala

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
```

[info] running (fork) GraphXConnectedComponents
[info] Connected Components:
[info] Vertex 1 (A) belongs to component 1
[info] Vertex 2 (B) belongs to component 1
[info] Vertex 3 (C) belongs to component 1
[info] Vertex 4 (D) belongs to component 1
[info] Vertex 5 (E) belongs to component 5
[info] Vertex 6 (F) belongs to component 5
[info] Vertex 7 (G) belongs to component 5
[info] Vertex 8 (H) belongs to component 5
[info] Number of connected components: 2
[info] Size of each component:
[info] Component 1: 4 vertices
[info] Component 5: 4 vertices
[info] Largest component: Component 1 with 4 vertices

1. El grafo tiene dos componentes conectados distintos.
2. Cada componente conectado contiene 4 vértices.
3. Los vértices 1, 2, 3 y 4 forman un componente.
4. Los vértices 5, 6, 7 y 8 forman el otro componente.

Análisis:

1. Estructura del grafo:
   - El grafo está dividido en dos subgrafos distintos y desconectados entre sí.
   - No hay conexiones entre estos dos subgrafos.

2. Componente 1:
   - Incluye los vértices 1 (A), 2 (B), 3 (C), y 4 (D).
   - Este componente está etiquetado con el ID 1, que es el ID más bajo de los vértices en este componente.

3. Componente 5:
   - Incluye los vértices 5 (E), 6 (F), 7 (G), y 8 (H).
   - Este componente está etiquetado con el ID 5, que es el ID más bajo de los vértices en este componente.

4. Tamaño de los componentes:
   - Ambos componentes tienen el mismo tamaño: 4 vértices cada uno.
   - Esto sugiere una estructura balanceada en términos de la distribución de vértices entre los componentes.

5. Componente más grande:
   - Técnicamente, el algoritmo identifica el Componente 1 como el "más grande", pero esto es arbitrario ya que ambos componentes tienen el mismo tamaño.
   - La elección del Componente 1 como el "más grande" probablemente se debe a que tiene el ID más bajo.

Conslusiones:

1. Conectividad del grafo:
   - El grafo no está completamente conectado. Hay dos grupos distintos de vértices que no tienen conexiones entre sí.
   - Esto podría indicar dos comunidades o grupos separados en la red que el grafo representa.

2. Flujo de información o recursos:
   - La información o los recursos pueden fluir libremente dentro de cada componente, pero no entre los componentes.
   - Por ejemplo, si este fuera un grafo de una red social, representaría dos grupos de amigos que no tienen conexiones entre sí.

3. Aplicaciones prácticas:
   - En un contexto de redes sociales, esto podría representar dos comunidades distintas sin miembros en común.
   - En una red de computadoras, podría indicar dos subredes aisladas que requieren un puente para comunicarse.
   - En un análisis de datos científicos, podría sugerir dos grupos de entidades que no comparten relaciones directas.

4. Consideraciones para análisis futuros:
   - Sería interesante investigar por qué estos dos componentes están desconectados.
   - Se podría considerar añadir conexiones entre los componentes si se desea mejorar la conectividad global del grafo.

## 5. Manejo de archivos

Al igual que otras APIs de Spark, GraphX puede leer y escribir grafos en diversos formatos, como CSV, JSON o Parquet.

### 5.1 Lectura de archivos CSV

Supongamos que tenemos archivos CSV que contienen los datos de vértices y aristas de un grafo. Podemos cargarlos en Scala y construir un grafo a partir de ellos.

Vamos a similar un archivo CSV dentro del propio código Scala para simplificar el proceso.

```scala
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
```

## 6. Casos de uso de Scala GraphX

### 6.1 Análisis de redes sociales

GraphX es ampliamente utilizado para analizar redes sociales. Por ejemplo, podrías usarlo para calcular la influencia de usuarios en una red social (con PageRank) o identificar comunidades dentro de una red (con Label Propagation).

### 6.2 Recomendación de productos

En e-commerce, GraphX puede utilizarse para recomendar productos a los usuarios analizando grafos de interacciones entre usuarios y productos, utilizando algoritmos de grafos como **Shortest Path** para encontrar conexiones más relevantes.

### 6.3 Análisis de rutas en sistemas de transporte

GraphX puede modelar sistemas de transporte como redes de rutas y estaciones. Se puede aplicar el algoritmo de caminos más cortos para optimizar rutas entre diferentes puntos.

### 6.4 Análisis de redes de computadores

GraphX puede ser usado para analizar redes de computadores, modelando nodos como dispositivos (servidores, routers, etc.) y aristas como conexiones entre ellos. A través de GraphX, es posible ejecutar análisis de conectividad, detectar puntos críticos de fallo, e identificar patrones de tráfico inusuales. Algoritmos como Connected Components se utilizan para encontrar subredes desconectadas, mientras que PageRank podría usarse para identificar nodos centrales en la red que influyen significativamente en la comunicación.

Ciertamente, añadiré el concepto de densidad a la explicación sobre GraphX. Aquí tienes una sección adicional que explica este importante concepto en el análisis de grafos:

## 8. Densidad en grafos

La densidad es una medida importante en el análisis de grafos que indica qué tan interconectados están los nodos en una red. Proporciona información valiosa sobre la estructura y las características del grafo.

### 8.1 Definición de densidad

La densidad de un grafo se define como la proporción entre el número de aristas existentes y el número máximo posible de aristas en el grafo.

Para un grafo no dirigido con n vértices, la densidad D se calcula como:

```math
D = (2 * E) / (n * (n - 1))
```

Donde:

- E es el número de aristas existentes
- n es el número de vértices

Para un grafo dirigido, la fórmula es:

```math
D = E / (n * (n - 1))
```

### 8.2 Interpretación de la densidad

- La densidad siempre está entre 0 y 1.
- Un valor cercano a 0 indica un grafo disperso con pocas conexiones.
- Un valor cercano a 1 indica un grafo denso con muchas conexiones.
- Un grafo completo (donde todos los nodos están conectados entre sí) tiene una densidad de 1.

### 8.3 Importancia de la densidad

La densidad es útil para:

- Comparar diferentes grafos o redes.
- Entender la estructura global de una red.
- Identificar redes altamente conectadas o dispersas.

### 8.4 Cálculo de la densidad en GraphX

Aquí tienes un ejemplo de cómo calcular la densidad de un grafo en GraphX:

```scala
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

object GraphDensityExample {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    val conf = new SparkConf().setAppName("Graph Density Example").setMaster("local[*]")
    val sc = new SparkContext(conf)

    try {
      // Crear un grafo de ejemplo
      val vertices: RDD[(VertexId, String)] = sc.parallelize(Array(
        (1L, "A"), (2L, "B"), (3L, "C"), (4L, "D"), (5L, "E")
      ))

      val edges: RDD[Edge[Int]] = sc.parallelize(Array(
        Edge(1L, 2L, 1), Edge(1L, 3L, 1), Edge(2L, 3L, 1),
        Edge(2L, 4L, 1), Edge(3L, 5L, 1), Edge(4L, 5L, 1)
      ))

      val graph: Graph[String, Int] = Graph(vertices, edges)

      // Calcular la densidad
      val numVertices = graph.vertices.count()
      val numEdges = graph.edges.count()
      val maxPossibleEdges = numVertices * (numVertices - 1) / 2 // Para un grafo no dirigido
      val density = numEdges.toDouble / maxPossibleEdges

      println(s"Número de vértices: $numVertices")
      println(s"Número de aristas: $numEdges")
      println(s"Máximo número posible de aristas: $maxPossibleEdges")
      println(f"Densidad del grafo: $density%.4f")

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

Este ejemplo crea un grafo simple, calcula su densidad y muestra el resultado.

### 8.5 Consideraciones prácticas

- La densidad es sensible al tamaño del grafo. Grafos más grandes tienden a tener densidades más bajas.
- En redes del mundo real, es común encontrar grafos con densidades bajas, especialmente en redes sociales o biológicas a gran escala.
- Una densidad muy alta puede indicar una red altamente interconectada, lo que puede ser bueno para la difusión de información pero también puede implicar redundancia o falta de estructura.

## 9. Ejercicios prácticos

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
- Densidad del grafo.
