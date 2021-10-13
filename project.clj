(defproject simple-clojure-etl "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[com.github.seancorfield/next.jdbc "1.2.724"]
                 [com.taoensso/timbre "5.1.2"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/core.async "1.3.618"]
                 [org.postgresql/postgresql "42.2.24.jre7"]]
  :main ^:skip-aot simple-clojure-etl.core
  :target-path "target/%s"
  :resource-paths ["resources"]
  :profiles {:uberjar {:aot [simple-clojure-etl.core]
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
