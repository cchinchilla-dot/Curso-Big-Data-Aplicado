import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import java.sql.Timestamp

// Definir un objeto principal
object FunctionsExample {
  // Definir un caso de clase para el Dataset
  case class Evento(timestamp: Timestamp, usuario_id: String, tipo_evento: String, monto: Double)

  def main(args: Array[String]): Unit = {
    // Crear una sesión de Spark
    val spark = SparkSession.builder()
      .appName("FunctionsExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Crear una secuencia de datos simulando los eventos
    val eventos = Seq(
      Evento(Timestamp.valueOf("2024-09-01 10:00:00"), "user1", "compra", 100.0),
      Evento(Timestamp.valueOf("2024-09-01 12:30:00"), "user2", "compra", 200.0),
      Evento(Timestamp.valueOf("2024-09-02 09:15:00"), "user1", "visita", 0.0),
      Evento(Timestamp.valueOf("2024-09-03 15:45:00"), "user3", "compra", 150.0),
      Evento(Timestamp.valueOf("2024-09-04 16:30:00"), "user4", "visita", 0.0),
      Evento(Timestamp.valueOf("2024-09-05 14:00:00"), "user2", "compra", 300.0),
      Evento(Timestamp.valueOf("2024-09-06 18:00:00"), "user5", "visita", 0.0),
      Evento(Timestamp.valueOf("2024-09-07 20:00:00"), "user1", "compra", 400.0),
      Evento(Timestamp.valueOf("2024-09-07 21:30:00"), "user3", "compra", 250.0)
    )

    // Crear un Dataset a partir de la secuencia de datos
    val df = eventos.toDF()

    // Procesar el DataFrame
    val dfProcesado = df
      .withColumn("fecha", to_date($"timestamp"))  // Crear una nueva columna "fecha" convirtiendo el "timestamp" a solo la fecha (sin la hora)
      .withColumn("hora", hour($"timestamp"))  // Crear una nueva columna "hora" extrayendo la hora del "timestamp"
      .withColumn("es_fin_semana", when(dayofweek($"fecha").isin(1, 7), true).otherwise(false))  // Crear una columna booleana "es_fin_semana" que es true si "fecha" es sábado o domingo
      .groupBy($"fecha", $"es_fin_semana")  // Agrupar los datos por "fecha" y si es fin de semana o no
      .agg(
        countDistinct($"usuario_id").as("usuarios_unicos"),  // Contar el número de usuarios únicos ("usuario_id") en cada grupo y almacenarlo en "usuarios_unicos"
        sum(when($"tipo_evento" === "compra", $"monto").otherwise(0)).as("total_ventas")  // Sumar los "monto" de eventos donde "tipo_evento" es "compra" y almacenar en "total_ventas"
      )
      .withColumn("promedio_movil_ventas", 
        avg($"total_ventas").over(Window.partitionBy($"es_fin_semana")  // Calcular el promedio móvil de "total_ventas" para cada grupo de "es_fin_semana"
          .orderBy($"fecha")  // Ordenar por "fecha" para calcular el promedio móvil en orden cronológico
          .rowsBetween(-6, 0)))  // Definir la ventana de promedio móvil como los últimos 7 días (6 días anteriores más el día actual)

    // Mostrar el resultado
    dfProcesado.show()

    // Detener la sesión de Spark
    spark.stop()
  }
}

