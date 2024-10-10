# Guía avanzada de Apache Hive

En relación con los contenidos del curso, se corresponde con:

- Modulo 2:
  - Herramientas: Hive.
  - Herramientas: Consultas Hive.

## 1. Introducción a Apache Hive

Apache Hive es un sistema de data warehouse distribuido construido sobre Hadoop. Proporciona una interfaz similar a SQL para consultar y gestionar grandes conjuntos de datos almacenados en diversos sistemas de archivos compatibles con Hadoop.

### Conceptos clave

1. **Data Warehouse**

   Un data warehouse es un sistema utilizado para el análisis y reporte de datos. Almacena datos históricos y actuales de múltiples fuentes en un formato estructurado y optimizado para consultas analíticas complejas. Características principales:

   - Datos integrados de múltiples fuentes
   - Orientado a temas específicos
   - No volátil (los datos históricos no se modifican)
   - Variante en el tiempo (mantiene el historial de los datos)

2. **Data Lake**

   Un data lake es un repositorio centralizado que permite almacenar todos los datos estructurados y no estructurados a cualquier escala. A diferencia de un data warehouse, los datos en un data lake se almacenan en su formato nativo. Características principales:

   - Almacena datos en bruto
   - Soporta todos los tipos de datos
   - Esquema en lectura (los datos se estructuran al momento de ser utilizados)
   - Alta flexibilidad y escalabilidad

3. **Metadatos**

   Los metadatos son "datos sobre los datos". En el contexto de Hive, los metadatos incluyen información sobre la estructura de las tablas, las columnas, los tipos de datos, la ubicación de los archivos de datos, y las estadísticas de las tablas. El metastore de Hive es responsable de almacenar y gestionar estos metadatos.

### Características clave de Hive

- **Lenguaje similar a SQL (HiveQL)**: Hive proporciona un lenguaje de consulta llamado HiveQL, que es similar a SQL estándar. Esto facilita la transición para usuarios familiarizados con SQL y permite a los analistas de datos aprovechar sus habilidades existentes.

- **Procesamiento de grandes volúmenes de datos**: Hive está diseñado para manejar conjuntos de datos a escala de petabytes. Utiliza el poder de procesamiento distribuido de Hadoop para ejecutar consultas en paralelo en múltiples nodos.

- **Soporte para diferentes formatos de datos**: Hive puede trabajar con varios formatos de datos, incluyendo texto plano, RCFile, ORC, Parquet, y Avro. Esto proporciona flexibilidad para trabajar con diversos tipos de datos y optimizar el rendimiento según las necesidades específicas.

- **Extensibilidad**: Hive permite a los usuarios definir funciones personalizadas (UDFs, UDAFs, UDTFs) para extender sus capacidades y realizar operaciones específicas del dominio.

- **Integración con el ecosistema Hadoop**: Hive se integra estrechamente con otras herramientas del ecosistema Hadoop, como HBase, Pig, y Spark, permitiendo flujos de trabajo de datos complejos y análisis avanzados.

### Arquitectura de Hive

1. **HiveServer2**: Es la interfaz principal para que los clientes se conecten y ejecuten consultas en Hive. Soporta múltiples clientes concurrentes y proporciona autenticación.

2. **Metastore**: Almacena los metadatos de Hive, incluyendo la definición de tablas, columnas, particiones, y estadísticas. Puede configurarse para usar una base de datos relacional como backend para mayor escalabilidad y rendimiento.

3. **Driver**: Gestiona el ciclo de vida de las consultas HiveQL. Recibe las consultas, las procesa a través de las diferentes etapas de ejecución y devuelve los resultados al cliente.

4. **Compiler**: Realiza la compilación semántica de la consulta HiveQL. Convierte la consulta en un plan lógico y luego en un plan físico de ejecución.

5. **Optimizer**: Aplica varias optimizaciones al plan de ejecución para mejorar el rendimiento. Esto incluye pushdown de predicados, optimización de joins, y reordenamiento de operaciones.

6. **Execution Engine**: Ejecuta las tareas generadas por el compilador. Tradicionalmente, Hive utilizaba MapReduce, pero las versiones más recientes pueden usar motores como Tez o Spark para una ejecución más eficiente.

## 2. Ejecución de consultas en Hive

### 2.1 Instalación

#### 2.1.1 Instalar VSCode

Si aún no tienes VSCode instalado, descárgalo e instálalo desde <https://code.visualstudio.com>.

#### 2.2.2 Instalar extensiones necesarias

Abre VSCode y instala las siguientes extensiones:

- "Spark & Hive Tools"

Para instalar estas extensiones:

- Ve a la vista de extensiones (icono de cuadrados en la barra lateral izquierda o Ctrl+Shift+X).
- Busca cada extensión por su nombre.
- Haz clic en "Install" para cada una.

Hive proporciona varias formas de ejecutar consultas, cada una adaptada a diferentes casos de uso y preferencias de los usuarios:

### 2.2 Ejecución

#### 2.2.1 Conexión usando Beeline

Beeline es la herramienta de línea de comandos recomendada para conectarse a Hive. Aquí tienes una explicación más detallada:

- Abre una terminal
- Usa el siguiente comando para conectarte:

```bash
beeline -u jdbc:hive2://localhost:10000 -n hive -p hive_password
```

Donde:

- jdbc:hive2://localhost:10000 es la URL de conexión (asume que HiveServer2 está en localhost y puerto 10000)
- -n hive especifica el nombre de usuario
- -p hive_password especifica la contraseña

Si la conexión es exitosa, verás un prompt de Beeline:

```bash
0: jdbc:hive2://localhost:10000>
```

Ahora puedes ejecutar consultas SQL directamente.

#### 2.2.2 Conexión en modo batch

El modo batch no requiere una conexión interactiva. En su lugar, ejecutas un script que contiene las consultas:

Crea un archivo ```test.hql``` con tus consultas con el siguiente contenido para crear una base de datos con información ficitcia para realiza una consulta de prueba.

```sql
-- Paso 1: Crear la base de datos
CREATE DATABASE IF NOT EXISTS test_db;

-- Paso 2: Usar la base de datos
USE test_db;

-- Paso 3: Crear una tabla simple
CREATE TABLE IF NOT EXISTS test_table (
    id INT,
    name STRING
);

-- Paso 4: Insertar un dato
INSERT INTO test_table VALUES (1, 'Test');

-- Paso 5: Verificar el dato
SELECT * FROM test_table;
```

Ejecuta el script usando el comando:

```bash
beeline -u jdbc:hive2://localhost:10000 -n hive -p hive_password --silent=true -f test.hql 
```

Este comando se conectará a Hive, ejecutará las consultas en el script, y luego se desconectará automáticamente.

Si necesitas especificar credenciales, puedes hacerlo así:

#### 2.2.3 Conexión desde aplicaciones Java

Para conectar a Hive desde una aplicación Java, necesitas usar el driver JDBC de Hive. Aquí tienes una explicación más detallada:

- Primero, asegúrate de tener el driver JDBC de Hive en tu classpath. Puedes descargarlo del sitio web de Apache Hive o incluirlo como dependencia en tu proyecto si estás usando un gestor de dependencias como Maven.

Aquí tienes un ejemplo más completo de cómo conectar y ejecutar una consulta:

HiveJDBCExample.javaClick to open code

- Para ejecutar este código:

Compílalo: ```javac HiveJDBCExample.java```
Ejecútalo: ```java -cp .:path/to/hive-jdbc.jar HiveJDBCExample```

Asegúrate de reemplazar ```path/to/hive-jdbc.jar``` con la ruta real al JAR del driver JDBC de Hive.

## 3. Fundamentos de HiveQL

HiveQL es el lenguaje de consulta de Hive, muy similar a SQL estándar pero con algunas particularidades para trabajar con grandes volúmenes de datos en un entorno distribuido.

### 3.1 Tipos de datos

Hive soporta una variedad de tipos de datos para adaptarse a diferentes necesidades de almacenamiento y análisis:

#### Tipos primitivos

- `TINYINT`: Entero de 8 bits con signo
- `SMALLINT`: Entero de 16 bits con signo
- `INT`: Entero de 32 bits con signo
- `BIGINT`: Entero de 64 bits con signo
- `FLOAT`: Número de punto flotante de precisión simple
- `DOUBLE`: Número de punto flotante de doble precisión
- `BOOLEAN`: Valor booleano (true/false)
- `STRING`: Cadena de caracteres en formato UTF-8
- `VARCHAR`: Cadena de caracteres de longitud variable con límite máximo
- `CHAR`: Cadena de caracteres de longitud fija
- `TIMESTAMP`: Instante temporal con precisión de nanosegundos
- `DATE`: Fecha sin componente de tiempo
- `BINARY`: Array de bytes

#### Tipos complejos

- `ARRAY<data_type>`: Colección ordenada de elementos del mismo tipo
- `MAP<key_type, value_type>`: Colección de pares clave-valor
- `STRUCT<col_name : data_type [COMMENT col_comment], ...>`: Registro con campos nombrados
- `UNIONTYPE<data_type, data_type, ...>`: Valor que puede ser de uno de varios tipos

Ejemplo de uso:

```sql
CREATE TABLE empleados (
  id INT,
  nombre STRING,
  habilidades ARRAY<STRING>,
  detalles MAP<STRING, STRING>,
  direccion STRUCT<calle:STRING, ciudad:STRING, cp:STRING>
);
```

### 3.2 DDL (Data Definition Language)

El DDL en HiveQL permite a los usuarios definir y modificar la estructura de los datos:

1. **CREATE**
   - Crea nuevas bases de datos, tablas, vistas, o índices.
   - Soporta opciones como particionamiento y bucketing para optimizar el rendimiento.

   Ejemplo:

   ```sql
   CREATE DATABASE mydatabase;
   
   CREATE TABLE mytable (
     id INT,
     name STRING,
     age INT
   )
   PARTITIONED BY (year INT, month INT)
   CLUSTERED BY (id) INTO 4 BUCKETS
   STORED AS ORC;
   ```

2. **ALTER**
   - Modifica la estructura de objetos existentes como tablas o particiones.
   - Permite añadir/eliminar columnas, cambiar propiedades de la tabla, etc.

   Ejemplo:

   ```sql
   ALTER TABLE mytable ADD COLUMNS (salary DOUBLE);
   
   ALTER TABLE mytable PARTITION (year=2023, month=6) 
   SET FILEFORMAT PARQUET;
   ```

3. **DROP**
   - Elimina objetos como bases de datos, tablas, o vistas.
   - Puede incluir la opción CASCADE para eliminar objetos dependientes.

   Ejemplo:

   ```sql
   DROP TABLE IF EXISTS mytable;
   
   DROP DATABASE mydatabase CASCADE;
   ```

4. **TRUNCATE**
   - Elimina todos los datos de una tabla o partición, pero mantiene la estructura.

   Ejemplo:

   ```sql
   TRUNCATE TABLE mytable;
   ```

### 3.3 DML (Data Manipulation Language)

El DML en HiveQL permite a los usuarios manipular los datos dentro de las tablas:

1. **SELECT**
   - Recupera datos de una o más tablas.
   - Soporta cláusulas como WHERE, GROUP BY, HAVING, ORDER BY, LIMIT.

   Ejemplo:

   ```sql
   SELECT nombre, edad 
   FROM empleados 
   WHERE edad > 30 
   ORDER BY edad DESC 
   LIMIT 10;
   ```

2. **INSERT**
   - Inserta nuevos datos en una tabla.
   - Puede insertar valores específicos o resultados de una consulta.

   Ejemplo:

   ```sql
   INSERT INTO TABLE empleados
   VALUES (1, 'John Doe', 35);
   
   INSERT OVERWRITE TABLE empleados_senior
   SELECT * FROM empleados WHERE edad > 50;
   ```

3. **UPDATE** (requiere transacciones ACID habilitadas)
   - Modifica datos existentes en una tabla.

   Ejemplo:

   ```sql
   UPDATE empleados 
   SET salario = salario * 1.1 
   WHERE performance_rating > 8;
   ```

4. **DELETE** (requiere transacciones ACID habilitadas)
   - Elimina filas específicas de una tabla.

   Ejemplo:

   ```sql
   DELETE FROM empleados 
   WHERE termination_date IS NOT NULL;
   ```

5. **MERGE**
   - Realiza operaciones de upsert (insert/update) basadas en una condición de coincidencia.

   Ejemplo:

   ```sql
   MERGE INTO target t
   USING source s
   ON t.id = s.id
   WHEN MATCHED THEN UPDATE SET t.value = s.value
   WHEN NOT MATCHED THEN INSERT VALUES (s.id, s.value);
   ```

6. **LOAD DATA**
   - Carga datos en una tabla Hive desde el sistema de archivos.

   Ejemplo:

   ```sql
   LOAD DATA LOCAL INPATH '/path/to/file/empleados.txt' 
   OVERWRITE INTO TABLE empleados;
   ```

### 3.4 Funciones incorporadas

Hive proporciona numerosas funciones incorporadas para facilitar el análisis de datos:

1. **Funciones de agregación**
   - Realizan cálculos sobre conjuntos de valores y devuelven un único resultado.
   - Incluyen: `COUNT()`, `SUM()`, `AVG()`, `MIN()`, `MAX()`

   Ejemplo:

   ```sql
   SELECT dept_id, AVG(salario) as avg_salary, COUNT(*) as num_empleados
   FROM empleados
   GROUP BY dept_id;
   ```

2. **Funciones de cadena**
   - Manipulan y analizan datos de tipo string.
   - Incluyen: `CONCAT()`, `SUBSTR()`, `UPPER()`, `LOWER()`, `TRIM()`, `LENGTH()`

   Ejemplo:

   ```sql
   SELECT CONCAT(UPPER(nombre), ' ', UPPER(apellido)) as nombre_completo,
          LENGTH(nombre) as longitud_nombre
   FROM empleados;
   ```

3. **Funciones de fecha**
   - Manipulan y extraen información de datos de tipo date y timestamp.
   - Incluyen: `YEAR()`, `MONTH()`, `DAY()`, `DATE_ADD()`, `DATEDIFF()`, `TO_DATE()`

   Ejemplo:

   ```sql
   SELECT nombre, 
          DATEDIFF(CURRENT_DATE, fecha_contratacion) as dias_empleado,
          DATE_ADD(fecha_contratacion, 365) as fecha_aniversario
   FROM empleados;
   ```

4. **Funciones matemáticas**
   - Realizan operaciones matemáticas.
   - Incluyen: `ROUND()`, `FLOOR()`, `CEIL()`, `ABS()`, `SQRT()`, `POW()`, `EXP()`, `LOG()`

   Ejemplo:

   ```sql
   SELECT 
     ROUND(salario, 2) as salario_redondeado,
     FLOOR(salario) as salario_piso,
     CEIL(salario) as salario_techo,
     SQRT(salario) as raiz_cuadrada_salario
   FROM empleados;
   ```

5. **Funciones condicionales**
   - Permiten lógica condicional en las consultas.
   - Incluyen: `IF`, `CASE`, `COALESCE`, `NULLIF`

   Ejemplo:

   ```sql
   SELECT nombre, 
          CASE 
            WHEN edad < 30 THEN 'Joven'
            WHEN edad BETWEEN 30 AND 50 THEN 'Adulto'
            ELSE 'Senior'
          END as categoria_edad,
          COALESCE(departamento, 'Sin asignar') as dept
   FROM empleados;
   ```

6. **Funciones de conversión de tipo**
   - Convierten datos de un tipo a otro.
   - Incluyen: `CAST()`, `BINARY()`, `TO_DATE()`, `UNIX_TIMESTAMP()`

   Ejemplo:

   ```sql
   SELECT 
     CAST(id AS STRING) as id_string,
     TO_DATE(fecha_contratacion) as fecha_solo,
     UNIX_TIMESTAMP(fecha_contratacion) as timestamp_unix
   FROM empleados;
   ```

7. **Funciones de colección**
   - Operan en tipos de datos complejos como arrays y maps.
   - Incluyen: `SIZE()`, `ARRAY_CONTAINS()`, `SORT_ARRAY()`, `MAP_KEYS()`, `MAP_VALUES()`

   Ejemplo:

   ```sql
   SELECT 
     nombre,
     SIZE(habilidades) as num_habilidades,
     ARRAY_CONTAINS(habilidades, 'SQL') as conoce_sql,
     SORT_ARRAY(habilidades) as habilidades_ordenadas
   FROM empleados;
   ```

8. **Funciones de ventana**
   - Realizan cálculos a través de un conjunto de filas que están relacionadas con la fila actual.
   - Incluyen: `ROW_NUMBER()`, `RANK()`, `DENSE_RANK()`, `LAG()`, `LEAD()`

   Ejemplo:

   ```sql
   SELECT 
     nombre,
     departamento,
     salario,
     RANK() OVER (PARTITION BY departamento ORDER BY salario DESC) as ranking_salario_dept,
     AVG(salario) OVER (PARTITION BY departamento) as promedio_salario_dept
   FROM empleados;
   ```

9. **Funciones de tabla**
   - Generan conjuntos de filas.
   - Incluyen: `EXPLODE()`, `POSEXPLODE()`, `STACK()`

   Ejemplo:

   ```sql
   SELECT nombre, habilidad
   FROM empleados
   LATERAL VIEW EXPLODE(habilidades) habilidad_view AS habilidad;
   ```

10. **Otras funciones**
    - Diversas funciones útiles que no caen en las categorías anteriores.
    - Incluyen: `GET_JSON_OBJECT()`, `XPATH()`, `REFLECT()`, `HASH()`

   Ejemplo:

```sql
SELECT 
GET_JSON_OBJECT(datos_json, '$.nombre') as nombre,
HASH(id) as id_hash
FROM registros;
```

## 4. Procesamiento con Hive

### 4.1 Optimización de consultas

La optimización de consultas es crucial para mejorar el rendimiento en Hive, especialmente cuando se trabaja con grandes volúmenes de datos.

- **Particionamiento**

   El particionamiento divide los datos en directorios separados basados en los valores de una o más columnas. Esto mejora significativamente el rendimiento de las consultas al permitir el "partition pruning", es decir, Hive puede evitar escanear particiones que no son relevantes para una consulta específica.

   Ejemplo:

   ```sql
   CREATE TABLE ventas (
     id INT, 
     producto STRING, 
     cantidad INT
   )
   PARTITIONED BY (fecha DATE);
   
   INSERT OVERWRITE TABLE ventas PARTITION (fecha='2023-06-01')
   SELECT id, producto, cantidad FROM ventas_raw WHERE fecha='2023-06-01';
   ```

   Explicación: En este ejemplo, la tabla "ventas" está particionada por fecha. Cuando se realiza una consulta que incluye una condición en la columna "fecha", Hive puede limitar la búsqueda solo a las particiones relevantes, reduciendo significativamente la cantidad de datos que necesita escanear.

- **Bucketing**

   El bucketing distribuye los datos en un número fijo de archivos basado en el hash de una o más columnas. Esto puede mejorar el rendimiento de los joins y ciertas operaciones de agregación.

   Ejemplo:

   ```sql
   CREATE TABLE empleados (
     id INT,
     nombre STRING,
     departamento STRING
   )
   CLUSTERED BY (id) INTO 4 BUCKETS;
   ```

   Explicación: Aquí, la tabla "empleados" se divide en 4 buckets basados en el hash de la columna "id". Esto puede acelerar las operaciones de join cuando se unen dos tablas que están bucketeadas por la misma columna.

- **Índices**

   Aunque menos comunes en Hive que en bases de datos tradicionales, los índices pueden mejorar el rendimiento de ciertas consultas, especialmente para tablas muy grandes.

   Ejemplo

   ```sql
   CREATE INDEX idx_empleados_dept ON TABLE empleados (departamento) AS 'COMPACT' WITH DEFERRED REBUILD;
   ALTER INDEX idx_empleados_dept ON empleados REBUILD;
   ```

   Explicación: Este índice en la columna "departamento" puede acelerar las consultas que filtran o agrupan por departamento. Sin embargo, es importante notar que los índices en Hive tienen limitaciones y no siempre proporcionan mejoras significativas de rendimiento.

### 4.2 Vistas y vistas materializadas

Las vistas y vistas materializadas son herramientas poderosas para simplificar consultas complejas y mejorar el rendimiento.

1. **Vistas**
   Las vistas son consultas almacenadas que se comportan como tablas virtuales. Simplifican consultas complejas y proporcionan una capa de abstracción.

   Ejemplo:

   ```sql
   CREATE VIEW empleados_senior AS
   SELECT * FROM empleados WHERE edad > 50;
   ```

   Explicación: Esta vista simplifica las consultas sobre empleados mayores de 50 años. Los usuarios pueden consultar esta vista como si fuera una tabla regular, sin necesidad de recordar la condición de filtrado.

2. **Vistas materializadas**

   Las vistas materializadas almacenan físicamente los resultados de una consulta para un acceso más rápido. Son útiles para consultas complejas que se ejecutan frecuentemente.

   Ejemplo

   ```sql
   CREATE MATERIALIZED VIEW mv_ventas_diarias AS
   SELECT fecha, SUM(cantidad) as total_ventas
   FROM ventas
   GROUP BY fecha;
   
   -- Actualizar la vista materializada
   ALTER MATERIALIZED VIEW mv_ventas_diarias REBUILD;
   ```

   Explicación: Esta vista materializada precalcula las ventas totales por día. Las consultas posteriores sobre ventas diarias pueden usar esta vista materializada en lugar de recalcular los totales cada vez, lo que puede ser significativamente más rápido para grandes volúmenes de datos.

### 4.3 Manejo de datos complejos y semiestructurados

Hive proporciona herramientas poderosas para trabajar con datos complejos y semiestructurados, como arrays, mapas y JSON.

1. **Trabajar con arrays**
   Hive permite almacenar y manipular arrays directamente en las tablas.

   Ejemplo:

   ```sql
   CREATE TABLE productos (
     id INT,
     nombre STRING,
     categorias ARRAY<STRING>
   );
   
   SELECT id, nombre, categorias[0] as categoria_principal
   FROM productos;
   
   SELECT id, categoria
   FROM productos
   LATERAL VIEW explode(categorias) categoria_view AS categoria;
   ```

   Explicación: La primera consulta selecciona la primera categoría de cada producto. La segunda consulta "explota" el array de categorías, creando una fila separada para cada categoría de cada producto.

2. **Trabajar con mapas**
   Los mapas en Hive permiten almacenar pares clave-valor.

   Ejemplo:

   ```sql
   CREATE TABLE usuarios (
     id INT,
     nombre STRING,
     atributos MAP<STRING, STRING>
   );
   
   SELECT id, nombre, atributos['ciudad'] as ciudad
   FROM usuarios;
   ```

   Explicación: Esta consulta extrae el valor asociado con la clave 'ciudad' del mapa de atributos para cada usuario.

3. **Procesamiento de JSON**
   Hive ofrece funciones para trabajar con datos JSON.

   Ejemplo:

   ```sql
   CREATE TABLE eventos (evento STRING);
   
   SELECT get_json_object(evento, '$.user_id') as user_id,
          get_json_object(evento, '$.action') as action
   FROM eventos;
   ```

   Explicación: Esta consulta extrae los campos 'user_id' y 'action' de una cadena JSON almacenada en la columna 'evento'.

### 4.4 Joins y operaciones de conjunto

Hive soporta varios tipos de joins y operaciones de conjunto para combinar y analizar datos de múltiples fuentes.

1. **Tipos de joins**
   Hive soporta inner join, left outer join, right outer join, full outer join, y cross join.

   Ejemplo:

   ```sql
   SELECT e.nombre, d.nombre as departamento
   FROM empleados e
   JOIN departamentos d ON e.dept_id = d.id;
   ```

   Explicación: Este es un inner join que combina datos de las tablas 'empleados' y 'departamentos' basándose en la correspondencia entre 'dept_id' y 'id'.

2. **Map-side join**
   Una optimización para joins donde una tabla es significativamente más pequeña que la otra.

   Ejemplo:

   ```sql
   SET hive.auto.convert.join=true;
   SET hive.mapjoin.smalltable.filesize=25000000;
   
   SELECT /*+ MAPJOIN(d) */ e.nombre, d.nombre as departamento
   FROM empleados e
   JOIN departamentos d ON e.dept_id = d.id;
   ```

   Explicación: Esta configuración y hint le dicen a Hive que intente realizar un map-side join, cargando la tabla más pequeña (departamentos) en memoria en cada mapper.

3. **Operaciones de conjunto**:
   Hive soporta operaciones de conjunto como UNION, INTERSECT, y EXCEPT.

   Ejemplo:

   ```sql
   -- UNION
   SELECT * FROM empleados_2022
   UNION
   SELECT * FROM empleados_2023;
   
   -- INTERSECT
   SELECT departamento FROM empleados
   INTERSECT
   SELECT departamento FROM departamentos_activos;
   
   -- EXCEPT
   SELECT departamento FROM empleados
   EXCEPT
   SELECT departamento FROM departamentos_cerrados;
   ```

   Explicación: UNION combina los resultados de dos consultas, INTERSECT encuentra los valores comunes, y EXCEPT encuentra los valores en la primera consulta que no están en la segunda.

## 5. User-Defined Functions (UDFs)

Las User-Defined Functions (UDFs) en Hive permiten a los usuarios extender las capacidades de HiveQL con funciones personalizadas. Estas funciones pueden ser escritas en Java, Python (usando Jython), o cualquier lenguaje que pueda ser invocado desde Java.

### 5.1 UDFs escalares

Las UDFs escalares operan en una sola fila y devuelven un solo valor.

Ejemplo en Java:

```java
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

public class LowerCase extends UDF {
    public Text evaluate(Text input) {
        if(input == null) return null;
        return new Text(input.toString().toLowerCase());
    }
}
```

Para usar esta UDF en Hive:

```sql
ADD JAR /path/to/my-udfs.jar;
CREATE TEMPORARY FUNCTION lowercase AS 'com.example.LowerCase';

SELECT lowercase(nombre) FROM empleados;
```

Explicación: Esta UDF convierte el texto de entrada a minúsculas. Después de añadir el JAR y crear la función temporal, puede ser utilizada en consultas SQL como cualquier otra función.

### 5.2 UDAFs (User-Defined Aggregate Functions)

Las UDAFs operan en múltiples filas y devuelven un solo valor. Son útiles para implementar operaciones de agregación personalizadas.

Ejemplo simplificado en Java:

```java
import org.apache.hadoop.hive.ql.exec.UDAF;
import org.apache.hadoop.hive.ql.exec.UDAFEvaluator;
import org.apache.hadoop.io.IntWritable;

public class Median extends UDAF {
    public static class MedianEvaluator implements UDAFEvaluator {
        private ArrayList<Integer> values;

        public MedianEvaluator() {
            super();
            values = new ArrayList<Integer>();
        }

        public void init() {
            values.clear();
        }

        public boolean iterate(IntWritable value) {
            if (value == null) {
                return true;
            }
            values.add(value.get());
            return true;
        }

        public ArrayList<Integer> terminatePartial() {
            return values;
        }

        public boolean merge(ArrayList<Integer> other) {
            if (other == null) {
                return true;
            }
            values.addAll(other);
            return true;
        }

        public IntWritable terminate() {
            if (values.size() == 0) {
                return null;
            }
            Collections.sort(values);
            int middle = values.size() / 2;
            if (values.size() % 2 == 1) {
                return new IntWritable(values.get(middle));
            } else {
                int median = (values.get(middle-1) + values.get(middle)) / 2;
                return new IntWritable(median);
            }
        }
    }
}
```

Para usar esta UDAF en Hive:

```sql
ADD JAR /path/to/my-udafs.jar;
CREATE TEMPORARY FUNCTION median AS 'com.example.Median';

SELECT departamento, median(salario) FROM empleados GROUP BY departamento;
```

Explicación: Esta UDAF calcula la mediana de un conjunto de valores. Es más compleja que una UDF escalar porque necesita manejar múltiples fases de agregación (iterate, terminatePartial, merge, terminate) para funcionar correctamente en un entorno distribuido.

### 5.3 UDTFs (User-Defined Table-Generating Functions)

Las UDTFs operan en una sola fila pero pueden producir múltiples filas de salida. Son útiles para operaciones como explode o parsing de cadenas complejas.

Ejemplo simplificado en Java:

```java
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.ArrayList;
import java.util.List;

public class SplitString extends GenericUDTF {
    @Override
    public StructObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
        List<String> fieldNames = new ArrayList<String>();
        List<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>();

        fieldNames.add("split_value");
        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);

        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldOIs);
    }

    @Override
    public void process(Object[] args) throws HiveException {
        String input = args[0].toString();
        String separator = args[1].toString();

        String[] results = input.split(separator);
        for(String r : results) {
            forward(new Object[]{r});
        }
    }

    @Override
    public void close() throws HiveException {
        // No es necesario hacer nada aquí
    }
}
```

Para usar esta UDTF en Hive:

```sql
ADD JAR /path/to/my-udtfs.jar;
CREATE TEMPORARY FUNCTION split_string AS 'com.example.SplitString';

SELECT split_string(texto, ',') AS palabra FROM documentos;
```

Explicación: Esta UDTF divide una cadena en múltiples filas basándose en un separador. Es útil para descomponer campos que contienen múltiples valores en filas separadas para un análisis más detallado.

## 6. Integración con otros sistemas

Hive puede integrarse con varios sistemas dentro del ecosistema Hadoop y más allá, permitiendo flujos de trabajo de datos complejos y análisis avanzados.

### 6.1 Hive y HBase

HBase es una base de datos NoSQL distribuida que proporciona acceso en tiempo real a grandes conjuntos de datos. Hive puede integrarse con HBase para combinar las capacidades de procesamiento por lotes de Hive con el acceso en tiempo real de HBase.

Ejemplo de creación de una tabla Hive sobre HBase:

```sql
CREATE EXTERNAL TABLE hbase_table_emp(
  id INT,
  nombre STRING,
  salario FLOAT
)
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,personal:nombre,personal:salario")
TBLPROPERTIES ("hbase.table.name" = "emp");
```

Explicación: Esta consulta crea una tabla externa en Hive que se mapea a una tabla en HBase. Los datos se almacenan en HBase, pero pueden ser consultados y analizados usando HiveQL.

### 6.2 Hive y sistemas de archivos externos (S3, Azure Blob Storage)

Hive puede trabajar con datos almacenados en sistemas de archivos distribuidos como HDFS, pero también puede integrarse con almacenamiento en la nube como Amazon S3 o Azure Blob Storage.

Ejemplo de creación de una tabla en S3:

```sql
CREATE EXTERNAL TABLE s3_table (
  id INT,
  nombre STRING,
  edad INT
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
LOCATION 's3a://my-bucket/path/to/data/';
```

Explicación: Esta consulta crea una tabla externa en Hive que lee datos directamente de un bucket de S3. Esto permite a Hive trabajar con datos almacenados en la nube sin necesidad de moverlos a HDFS.

### 6.3 Hive y Spark

Apache Spark es un motor de procesamiento de datos rápido y de propósito general. Hive puede integrarse con Spark de varias maneras:

- Usando Spark como motor de ejecución para Hive:

```sql
SET hive.execution.engine=spark;
```

- Leyendo tablas de Hive desde Spark:

```scala
val spark = SparkSession.builder().enableHiveSupport().getOrCreate()
val df = spark.sql("SELECT * FROM mi_tabla_hive")
```

Explicación: La primera línea configura Hive para usar Spark como su motor de ejecución, lo que puede mejorar significativamente el rendimiento de las consultas. La segunda parte muestra cómo se pueden leer tablas de Hive directamente en Spark para un procesamiento adicional.

## 7. Optimización y ajuste de rendimiento

La optimización y el ajuste de rendimiento son cruciales para asegurar que las consultas y los procesos de Hive se ejecuten de manera eficiente, especialmente cuando se trabaja con grandes volúmenes de datos.

### 7.1 Configuración de memoria y paralelismo

Ajustar la configuración de memoria y paralelismo puede mejorar significativamente el rendimiento de Hive.

1. **Configuración de memoria**

   ```sql
   SET mapreduce.map.memory.mb=4096;
   SET mapreduce.reduce.memory.mb=8192;
   SET hive.exec.reducers.bytes.per.reducer=1073741824;  -- 1GB por reducer
   ```

   Explicación: Estos ajustes aumentan la memoria disponible para los mappers y reducers, y establecen el tamaño de datos por reducer, lo que puede mejorar el rendimiento para trabajos con grandes conjuntos de datos.

2. **Paralelismo**

   ```sql
   SET hive.exec.parallel=true;
   SET hive.exec.parallel.thread.number=8;
   ```

   Explicación: Estas configuraciones permiten la ejecución paralela de consultas y establecen el número de hilos para la ejecución paralela.

### 7.2 Optimización del formato de almacenamiento (ORC, Parquet)

El uso de formatos de archivo columnares como ORC (Optimized Row Columnar) o Parquet puede mejorar significativamente el rendimiento de las consultas.

```sql
CREATE TABLE mi_tabla_orc (
  id INT,
  nombre STRING,
  edad INT
) STORED AS ORC;

INSERT INTO TABLE mi_tabla_orc SELECT * FROM mi_tabla_original;
```

Explicación: Este ejemplo crea una nueva tabla utilizando el formato ORC y luego inserta datos desde una tabla existente. ORC proporciona una compresión eficiente y permite la lectura columnar, lo que puede acelerar significativamente ciertas consultas.

### 7.3 Técnicas de optimización de joins

La optimización de joins es crucial para mejorar el rendimiento de consultas complejas.

1. **Map Join**:

   ```sql
   SET hive.auto.convert.join=true;
   SET hive.mapjoin.smalltable.filesize=25000000;
   ```

   Explicación: Estas configuraciones permiten a Hive convertir automáticamente joins comunes en map joins cuando una de las tablas es lo suficientemente pequeña como para caber en memoria.

2. **Bucket Map Join**:

   ```sql
   SET hive.optimize.bucketmapjoin=true;
   ```

   Explicación: Esta configuración permite a Hive utilizar bucket map joins cuando las tablas están bucketeadas en la misma columna de join.

3. **Sort Merge Bucket Join**:

   ```sql
   SET hive.auto.convert.sortmerge.join=true;
   SET hive.optimize.bucketmapjoin=true;
   SET hive.optimize.bucketmapjoin.sortedmerge=true;
   ```

   Explicación: Estas configuraciones permiten a Hive utilizar sort merge bucket joins, que pueden ser muy eficientes cuando las tablas están bucketeadas y ordenadas por la columna de join.

## 8. Casos de uso y patrones de diseño

Hive se utiliza en una variedad de escenarios de big data. Aquí se presentan algunos casos de uso comunes y patrones de diseño asociados.

### 8.1 ETL con Hive

Hive es ampliamente utilizado para procesos de Extract, Transform, Load (ETL).

Ejemplo de un proceso ETL simple:

```sql
-- Extraer datos de una tabla de origen
CREATE EXTERNAL TABLE datos_origen (
  id INT, 
  nombre STRING, 
  fecha_registro STRING
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
LOCATION '/path/to/source/data';

-- Transformar y cargar en una tabla de destino
CREATE TABLE datos_destino (
  id INT,
  nombre STRING,
  fecha_registro DATE
)
PARTITIONED BY (año INT, mes INT)
STORED AS ORC;

INSERT OVERWRITE TABLE datos_destino
PARTITION (año, mes)
SELECT 
  id,
  UPPER(nombre) as nombre,
  TO_DATE(fecha_registro) as fecha_registro,
  YEAR(TO_DATE(fecha_registro)) as año,
  MONTH(TO_DATE(fecha_registro)) as mes
FROM datos_origen;
```

Explicación: Este ejemplo muestra cómo extraer datos de una fuente externa, transformarlos (convirtiendo el nombre a mayúsculas y la fecha a un formato DATE) y cargarlos en una tabla de destino particionada y optimizada.

### 8.2 Data Warehousing

Hive es idoneo para construir data warehouses debido a su capacidad para manejar grandes volúmenes de datos y realizar análisis complejos.

Ejemplo de un esquema estrella simple:

```sql
-- Tabla de hechos
CREATE TABLE ventas_hechos (
  venta_id INT,
  producto_id INT,
  cliente_id INT,
  fecha_id INT,
  cantidad INT,
  monto DECIMAL(10,2)
)
PARTITIONED BY (año INT, mes INT)
CLUSTERED BY (producto_id) INTO 32 BUCKETS
STORED AS ORC;

-- Dimensión de producto
CREATE TABLE dim_producto (
  producto_id INT,
  nombre STRING,
  categoria STRING,
  precio DECIMAL(10,2)
)
STORED AS ORC;

-- Consulta analítica
SELECT 
  p.categoria,
  SUM(v.monto) as total_ventas
FROM ventas_hechos v
JOIN dim_producto p ON v.producto_id = p.producto_id
WHERE v.año = 2023
GROUP BY p.categoria
ORDER BY total_ventas DESC;
```

Explicación: Este ejemplo muestra un esquema estrella básico con una tabla de hechos y una dimensión. La consulta analítica demuestra cómo se pueden realizar análisis complejos sobre este esquema.

### 8.3 Análisis de logs y eventos

Hive es muy eficaz para analizar grandes volúmenes de logs y datos de eventos.

Ejemplo de análisis de logs de servidor web:

```sql
CREATE EXTERNAL TABLE logs (
  ip STRING,
  fecha STRING,
  metodo STRING,
  url STRING,
  status INT,
  size INT,
  referer STRING,
  user_agent STRING
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.RegexSerDe'
WITH SERDEPROPERTIES (
  "input.regex" = "^(\\S+) \\S+ \\S+ \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(\\S+) (\\S+) \\S+\" (\\d{3}) (\\d+) \"(\\S+)\" \"(.*)\"$"
)
LOCATION '/path/to/log/files';

-- Análisis de URLs más visitadas
SELECT url, COUNT(*) as visitas
FROM logs
WHERE status = 200
GROUP BY url
ORDER BY visitas DESC
LIMIT 10;
```

Explicación: Este ejemplo muestra cómo crear una tabla externa para analizar logs de servidor web utilizando un SerDe personalizado para parsear el formato de log. La consulta analítica encuentra las URLs más visitadas.

## 11. Códigos de prueba

Crea un archivo ```02_script.hql``` con tus consultas con el siguiente contenido para crear una base de datos con información ficitcia para realiza una consulta de prueba.

```sql
-- Configuraciones para mejorar la ejecución
SET hive.exec.mode.local.auto=true;
SET hive.fetch.task.conversion=more;

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS ventas_db;
USE ventas_db;

-- Crear tablas
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente INT,
    nombre STRING,
    email STRING,
    fecha_registro STRING
) STORED AS TEXTFILE;

CREATE TABLE IF NOT EXISTS productos (
    id_producto INT,
    nombre_producto STRING,
    categoria STRING,
    precio DOUBLE
) STORED AS TEXTFILE;

CREATE TABLE IF NOT EXISTS ventas (
    id_venta INT,
    id_cliente INT,
    id_producto INT,
    cantidad INT,
    fecha_venta STRING
) STORED AS TEXTFILE;

-- Insertar datos de ejemplo
INSERT INTO TABLE clientes VALUES
(1, 'Cliente1', 'cliente1@example.com', '2023-01-01');

INSERT INTO TABLE productos VALUES
(1, 'Producto1', 'Electrónica', 500.00);

INSERT INTO TABLE ventas VALUES
(1, 1, 1, 2, '2023-06-01');

-- Consulta simplificada
SELECT 
    p.categoria,
    SUM(v.cantidad * p.precio) as ventas_totales
FROM 
    ventas v
JOIN 
    productos p ON v.id_producto = p.id_producto
GROUP BY 
    p.categoria;
```

Ejecuta el script usando el comando:

```bash
beeline -u jdbc:hive2://localhost:10000 -n hive -p hive_password -f 02_script.hql 
```

**Nota importante**:

La ejecución del script Hive probablemente fallara debido a las limitaciones de recursos en el entorno local de bajos recursos que ha sido dockerizado.

```Error: Error while compiling statement: FAILED: Execution Error, return code 2 from org.apache.hadoop.hive.ql.exec.mr.MapRedTask (state=08S01,code=2)```

## 10. Ejercicios prácticos

### Ejercicio 1

Diseña e implementa un proceso ETL en Hive para cargar datos de ventas desde un archivo CSV a una tabla optimizada. La tabla de destino debe estar particionada por año y mes, y agrupada (bucketed) por ID de producto. Luego, realiza un análisis para obtener las ventas totales y el número de clientes únicos por mes.

### Ejercicio 2

Crea una User-Defined Function (UDF) en Java para extraer el nivel de log (ERROR, WARN, INFO) de mensajes de log de una aplicación. Utiliza esta UDF en Hive para analizar una tabla de logs y generar un resumen de la frecuencia de cada nivel de log.

### Ejercicio 3

Diseña un esquema en Hive para almacenar y analizar datos de posts de redes sociales en formato JSON, incluyendo información del usuario, contenido del post, etiquetas y métricas de engagement. Escribe consultas para identificar las etiquetas más populares y los usuarios más influyentes basándose en sus seguidores y likes promedio por post.
