# Curso Big Data Aplicado

Este README te guiará en la construcción y configuración de un entorno de Big Data utilizando Docker. A lo largo del curso, instalarás y configurarás diversas herramientas y servicios clave, como Hadoop, Spark, Hive, Sqoop, Airflow, entre otros. El objetivo es que adquieras conocimientos prácticos y profundices en el fascinante mundo del Big Data. ¡Disfruta del proceso de aprendizaje!

## Contenidos del curso

Este curso ha sido diseñado en base a los contenidos de la asignatura Big Data Aplicado, conforme al Real Decreto 279/2021, de 20 de abril, que regula el Curso de Especialización en Inteligencia Artificial y Big Data, estableciendo los aspectos básicos del currículo.

## Objetivo

Este curso está orientado específicamente para la formación del profesorado de la asignatura Big Data Aplicado. La formación se impartió en septiembre de 2024, con el propósito de proporcionar a los docentes las competencias necesarias para enseñar el uso de estas herramientas y tecnologías en el ámbito educativo.

## Nota importante

Algunas instalaciones, herramientas, contenidos y ejercicios han sido simplificados para ajustarse a los requisitos del curso y mejorar la experiencia de aprendizaje. El entorno Dockerizado que se configurará durante el curso no está diseñado para su uso en entornos productivos, y debe emplearse únicamente con fines educativos y formativos.

---
## Guía de configuración y verificación del entorno Big Data

### Introducción a Docker

Docker es una plataforma de virtualización a nivel de sistema operativo que facilita la creación, distribución y ejecución de aplicaciones en contenedores. Los contenedores son unidades estandarizadas de software que empaquetan el código y todas sus dependencias, permitiendo que la aplicación se ejecute de manera rápida y confiable en diferentes entornos informáticos.

### Tecnologías incluidas en el entorno Docker

Este entorno Docker incluye las siguientes tecnologías esenciales para Big Data:

- **Apache Hadoop**: Framework de software para almacenamiento y procesamiento distribuido de grandes conjuntos de datos.
  - **HDFS (Hadoop Distributed File System)**: Sistema de archivos distribuido.
  - **MapReduce**: Modelo de programación para procesamiento de datos a gran escala.

- **Apache Spark**: Motor de análisis unificado para procesamiento de datos a gran escala.
  - **Spark Core**: Motor de ejecución distribuida.
  - **Spark SQL**: Módulo para trabajar con datos estructurados.
  - **MLlib**: Biblioteca de aprendizaje automático.

- **Apache Hive**: Data warehouse que facilita la lectura, escritura y gestión de grandes conjuntos de datos en almacenamiento distribuido usando SQL.

- **Apache Pig**: Plataforma para analizar grandes conjuntos de datos que consta de un lenguaje de alto nivel (Pig Latin) para expresar programas de análisis de datos.

- **Apache Sqoop**: Herramienta diseñada para transferir eficientemente datos entre Hadoop y bases de datos relacionales.

- **Apache Flume**: Servicio distribuido para recopilar, agregar y mover eficientemente grandes cantidades de datos de registro.

- **Apache ZooKeeper**: Servicio centralizado para mantener información de configuración, nomenclatura, proporcionar sincronización distribuida y servicios grupales.

- **Apache Airflow**: Plataforma para programar y monitorear flujos de trabajo.

- **Jupyter Notebook**: Aplicación web de código abierto que permite crear y compartir documentos que contienen código en vivo, ecuaciones, visualizaciones y texto narrativo.

- **MySQL**: Sistema de gestión de bases de datos relacionales.

- **Ganglia**: Sistema de monitorización distribuido escalable para sistemas de cómputo de alto rendimiento.

### 1. Construcción y ejecución del contenedor Docker

#### Comando

```bash
docker build -t bigdata-course .
```

### 2. Proceso de configuración del entorno

#### Comando

Macos y Linux:

```bash
docker run -it -p 8888:8888 -p 9870:9870 -p 8080:8080 -v $(pwd):/workspace bigdata-course
```

Windows:

```bash
docker run -it -p 8888:8888 -p 9870:9870 -p 8080:8080 -v RUTA-ABSOLUTA:/workspace bigdata-course
```

Para ejecutar el comando Docker en Windows, sigue estos pasos:

- Identifica la ruta completa de la carpeta descargada: Primero, localiza la carpeta en tu sistema donde tienes los archivos que deseas montar en el contenedor Docker. Asegúrate de que el nombre de la carpeta no contenga espacios, ya que esto podría causar problemas al ejecutar el comando.
- Reemplaza ```RUTA-ABSOLUTA``` en el comando: Sustituye ```RUTA-ABSOLUTA``` con la ruta completa de la carpeta que identificaste en el paso anterior.
- Ejemplo: Si la carpeta está en el escritorio y se llama bigdata, la ruta completa podría ser algo como ```C:\Users\TuUsuario\Desktop\bigdata```
Ejecuta el comando en tu terminal de Windows: Abre la terminal de Windows (puede ser PowerShell, Símbolo del sistema, o la terminal de Docker) y ejecuta el siguiente comando:

```bash
docker run -it -p 8888:8888 -p 9870:9870 -p 8080:8080 -v C:\Users\TuUsuario\Desktop\bigdata:/workspace bigdata-course
```

Nota importante: Asegúrate de que la ruta no contenga espacios. Si la ruta tiene espacios y no puedes evitarlo, encierra la ruta completa entre comillas dobles (").

#### Accesos

- **Airflow UI**: [http://localhost:8080](http://localhost:8080) (usuario: `admin`, contraseña: `admin`)
- **Jupyter Notebook**: [http://localhost:8888](http://localhost:8888) (sin token requerido)

### 3. Ejecución del script de verificación

#### Comando

```bash
/home/hdfs/verify_environment.sh
```

### Nota

En caso de que la línea de comandos se quede atascada durante alguna descarga, puedes utilizar Ctrl + C para detener el proceso. Después de detenerlo, vuelve a ejecutar el comando para continuar con la configuración.

Si experimentas errores persistentes, puedes utilizar el siguiente comando para limpiar el sistema Docker y liberar espacio, eliminando contenedores, volúmenes, imágenes y redes no utilizados:

```bash
docker system prune --all --force --volumes
```

Este comando eliminará todos los recursos que no están siendo utilizados por los contenedores activos. Úsalo con precaución, ya que se eliminarán todos los volúmenes y datos asociados.

---

## Aviso de derechos de autor y descargo de responsabilidad

### Derechos de autor

El contenido de este repositorio y todos los archivos incluidos en él son propiedad intelectual de Carlos Chinchilla Corbacho. Todos los derechos están reservados.

Queda estrictamente prohibida la copia, modificación, distribución, venta, publicación o cualquier otro uso, total o parcial, de este material sin la autorización expresa por escrito de Carlos Chinchilla Corbacho.

### Descargo de responsabilidad

El código y las instrucciones proporcionadas en este proyecto se ofrecen "tal cual", sin garantía de ningún tipo, expresa o implícita. El autor no se hace responsable de ningún daño directo, indirecto, incidental, especial, ejemplar o consecuente (incluidos, entre otros, la adquisición de bienes o servicios sustitutos, la pérdida de uso, datos o beneficios, o la interrupción del negocio) sin importar su causa, ni de ninguna teoría de responsabilidad, ya sea por contrato, responsabilidad estricta o agravio (incluyendo negligencia o de otra manera) que surja de cualquier manera del uso de este software o documentación, incluso si se advierte de la posibilidad de tales daños.

El usuario asume todos los riesgos y responsabilidades asociados con el uso de este material y se compromete a utilizarlo de acuerdo con todas las leyes y regulaciones aplicables.

© 2024 Carlos Chinchilla Corbacho. Todos los derechos reservados.

---
