import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.Configuration
import java.net.URL
import java.io.{File, FileOutputStream}

object ParquetHandlingExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("ParquetHandlingExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // URL del archivo Parquet a descargar (yellow taxi trip records)
    val url = "https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2022-01.parquet"
    
    // Descargar el archivo Parquet
    val localFilePath = "yellow_tripdata_2022-01.parquet"
    downloadFile(url, localFilePath)
    println(s"Archivo Parquet descargado en: $localFilePath")

    // Leer el archivo Parquet local
    val dfParquetLocal = spark.read.parquet(localFilePath)
    println("DataFrame leido del archivo Parquet local:")
    dfParquetLocal.show(5)
    println(s"Numero de filas: ${dfParquetLocal.count()}")
    println("Esquema del DataFrame:")
    dfParquetLocal.printSchema()

    // Configurar HDFS
    val hdfsUri = "hdfs://localhost:9000"  // Reemplaza con la URI de tu HDFS
    val hdfsInputPath = s"$hdfsUri/curso/yellow_taxi_2022-01.parquet"
    val hdfsOutputPath = s"$hdfsUri/curso/yellow_taxi_2022-01_processed.parquet"

    // Obtener el sistema de archivos HDFS
    val hadoopConf = new Configuration()
    hadoopConf.set("fs.defaultFS", hdfsUri)
    val hdfs = FileSystem.get(hadoopConf)

    // Copiar el archivo Parquet a HDFS
    hdfs.copyFromLocalFile(new Path(localFilePath), new Path(hdfsInputPath))
    println(s"Archivo Parquet copiado a HDFS: $hdfsInputPath")

    // Lectura del archivo Parquet desde HDFS
    val dfParquetHDFS = spark.read.parquet(hdfsInputPath)
    println("DataFrame leido desde HDFS (Parquet):")
    dfParquetHDFS.show(5)

    // Realizar una operación simple: filtrar las filas donde trip_distance > 5.0
    val dfFiltered = dfParquetHDFS.filter($"trip_distance" > 5.0)
    println("DataFrame filtrado (viajes con distancia > 5.0 millas):")
    dfFiltered.show(5)

    // Guardar el DataFrame filtrado en un nuevo archivo Parquet en HDFS
    dfFiltered.write.parquet(hdfsOutputPath)
    println(s"DataFrame filtrado guardado en HDFS: $hdfsOutputPath")

    // Detener la sesión de Spark
    spark.stop()
  }

  def downloadFile(url: String, localFilePath: String): Unit = {
    val connection = new URL(url).openConnection()
    val inputStream = connection.getInputStream
    val outputStream = new FileOutputStream(new File(localFilePath))

    try {
      val buffer = new Array[Byte](4096)
      var bytesRead = inputStream.read(buffer)
      while (bytesRead != -1) {
        outputStream.write(buffer, 0, bytesRead)
        bytesRead = inputStream.read(buffer)
      }
    } finally {
      inputStream.close()
      outputStream.close()
    }
  }
}