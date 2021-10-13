(ns simple-clojure-etl.db
  (:require [next.jdbc :as jdbc]))

(def db-spec-source {:dbtype   "postgresql"
                     :dbname   "lab_banco"
                     :port     5433
                     :user     "root"
                     :password "postgres"})

(def db-spec-target {:dbtype   "postgresql"
                     :dbname   "lab_banco"
                     :port     5434
                     :user     "root"
                     :password "postgres"})

(def src (jdbc/get-datasource db-spec-source))
(def target (jdbc/get-datasource db-spec-target))

(comment
  ;; Versão com spec da oracle
;; TENTATIVA COM ORACLE QUE NÂO DEU CERTO, TAMBEM POSSUI LOGICA DIFERENCIADA
;;
;;
  (def db-spec-source "jdbc:oracle:thin:ACADEMICO_SRC/ACADEMICO_SRC@localhost:1521:XE")

  (def db-spec-target "jdbc:oracle:thin:ACADEMICO/ACADEMICO@localhost:1521:XE")

  (jdbc/with-transaction [tx (jdbc/get-connection src)]
    (jdbc/execute! tx ["ALTER SESSION SET CURRENT_SCHEMA = sys"])
    (jdbc/execute! tx ["select cod_curso, nom_curso from ACADEMICO_SRC.cursos"])
    (jdbc/execute! tx ["select sys_context( 'userenv', 'current_schema' ) from dual"])
    (jdbc/execute! tx ["select user from dual"])

    (jdbc/execute! tx ["select table_name from user_tables"])
    (jdbc/execute! tx ["select * from all_tables where owner='ACADEMICO_SRC'"]))
;; => []
  )
