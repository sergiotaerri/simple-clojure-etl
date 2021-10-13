(ns simple-clojure-etl.transform
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.types :refer [as-date as-varchar as-numeric]]
   [taoensso.timbre :refer [spy report]])
  (:import (java.time LocalDate)))

(defn type-processor [_ _ data]
  (into {}
        (map (fn [[k v]]
               (cond
                 (= java.lang.String (type v))          [k (as-varchar v)]
                 (= java.sql.Date (type v))             [k (as-date v)]
                 (or (= java.lang.Long (type v))
                     (= java.math.BigDecimal (type v))) [k (as-numeric v)]))
             data)))

(defn pipeline
  "Recieves a transaction, table-hash-map, a vector of functions and data to be processed"
  [tx dm-hash-map functions input-data]
  (do
    (report (format "Pipeline for table  %s row started with data:"
                    (name (:self-reference dm-hash-map))) input-data)
    (reduce (fn [v f]
              (if (nil? v) (reduced v)
                  (spy :info (str f)
                       (f tx dm-hash-map v)))) input-data functions)))

(defn rm-temp-cols
  "Filters out keys starting with  \"temp_\""
  [_ _ processed-data]
  (into {}
        (filter (fn [[k v]] (not (re-find #"^temp_(\S+)$" (name k)))) processed-data)))

;; Implementation specific code below
(defn semester-day-1
  "Returns first day of semester"
  [date]
  (let [local-date (->
                    date
                    .toString
                    LocalDate/parse
                    (.withDayOfMonth 1))]
    (if (< (.getMonthValue local-date) 6)
      (.withMonth local-date 7) (.withMonth local-date 1))))

(defn descobre-ano-semestre [_ _ {:keys [temp_dat_inicio_sistema temp_semestre_desde_inicio _desde_inicio] :as processed-data}]
  (let [primeiro-dia-semestre (semester-day-1 temp_dat_inicio_sistema)
        ano-da-matricula (+ (.getYear primeiro-dia-semestre)
                            (Math/floor (/ temp_semestre_desde_inicio 2)))
        semestre-da-matricula (-> primeiro-dia-semestre
                                  (.plusMonths (* 6 temp_semestre_desde_inicio))
                                  .getMonthValue
                                  ((fn [m] (if  (< m 7)
                                             1 2))))]
    (assoc processed-data
           :semestre_desde_inicio temp_semestre_desde_inicio
           :semestre semestre-da-matricula
           :ano (int ano-da-matricula))))

(defn filtra-aprovado [_ _ {:keys [temp_nota temp_faltas temp_carga_horaria]
                            :as processed-data}]
  (if (and (>= temp_nota 7)
           (<= (/ (* temp_faltas 100) temp_carga_horaria) 25))
    nil
    processed-data))
