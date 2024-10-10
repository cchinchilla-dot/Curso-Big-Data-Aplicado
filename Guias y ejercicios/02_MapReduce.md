# Guía básica de MapReduce

En relación con los contenidos del curso, se corresponde con:

- Módulo 1:
  - Procesamiento de datos.

- Módulo 2:
  - Herramienta: MapReduce.
  
## 1. Introducción a MapReduce

MapReduce es un modelo de programación y un framework de procesamiento diseñado para procesar y generar grandes conjuntos de datos en sistemas de computación distribuidos. Fue desarrollado por Google y posteriormente implementado en proyectos de código abierto como Apache Hadoop.

### Características clave

- Procesamiento distribuido de datos
- Escalabilidad horizontal
- Tolerancia a fallos
- Procesamiento en paralelo
- Abstracción de la complejidad de sistemas distribuidos

### Arquitectura básica

- **JobTracker**: Coordina la ejecución de los trabajos MapReduce (en Hadoop 1.x).
- **TaskTracker**: Ejecuta las tareas individuales de Map y Reduce (en Hadoop 1.x).
- **ResourceManager**: Gestiona los recursos del clúster (en YARN/Hadoop 2.x+).
- **NodeManager**: Gestiona los recursos y ejecuta las tareas en cada nodo (en YARN/Hadoop 2.x+).

## 2. Ejecución de MapReduce

Para ejecutar un trabajo MapReduce en un clúster Hadoop, sigue estos pasos:

1. **Preparación del código**: Compila tu código Java y empaquétalo en un archivo JAR.

```bash
javac -classpath `hadoop classpath` WordCount.java
jar cf wc.jar WordCount*.class
```

2. **Preparación de los datos**: Asegúrate de que tus datos de entrada estén en HDFS.

```bash
hadoop fs -put input_file.txt /user/hadoop/input/
```

3. **Ejecución del trabajo**: Usa el comando `hadoop jar` para ejecutar tu trabajo.

```bash
hadoop jar wc.jar WordCount /user/hadoop/input /user/hadoop/output
```

4. **Monitoreo**: Puedes monitorear el progreso de tu trabajo usando la interfaz web de Hadoop (generalmente en `http://namenode:8088`).

5. **Recuperación de resultados**: Una vez completado el trabajo, recupera los resultados de HDFS.

```bash
hadoop fs -get /user/hadoop/output ./local_output
```

### 2.1 Ejecución en modo local

Para desarrollo y pruebas, puedes ejecutar MapReduce en modo local:

1. Configura la propiedad `mapreduce.framework.name` a `local` en tu `Configuration`.

```java
Configuration conf = new Configuration();
conf.set("mapreduce.framework.name", "local");
```

2. Ejecuta tu programa Java normalmente, asegurándote de que todas las dependencias de Hadoop estén en el classpath.

## 3. Conceptos fundamentales de MapReduce

### 3.1 Fase Map

La fase Map procesa los datos de entrada en pares clave-valor y produce un conjunto intermedio de pares clave-valor.

```java
public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(line);
        while (tokenizer.hasMoreTokens()) {
            word.set(tokenizer.nextToken());
            context.write(word, one);
        }
    }
}
```

### 3.2 Fase Shuffle y Sort

Esta fase intermedia organiza y agrupa los datos producidos por los mappers antes de enviarlos a los reducers.

### 3.3 Fase Reduce

La fase Reduce combina todos los valores intermedios asociados con la misma clave intermedia.

```java
public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
    public void reduce(Text key, Iterable<IntWritable> values, Context context) 
            throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        context.write(key, new IntWritable(sum));
    }
}
```

## 4. Implementación de MapReduce en Hadoop

### 4.1 Configuración del Job

```java
public class WordCount {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(Map.class);
        job.setCombinerClass(Reduce.class);
        job.setReducerClass(Reduce.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
```

### 4.2 Tipos de datos en Hadoop

Hadoop proporciona sus propios tipos de datos para optimizar la serialización y comparación:

- **Text**: Para cadenas de texto
- **IntWritable**, **LongWritable**, **DoubleWritable**: Para valores numéricos
- **BooleanWritable**: Para valores booleanos
- **ArrayWritable**: Para arrays

## 5. Técnicas avanzadas de MapReduce

### 5.1 Combiner

El Combiner es una optimización que realiza una reducción local en el nodo del Mapper antes de enviar los datos a través de la red.

```java
job.setCombinerClass(Reduce.class);
```

### 5.2 Partitioner

El Partitioner define cómo se distribuyen las claves entre los Reducers.

```java
public static class CustomPartitioner extends Partitioner<Text, IntWritable> {
    @Override
    public int getPartition(Text key, IntWritable value, int numPartitions) {
        return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
    }
}

// En la configuración del job
job.setPartitionerClass(CustomPartitioner.class);
```

### 5.3 Secondary Sort

La técnica de Secondary Sort permite ordenar los valores para una clave dada en la fase de Reduce.

```java
public static class CompositeKey implements WritableComparable<CompositeKey> {
    private Text naturalKey;
    private IntWritable secondaryKey;

    // Implementar métodos write, readFields y compareTo
}

public static class GroupingComparator extends WritableComparator {
    protected GroupingComparator() {
        super(CompositeKey.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        CompositeKey k1 = (CompositeKey)a;
        CompositeKey k2 = (CompositeKey)b;
        return k1.getNaturalKey().compareTo(k2.getNaturalKey());
    }
}

// En la configuración del job
job.setGroupingComparatorClass(GroupingComparator.class);
```

## 6. Optimización de trabajos MapReduce

### 6.1 Compresión de datos

La compresión puede reducir significativamente el uso de disco y red.

```java
// Habilitar compresión de salida
FileOutputFormat.setCompressOutput(job, true);
FileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
```

### 6.2 Reuso de objetos

Reutilizar objetos en el Mapper y Reducer puede mejorar el rendimiento al reducir la creación de objetos.

```java
public static class ReuseMap extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        // Reutilizar 'word' en lugar de crear un nuevo objeto Text cada vez
        word.set(value.toString());
        context.write(word, one);
    }
}
```

### 6.3 Combiner

El uso de un Combiner puede reducir significativamente la cantidad de datos transferidos entre las fases Map y Reduce.

```java
job.setCombinerClass(Reduce.class);
```

## 7. Patrones de diseño en MapReduce

### 7.1 Patrón de suma

Utilizado para calcular sumas o promedios.

### 7.2 Patrón de filtrado

Para filtrar registros basados en ciertos criterios.

### 7.3 Patrón de unión (Join)

Para combinar datos de múltiples fuentes.

```java
public static class JoinMapper extends Mapper<LongWritable, Text, Text, Text> {
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] parts = value.toString().split(",");
        context.write(new Text(parts[0]), new Text(parts[1] + "," + parts[2]));
    }
}

public static class JoinReducer extends Reducer<Text, Text, Text, Text> {
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        // Lógica para combinar los valores asociados con la misma clave
    }
}
```

## 8. Ejemplo práctico: Análisis de logs

Supongamos que tenemos logs de acceso a un sitio web y queremos contar el número de visitas por IP.

```java
public class LogAnalysis {
    public static class LogMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text ip = new Text();

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] parts = value.toString().split("\\s+");
            if (parts.length > 0) {
                ip.set(parts[0]);
                context.write(ip, one);
            }
        }
    }

    public static class LogReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        public void reduce(Text key, Iterable<IntWritable> values, Context context) 
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "log analysis");
        job.setJarByClass(LogAnalysis.class);
        job.setMapperClass(LogMapper.class);
        job.setCombinerClass(LogReducer.class);
        job.setReducerClass(LogReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
```

## 9. Comparación con otros frameworks

### 9.1 MapReduce vs. Spark

- **Procesamiento**: MapReduce es orientado a disco, Spark es orientado a memoria.
- **Velocidad**: Spark generalmente es más rápido para operaciones iterativas.
- **Facilidad de uso**: Spark ofrece APIs más intuitivas y de alto nivel.
- **Ecosistema**: MapReduce es parte del ecosistema Hadoop, Spark tiene su propio ecosistema.

### 9.2 MapReduce vs. Flink

- **Modelo de procesamiento**: MapReduce es batch, Flink soporta tanto batch como streaming.
- **Latencia**: Flink ofrece procesamiento de baja latencia.
- **Optimización**: Flink tiene un optimizador de consultas más avanzado.

### 9.3 Justificación de la preferencia de Spark sobre MapReduce

Aunque MapReduce fue revolucionario, Spark ha ganado popularidad y es a menudo preferido por varias razones:

1. **Rendimiento**:
   - Spark opera principalmente en memoria, lo que puede hacerlo hasta 100 veces más rápido que MapReduce para ciertas operaciones.
   - MapReduce escribe resultados intermedios al disco, lo que introduce latencia.

2. **Facilidad de uso**:
   - Spark ofrece APIs de alto nivel en varios lenguajes (Scala, Java, Python, R).
   - MapReduce requiere más código boilerplate y es más difícil de escribir y mantener.

3. **Versatilidad**:
   - Spark soporta una variedad de cargas de trabajo: batch processing, streaming, machine learning, y procesamiento de grafos.
   - MapReduce está principalmente orientado al procesamiento batch.

4. **Procesamiento iterativo**:
   - Spark es mucho más eficiente para algoritmos iterativos (como machine learning) debido a su capacidad de mantener datos en memoria.
   - MapReduce debe leer y escribir en disco en cada iteración, lo que lo hace ineficiente para tales tareas.

5. **Ecosistema**:
   - Spark tiene un ecosistema rico que incluye SparkSQL, MLlib, GraphX y Spark Streaming.
   - Aunque Hadoop tiene un ecosistema amplio, muchas de sus herramientas (como Hive) ahora soportan Spark como motor de ejecución.

6. **Latencia**:
   - Spark puede proporcionar resultados casi en tiempo real para ciertas aplicaciones.
   - MapReduce tiene una mayor latencia debido a su naturaleza orientada a disco.

7. **Desarrollo activo**:
   - Spark tiene un desarrollo más activo y frecuentes actualizaciones.
   - Aunque MapReduce sigue siendo mantenido, su desarrollo es menos activo.

8. **Compatibilidad**:
   - Spark puede trabajar con datos almacenados en HDFS, lo que permite una transición suave desde entornos MapReduce existentes.

Sin embargo, MapReduce sigue siendo relevante en ciertos escenarios:

- Cuando se trabaja con conjuntos de datos extremadamente grandes que no caben en memoria.
- En entornos donde la estabilidad y la madurez son críticas.
- Cuando se requiere procesamiento batch puro y no se necesitan capacidades adicionales.
