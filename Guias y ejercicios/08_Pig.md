# Guía avanzada de Apache Pig

En relación con los contenidos del curso, se corresponde con:

- Modulo 2:
  - Herramientas: Pig.
  - Herramientas: Consultas Pig.

## 1. Introducción a Apache Pig

Apache Pig es una plataforma para analizar grandes conjuntos de datos que consiste en un lenguaje de alto nivel para expresar programas de análisis de datos, junto con la infraestructura para evaluar estos programas.

### Conceptos clave

1. **Pig Latin**

   Pig Latin es el lenguaje de programación para Apache Pig. Es un lenguaje de flujo de datos que permite a los usuarios describir cómo los datos deben ser cargados, transformados y almacenados.

2. **Relaciones y tuplas**

   En Pig, los datos se organizan en relaciones, que son colecciones de tuplas. Una tupla es una colección ordenada de campos.

3. **Lazy Evaluation**

   Pig utiliza evaluación perezosa, lo que significa que las operaciones no se ejecutan hasta que se necesitan los resultados.

### Características clave de Pig

- **Facilidad de programación**: Pig Latin es similar a SQL, lo que lo hace accesible para usuarios familiarizados con bases de datos relacionales.
- **Optimización automática**: Pig optimiza automáticamente los planes de ejecución.
- **Extensibilidad**: Los usuarios pueden crear sus propias funciones para realizar procesamiento personalizado.
- **Manejo de datos no estructurados**: Pig puede trabajar con datos que no tienen un esquema predefinido.

### Arquitectura de Pig

1. **Parser**: Comprueba la sintaxis y realiza el análisis semántico del script Pig Latin.
2. **Optimizer**: Realiza optimizaciones lógicas y físicas en el plan de ejecución.
3. **Compiler**: Convierte el script optimizado en una serie de jobs de MapReduce.
4. **Execution Engine**: Ejecuta los jobs de MapReduce en el cluster Hadoop.

### Estado actual y alternativas

Es importante notar que, aunque Pig sigue siendo una herramienta muy utilizada, su uso ha disminuido en los últimos años en favor de otras tecnologías. Hay varias razones para esto:

1. **Evolución del ecosistema Big Data**: Con la aparición de herramientas más modernas y versátiles, Pig ha perdido popularidad.

2. **Curva de aprendizaje**: Aunque Pig Latin es relativamente fácil de aprender, requiere aprender un nuevo lenguaje específico.

3. **Rendimiento**: Herramientas más nuevas como Spark ofrecen mejor rendimiento, especialmente para procesamiento en memoria.

4. **Ecosistema más amplio**: Alternativas como Hive y Spark tienen ecosistemas más grandes y activos, con más integraciones y herramientas de soporte.

5. **Mantenimiento**: El desarrollo activo de Pig ha disminuido en comparación con otras herramientas del ecosistema Hadoop.

Alternativas preferidas:

- **Apache Hive**: Ofrece un lenguaje similar a SQL (HiveQL) que es más familiar para muchos usuarios. Hive se integra bien con otras herramientas del ecosistema Hadoop y tiene un buen rendimiento para consultas analíticas.

- **Apache Spark y Spark SQL**: Spark ofrece procesamiento en memoria, lo que resulta en un rendimiento significativamente mejor para muchos tipos de trabajos. Spark SQL proporciona una interfaz SQL que es familiar y potente.

- **Presto**: Para consultas interactivas, Presto ofrece un rendimiento excelente y soporta una variedad de fuentes de datos.

Aunque Pig ya no es la herramienta preferida para muchos casos de uso, sigue siendo relevante en ciertos escenarios, especialmente en organizaciones que ya tienen una inversión significativa en scripts y flujos de trabajo de Pig.

## 2. Ejecución de scripts en Pig

### 2.1 Modos de ejecución

Pig puede ejecutarse en dos modos:

1. **Modo local**: Pig se ejecuta en una única JVM, útil para pruebas con conjuntos de datos pequeños.
2. **Modo MapReduce**: Pig se ejecuta en un cluster Hadoop, adecuado para grandes conjuntos de datos.

### 2.2 Ejecución de scripts

#### 2.2.1 Usando Grunt (shell interactivo de Pig)

1. Inicia Grunt:

   ```bash
   pig
   ```

2. Ejecuta comandos Pig Latin:

   ```pig
   A = LOAD 'data.txt' AS (name:chararray, age:int);
   B = FILTER A BY age >= 18;
   DUMP B;
   ```

#### 2.2.2 Ejecución en modo batch

1. Crea un archivo script.pig con tus comandos Pig Latin.

2. Ejecuta el script:

   ```bash
   pig script.pig
   ```

## 3. Fundamentos de Pig Latin

### 3.1 Tipos de datos

Pig soporta los siguientes tipos de datos:

- int
- long
- float
- double
- chararray
- bytearray
- boolean
- datetime
- biginteger
- bigdecimal

Tipos complejos:

- tuple
- bag
- map

### 3.2 Operaciones básicas

1. **LOAD**: Carga datos en una relación.

   ```pig
   A = LOAD 'data.txt' AS (name:chararray, age:int);
   ```

2. **FILTER**: Selecciona tuplas que cumplen una condición.

   ```pig
   B = FILTER A BY age >= 18;
   ```

3. **FOREACH**: Genera datos a partir de cada tupla de una relación.

   ```pig
   C = FOREACH A GENERATE name, age+1 AS next_year_age;
   ```

4. **GROUP**: Agrupa datos basados en una o más expresiones.

   ```pig
   D = GROUP A BY age;
   ```

5. **JOIN**: Combina dos o más relaciones.

   ```pig
   E = JOIN A BY name, B BY customer_name;
   ```

6. **ORDER**: Ordena una relación basada en uno o más campos.

   ```pig
   F = ORDER A BY age DESC;
   ```

7. **LIMIT**: Limita el número de tuplas en una relación.

   ```pig
   G = LIMIT A 10;
   ```

8. **STORE**: Guarda los resultados en el sistema de archivos.

   ```pig
   STORE G INTO 'output';
   ```

### 3.3 Funciones incorporadas

Pig proporciona numerosas funciones incorporadas para facilitar el procesamiento de datos:

1. **Funciones de evaluación**
   - AVG, COUNT, MAX, MIN, SUM

2. **Funciones de cadena**
   - CONCAT, SUBSTRING, LOWER, UPPER, TRIM

3. **Funciones de fecha**
   - CurrentTime, DaysBetween, ToDate

4. **Funciones matemáticas**
   - ABS, CEIL, FLOOR, ROUND, LOG, EXP

## 4. Procesamiento avanzado con Pig

### 4.1 UDFs (User-Defined Functions)

Las UDFs permiten extender las capacidades de Pig con funciones personalizadas.

Ejemplo en Java:

```java
package com.example;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class ToUpperCase extends EvalFunc<String> {
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0)
            return null;
        String str = (String)input.get(0);
        return str.toUpperCase();
    }
}
```

Uso en Pig:

```pig
REGISTER myudfs.jar;
A = LOAD 'data.txt' AS (name:chararray);
B = FOREACH A GENERATE com.example.ToUpperCase(name);
```

### 4.2 Manejo de datos complejos

Pig es especialmente útil para trabajar con datos anidados y complejos.

```pig
-- Supongamos que tenemos datos JSON
A = LOAD 'data.json' USING JsonLoader('name:chararray, friends:{(name:chararray, age:int)}');

-- Aplanar la estructura anidada
B = FOREACH A GENERATE name, FLATTEN(friends);

-- Agrupar y contar
C = GROUP B BY name;
D = FOREACH C GENERATE group, COUNT(B) AS friend_count;
```

### 4.3 Optimización de rendimiento

1. **Paralelismo**: Ajusta el número de reducers.

   ```pig
   SET default_parallel 20;
   ```

2. **Particionamiento**: Usa particionamiento para mejorar el rendimiento de los joins.

   ```pig
   A = LOAD 'data1' AS (key, value1);
   B = LOAD 'data2' AS (key, value2);
   C = JOIN A BY key, B BY key USING 'skewed';
   ```

3. **Uso de ORC o Parquet**: Almacena los datos en formatos columnar para mejorar el rendimiento de lectura.

   ```pig
   STORE A INTO 'output' USING OrcStorage();
   ```

## 5. Integración con otros sistemas

### 5.1 Pig y Hive

Pig puede leer y escribir tablas de Hive usando HCatalog.

```pig
A = LOAD 'hive_table' USING org.apache.hive.hcatalog.pig.HCatLoader();
-- Procesar datos
STORE B INTO 'hive_output_table' USING org.apache.hive.hcatalog.pig.HCatStorer();
```

### 5.2 Pig y sistemas de archivos externos

Pig puede trabajar con datos almacenados en sistemas como Amazon S3.

```pig
A = LOAD 's3://my-bucket/data' USING PigStorage(',');
```

## 6. Casos de uso y patrones de diseño

### 6.1 ETL con Pig

Pig es excelente para procesos de Extract, Transform, Load (ETL).

```pig
-- Extraer
raw_data = LOAD 'input' AS (id:int, name:chararray, date:chararray, value:float);

-- Transformar
cleaned_data = FOREACH raw_data GENERATE 
    id, 
    UPPER(name) AS name, 
    ToDate(date, 'yyyy-MM-dd') AS date, 
    (value IS NULL ? 0 : value) AS value;

-- Cargar
STORE cleaned_data INTO 'output' USING PigStorage(',');
```

### 6.2 Análisis de logs

Pig es muy eficaz para analizar grandes volúmenes de logs.

```pig
-- Cargar logs
logs = LOAD 'webserver_logs' AS (ip:chararray, timestamp:chararray, request:chararray, status:int, size:int);

-- Filtrar errores
errors = FILTER logs BY status >= 400;

-- Agrupar por código de error
error_counts = GROUP errors BY status;
error_summary = FOREACH error_counts GENERATE group AS status, COUNT(errors) AS count;

-- Ordenar y almacenar resultados
ordered_summary = ORDER error_summary BY count DESC;
STORE ordered_summary INTO 'error_analysis';
```

## 7. Ejercicios prácticos

### Ejercicio 1

Diseña e implementa un proceso ETL en Pig para cargar datos de ventas desde un archivo CSV, realizar algunas transformaciones (por ejemplo, convertir fechas, calcular totales) y luego almacenar los resultados en un formato optimizado como ORC o Parquet.

### Ejercicio 2

Crea una UDF en Java para realizar una operación personalizada (por ejemplo, un cálculo complejo o una limpieza de datos específica). Utiliza esta UDF en un script de Pig para procesar un conjunto de datos.

### Ejercicio 3

Desarrolla un script de Pig para analizar un conjunto de datos de redes sociales. El script debe cargar los datos, realizar algunas agregaciones (por ejemplo, contar posts por usuario, calcular promedios de engagement) y producir un resumen de los usuarios más activos e influyentes.

 