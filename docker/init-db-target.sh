#!/usr/bin/bash
set -euo pipefail

# Por SQL de inicializacao de script aqui
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE TABLE DM_DEPARTAMENTO (
        COD_DPTO NUMERIC(4) NOT NULL,
        NOME_DPTO VARCHAR(255) NOT NULL,
        PRIMARY KEY (COD_DPTO));

    CREATE TABLE DM_PROFESSOR (
        MAT_PROF NUMERIC(10) NOT NULL,
        NOME_PROF VARCHAR(255) NOT NULL,
        TITULO VARCHAR(255) NOT NULL,
        ENDERECO VARCHAR(255) NOT NULL,
        COD_DPTO_FK NUMERIC(4) NOT NULL,
        PRIMARY KEY (MAT_PROF));

    CREATE TABLE DM_CURSO (
        COD_DM_CURSO NUMERIC(3) NOT NULL,
        DESC_DM_CURSO VARCHAR(255) NOT NULL,
        NUM_CRED_DM_CURSO NUMERIC(4) NOT NULL,
        COD_DPTO_FK NUMERIC(4) NOT NULL,
        DURACAO_NORMAL VARCHAR(100) NOT NULL,
        PRIMARY KEY (COD_DM_CURSO));

    CREATE TABLE FT_PROD_PROFESSOR (
        MAT_PROF NUMERIC(10) NOT NULL,
        ALUNOS_POR_PERIODO NUMERIC(10) NOT NULL,
        PERCENT_ALUNOS_APROVADOS NUMERIC(10) NOT NULL,
        PRIMARY KEY (MAT_PROF));

    CREATE TABLE ADD CONSTRAINT DPTO_FK FOREIGN KEY (COD_DPTO) REFERENCES DM_DEPARTAMENTO (COD_DPTO);
    ALTER TABLE DM_CURSO ADD CONSTRAINT DPTO_FK FOREIGN KEY (COD_DPTO_FK) REFERENCES DM_DEPARTAMENTO (COD_DPTO);
    ALTER TABLE DM_PROFESSOR ADD CONSTRAINT DPTO2_FK FOREIGN KEY (COD_DPTO_FK) REFERENCES DM_DEPARTAMENTO (COD_DPTO);
    ALTER TABLE FT_PROD_PROFESSOR ADD CONSTRAINT PROF_FK FOREIGN KEY (MAT_PROF) REFERENCES DM_PROFESSOR (MAT_PROF);

EOSQL
