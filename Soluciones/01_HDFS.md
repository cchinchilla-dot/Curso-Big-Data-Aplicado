
# Guía Práctica de HDFS (Hadoop Distributed File System)

## 1. Ejercicios prácticos

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

## 2. Soluciones

### Ejercicio 1: Gestión básica de archivos en HDFS

#### Crear un directorio

```bash
cd ~ && pwd
```

```bash
hdfs dfs -mkdir -p /curso/ejercicio1
```

#### Subir un archivo

```bash
echo "Este es un archivo de prueba para el ejercicio en HDFS." > archivo_local.txt
hdfs dfs -put archivo_local.txt /curso/ejercicio1/
```

#### Comprobar el archivo

```bash
hdfs dfs -ls /curso/ejercicio1
```

#### Cambiar permisos

```bash
hdfs dfs -chmod 600 /curso/ejercicio1/archivo_local.txt
```

#### Mover el archivo

```bash
hdfs dfs -mkdir -p /curso/ejercicio1/apartado1
hdfs dfs -mv /curso/ejercicio1/archivo_local.txt /curso/ejercicio1/apartado1
```

#### Eliminar archivo y subdirectorio

```bash
hdfs dfs -rm /curso/ejercicio1/apartado1/archivo_local.txt
hdfs dfs -rmdir /curso/ejercicio1/apartado1
```

### Ejercicio 2: Replicación y comprobación de bloques

#### Descargar archivo grande

```bash
wget https://download.documentfoundation.org/libreoffice/stable/24.8.0/deb/x86_64/LibreOffice_24.8.0_Linux_x86-64_deb.tar.gz -O archivo_grande.dat
```

#### Mover el fichero

```bash
hdfs dfs -put archivo_grande.dat /curso/ejercicio1
```

#### Verificar factor de replicación

```bash
hdfs dfs -stat %r /curso/ejercicio1/archivo_grande.dat
```

#### Cambiar factor de replicación

```bash
hdfs dfs -setrep 2 /curso/ejercicio1/archivo_grande.dat
```

#### Verificar distribución de bloques

```bash
hdfs fsck /curso/ejercicio1/archivo_grande.dat -files -blocks -locations
```

## Ejercicio 3: Monitoreo y diagnóstico de HDFS

### Acceder a la interfaz web

Abrir navegador y visitar `http://<NameNode-host>:9870`

### Ejecutar reporte

```bash
hdfs dfsadmin -report
```

### Analizar salud del sistema

```bash
hdfs fsck /
```

### Configurar alerta (ejemplo básico usando df y mail)

#### 1. Crear el script usando `cat`

- Abre una terminal.
- Usa el siguiente comando `cat` para crear el archivo `hdfs_alert.sh`:

```bash
cat << 'EOF' > /root/hdfs_alert.sh
#!/bin/bash
USAGE=$(hdfs dfs -df -h / | awk 'NR==2 {print $3}' | sed 's/%//')
if [ $USAGE -gt 80 ]; then
  echo "HDFS usage is above 80%" | mail -s "HDFS Alert" admin@example.com
fi
EOF
 ```

- Haz que el script sea ejecutable:

```bash
chmod +x /root/hdfs_alert.sh
```

#### 2. Explicación del script

- `hdfs dfs -df -h /`: Obtiene el uso de HDFS.
- `awk 'NR==2 {print $3}'`: Selecciona la línea que contiene el porcentaje de uso.
- `sed 's/%//'`: Elimina el símbolo `%` del valor.
- `if [ $USAGE -gt 80 ]; then`: Comprueba si el uso es mayor que 80.
- `mail -s "HDFS Alert" admin@example.com`: Envía un correo de alerta si el uso es mayor al 80%.

## Ejercicio 4: Análisis de datos con el dataset Iris

### Descargar dataset

```bash
hdfs dfs -mkdir /datasets
wget https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data -O iris.csv && hdfs dfs -put iris.csv /datasets
```

### Ver primeras 10 líneas

```bash
hdfs dfs -cat /datasets/iris.csv | head -n 10
```

### Crear copia con factor de replicación 3

```bash
hdfs dfs -cp /datasets/iris.csv /datasets/iris_replicated.csv
hdfs dfs -setrep 3 /datasets/iris_replicated.csv
```

### Verificar distribución de bloques

```bash
hdfs fsck /datasets/iris_replicated.csv -files -blocks -locations
```

## Ejercicio 5: Gestión de cuotas y espacio

### Establecer cuota

```bash
hdfs dfs -mkdir /cuota_test
hdfs dfsadmin -setSpaceQuota 10M /cuota_test
```

### Intentar subir archivo que supere la cuota

```bash
dd if=/dev/zero of=archivo_grande bs=1M count=15
hdfs dfs -put archivo_grande /cuota_test/
```

Este comando fallará debido a la cuota.

### Verificar cuota

```bash
hdfs dfs -count -q /cuota_test
```

### Eliminar cuota y subir archivo

```bash
hdfs dfsadmin -clrSpaceQuota /cuota_test
hdfs dfs -put archivo_grande /cuota_test
```

Ahora el archivo se subirá correctamente.
