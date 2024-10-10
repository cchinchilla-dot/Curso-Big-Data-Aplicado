FROM ubuntu:24.04

# Configurar el modo no interactivo para apt-get
ENV DEBIAN_FRONTEND=noninteractive

# Instalar utilidades básicas y dependencias necesarias
RUN apt-get update && apt-get install -y --no-install-recommends \
    gnupg wget curl openssh-server pdsh openjdk-11-jdk \
    python3.10 python3-pip python3-venv python3-dev \
    rsync net-tools sudo libkrb5-dev krb5-user libsasl2-dev libsasl2-modules-gssapi-mit \
    build-essential libssl-dev zlib1g-dev libbz2-dev libreadline-dev libsqlite3-dev \
    libncursesw5-dev xz-utils tk-dev libxml2-dev libxmlsec1-dev libffi-dev liblzma-dev \
    libpq-dev gcc mysql-server g++ scala && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Configurar SSH
RUN mkdir /var/run/sshd && \
    echo 'root:root' | chpasswd && \
    sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && \
    sed -i 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' /etc/pam.d/sshd && \
    echo "export VISIBLE=now" >> /etc/profile

# Crear usuarios y configurar permisos
RUN groupadd -r hadoop && \
    useradd -r -g hadoop -m -d /home/hdfs hdfs && \
    useradd -r -g hadoop -m yarn && \
    useradd -r -g hadoop -m mapred && \
    usermod -aG sudo hdfs && \
    echo "hdfs ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    echo "root ALL=(hdfs) NOPASSWD: ALL" >> /etc/sudoers

# Configurar Java
RUN ARCH=$(dpkg --print-architecture) && \
    if [ "$ARCH" = "amd64" ]; then \
        JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64; \
    elif [ "$ARCH" = "arm64" ]; then \
        JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64; \
    else \
        echo "Arquitectura desconocida: $ARCH"; \
        exit 1; \
    fi && \
    echo "JAVA_HOME=$JAVA_HOME" >> /etc/environment && \
    echo "export JAVA_HOME=$JAVA_HOME" >> /etc/profile.d/java_env.sh && \
    echo "export PATH=\$PATH:\$JAVA_HOME/bin" >> /etc/profile.d/java_env.sh && \
    chmod +x /etc/profile.d/java_env.sh

# Establecer las variables de entorno JAVA_HOME y PATH para el sistema
ENV JAVA_HOME=${JAVA_HOME}
ENV PATH=$PATH:$JAVA_HOME/bin

# Asegurarse de que las variables de entorno se carguen en sesiones SSH
RUN echo "if [ -f /etc/profile.d/java_env.sh ]; then . /etc/profile.d/java_env.sh; fi" >> /etc/bash.bashrc && \
    echo "if [ -f /etc/profile.d/bigdata_env.sh ]; then . /etc/profile.d/bigdata_env.sh; fi" >> /etc/bash.bashrc && \
    echo "if [ -f /etc/profile.d/java_env.sh ]; then . /etc/profile.d/java_env.sh; fi" >> /etc/profile && \
    echo "if [ -f /etc/profile.d/bigdata_env.sh ]; then . /etc/profile.d/bigdata_env.sh; fi" >> /etc/profile

# Instalar Hadoop
ENV HADOOP_VERSION=3.4.0
ENV HADOOP_HOME=/opt/hadoop
RUN wget https://dlcdn.apache.org/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz && \
    tar -xzf hadoop-${HADOOP_VERSION}.tar.gz && \
    mv hadoop-${HADOOP_VERSION} ${HADOOP_HOME} && \
    rm hadoop-${HADOOP_VERSION}.tar.gz && \
    mkdir -p ${HADOOP_HOME}/logs && \
    chown -R hdfs:hadoop ${HADOOP_HOME} && \
    chmod -R 755 ${HADOOP_HOME}
ENV PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin

# Configurar Hadoop
RUN mkdir -p /app/hadoop/tmp /app/hadoop/namenode /app/hadoop/datanode && \
    chown -R hdfs:hadoop /app/hadoop && \
    chmod 750 /app/hadoop/namenode /app/hadoop/datanode && \
    echo "export JAVA_HOME=${JAVA_HOME}" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh && \
    echo "export HADOOP_HOME=${HADOOP_HOME}" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh && \
    echo "export HADOOP_CONF_DIR=${HADOOP_HOME}/etc/hadoop" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh

# Instalar sbt
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x99E82A75642AC823" | sudo apt-key add - && \
    sudo apt-get update && \
    sudo apt-get install -y sbt
        
# Instalar Spark
ENV SPARK_VERSION=3.5.2
ENV SPARK_HOME=/opt/spark
RUN wget https://dlcdn.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop3.tgz && \
    tar -xzf spark-${SPARK_VERSION}-bin-hadoop3.tgz && \
    mv spark-${SPARK_VERSION}-bin-hadoop3 ${SPARK_HOME} && \
    rm spark-${SPARK_VERSION}-bin-hadoop3.tgz
ENV PATH=$PATH:$SPARK_HOME/bin

# Obtener el classpath de Spark para uso posterior
RUN echo "SPARK_CLASSPATH=\$(find $SPARK_HOME/jars/ -name '*.jar' | tr '\n' ':')" >> /etc/profile.d/spark_env.sh

# Asegurarse de que los scripts usen el classpath de Spark
RUN echo "export SPARK_CLASSPATH=\$SPARK_CLASSPATH" >> /etc/profile.d/bigdata_env.sh && \
    echo "export SCALA_CLASSPATH=\$SPARK_CLASSPATH" >> /etc/profile.d/bigdata_env.sh

# Instalar Hive
ENV HIVE_VERSION=4.0.0
ENV HIVE_HOME=/opt/hive
RUN wget https://dlcdn.apache.org/hive/hive-${HIVE_VERSION}/apache-hive-${HIVE_VERSION}-bin.tar.gz && \
    tar -xzf apache-hive-${HIVE_VERSION}-bin.tar.gz && \
    mv apache-hive-${HIVE_VERSION}-bin ${HIVE_HOME} && \
    rm apache-hive-${HIVE_VERSION}-bin.tar.gz
ENV PATH=$PATH:$HIVE_HOME/bin

# Configurar Hive
RUN mkdir -p ${HIVE_HOME}/logs && \
    chown -R hdfs:hadoop ${HIVE_HOME} && \
    chmod -R 755 ${HIVE_HOME}

# Instalar el controlador JDBC de MySQL
RUN wget -O ${HIVE_HOME}/lib/mysql-connector-java.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar

# Instalar Pig
ENV PIG_VERSION=0.17.0
ENV PIG_HOME=/opt/pig
RUN wget https://dlcdn.apache.org/pig/pig-${PIG_VERSION}/pig-${PIG_VERSION}.tar.gz && \
    tar -xzf pig-${PIG_VERSION}.tar.gz && \
    mv pig-${PIG_VERSION} ${PIG_HOME} && \
    rm pig-${PIG_VERSION}.tar.gz
ENV PATH=$PATH:$PIG_HOME/bin

# Instalar Flume
ENV FLUME_VERSION=1.11.0
ENV FLUME_HOME=/opt/flume
RUN wget https://dlcdn.apache.org/flume/${FLUME_VERSION}/apache-flume-${FLUME_VERSION}-bin.tar.gz && \
    tar -xzf apache-flume-${FLUME_VERSION}-bin.tar.gz && \
    mv apache-flume-${FLUME_VERSION}-bin ${FLUME_HOME} && \
    rm apache-flume-${FLUME_VERSION}-bin.tar.gz
ENV PATH=$PATH:$FLUME_HOME/bin

# Configurar Flume
RUN echo "export JAVA_OPTS=\"-Xmx512m\"" >> ${FLUME_HOME}/conf/flume-env.sh

# Instalar Sqoop
ENV SQOOP_VERSION=1.4.7
ENV SQOOP_HOME=/opt/sqoop
RUN wget https://archive.apache.org/dist/sqoop/${SQOOP_VERSION}/sqoop-${SQOOP_VERSION}.bin__hadoop-2.6.0.tar.gz && \
    tar -xzf sqoop-${SQOOP_VERSION}.bin__hadoop-2.6.0.tar.gz && \
    mv sqoop-${SQOOP_VERSION}.bin__hadoop-2.6.0 ${SQOOP_HOME} && \
    rm sqoop-${SQOOP_VERSION}.bin__hadoop-2.6.0.tar.gz
ENV PATH=$PATH:$SQOOP_HOME/bin

# Configurar Sqoop
ENV HBASE_HOME=/opt/hbase
ENV HCAT_HOME=/opt/hive
ENV ACCUMULO_HOME=/opt/accumulo
ENV ZOOKEEPER_HOME=/opt/zookeeper
RUN wget https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar -P $SQOOP_HOME/lib/

# Añadir JAVA_HOME al entorno de Sqoop
RUN echo "export JAVA_HOME=${JAVA_HOME}" >> ${SQOOP_HOME}/conf/sqoop-env.sh

# Instalar Ganglia
RUN apt-get update && apt-get install -y ganglia-monitor gmetad ganglia-webfrontend && \
    rm -rf /var/lib/apt/lists/*

# Crear un entorno virtual para Python
ENV VIRTUAL_ENV=/opt/venv
RUN python3 -m venv $VIRTUAL_ENV
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

# Actualizar pip e instalar wheel
RUN pip install --upgrade pip wheel setuptools

# Instalar paquetes de Python necesarios
RUN pip install jupyter pandas numpy scikit-learn matplotlib graphviz seaborn pyspark apache-airflow apache-airflow-providers-apache-spark && \
    pip install apache-airflow-providers-apache-hdfs apache-airflow-providers-apache-hive

# Configurar Airflow
ENV AIRFLOW_HOME=/opt/airflow
RUN mkdir -p ${AIRFLOW_HOME} && chown -R hdfs:hadoop ${AIRFLOW_HOME}

# Inicializar la base de datos de Airflow y crear un usuario administrador
RUN airflow db init && \
    airflow users create --username admin --firstname Admin --lastname User --role Admin --email admin@example.com --password admin

# Configurar Jupyter para que escuche en todas las interfaces y sin autenticación
RUN jupyter notebook --generate-config && \
    echo "c.NotebookApp.ip = '0.0.0.0'" >> /root/.jupyter/jupyter_notebook_config.py && \
    echo "c.NotebookApp.open_browser = False" >> /root/.jupyter/jupyter_notebook_config.py && \
    echo "c.NotebookApp.port = 8888" >> /root/.jupyter/jupyter_notebook_config.py && \
    echo "c.NotebookApp.token = ''" >> /root/.jupyter/jupyter_notebook_config.py && \
    echo "c.NotebookApp.password = ''" >> /root/.jupyter/jupyter_notebook_config.py

# Configurar claves SSH para el usuario hdfs
USER hdfs
RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && \
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
    chmod 0600 ~/.ssh/authorized_keys

USER root

# Copiar scripts y configurar permisos
COPY verify_environment.sh /home/hdfs/verify_environment.sh
COPY start.sh /start.sh
RUN chown hdfs:hadoop /home/hdfs/verify_environment.sh /start.sh && \
    chmod +x /home/hdfs/verify_environment.sh /start.sh

# Actualizar variables de entorno globales para todas las herramientas
RUN echo "export JAVA_HOME=${JAVA_HOME}" > /etc/profile.d/bigdata_env.sh && \
    echo "export HADOOP_HOME=${HADOOP_HOME}" >> /etc/profile.d/bigdata_env.sh && \
    echo "export PIG_HOME=${PIG_HOME}" >> /etc/profile.d/bigdata_env.sh && \
    echo "export SPARK_HOME=${SPARK_HOME}" >> /etc/profile.d/bigdata_env.sh && \
    echo "export HIVE_HOME=${HIVE_HOME}" >> /etc/profile.d/bigdata_env.sh && \
    echo "export FLUME_HOME=${FLUME_HOME}" >> /etc/profile.d/bigdata_env.sh && \
    echo "export SQOOP_HOME=${SQOOP_HOME}" >> /etc/profile.d/bigdata_env.sh && \
    echo "export PATH=\$PATH:\$JAVA_HOME/bin:\$HADOOP_HOME/bin:\$HADOOP_HOME/sbin:\$PIG_HOME/bin:\$SPARK_HOME/bin:\$HIVE_HOME/bin:\$FLUME_HOME/bin:\$SQOOP_HOME/bin" >> /etc/profile.d/bigdata_env.sh

# Configurar SSH para que lea el archivo de entorno
RUN sed -i 's/^PermitUserEnvironment no/PermitUserEnvironment yes/' /etc/ssh/sshd_config

# Crear el archivo .ssh/environment para el usuario hdfs con las variables de entorno necesarias
USER hdfs
RUN mkdir -p ~/.ssh && \
    echo "JAVA_HOME=${JAVA_HOME}" >> ~/.ssh/environment && \
    echo "HADOOP_HOME=${HADOOP_HOME}" >> ~/.ssh/environment && \
    echo "PATH=${PATH}" >> ~/.ssh/environment && \
    echo "if [ -f /etc/profile.d/java_env.sh ]; then . /etc/profile.d/java_env.sh; fi" >> ~/.bashrc && \
    echo "if [ -f /etc/profile.d/bigdata_env.sh ]; then . /etc/profile.d/bigdata_env.sh; fi" >> ~/.bashrc
USER root

# Asegurar que bash sea el shell predeterminado
RUN sed -i 's/bin\/sh/bin\/bash/g' /etc/passwd

# Exponer los puertos necesarios
EXPOSE 8080 8088 9870 10000 16010 8042 8888 50070 50075 50090 50105 50030 50060 22

# Asegurar que el directorio /workspace existe
RUN mkdir -p /workspace

# Establecer el directorio de trabajo
WORKDIR /workspace

# Establecer el punto de entrada para el script de inicio
ENTRYPOINT ["/bin/bash", "/start.sh"]
