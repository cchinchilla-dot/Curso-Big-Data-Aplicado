#!/bin/bash
set -e

# Definir colores para la salida en la terminal
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # Sin color

# Función para imprimir el resultado de una operación
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}[OK]${NC} $2"
    else
        echo -e "${RED}[FAILED]${NC} $2"
    fi
}

# Función para imprimir una sección del script
print_section() {
    echo ""
    echo -e "${BLUE}# $1${NC}"
}

# Función para imprimir un banner al inicio o final del script
print_banner() {
    echo -e "${YELLOW}"
    echo "****************************************************"
    echo "*                                                  *"
    echo "*              $1            *"
    echo "*                                                  *"
    echo "****************************************************"
    echo -e "${NC}"
}

# Función para iniciar un servicio y verificar si se inició correctamente
start_service() {
    local command="$1"
    local log_file="$2"
    local name="$3"

    echo "Iniciando $name..."
    nohup $command > "$log_file" 2>&1 &
    sleep 5
    if pgrep -f "$name" > /dev/null; then
        print_result 0 "$name iniciado correctamente."
    else
        print_result 1 "Error al iniciar $name. Revisa $log_file para más detalles."
    fi
}

# Función para ejecutar un comando como el usuario hdfs
run_as_hdfs() {
    sudo -u hdfs bash -c "source /etc/profile && $1"
}

# Función para detectar la arquitectura del sistema
detect_architecture() {
    local arch=$(uname -m)
    case $arch in
        x86_64)
            echo "amd64"
            ;;
        aarch64)
            echo "arm64"
            ;;
        armv7l)
            echo "armhf"
            ;;
        *)
            echo "unknown"
            ;;
    esac
}

# Función para establecer JAVA_HOME en función de la arquitectura
set_java_home() {
    local arch=$(detect_architecture)
    case $arch in
        amd64)
            echo "/usr/lib/jvm/java-11-openjdk-amd64"
            ;;
        arm64)
            echo "/usr/lib/jvm/java-11-openjdk-arm64"
            ;;
        armhf)
            echo "/usr/lib/jvm/java-11-openjdk-armhf"
            ;;
        *)
            echo ""
            ;;
    esac
}

# Inicio del script
print_banner "Iniciando configuración "

###################################################################
#                       CONFIGURAR ENTORNO                        #
###################################################################

# --------------------- 1. Cargar variables de entorno ---------------------
source /etc/profile.d/bigdata_env.sh

# ------------------- 2. Configurar JAVA_HOME globalmente -------------------

JAVA_HOME=$(set_java_home)

if [ -z "$JAVA_HOME" ]; then
    echo "Error: Unable to determine JAVA_HOME for this architecture."
    exit 1
fi

# Configurar JAVA_HOME globalmente
echo "export JAVA_HOME=$JAVA_HOME" | sudo tee -a /etc/profile.d/bigdata_env.sh > /dev/null || echo "Error al configurar JAVA_HOME"

# ------------- 3. Asegurarse de que JAVA_HOME esté configurado -------------

export JAVA_HOME

# ---------------------- 4. Verificar configuración de JAVA_HOME ------------
print_section "Configurando JAVA_HOME"

print_result 0 "Configuración de de JAVA_HOME en $JAVA_HOME"

# Verify JAVA_HOME configuration
print_section "Configurando JAVA_HOME"
if [ -d "$JAVA_HOME" ]; then
    print_result 0 "JAVA_HOME configurado correctamente en: $JAVA_HOME"
else
    print_result 1 "JAVA_HOME no está configurado correctamente. Directorio no existe: $JAVA_HOME"
    exit 1
fi

# --------------- 5. Verificar que Java esté instalado y configurado --------
# Verify Java installation
if "$JAVA_HOME/bin/java" -version 2>&1 >/dev/null; then
    print_result 0 "Java está correctamente instalado y configurado"
else
    print_result 1 "Java no está correctamente instalado o configurado"
    exit 1
fi

# -------------------- 6. Activar el entorno virtual de Python --------------
print_section "Activando entorno virtual"
source /opt/venv/bin/activate
print_result $? "Entorno virtual activado"

# ---------------- 7. Iniciar servicios básicos, como SSH -------------------
print_section "Iniciando servicios básicos"
service ssh start && print_result 0 "SSH iniciado" || print_result 1 "Error al iniciar SSH"

###################################################################
#                     CONFIGURAR HADOOP                           #
###################################################################

# --------------- 1. Verificar configuración de Hadoop ----------------------
print_section "Configurando Hadoop"
if [ -z "$HADOOP_HOME" ]; then
    print_result 1 "HADOOP_HOME no está configurado"
    exit 1
fi
print_result 0 "HADOOP_HOME configurado en: $HADOOP_HOME"

# --------------- 2. Configurar archivo hadoop-env.sh con JAVA_HOME ----------
echo "export JAVA_HOME=${JAVA_HOME}" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh

# --------------- 3. Configurar core-site.xml -------------------------------
cat > $HADOOP_HOME/etc/hadoop/core-site.xml <<EOF
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
    </property>
    <property>
        <name>dfs.datanode.max.transfer.threads</name>
        <value>4096</value>
        <description>Number of transfer threads that a datanode can handle concurrently</description>
    </property>
    <property>
        <name>ipc.maximum.data.length</name>
        <value>671088640</value> 
    </property>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
    </property>
    <property>
        <name>hadoop.proxyuser.hdfs.groups</name>
        <value>*</value>
    </property>
    <property>
        <name>hadoop.proxyuser.hdfs.hosts</name>
        <value>*</value>
    </property>
    <property>
        <name>hadoop.proxyuser.hive.groups</name>
        <value>*</value>
    </property>
    <property>
        <name>hadoop.proxyuser.hive.hosts</name>
        <value>*</value>
    </property>
</configuration>
EOF

# --------------- 4. Configurar hdfs-site.xml -------------------------------
cat > $HADOOP_HOME/etc/hadoop/hdfs-site.xml <<EOF
<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
    <property>
        <name>dfs.permissions.superusergroup</name>
        <value>root</value>
    </property>
    <property>
        <name>dfs.permissions.enabled</name>
        <value>false</value>
    </property>
</configuration>
EOF
print_result $? "Configuración de Hadoop completada"

# --------------- 5.1. Configurar SLF4J para Hadoop ---------------------------
print_section "Configurando SLF4J para Hadoop"

HADOOP_LIB_PATH="/opt/hadoop/share/hadoop/common/lib"

# Eliminar versiones antiguas o conflictivas de SLF4J y Log4j en Hadoop
rm -f ${HADOOP_LIB_PATH}/slf4j-*.jar
rm -f ${HADOOP_LIB_PATH}/log4j-*.jar

# Descargar las versiones correctas de SLF4J y Log4j 2.x
SLF4J_VERSION="1.7.36"
LOG4J_VERSION="2.20.0"

# Descargar SLF4J API y log4j-slf4j-impl para Log4j 2.x
wget -q https://repo1.maven.org/maven2/org/slf4j/slf4j-api/${SLF4J_VERSION}/slf4j-api-${SLF4J_VERSION}.jar -P $HADOOP_LIB_PATH
wget -q https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/${LOG4J_VERSION}/log4j-api-${LOG4J_VERSION}.jar -P $HADOOP_LIB_PATH
wget -q https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/${LOG4J_VERSION}/log4j-core-${LOG4J_VERSION}.jar -P $HADOOP_LIB_PATH
wget -q https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-slf4j-impl/${LOG4J_VERSION}/log4j-slf4j-impl-${LOG4J_VERSION}.jar -P $HADOOP_LIB_PATH

# Actualizar las variables de entorno
echo "export HADOOP_CLASSPATH=$HADOOP_LIB_PATH/slf4j-api-${SLF4J_VERSION}.jar:$HADOOP_LIB_PATH/log4j-api-${LOG4J_VERSION}.jar:$HADOOP_LIB_PATH/log4j-core-${LOG4J_VERSION}.jar:$HADOOP_LIB_PATH/log4j-slf4j-impl-${LOG4J_VERSION}.jar:$HADOOP_CLASSPATH" >> /etc/profile.d/bigdata_env.sh
source /etc/profile.d/bigdata_env.sh

# Crear o actualizar log4j.properties para Hadoop
cat > $HADOOP_HOME/etc/hadoop/log4j.properties <<EOF
# Root logger option
log4j.rootLogger=INFO, stdout

# Direct log messages to stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Target=System.out
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
EOF

print_result 0 "Configuración de SLF4J y Log4j para Hadoop completada"

# --------------- 5.2. Eliminar posibles conflictos de SLF4J en Hive -----------
print_section "Configurando SLF4J y Log4j para Hive"

HIVE_LIB_PATH="$HIVE_HOME/lib"

# Eliminar SLF4J y Log4j que no sean necesarios en Hive
rm -f ${HIVE_LIB_PATH}/slf4j-*.jar
rm -f ${HIVE_LIB_PATH}/log4j-1*.jar

# Descargar las versiones correctas de SLF4J y Log4j 2.x para Hive
wget -q https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/${LOG4J_VERSION}/log4j-api-${LOG4J_VERSION}.jar -P $HIVE_LIB_PATH
wget -q https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/${LOG4J_VERSION}/log4j-core-${LOG4J_VERSION}.jar -P $HIVE_LIB_PATH
wget -q https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-slf4j-impl/${LOG4J_VERSION}/log4j-slf4j-impl-${LOG4J_VERSION}.jar -P $HIVE_LIB_PATH

# Verificar que los archivos necesarios estén en su lugar
if [[ -f ${HIVE_LIB_PATH}/log4j-api-${LOG4J_VERSION}.jar && -f ${HIVE_LIB_PATH}/log4j-core-${LOG4J_VERSION}.jar && -f ${HIVE_LIB_PATH}/log4j-slf4j-impl-${LOG4J_VERSION}.jar ]]; then
    print_result 0 "Log4j 2.x y SLF4J configurados correctamente para Hive"
else
    print_result 1 "Error en la configuración de Log4j 2.x y SLF4J para Hive"
    exit 1
fi

# Verificar si hay duplicados en Hive y Hadoop
if [ -f /opt/hadoop/share/hadoop/common/lib/log4j-slf4j-impl-2.20.0.jar ]; then
    rm /opt/hive/lib/log4j-slf4j-impl-2.20.0.jar
else
    mv /opt/hive/lib/log4j-slf4j-impl-2.20.0.jar /opt/hadoop/share/hadoop/common/lib/
fi

# Asegurar que el entorno de Hive usa Log4j 2.x
print_result 0 "Configuración de SLF4J y Log4j para Hive completada"

# --------------- 6. Formatear NameNode si es necesario ---------------------
if [ ! -d "${HADOOP_HOME}/dfs/name/current" ]; then
    print_section "Formateando NameNode"
    su - hdfs -s /bin/bash -c "source /etc/profile && ${HADOOP_HOME}/bin/hdfs namenode -format" >/dev/null 2>&1 || echo "Error al formatear NameNode"
    if [ $? -eq 0 ]; then
        print_result 0 "NameNode formateado correctamente"
    else
        print_result 1 "Error al formatear NameNode"
        exit 1
    fi
else
    print_result 0 "NameNode ya está formateado"
fi

# --------------- 7. Configurar pdsh para usar SSH --------------------------
print_section "Configurando pdsh"
apt-get update > /dev/null 2>&1
apt-get install -y pdsh > /dev/null 2>&1
echo "ssh" > /etc/pdsh/rcmd_default
echo "export PDSH_RCMD_TYPE=ssh" >> /etc/profile.d/bigdata_env.sh 
source /etc/profile.d/bigdata_env.sh
print_result $? "pdsh configurado para usar SSH"

# --------------- 8. Iniciar los servicios de Hadoop ------------------------
print_section "Iniciando servicios de Hadoop"
su - hdfs -c "source /etc/profile && $HADOOP_HOME/sbin/start-dfs.sh"
su - hdfs -c "source /etc/profile && $HADOOP_HOME/sbin/start-yarn.sh"
su - hdfs -c "source /etc/profile && mapred --daemon start historyserver"

# --------------- 9. Esperar a que HDFS esté disponible ---------------------
for i in {1..30}; do
    if hdfs dfs -ls / > /dev/null 2>&1; then
        print_result 0 "HDFS iniciado correctamente"
        break
    fi
    if [ $i -eq 30 ]; then
        print_result 1 "Error al iniciar HDFS"
        exit 1
    fi
    sleep 1
done

# --------------- 10. Configurar permisos en HDFS para Hive ------------------
print_section "Configurando permisos en HDFS para Hive"
su - hdfs -c "hdfs dfs -mkdir -p /user/hive/warehouse"
su - hdfs -c "hdfs dfs -chmod g+w /user/hive/warehouse"
su - hdfs -c "hdfs dfs -chown -R hive:hadoop /user/hive"
print_result 0 "Permisos en HDFS configurados para Hive"

###################################################################
#                  VERIFICAR INSTALACIONES                        #
###################################################################

# --------------- 1. Verificar que todas las herramientas estén instaladas ---
print_section "Verificando instalaciones"
for tool in java hadoop spark-submit hive pig flume-ng sqoop python3; do
    command -v $tool > /dev/null 2>&1 && print_result 0 "$tool instalado" || print_result 1 "$tool no encontrado"
done

###################################################################
#                    CONFIGURAR MYSQL                             #
###################################################################

# --------------- 1. Configurar y iniciar MySQL -----------------------------
print_section "Configurando MySQL"
cat << EOF > /etc/mysql/mysql.conf.d/mysqld.cnf
[mysqld]
pid-file = /var/run/mysqld/mysqld.pid
socket = /var/run/mysqld/mysqld.sock
datadir = /var/lib/mysql
log-error = /var/log/mysql/error.log
bind-address = 0.0.0.0
EOF

# --------------- 2. Crear directorios necesarios y asignar permisos ---------
mkdir -p /var/run/mysqld /var/lib/mysql
chown -R mysql:mysql /var/run/mysqld /var/lib/mysql
chmod 755 /var/run/mysqld
service mysql start && print_result 0 "MySQL iniciado" || print_result 1 "Error al iniciar MySQL"

# --------------- 3. Crear archivo de configuración temporal para MySQL -------
cat << EOF > /tmp/mysql_credentials.cnf
[client]
user=root
password=new_root_password
EOF

# --------------- 4. Asegurar instalación de MySQL usando credenciales -------
mysql --defaults-extra-file=/tmp/mysql_credentials.cnf <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_root_password';
DELETE FROM mysql.user WHERE User='';
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');
DROP DATABASE IF EXISTS test;
DELETE FROM mysql.db WHERE Db='test' OR Db='test\\_%';
CREATE USER 'hive'@'localhost' IDENTIFIED BY 'hive_password';
GRANT ALL PRIVILEGES ON *.* TO 'hive'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF

# --------------- 5. Eliminar archivo temporal de credenciales ---------------
rm /tmp/mysql_credentials.cnf


###################################################################
#                    CREAR Y CONFIGURAR USUARIO HIVE              #
###################################################################

print_section "Creando y configurando usuario 'hive'"

# Crear usuario 'hive'
useradd -m -s /bin/bash hive
echo "hive:hive_password" | chpasswd
usermod -aG hadoop hive

# Configurar permisos para el directorio de Hive
sudo chown -R hive:hadoop $HIVE_HOME
sudo chmod -R 775 $HIVE_HOME

# Crear directorio para logs de Hive
sudo mkdir -p /var/log/hive
sudo chown -R hive:hadoop /var/log/hive
sudo chmod -R 775 /var/log/hive

# Asegurarse de que el usuario 'hive' pueda escribir en el directorio de HDFS
su - hdfs -c "hdfs dfs -mkdir -p /user/hive"
su - hdfs -c "hdfs dfs -chown hive:hadoop /user/hive"

print_result 0 "Usuario 'hive' creado y configurado"


###################################################################
#                    CONFIGURAR HIVE                              #
###################################################################

# --------------- 1. Configurar Hive ----------------------------------------
print_section "Configurando Hive"

# Asegurar que los directorios necesarios existan y tengan los permisos correctos
sudo mkdir -p /tmp/hive /user/hive/warehouse
sudo chown -R hive:hadoop /tmp/hive /user/hive/warehouse
sudo chmod 775 /tmp/hive /user/hive/warehouse

# Configurar hive-site.xml
cat > $HIVE_HOME/conf/hive-site.xml <<EOF
<configuration>
    <property>
        <name>javax.jdo.option.ConnectionURL</name>
        <value>jdbc:mysql://localhost:3306/metastore?createDatabaseIfNotExist=true</value>
    </property>
    <property>
        <name>javax.jdo.option.ConnectionDriverName</name>
        <value>com.mysql.cj.jdbc.Driver</value>
    </property>
    <property>
        <name>javax.jdo.option.ConnectionUserName</name>
        <value>hive</value>
    </property>
    <property>
        <name>javax.jdo.option.ConnectionPassword</name>
        <value>hive_password</value>
    </property>
    <property>
        <name>hive.metastore.schema.verification</name>
        <value>false</value>
    </property>
    <property>
        <name>hive.metastore.warehouse.dir</name>
        <value>/user/hive/warehouse</value>
    </property>
    <property>
        <name>hive.server2.enable.doAs</name>
        <value>false</value>
    </property>
    <property>
        <name>hive.server2.authentication</name>
        <value>NONE</value>
    </property>
    <property>
        <name>hive.metastore.uris</name>
        <value>thrift://localhost:9083</value>
    </property>
    <property>
        <name>hive.exec.scratchdir</name>
        <value>/tmp/hive</value>
    </property>
    <property>
        <name>hive.metastore.event.db.notification.api.auth</name>
        <value>false</value>
    </property>
    <property>
        <name>hive.security.authorization.enabled</name>
        <value>false</value>
    </property>
    <property>
        <name>hive.support.concurrency</name>
        <value>true</value>
    </property>
    <property>
        <name>hive.enforce.bucketing</name>
        <value>true</value>
    </property>
    <property>
        <name>hive.exec.dynamic.partition.mode</name>
        <value>nonstrict</value>
    </property>
    <property>
        <name>hive.txn.manager</name>
        <value>org.apache.hadoop.hive.ql.lockmgr.DbTxnManager</value>
    </property>
    <property>
        <name>hive.compactor.initiator.on</name>
        <value>true</value>
    </property>
    <property>
        <name>hive.compactor.worker.threads</name>
        <value>1</value>
    </property>
</configuration>
EOF

# Configurar hive-env.sh
cat > $HIVE_HOME/conf/hive-env.sh <<EOF
export HADOOP_HOME=${HADOOP_HOME}
export HIVE_CONF_DIR=${HIVE_HOME}/conf
export HIVE_AUX_JARS_PATH=${HIVE_HOME}/lib
EOF

# --------------- 2. Eliminar posibles conflictos de SLF4J -------------------
rm -f /opt/hadoop/share/hadoop/common/lib/slf4j-reload4j-1.7.36.jar && print_result 0 "Binding conflictivo eliminado" || print_result 1 "Error al eliminar binding conflictivo"

# --------------- 3. Inicializar el esquema de Hive --------------------------
print_section "Inicializando esquema de Hive"
su - hive -c "${HIVE_HOME}/bin/schematool -dbType mysql -initSchema --verbose" > /dev/null 2>&1

# --------------- 4. Iniciar servicios de Hive -------------------------------
print_section "Iniciando servicios de Hive"

# Configurar variables de entorno para Hive
export HADOOP_OPTS="$HADOOP_OPTS --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED"
export HADOOP_CLIENT_OPTS="$HADOOP_CLIENT_OPTS -Djava.security.auth.login.config=$HADOOP_HOME/conf/login-sandbox.conf"
export HIVE_OPTS="$HIVE_OPTS -Dhive.metastore.uris=thrift://localhost:9083"

# Iniciar Hive Metastore
print_section "Iniciando Hive Metastore"
su - hive -c "source /etc/profile && nohup $HIVE_HOME/bin/hive --service metastore > /tmp/hive_metastore.log 2>&1 &" > /dev/null 2>&1
sleep 10

# Iniciar HiveServer2
print_section "Iniciando HiveServer2"
su - hive -c "source /etc/profile && nohup $HIVE_HOME/bin/hiveserver2 --hiveconf hive.server2.enable.doAs=false > /tmp/hiveserver2.log 2>&1 &" > /dev/null 2>&1
sleep 20

# --------------- 5. Verificar que los servicios de Hive estén en ejecución ---
if pgrep -f org.apache.hadoop.hive.metastore.HiveMetaStore > /dev/null && pgrep -f org.apache.hive.service.server.HiveServer2 > /dev/null; then
    print_result 0 "Hive iniciado correctamente"
else
    print_result 1 "Error al iniciar servicios de Hive"
    echo "Contenido de /tmp/hive_metastore.log:"
    cat /tmp/hive_metastore.log
    echo "Contenido de /tmp/hiveserver2.log:"
    cat /tmp/hiveserver2.log
    exit 1
fi

# --------------- 6. Verificar conexión de Hive ------------------------------
print_section "Verificando conexión de Hive"
if beeline -u "jdbc:hive2://localhost:10000" -n hive -p hive_password -e "SHOW DATABASES;" > /tmp/beeline_output.log 2>&1; then
    print_result 0 "Conexión a Hive establecida correctamente"
    cat /tmp/beeline_output.log
else
    print_result 1 "Error al conectar con Hive"
    echo "Contenido de /tmp/beeline_output.log:"
    cat /tmp/beeline_output.log
    echo "Contenido de /tmp/hiveserver2.log:"
    cat /tmp/hiveserver2.log
    exit 1
fi

###################################################################
#                    CONFIGURAR PIG                               #
###################################################################

# --------------- 1. Configurar archivo pig-env.sh --------------------------
print_section "Configurando Pig"

PIG_ENV_FILE="$PIG_HOME/conf/pig-env.sh"

if [ -f "$PIG_ENV_FILE" ]; then
    sed -i "s#export JAVA_HOME=.*#export JAVA_HOME=$JAVA_HOME#" $PIG_ENV_FILE
    print_result $? "JAVA_HOME actualizado en $PIG_ENV_FILE"
else
    echo "export JAVA_HOME=$JAVA_HOME" > $PIG_ENV_FILE
    echo "Archivo $PIG_ENV_FILE creado con JAVA_HOME"
fi

# --------------- 2. Asegurarse de que pig-env.sh se carga -------------------
PIG_CONF_FILE="$PIG_HOME/conf/pig.properties"
if [ -f "$PIG_CONF_FILE" ]; then
    if ! grep -q "pig-env.sh" $PIG_CONF_FILE; then
        echo "pig.load.default.properties=true" >> $PIG_CONF_FILE
        echo "Configuración para cargar pig-env.sh añadida a $PIG_CONF_FILE"
    fi
else
    echo "pig.load.default.properties=true" > $PIG_CONF_FILE
    echo "Archivo $PIG_CONF_FILE creado con configuración para cargar pig-env.sh"
fi
print_result 0 "Pig configurado correctamente"

###################################################################
#                    CONFIGURAR FLUME                             #
###################################################################

# --------------- 1. Configurar Flume ---------------------------------------
print_section "Configurando Flume"
FLUME_CONF_DIR="/opt/flume/conf"
mkdir -p $FLUME_CONF_DIR

# --------------- 2. Crear archivo de configuración básico para Flume --------
cat << EOF > $FLUME_CONF_DIR/flume-conf.properties
# Configuración básica de Flume
agent.sources = r1
agent.sinks = k1
agent.channels = c1

# Configurar source
agent.sources.r1.type = netcat
agent.sources.r1.bind = localhost
agent.sources.r1.port = 44444

# Configurar sink
agent.sinks.k1.type = logger

# Configurar channel
agent.channels.c1.type = memory
agent.channels.c1.capacity = 1000
agent.channels.c1.transactionCapacity = 100

# Vincular source y sink al canal
agent.sources.r1.channels = c1
agent.sinks.k1.channel = c1
EOF

# --------------- 3. Actualizar el script de inicio de Flume -----------------
FLUME_BIN="/opt/flume/bin/flume-ng"
sed -i "s|^FLUME_CONF_DIR=.*|FLUME_CONF_DIR=$FLUME_CONF_DIR|" $FLUME_BIN
sed -i "s|^JAVA_HOME=.*|JAVA_HOME=$JAVA_HOME|" $FLUME_BIN

print_result 0 "Flume configurado correctamente"

###################################################################
#                    CONFIGURAR SQOOP, ACCUMULO, ZOOKEEPER         #
###################################################################

# --------------- 1. Función para manejar errores ---------------------------
handle_error() {
    print_result 1 "Error en $1"
    echo "Detalles del error:"
    echo "$2"
    exit 1
}

# --------------- 2. Configurar Sqoop, Accumulo y ZooKeeper -------------------
print_section "Configurando Sqoop, Accumulo y ZooKeeper"

# --------------- 3. Instalar el controlador JDBC de MySQL --------------------
MYSQL_CONNECTOR_VERSION="9.0.0"
MYSQL_CONNECTOR_JAR="mysql-connector-j-$MYSQL_CONNECTOR_VERSION.jar"
wget -q https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/$MYSQL_CONNECTOR_VERSION/$MYSQL_CONNECTOR_JAR -P /tmp || handle_error "Descarga del controlador MySQL" "Fallo en la descarga"
mv /tmp/$MYSQL_CONNECTOR_JAR $SQOOP_HOME/lib/ || handle_error "Instalación del controlador MySQL" "Fallo al mover el archivo"
print_result 0 "Controlador JDBC de MySQL instalado"

# --------------- 4. Instalar Accumulo ---------------------------------------
if [ ! -d "/opt/accumulo" ]; then
    ACCUMULO_VERSION="3.0.0"
    wget -q https://downloads.apache.org/accumulo/$ACCUMULO_VERSION/accumulo-$ACCUMULO_VERSION-bin.tar.gz || handle_error "Descarga de Accumulo" "Fallo en la descarga"
    tar -xzf accumulo-$ACCUMULO_VERSION-bin.tar.gz || handle_error "Descompresión de Accumulo" "Fallo al descomprimir"
    mv accumulo-$ACCUMULO_VERSION /opt/accumulo || handle_error "Instalación de Accumulo" "Fallo al mover los archivos"
    rm accumulo-$ACCUMULO_VERSION-bin.tar.gz
    print_result 0 "Accumulo instalado"
else
    print_result 0 "Accumulo ya está instalado"
fi

# --------------- 5. Instalar ZooKeeper --------------------------------------
if [ ! -d "/opt/zookeeper" ]; then
    ZOOKEEPER_VERSION="3.9.2"
    wget -q https://downloads.apache.org/zookeeper/zookeeper-$ZOOKEEPER_VERSION/apache-zookeeper-$ZOOKEEPER_VERSION-bin.tar.gz || handle_error "Descarga de ZooKeeper" "Fallo en la descarga"
    tar -xzf apache-zookeeper-$ZOOKEEPER_VERSION-bin.tar.gz || handle_error "Descompresión de ZooKeeper" "Fallo al descomprimir"
    mv apache-zookeeper-$ZOOKEEPER_VERSION-bin /opt/zookeeper || handle_error "Instalación de ZooKeeper" "Fallo al mover los archivos"
    rm apache-zookeeper-$ZOOKEEPER_VERSION-bin.tar.gz
    print_result 0 "ZooKeeper instalado"
else
    print_result 0 "ZooKeeper ya está instalado"
fi

# --------------- 6. Instalar HBase ------------------------------------------
print_section "Instalando HBase"
install_hbase() {
    local HBASE_VERSION="2.5.9"
    local HBASE_DOWNLOAD_URL="https://dlcdn.apache.org/hbase/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz"
    local HBASE_TAR_FILE="hbase-${HBASE_VERSION}-bin.tar.gz"
    local MAX_RETRIES=3
    local RETRY_COUNT=0

    while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
        if wget -q "${HBASE_DOWNLOAD_URL}" -O "${HBASE_TAR_FILE}"; then
            if tar -tzf "${HBASE_TAR_FILE}" > /dev/null 2>&1; then
                tar -xzf "${HBASE_TAR_FILE}" && \
                mv "hbase-${HBASE_VERSION}" /opt/hbase && \
                rm "${HBASE_TAR_FILE}" && \
                print_result 0 "HBase instalado correctamente" && \
                return 0
            else
                print_result 1 "El archivo de HBase está corrupto o incompleto. Intentando nuevamente..."
                rm "${HBASE_TAR_FILE}"
            fi
        else
            print_result 1 "Error al descargar HBase. Intentando nuevamente..."
        fi
        RETRY_COUNT=$((RETRY_COUNT + 1))
        sleep 5
    done

    print_result 1 "No se pudo instalar HBase después de ${MAX_RETRIES} intentos."
    return 1
}

if [ ! -d "/opt/hbase" ]; then
    if ! install_hbase; then
        exit 1
    fi
else
    print_result 0 "HBase ya está instalado"
fi

# Configurar HBase
cat > /opt/hbase/conf/hbase-site.xml <<EOF
<configuration>
  <property>
    <name>hbase.rootdir</name>
    <value>hdfs://localhost:9000/hbase</value>
  </property>
  <property>
    <name>hbase.cluster.distributed</name>
    <value>true</value>
  </property>
  <property>
    <name>hbase.zookeeper.quorum</name>
    <value>localhost</value>
  </property>
</configuration>
EOF

# Configurar variables de entorno para HBase
echo "export HBASE_HOME=/opt/hbase" >> /etc/profile.d/bigdata_env.sh
echo "export PATH=\$PATH:\$HBASE_HOME/bin" >> /etc/profile.d/bigdata_env.sh
source /etc/profile.d/bigdata_env.sh

print_result 0 "HBase configurado correctamente"

# --------------- 7. Instalar dependencia SLF4J para Sqoop -------------------
SLF4J_VERSION="1.7.36"
wget -q https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/$SLF4J_VERSION/slf4j-simple-$SLF4J_VERSION.jar -P $SQOOP_HOME/lib/ || handle_error "Descarga de SLF4J" "Fallo en la descarga"
print_result 0 "Dependencia SLF4J instalada para Sqoop"

# --------------- 8. Configurar variables de entorno -------------------------
ENV_FILE="/etc/profile.d/bigdata_env.sh"
echo "Configurando variables de entorno en $ENV_FILE..."

sudo tee -a $ENV_FILE > /dev/null <<EOF || handle_error "Configuración de variables de entorno" "Fallo al escribir en el archivo"
export SQOOP_HOME=/opt/sqoop
export ACCUMULO_HOME=/opt/accumulo
export ZOOKEEPER_HOME=/opt/zookeeper
export HBASE_HOME=/opt/hbase
export PATH=\$PATH:\$SQOOP_HOME/bin:\$ACCUMULO_HOME/bin:\$ZOOKEEPER_HOME/bin:\$HBASE_HOME/bin
export HADOOP_COMMON_HOME=$HADOOP_HOME
export HADOOP_MAPRED_HOME=$HADOOP_HOME
export JAVA_HOME=$JAVA_HOME
EOF

# --------------- 9. Verificar si el archivo se escribió correctamente --------
if [ -f "$ENV_FILE" ] && grep -q "SQOOP_HOME" "$ENV_FILE" && grep -q "ACCUMULO_HOME" "$ENV_FILE" && grep -q "ZOOKEEPER_HOME" "$ENV_FILE" && grep -q "HBASE_HOME" "$ENV_FILE"; then
    print_result 0 "Variables de entorno configuradas correctamente"
    source $ENV_FILE
else
    handle_error "Verificación de variables de entorno" "El archivo $ENV_FILE no existe o no contiene la configuración esperada"
fi

# --------------- 10. Configurar Sqoop ---------------------------------------
SQOOP_CONF_FILE="$SQOOP_HOME/conf/sqoop-env.sh"
SQOOP_SITE_FILE="$SQOOP_HOME/conf/sqoop-site.xml"

if [ ! -f "$SQOOP_CONF_FILE" ]; then
    cp $SQOOP_HOME/conf/sqoop-env-template.sh "$SQOOP_CONF_FILE" || handle_error "Configuración de Sqoop" "No se pudo crear el archivo de configuración"
fi

# --------------- 11. Añadir JAVA_HOME a la configuración de Sqoop ------------
echo "export JAVA_HOME=${JAVA_HOME}" >> ${SQOOP_CONF_FILE}

# --------------- 12. Actualizar sqoop-site.xml ------------------------------
cat > "$SQOOP_SITE_FILE" <<EOF
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
  <property>
    <name>sqoop.connection.factories</name>
    <value>com.cloudera.sqoop.manager.DefaultManagerFactory</value>
  </property>
  <property>
    <name>sqoop.connection.provider.factory</name>
    <value>com.mysql.cj.jdbc.MysqlDataSource</value>
  </property>
  <property>
    <name>sqoop.metastore.client.autoconnect.url</name>
    <value>jdbc:hsqldb:file:/tmp/sqoop-metastore/metastore.db;shutdown=true</value>
  </property>
  <property>
    <name>sqoop.metastore.client.enable.autoconnect</name>
    <value>true</value>
  </property>
</configuration>
EOF

print_result 0 "Archivo sqoop-site.xml actualizado"

# --------------- 13. Verificar si el directorio lib de Sqoop contiene MySQL ---
if [ ! -f $SQOOP_HOME/lib/mysql-connector-java.jar ]; then
    wget https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar -O $SQOOP_HOME/lib/mysql-connector-java.jar > /dev/null 2>&1
fi

# --------------- 14. Actualizar otras configuraciones de Sqoop ---------------
sed -i '/export HADOOP_COMMON_HOME/c\export HADOOP_COMMON_HOME=/opt/hadoop' "$SQOOP_CONF_FILE"
sed -i '/export HADOOP_MAPRED_HOME/c\export HADOOP_MAPRED_HOME=/opt/hadoop' "$SQOOP_CONF_FILE"
sed -i '/export HIVE_HOME/c\export HIVE_HOME=/opt/hive' "$SQOOP_CONF_FILE"
sed -i '/export ACCUMULO_HOME/c\export ACCUMULO_HOME=/opt/accumulo' "$SQOOP_CONF_FILE"
sed -i '/export ZOOKEEPER_HOME/c\export ZOOKEEPER_HOME=/opt/zookeeper' "$SQOOP_CONF_FILE"
sed -i '/export HBASE_HOME/c\export HBASE_HOME=/opt/hbase' "$SQOOP_CONF_FILE"

print_result 0 "Configuración de Sqoop actualizada"

# --------------- 15. Configurar usuario de MySQL para Sqoop -------------------
mysql -u root -pnew_root_password <<EOF > /dev/null 2>&1
CREATE USER IF NOT EXISTS 'sqoop'@'localhost' IDENTIFIED BY 'sqoop_password';
GRANT ALL PRIVILEGES ON *.* TO 'sqoop'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF
print_result $? "Usuario de MySQL para Sqoop configurado"

# --------------- 16. Verificar conexión de Sqoop con MySQL --------------------
print_section "Verificando la conexión de Sqoop con MySQL"

# --------------- 17. Cargar variables de entorno de Sqoop ---------------------
source ${SQOOP_HOME}/conf/sqoop-env.sh

# --------------- 18. Verificar que JAVA_HOME esté configurado -----------------
if [ -z "$JAVA_HOME" ]; then
    print_result 1 "JAVA_HOME sigue sin estar configurado. Revisa la configuración."
    exit 1
else
    print_result 0 "JAVA_HOME configurado en: $JAVA_HOME"
fi

# --------------- 19. Ejecutar comando de Sqoop para verificar conexión --------
if SQOOP_TEST_OUTPUT=$(sqoop list-databases --connect jdbc:mysql://localhost:3306 --username sqoop --password sqoop_password 2>&1); then
    if echo "$SQOOP_TEST_OUTPUT" | grep -q "information_schema"; then
        print_result 0 "Sqoop puede conectarse a MySQL correctamente"
        echo "Bases de datos disponibles:"
        echo "$SQOOP_TEST_OUTPUT" | grep -Ev "Warning:|^INFO|Please set|SLF4J:|Loading class"
    else
        print_result 1 "Sqoop se conectó pero no pudo listar las bases de datos"
        echo "Salida de Sqoop:"
        echo "$SQOOP_TEST_OUTPUT"
    fi
else
    print_result 1 "Error al conectar Sqoop con MySQL"
    echo "Detalles del error de Sqoop:"
    echo "$SQOOP_TEST_OUTPUT"
fi

# --------------- 20. Verificación final de herramientas -----------------------
print_section "Verificación final"
sqoop version > /dev/null 2>&1 && print_result 0 "Sqoop instalado y configurado" || print_result 1 "Problemas con Sqoop"
accumulo version > /dev/null 2>&1 && print_result 0 "Accumulo instalado y configurado" || print_result 1 "Problemas con Accumulo"
zkServer.sh version > /dev/null 2>&1 && print_result 0 "ZooKeeper instalado y configurado" || print_result 1 "Problemas con ZooKeeper"
print_result 0 "Configuración completada"

###################################################################
#                    CONFIGURAR AIRFLOW                           #
###################################################################

# --------------- 1. Inicializar y configurar Airflow ------------------------
print_section "Inicializando y configurando Airflow"
airflow db init > /dev/null 2>&1 || print_result 1 "Error al inicializar la base de datos de Airflow"

# --------------- 2. Crear el usuario admin de Airflow -----------------------
airflow users create \
    --username admin \
    --firstname Admin \
    --lastname User \
    --role Admin \
    --email admin@example.com \
    --password admin > /dev/null 2>&1 || log "Usuario ya existe o hubo un error al crearlo"

# --------------- 3. Iniciar servicios de Airflow ----------------------------
start_service "airflow webserver -p 8080" "/tmp/airflow_webserver.log" "airflow webserver" > /dev/null 2>&1
start_service "airflow scheduler" "/tmp/airflow_scheduler.log" "airflow scheduler" > /dev/null 2>&1
print_result 0 "Airflow iniciado"

# --------------- 4. Iniciar Jupyter Notebook --------------------------------
print_section "Iniciando Jupyter Notebook"
start_service "jupyter notebook --ip 0.0.0.0 --port 8888 --no-browser --allow-root --NotebookApp.token='' --NotebookApp.password=''" "/tmp/jupyter.log" "jupyter-notebook"

###################################################################
#                    MENSAJE DE FINALIZACIÓN                      #
###################################################################

# ----------------  Mensaje de finalización ----------------------------------
print_banner "Configuración completadaa"
echo "Todos los servicios iniciados. El contenedor está listo."
echo "Airflow UI: http://localhost:8080 (usuario: admin, contraseña: admin)"
echo "Jupyter Notebook: http://localhost:8888 (sin token requerido)"
echo ""

# ----------------  Mantener el contenedor en ejecución ----------------------
exec /bin/bash
