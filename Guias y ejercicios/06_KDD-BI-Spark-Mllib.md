# KDD, Inteligencia de Negocios y Spark MLlib

En relación con los contenidos del curso, se corresponde con:

- Módulo 5:
  - Modelos de Inteligencia de negocios.
  - Proceso del modelo KDD (Knowledge Discovery in Databases).
  - Etapas: Selección, limpieza, transformación de datos, minería de datos, interpretación y evaluación de datos.
  - Implantación de modelos de inteligencia de negocios BI.
  - Técnicas de validación de modelos BI.

## 1. Knowledge Discovery in Databases (KDD)

### 1.1 Definición y Propósito

Knowledge Discovery in Databases (KDD) es un proceso sistemático y multidisciplinario para identificar patrones válidos, novedosos, potencialmente útiles y comprensibles a partir de conjuntos de datos complejos y de gran volumen. El objetivo principal del KDD es transformar datos brutos en información accionable.

### 1.2 Etapas del Proceso KDD

1. **Selección**: Identificar las fuentes de datos relevantes y extraer el conjunto de datos objetivo.
2. **Preprocesamiento**: Limpiar los datos, manejar valores faltantes, eliminar ruido y resolver inconsistencias.
3. **Transformación**: Convertir los datos en un formato adecuado para la minería, incluyendo la reducción de dimensionalidad y la transformación de atributos.
4. **Minería de Datos**: Aplicar métodos inteligentes para extraer patrones de los datos.
5. **Interpretación/Evaluación**: Identificar los patrones verdaderamente interesantes que representan conocimiento basado en medidas de interés.

### 1.3 Importancia del KDD

El KDD es crucial en la era del big data, ya que permite:

- Descubrir conocimiento oculto en grandes volúmenes de datos.
- Automatizar el proceso de análisis de datos.
- Identificar tendencias y patrones que no son evidentes a simple vista.
- Apoyar la toma de decisiones basada en datos.

## 2. Inteligencia de Negocios (BI)

### 2.1 Definición y Propósito

La Inteligencia de Negocios (BI) se refiere al conjunto de estrategias, procesos, aplicaciones, datos, tecnologías y arquitecturas técnicas utilizadas por las empresas para apoyar la recopilación, análisis, presentación y diseminación de información empresarial. El objetivo principal de BI es facilitar la toma de decisiones basada en datos.

### 2.2 Componentes Clave de BI

1. **Data Warehousing**: Almacenamiento centralizado de datos empresariales.
2. **Reporting**: Generación de informes estructurados.
3. **Análisis OLAP**: Procesamiento analítico en línea para análisis multidimensional.
4. **Data Mining**: Descubrimiento de patrones en grandes conjuntos de datos.
5. **Visualización de Datos**: Representación gráfica de información y datos.
6. **Dashboards**: Interfaces visuales que muestran KPIs y métricas clave.

### 2.3 Tipos de Análisis en BI

1. **Análisis Descriptivo**: ¿Qué ha sucedido?
2. **Análisis Diagnóstico**: ¿Por qué sucedió?
3. **Análisis Predictivo**: ¿Qué podría suceder?
4. **Análisis Prescriptivo**: ¿Qué deberíamos hacer?

## 3. Spark MLlib

### 3.1 Definición y Propósito

Spark MLlib es la biblioteca de aprendizaje automático de Apache Spark, diseñada para ser escalable, rápida y fácil de usar. Proporciona una amplia gama de algoritmos y utilidades para el aprendizaje automático distribuido.

### 3.2 Características Principales

- Algoritmos de clasificación, regresión, clustering y filtrado colaborativo.
- Funcionalidades de extracción, transformación y selección de características.
- Pipelines para construir, evaluar y ajustar modelos de ML.
- Persistencia de modelos para guardar y cargar algoritmos, modelos y pipelines.
- Utilidades para álgebra lineal, estadísticas y manejo de datos.

### 3.3 Ventajas de Spark MLlib

- Escalabilidad para manejar big data.
- Integración seamless con el ecosistema Spark.
- Soporte para procesamiento en memoria para mayor velocidad.
- APIs de alto nivel para facilitar el desarrollo.

## 4. Interrelación entre KDD, BI y Spark MLlib

### 4.1 KDD como Base Conceptual

El proceso KDD proporciona el marco conceptual para el descubrimiento de conocimiento, que es fundamental tanto para BI como para el aprendizaje automático con Spark MLlib.

### 4.2 BI como Contexto de Aplicación

La Inteligencia de Negocios proporciona el contexto empresarial y los objetivos para la aplicación del KDD y MLlib. BI define las preguntas de negocio que se deben responder y los insights que se buscan.

### 4.3 Spark MLlib como Herramienta de Implementación

Spark MLlib ofrece las herramientas técnicas para implementar las etapas del KDD y lograr los objetivos de BI a gran escala.

### 4.4 Alineación de Conceptos

1. **Selección de Datos**:
   - KDD: Define qué datos son relevantes.
   - BI: Identifica las fuentes de datos empresariales.
   - MLlib: Proporciona herramientas para cargar y muestrear grandes conjuntos de datos.

2. **Preprocesamiento y Transformación**:
   - KDD: Establece la necesidad de limpiar y preparar los datos.
   - BI: Define las reglas de negocio para la calidad de datos.
   - MLlib: Ofrece funciones para la limpieza, imputación y transformación de datos a escala.

3. **Minería de Datos/Análisis**:
   - KDD: Especifica la aplicación de algoritmos para descubrir patrones.
   - BI: Define los tipos de insights necesarios (descriptivos, predictivos, prescriptivos).
   - MLlib: Implementa algoritmos escalables para clasificación, regresión, clustering, etc.

4. **Interpretación/Evaluación**:
   - KDD: Enfatiza la importancia de validar y comprender los patrones descubiertos.
   - BI: Proporciona el contexto para interpretar los resultados en términos de impacto empresarial.
   - MLlib: Ofrece métricas y herramientas de evaluación para validar los modelos.

### 4.5 Flujo de Trabajo Integrado

Un flujo de trabajo típico que integra KDD, BI y Spark MLlib podría ser:

1. Definir objetivos de negocio (BI)
2. Seleccionar y recopilar datos relevantes (KDD + BI)
3. Preparar y transformar los datos usando Spark MLlib
4. Aplicar algoritmos de minería de datos con MLlib
5. Evaluar los resultados en el contexto de los objetivos de BI
6. Implementar los insights en sistemas de BI para la toma de decisiones
7. Monitorear y refinar continuamente el proceso (KDD iterativo)

Este enfoque integrado permite a las organizaciones aprovechar el poder del big data y el aprendizaje automático para obtener insights de negocio accionables y mejorar la toma de decisiones.

## 5. Configuración del entorno Spark MLlib

### 5.1 Instalación de Spark con MLlib

Spark MLlib viene incluido en la distribución estándar de Apache Spark. Para instalarlo:

```bash
pip install pyspark
```

### 5.2 Importación de módulos MLlib

```python
from pyspark.sql import SparkSession
from pyspark.ml import Pipeline
from pyspark.ml.classification import LogisticRegression
from pyspark.ml.feature import VectorAssembler, StringIndexer, OneHotEncoder
from pyspark.ml.evaluation import BinaryClassificationEvaluator
```

### 5.3 Creación de una sesión Spark

```python
spark = SparkSession.builder \
    .appName("MLlibExample") \
    .getOrCreate()
```

## 6. Conceptos fundamentales de Spark MLlib

### 6.1 DataFrame y Dataset

MLlib trabaja principalmente con DataFrames y Datasets, que proporcionan una abstracción uniforme para diferentes tipos de datos.

```python
# Crear un DataFrame de ejemplo
data = [(1, "A", 0.1), (2, "B", 0.2), (3, "C", 0.3)]
df = spark.createDataFrame(data, ["id", "category", "feature"])
```

### 6.2 Vectores de características

MLlib requiere que las características de entrada estén en un único vector. El `VectorAssembler` se utiliza para combinar múltiples columnas en un vector de características.

```python
assembler = VectorAssembler(inputCols=["feature"], outputCol="features")
df_assembled = assembler.transform(df)
```

### 6.3 Pipeline

Pipeline es una abstracción de alto nivel para construir flujos de trabajo de ML.

```python
pipeline = Pipeline(stages=[
    StringIndexer(inputCol="category", outputCol="categoryIndex"),
    OneHotEncoder(inputCols=["categoryIndex"], outputCols=["categoryVec"]),
    VectorAssembler(inputCols=["categoryVec", "feature"], outputCol="features"),
    LogisticRegression(featuresCol="features", labelCol="id")
])
```

## 7. Preprocesamiento de datos

### 7.1 Manejo de valores faltantes

```python
from pyspark.ml.feature import Imputer

imputer = Imputer(inputCols=["feature"], outputCols=["feature_imputed"])
imputer_model = imputer.fit(df)
df_imputed = imputer_model.transform(df)
```

### 7.2 Escalado de características

```python
from pyspark.ml.feature import StandardScaler

scaler = StandardScaler(inputCol="features", outputCol="scaledFeatures")
scaler_model = scaler.fit(df_assembled)
df_scaled = scaler_model.transform(df_assembled)
```

### 7.3 Codificación de variables categóricas

```python
from pyspark.ml.feature import StringIndexer, OneHotEncoder

indexer = StringIndexer(inputCol="category", outputCol="categoryIndex")
encoder = OneHotEncoder(inputCols=["categoryIndex"], outputCols=["categoryVec"])
```

## 8. Algoritmos de aprendizaje automático

### 8.1 Clasificación

#### Regresión Logística

```python
from pyspark.ml.classification import LogisticRegression

lr = LogisticRegression(featuresCol="features", labelCol="label")
lr_model = lr.fit(train_data)
predictions = lr_model.transform(test_data)
```

#### Árboles de decisión

```python
from pyspark.ml.classification import DecisionTreeClassifier

dt = DecisionTreeClassifier(featuresCol="features", labelCol="label")
dt_model = dt.fit(train_data)
```

#### Random Forest

```python
from pyspark.ml.classification import RandomForestClassifier

rf = RandomForestClassifier(featuresCol="features", labelCol="label", numTrees=10)
rf_model = rf.fit(train_data)
```

### 8.2 Regresión

#### Regresión Lineal

```python
from pyspark.ml.regression import LinearRegression

lr = LinearRegression(featuresCol="features", labelCol="label")
lr_model = lr.fit(train_data)
```

#### Árboles de decisión para regresión

```python
from pyspark.ml.regression import DecisionTreeRegressor

dt = DecisionTreeRegressor(featuresCol="features", labelCol="label")
dt_model = dt.fit(train_data)
```

### 8.3 Clustering

#### K-Means

```python
from pyspark.ml.clustering import KMeans

kmeans = KMeans(featuresCol="features", k=3)
kmeans_model = kmeans.fit(data)
```

### 9.4 Recomendación

#### ALS (Alternating Least Squares)

```python
from pyspark.ml.recommendation import ALS

als = ALS(userCol="user", itemCol="item", ratingCol="rating")
als_model = als.fit(ratings_data)
```

## 10. Evaluación de modelos

Permite entender qué tan bien se desempeña un modelo en datos no vistos y ayuda a detectar problemas como el sobreajuste o el subajuste.

### 10.1 Métricas de evaluación

Las métricas de evaluación proporcionan una medida cuantitativa del rendimiento del modelo. Spark MLlib ofrece diferentes evaluadores según el tipo de problema.

```python
from pyspark.ml.evaluation import BinaryClassificationEvaluator, MulticlassClassificationEvaluator, RegressionEvaluator

# Para clasificación binaria
binary_evaluator = BinaryClassificationEvaluator(labelCol="label", rawPredictionCol="rawPrediction")
auc = binary_evaluator.evaluate(predictions)

# Para clasificación multiclase
multi_evaluator = MulticlassClassificationEvaluator(labelCol="label", predictionCol="prediction")
accuracy = multi_evaluator.evaluate(predictions)

# Para regresión
reg_evaluator = RegressionEvaluator(labelCol="label", predictionCol="prediction", metricName="rmse")
rmse = reg_evaluator.evaluate(predictions)
```

### 10.2 Técnicas de validación de modelos

Las técnicas de validación de modelos ayudan a estimar el rendimiento del modelo en datos no vistos y a ajustar los hiperparámetros.

#### 10.2.1 Validación cruzada (Cross-validation)

La validación cruzada divide los datos en k subconjuntos (folds), entrenando el modelo k veces, cada vez usando k-1 folds para entrenar y 1 fold para validar.

```python
from pyspark.ml.tuning import CrossValidator, ParamGridBuilder
from pyspark.ml.classification import LogisticRegression

# Definir el modelo
lr = LogisticRegression()

# Definir la rejilla de parámetros
paramGrid = ParamGridBuilder() \
    .addGrid(lr.regParam, [0.1, 0.01]) \
    .addGrid(lr.elasticNetParam, [0.0, 0.5, 1.0]) \
    .build()

# Configurar la validación cruzada
cv = CrossValidator(estimator=lr, 
                    estimatorParamMaps=paramGrid, 
                    evaluator=BinaryClassificationEvaluator(),
                    numFolds=3)

# Ajustar el modelo
cv_model = cv.fit(train_data)

# Obtener el mejor modelo
best_model = cv_model.bestModel
```

#### 10.2.2 Train-Validation Split

Esta técnica divide los datos en conjuntos de entrenamiento, validación y prueba. Es útil cuando se tienen suficientes datos y se quiere un conjunto de prueba completamente separado.

```python
from pyspark.ml.tuning import TrainValidationSplit

# Configurar Train-Validation Split
tvs = TrainValidationSplit(estimator=lr,
                           estimatorParamMaps=paramGrid,
                           evaluator=BinaryClassificationEvaluator(),
                           trainRatio=0.8)  # 80% entrenamiento, 20% validación

# Ajustar el modelo
tvs_model = tvs.fit(train_data)
```

#### 10.2.3 Holdout Validation

En esta técnica, se reserva una parte de los datos (holdout set) para la evaluación final del modelo.

```python
# Dividir los datos en conjuntos de entrenamiento y prueba
train_data, test_data = df.randomSplit([0.8, 0.2], seed=42)

# Entrenar el modelo en los datos de entrenamiento
model = lr.fit(train_data)

# Evaluar en el conjunto de prueba (holdout set)
predictions = model.transform(test_data)
accuracy = evaluator.evaluate(predictions)
```

### 10.3 Interpretación de resultados

Después de la validación, es crucial interpretar los resultados correctamente:

1. **Comparar métricas**: Comparar las métricas de rendimiento (por ejemplo, accuracy, RMSE) entre el conjunto de entrenamiento y el de validación/prueba.

2. **Curvas de aprendizaje**: Visualizar cómo cambia el rendimiento del modelo a medida que aumenta el tamaño del conjunto de entrenamiento.

```python
import matplotlib.pyplot as plt

def plot_learning_curve(train_sizes, train_scores, val_scores):
    plt.figure(figsize=(10, 6))
    plt.plot(train_sizes, train_scores, 'o-', label="Training score")
    plt.plot(train_sizes, val_scores, 'o-', label="Cross-validation score")
    plt.xlabel("Training examples")
    plt.ylabel("Score")
    plt.legend(loc="best")
    plt.title("Learning Curve")
    plt.show()

# Nota: Necesitarías implementar la lógica para obtener train_sizes, train_scores y val_scores
```

3. **Matriz de confusión**: Para problemas de clasificación, examinar la matriz de confusión para entender los tipos de errores que comete el modelo.

```python
from pyspark.ml.evaluation import MulticlassClassificationEvaluator

def print_confusion_matrix(predictions):
    conf_matrix = predictions.groupBy("label", "prediction").count().orderBy("label", "prediction")
    conf_matrix.show()

print_confusion_matrix(predictions)
```

4. **Importancia de características**: Analizar qué características tienen más impacto en las predicciones del modelo.

```python
# Para modelos como RandomForest que proporcionan importancia de características
feature_importance = model.featureImportances
for i, imp in enumerate(feature_importance):
    print(f"Feature {i}: {imp}")
```

### 10.4 Detección de problemas comunes

1. **Sobreajuste (Overfitting)**: El modelo se desempeña muy bien en los datos de entrenamiento pero mal en los de validación.
   - Solución: Aumentar la regularización, reducir la complejidad del modelo, o conseguir más datos.

2. **Subajuste (Underfitting)**: El modelo se desempeña mal tanto en los datos de entrenamiento como en los de validación.
   - Solución: Aumentar la complejidad del modelo, reducir la regularización, o añadir más características relevantes.

3. **Sesgo de datos**: El modelo funciona bien en ciertos subgrupos de datos pero mal en otros.
   - Solución: Asegurar que los datos de entrenamiento sean representativos, considerar técnicas de muestreo estratificado.

Al aplicar estas técnicas de validación y evaluación, podrás desarrollar modelos más robustos y confiables, capaces de generalizar bien a nuevos datos en entornos de producción.

## Implantación de modelos de inteligencia de negocios BI

La implantación efectiva de modelos de inteligencia de negocios (BI) es crucial para transformar los insights derivados del análisis de datos en valor tangible para la organización. Spark MLlib, combinado con tecnologías de contenedorización como Docker, ofrece una solución robusta y escalable para la implementación de modelos de BI en entornos de producción.

## 11. Preparación del modelo para producción

### 11.1. Serialización del modelo

```python
from pyspark.ml import PipelineModel

# Guardar el modelo entrenado
model_path = "/path/to/model"
trained_model.save(model_path)

# Cargar el modelo para uso en producción
loaded_model = PipelineModel.load(model_path)
```

### 11.2. Optimización del modelo

- Utilizar `CrossValidator` o `TrainValidationSplit` para ajustar hiperparámetros.
- Considerar la compresión del modelo para reducir el tamaño de almacenamiento.

#### 11.3. Creación de una API de servicio

Implementar una API RESTful utilizando Flask:

```python
from flask import Flask, request, jsonify
from pyspark.sql import SparkSession
from pyspark.ml import PipelineModel

app = Flask(__name__)
spark = SparkSession.builder.appName("BIModelService").getOrCreate()
model = PipelineModel.load("/path/to/model")

@app.route('/predict', methods=['POST'])
def predict():
    data = request.json
    df = spark.createDataFrame([data])
    prediction = model.transform(df)
    return jsonify(prediction.select("prediction").collect()[0].asDict())

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
```

### 11.4. Dockerización de la aplicación

Crear un Dockerfile

```dockerfile
FROM apache/spark-py:v3.1.2

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .

EXPOSE 5000

CMD ["python", "app.py"]
```

Construir y ejecutar el contenedor Docker:

```bash
docker build -t bi-model-service .
docker run -p 5000:5000 bi-model-service
```

### 11.5. Escalabilidad y gestión de recursos

4.1. Utilizar Docker Swarm o Kubernetes para orquestación de contenedores:

```yaml
# docker-compose.yml para Docker Swarm
version: '3'
services:
  bi-model-service:
    image: bi-model-service
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '0.50'
          memory: 512M
    ports:
      - "5000:5000"
```

4.2. Configurar escalado automático basado en métricas de uso.

### 11.6. Monitoreo y mantenimiento

Implementar logging y monitoreo:

```python
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.route('/predict', methods=['POST'])
def predict():
    logger.info("Received prediction request")
    # ... (código de predicción) ...
    logger.info("Prediction completed")
```

Utilizar herramientas como Prometheus y Grafana para monitoreo en tiempo real.

Establecer un pipeline de CI/CD para actualizaciones automáticas del modelo:

- Utilizar Jenkins o GitLab CI para automatizar el entrenamiento y despliegue de modelos.
- Implementar pruebas A/B para comparar el rendimiento de nuevos modelos antes de su despliegue completo.

### 11.7. Seguridad y cumplimiento

Implementar autenticación y autorización para la API:

```python
from flask_jwt_extended import JWTManager, jwt_required

app.config['JWT_SECRET_KEY'] = 'your-secret-key'
jwt = JWTManager(app)

@app.route('/predict', methods=['POST'])
@jwt_required
def predict():
    # ... (código de predicción) ...
```

Asegurar el cumplimiento de regulaciones de datos (por ejemplo, GDPR, CCPA) en el procesamiento y almacenamiento de datos.

## 12. Integración con sistemas de BI existentes

La integración efectiva de los modelos de Spark MLlib con los sistemas de BI existentes es crucial para maximizar el valor de las predicciones y análisis de machine learning en el contexto empresarial más amplio.

### 12.1. Desarrollo de conectores para herramientas de visualización

#### 12.1.1. Conectores para Tableau

Tableau ofrece varias opciones para integrar datos de Spark:

a) Conector JDBC:

- Utiliza el conector JDBC de Spark para Tableau.
- Configura la conexión en Tableau especificando el host, puerto y credenciales de Spark.

b) Tableau Hyper API:

- Exporta los resultados de Spark a archivos Hyper de Tableau.
- Utiliza la Hyper API para crear y actualizar extractos de Tableau programáticamente.

Ejemplo de código usando Hyper API:

```python
from tableauhyperapi import HyperProcess, Connection, SqlType, TableDefinition, Inserter

def export_to_hyper(spark_df, hyper_file):
    with HyperProcess(telemetry=Telemetry.DO_NOT_SEND_USAGE_DATA_TO_TABLEAU) as hyper:
        with Connection(hyper.endpoint, hyper_file, CreateMode.CREATE_AND_REPLACE) as connection:
            schema = {col: SqlType.varchar() if dtype == 'string' else SqlType.double() 
                      for col, dtype in spark_df.dtypes}
            table_def = TableDefinition("extract", schema.items())
            
            connection.catalog.create_table(table_def)
            with Inserter(connection, table_def) as inserter:
                for row in spark_df.collect():
                    inserter.add_row(row)
                inserter.execute()
```

#### 12.1.2. Conectores para Power BI

a) Conector Spark para Power BI:

- Utiliza el conector nativo de Spark en Power BI.
- Configura la conexión especificando el servidor Spark y las credenciales.

b) API de Power BI:

- Desarrolla un script personalizado para enviar datos de Spark a Power BI.

Ejemplo de código usando la API de Power BI:

```python
import requests

def send_to_powerbi(spark_df, dataset_id, table_name):
    endpoint = f"https://api.powerbi.com/v1.0/myorg/datasets/{dataset_id}/tables/{table_name}/rows"
    headers = {
        "Authorization": f"Bearer {your_access_token}",
        "Content-Type": "application/json"
    }
    
    rows = spark_df.toJSON().collect()
    data = {"rows": [json.loads(row) for row in rows]}
    
    response = requests.post(endpoint, headers=headers, json=data)
    return response.status_code
```

### 12.2. Implementación de data warehouse o data lake

#### 12.2.1. Data Warehouse

a) Integración con un data warehouse relacional:

- Utiliza JDBC para escribir resultados de Spark en un data warehouse como Redshift o Snowflake.

Ejemplo de código para escribir en Redshift:

```python
jdbc_url = "jdbc:redshift://your-cluster.redshift.amazonaws.com:5439/your_database"
spark_df.write \
    .format("jdbc") \
    .option("url", jdbc_url) \
    .option("dbtable", "your_table") \
    .option("user", "your_username") \
    .option("password", "your_password") \
    .mode("append") \
    .save()
```

b) Optimización para consultas analíticas:

- Utiliza particionamiento y clustering para optimizar el

## 13. Funciones clave de Spark MLlib

### Preprocesamiento y extracción de características

1. `VectorAssembler`: Combina múltiples columnas en un vector de características.
2. `StringIndexer`: Codifica una columna de strings en una columna de índices.
3. `OneHotEncoder`: Codifica variables categóricas como vectores one-hot.
4. `StandardScaler`: Estandariza las características.
5. `MinMaxScaler`: Escala las características a un rango específico.
6. `PCA`: Realiza análisis de componentes principales.
7. `Word2Vec`: Convierte documentos de texto en vectores de características.

### Algoritmos de clasificación

8. `LogisticRegression`: Implementa regresión logística binaria y multinomial.
9. `DecisionTreeClassifier`: Implementa árboles de decisión para clasificación.
10. `RandomForestClassifier`: Implementa random forests para clasificación.
11. `GBTClassifier`: Implementa árboles de gradiente boosted para clasificación.
12. `NaiveBayes`: Implementa clasificador Naive Bayes.
13. `MultilayerPerceptronClassifier`: Implementa redes neuronales feed-forward.

### Algoritmos de regresión

14. `LinearRegression`: Implementa regresión lineal.
15. `DecisionTreeRegressor`: Implementa árboles de decisión para regresión.
16. `RandomForestRegressor`: Implementa random forests para regresión.
17. `GBTRegressor`: Implementa árboles de gradiente boosted para regresión.

### Algoritmos de clustering

18. `KMeans`: Implementa el algoritmo k-means.
19. `GaussianMixture`: Implementa modelos de mezcla gaussiana.
20. `BisectingKMeans`: Implementa el algoritmo k-means bisectante.

### Algoritmos de recomendación

21. `ALS`: Implementa el algoritmo Alternating Least Squares para filtrado colaborativo.

### Evaluación de modelos

22. `BinaryClassificationEvaluator`: Evalúa el rendimiento de modelos de clasificación binaria.
23. `MulticlassClassificationEvaluator`: Evalúa el rendimiento de modelos de clasificación multiclase.
24. `RegressionEvaluator`: Evalúa el rendimiento de modelos de regresión.
25. `ClusteringEvaluator`: Evalúa el rendimiento de modelos de clustering.

### Selección y ajuste de modelos

26. `CrossValidator`: Realiza validación cruzada para selección de modelos.
27. `TrainValidationSplit`: Realiza una división train-validation para selección de modelos.
28. `ParamGridBuilder`: Construye una cuadrícula de parámetros para búsqueda de hiperparámetros.

### Pipeline

29. `Pipeline`: Encadena múltiples transformadores y estimadores.
30. `PipelineModel`: Representa un modelo ajustado producido por un Pipeline.

### Funciones de utilidad

31. `FeatureHasher`: Proyecta características en un espacio de características de tamaño fijo.
32. `SQLTransformer`: Aplica una transformación SQL personalizada.
33. `Tokenizer`: Convierte texto en tokens.
34. `StopWordsRemover`: Elimina palabras de parada de un conjunto de tokens.
35. `CountVectorizer`: Convierte una colección de documentos de texto en vectores de recuento de términos.

## 14. Ejercicios

### Ejercicio 1: Clasificación de Iris

**Objetivo**: Utilizar Spark MLlib para clasificar las especies de flores Iris.

**Dataset**: <https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data>

**Instrucciones**:

1. Carga el dataset en un DataFrame de Spark.
2. Preprocesa los datos:
   a) Convierte las etiquetas de clase a índices numéricos.
   b) Combina las características en un vector.
3. Divide los datos en conjuntos de entrenamiento y prueba.
4. Entrena un modelo de Regresión Logística.
5. Evalúa el modelo utilizando precisión y matriz de confusión.

### Ejercicio 2: Predicción de precios de viviendas

**Objetivo**: Utilizar Spark MLlib para predecir precios de viviendas.

**Dataset**: <https://raw.githubusercontent.com/ageron/handson-ml/master/datasets/housing/housing.csv>

**Instrucciones**:

1. Carga el dataset en un DataFrame de Spark.
2. Preprocesa los datos:
   a) Maneja los valores faltantes.
   b) Codifica las variables categóricas.
   c) Escala las características numéricas.
3. Divide los datos en conjuntos de entrenamiento y prueba.
4. Crea un pipeline que incluya el preprocesamiento y un modelo de Random Forest Regressor.
5. Utiliza CrossValidator para encontrar los mejores hiperparámetros.
6. Evalúa el modelo final utilizando RMSE y R².

## Ejercicio final

### Objetivo

Realizar un análisis completo de inteligencia de negocios utilizando Spark MLlib para:

1. Predecir las ventas futuras de productos.
2. Clasificar la satisfacción del cliente basada en reseñas.
3. Segmentar clientes para estrategias de marketing personalizadas.

Dataset: Retail Sales and Customer Satisfaction Dataset
<https://github.com/databricks/Spark-The-Definitive-Guide/blob/master/data/retail-data/all/online-retail-dataset.csv>

### Instrucciones

1. Cargar y explorar el dataset
2. Realizar preprocesamiento de datos
3. Implementar tres pipelines de ML:
   a) Regresión para predicción de ventas
   b) Clasificación para satisfacción del cliente
   c) Clustering para segmentación de clientes
4. Utilizar validación cruzada para ajustar hiperparámetros
5. Evaluar los modelos finales
6. Extraer insights de negocio de los modelos

### Desafío adicional

- Crear un dashboard con los KPIs más relevantes
- Proponer estrategias de negocio basadas en los resultados
  
 