# Soluciones a los ejercicios de Apache Flume

## 1. Ejercicios prácticos

### Ejercicio 1

Configura un agente Flume para leer tweets en tiempo real usando la API de Twitter y almacenarlos en HDFS.

### Ejercicio 2

Implementa un interceptor personalizado que filtre eventos basados en una palabra clave específica en el cuerpo del evento.

### Ejercicio 3

Diseña una topología Flume multi-agente para recopilar logs de múltiples servidores web, agregarlos, y almacenarlos en HBase.

## 2. Soluciones

### Ejercicio 1

```properties
# Nombre del agente
TwitterAgent.sources = Twitter
TwitterAgent.channels = MemChannel
TwitterAgent.sinks = HDFS

# Configuración de la fuente Twitter
TwitterAgent.sources.Twitter.type = org.apache.flume.source.twitter.TwitterSource
TwitterAgent.sources.Twitter.channels = MemChannel
TwitterAgent.sources.Twitter.consumerKey = [TU_CONSUMER_KEY]
TwitterAgent.sources.Twitter.consumerSecret = [TU_CONSUMER_SECRET]
TwitterAgent.sources.Twitter.accessToken = [TU_ACCESS_TOKEN]
TwitterAgent.sources.Twitter.accessTokenSecret = [TU_ACCESS_TOKEN_SECRET]
TwitterAgent.sources.Twitter.keywords = apache, hadoop, bigdata, flume

# Configuración del canal de memoria
TwitterAgent.channels.MemChannel.type = memory
TwitterAgent.channels.MemChannel.capacity = 10000
TwitterAgent.channels.MemChannel.transactionCapacity = 100

# Configuración del sink HDFS
TwitterAgent.sinks.HDFS.type = hdfs
TwitterAgent.sinks.HDFS.channel = MemChannel
TwitterAgent.sinks.HDFS.hdfs.path = /flume/tweets/%Y/%m/%d/%H
TwitterAgent.sinks.HDFS.hdfs.filePrefix = tweets-
TwitterAgent.sinks.HDFS.hdfs.round = true
TwitterAgent.sinks.HDFS.hdfs.roundValue = 60
TwitterAgent.sinks.HDFS.hdfs.roundUnit = minute
TwitterAgent.sinks.HDFS.hdfs.rollInterval = 3600
TwitterAgent.sinks.HDFS.hdfs.rollSize = 67108864
TwitterAgent.sinks.HDFS.hdfs.rollCount = 0
TwitterAgent.sinks.HDFS.hdfs.fileType = DataStream
```

### Ejercicio 2

```java
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import java.util.ArrayList;
import java.util.List;

public class KeywordFilterInterceptor implements Interceptor {
    private final String keyword;

    public KeywordFilterInterceptor(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void initialize() {
        // No se necesita inicialización
    }

    @Override
    public Event intercept(Event event) {
        if (event == null) {
            return null;
        }

        String body = new String(event.getBody());
        if (body.contains(keyword)) {
            return event;
        } else {
            return null;
        }
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        List<Event> interceptedEvents = new ArrayList<>(events.size());
        for (Event event : events) {
            Event interceptedEvent = intercept(event);
            if (interceptedEvent != null) {
                interceptedEvents.add(interceptedEvent);
            }
        }
        return interceptedEvents;
    }

    @Override
    public void close() {
        // No se necesitan recursos para liberar
    }

    public static class Builder implements Interceptor.Builder {
        private String keyword;

        @Override
        public Interceptor build() {
            return new KeywordFilterInterceptor(keyword);
        }

        @Override
        public void configure(Context context) {
            keyword = context.getString("keyword");
        }
    }
}
```

Configuración en Flume para usar el interceptor:

```properties
agent.sources.r1.interceptors = i1
agent.sources.r1.interceptors.i1.type = com.example.KeywordFilterInterceptor$Builder
agent.sources.r1.interceptors.i1.keyword = tuPalabraClave
```

### Ejercicio 3

```properties
# Configuración del Agente 1 (recopilador de logs del servidor web 1)
agent1.sources = weblog1
agent1.channels = memoryChannel
agent1.sinks = avroSink

agent1.sources.weblog1.type = exec
agent1.sources.weblog1.command = tail -F /var/log/apache2/access.log
agent1.sources.weblog1.channels = memoryChannel

agent1.channels.memoryChannel.type = memory
agent1.channels.memoryChannel.capacity = 10000
agent1.channels.memoryChannel.transactionCapacity = 100

agent1.sinks.avroSink.type = avro
agent1.sinks.avroSink.channel = memoryChannel
agent1.sinks.avroSink.hostname = aggregator.example.com
agent1.sinks.avroSink.port = 4545

# Configuración del Agente 2 (recopilador de logs del servidor web 2)
agent2.sources = weblog2
agent2.channels = memoryChannel
agent2.sinks = avroSink

agent2.sources.weblog2.type = exec
agent2.sources.weblog2.command = tail -F /var/log/apache2/access.log
agent2.sources.weblog2.channels = memoryChannel

agent2.channels.memoryChannel.type = memory
agent2.channels.memoryChannel.capacity = 10000
agent2.channels.memoryChannel.transactionCapacity = 100

agent2.sinks.avroSink.type = avro
agent2.sinks.avroSink.channel = memoryChannel
agent2.sinks.avroSink.hostname = aggregator.example.com
agent2.sinks.avroSink.port = 4545

# Configuración del Agente Agregador
aggregator.sources = avroSource
aggregator.channels = memoryChannel
aggregator.sinks = hbaseSink

aggregator.sources.avroSource.type = avro
aggregator.sources.avroSource.bind = 0.0.0.0
aggregator.sources.avroSource.port = 4545
aggregator.sources.avroSource.channels = memoryChannel

aggregator.channels.memoryChannel.type = memory
aggregator.channels.memoryChannel.capacity = 20000
aggregator.channels.memoryChannel.transactionCapacity = 200

aggregator.sinks.hbaseSink.type = hbase
aggregator.sinks.hbaseSink.channel = memoryChannel
aggregator.sinks.hbaseSink.table = weblogs
aggregator.sinks.hbaseSink.columnFamily = logs
aggregator.sinks.hbaseSink.serializer = org.apache.flume.sink.hbase.RegexHbaseEventSerializer
aggregator.sinks.hbaseSink.serializer.regex = ^([^ ]*) ([^ ]*) ([^ ]*) (-|\\[[^\\]]*\\]) ([^ \"]*|\"[^\"]*\") (-|[0-9]*) (-|[0-9]*)(?: ([^ \"]*|\"[^\"]*\") ([^ \"]*|\"[^\"]*\"))?$
aggregator.sinks.hbaseSink.serializer.colNames = ip,timestamp,method,url,status,size
```

## 3. Explicación detallada

### Ejercicio 1

Este ejercicio configura un agente Flume para recopilar tweets en tiempo real y almacenarlos en HDFS.

```properties
# Nombre del agente
TwitterAgent.sources = Twitter
TwitterAgent.channels = MemChannel
TwitterAgent.sinks = HDFS
```

Esta sección define el nombre del agente y sus componentes principales: una fuente (Twitter), un canal (MemChannel) y un sumidero (HDFS).

```properties
# Configuración de la fuente Twitter
TwitterAgent.sources.Twitter.type = org.apache.flume.source.twitter.TwitterSource
TwitterAgent.sources.Twitter.channels = MemChannel
TwitterAgent.sources.Twitter.consumerKey = [TU_CONSUMER_KEY]
TwitterAgent.sources.Twitter.consumerSecret = [TU_CONSUMER_SECRET]
TwitterAgent.sources.Twitter.accessToken = [TU_ACCESS_TOKEN]
TwitterAgent.sources.Twitter.accessTokenSecret = [TU_ACCESS_TOKEN_SECRET]
TwitterAgent.sources.Twitter.keywords = apache, hadoop, bigdata, flume
```

Esta parte configura la fuente de Twitter:

- Se especifica el tipo de fuente como `TwitterSource`.
- Se proporciona las credenciales de la API de Twitter (que deben ser reemplazadas con tus propias credenciales).
- Se define una lista de palabras clave para filtrar los tweets.

```properties
# Configuración del canal de memoria
TwitterAgent.channels.MemChannel.type = memory
TwitterAgent.channels.MemChannel.capacity = 10000
TwitterAgent.channels.MemChannel.transactionCapacity = 100
```

Esta sección configura un canal de memoria:

- Se establece la capacidad total del canal y la capacidad de transacción.

```properties
# Configuración del sink HDFS
TwitterAgent.sinks.HDFS.type = hdfs
TwitterAgent.sinks.HDFS.channel = MemChannel
TwitterAgent.sinks.HDFS.hdfs.path = /flume/tweets/%Y/%m/%d/%H
TwitterAgent.sinks.HDFS.hdfs.filePrefix = tweets-
TwitterAgent.sinks.HDFS.hdfs.round = true
TwitterAgent.sinks.HDFS.hdfs.roundValue = 60
TwitterAgent.sinks.HDFS.hdfs.roundUnit = minute
TwitterAgent.sinks.HDFS.hdfs.rollInterval = 3600
TwitterAgent.sinks.HDFS.hdfs.rollSize = 67108864
TwitterAgent.sinks.HDFS.hdfs.rollCount = 0
TwitterAgent.sinks.HDFS.hdfs.fileType = DataStream
```

Esta parte configura el sumidero HDFS:

- Se especifica la ruta en HDFS donde se almacenarán los tweets, usando una estructura de directorios basada en la fecha y hora.
- Se configura el prefijo de los archivos y las condiciones para crear nuevos archivos (basado en tiempo, tamaño, etc.).

### Ejercicio 2

Este ejercicio implementa un interceptor personalizado que filtra eventos basados en una palabra clave.

```java
public class KeywordFilterInterceptor implements Interceptor {
    private final String keyword;

    public KeywordFilterInterceptor(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public Event intercept(Event event) {
        if (event == null) {
            return null;
        }

        String body = new String(event.getBody());
        if (body.contains(keyword)) {
            return event;
        } else {
            return null;
        }
    }

    // ... otros métodos ...
}
```

Este interceptor:

- Implementa la interfaz `Interceptor` de Flume.
- En el método `intercept`, comprueba si el cuerpo del evento contiene la palabra clave.
- Si contiene la palabra clave, devuelve el evento; de lo contrario, devuelve null (lo que efectivamente filtra el evento).

```java
public static class Builder implements Interceptor.Builder {
    private String keyword;

    @Override
    public Interceptor build() {
        return new KeywordFilterInterceptor(keyword);
    }

    @Override
    public void configure(Context context) {
        keyword = context.getString("keyword");
    }
}
```

La clase `Builder`:

- Permite la configuración del interceptor a través del archivo de configuración de Flume.
- Lee la palabra clave del contexto de configuración.

Para usar este interceptor en Flume, se añade la siguiente configuración:

```properties
agent.sources.r1.interceptors = i1
agent.sources.r1.interceptors.i1.type = com.example.KeywordFilterInterceptor$Builder
agent.sources.r1.interceptors.i1.keyword = tuPalabraClave
```

### Ejercicio 3

Este ejercicio diseña una topología Flume multi-agente para recopilar y agregar logs de múltiples servidores web.

Configuración para los agentes recolectores (por ejemplo, Agente 1):

```properties
agent1.sources = weblog1
agent1.channels = memoryChannel
agent1.sinks = avroSink

agent1.sources.weblog1.type = exec
agent1.sources.weblog1.command = tail -F /var/log/apache2/access.log
agent1.sources.weblog1.channels = memoryChannel
```

Esta configuración:

- Define una fuente de tipo `exec` que ejecuta el comando `tail -F` para leer continuamente el archivo de log.
- Utiliza un canal de memoria para almacenar temporalmente los eventos.
- Configura un sumidero Avro para enviar los eventos al agente agregador.

Configuración para el agente agregador:

```properties
aggregator.sources = avroSource
aggregator.channels = memoryChannel
aggregator.sinks = hbaseSink

aggregator.sources.avroSource.type = avro
aggregator.sources.avroSource.bind = 0.0.0.0
aggregator.sources.avroSource.port = 4545
aggregator.sources.avroSource.channels = memoryChannel

aggregator.sinks.hbaseSink.type = hbase
aggregator.sinks.hbaseSink.channel = memoryChannel
aggregator.sinks.hbaseSink.table = weblogs
aggregator.sinks.hbaseSink.columnFamily = logs
aggregator.sinks.hbaseSink.serializer = org.apache.flume.sink.hbase.RegexHbaseEventSerializer
aggregator.sinks.hbaseSink.serializer.regex = ^([^ ]*) ([^ ]*) ([^ ]*) (-|\\[[^\\]]*\\]) ([^ \"]*|\"[^\"]*\") (-|[0-9]*) (-|[0-9]*)(?: ([^ \"]*|\"[^\"]*\") ([^ \"]*|\"[^\"]*\"))?$
aggregator.sinks.hbaseSink.serializer.colNames = ip,timestamp,method,url,status,size
```

Esta configuración del agente agregador:

- Define una fuente Avro que escucha en un puerto específico para recibir eventos de los agentes recolectores.
- Utiliza un canal de memoria para almacenar temporalmente los eventos recibidos.
- Configura un sumidero HBase para almacenar los logs agregados.
- Utiliza un serializador RegexHbase para parsear los logs y mapearlos a columnas específicas en HBase.

Esta topología permite recopilar logs de múltiples servidores web, agregarlos en un solo punto, y almacenarlos de manera estructurada en HBase para su posterior análisis.
