# Guía avanzada de Apache Flume

En relación con los contenidos del curso, se corresponde con:

- Módulo 2:
  - Herramientas: Flume.

## 1. Introducción a Apache Flume

Apache Flume es una herramienta distribuida y confiable para recopilar, agregar y mover eficientemente grandes cantidades de datos de registros (logs) desde muchas fuentes diferentes a un almacén de datos centralizado.

### Conceptos clave

1. **Event**
   Un evento es la unidad básica de datos que Flume transporta. Consiste en:
   - Un cuerpo (payload) de bytes
   - Un conjunto opcional de headers (pares clave-valor)

2. **Source**
   Una fuente consume eventos entregados a ella por una fuente externa (por ejemplo, servidor web) y los pasa al canal.

3. **Channel**
   Un canal es un almacén temporal para eventos. Los eventos son depositados en un canal por las fuentes y son removidos por los sinks.

4. **Sink**
   Un sink retira eventos del canal y los pasa al siguiente agente o al almacén de datos final.

5. **Agent**
   Un agente es un proceso (JVM) que hospeda los componentes de Flume (Source, Channel, Sink) y proporciona el contenedor para mover los datos de una fuente externa a un destino.

### Características clave de Flume

- **Fiabilidad**: Flume utiliza un modelo de transacciones para garantizar la entrega confiable de eventos.
- **Escalabilidad**: La arquitectura basada en streaming de datos permite escalar horizontalmente.
- **Extensibilidad**: Flume permite la creación de fuentes, sumideros y canales personalizados.
- **Flexibilidad**: Flume soporta una amplia variedad de fuentes y destinos, incluyendo HDFS, HBase, y más.

### Arquitectura de Flume

La arquitectura de Flume se basa en la composición de flujos de datos donde cada paso del flujo puede ser ajustado con la capacidad de retención y recuperación. Los principales componentes son:

1. **Client**: La entidad que genera los eventos y los envía a una o más fuentes de Flume.
2. **Agent**: Un proceso (JVM) que hospeda los componentes a través de los cuales fluyen los eventos.
3. **Source**: El componente que consume eventos entregados a él por un cliente externo o por otro agente.
4. **Channel**: El conducto entre la fuente y el sumidero; almacena los eventos hasta que son consumidos por el sumidero.
5. **Sink**: Remueve los eventos del canal y los pasa al próximo agente en el flujo o al destino final.

## 2. Configuración y Despliegue de Flume

### 2.1 Iniciar un agente Flume

Para iniciar un agente Flume:

```bash
flume-ng agent --conf conf --conf-file example.conf --name a1 -Dflume.root.logger=INFO,console
```

## 3. Componentes de Flume en detalle

### 3.1 Sources

Flume proporciona varios tipos de fuentes integradas:

1. **Avro Source**: Escucha eventos Avro en un puerto específico.

   ```properties
   a1.sources.r1.type = avro
   a1.sources.r1.bind = localhost
   a1.sources.r1.port = 4141
   ```

2. **Exec Source**: Ejecuta un comando dado y trata la salida como eventos.

   ```properties
   a1.sources.r1.type = exec
   a1.sources.r1.command = tail -F /var/log/messages
   ```

3. **Spooling Directory Source**: Monitorea un directorio para nuevos archivos y lee eventos de ellos.

   ```properties
   a1.sources.r1.type = spooldir
   a1.sources.r1.spoolDir = /var/log/apache/flumeSpool
   ```

### 3.2 Channels

Los canales actúan como el puente entre fuentes y sumideros:

1. **Memory Channel**: Almacena eventos en memoria.

   ```properties
   a1.channels.c1.type = memory
   a1.channels.c1.capacity = 10000
   a1.channels.c1.transactionCapacity = 1000
   ```

2. **File Channel**: Almacena eventos en el sistema de archivos para mayor durabilidad.

   ```properties
   a1.channels.c1.type = file
   a1.channels.c1.checkpointDir = /var/flume/checkpoint
   a1.channels.c1.dataDirs = /var/flume/data
   ```

### 3.3 Sinks

Flume ofrece varios tipos de sumideros para diferentes destinos:

1. **HDFS Sink**: Escribe eventos en HDFS.

   ```properties
   a1.sinks.k1.type = hdfs
   a1.sinks.k1.hdfs.path = /flume/events/%Y/%m/%d/%H
   a1.sinks.k1.hdfs.filePrefix = events-
   a1.sinks.k1.hdfs.rollInterval = 3600
   ```

2. **Avro Sink**: Envía eventos a un receptor Avro (generalmente otro agente Flume).

   ```properties
   a1.sinks.k1.type = avro
   a1.sinks.k1.hostname = 10.10.10.10
   a1.sinks.k1.port = 4545
   ```

3. **HBase Sink**: Escribe eventos en HBase.

   ```properties
   a1.sinks.k1.type = hbase
   a1.sinks.k1.table = flume_events
   a1.sinks.k1.columnFamily = cf
   ```

## 4. Flujos de datos complejos

### 4.1 Flujos de datos multi-agente

Flume permite crear topologías complejas conectando múltiples agentes:

```properties
# Agente 1
a1.sources = r1
a1.sinks = k1
a1.channels = c1

a1.sources.r1.type = netcat
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444

a1.sinks.k1.type = avro
a1.sinks.k1.hostname = localhost
a1.sinks.k1.port = 4545

a1.channels.c1.type = memory

# Agente 2
a2.sources = r1
a2.sinks = k1
a2.channels = c1

a2.sources.r1.type = avro
a2.sources.r1.bind = localhost
a2.sources.r1.port = 4545

a2.sinks.k1.type = hdfs
a2.sinks.k1.hdfs.path = /flume/events

a2.channels.c1.type = memory
```

### 4.2 Interceptores

Los interceptores permiten modificar o filtrar eventos en el flujo:

```properties
a1.sources.r1.interceptors = i1
a1.sources.r1.interceptors.i1.type = timestamp
```

Este interceptor añade una marca de tiempo a cada evento.

### 4.3 Channel Selectors

Los selectores de canal permiten enrutar eventos a diferentes canales basados en criterios específicos:

```properties
a1.sources.r1.selector.type = multiplexing
a1.sources.r1.selector.header = state
a1.sources.r1.selector.mapping.ca = c1
a1.sources.r1.selector.mapping.ny = c2
a1.sources.r1.selector.default = c3
```

## 5. Monitoreo y gestión

### 5.1 JMX Monitoring

Flume expone varias métricas a través de JMX:

```bash
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=54321
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
```

### 5.2 Logging

Flume utiliza log4j para logging. Puedes configurar el logging en `log4j.properties`:

```properties
flume.root.logger=INFO,LOGFILE
flume.log.dir=/var/log/flume
flume.log.file=flume.log
```

## 6. Optimización y ajuste de rendimiento

### 6.1 Ajuste del tamaño de lote

Ajustar el tamaño de lote puede mejorar significativamente el rendimiento:

```properties
a1.sinks.k1.hdfs.batchSize = 1000
```

### 6.2 Uso de múltiples canales y sumideros

Puedes mejorar el rendimiento utilizando múltiples canales y sumideros:

```properties
a1.channels = c1 c2
a1.sinks = k1 k2
a1.sinks.k1.channel = c1
a1.sinks.k2.channel = c2
```

### 6.3 Compresión

La compresión puede reducir significativamente el uso de ancho de banda y almacenamiento:

```properties
a1.sinks.k1.hdfs.compression.codec = snappy
```

## 7. Casos de uso y patrones de diseño

### 7.1 Recopilación de logs

Flume es ideal para recopilar logs de múltiples servidores:

```properties
a1.sources = r1
a1.channels = c1
a1.sinks = k1

a1.sources.r1.type = exec
a1.sources.r1.command = tail -F /var/log/messages
a1.sources.r1.channels = c1

a1.sinks.k1.type = hdfs
a1.sinks.k1.channel = c1
a1.sinks.k1.hdfs.path = /flume/logs/%Y/%m/%d
```

### 7.2 Streaming de datos en tiempo real

Flume puede ser usado para streaming de datos en tiempo real:

```properties
a1.sources = r1
a1.channels = c1
a1.sinks = k1

a1.sources.r1.type = avro
a1.sources.r1.bind = 0.0.0.0
a1.sources.r1.port = 4141
a1.sources.r1.channels = c1

a1.sinks.k1.type = hbase
a1.sinks.k1.channel = c1
a1.sinks.k1.table = streaming_data
a1.sinks.k1.columnFamily = cf
```

## 8. Ejercicios prácticos

### Ejercicio 1

Configura un agente Flume para leer tweets en tiempo real usando la API de Twitter y almacenarlos en HDFS.

### Ejercicio 2

Implementa un interceptor personalizado que filtre eventos basados en una palabra clave específica en el cuerpo del evento.

### Ejercicio 3

Diseña una topología Flume multi-agente para recopilar logs de múltiples servidores web, agregarlos, y almacenarlos en HBase.
