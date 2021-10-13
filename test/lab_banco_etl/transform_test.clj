(ns lab-banco-etl.transform-test
  (:require [clojure.instant :refer [read-instant-date]]
            [clojure.test :refer :all]
            [simple-clojure-etl.transform :as sut]))

(deftest pipeline
  (testing "Function recieves a vector and executes them against the input-data"
    (is (=
         (let [functions  [(fn [_ _ travel-log] (conj  travel-log "1st function."))
                           (fn [_ _ travel-log] (conj  travel-log "2nd function."))
                           (fn [_ _ travel-log] (conj  travel-log "Arrived at 3rd function."))]]

           (sut/pipeline 'tx {:self-reference :sample-ref} functions ["Started travelling"]))
         ["Started travelling"
          "1st function."
          "2nd function."
          "Arrived at 3rd function."])))
  (testing "Pipeline stops when a function returns nil"
    (let [functions [(fn [_ _ v] (when (:shall-continue v) v))
                     (fn [_ _ v] (merge v {:finished true}))]]
      (is (=
           nil
           (sut/pipeline 'tx {:self-reference :sample-ref}
                         functions
                         {:shall-continue false})))
      (is (=
           {:finished true
            :shall-continue true}
           (sut/pipeline 'tx {:self-reference :sample-ref}
                         functions
                         {:shall-continue true}))))))

(deftest rm-temp-cols
  (testing "Filters out keys starting with  \"temp_\""
    (is (=
         (sut/rm-temp-cols 'tx {} {:temp_1 1 :temp_2 2 , :temp_3 3})
         nil))
    (is (=
         (sut/rm-temp-cols 'tx {} {:temp_1 1 :temp_2 2 , :not_temp 3})
         {:not_temp 3}))))

;; Implementation specific code below
(deftest descobre-ano-semestre
  (testing "Year count in first semester"
    (let [sql-date (->
                    (read-instant-date "2021-10-08T03:00:00.000-00:00")
                    .getTime
                    java.sql.Date.)]
      (is (=
           {:temp_semestre_desde_inicio 4M,
            :dat_inicio_sistema sql-date
            :semestre_desde_inicio 4M,
            :semestre 1,
            :ano 2023}
           (sut/descobre-ano-semestre
            'tx {} {:temp_semestre_desde_inicio 4M
                    :dat_inicio_sistema  sql-date})))))
  (testing "Year count in second semester"
    (let [sql-date (->
                    (read-instant-date "2021-10-08T03:00:00.000-00:00")
                    .getTime
                    java.sql.Date.)]
      (is (=
           {:temp_semestre_desde_inicio 1M,
            :dat_inicio_sistema sql-date
            :semestre_desde_inicio 1M,
            :semestre 2,
            :ano 2021}
           (sut/descobre-ano-semestre
            'tx {} {:temp_semestre_desde_inicio 1M
                    :dat_inicio_sistema  sql-date}))))))

(deftest filtra-aprovado
  (testing "Non-passing student is returned"
    (is (=
         {:temp_nota 4 :temp_faltas 30 :temp_carga_horaria 100}
         (sut/filtra-aprovado
          'tx {} {:temp_nota 4 :temp_faltas 30 :temp_carga_horaria 100}))))
  (testing "Passing student is not returned"
    (is (=
         nil
         (sut/filtra-aprovado
          'tx {} {:temp_nota 8 :temp_faltas 30 :temp_carga_horaria 100})))))
