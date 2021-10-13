(ns simple-clojure-etl.core
  (:gen-class)
  (:require [next.jdbc :as jdbc]
            [next.jdbc.optional :as opt]
            [next.jdbc.sql :as sql]
            [simple-clojure-etl.db :refer [src target]]
            simple-clojure-etl.log
            [simple-clojure-etl.transform :as t]
            [taoensso.timbre :refer [merge-config! report set-level!]]))

;; Para utilizar, basta adicionar uma nova entrada ao hash-map
;; :src o select que será utilizado para extrair e (podendo tambem) conter
;; transformação de dados.
;; :sg-nk-keys A chave primaria utilizada para dar insert/update
;; na tabela de destino,
;; :transform-fns funções callback para entrar na pipeline de
;; transformação

(def queries
  (->>
   {:dm_departamentos
    {:src                       ["select d.nome_dpto, d.cod_dpto as sg_cod_dpto from departamentos d"]
     :sg-nk-keys                 ["sg_cod_dpto"]
     :transform-fns []}
    :dm_cursos
    {:src                       ["select cod_curso as sg_cod_curso, nom_curso as nome_curso, cod_dpto as sg_cod_dpto_fk from cursos"]
     :sg-nk-keys                 ["sg_cod_curso" "sg_cod_dpto_fk"]
     :transform-fns []}
    :dm_alunos
    {:src                       ["select mat_alu as sg_mat_aluno, nome, cotista, m.faltas from alunos
                                    join matriculas m using(mat_alu)"]
     :sg-nk-keys                 ["sg_mat_aluno"]
     :transform-fns []}
    :dm_tempo
    {:src                        ["select m.semestre as temp_semestre_desde_inicio, (select * from
                                        (select DAT_ENTRADA from ALUNOS order by DAT_ENTRADA asc) as temp
                                    LIMIT 1) as temp_dat_inicio_sistema
                                        from matriculas m"]
     :sg-nk-keys                  ["ano" "semestre"]
     :transform-fns [t/descobre-ano-semestre t/rm-temp-cols]}
    :dm_disciplinas
    {:src                       ["select cod_disc as sg_cod_disciplina, nome_disc, nota, status,
                                        c.cod_curso as sg_cod_curso_fk
                                    from disciplinas d
                                    join matriculas m using(cod_disc)
                                    join matrizes_cursos mc using(cod_disc)
                                    join cursos c on c.cod_curso = mc.cod_curso"]
     :sg-nk-keys                 ["sg_cod_disciplina"]
     :transform-fns []}
    :ft_matriculados
    {:src                       ["select a.mat_alu as mat_aluno_fk, m.cod_disc cod_disciplina_fk, m.faltas as temp_faltas, m.semestre as temp_semestre_desde_inicio, m.nota as temp_nota, d.carga_horaria as temp_carga_horaria
,(select * from
                                        (select DAT_ENTRADA from ALUNOS order by DAT_ENTRADA asc) as temp
                                    LIMIT 1) as temp_dat_inicio_sistema
                                    from matriculas m join alunos a using(mat_alu)
                                    join disciplinas d using(cod_disc)"]
     :sg-nk-keys                 ["mat_aluno_fk" "cod_disciplina_fk" "semestre" "ano"]
     :transform-fns [t/descobre-ano-semestre t/rm-temp-cols]}
    :ft_reprovados
    {:src                       ["select a.mat_alu as mat_aluno_fk, m.cod_disc cod_disciplina_fk, m.faltas as temp_faltas, m.semestre as temp_semestre_desde_inicio, m.nota as temp_nota, d.carga_horaria as temp_carga_horaria
,(select * from
                                        (select DAT_ENTRADA from ALUNOS order by DAT_ENTRADA asc) as temp
                                    LIMIT 1) as temp_dat_inicio_sistema
                                    from matriculas m join alunos a using(mat_alu)
                                    join disciplinas d using(cod_disc)"]
     :sg-nk-keys                 ["mat_aluno_fk" "cod_disciplina_fk" "semestre" "ano"]
     :transform-fns [t/descobre-ano-semestre t/filtra-aprovado t/rm-temp-cols]}}
   (into {} (map (fn [[k v]] [k (assoc v :self-reference k)])))))

(defn insert-or-update!
  "processed-data: An unqualified hash-map from an execute-one! "
  [tx dm-hash-map processed-data]
  (if processed-data
    (let [key-map (into {}
                        (map (fn [k]
                               [(keyword k) ((keyword k) processed-data)]) (:sg-nk-keys dm-hash-map)))]
      (if (empty? (sql/find-by-keys tx (:self-reference dm-hash-map) key-map))
        (sql/insert! tx (:self-reference dm-hash-map) processed-data)
        (sql/update! tx (:self-reference dm-hash-map) processed-data key-map)))
    (report (format "Pipeline for table %s was halted."
                    (name (:self-reference dm-hash-map))))))

(defn insert-dm [dm-hash-map]
  (report (format "Executing ETL for target table: %s" (:self-reference dm-hash-map)))
  (jdbc/with-transaction [tx (jdbc/get-connection target)]
    (report "Rows inserted/updated: "
            (clojure.core.reducers/fold
             1
             + (fn [qtd row]
                 (if (->> row
                          (t/pipeline tx dm-hash-map (:transform-fns dm-hash-map))
                          (insert-or-update! tx dm-hash-map))
                   (inc qtd)
                   qtd))
             (jdbc/plan src (:src dm-hash-map) {:builder-fn opt/as-unqualified-maps})))))

(defn -main
  "Args: A space separated list of configured tables to run the script on"
  [& args]
  (do
    (set-level! :info)
    (merge-config! {:appenders {:println {:enabled? true}}})
    (if (seq args)
      (run! insert-dm (vals (select-keys queries (map keyword args))))
      (run! insert-dm (vals queries))))
  (shutdown-agents))

(comment
  (run! insert-dm (select-keys queries [:dm_alunos :dm_cursos]))

  (insert-dm (:dm_departamentos queries))

  (insert-dm (:dm_cursos queries))

  (insert-dm (:dm_disciplinas queries))

  (insert-dm (:dm_alunos queries))

  (insert-dm (:dm_tempo queries))

  (insert-dm (:ft_reprovados queries))

  (insert-dm (:ft_matriculados queries))

  (-main)

;;
  )
