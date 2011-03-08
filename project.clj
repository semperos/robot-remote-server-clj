(defproject robot-remote-server "0.1.0-SNAPSHOT"
  :description "Implementation of a RobotFramework remote server in Clojure"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [ring/ring-jetty-adapter "0.3.6"]
                 [necessary-evil "1.0.0-SNAPSHOT"]]
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"]
                     [marginalia "0.5.0"]]
  :main robot-remote-server.keyword)
