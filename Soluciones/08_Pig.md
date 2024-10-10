# Soluciones a los ejercicios de Apache Pig

## 1. Ejercicios prácticos

### Ejercicio 1

Diseña e implementa un proceso ETL en Pig para cargar datos de ventas desde un archivo CSV, realizar algunas transformaciones (por ejemplo, convertir fechas, calcular totales) y luego almacenar los resultados en un formato optimizado como ORC o Parquet.

### Ejercicio 2

Crea una UDF en Java para realizar una operación personalizada (por ejemplo, un cálculo complejo o una limpieza de datos específica). Utiliza esta UDF en un script de Pig para procesar un conjunto de datos.

### Ejercicio 3

Desarrolla un script de Pig para analizar un conjunto de datos de redes sociales. El script debe cargar los datos, realizar algunas agregaciones (por ejemplo, contar posts por usuario, calcular promedios de engagement) y producir un resumen de los usuarios más activos e influyentes.

## 2. Soluciones

### Ejercicio 1

```sql
-- Cargar datos de ventas desde CSV
ventas = LOAD '/path/to/ventas.csv' USING PigStorage(',') AS (
    id_venta:int,
    fecha:chararray,
    id_producto:int,
    id_cliente:int,
    cantidad:int,
    monto:float
);

-- Transformar datos
ventas_transformadas = FOREACH ventas GENERATE
    id_venta,
    ToDate(fecha, 'yyyy-MM-dd') AS fecha,
    id_producto,
    id_cliente,
    cantidad,
    monto,
    cantidad * monto AS total,
    GetYear(ToDate(fecha, 'yyyy-MM-dd')) AS anio,
    GetMonth(ToDate(fecha, 'yyyy-MM-dd')) AS mes;

-- Agrupar por año y mes
ventas_agrupadas = GROUP ventas_transformadas BY (anio, mes);

-- Calcular totales mensuales
resumen_mensual = FOREACH ventas_agrupadas GENERATE
    group.anio AS anio,
    group.mes AS mes,
    SUM(ventas_transformadas.total) AS ventas_totales,
    COUNT(DISTINCT ventas_transformadas.id_cliente) AS clientes_unicos;

-- Almacenar resultados en formato ORC
STORE resumen_mensual INTO '/path/to/output' USING OrcStorage();
```

### Ejercicio 2

Primero, creamos una UDF en Java para convertir texto a mayúsculas:

```java
package com.example.pig.udf;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class ToUpperCase extends EvalFunc<String> {
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0)
            return null;
        String str = (String)input.get(0);
        return (str == null) ? null : str.toUpperCase();
    }
}
```

Luego, usamos esta UDF en un script de Pig:

```sql
-- Registrar el JAR que contiene la UDF
REGISTER /path/to/myudfs.jar;

-- Cargar datos
datos = LOAD '/path/to/input.txt' AS (texto:chararray);

-- Usar la UDF para convertir el texto a mayúsculas
datos_mayusculas = FOREACH datos GENERATE com.example.pig.udf.ToUpperCase(texto) AS texto_mayusculas;

-- Almacenar los resultados
STORE datos_mayusculas INTO '/path/to/output';
```

### Ejercicio 3

```sql
-- Cargar datos de redes sociales
posts = LOAD '/path/to/social_data.json' USING JsonLoader('
    user_id:chararray,
    username:chararray,
    followers:int,
    post_content:chararray,
    likes:int,
    shares:int,
    timestamp:chararray
');

-- Contar posts por usuario
posts_por_usuario = GROUP posts BY (user_id, username, followers);
resumen_usuario = FOREACH posts_por_usuario GENERATE
    group.user_id AS user_id,
    group.username AS username,
    group.followers AS followers,
    COUNT(posts) AS num_posts,
    AVG(posts.likes) AS avg_likes,
    AVG(posts.shares) AS avg_shares;

-- Calcular engagement score (ejemplo simple: promedio de likes + shares)
usuarios_engagement = FOREACH resumen_usuario GENERATE
    user_id,
    username,
    followers,
    num_posts,
    avg_likes + avg_shares AS engagement_score;

-- Ordenar usuarios por engagement y número de seguidores
usuarios_top = ORDER usuarios_engagement BY engagement_score DESC, followers DESC;

-- Limitar a los 10 usuarios más influyentes
top_10_usuarios = LIMIT usuarios_top 10;

-- Almacenar resultados
STORE top_10_usuarios INTO '/path/to/top_users_output' USING PigStorage('\t');
```

## 3. Explicación detallada

### Ejercicio 1

Este script implementa un proceso ETL (Extracción, Transformación y Carga) para datos de ventas.

```sql
-- Cargar datos de ventas desde CSV
ventas = LOAD '/path/to/ventas.csv' USING PigStorage(',') AS (
    id_venta:int,
    fecha:chararray,
    id_producto:int,
    id_cliente:int,
    cantidad:int,
    monto:float
);
```

Esta parte carga los datos desde un archivo CSV:

- `LOAD` lee los datos del archivo especificado.
- `PigStorage(',')` indica que los campos están separados por comas.
- `AS (...)` define el esquema de los datos.

```sql
-- Transformar datos
ventas_transformadas = FOREACH ventas GENERATE
    id_venta,
    ToDate(fecha, 'yyyy-MM-dd') AS fecha,
    id_producto,
    id_cliente,
    cantidad,
    monto,
    cantidad * monto AS total,
    GetYear(ToDate(fecha, 'yyyy-MM-dd')) AS anio,
    GetMonth(ToDate(fecha, 'yyyy-MM-dd')) AS mes;
```

Esta parte transforma los datos:

- `FOREACH ... GENERATE` aplica transformaciones a cada tupla.
- `ToDate()` convierte la fecha de string a formato de fecha.
- `GetYear()` y `GetMonth()` extraen el año y mes de la fecha.
- Se calcula un nuevo campo `total` multiplicando cantidad por monto.

```sql
-- Agrupar por año y mes
ventas_agrupadas = GROUP ventas_transformadas BY (anio, mes);

-- Calcular totales mensuales
resumen_mensual = FOREACH ventas_agrupadas GENERATE
    group.anio AS anio,
    group.mes AS mes,
    SUM(ventas_transformadas.total) AS ventas_totales,
    COUNT(DISTINCT ventas_transformadas.id_cliente) AS clientes_unicos;
```

Estas partes agrupan los datos y calculan resúmenes:

- `GROUP ... BY` agrupa los datos por año y mes.
- El siguiente `FOREACH` calcula las ventas totales y el número de clientes únicos para cada grupo.

```sql
-- Almacenar resultados en formato ORC
STORE resumen_mensual INTO '/path/to/output' USING OrcStorage();
```

Esta línea almacena los resultados en formato ORC, que es un formato de archivo columnar eficiente para Hadoop.

### Ejercicio 2

Este ejercicio consta de dos partes: una UDF en Java y un script de Pig que la utiliza.

#### UDF en Java

```java
package com.example.pig.udf;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class ToUpperCase extends EvalFunc<String> {
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0)
            return null;
        String str = (String)input.get(0);
        return (str == null) ? null : str.toUpperCase();
    }
}
```

Esta UDF:

- Extiende `EvalFunc<String>`, indicando que la función toma una entrada y devuelve un String.
- El método `exec()` toma una `Tuple` como entrada, que es la forma en que Pig pasa datos a las UDFs.
- Convierte el input a mayúsculas usando `toUpperCase()`.

#### Script de Pig

```sql
-- Registrar el JAR que contiene la UDF
REGISTER /path/to/myudfs.jar;

-- Cargar datos
datos = LOAD '/path/to/input.txt' AS (texto:chararray);

-- Usar la UDF para convertir el texto a mayúsculas
datos_mayusculas = FOREACH datos GENERATE com.example.pig.udf.ToUpperCase(texto) AS texto_mayusculas;

-- Almacenar los resultados
STORE datos_mayusculas INTO '/path/to/output';
```

Este script:

- Registra el JAR que contiene la UDF.
- Carga datos de un archivo de texto.
- Aplica la UDF `ToUpperCase` a cada línea de texto.
- Almacena los resultados en un archivo de salida.

### Ejercicio 3

```sql
-- Cargar datos de redes sociales
posts = LOAD '/path/to/social_data.json' USING JsonLoader('
    user_id:chararray,
    username:chararray,
    followers:int,
    post_content:chararray,
    likes:int,
    shares:int,
    timestamp:chararray
');
```

Esta parte carga datos de un archivo JSON, especificando el esquema de los datos.

```sql
-- Contar posts por usuario
posts_por_usuario = GROUP posts BY (user_id, username, followers);
resumen_usuario = FOREACH posts_por_usuario GENERATE
    group.user_id AS user_id,
    group.username AS username,
    group.followers AS followers,
    COUNT(posts) AS num_posts,
    AVG(posts.likes) AS avg_likes,
    AVG(posts.shares) AS avg_shares;
```

Aquí se agrupan los posts por usuario y se calculan estadísticas como el número de posts, promedio de likes y shares.

```sql
-- Calcular engagement score (ejemplo simple: promedio de likes + shares)
usuarios_engagement = FOREACH resumen_usuario GENERATE
    user_id,
    username,
    followers,
    num_posts,
    avg_likes + avg_shares AS engagement_score;

-- Ordenar usuarios por engagement y número de seguidores
usuarios_top = ORDER usuarios_engagement BY engagement_score DESC, followers DESC;

-- Limitar a los 10 usuarios más influyentes
top_10_usuarios = LIMIT usuarios_top 10;
```

Estas partes calculan un score de engagement simple, ordenan los usuarios por este score y por número de seguidores, y seleccionan los 10 usuarios más influyentes.

```sql
-- Almacenar resultados
STORE top_10_usuarios INTO '/path/to/top_users_output' USING PigStorage('\t');
```

Finalmente, los resultados se almacenan en un archivo, usando tabulaciones como separador.
