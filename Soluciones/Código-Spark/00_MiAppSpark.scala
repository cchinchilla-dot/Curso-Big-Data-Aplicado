import org.apache.spark.sql.SparkSession
import org.apache.log4j.{Level, Logger}

object MiAppSpark {
    def main(args: Array[String]): Unit = {
        // Configurar el nivel de log globalmente
        Logger.getLogger("org").setLevel(Level.OFF)
        Logger.getLogger("akka").setLevel(Level.OFF)
        Logger.getRootLogger.setLevel(Level.OFF)

        // Desactivar el logging de Spark
        System.setProperty("spark.logging.initialized", "true")

        // Crear una sesión de Spark con configuración optimizada
        val spark = SparkSession.builder()
            .appName("MiAppSpark")
            .master("local[*]")
            .config("spark.driver.memory", "2g")
            .config("spark.executor.memory", "2g")
            .config("spark.log.level", "OFF")
            .config("spark.sql.legacy.createHiveTableByDefault", "false")
            .config("spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version", "2")
            .config("spark.ui.showConsoleProgress", "false")
            .config("spark.sql.catalogImplementation", "in-memory")
            .config("spark.driver.extraJavaOptions", "-Dlog4j.configuration=log4j2.properties")
            .config("spark.executor.extraJavaOptions", "-Dlog4j.configuration=log4j2.properties")
            .getOrCreate()

        // Configurar el nivel de log para Spark
        spark.sparkContext.setLogLevel("OFF")

        // Desactivar el logging de Hadoop
        val hadoopConf = spark.sparkContext.hadoopConfiguration
        hadoopConf.set("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false")

        // Crear un DataFrame de ejemplo
        val df = spark.createDataFrame(Seq(
        (1, "Alice", 25),
        (2, "Bob", 30),
        (3, "Charlie", 35)
        )).toDF("id", "name", "age")

        // Mostrar el contenido del DataFrame
        df.show()

        // Realizar una operación simple
        val resultDF = df.filter(df("age") > 28)
        resultDF.show()

        // Detener la sesión de Spark
        spark.stop()
    }
}