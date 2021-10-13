#!/usr/bin/bash
set -euo pipefail

# Por SQL de inicializacao de script aqui
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    create table dm_departamentos
    (
        sg_cod_dpto numeric not null
            constraint dm_departamentos_pk
                primary key,
        nome_dpto   varchar(50)
    );

    create table dm_cursos
    (
        sg_cod_curso   numeric not null
            constraint dm_cursos_pk
                primary key,
        nome_curso     varchar(90),
        sg_cod_dpto_fk integer
            constraint cursos_departamentos__fk
                references dm_departamentos
    );

    create unique index dm_cursos_sg_cod_curso_uindex
        on dm_cursos (sg_cod_curso);

    create unique index dm_departamentos_sg_cod_dpto_uindex
        on dm_departamentos (sg_cod_dpto);

    create table dm_tempo
    (
        ano                   numeric not null,
        semestre              numeric not null,
        semestre_desde_inicio numeric not null,
        constraint dm_tempo_pkey
            primary key (ano, semestre, semestre_desde_inicio)
    );

    create table dm_alunos
    (
        sg_mat_aluno numeric(10) not null
            constraint dm_alunos_pk
                primary key,
        nome         varchar(90),
        cotista      char,
        faltas       numeric
    );

    create table dm_disciplinas
    (
        sg_cod_disciplina numeric not null
            constraint dm_disciplinas_pk
                primary key,
        nome_disc         varchar(60),
        nota              numeric(5, 2),
        status            char,
        sg_cod_curso_fk   numeric
            constraint disciplinas_cursos_fk
                references dm_cursos
    );

    create table ft_matriculados
    (
        mat_aluno_fk          numeric(10) not null
            constraint matriculados_dm_alunos_aluno_fk
                references dm_alunos,
        ano                   numeric     not null,
        semestre              numeric     not null,
        semestre_desde_inicio numeric     not null,
        cod_disciplina_fk     numeric     not null
            constraint matriculados_disciplinas_fk
                references dm_disciplinas,
        constraint ft_matriculados_pk
            primary key (mat_aluno_fk, cod_disciplina_fk, ano, semestre, semestre_desde_inicio),
        constraint matriculados_tempo_fk
            foreign key (ano, semestre, semestre_desde_inicio) references dm_tempo
    );

    create table ft_reprovados
    (
        mat_aluno_fk          numeric(10) not null
            constraint reprovados_dm_alunos_aluno_fk
                references dm_alunos,
        ano                   numeric     not null,
        semestre              numeric     not null,
        semestre_desde_inicio numeric     not null,
        cod_disciplina_fk     numeric     not null
            constraint reprovados_disciplinas_fk
                references dm_disciplinas,
        constraint ft_reprovados_pk
            primary key (mat_aluno_fk, cod_disciplina_fk, ano, semestre, semestre_desde_inicio),
        constraint reprovados_tempo_fk
            foreign key (ano, semestre, semestre_desde_inicio) references dm_tempo
    );

EOSQL
