(ns robot-remote-server.core
  (:require [necessary-evil.core :as xml-rpc]
            [clojure.string :as str])
  (:import org.mortbay.jetty.Server)
  (:use robot-remote-server.keyword
        ring.adapter.jetty))

(def *result* (atom {:status "PASS", :return "", :output "", :error "", :traceback ""}))
(def *server* (atom nil))
(def rf-ns (atom 'robot-remote-server.keyword))

;; WARNING: Less-than-functional code follows

(defn find-kw-fn
  [ns-name fn-name]
  (ns-resolve (find-ns ns-name) (symbol fn-name)))

(defn clojurify-name
  "Make it nicer for Clojure developers to write keywords; replace underscores with dashes"
  [s]
  (str/replace s "_" "-"))

(defn- run-keyword
  "Run a single keyword"
  [kw-name args]
  (let [clj-kw-name (clojurify-name kw-name)
        a-fn (find-kw-fn @rf-ns clj-kw-name)
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
  "Get all keywords defined in rf-ns namespace, and make the names RobotFramework-friendly"
  []
  (vec (map #(str/replace % "-" "_") (map str (map first (ns-publics @rf-ns))))))

(defn- get-keyword-arguments
  [kw-name]
  (let [clj-kw-name (clojurify-name kw-name)
        a-fn (find-kw-fn @rf-ns clj-kw-name)]
    (vec (map str (last (:arglists (meta a-fn)))))))

(defn- get-keyword-documentation
  [kw-name]
  (let [clj-kw-name (clojurify-name kw-name)
        a-fn (find-kw-fn @rf-ns clj-kw-name)]
    (:doc (meta a-fn))))

(declare app-handler)
(defn server-start!
  ([] (server-start! app-handler {:port 8270, :join? false}))
  ([hndlr opts]
     (reset! *server* (run-jetty hndlr opts))))

(defn server-stop!
  []
  (.stop @*server*))

(def app-handler (xml-rpc/end-point
                  {:run_keyword run-keyword
                   :get_keyword_names get-keyword-names
                   :get_keyword_arguments get-keyword-arguments
                   :get_keyword_documentation get-keyword-documentation
                   :stop_remote_server server-stop!}))

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