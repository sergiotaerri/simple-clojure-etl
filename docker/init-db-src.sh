#!/usr/bin/bash
set -euo pipefail

# Por SQL de inicializacao de script aqui
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE TABLE DISCIPLINA (
        COD_DISC NUMERIC(4) NOT NULL,
        NOME_DISC VARCHAR(255) NOT NULL,
        NUM_CRED_DISC NUMERIC(3) NOT NULL,
        NATUREZA CHAR(1) NOT NULL,
        ID_CURSO_FK NUMERIC(4) NOT NULL,
        PRIMARY KEY (COD_DISC));

    CREATE TABLE PROFESSOR (
        MAT_PROF NUMERIC(10) NOT NULL,
        NOME_PROF VARCHAR(255) NOT NULL,
        TITULO VARCHAR(255) NOT NULL,
        ENDERECO VARCHAR(255) NOT NULL,
        COD_DPTO_FK NUMERIC(4) NOT NULL,
        PRIMARY KEY (MAT_PROF));

    CREATE TABLE ALUNO (
        MAT_ALU NUMERIC(10) NOT NULL,
        NOME_ALU VARCHAR(255) NOT NULL,
        ESTADO_CIVIL VARCHAR(100) NOT NULL,
        SEXO CHAR(1) NOT NULL,
        ANO_INGRESSO DATE NOT NULL,
        COD_CURSO_FK NUMERIC(3) NOT NULL,
        NOTA NUMERIC(6, 2) NOT NULL,
        PRIMARY KEY (MAT_ALU));

    CREATE TABLE CURSO (
        COD_CURSO NUMERIC(3) NOT NULL,
        DESC_CURSO VARCHAR(255) NOT NULL,
        NUM_CRED_CURSO NUMERIC(4) NOT NULL,
        COD_DPTO_FK NUMERIC(4) NOT NULL,
        DURACAO_NORMAL VARCHAR(100) NOT NULL,
        PRIMARY KEY (COD_CURSO));

    CREATE TABLE TURMA (
        ANO NUMERIC(4) NOT NULL,
        PERIODO NUMERIC(2) NOT NULL,
        SALA VARCHAR(10) NOT NULL,
        ID_PROF_FK NUMERIC(10) NOT NULL,
        MAT_ALU_FK NUMERIC(10) NOT NULL,
        COD_DISC_FK NUMERIC(4) NOT NULL);

    CREATE TABLE DEPARTAMENTO (
        COD_DPTO NUMERIC(4) NOT NULL,
        NOME_DPTO VARCHAR(255) NOT NULL,
        PRIMARY KEY (COD_DPTO));

    ALTER TABLE CURSO ADD CONSTRAINT DPTO_FK FOREIGN KEY (COD_DPTO_FK) REFERENCES DEPARTAMENTO (COD_DPTO);
    ALTER TABLE TURMA ADD CONSTRAINT PROF_FK FOREIGN KEY (ID_PROF_FK) REFERENCES PROFESSOR (MAT_PROF);
    ALTER TABLE DISCIPLINA ADD CONSTRAINT CURSO_FK FOREIGN KEY (ID_CURSO_FK) REFERENCES CURSO (COD_CURSO);
    ALTER TABLE TURMA ADD CONSTRAINT ALU_FK FOREIGN KEY (MAT_ALU_FK) REFERENCES ALUNO (MAT_ALU);
    ALTER TABLE PROFESSOR ADD CONSTRAINT DPTO2_FK FOREIGN KEY (COD_DPTO_FK) REFERENCES DEPARTAMENTO (COD_DPTO);
    ALTER TABLE ALUNO ADD CONSTRAINT CURSO2_FK FOREIGN KEY (COD_CURSO_FK) REFERENCES CURSO (COD_CURSO);
    ALTER TABLE TURMA ADD CONSTRAINT DISC_FK FOREIGN KEY (COD_DISC_FK) REFERENCES DISCIPLINA (COD_DISC);

    INSERT INTO DEPARTAMENTO (COD_DPTO, NOME_DPTO) VALUES (1, 'Computação');

    INSERT INTO PROFESSOR (MAT_PROF, NOME_PROF, TITULO, ENDERECO, COD_DPTO_FK) VALUES (1, 'A', 'A', 'A', 1);
    INSERT INTO PROFESSOR (MAT_PROF, NOME_PROF, TITULO, ENDERECO, COD_DPTO_FK) VALUES (2, 'B', 'A', 'B', 1);
    INSERT INTO PROFESSOR (MAT_PROF, NOME_PROF, TITULO, ENDERECO, COD_DPTO_FK) VALUES (3, 'C', 'A', 'C', 1);

    INSERT INTO CURSO (COD_CURSO, DESC_CURSO, NUM_CRED_CURSO, COD_DPTO_FK, DURACAO_NORMAL) VALUES (1, 'AAAA', 10, 1, '0h');
    INSERT INTO CURSO (COD_CURSO, DESC_CURSO, NUM_CRED_CURSO, COD_DPTO_FK, DURACAO_NORMAL) VALUES (2, 'BBBB', 11, 1, '0h');
    INSERT INTO CURSO (COD_CURSO, DESC_CURSO, NUM_CRED_CURSO, COD_DPTO_FK, DURACAO_NORMAL) VALUES (3, 'CCCC', 23, 1, '0h');

    INSERT INTO DISCIPLINA (COD_DISC, NOME_DISC, NUM_CRED_DISC, NATUREZA, ID_CURSO_FK) VALUES (1, 'DDDD', 10, 'T', 1);

    INSERT INTO ALUNO (MAT_ALU, NOME_ALU, ESTADO_CIVIL, SEXO, ANO_INGRESSO, NOTA, COD_CURSO_FK) VALUES (1, 'A', 'A', 'M', CURRENT_DATE, 8, 1);
    INSERT INTO ALUNO (MAT_ALU, NOME_ALU, ESTADO_CIVIL, SEXO, ANO_INGRESSO, NOTA, COD_CURSO_FK) VALUES (2, 'B', 'A', 'F', CURRENT_DATE, 5, 1);
    INSERT INTO ALUNO (MAT_ALU, NOME_ALU, ESTADO_CIVIL, SEXO, ANO_INGRESSO, NOTA, COD_CURSO_FK) VALUES (3, 'B', 'A', 'M', CURRENT_DATE, 2, 1);

    INSERT INTO TURMA (ANO, PERIODO, SALA, ID_PROF_FK, MAT_ALU_FK, COD_DISC_FK) VALUES (2021, 1, 'B23', 1, 1, 1);
    INSERT INTO TURMA (ANO, PERIODO, SALA, ID_PROF_FK, MAT_ALU_FK, COD_DISC_FK) VALUES (2021, 1, 'B25', 3, 2, 1);
    INSERT INTO TURMA (ANO, PERIODO, SALA, ID_PROF_FK, MAT_ALU_FK, COD_DISC_FK) VALUES (2021, 1, 'A23', 1, 3, 1);
EOSQL
