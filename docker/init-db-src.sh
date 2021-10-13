#!/usr/bin/bash
set -euo pipefail

# Por SQL de inicializacao de script aqui
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE TABLE alunos (
        mat_alu       NUMERIC(10) NOT NULL,
        nome          VARCHAR(100) NOT NULL,
        dat_entrada   DATE NOT NULL,
        cod_curso     NUMERIC(3) NOT NULL,
        cotista       CHAR(1) NOT NULL
    );

    ALTER TABLE alunos ADD CONSTRAINT alu_fk PRIMARY KEY ( mat_alu );

    CREATE TABLE cursos (
        cod_curso   NUMERIC(4) NOT NULL,
        nom_curso   VARCHAR(80) NOT NULL,
        cod_dpto    NUMERIC(3) NOT NULL
    );

    ALTER TABLE cursos ADD CONSTRAINT cur_pk PRIMARY KEY ( cod_curso );

    CREATE TABLE departamentos (
        cod_dpto    NUMERIC(3) NOT NULL,
        nome_dpto   VARCHAR(50) NOT NULL
    );

    ALTER TABLE departamentos ADD CONSTRAINT departamentos_pk PRIMARY KEY ( cod_dpto );

    CREATE TABLE disciplinas (
        cod_disc        NUMERIC(5) NOT NULL,
        nome_disc       VARCHAR(60) NOT NULL,
        carga_horaria   NUMERIC(5, 2) NOT NULL
    );

    ALTER TABLE disciplinas ADD CONSTRAINT disc_pk PRIMARY KEY ( cod_disc );

    CREATE TABLE matriculas (
        semestre   NUMERIC(6) NOT NULL,
        mat_alu    NUMERIC(10) NOT NULL,
        cod_disc   NUMERIC(5) NOT NULL,
        nota       NUMERIC(5, 2),
        faltas     NUMERIC(3),
        status     CHAR(1)
    );

    ALTER TABLE matriculas ADD CONSTRAINT mat_pk PRIMARY KEY ( mat_alu,
                                                            semestre );

    CREATE TABLE matrizes_cursos (
        cod_curso   NUMERIC(4) NOT NULL,
        cod_disc    NUMERIC(5) NOT NULL,
        periodo     NUMERIC(2) NOT NULL
    );

    ALTER TABLE matrizes_cursos ADD CONSTRAINT mcu_pk PRIMARY KEY ( cod_curso,
                                                                    cod_disc );

    ALTER TABLE alunos
        ADD CONSTRAINT alu_cur_fk FOREIGN KEY ( cod_curso )
            REFERENCES cursos ( cod_curso );

    ALTER TABLE cursos
        ADD CONSTRAINT cur_der_fk FOREIGN KEY ( cod_dpto )
            REFERENCES departamentos ( cod_dpto );

    ALTER TABLE matriculas
        ADD CONSTRAINT mat_alu_fk FOREIGN KEY ( mat_alu )
            REFERENCES alunos ( mat_alu );

    ALTER TABLE matriculas
        ADD CONSTRAINT mat_dis_fk FOREIGN KEY ( cod_disc )
            REFERENCES disciplinas ( cod_disc );

    ALTER TABLE matrizes_cursos
        ADD CONSTRAINT mcu_cur_fk FOREIGN KEY ( cod_curso )
            REFERENCES cursos ( cod_curso );

    ALTER TABLE matrizes_cursos
        ADD CONSTRAINT mcu_dis_fk FOREIGN KEY ( cod_disc )
            REFERENCES disciplinas ( cod_disc );

    -- Inserts
    insert into departamentos (cod_dpto, nome_dpto)
    values (1, 'Anti-emprego');
    insert into cursos (COD_CURSO, NOM_CURSO, COD_DPTO)
    VALUES (1, 'Ciência Anti-mercadológicas', 1);
    INSERT INTO ALUNOS (MAT_ALU, NOME, DAT_ENTRADA, COD_CURSO, COTISTA)
    VALUES (1, 'Josias Pacatau', CURRENT_DATE, 1, 'S');
    Insert Into disciplinas (COD_DISC, NOME_DISC, CARGA_HORARIA)
    values (1, 'Intervencionismo 1', 200);
    Insert Into disciplinas (COD_DISC, NOME_DISC, CARGA_HORARIA)
    values (2, 'Intervencionismo 2', 200);
    insert into matrizes_cursos (cod_curso, cod_disc, periodo)
    values (1, 1, 1);
    insert into matrizes_cursos (cod_curso, cod_disc, periodo)
    values (1, 2, 2);
    insert into matriculas (semestre, mat_alu, cod_disc, nota, faltas, status)
    VALUES (1, 1, 1, 9.85, 5, 'A');
    insert into matriculas (semestre, mat_alu, cod_disc, nota, faltas, status)
    VALUES (2, 1, 2, 6.85, 5, 'R');
EOSQL
