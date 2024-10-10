# Guía básica de Apache Oozie

En relación con los contenidos del curso, esta guía se corresponde con:

- Módulo 2:
  - Herramientas: Oozie.

Nota importante: Apache Oozie no se encuentra instalado en el entorno de pruebas de este curso. En su lugar, se ha optado por utilizar Apache Airflow como herramienta de orquestación de flujos de trabajo. Al final de esta guía, encontrará una sección comparativa entre Oozie y Airflow, explicando las razones por las que se prefiere Airflow en muchos entornos modernos. Para información práctica sobre la herramienta que utilizará en este curso, por favor refiérase a la sección de Apache Airflow.

## 1. Introducción a Apache Oozie

Apache Oozie es un sistema de programación y coordinación de flujos de trabajo para gestionar trabajos de Hadoop. Permite a los usuarios definir una serie de trabajos escritos en múltiples lenguajes (como MapReduce, Pig, Hive, etc.) y las dependencias entre estos trabajos.

### Conceptos clave

1. **Workflow**: Una secuencia de acciones (trabajos) organizadas en un grafo acíclico dirigido (DAG).
2. **Coordinator**: Un trabajo que se ejecuta periódicamente y puede lanzar flujos de trabajo basados en la disponibilidad de datos y tiempo.
3. **Bundle**: Una colección de coordinadores que se gestionan como una unidad.

### Características principales

- **Integración con Hadoop**: Se integra estrechamente con el ecosistema Hadoop.
- **Flujos de trabajo complejos**: Permite definir flujos de trabajo complejos con bifurcaciones y uniones.
- **Programación basada en tiempo**: Puede programar trabajos para que se ejecuten en intervalos regulares.
- **Dependencias de datos**: Puede iniciar trabajos basados en la disponibilidad de datos.
- **Recuperación y reinicio**: Proporciona capacidades de recuperación y reinicio para flujos de trabajo fallidos.

## 2. Arquitectura de Oozie

La arquitectura de Oozie consta de varios componentes clave:

1. **Oozie Server**: El componente central que maneja la ejecución de flujos de trabajo.
2. **Oozie Client**: Una interfaz de línea de comandos para interactuar con el servidor Oozie.
3. **Oozie WebApp**: Una interfaz web para monitorear y administrar trabajos de Oozie.
4. **Oozie Database**: Almacena metadatos sobre flujos de trabajo, coordinadores y bundles.

## 3. Definición de flujos de trabajo

Los flujos de trabajo en Oozie se definen utilizando XML. Aquí tienes un ejemplo básico:

```xml
<workflow-app xmlns="uri:oozie:workflow:0.5" name="simple-wf">
    <start to="hello-world"/>
    <action name="hello-world">
        <shell xmlns="uri:oozie:shell-action:0.3">
            <exec>echo</exec>
            <argument>Hello Oozie!</argument>
        </shell>
        <ok to="end"/>
        <error to="fail"/>
    </action>
    <kill name="fail">
        <message>Shell action failed, error message[${wf:errorMessage(wf:lastErrorNode())}]</message>
    </kill>
    <end name="end"/>
</workflow-app>
```

## 4. Coordinadores

Los coordinadores permiten la ejecución periódica de flujos de trabajo. Ejemplo:

```xml
<coordinator-app name="my-coord" frequency="${coord:days(1)}"
                 start="2023-01-01T00:00Z" end="2024-01-01T00:00Z" timezone="UTC"
                 xmlns="uri:oozie:coordinator:0.4">
    <action>
        <workflow>
            <app-path>${workflowAppUri}</app-path>
        </workflow>
    </action>
</coordinator-app>
```

## 5. Bundles

Los bundles agrupan múltiples coordinadores. Ejemplo:

```xml
<bundle-app name='my-bundle' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
            xmlns='uri:oozie:bundle:0.2'>
    <coordinator name='coord1'>
        <app-path>${nameNode}/user/${wf:user()}/coord1-app</app-path>
    </coordinator>
    <coordinator name='coord2'>
        <app-path>${nameNode}/user/${wf:user()}/coord2-app</app-path>
    </coordinator>
</bundle-app>
```

## 6. Ejecución y monitoreo

Para ejecutar un flujo de trabajo:

```bash
oozie job -run -config job.properties -oozie http://localhost:11000/oozie
```

Para monitorear un trabajo:

```bash
oozie job -info <job-id>
```

## 7. Comparativa: Oozie vs. Airflow

### Apache Oozie

**Ventajas:**

- Integración nativa con Hadoop
- Soporte robusto para flujos de trabajo basados en Hadoop
- Modelo de ejecución basado en push

**Desventajas:**

- Configuración basada en XML puede ser verbosa
- Curva de aprendizaje pronunciada
- Menos flexibilidad para flujos de trabajo no-Hadoop

### Apache Airflow

**Ventajas:**

- Configuración de flujos de trabajo en Python (DAGs)
- Mayor flexibilidad y extensibilidad
- Interfaz de usuario más moderna y amigable
- Amplio ecosistema de operadores y hooks
- Mejor soporte para flujos de trabajo no relacionados con Hadoop

**Desventajas:**

- Puede requerir más configuración para integrarse completamente con Hadoop
- Modelo de ejecución basado en pull

### ¿Por qué se prefiere Airflow?

1. **Flexibilidad**: Airflow no está limitado al ecosistema Hadoop y puede orquestar una amplia variedad de tareas y sistemas.

2. **Programación en Python**: Permite a los desarrolladores definir flujos de trabajo complejos utilizando un lenguaje de programación completo, en lugar de XML.

3. **Comunidad activa**: Airflow tiene una comunidad más grande y activa, lo que resulta en más recursos, plugins y actualizaciones frecuentes.

4. **Interfaz de usuario moderna**: Ofrece una interfaz web más intuitiva y rica en funciones para monitoreo y depuración.

5. **Escalabilidad**: Diseñado para escalar horizontalmente y manejar miles de tareas concurrentes.

6. **Adopción en la industria**: Muchas empresas tecnológicas líderes han adoptado Airflow, lo que ha impulsado su desarrollo y adopción.

7. **Operadores predefinidos**: Viene con una amplia gama de operadores listos para usar, facilitando la integración con diversos servicios y sistemas.
