(defproject string-diff "0.1.0-SNAPSHOT"
  :main string-diff.server
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [bidi "2.0.9"]
                 [liberator "0.14.1"] 
                 [ring "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-json "0.4.0"]]
  :profiles {:dev {:dependencies [[midje "1.8.3"]]
                   :plugins [[lein-midje "3.2"]]}})
