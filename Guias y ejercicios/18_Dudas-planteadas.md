# Aclaración de dudas planteadas durante las sesiones

## Explicación de transformaciones complejas en RDD de Spark

Vamos a analizar la siguiente secuencia de transformaciones:

```python
resultRDD = rdd \
 .map(lambda x: (x % 10, x)) \
 .reduceByKey(lambda a, b: a + b) \
 .mapValues(lambda x: x * x)
```

### Paso 1: map(lambda x: (x % 10, x))

Esta transformación aplica la función lambda a cada elemento del RDD original.

- `x % 10` calcula el residuo de la división de x por 10 (es decir, el último dígito de x).
- El resultado es un par (key, value) donde la clave es el último dígito y el valor es el número original.

Ejemplo:
Si rdd = [12, 23, 34, 45, 56]
Después de este map: [(2, 12), (3, 23), (4, 34), (5, 45), (6, 56)]

### Paso 2: reduceByKey(lambda a, b: a + b)

Esta operación agrupa los elementos por clave (el último dígito en este caso) y aplica la función de reducción a los valores de cada grupo.

- La función lambda `a + b` suma los valores para cada clave.

Continuando el ejemplo:
Después de reduceByKey: [(2, 12), (3, 23), (4, 34), (5, 45), (6, 56)]
(No hay cambios en este caso porque no hay claves repetidas, pero si las hubiera, se sumarían sus valores)

### Paso 3: mapValues(lambda x: x * x)

Esta transformación aplica la función lambda solo a los valores de cada par (key, value), manteniendo las claves sin cambios.

- `x * x` eleva al cuadrado cada valor.

Resultado final:
[(2, 144), (3, 529), (4, 1156), (5, 2025), (6, 3136)]

### Ejemplo completo

Supongamos que tenemos un RDD inicial con más números:

```python
rdd = sc.parallelize([12, 23, 34, 45, 56, 67, 78, 89, 90, 11])

resultRDD = rdd \
 .map(lambda x: (x % 10, x)) \
 .reduceByKey(lambda a, b: a + b) \
 .mapValues(lambda x: x * x)

print(resultRDD.collect())
```

Resultado:

```bash
[(2, 144), (3, 529), (4, 1156), (5, 2025), (6, 3136), (7, 4489), (8, 6084), (9, 7921), (0, 8100), (1, 121)]
```

Explicación del resultado:

- Todos los números que terminan en el mismo dígito se suman.
- Luego, cada suma se eleva al cuadrado.
- Por ejemplo, para la clave 1, solo teníamos el número 11, así que 11^2 = 121.
- Para la clave 0, teníamos 90, así que 90^2 = 8100.

Esta secuencia de transformaciones es útil para realizar agregaciones basadas en alguna característica de los datos (en este caso, el último dígito) y luego aplicar una transformación adicional al resultado agregado.

## Explicación detallada del código Spark GraphX

### Accesos y uso de `._1`, `._2`, y `_._1`

1. Tuplas y acceso a elementos:
   - En Scala, `._1`, `._2`, etc., son métodos para acceder a los elementos de una tupla por su posición.
   - `._1` se refiere al primer elemento, `._2` al segundo, y así sucesivamente.
   - Ejemplo: Para una tupla `val t = (1, "a", true)`, `t._1` devuelve `1`, `t._2` devuelve `"a"`, y `t._3` devuelve `true`.

2. Uso en funciones lambda y patrones de coincidencia:
   - `_._1` y `_._2` se utilizan frecuentemente en funciones lambda (funciones anónimas) cuando se trabaja con colecciones de tuplas.
   - El guion bajo `_` es un comodín que representa toda la tupla, y `._1` o `._2` especifica qué elemento de esa tupla se está utilizando.
   - Ejemplo: `list.sortBy(_._2)` ordena una lista de tuplas basándose en el segundo elemento de cada tupla.

3. Contexto en Spark y GraphX:
   - En Spark RDDs y DataFrames, estas notaciones son comunes al trabajar con pares clave-valor o al acceder a columnas.
   - En GraphX, se usan frecuentemente al manipular vértices y aristas, donde los datos a menudo se representan como tuplas.
   - Ejemplo: `graph.vertices.map { case (id, attr) => (id, attr._2) }` extrae el segundo elemento del atributo de cada vértice.

4. Alternativas y buenas prácticas:
   - Aunque `._1` y `._2` son concisos, pueden hacer el código menos legible en casos complejos.
   - Para mejorar la legibilidad, se pueden usar patrones de coincidencia o case classes.
   - Ejemplo con patrón de coincidencia: `graph.vertices.map { case (id, (name, age)) => (id, age) }`

5. Rendimiento y optimización:
   - El acceso a elementos de tuplas mediante `._1`, `._2`, etc., es una operación de tiempo constante y eficiente.
   - En Spark, usar estas notaciones no afecta significativamente el rendimiento en comparación con otras formas de acceso a datos.

6. Limitaciones y consideraciones:
   - Las tuplas en Scala están limitadas a 22 elementos (Scala 2.x). Para estructuras más grandes, se recomienda usar case classes.
   - El uso excesivo de `._1`, `._2`, etc., puede hacer que el código sea difícil de mantener, especialmente si el significado de cada posición no es inmediatamente claro.

### Explicación de su utilización en los códigos de ejemplo y ejercicios

#### 1. Ejemplo básico: Creación de un grafo simple

```scala
println("Vertices:")
graph.vertices.collect().foreach { case (id, name) =>
  println(s"ID: $id, Nombre: $name")
}
```

- Aquí, `case (id, name)` descompone la tupla retornada por `vertices`. Es equivalente a usar `_._1` para `id` y `_._2` para `name`.

```scala
println("\nAristas:")
graph.edges.collect().foreach { case Edge(src, dst, relationship) =>
  println(s"Origen: $src, Destino: $dst, Relación: $relationship")
}
```

- En este caso, se usa pattern matching para descomponer el objeto `Edge` directamente.

```scala
val neighbors = graph.edges
  .filter(e => e.srcId == nodeId || e.dstId == nodeId)
  .map(e => if (e.srcId == nodeId) e.dstId else e.srcId)
```

- Aquí no se usa `_._1` o `_._2` directamente, pero se accede a los campos `srcId` y `dstId` del objeto `Edge`.

```scala
val neighborName = graph.vertices.filter(_._1 == neighborId).first()._2
```

- `_._1` se usa para acceder al ID del vértice en el filtro.
- `._2` se usa para obtener el nombre del vértice del resultado filtrado.

#### 2. Ejemplo de PageRank

```scala
ranks.join(vertices).sortBy(_._2._1, ascending = false).take(5).foreach {
  case (id, (rank, name)) => println(f"Vertex $id ($name) has rank $rank%.5f")
}
```

- `sortBy(_._2._1, ...)`: Ordena por el primer elemento de la segunda parte de la tupla (el rank).
- `case (id, (rank, name))`: Descompone la tupla compleja resultante del join.

#### 3. Ejemplo de Label Propagation

```scala
labels.vertices.collect().sortBy(_._1).foreach { case (id, label) =>
  println(s"Vertex $id (${graph.vertices.filter(_._1 == id).first()._2}) belongs to community $label")
}
```

- `sortBy(_._1)`: Ordena por el primer elemento de la tupla (el ID del vértice).
- `filter(_._1 == id)`: Filtra los vértices por ID.
- `.first()._2`: Obtiene el segundo elemento (el nombre) del primer vértice que coincide.

#### 4. Ejemplo de Shortest Paths

```scala
shortestPaths.vertices.collect().sortBy(_._1).foreach { case (id, spMap) =>
  val vertexName = graph.vertices.filter(_._1 == id).first()._2
  val distance = spMap.getOrElse(sourceId, Int.MaxValue)
  println(s"To vertex $id ($vertexName): ${if(distance == Int.MaxValue) "Unreachable" else distance}")
}
```

- `sortBy(_._1)`: Ordena por el ID del vértice.
- `filter(_._1 == id).first()._2`: Obtiene el nombre del vértice filtrando por ID.

#### 5. Ejemplo de Connected Components

```scala
cc.vertices.collect().sortBy(_._1).foreach { case (id, component) =>
  val vertexName = graph.vertices.filter(_._1 == id).first()._2
  println(s"Vertex $id ($vertexName) belongs to component $component")
}
```

- `sortBy(_._1)`: Ordena por el ID del vértice.
- `filter(_._1 == id).first()._2`: Obtiene el nombre del vértice.

```scala
val componentSizes = cc.vertices.map { case (_, component) => (component, 1) }
  .reduceByKey(_ + _)
  .collect()
  .sortBy(_._1)
```

- `map { case (_, component) => ... }`: Ignora el primer elemento de la tupla (ID) y usa solo el segundo (componente).
- `sortBy(_._1)`: Ordena los resultados por el ID del componente.

#### 6. Ejemplo de lectura de CSV

```scala
val vertices: RDD[(VertexId, String)] = vertexDF.rdd.map(row => (row.getString(0).toLong, row.getString(1)))
```

- Aunque no usa `_._1` o `_._2`, crea tuplas donde el primer elemento es el ID y el segundo es el nombre.

```scala
graph.degrees.collect().foreach { case (id, degree) =>
  val name = graph.vertices.filter(_._1 == id).first()._2
  println(s"$name (ID: $id): $degree")
}
```

- `filter(_._1 == id)`: Filtra los vértices por ID.
- `.first()._2`: Obtiene el nombre del vértice.

```scala
val maxDegreeVertex = graph.degrees.reduce((a, b) => if (a._2 > b._2) a else b)
val maxDegreeName = graph.vertices.filter(_._1 == maxDegreeVertex._1).first()._2
```

- `a._2 > b._2`: Compara los grados (segundo elemento de la tupla).
- `maxDegreeVertex._1`: Accede al ID del vértice con el grado máximo.
- `filter(_._1 == maxDegreeVertex._1).first()._2`: Obtiene el nombre del vértice con el grado máximo.

Estos ejemplos muestran cómo `_._1` y `_._2` se usan frecuentemente en GraphX para acceder a elementos de tuplas, especialmente en operaciones de filtrado, mapeo y ordenación. También vemos cómo el pattern matching se usa como una alternativa más legible en algunos casos, especialmente cuando se trabaja con estructuras más complejas.

#### Ejercicio 1: Red de Colaboración Académica

Creación de vértices:
  
```scala
(1L to 10L).map(id => (id, s"Researcher $id"))
```

- No usa `._1` o `._2` directamente, pero crea tuplas donde el primer elemento (`._1`) es el ID y el segundo (`._2`) es el nombre del investigador.

Cálculo de grados:

```scala
graph.degrees.collect().sortBy(_._1).foreach { case (id, degree) => ... }
```

- `sortBy(_._1)`: Ordena por el primer elemento de cada tupla, que es el ID del vértice.
- `case (id, degree)`: Descompone la tupla, donde `id` es `._1` y `degree` es `._2`.

## Ejercicio 2: Ecosistema de Redes Sociales

Cálculo de grados:

```scala
graph.degrees.collect().sortBy(_._1).foreach { case (id, degree) => ... }
```

- Idéntico al Ejercicio 1, accede al ID del vértice para ordenar.

Identificación de influencers:

```scala
val influencers = graph.degrees.filter(_._2 > 5).collect()
```

- `_._2 > 5`: Filtra basándose en el segundo elemento de la tupla, que es el grado del vértice.

## Ejercicio 3: Red de Colaboración Científica

Creación de vértices:

```scala
vertices: RDD[(VertexId, String)] = sc.parallelize(Array(
  (1L, "Dr. Smith"), (2L, "Dr. Johnson"), ...
))
```

- Crea tuplas donde `._1` es el ID del vértice y `._2` es el nombre del investigador.

Identificación de comunidades:

```scala
cc.vertices.map { case (id, label) => (label, id) }.groupByKey()
```

- `case (id, label)`: Descompone la tupla del vértice, donde `id` es `._1` y `label` es `._2`.
- Crea una nueva tupla invirtiendo el orden: `(label, id)`.

Cálculo de PageRank:

```scala
graph.pageRank(0.001).vertices.collect().sortBy(-_._2)
```

- `sortBy(-_._2)`: Ordena por el segundo elemento de cada tupla (el valor de PageRank) en orden descendente.

Impresión de resultados de PageRank:

```scala
pageRanks.take(5).foreach { case (id, rank) => ... }
```

- `case (id, rank)`: Descompone la tupla, donde `id` es `._1` y `rank` es `._2`.

Inicialización del grafo para el algoritmo de camino más corto:

```scala
graph.mapVertices((id, _) => if (id == sourceId) 0.0 else Double.PositiveInfinity)
```

- `(id, _)`: `id` es `._1`, y `_` ignora `._2` (el atributo original del vértice).

Implementación de Pregel para el camino más corto:

```scala
triplet => {
  if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
    Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
  } else {
    Iterator.empty
  }
}
```

- Crea nuevas tuplas `(triplet.dstId, triplet.srcAttr + triplet.attr)` donde `._1` es el ID del vértice destino y `._2` es la nueva distancia calculada.

Obtención del resultado del camino más corto:

```scala
val shortestDistance = sssp.vertices.filter(_._1 == targetId).first()._2
```

- `filter(_._1 == targetId)`: Filtra por el primer elemento de la tupla (ID del vértice).
- `first()._2`: Accede al segundo elemento de la tupla resultante (la distancia calculada).

### Ejercicio 1: Red de Colaboración Académica

```scala
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}
```

Estas líneas importan las clases y objetos necesarios de Spark y GraphX.

```scala
object AcademicCollaborationNetwork {
  def main(args: Array[String]): Unit = {
```

Define el objeto principal y el método `main`, que es el punto de entrada del programa.

```scala
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)
```

Configura el nivel de log para reducir la verbosidad de los mensajes de Spark.

```scala
    val conf = new SparkConf().setAppName("Academic Collaboration Network").setMaster("local[*]")
    val sc = new SparkContext(conf)
```

Crea la configuración de Spark y el SparkContext.

```scala
    val vertices: RDD[(VertexId, String)] = sc.parallelize((1L to 10L).map(id => (id, s"Researcher $id")))
```

Crea los vértices (investigadores) como un RDD de tuplas (VertexId, String).

- `1L to 10L` crea un rango de Long de 1 a 10.
- `.map(id => (id, s"Researcher $id"))` transforma cada número en una tupla (id, "Researcher id").

```scala
    val edges: RDD[Edge[Int]] = sc.parallelize(
      (for {
        i <- 1L to 5L
        j <- i + 1L to 5L
      } yield Edge(i, j, 3)) ++
      // ... más código ...
    )
```

Crea las aristas (colaboraciones) como un RDD de Edge[Int].

- Las comprensiones `for` generan todas las combinaciones de aristas dentro de cada grupo.
- `Edge(i, j, 3)` crea una arista del nodo i al nodo j con peso 3.

```scala
    val graph: Graph[String, Int] = Graph(vertices, edges)
```

Crea el grafo utilizando los vértices y aristas definidos.

```scala
    println("Número de vértices: " + graph.vertices.count())
    println("Número de aristas: " + graph.edges.count())
```

Imprime el número de vértices y aristas en el grafo.

```scala
    graph.triplets.collect().foreach { triplet =>
      println(s"${triplet.srcAttr} colabora con ${triplet.dstAttr} (Peso: ${triplet.attr})")
    }
```

Muestra las conexiones de cada investigador.

- `triplet.srcAttr` es el atributo del vértice de origen.
- `triplet.dstAttr` es el atributo del vértice de destino.
- `triplet.attr` es el atributo de la arista (peso).

```scala
    graph.degrees.collect().sortBy(_._1).foreach { case (id, degree) =>
      println(s"Researcher $id: $degree conexiones")
    }
```

Calcula y muestra el grado de cada vértice.

- `_._1` se refiere al primer elemento de la tupla (id).
- `case (id, degree)` es un patrón de coincidencia que descompone la tupla.

### Ejercicio 2: Ecosistema de Redes Sociales

El código para el Ejercicio 2 sigue una estructura similar al Ejercicio 1, con algunas diferencias clave:

```scala
    val edges: RDD[Edge[Int]] = sc.parallelize(
      // ... código previo ...
      // Conexiones adicionales de los "influencers"
      (for {
        i <- 1L to 5L if i != 2L
      } yield Edge(2L, i, 2)) ++
      // ... más código ...
    )
```

Este fragmento crea conexiones adicionales para los "influencers" en cada comunidad.

```scala
    val influencers = graph.degrees.filter(_._2 > 5).collect()
    influencers.foreach { case (id, degree) =>
      println(s"User $id: $degree conexiones")
    }
```

Identifica a los "influencers" filtrando los nodos con grado mayor a 5.

- `_._2 > 5` se refiere al segundo elemento de la tupla (degree) y lo compara con 5.

### Ejercicio 3: Red de Colaboración Científica

Este ejercicio introduce conceptos más avanzados de GraphX:

```scala
    val cc = graph.connectedComponents()
    val communityGroups = cc.vertices.map { case (id, label) =>
      (label, id)
    }.groupByKey().collect()
```

Identifica comunidades usando el algoritmo de Componentes Conectados.

- `groupByKey()` agrupa los vértices por su etiqueta de componente.

```scala
    val pageRanks = graph.pageRank(0.001).vertices.collect().sortBy(-_._2)
```

Calcula la centralidad de los investigadores usando PageRank.

- `-_._2` ordena los resultados en orden descendente basado en la puntuación de PageRank.

```scala
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
```

Implementa el algoritmo de camino más corto usando Pregel.

- `mapVertices` inicializa las distancias.
- `pregel` ejecuta el algoritmo de manera distribuida.

```scala
    val density = numEdges.toDouble / maxPossibleEdges
```

Calcula la densidad del grafo dividiendo el número de aristas existentes por el número máximo posible de aristas.
