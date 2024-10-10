# Guía avanzada de Apache Sqoop

En relación con los contenidos del curso, se corresponde con:

- Módulo 2:
  - Herramientas: Sqoop.

## 1. Introducción a Apache Sqoop

Apache Sqoop es una herramienta diseñada para transferir eficientemente datos en masa entre Apache Hadoop y bases de datos estructuradas como bases de datos relacionales. El nombre "Sqoop" es una contracción de "SQL to Hadoop".

### Conceptos clave

1. **Importación**
   El proceso de transferir datos desde una base de datos relacional (RDBMS) a Hadoop (HDFS, Hive, HBase).

2. **Exportación**
   El proceso de transferir datos desde Hadoop hacia una base de datos relacional.

3. **Conectores**
   Componentes que permiten a Sqoop interactuar con diferentes sistemas de bases de datos.

4. **Job**
   Una tarea de Sqoop que define una operación de importación o exportación.

### Características clave de Sqoop

- **Paralelismo**: Sqoop puede realizar operaciones de transferencia de datos en paralelo para mejorar el rendimiento.
- **Carga incremental**: Permite importar solo los datos nuevos o actualizados desde la última importación.
- **Integración con el ecosistema Hadoop**: Se integra bien con otras herramientas como Hive, HBase, y Oozie.
- **Seguridad**: Soporta varios mecanismos de seguridad, incluyendo Kerberos.

### Arquitectura de Sqoop

Sqoop utiliza un enfoque de MapReduce para la transferencia de datos, lo que le permite aprovechar el paralelismo de Hadoop:

1. **Cliente Sqoop**: Analiza los argumentos de línea de comandos y configura los jobs.
2. **Conector de base de datos**: Interactúa con la base de datos específica.
3. **Hadoop MapReduce**: Ejecuta los jobs de importación/exportación.
4. **HDFS/Hive/HBase**: Almacena o lee los datos transferidos.

## 2. Ejecución de Sqoop

### 2.1 Ejecución básica

Para ejecutar un comando Sqoop, simplemente escribe `sqoop` seguido del comando y los argumentos necesarios:

```bash
sqoop import --connect jdbc:mysql://localhost/mydatabase --table employees --username myuser --password mypassword
```

### 2.2 Ejecución con archivo de configuración

Puedes guardar los argumentos comunes en un archivo de configuración y referenciarlo en la ejecución:

1. Crea un archivo `sqoop.properties`:

```properties
connect=jdbc:mysql://localhost/mydatabase
username=myuser
password=mypassword
```

2. Ejecuta Sqoop usando este archivo:

```bash
sqoop import --options-file sqoop.properties --table employees
```

### 2.3 Ejecución de jobs guardados

Sqoop permite guardar jobs para su ejecución posterior:

1. Crear un job:

```bash
sqoop job --create myjob -- import --connect jdbc:mysql://localhost/mydatabase --table employees --username myuser --password mypassword
```

2. Ejecutar el job guardado:

```bash
sqoop job --exec myjob
```

### 2.4 Ejecución en un cluster Hadoop

Para ejecutar Sqoop en un cluster Hadoop, asegúrate de que Sqoop esté instalado en todos los nodos y que las variables de entorno estén configuradas correctamente. Luego, puedes ejecutar los comandos desde cualquier nodo del cluster.

### 2.5 Ejecución con Hadoop client

Si estás usando un Hadoop client para conectarte a un cluster remoto, asegúrate de que tu `core-site.xml` y `hdfs-site.xml` estén configurados correctamente, y luego ejecuta Sqoop como lo harías normalmente.

### 2.7 Depuración de ejecuciones de Sqoop

Para obtener más información sobre lo que está haciendo Sqoop, puedes aumentar el nivel de logging:

```bash
sqoop import -Dsqoop.root.logger=DEBUG,console ...
```

Sqoop utiliza MapReduce para realizar sus operaciones, por lo que también puedes monitorear los jobs de MapReduce en la interfaz web de YARN si estás utilizando un cluster Hadoop.

## 3. Operaciones básicas de Sqoop

### 3.1 Importación

Importar una tabla completa:

```bash
sqoop import --connect jdbc:mysql://localhost/mydatabase --table employees --username myuser --password mypassword
```

### 3.2 Exportación

Exportar datos desde HDFS a una tabla de base de datos:

```bash
sqoop export --connect jdbc:mysql://localhost/mydatabase --table employees --export-dir /user/hive/warehouse/employees --input-fields-terminated-by '\t'
```

### 3.3 Listado de bases de datos y tablas

Listar todas las bases de datos:

```bash
sqoop list-databases --connect jdbc:mysql://localhost/ --username myuser --password mypassword
```

Listar todas las tablas en una base de datos:

```bash
sqoop list-tables --connect jdbc:mysql://localhost/mydatabase --username myuser --password mypassword
```

## 4. Importación avanzada

### 4.1 Importación selectiva

Importar solo ciertas columnas:

```bash
sqoop import --connect jdbc:mysql://localhost/mydatabase --table employees --columns "id,name,salary" --username myuser --password mypassword
```

Importar basado en una consulta:

```bash
sqoop import --connect jdbc:mysql://localhost/mydatabase --query 'SELECT * FROM employees WHERE $CONDITIONS AND salary > 50000' --split-by id --target-dir /user/hive/warehouse/high_salary_employees --username myuser --password mypassword
```

### 4.2 Importación incremental

Importación incremental basada en una columna de marca de tiempo:

```bash
sqoop import --connect jdbc:mysql://localhost/mydatabase --table employees --check-column last_modified --incremental lastmodified --last-value "2023-01-01" --username myuser --password mypassword
```

### 4.3 Importación a Hive

Importar directamente a una tabla Hive:

```bash
sqoop import --connect jdbc:mysql://localhost/mydatabase --table employees --hive-import --create-hive-table --hive-table employees_hive --username myuser --password mypassword
```

## 5. Exportación avanzada

### 5.1 Exportación selectiva

Exportar solo ciertas columnas:

```bash
sqoop export --connect jdbc:mysql://localhost/mydatabase --table employees --columns "id,name,salary" --export-dir /user/hive/warehouse/employees --input-fields-terminated-by '\t' --username myuser --password mypassword
```

### 5.2 Actualización de registros existentes

Usar el modo de actualización para registros existentes:

```bash
sqoop export --connect jdbc:mysql://localhost/mydatabase --table employees --update-key id --update-mode allowinsert --export-dir /user/hive/warehouse/employees --input-fields-terminated-by '\t' --username myuser --password mypassword
```

## 6. Conectores de Sqoop

Sqoop proporciona varios conectores para diferentes bases de datos:

- Conector genérico JDBC
- Conector MySQL
- Conector PostgreSQL
- Conector Oracle
- Conector Microsoft SQL Server

Ejemplo de uso del conector Oracle:

```bash
sqoop import --connect jdbc:oracle:thin:@//localhost:1521/XE --table employees --username myuser --password mypassword --connection-manager org.apache.sqoop.manager.OracleManager
```

## 7. Seguridad en Sqoop

### 7.1 Autenticación Kerberos

Para usar Sqoop con Kerberos:

```bash
sqoop import -Dhadoop.security.credential.provider.path=jceks://hdfs/user/sqoop/mysql.password.jceks --connect jdbc:mysql://localhost/mydatabase --table employees --username myuser
```

### 7.2 Almacenamiento seguro de contraseñas

Crear un almacén de contraseñas:

```bash
hadoop credential create mysql.password -provider jceks://hdfs/user/sqoop/mysql.password.jceks -v mypassword
```

## 8. Optimización de rendimiento

### 8.1 Ajuste del número de mappers

Controlar el paralelismo ajustando el número de mappers:

```bash
sqoop import --connect jdbc:mysql://localhost/mydatabase --table employees --username myuser --password mypassword -m 8
```

### 8.2 Compresión

Habilitar la compresión para reducir el uso de almacenamiento y red:

```bash
sqoop import --connect jdbc:mysql://localhost/mydatabase --table employees --username myuser --password mypassword --compress --compression-codec org.apache.hadoop.io.compress.SnappyCodec
```

## 9. Integración con otras herramientas

### 9.1 Integración con Oozie

Ejemplo de job de Oozie para ejecutar Sqoop:

```xml
<workflow-app xmlns="uri:oozie:workflow:0.5" name="sqoop-import-wf">
    <start to="sqoop-node"/>
    <action name="sqoop-node">
        <sqoop xmlns="uri:oozie:sqoop-action:0.2">
            <job-tracker>${jobTracker}</job-tracker>
            <name-node>${nameNode}</name-node>
            <command>import --connect jdbc:mysql://localhost/mydatabase --table employees --username myuser --password mypassword</command>
        </sqoop>
        <ok to="end"/>
        <error to="fail"/>
    </action>
    <kill name="fail">
        <message>Sqoop job failed, error message[${wf:errorMessage(wf:lastErrorNode())}]</message>
    </kill>
    <end name="end"/>
</workflow-app>
```

### 9.2 Integración con Hive y HBase

Importar datos directamente a HBase:

```bash
sqoop import --connect jdbc:mysql://localhost/mydatabase --table employees --hbase-table employees --column-family personal_data --hbase-row-key id --username myuser --password mypassword
```

## 10. Casos de uso y patrones de diseño

### 10.1 ETL con Sqoop y Hive

1. Importar datos desde RDBMS a HDFS usando Sqoop
2. Crear una tabla externa en Hive sobre los datos importados
3. Transformar los datos usando HiveQL
4. Exportar los datos transformados de vuelta a RDBMS usando Sqoop

### 10.2 Sincronización de datos

Usar importación incremental para mantener una copia actualizada de los datos de RDBMS en Hadoop:

```bash
sqoop job --create incremental_import -- import --connect jdbc:mysql://localhost/mydatabase --table employees --check-column last_modified --incremental lastmodified --last-value "2023-01-01" --username myuser --password mypassword
```

Programar este job para que se ejecute periódicamente.

## 11. Ejercicios prácticos

### Ejercicio 1

Configura un job de Sqoop para importar datos de una tabla de clientes desde MySQL a HDFS, transformarlos usando Hive, y luego exportar los resultados a PostgreSQL.

### Ejercicio 2

Implementa un proceso de importación incremental para una tabla de transacciones, asegurándote de manejar correctamente las actualizaciones y las nuevas inserciones.

### Ejercicio 3

Diseña un flujo de trabajo en Oozie que incluya múltiples jobs de Sqoop para sincronizar datos entre un data warehouse relacional y un data lake en Hadoop.
