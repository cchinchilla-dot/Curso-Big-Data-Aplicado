# Guía práctica de HDFS (Hadoop Distributed File System)

En relación con los contenidos del curso, se corresponde con:

- Modulo 1:
  - Almacenamiento de datos masivo

- Modulo 2:
  - Sistemas de almacenamiento distribuidos.

## 1. Introducción a HDFS

HDFS (Hadoop Distributed File System) es un sistema de archivos distribuido diseñado para ejecutarse sobre hardware commodity. Su principal objetivo es gestionar grandes volúmenes de datos de manera eficiente, garantizando alta disponibilidad y tolerancia a fallos.

### Características principales

- **Escalabilidad masiva**: Capaz de manejar petabytes de datos.
- **Tolerancia a fallos**: Mantiene réplicas de los datos en diferentes nodos.
- **Alta disponibilidad**: Continúa funcionando incluso si algunos nodos fallan.
- **Optimización para grandes archivos**: Diseñado para leer y escribir grandes cantidades de datos.

## 2. Arquitectura de HDFS

### Componentes principales

1. **NameNode (Maestro)**:
   - Gestiona el espacio de nombres del sistema de archivos.
   - Mantiene un registro de los metadatos y la ubicación de los bloques de datos.
   - No almacena los datos de usuario, sino información sobre dónde están ubicados esos datos.

2. **DataNodes (Esclavos)**:
   - Almacenan los bloques de datos.
   - Ejecutan operaciones de lectura y escritura de datos bajo la dirección del NameNode.
   - Informan periódicamente al NameNode sobre las réplicas de bloques que almacenan.

3. **Secondary NameNode**:
   - Realiza puntos de control periódicos de los metadatos.
   - No es un respaldo del NameNode, pero ayuda en su recuperación en caso de fallo.

## 3. Comandos Básicos de HDFS

El usuario predeterminado del contenedor docker es ```root```.

### Operaciones básicas

#### Listar contenido en un directorio

```bash
hdfs dfs -ls /
```

Este comando lista el contenido del directorio raíz en HDFS. Es similar al comando ls en sistemas Unix.

#### Crear un nuevo directorio

```bash
hdfs dfs -mkdir -p /user/tu_usuario
```

Crea un nuevo directorio en HDFS. En este caso, crea un directorio con el nombre de tu usuario en la ruta ```/user```.

#### Copiar un archivo desde el sistema local a HDFS

```bash
hdfs dfs -put archivo_local.txt /user/tu_usuario/
```

Este comando copia un archivo desde tu sistema local a HDFS. archivo_local.txt es el nombre del archivo en tu sistema, y /user/tu_usuario/ es la ruta de destino en HDFS.

#### Leer el contenido de un archivo en HDFS

```bash
hdfs dfs -cat /user/tu_usuario/archivo_local.txt
```

Muestra el contenido de un archivo en HDFS directamente en la consola, similar al comando cat en sistemas Unix.

#### Descargar un archivo desde HDFS al sistema local

```bash
hdfs dfs -get /user/tu_usuario/archivo_local.txt archivo_local_copia.txt
```

Copia un archivo desde HDFS a tu sistema local. El primer argumento es la ruta del archivo en HDFS, el segundo es el nombre que tendrá en tu sistema local.

#### Mover archivos dentro de HDFS

```bash
hdfs dfs -mv /user/tu_usuario/archivo_local.txt /user/tu_usuario/subdirectorio/
```

Mueve un archivo de una ubicación a otra dentro de HDFS.

#### Eliminar archivos y directorios en HDFS

```bash
hdfs dfs -rm /user/tu_usuario/archivo_local.txt
hdfs dfs -rmdir /user/tu_usuario/subdirectorio/
```

El primer comando elimina un archivo, el segundo elimina un directorio vacío en HDFS.

## 4. Permisos y propietarios

### Cambiar permisos de un archivo

```bash
hdfs dfs -chmod 755 /user/tu_usuario/archivo_local.txt
```

### Cambiar propietario de un archivo

```bash
hdfs dfs -chown usuario:grupo /user/tu_usuario/archivo_local.txt
```

## 5. Información del sistema de archivos

### Ver información detallada de un archivo

```bash
hdfs dfs -stat %r,%o,%u,%g,%n /user/tu_usuario/archivo_local.txt
```

### Ver el uso del espacio en HDFS

```bash
hdfs dfs -du -h /user/tu_usuario
```

### Comprobar la salud del sistema de archivos

```bash
hdfs fsck /
```

## 6. Replicación y bloques

### Ver el factor de replicación de un archivo

```bash
hdfs dfs -stat %r /user/tu_usuario/archivo_local.txt
```

### Cambiar el factor de replicación de un archivo

```bash
hdfs dfs -setrep -w 2 /user/tu_usuario/archivo_local.txt
```

Nota importante: La opción -w (wait) en el comando hdfs dfs -setrep fuerza a HDFS a esperar hasta que todas las réplicas se completen antes de devolver el control a la terminal. Esto asegura que el comando no finalice hasta que el nuevo factor de replicación se haya aplicado completamente en todo el clúster.

### Ver información de los bloques de un archivo

```bash
hdfs fsck /user/tu_usuario/archivo_local.txt -files -blocks -locations
```

## 7. Monitoreo avanzado de HDFS

### Herramientas de monitoreo integradas

- **Interfaz web de Hadoop**: Accede a la interfaz web del NameNode para monitorear HDFS.
  
  ```text
  http://<NameNode-host>:9870
  ```

### Comandos de administración y diagnóstico

#### Informe de HDFS

```bash
hdfs dfsadmin -report
```

#### Comprobación de salud del sistema de archivos

```bash
hdfs fsck / -files -blocks -locations
```

#### Balanceo de datos en el clúster

```bash
hdfs balancer -threshold 10
```

#### Verificar la consistencia de los metadatos

```bash
hdfs namenode -metadataVersion
```

#### Analizar logs del NameNode

```bash
tail -f /var/log/hadoop/hdfs/hadoop-hdfs-namenode-*.log
```

## 8. Comandos avanzados de HDFS

### Concatenar archivos en HDFS

Puedes concatenar varios archivos en uno solo en HDFS:

```bash
hdfs dfs -cat /user/tu_usuario/archivo1.txt /user/tu_usuario/archivo2.txt | hdfs dfs -put - /user/tu_usuario/archivo_concatenado.txt
```

### Ver el espacio utilizado por los archivos en HDFS

```bash
hdfs dfs -du -s -h /user/tu_usuario/
```

### Compactar archivos pequeños en HDFS

Para mejorar la eficiencia, puedes compactar archivos pequeños en un solo archivo:

```bash
hdfs dfs -cat /user/tu_usuario/archivo_pequeño*.txt | hdfs dfs -put - /user/tu_usuario/archivo_grande.txt
```

### Listar los bloques y ubicaciones de un archivo

```bash
hdfs fsck /user/tu_usuario/archivo_grande.txt -files -blocks -locations
```

## 9. Descargar e incluir un dataset en HDFS

Para descargar un dataset de internet e incluirlo en HDFS, puedes usar el siguiente comando:

```bash
wget https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data -O iris.csv && hdfs dfs -put iris.csv /user/tu_usuario/datasets/
```

Este comando hace lo siguiente:

- wget descarga el archivo de datos Iris del repositorio UCI.
- -O iris.csv guarda el archivo descargado como "iris.csv".
- && ejecuta el siguiente comando si el anterior fue exitoso.
- hdfs dfs -put sube el archivo descargado a HDFS.
- /user/tu_usuario/datasets/ es la ruta de destino en HDFS donde se guardará el archivo.

## 10. Gestión de cuotas en HDFS

Las cuotas en HDFS permiten a los administradores controlar el uso del espacio y el número de archivos en directorios específicos. Hay dos tipos de cuotas en HDFS:

1. **Cuota de Espacio**: Limita la cantidad de espacio que puede ser utilizado.
2. **Cuota de Nombres**: Limita el número de archivos y directorios.

### Comandos para gestionar cuotas

#### Establecer una cuota de espacio

```bash
hdfs dfsadmin -setSpaceQuota <N> <directorio>
```

Donde `<N>` es el límite en bytes. Puedes usar sufijos como 'k', 'm', 'g', 't', 'p' para kilobytes, megabytes, etc.

Ejemplo:

```bash
hdfs dfsadmin -setSpaceQuota 10m /user/ejemplo
```

Esto establece una cuota de 10 megabytes para el directorio `/user/ejemplo`.

#### Establecer una cuota de nombres

```bash
hdfs dfsadmin -setQuota <N> <directorio>
```

Donde `<N>` es el número máximo de archivos y directorios permitidos.

Ejemplo:

```bash
hdfs dfsadmin -setQuota 1000 /user/ejemplo
```

Esto limita el directorio `/user/ejemplo` a un máximo de 1000 archivos y directorios.

### Eliminar cuotas

Para eliminar una cuota de espacio:

```bash
hdfs dfsadmin -clrSpaceQuota <directorio>
```

Para eliminar una cuota de nombres:

```bash
hdfs dfsadmin -clrQuota <directorio>
```

### Verificar Cuotas

Para ver las cuotas establecidas y el espacio utilizado:

```bash
hdfs dfs -count -q <directorio>
```

La salida mostrará: cuota de nombres, cuota de espacio restante, espacio disponible, número de nombres.

## 11. Ejercicios prácticos

### Ejercicio 1: Gestión básica de archivos en HDFS

1. Crea un directorio en HDFS llamado `/curso/ejercicio1`.
2. Sube un archivo de texto local a este directorio.
3. Cambia los permisos del archivo para que sólo el propietario pueda leer y escribir.
4. Mueve el archivo a un subdirectorio dentro de `ejercicio1`.
5. Elimina el archivo y el subdirectorio.

### Ejercicio 2: Replicación y comprobación de bloques

1. Sube un archivo grande (más de 128 MB) a HDFS.
2. Verifica el factor de replicación del archivo.
3. Cambia el factor de replicación a 2.
4. Utiliza `hdfs fsck` para verificar la distribución de bloques del archivo en los DataNodes.

### Ejercicio 3: Monitoreo y diagnóstico de HDFS

1. Accede a la interfaz web del NameNode para revisar el estado general de HDFS.
2. Ejecuta el comando `hdfs dfsadmin -report` y analiza la salida.
3. Utiliza `hdfs fsck` para analizar la salud del sistema de archivos en HDFS.
4. Configura una alerta simple para cuando el uso del espacio en HDFS supere el 80% (no es necesario que configures el cron, solo escribir el scrip).

### Ejercicio 4: Análisis de datos con el dataset Iris

1. Descarga el dataset Iris como se muestra en la sección 9.
2. Usa el comando hdfs dfs -cat para ver las primeras 10 líneas del dataset.
3. Crea una copia del dataset en HDFS con un factor de replicación de 3.
4. Usa hdfs fsck para verificar la distribución de los bloques de la copia.

### Ejercicio 5: Gestión de cuotas y espacio

1. Establece una cuota de 10 MB para el directorio /cuota_test.
2. Intenta subir un archivo que supere esta cuota y observa el resultado.
3. Usa hdfs dfs -count -q para verificar la cuota establecida y el espacio utilizado.
4. Elimina la cuota y vuelve a intentar subir el archivo.
