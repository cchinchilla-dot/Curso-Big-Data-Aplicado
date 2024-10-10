# Guía avanzada de Apache Sqoop: Ejercicios prácticos

## 1. Ejercicios prácticos

### Ejercicio 1

Configura un job de Sqoop para importar datos de una tabla de clientes desde MySQL a HDFS, transformarlos usando Hive, y luego exportar los resultados a PostgreSQL.

### Ejercicio 2

Implementa un proceso de importación incremental para una tabla de transacciones, asegurándote de manejar correctamente las actualizaciones y las nuevas inserciones.

### Ejercicio 3

Diseña un flujo de trabajo en Oozie que incluya múltiples jobs de Sqoop para sincronizar datos entre un data warehouse relacional y un data lake en Hadoop.

## 2. Soluciones

### Ejercicio 1

1. Importar datos de clientes desde MySQL a HDFS:

```bash
sqoop import \
  --connect jdbc:mysql://localhost/retail_db \
  --username retail_user \
  --password retail_password \
  --table customers \
  --target-dir /user/hive/warehouse/retail_db.db/customers \
  --fields-terminated-by ',' \
  --lines-terminated-by '\n' \
  --hive-import \
  --create-hive-table \
  --hive-table retail_db.customers
```

2. Transformar datos usando Hive:

```sql
CREATE TABLE retail_db.customers_transformed AS
SELECT 
  customer_id,
  CONCAT(customer_fname, ' ', customer_lname) AS full_name,
  customer_email,
  customer_city,
  customer_state
FROM retail_db.customers;
```

3. Exportar resultados a SQL Server:

```bash
sqoop export \
  --connect jdbc:sqlserver://localhost;databaseName=retail_dw \
  --username retail_dw_user \
  --password retail_dw_password \
  --table customers_dim \
  --export-dir /user/hive/warehouse/retail_db.db/customers_transformed \
  --input-fields-terminated-by ',' \
  --input-lines-terminated-by '\n' \
  --columns "customer_id,full_name,email,city,state"
```

### Ejercicio 2

1. Configurar la importación incremental:

```bash
sqoop job --create incremental_import -- import \
  --connect jdbc:mysql://localhost/retail_db \
  --username retail_user \
  --password retail_password \
  --table transactions \
  --target-dir /user/hive/warehouse/retail_db.db/transactions \
  --check-column transaction_date \
  --incremental lastmodified \
  --last-value "2023-01-01 00:00:00" \
  --merge-key transaction_id
```

2. Ejecutar la importación incremental:

```bash
sqoop job --exec incremental_import
```

3. Actualizar el valor de last-value después de cada importación:

```bash
last_value=$(hdfs dfs -cat /user/hive/warehouse/retail_db.db/transactions/.lastmodified 2>/dev/null)
sqoop job --update incremental_import -- \
  --last-value "$last_value"
```

### Ejercicio 3

Crear un workflow de Oozie (`workflow.xml`):

```xml
<workflow-app xmlns="uri:oozie:workflow:0.5" name="data-sync-workflow">
    <start to="import-customers"/>
    
    <action name="import-customers">
        <sqoop xmlns="uri:oozie:sqoop-action:0.2">
            <job-tracker>${jobTracker}</job-tracker>
            <name-node>${nameNode}</name-node>
            <command>import --connect jdbc:mysql://localhost/retail_db --table customers --target-dir ${customersDir} --username ${dbUser} --password ${dbPassword}</command>
        </sqoop>
        <ok to="import-orders"/>
        <error to="fail"/>
    </action>
    
    <action name="import-orders">
        <sqoop xmlns="uri:oozie:sqoop-action:0.2">
            <job-tracker>${jobTracker}</job-tracker>
            <name-node>${nameNode}</name-node>
            <command>import --connect jdbc:mysql://localhost/retail_db --table orders --target-dir ${ordersDir} --username ${dbUser} --password ${dbPassword}</command>
        </sqoop>
        <ok to="export-summary"/>
        <error to="fail"/>
    </action>
    
    <action name="export-summary">
        <sqoop xmlns="uri:oozie:sqoop-action:0.2">
            <job-tracker>${jobTracker}</job-tracker>
            <name-node>${nameNode}</name-node>
            <command>export --connect jdbc:postgresql://localhost/retail_dw --table order_summary --export-dir ${summaryDir} --username ${dwUser} --password ${dwPassword}</command>
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

Crear un archivo de propiedades (`job.properties`):

```properties
nameNode=hdfs://localhost:8020
jobTracker=localhost:8032
queueName=default

oozie.use.system.libpath=true
oozie.wf.application.path=${nameNode}/user/${user.name}/sqoop-workflow

customersDir=/user/hive/warehouse/retail_db.db/customers
ordersDir=/user/hive/warehouse/retail_db.db/orders
summaryDir=/user/hive/warehouse/retail_db.db/order_summary

dbUser=retail_user
dbPassword=retail_password
dwUser=retail_dw_user
dwPassword=retail_dw_password
```

Ejecutar el workflow:

```bash
oozie job -run -config job.properties
```

## Explicación detallada

### Ejercicio 1

Este ejercicio demuestra un flujo de trabajo ETL completo utilizando Sqoop y Hive.

1. **Importación de MySQL a HDFS**:
   - Utilizamos Sqoop para importar la tabla `customers` de MySQL a HDFS.
   - La opción `--hive-import` crea automáticamente una tabla Hive.

2. **Transformación con Hive**:
   - Creamos una nueva tabla Hive que combina el nombre y apellido en un campo `full_name`.
   - Esta transformación podría incluir más lógica de negocio según sea necesario.

3. **Exportación a SQL Server**:
   - Usamos Sqoop para exportar los datos transformados a una tabla en SQL Server.
   - La opción `--columns` asegura que los campos se mapeen correctamente.

### Ejercicio 2

Este ejercicio muestra cómo configurar y ejecutar una importación incremental.

1. **Configuración del job**:
   - Usamos `--check-column` para especificar la columna que se usará para detectar nuevos o actualizados registros.
   - `--incremental lastmodified` indica que estamos usando una columna de tipo timestamp.
   - `--merge-key` es crucial para manejar actualizaciones correctamente.

2. **Ejecución del job**:
   - Simplemente ejecutamos el job creado anteriormente.

3. **Actualización del último valor**:
   - Después de cada ejecución, actualizamos el `--last-value` para la próxima ejecución.
   - Esto asegura que solo importemos nuevos o actualizados registros en futuras ejecuciones.

### Ejercicio 3

Este ejercicio demuestra cómo orquestar múltiples jobs de Sqoop usando Oozie.

1. **Workflow XML**:
   - Define tres acciones de Sqoop: importar clientes, importar órdenes, y exportar un resumen.
   - Cada acción tiene un camino de éxito y de error.

2. **Archivo de propiedades**:
   - Contiene todas las variables necesarias para el workflow.
   - Incluye rutas HDFS, credenciales de base de datos, etc.

3. **Ejecución del workflow**:
   - Usamos el comando `oozie job -run` para iniciar el workflow.

Este enfoque permite una sincronización de datos automatizada y programada entre sistemas relacionales y Hadoop, manejando múltiples tablas y operaciones en un solo flujo de trabajo.
