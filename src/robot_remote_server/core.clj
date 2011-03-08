(ns robot-remote-server.core
  (:require [necessary-evil.core :as xml-rpc])
  (:import org.mortbay.jetty.Server)
  (:use robot-remote-server.keyword
        ring.adapter.jetty))

(def *result* (atom {:status "PASS", :return "", :output "", :error "", :traceback ""}))
(def *server* (atom (Server.)))

(defn find-kw-fn
  [ns-name fn-name]
  (ns-resolve (find-ns ns-name) (symbol fn-name)))

(defn- run-keyword
  "Run a single keyword"
  [kw-name args]
  (let [a-fn (find-kw-fn 'robot-remote-server.keyword kw-name)
        output (with-out-str (try
                               (apply a-fn args)
                               (catch Exception e
                                 (do
                                   (reset! *result* {:status "FAIL", :return "", :output "",
                                                     :error (with-out-str (prn e)), :traceback (with-out-str (.printStackTrace e))})
                                   @*result*))))]
    (swap! *result* assoc :output output :return output)
    @*result*))

(defn- get-keyword-names
  []
  (vec (map str (map first (ns-publics 'robot-remote-server.keyword)))))

(defn- get-keyword-arguments
  [kw-name]
  (let [a-fn (find-kw-fn 'robot-remote-server.keyword kw-name)]
    (vec (map str (last (:arglists (meta a-fn)))))))

(defn- get-keyword-documentation
  [kw-name]
  (let [a-fn (find-kw-fn 'robot-remote-server.keyword kw-name)]
    (:doc (meta a-fn))))

(declare app-handler)
(defn server-start
  ([] (start-server app-handler {:port 8270, :join? false}))
  ([hndlr opts]
     (reset! *server* (run-jetty hndlr opts))))

(defn server-stop
  []
  (.stop @*server*))

(def app-handler (xml-rpc/end-point
                  {:run_keyword run-keyword
                   :get_keyword_names get-keyword-names
                   :get_keyword_arguments get-keyword-arguments
                   :get_keyword_documentation get-keyword-documentation
                   :stop_remote_server stop-server}))

(comment
  
  (defn- handle-return-val
    "Convert everything to RobotFramework-acceptable types. See implementations in other languages for examples"
    [ret]
    (condp class ret
      java.lang.String                           ret
      java.util.concurrent.atomic.AtomicInteger  ret
      java.util.concurrent.atomic.AtomicLong     ret
      java.math.BigDecimal                       ret
      java.math.BigInteger                       ret
      java.lang.Byte                             ret
      java.lang.Double                           ret
      java.lang.Float                            ret
      java.lang.Integer                          ret
      java.lang.Long                             ret
      java.lang.Short                            ret
      clojure.lang.PersistentVector              (map handle-return-val ret)
      clojure.lang.PersistentArrayMap            (into {}
                                                       (for [[k v] ret]
                                                         [(.toString k) (handle-return-val v)]))
      clojure.lang.PersistentTreeMap             (into {}
                                                       (for [[k v] ret]
                                                         [(.toString k) (handle-return-val v)]))
      :else ret))

  (defonce *server* (run-jetty #'handler {:port 8271 :join? false}))
  (doto (Thread. #(run-jetty #'handler {:port 8271})) .start)
)