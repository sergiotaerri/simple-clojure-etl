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
   {:dm_departamento
    {:src                       ["select d.cod_dpto, d.nome_dpto from departamento d"]
     :sg-nk-keys                 ["cod_dpto"]
     :transform-fns []}
    :dm_professor
    {:src                       ["select p.mat_prof, p.nome_prof, p.titulo,
                                    p.endereco, p.cod_dpto_fk from professor p "]
     :sg-nk-keys                 ["mat_prof"]
     :transform-fns []}
    :dm_curso
    {:src                       ["select cod_curso as cod_dm_curso, desc_curso as desc_dm_curso,
                                    num_cred_curso as num_cred_dm_curso, cod_dpto_fk, duracao_normal from curso"]
     :sg-nk-keys                 ["cod_dm_curso"]
     :transform-fns []}
    :ft_prod_professor
    {:src                       ["select (select count(mat_alu) /
                                        (select count(1) from (select distinct EXTRACT(YEAR from ano_ingresso)::bigint as temp_ano,
                                        (CASE WHEN EXTRACT(QUARTER from ano_ingresso)::bigint <= 2 THEN 1
                                                WHEN EXTRACT(QUARTER from ano_ingresso)::bigint > 2 THEN 2
                                        END) as temp_semestre from aluno
                                    join turma t2 on aluno.mat_alu = t2.mat_alu_fk
                                    join professor p2 on t2.id_prof_fk = p2.mat_prof
                                            ) as periodos)
                                            as qtd_periodos
                                        from aluno) as alunos_por_periodo,
                                        ((select count(1) from aluno where nota > 7) / (select count(1) from aluno))
                                            as percent_alunos_aprovados, mat_prof from professor "]
     :sg-nk-keys                 ["mat_prof"]
     :transform-fns []}}
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

  (insert-dm (:dm_departamento queries))
  (insert-dm (:dm_curso queries))
  (insert-dm (:dm_professor queries))

  (insert-dm (:ft_prod_professor queries))

  (-main)

;;
  )
