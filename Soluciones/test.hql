-- Paso 1: Crear la base de datos
CREATE DATABASE IF NOT EXISTS test_db;

-- Paso 2: Usar la base de datos
USE test_db;

-- Paso 3: Crear una tabla simple
CREATE TABLE IF NOT EXISTS test_table (
    id INT,
    name STRING
);

-- Paso 4: Insertar un dato
INSERT INTO test_table VALUES (1, 'Test');

-- Paso 5: Verificar el dato
SELECT * FROM test_table;