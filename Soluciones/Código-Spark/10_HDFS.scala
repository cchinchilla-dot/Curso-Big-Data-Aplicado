import org.apache.spark.sql.SparkSession
import org.apache.hadoop.fs.{FileSystem, Path, FSDataOutputStream}
import java.io.{BufferedReader, InputStreamReader}
import scala.io.{Source, Codec}
import org.apache.hadoop.conf.Configuration

object HDFSFileHandlingExample {
  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("HDFSFileHandlingExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // URL del archivo de texto a descargar
    val url = "https://www.gutenberg.org/files/11/11-0.txt"  // "Alice's Adventures in Wonderland" de Lewis Carroll

    // Descargar el archivo de texto con la codificación UTF-8
    implicit val codec: Codec = Codec.UTF8
    val textData = Source.fromURL(url).reader()  // Obtener un Reader directamente desde la fuente

    // Configurar HDFS
    val hdfsUri = "hdfs://localhost:9000"  // Reemplaza con la URI de tu HDFS
    val hdfsInputPath = s"$hdfsUri/curso/alice.txt"
    val hdfsOutputPath = s"$hdfsUri/curso/alice_processed.txt"

    // Obtener el sistema de archivos HDFS
    val hadoopConf = new Configuration()
    hadoopConf.set("fs.defaultFS", hdfsUri)
    val hdfs = FileSystem.get(hadoopConf)

    // Crear un archivo en HDFS para escribir en él
    val outputStream: FSDataOutputStream = hdfs.create(new Path(hdfsInputPath))

    // Leer y subir el archivo línea por línea para evitar transferencias grandes
    val bufferedReader = new BufferedReader(textData)  // Utilizar BufferedReader para leer eficientemente
    var line: String = null
    while ({ line = bufferedReader.readLine(); line != null }) {
      outputStream.writeBytes(line + "\n")
    }

    bufferedReader.close()
    outputStream.close()
    println(s"Archivo subido a HDFS: $hdfsInputPath")

    // Lectura del archivo de texto desde HDFS
    val dfHDFS = spark.read.textFile(hdfsInputPath)

    // Mostrar algunas líneas del archivo leído desde HDFS
    dfHDFS.show(10, truncate = false)

    // Escritura del DataFrame en un archivo de texto en HDFS
    dfHDFS.write.text(hdfsOutputPath)
    println(s"Archivo de texto procesado guardado en HDFS: $hdfsOutputPath")

    // Detener la sesión de Spark
    spark.stop()
  }
}
