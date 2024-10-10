
# Datasets for Big Data Projects

Este documento proporciona una descripción detallada de varios datasets grandes que son adecuados para proyectos de Big Data. Cada dataset incluye información sobre qué es, los campos que contiene, para qué puede ser utilizado y cómo descargarlo.

Antes de descargar los dataset crea un directorio para los datasets:

```bash
hdfs dfs -mkdir /datasets
```

## 1. Enron Email Dataset

### Descripción

El Enron Email Dataset es una colección masiva de correos electrónicos de ejecutivos de Enron, que es ampliamente utilizado para análisis de texto y procesamiento de lenguaje natural.

### Campos

- **Message-ID**: Identificador único del correo electrónico.
- **Date**: Fecha y hora de envío del correo.
- **From**: Dirección de correo del remitente.
- **To**: Dirección de correo del destinatario.
- **Subject**: Asunto del correo.
- **Body**: Cuerpo del mensaje del correo.

### Usos

- Análisis de redes sociales.
- Procesamiento de lenguaje natural.
- Clasificación de correos electrónicos.
- Detección de fraude.

### Cómo descargarlo

```bash
wget https://www.cs.cmu.edu/~enron/enron_mail_20150507.tar.gz -O enron_mail.tgz
hdfs dfs -put enron_mail.tgz /datasets
```

## 2. Wikipedia Dump (Inglés)

### Descripción

El volcado de Wikipedia contiene un archivo masivo de texto de toda la Wikipedia en inglés. Es útil para realizar análisis de texto a gran escala, minería de datos, y entrenar modelos de procesamiento de lenguaje natural.

### Campos

- **Título del artículo**: Nombre del artículo de Wikipedia.
- **Texto del artículo**: Contenido completo del artículo.
- **Enlaces**: Enlaces a otros artículos dentro de Wikipedia.
- **Metadatos**: Información adicional como fecha de modificación, autores, etc.

### Usos

- Análisis de texto.
- Minería de datos.
- Entrenamiento de modelos de lenguaje natural.
- Análisis semántico.

### Cómo descargarlo

```bash
wget https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2 -O enwiki-latest-pages-articles.xml.bz2
hdfs dfs -put enwiki-latest-pages-articles.xml.bz2 /datasets
```

Aquí tienes una lista ampliada que incluye los datasets que solicitaste junto con uno más del **UCI Machine Learning Repository**.

## 4. IMDB Movie Reviews Dataset

### Descripción

El dataset de reseñas de películas de IMDB contiene una gran cantidad de comentarios sobre películas de usuarios en IMDB, y es comúnmente utilizado para análisis de sentimiento y modelos de clasificación de texto.

### Campos

- **Review**: La reseña del usuario sobre la película.
- **Sentiment**: Clasificación de la reseña en "positiva" o "negativa".

### Usos

- Análisis de sentimiento.
- Clasificación de texto.
- Procesamiento de lenguaje natural.

### Cómo descargarlo

```bash
wget http://ai.stanford.edu/~amaas/data/sentiment/aclImdb_v1.tar.gz -O aclImdb.tar.gz
hdfs dfs -put aclImdb.tar.gz /datasets
```

---

## 5. UCI Machine Learning Repository – Bank Marketing Dataset

### Descripción

Este dataset de la UCI contiene datos de campañas de marketing de un banco portugués. Es usado frecuentemente para modelos de predicción y clasificación.

### Campos

- **Age**: Edad del cliente.
- **Job**: Tipo de trabajo del cliente.
- **Marital**: Estado civil del cliente.
- **Education**: Nivel educativo del cliente.
- **Balance**: Balance de la cuenta bancaria del cliente.
- **Target**: Si el cliente suscribió un depósito a plazo fijo (yes/no).

### Usos

- Modelos de clasificación.
- Modelos de predicción.
- Análisis de marketing.

### Cómo descargarlo

```bash
wget https://archive.ics.uci.edu/ml/machine-learning-databases/00222/bank.zip -O bank.zip
unzip bank.zip
hdfs dfs -put bank /datasets
```

---

Puedes encontrar más dataset en <https://archive.ics.uci.edu/> y <https://www.kaggle.com/>.
