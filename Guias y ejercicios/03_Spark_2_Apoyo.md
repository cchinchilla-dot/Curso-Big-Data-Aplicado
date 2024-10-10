# Guía avanzada de Apache Spark (Materiales de apoyo)

Scala cheatsheets: <https://docs.scala-lang.org/cheatsheets/index.html>

## Funciones clave

### Operaciones de RDD

1. `parallelize()`: Crea un RDD a partir de una colección local.
2. `map()`: Aplica una función a cada elemento del RDD.
3. `flatMap()`: Similar a map, pero cada entrada puede generar 0 o más salidas.
4. `filter()`: Selecciona elementos que cumplen una condición.
5. `reduce()`: Agrega los elementos del RDD usando una función asociativa.
6. `collect()`: Retorna todos los elementos del RDD al driver.
7. `count()`: Retorna el número de elementos en el RDD.
8. `take(n)`: Retorna los primeros n elementos del RDD.

### Operaciones de Pares Clave-Valor

9. `reduceByKey()`: Combina valores para cada clave usando una función asociativa.
10. `groupByKey()`: Agrupa valores para cada clave.
11. `sortByKey()`: Ordena un RDD de pares (K,V) basado en la clave.
12. `join()`: Realiza un join interno entre dos RDDs de pares.
13. `cogroup()`: Agrupa datos de varios RDDs por clave.

### Transformaciones de DataFrames

14. `select()`: Selecciona una lista de columnas.
15. `filter()` / `where()`: Filtra filas usando la condición dada.
16. `groupBy()`: Agrupa el DataFrame usando las columnas especificadas.
17. `agg()`: Realiza agregaciones después de un groupBy.
18. `orderBy()` / `sort()`: Ordena el DataFrame por las columnas especificadas.
19. `drop()`: Elimina columnas especificadas.
20. `withColumn()`: Añade una nueva columna o reemplaza una existente.
21. `withColumnRenamed()`: Renombra una columna existente.
22. `join()`: Realiza un join entre DataFrames.

### Funciones de ventana

23. `Window.partitionBy()`: Define una partición para operaciones de ventana.
24. `Window.orderBy()`: Ordena dentro de una partición para operaciones de ventana.
25. `Window.rowsBetween()`: Define el frame de la ventana basado en filas.
26. `lead()`: Accede a un valor en una fila subsiguiente.
27. `lag()`: Accede a un valor en una fila anterior.

### Funciones de agregación

28. `sum()`: Calcula la suma de los valores en un grupo.
29. `avg()`: Calcula el promedio de los valores en un grupo.
30. `max()`: Encuentra el valor máximo en un grupo.
31. `min()`: Encuentra el valor mínimo en un grupo.
32. `count()`: Cuenta el número de filas en un grupo.
33. `countDistinct()`: Cuenta el número de valores distintos en un grupo.

### Funciones de fecha y tiempo

34. `to_date()`: Convierte una columna a tipo fecha.
35. `date_format()`: Formatea una fecha según un patrón específico.
36. `datediff()`: Calcula la diferencia entre dos fechas.
37. `months_between()`: Calcula el número de meses entre dos fechas.
38. `unix_timestamp()`: Convierte una fecha/hora a timestamp Unix.

### Funciones de cadena

39. `concat()`: Concatena dos o más cadenas.
40. `substring()`: Extrae una subcadena.
41. `lower()`: Convierte una cadena a minúsculas.
42. `upper()`: Convierte una cadena a mayúsculas.
43. `trim()`: Elimina espacios en blanco al inicio y final de una cadena.
44. `regexp_extract()`: Extrae una parte de la cadena que coincide con un patrón regex.

### UDFs (User-Defined Functions)

45. `udf()`: Crea una función definida por el usuario.
46. `spark.udf.register()`: Registra una UDF para su uso en consultas SQL.

### Operaciones de E/S

47. `read.parquet()`: Lee archivos en formato Parquet.
48. `write.parquet()`: Escribe DataFrames en formato Parquet.
49. `read.csv()`: Lee archivos CSV.
50. `write.csv()`: Escribe DataFrames en formato CSV.
51. `read.json()`: Lee archivos JSON.
52. `write.json()`: Escribe DataFrames en formato JSON.

### Operaciones de Streaming

53. `readStream`: Inicia la definición de un streaming query.
54. `writeStream`: Especifica el destino de salida de un streaming query.
55. `trigger()`: Define cuándo se debe activar el procesamiento de datos en streaming.
56. `outputMode()`: Especifica cómo se deben escribir los resultados del streaming.

### Optimización y rendimiento

57. `cache()`: Almacena en caché el DataFrame.
58. `persist()`: Almacena en caché el DataFrame con un nivel de almacenamiento específico.
59. `unpersist()`: Elimina el DataFrame de la caché.
60. `explain()`: Muestra el plan de ejecución de una consulta.
61. `coalesce()`: Reduce el número de particiones.
62. `repartition()`: Aumenta o disminuye el número de particiones.

### Otras funciones

63. `broadcast()`: Crea una variable de transmisión para optimizar joins.
64. `explode()`: Crea una nueva fila para cada elemento en una colección.
65. `lit()`: Crea una columna con un valor literal.
66. `when()`: Añade una cláusula condicional a una expresión.
67. `over()`: Especifica una ventana para una función de ventana.
68. `partitionBy()`: Especifica las columnas de partición al escribir datos.

## Explicaciones detalladas de los ejercicios de prueba

### 1. parallelize()

```scala
val rdd = spark.sparkContext.parallelize(1 to 1000000, 100)
```

Esta función crea un RDD (Resilient Distributed Dataset) a partir de una colección local en el programa driver.

- **Parámetros:**
  1. Una colección de datos (en este caso, un rango de 1 a 1,000,000).
  2. El número de particiones (100 en este ejemplo).

- **Funcionamiento:**
  - Divide la colección en el número especificado de particiones, distribuyéndolas a través del clúster.
  - Cada partición contendrá aproximadamente el mismo número de elementos.

- **Modificación y uso:**
  - Ajusta el número de particiones para optimizar el rendimiento según tu clúster y carga de trabajo.
  - Utiliza colecciones más pequeñas para pruebas y depuración.
  - Considera usar `textFile()` o `sparkContext.sequenceFile()` para datos más grandes almacenados en archivos.

### 2. map()

```scala
.map(x => (x % 10, x))
```

Aplica una función a cada elemento del RDD y devuelve un nuevo RDD con los resultados.

- **Parámetros:**
  - Una función que se aplica a cada elemento del RDD.

- **Funcionamiento:**
  - Transforma cada elemento individualmente sin cambiar la estructura del RDD.
  - En este ejemplo, crea pares clave-valor donde la clave es el residuo de la división por 10.

- **Modificación y uso:**
  - Cambia la función para aplicar diferentes transformaciones a los datos.
  - Utiliza para limpieza de datos, extracción de características, o cualquier transformación elemento por elemento.
  - Para operaciones más complejas, considera usar `flatMap()` o `mapPartitions()`.

### 3. repartitionAndSortWithinPartitions()

```scala
.repartitionAndSortWithinPartitions(new HashPartitioner(10))
```

Redistribuye los datos según un particionador y ordena los datos dentro de cada partición.

- **Parámetros:**
  - Un objeto Partitioner (en este caso, HashPartitioner con 10 particiones).

- **Funcionamiento:**
  - Redistribuye los datos en 10 particiones basadas en el hash de las claves.
  - Ordena los datos dentro de cada partición según la clave.

- **Modificación y uso:**
  - Ajusta el número de particiones según tus necesidades de procesamiento.
  - Utiliza diferentes tipos de Partitioner (e.g., RangePartitioner) para distribuir los datos de manera diferente.
  - Útil antes de operaciones que se benefician de datos ordenados o bien distribuidos.

### 4. mapValues()

```scala
.mapValues(x => x * x)
```

Aplica una función solo a los valores de un RDD de pares clave-valor, manteniendo las claves intactas.

- **Parámetros:**
  - Una función que se aplica a cada valor del RDD.

- **Funcionamiento:**
  - Transforma solo los valores, dejando las claves sin cambios.
  - En este ejemplo, eleva al cuadrado cada valor.

- **Modificación y uso:**
  - Cambia la función para aplicar diferentes transformaciones a los valores.
  - Útil para operaciones que solo necesitan modificar los valores sin afectar la distribución de las claves.
  - Considera usar `map()` si necesitas modificar tanto las claves como los valores.

### 5. reduceByKey()

```scala
.reduceByKey(_ + _)
```

Combina los valores para cada clave usando una función asociativa.

- **Parámetros:**
  - Una función que combina dos valores en uno.

- **Funcionamiento:**
  - Agrupa los datos por clave y aplica la función de reducción a los valores de cada grupo.
  - En este caso, suma todos los valores asociados a cada clave.

- **Modificación y uso:**
  - Cambia la función de reducción para realizar diferentes tipos de agregaciones (e.g., multiplicación, máximo, mínimo).
  - Útil para cálculos de resumen como totales, promedios, o conteos por categoría.
  - Para operaciones más complejas, considera `aggregateByKey()` o `combineByKey()`.

### 6. withColumn()

```scala
.withColumn("trip_duration_minutes", 
  (unix_timestamp($"tpep_dropoff_datetime") - unix_timestamp($"tpep_pickup_datetime")) / 60)
```

Añade una nueva columna a un DataFrame o reemplaza una existente.

- **Parámetros:**
  1. Nombre de la nueva columna (o de la columna a reemplazar).
  2. Expresión que define el contenido de la columna.

- **Funcionamiento:**
  - Crea una nueva columna basada en la expresión proporcionada.
  - En este ejemplo, calcula la duración del viaje en minutos.

- **Modificación y uso:**
  - Utiliza diferentes funciones de Spark SQL para crear columnas más complejas.
  - Combina con `when()` y `otherwise()` para lógica condicional.
  - Útil para feature engineering, cálculos derivados, o transformación de datos.

### 7. window()

```scala
Window.partitionBy($"producto")
  .orderBy($"fecha")
  .rowsBetween(-6, 0)
```

Define una ventana para operaciones de ventana en Spark SQL.

- **Componentes:**
  1. `partitionBy()`: Define cómo se agrupan los datos.
  2. `orderBy()`: Establece el orden dentro de cada partición.
  3. `rowsBetween()`: Especifica el rango de filas para la ventana.

- **Funcionamiento:**
  - Crea una ventana deslizante que incluye las 7 filas más recientes (incluyendo la actual) para cada producto, ordenadas por fecha.

- **Modificación y uso:**
  - Ajusta la partición para diferentes niveles de agregación.
  - Cambia el orden para diferentes secuencias temporales o lógicas.
  - Modifica el rango de filas para ventanas de diferentes tamaños.
  - Útil para cálculos de promedios móviles, rankings, o acumulaciones dentro de grupos.

### 8. udf()

```scala
val categorizeSalary = udf((salary: Int) => {
  if (salary < 3500) "Low"
  else if (salary < 4500) "Medium"
  else "High"
})
```

Crea una función definida por el usuario (UDF) para usar en transformaciones de DataFrame.

- **Parámetros:**
  - Una función Scala que define la lógica de la UDF.

- **Funcionamiento:**
  - Convierte una función Scala en una UDF que puede ser utilizada en operaciones de DataFrame.
  - En este ejemplo, categoriza salarios en "Low", "Medium", o "High".

- **Modificación y uso:**
  - Define funciones más complejas para transformaciones personalizadas.
  - Registra la UDF con `spark.udf.register()` para usarla en consultas SQL.
  - Útil para lógica de negocio específica que no está disponible en las funciones integradas de Spark.

### 9. explode()

```scala
explode($"fruits").as("fruit")
```

Crea una nueva fila para cada elemento en un array o map.

- **Parámetros:**
  - Una columna de tipo array o map.

- **Funcionamiento:**
  - Descompone un array o map en múltiples filas.
  - Cada elemento del array o cada par clave-valor del map se convierte en una nueva fila.

- **Modificación y uso:**
  - Útil para "desenrollar" estructuras de datos complejas en formato tabular.
  - Combina con `select()` o `withColumn()` para reestructurar tus datos.
  - Para operaciones más complejas, considera `posexplode()` (que incluye índices) o `explode_outer()` (que preserva filas con arrays vacíos).

### 10. createOrReplaceTempView()

```scala
dfProcessed.createOrReplaceTempView("taxi_trips")
```

Crea o reemplaza una vista temporal de un DataFrame para usar en consultas SQL.

- **Parámetros:**
  - Nombre de la vista temporal.

- **Funcionamiento:**
  - Registra el DataFrame como una tabla temporal en el catálogo de Spark SQL.
  - Permite usar el DataFrame en consultas SQL posteriores.

- **Modificación y uso:**
  - Usa nombres descriptivos para tus vistas temporales.
  - Combina múltiples vistas temporales en consultas SQL complejas.
  - Para vistas que persisten entre sesiones, considera `createOrReplaceGlobalTempView()`.

## Explicación de ejemplos

### Ejemplo de transformaciones

```scala
val dfProcesado = df
  .withColumn("fecha", to_date($"timestamp"))
```

Esta línea crea una nueva columna llamada "fecha" utilizando la función `to_date()`. Esta función convierte la columna "timestamp" existente a un formato de fecha, eliminando la información de hora. Esto es útil para agrupar datos por día.

```scala
  .withColumn("hora", hour($"timestamp"))
```

Aquí se crea otra columna nueva llamada "hora" usando la función `hour()`. Esta extrae solo la parte de la hora del "timestamp" original. Esto permite analizar patrones por hora del día.

```scala
  .withColumn("es_fin_semana", when(dayofweek($"fecha").isin(1, 7), true).otherwise(false))
```

Esta línea crea una columna booleana "es_fin_semana". Utiliza la función `dayofweek()` para obtener el día de la semana de la columna "fecha" (donde 1 es domingo y 7 es sábado). Luego, usa `when().otherwise()` para asignar 'true' si es sábado o domingo, y 'false' para los demás días.

```scala
  .groupBy($"fecha", $"es_fin_semana")
```

Agrupa los datos por las columnas "fecha" y "es_fin_semana". Esto prepara el DataFrame para realizar agregaciones en estos grupos.

```scala
  .agg(
    countDistinct($"usuario_id").as("usuarios_unicos"),
    sum(when($"tipo_evento" === "compra", $"monto").otherwise(0)).as("total_ventas")
  )
```

Realiza dos agregaciones:

1. `countDistinct($"usuario_id").as("usuarios_unicos")`: Cuenta el número de usuarios únicos en cada grupo.
2. `sum(when($"tipo_evento" === "compra", $"monto").otherwise(0)).as("total_ventas")`: Suma los montos de los eventos de tipo "compra", ignorando otros tipos de eventos (asignándoles 0).

```scala
  .withColumn("promedio_movil_ventas", 
    avg($"total_ventas").over(Window.partitionBy($"es_fin_semana")
      .orderBy($"fecha")
      .rowsBetween(-6, 0)))
```

Esta parte calcula un promedio móvil de 7 días para las ventas totales:

- `Window.partitionBy($"es_fin_semana")`: Crea una ventana separada para días de semana y fines de semana.
- `.orderBy($"fecha")`: Ordena los datos por fecha dentro de cada partición.
- `.rowsBetween(-6, 0)`: Define la ventana como las 7 filas más recientes (incluyendo la actual).
- `avg($"total_ventas").over(...)`: Calcula el promedio de ventas totales dentro de esta ventana deslizante.

### Ejemplo de Spark Streaming

1. Creación del DataFrame de streaming:

```scala
val streamingDF = spark.readStream
  .format("rate")
  .option("rowsPerSecond", "10")
  .load()
  .selectExpr("CAST(timestamp AS STRING)", "value AS id")
```

- `spark.readStream`: Inicia la definición de un streaming query.
- `.format("rate")`: Usa la fuente "rate", que genera datos de prueba a una velocidad constante.
- `.option("rowsPerSecond", "10")`: Configura la generación de 10 filas por segundo.
- `.load()`: Carga el stream de datos.
- `.selectExpr(...)`: Selecciona y transforma columnas:
  - Convierte "timestamp" a String.
  - Renombra "value" como "id".

Modificación: Puedes ajustar "rowsPerSecond" para simular diferentes cargas de datos.

2. Simulación de eventos:

```scala
val simulatedEventsDF = streamingDF
  .withColumn("tipo_evento", expr("CASE WHEN rand() < 0.5 THEN 'click' ELSE 'view' END"))
  .withColumn("timestamp", $"timestamp".cast("timestamp"))
```

- Añade una columna "tipo_evento" que simula eventos 'click' o 'view' aleatoriamente.
- Convierte la columna "timestamp" de vuelta al tipo timestamp.

Modificación: Puedes cambiar la lógica para generar más tipos de eventos o ajustar sus probabilidades.

3. Procesamiento del stream:

```scala
val procesadoDF = simulatedEventsDF
  .withWatermark("timestamp", "10 minutes")
  .groupBy(window($"timestamp", "5 minutes"), $"tipo_evento")
  .agg(count("*").as("total_eventos"))
```

- `.withWatermark("timestamp", "10 minutes")`: Establece un watermark de 10 minutos para manejar datos tardíos.
- `.groupBy(window($"timestamp", "5 minutes"), $"tipo_evento")`: Agrupa los datos en ventanas de 5 minutos y por tipo de evento.
- `.agg(count("*").as("total_eventos"))`: Cuenta el número de eventos en cada grupo.

Modificación: Puedes ajustar los intervalos de watermark y ventana, o añadir más agregaciones.

4. Configuración de la salida del stream:

```scala
val query = procesadoDF.writeStream
  .outputMode("update")
  .format("console")
  .trigger(Trigger.ProcessingTime("10 seconds"))
  .start()
```

- `.outputMode("update")`: Configura para mostrar solo resultados nuevos o actualizados.
- `.format("console")`: Dirige la salida a la consola.
- `.trigger(Trigger.ProcessingTime("10 seconds"))`: Procesa los datos cada 10 segundos.
- `.start()`: Inicia el streaming query.

Modificación: Puedes cambiar el modo de salida (e.g., "complete"), el formato de salida (e.g., a un archivo), o el intervalo de trigger.

### Datos completjos

1. Creación del DataFrame con datos complejos:

```scala
val data = Seq(
  (1, Array("apple", "banana"), Map("a" -> 1, "b" -> 2)),
  (2, Array("orange", "grape"), Map("x" -> 3, "y" -> 4))
).toDF("id", "fruits", "scores")
```

- Este código crea un DataFrame con tres columnas: "id", "fruits", y "scores".
- La columna "id" es de tipo entero.
- La columna "fruits" es un array de strings.
- La columna "scores" es un mapa de string a entero.
- `Seq(...)` crea una secuencia de tuplas.
- `.toDF("id", "fruits", "scores")` convierte la secuencia en un DataFrame, asignando nombres a las columnas.

Modificación: Puedes añadir más filas, cambiar los tipos de datos, o incluir más columnas complejas.

2. Operaciones con arrays:

```scala
val withArrayLength = data.withColumn("fruit_count", size($"fruits"))
```

- Esta línea añade una nueva columna llamada "fruit_count" al DataFrame.
- `size($"fruits")` calcula el número de elementos en el array "fruits" para cada fila.
- El resultado es un nuevo DataFrame que incluye la columna original "fruits" y la nueva columna "fruit_count".

Modificación: Podrías usar otras funciones de array como `array_contains()` para verificar la presencia de un elemento específico.

3. Operaciones con mapas:

```scala
val withMapValue = data.withColumn("score_a", $"scores".getItem("a"))
```

- Esta operación añade una nueva columna "score_a" al DataFrame.
- `$"scores".getItem("a")` extrae el valor asociado con la clave "a" del mapa "scores".
- Si la clave "a" no existe en alguna fila, el resultado será null para esa fila.

Modificación: Podrías usar `map_keys()` o `map_values()` para obtener todas las claves o valores del mapa.

4. Explosión (explode) de un array:

```scala
val explodedFruits = data.select($"id", explode($"fruits").as("fruit"))
```

- Esta operación "explota" el array "fruits", creando una nueva fila por cada elemento del array.
- `select($"id", ...)` mantiene la columna "id" en el resultado.
- `explode($"fruits")` crea una nueva fila para cada elemento en el array "fruits".
- `.as("fruit")` nombra la nueva columna resultante como "fruit".

Modificación: Podrías usar `posexplode()` si también necesitas el índice de cada elemento en el array.

5. Visualización de resultados:

```scala
withArrayLength.show()
withMapValue.show()
explodedFruits.show()
```

- Estas líneas muestran los resultados de cada operación en la consola.
- `show()` por defecto muestra las primeras 20 filas del DataFrame.

Modificación: Puedes usar `show(false)` para mostrar las columnas completas sin truncar, o `show(n)` para mostrar n filas.

### Ejemplo completo I

1. Transformaciones iniciales:

```scala
val dfProcessed = dfTaxi
  .withColumn("trip_duration_minutes", 
    (unix_timestamp($"tpep_dropoff_datetime") - unix_timestamp($"tpep_pickup_datetime")) / 60)
  .withColumn("price_per_mile", when($"trip_distance" > 0, $"total_amount" / $"trip_distance").otherwise(0))
  .withColumn("day_of_week", date_format($"tpep_pickup_datetime", "EEEE"))
```

- Esta sección crea un nuevo DataFrame `dfProcessed` con tres nuevas columnas:
  a. "trip_duration_minutes": Calcula la duración del viaje en minutos.
     - Usa `unix_timestamp()` para convertir las fechas a segundos Unix.
     - Resta los tiempos y divide por 60 para obtener minutos.
  b. "price_per_mile": Calcula el precio por milla.
     - Usa `when().otherwise()` para manejar casos donde la distancia es 0.
  c. "day_of_week": Extrae el día de la semana del tiempo de recogida.
     - `date_format()` con "EEEE" da el nombre completo del día.

Modificación: Podrías añadir más cálculos como la hora del día o la temporada.

2. Creación de vista temporal:

```scala
dfProcessed.createOrReplaceTempView("taxi_trips")
```

- Crea una vista temporal SQL llamada "taxi_trips" del DataFrame procesado.
- Permite usar SQL para consultar los datos.

3. Consulta SQL compleja:

```scala
val resultSQL = spark.sql("""
  SELECT 
    day_of_week,
    AVG(trip_duration_minutes) as avg_duration,
    AVG(trip_distance) as avg_distance,
    AVG(total_amount) as avg_amount,
    AVG(price_per_mile) as avg_price_per_mile
  FROM taxi_trips
  WHERE trip_distance > 0 AND trip_duration_minutes BETWEEN 5 AND 120
  GROUP BY day_of_week
  ORDER BY avg_amount DESC
""")
```

- Esta consulta SQL calcula promedios por día de la semana.
- Filtra viajes con distancia > 0 y duración entre 5 y 120 minutos.
- Agrupa por día de la semana y ordena por monto promedio descendente.

Modificación: Podrías añadir más métricas o cambiar los criterios de filtrado.

4. Análisis usando API de DataFrame:

```scala
val topPickupLocations = dfProcessed
  .groupBy("PULocationID")
  .agg(
    count("*").as("total_pickups"),
    avg("total_amount").as("avg_fare")
  )
  .orderBy(desc("total_pickups"))
  .limit(5)
```

- Este análisis encuentra las 5 ubicaciones de recogida más populares.
- Agrupa por ID de ubicación de recogida.
- Calcula el total de recogidas y la tarifa promedio para cada ubicación.
- Ordena por número total de recogidas descendente y limita a 5 resultados.

Modificación: Podrías cambiar el número de ubicaciones top o añadir más métricas.

5. Guardar resultados:

```scala
resultSQL.write.mode("overwrite").parquet("taxi_summary_by_day.parquet")
topPickupLocations.write.mode("overwrite").parquet("top_pickup_locations.parquet")
```

- Guarda los resultados de ambos análisis en archivos Parquet.
- `.mode("overwrite")` sobrescribe los archivos si ya existen.

Modificación: Podrías cambiar el formato de salida (por ejemplo, a CSV) o el modo de escritura.

### Ejemplo completo II

1. Lectura del archivo CSV:

```scala
val dfRoutes = spark.read
  .option("header", "false")
  .option("inferSchema", "true")
  .csv(localFilePath)
  .toDF("airline", "airline_id", "source_airport", "source_airport_id", 
        "destination_airport", "destination_airport_id", "codeshare", 
        "stops", "equipment")
```

- Lee un archivo CSV en un DataFrame.
- `option("header", "false")`: Indica que el archivo no tiene encabezado.
- `option("inferSchema", "true")`: Spark infiere automáticamente los tipos de datos.
- `.toDF(...)`: Asigna nombres a las columnas.

Modificación: Podrías especificar el esquema manualmente para un control más preciso.

2. Visualización del esquema y datos de muestra:

```scala
dfRoutes.printSchema()
dfRoutes.show(5)
```

- Imprime la estructura del DataFrame y muestra las primeras 5 filas.

3. Persistencia del DataFrame:

```scala
dfRoutes.persist(StorageLevel.MEMORY_AND_DISK)
```

- Almacena el DataFrame en memoria y disco para un acceso más rápido en operaciones subsiguientes.

4. Definición de una UDF (User-Defined Function):

```scala
val categorizeRoute = udf((stops: Int) => {
  if (stops == 0) "Direct"
  else if (stops == 1) "One Stop"
  else "Multiple Stops"
})
```

- Crea una función personalizada para categorizar rutas basadas en el número de paradas.

5. Aplicación de transformaciones:

```scala
val dfProcessed = dfRoutes
  .withColumn("route_type", categorizeRoute($"stops"))
  .withColumn("is_international", 
              when($"source_airport".substr(1, 2) =!= $"destination_airport".substr(1, 2), true)
              .otherwise(false))
```

- Añade dos nuevas columnas:
  - "route_type": Categoriza las rutas usando la UDF.
  - "is_international": Determina si la ruta es internacional comparando los dos primeros caracteres de los códigos de aeropuerto.

6. Creación de vista temporal para SQL:

```scala
dfProcessed.createOrReplaceTempView("flight_routes")
```

- Crea una vista temporal SQL del DataFrame procesado.

7. Análisis usando SQL:

```scala
val routeAnalysis = spark.sql("""
  SELECT 
    airline,
    route_type,
    COUNT(*) as route_count,
    SUM(CASE WHEN is_international THEN 1 ELSE 0 END) as international_routes
  FROM flight_routes
  GROUP BY airline, route_type
  ORDER BY route_count DESC
""")
```

- Realiza un análisis de rutas por aerolínea y tipo de ruta.
- Cuenta el número total de rutas y rutas internacionales.

8. Análisis avanzado con funciones de ventana:

```scala
val windowSpec = Window.partitionBy("airline").orderBy($"route_count".desc)

val topRoutesByAirline = routeAnalysis
  .withColumn("rank", dense_rank().over(windowSpec))
  .filter($"rank" <= 3)
  .orderBy($"airline", $"rank")
```

- Utiliza una función de ventana para rankear los tipos de rutas por aerolínea.
- Selecciona los 3 tipos de rutas más comunes para cada aerolínea.

9. Análisis de conectividad de aeropuertos:

```scala
val airportConnectivity = dfProcessed
  .groupBy("source_airport")
  .agg(
    countDistinct("destination_airport").as("destinations"),
    sum(when($"is_international", 1).otherwise(0)).as("international_connections")
  )
  .orderBy($"destinations".desc)
```

- Analiza la conectividad de los aeropuertos de origen.
- Cuenta destinos únicos y conexiones internacionales para cada aeropuerto.

10. Guardado de resultados:

```scala
routeAnalysis.write.mode("overwrite").parquet("route_analysis.parquet")
topRoutesByAirline.write.mode("overwrite").parquet("top_routes_by_airline.parquet")
airportConnectivity.write.mode("overwrite").parquet("airport_connectivity.parquet")
```

- Guarda los resultados de los diferentes análisis en archivos Parquet.

## Aspectos importantes para los ejercicios 1-5 de Spark

### Ejercicio 1: Análisis de ventas con funciones de ventana

Pistas:

1. Crea un DataFrame con columnas: fecha, categoria, ventas.
2. Convierte la columna 'fecha' a tipo DateType usando to_date().
3. Define una ventana con Window.partitionBy("categoria").orderBy("fecha").
4. Para ventas acumuladas, usa sum("ventas").over(windowSpec).
5. Para el promedio móvil de 3 días, modifica la ventana con .rowsBetween(-1, 1).
6. Usa avg("ventas").over(windowSpecMovil) para el promedio móvil.
7. Ordena el resultado final por categoria y fecha.

Funciones clave:

- to_date(), Window.partitionBy(), orderBy(), sum().over(), avg().over(), rowsBetween()

### Ejercicio 2: Análisis de logs con UDFs

Pistas:

1. Crea un DataFrame con una columna 'log' conteniendo strings de log.

// Simular logs
val logs = Seq(
    "2023-05-01 10:00:15 INFO [User:123] Login successful",
    "2023-05-01 10:05:20 ERROR [User:456] Failed login attempt",
    "2023-05-01 10:10:30 WARN [System] High CPU usage detected"
).toDF("log")

2. Define una UDF para extraer el nivel de log (INFO, ERROR, WARN).
3. Usa regexp_extract() para sacar timestamp y mensaje del log.
4. Aplica withColumn() para añadir columnas: timestamp, level, message.
5. Convierte timestamp a DateType con to_timestamp().
6. Agrupa por 'level' y usa count() para obtener frecuencias.

Funciones clave:

- udf(), regexp_extract(), withColumn(), to_timestamp(), groupBy(), count()

### Ejercicio 3: Streaming de datos de sensores

Pistas:

1. Usa readStream.format("rate") para generar datos simulados.
2. Añade columnas 'sensorId' y 'temperature' con valores aleatorios.
3. Define una ventana de 5 minutos con window($"timestamp", "5 minutes").
4. Agrupa por sensorId y la ventana de tiempo.
5. Calcula avg("temperature") y max("temperature") en cada grupo.
6. Configura writeStream con outputMode("complete") y format("console").
7. Usa trigger() para actualizar cada minuto.

Funciones clave:

- readStream.format("rate"), withColumn(), window(), groupBy(), avg(), max(), writeStream

### Ejercicio 4: ETL con datos Parquet

Pistas:

1. Lee el archivo Parquet con spark.read.parquet().
2. Convierte fechas con to_date() y extrae el mes con month().
3. Calcula la duración del viaje en minutos.
4. Usa withColumn() para añadir estas nuevas columnas.
5. Al escribir, usa partitionBy("trip_date") para particionar por fecha.
6. Para la consulta mensual, agrupa por mes y usa sum(), avg(), y count().
7. Llama a explain() para ver el plan de ejecución de la consulta.

Funciones clave:

- read.parquet(), to_date(), month(), withColumn(), partitionBy(), groupBy(), sum(), avg(), count(), explain()

### Ejercicio 5: Análisis de datos COVID-19

Pistas:

1. Lee el CSV con opciones "header" y "inferSchema".
2. Usa select() y explode() para transformar de formato ancho a largo.
3. Convierte fechas de string a DateType con to_date().
4. Calcula nuevos casos con lag() y una ventana particionada por país.
5. Identifica top 10 países con groupBy().agg(max()) y orderBy().desc.
6. Para la media móvil, usa avg().over() con una ventana de 7 días.
7. Guarda resultados con write.partitionBy("country").parquet().

Funciones clave:

- read.csv(), select(), explode(), to_date(), lag(), Window.partitionBy(), groupBy(), agg(), orderBy(), avg().over(), write.partitionBy()

 