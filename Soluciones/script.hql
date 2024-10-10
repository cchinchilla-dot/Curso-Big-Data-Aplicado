-- Configuraciones para mejorar la ejecución
SET hive.exec.mode.local.auto=true;
SET hive.fetch.task.conversion=more;

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS ventas_db;
USE ventas_db;

-- Crear tablas
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente INT,
    nombre STRING,
    email STRING,
    fecha_registro STRING
) STORED AS TEXTFILE;

CREATE TABLE IF NOT EXISTS productos (
    id_producto INT,
    nombre_producto STRING,
    categoria STRING,
    precio DOUBLE
) STORED AS TEXTFILE;

CREATE TABLE IF NOT EXISTS ventas (
    id_venta INT,
    id_cliente INT,
    id_producto INT,
    cantidad INT,
    fecha_venta STRING
) STORED AS TEXTFILE;

-- Insertar datos de ejemplo
INSERT INTO TABLE clientes VALUES
(1, 'Cliente1', 'cliente1@example.com', '2023-01-01');

INSERT INTO TABLE productos VALUES
(1, 'Producto1', 'Electrónica', 500.00);

INSERT INTO TABLE ventas VALUES
(1, 1, 1, 2, '2023-06-01');

-- Consulta simplificada
SELECT 
    p.categoria,
    SUM(v.cantidad * p.precio) as ventas_totales
FROM 
    ventas v
JOIN 
    productos p ON v.id_producto = p.id_producto
GROUP BY 
    p.categoria;
