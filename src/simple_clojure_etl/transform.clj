(ns simple-clojure-etl.transform
  (:require [next.jdbc.types :refer [as-date as-numeric as-varchar]]
            [taoensso.timbre :refer [report spy]]))

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
