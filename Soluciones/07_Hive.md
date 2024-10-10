# Guía avanzada de Apache Hive

## 1. Ejercicios prácticos

### Ejercicio 1

Diseña e implementa un proceso ETL en Hive para cargar datos de ventas desde un archivo CSV a una tabla optimizada. La tabla de destino debe estar particionada por año y mes, y agrupada (bucketed) por ID de producto. Luego, realiza un análisis para obtener las ventas totales y el número de clientes únicos por mes.

### Ejercicio 2

Crea una User-Defined Function (UDF) en Java para extraer el nivel de log (ERROR, WARN, INFO) de mensajes de log de una aplicación. Utiliza esta UDF en Hive para analizar una tabla de logs y generar un resumen de la frecuencia de cada nivel de log.

### Ejercicio 3

Diseña un esquema en Hive para almacenar y analizar datos de posts de redes sociales en formato JSON, incluyendo información del usuario, contenido del post, etiquetas y métricas de engagement. Escribe consultas para identificar las etiquetas más populares y los usuarios más influyentes basándose en sus seguidores y likes promedio por post.

## 2. Soluciones

### Ejercicio 1

```sql
-- Crear tabla externa para los datos de origen
CREATE EXTERNAL TABLE ventas_origen (
    id_venta INT,
    fecha STRING,
    id_producto INT,
    id_cliente INT,
    cantidad INT,
    monto DECIMAL(10,2)
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
STORED AS TEXTFILE
LOCATION '/path/to/csv/files';

-- Crear tabla optimizada de destino
CREATE TABLE ventas_destino (
    id_venta INT,
    fecha DATE,
    id_producto INT,
    id_cliente INT,
    cantidad INT,
    monto DECIMAL(10,2)
)
PARTITIONED BY (anio INT, mes INT)
CLUSTERED BY (id_producto) INTO 32 BUCKETS
STORED AS ORC;

-- Cargar datos en la tabla de destino
SET hive.exec.dynamic.partition.mode=nonstrict;

INSERT OVERWRITE TABLE ventas_destino
PARTITION (anio, mes)
SELECT 
    id_venta,
    TO_DATE(fecha) AS fecha,
    id_producto,
    id_cliente,
    cantidad,
    monto,
    YEAR(TO_DATE(fecha)) AS anio,
    MONTH(TO_DATE(fecha)) AS mes
FROM ventas_origen;

-- Análisis: Ventas totales y número de clientes únicos por mes
SELECT 
    anio,
    mes,
    SUM(monto) AS ventas_totales,
    COUNT(DISTINCT id_cliente) AS clientes_unicos
FROM ventas_destino
GROUP BY anio, mes
ORDER BY anio, mes;
```

### Ejercicio 2

```java
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

public class ExtractLogLevel extends UDF {
    public Text evaluate(Text s) {
        if (s == null) return null;
        String input = s.toString().toUpperCase();
        if (input.contains("ERROR")) return new Text("ERROR");
        if (input.contains("WARN")) return new Text("WARN");
        if (input.contains("INFO")) return new Text("INFO");
        return new Text("UNKNOWN");
    }
}
```

```sql
-- Asumiendo que ya has añadido el JAR con la UDF a Hive
CREATE TEMPORARY FUNCTION extract_log_level AS 'com.example.ExtractLogLevel';

-- Crear tabla de logs
CREATE TABLE logs (
    timestamp STRING,
    message STRING
);

-- Insertar algunos datos de ejemplo (en una situación real, cargarías tus logs reales)
INSERT INTO logs VALUES 
    ('2023-06-01 10:00:00', 'INFO: Application started'),
    ('2023-06-01 10:05:00', 'WARN: Low memory detected'),
    ('2023-06-01 10:10:00', 'ERROR: NullPointerException in module X');

-- Analizar frecuencia de niveles de log
SELECT 
    extract_log_level(message) AS log_level,
    COUNT(*) AS frequency
FROM logs
GROUP BY extract_log_level(message)
ORDER BY frequency DESC;
```

### Ejercicio 3

```sql
-- Crear tabla para almacenar posts de redes sociales
CREATE TABLE social_posts (
    post_id STRING,
    user_id STRING,
    username STRING,
    followers INT,
    post_content STRING,
    tags ARRAY<STRING>,
    likes INT,
    shares INT,
    timestamp TIMESTAMP
)
STORED AS ORC;

-- Insertar algunos datos de ejemplo (en una situación real, cargarías tus datos JSON reales)
INSERT INTO social_posts
SELECT
    'post1', 'user1', 'johndoe', 1000, 'Check out this awesome view! #travel #nature',
    array('travel', 'nature'), 50, 10, '2023-06-01 10:00:00'
UNION ALL
SELECT
    'post2', 'user2', 'janedoe', 5000, 'New recipe alert! #cooking #healthy',
    array('cooking', 'healthy'), 100, 20, '2023-06-01 11:00:00';

-- Identificar las etiquetas más populares
SELECT 
    tag,
    COUNT(*) AS usage_count
FROM social_posts
LATERAL VIEW explode(tags) tag_table AS tag
GROUP BY tag
ORDER BY usage_count DESC
LIMIT 10;

-- Identificar los usuarios más influyentes basados en seguidores y likes promedio
SELECT 
    user_id,
    username,
    followers,
    AVG(likes) AS avg_likes,
    COUNT(*) AS post_count
FROM social_posts
GROUP BY user_id, username, followers
ORDER BY followers DESC, avg_likes DESC
LIMIT 10;
```

## Explicación detallada

### Ejercicio 1: Proceso ETL para datos de ventas

Este script implementa un proceso ETL (Extracción, Transformación y Carga) para datos de ventas.

```sql
-- Crear tabla externa para los datos de origen
CREATE EXTERNAL TABLE ventas_origen (
    id_venta INT,
    fecha STRING,
    id_producto INT,
    id_cliente INT,
    cantidad INT,
    monto DECIMAL(10,2)
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
STORED AS TEXTFILE
LOCATION '/path/to/csv/files';
```

Esta parte crea una tabla externa que apunta a los archivos CSV de origen.

- `EXTERNAL` significa que Hive no gestiona los datos, solo los metadatos.
- `ROW FORMAT DELIMITED FIELDS TERMINATED BY ','` especifica que los campos están separados por comas.
- `LOCATION '/path/to/csv/files'` indica dónde están los archivos CSV.

```sql
-- Crear tabla optimizada de destino
CREATE TABLE ventas_destino (
    id_venta INT,
    fecha DATE,
    id_producto INT,
    id_cliente INT,
    cantidad INT,
    monto DECIMAL(10,2)
)
PARTITIONED BY (anio INT, mes INT)
CLUSTERED BY (id_producto) INTO 32 BUCKETS
STORED AS ORC;
```

Esta parte crea la tabla de destino optimizada:

- `PARTITIONED BY (anio INT, mes INT)` divide los datos en particiones por año y mes.
- `CLUSTERED BY (id_producto) INTO 32 BUCKETS` agrupa los datos en 32 buckets basados en el id_producto.
- `STORED AS ORC` utiliza el formato de archivo ORC para mejor compresión y rendimiento.

```sql
-- Cargar datos en la tabla de destino
SET hive.exec.dynamic.partition.mode=nonstrict;

INSERT OVERWRITE TABLE ventas_destino
PARTITION (anio, mes)
SELECT 
    id_venta,
    TO_DATE(fecha) AS fecha,
    id_producto,
    id_cliente,
    cantidad,
    monto,
    YEAR(TO_DATE(fecha)) AS anio,
    MONTH(TO_DATE(fecha)) AS mes
FROM ventas_origen;
```

Esta parte carga los datos en la tabla de destino:

- `SET hive.exec.dynamic.partition.mode=nonstrict;` permite la creación dinámica de particiones.
- La sentencia `INSERT OVERWRITE` carga los datos, transformando la fecha y calculando el año y mes para las particiones.

```sql
-- Análisis: Ventas totales y número de clientes únicos por mes
SELECT 
    anio,
    mes,
    SUM(monto) AS ventas_totales,
    COUNT(DISTINCT id_cliente) AS clientes_unicos
FROM ventas_destino
GROUP BY anio, mes
ORDER BY anio, mes;
```

Esta consulta analiza los datos cargados, calculando las ventas totales y el número de clientes únicos por mes.

### Ejercicio 2: UDF para análisis de logs

Este ejercicio consta de dos partes: una UDF en Java y un script SQL que la utiliza.

#### UDF en Java

```java
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

public class ExtractLogLevel extends UDF {
    public Text evaluate(Text s) {
        if (s == null) return null;
        String input = s.toString().toUpperCase();
        if (input.contains("ERROR")) return new Text("ERROR");
        if (input.contains("WARN")) return new Text("WARN");
        if (input.contains("INFO")) return new Text("INFO");
        return new Text("UNKNOWN");
    }
}
```

Esta UDF:

- Extiende la clase `UDF` de Hive.
- Define un método `evaluate` que toma un `Text` (el mensaje de log) y devuelve otro `Text` (el nivel de log).
- Convierte el input a mayúsculas y busca las palabras clave "ERROR", "WARN", e "INFO".
- Si no encuentra ninguna, devuelve "UNKNOWN".

#### Script SQL

```sql
-- Asumiendo que ya has añadido el JAR con la UDF a Hive
CREATE TEMPORARY FUNCTION extract_log_level AS 'com.example.ExtractLogLevel';
```

Esta línea crea una función temporal en Hive que utiliza la UDF Java.

```sql
-- Crear tabla de logs
CREATE TABLE logs (
    timestamp STRING,
    message STRING
);

-- Insertar algunos datos de ejemplo (en una situación real, cargarías tus logs reales)
INSERT INTO logs VALUES 
    ('2023-06-01 10:00:00', 'INFO: Application started'),
    ('2023-06-01 10:05:00', 'WARN: Low memory detected'),
    ('2023-06-01 10:10:00', 'ERROR: NullPointerException in module X');
```

Estas líneas crean una tabla de logs y la llenan con datos de ejemplo.

```sql
-- Analizar frecuencia de niveles de log
SELECT 
    extract_log_level(message) AS log_level,
    COUNT(*) AS frequency
FROM logs
GROUP BY extract_log_level(message)
ORDER BY frequency DESC;
```

Esta consulta utiliza la UDF para extraer el nivel de log de cada mensaje y luego cuenta la frecuencia de cada nivel.

### Ejercicio 3: Análisis de posts de redes sociales

```sql
-- Crear tabla para almacenar posts de redes sociales
CREATE TABLE social_posts (
    post_id STRING,
    user_id STRING,
    username STRING,
    followers INT,
    post_content STRING,
    tags ARRAY<STRING>,
    likes INT,
    shares INT,
    timestamp TIMESTAMP
)
STORED AS ORC;
```

Esta parte crea una tabla para almacenar posts de redes sociales. Nótese el uso de `ARRAY<STRING>` para las etiquetas, permitiendo múltiples etiquetas por post.

```sql
-- Insertar algunos datos de ejemplo (en una situación real, cargarías tus datos JSON reales)
INSERT INTO social_posts
SELECT
    'post1', 'user1', 'johndoe', 1000, 'Check out this awesome view! #travel #nature',
    array('travel', 'nature'), 50, 10, '2023-06-01 10:00:00'
UNION ALL
SELECT
    'post2', 'user2', 'janedoe', 5000, 'New recipe alert! #cooking #healthy',
    array('cooking', 'healthy'), 100, 20, '2023-06-01 11:00:00';
```

Esta parte inserta datos de ejemplo en la tabla. En un escenario real, estos datos se cargarían desde un archivo JSON.

```sql
-- Identificar las etiquetas más populares
SELECT 
    tag,
    COUNT(*) AS usage_count
FROM social_posts
LATERAL VIEW explode(tags) tag_table AS tag
GROUP BY tag
ORDER BY usage_count DESC
LIMIT 10;
```

Esta consulta identifica las etiquetas más populares:

- `LATERAL VIEW explode(tags)` "explota" el array de etiquetas, creando una fila por cada etiqueta.
- Luego, se cuenta la frecuencia de cada etiqueta y se ordenan de mayor a menor.

```sql
-- Identificar los usuarios más influyentes basados en seguidores y likes promedio
SELECT 
    user_id,
    username,
    followers,
    AVG(likes) AS avg_likes,
    COUNT(*) AS post_count
FROM social_posts
GROUP BY user_id, username, followers
ORDER BY followers DESC, avg_likes DESC
LIMIT 10;
```

Esta consulta identifica a los usuarios más influyentes:

- Agrupa los posts por usuario.
- Calcula el promedio de likes y el número de posts para cada usuario.
- Ordena los resultados primero por número de seguidores y luego por promedio de likes.
