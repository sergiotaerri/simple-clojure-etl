(ns simple-clojure-etl.log
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            [clojure.java.io :refer [resource delete-file]]
            [taoensso.encore :as enc]
            [clojure.string :as str]))

(def log-file-name "resources/main.log")

;; (delete-file log-file-name :quiet)

(defn default-fn+thread-id
  "Only difference from timbre/default-output-fn is it includes the Thread Id."
  ([data] (default-fn+thread-id nil data))
  ([opts data]                          ; For partials
   (-> (timbre/default-output-fn opts data)
       (str/split #" " 2)
       (#(vector (% 0) (format "[Thread: %s]" (.getId (Thread/currentThread))) (% 1)))
       (->> (str/join " ")))))

(timbre/merge-config!
 {:appenders {:spit (appenders/spit-appender {:fname log-file-name})}})

(timbre/merge-config!
 {:output-fn default-fn+thread-id})

(timbre/merge-config! {:appenders {:println {:enabled? false}}})

;; (timbre/merge-config! {:appenders {:spit {:enabled? false}}} ; To disable
;; (timbre/merge-config! {:appenders {:spit nil}}               ; To remove entirely
