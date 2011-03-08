(ns robot-remote-server.core
  (:require [necessary-evil.core :as xml-rpc]
            [clojure.string :as str])
  (:import org.mortbay.jetty.Server)
  (:use [robot-remote-server keyword]
        ring.adapter.jetty))

(def *result* (atom {:status "PASS", :return "", :output "", :error "", :traceback ""}))
(def *server* (atom nil))

(defn find-kw-fn
  [this-ns fn-name]
  (ns-resolve this-ns (symbol fn-name)))

(defn clojurify-name
  "Make it nicer for Clojure developers to write keywords; replace underscores with dashes"
  [s]
  (str/replace s "_" "-"))

(defn wrap-rpc
  "Ring middleware to limit server's response to the particular path that RobotFramework petitions"
  [handler]
  (fn [req]
    (if (= "/RPC2" (:uri req))
      (handler req)
      nil)))

;; WARNING: Less-than-functional code follows

(defmacro init-handler
  "Create handler for XML-RPC server. Justification: delayed evaluation of *ns*"
  []
  (let [this-ns *ns*]
    `(->
      (xml-rpc/end-point
          {:get_keyword_names (fn []
                                (vec
                                 (map #(str/replace % "-" "_")
                                      (remove #(re-find #"(\*|!)" %)
                                              (map str
                                                   (map first (ns-publics ~this-ns)))))))
           :run_keyword (fn
                          [kw-name# args#]
                          (let [clj-kw-name# (clojurify-name kw-name#)
                                a-fn# (find-kw-fn ~this-ns clj-kw-name#)
                                output# (with-out-str (try
                                                        (apply a-fn# args#)
                                                        (catch Exception e#
                                                          (do
                                                            (reset! *result* {:status "FAIL", :return "", :output "",
                                                                              :error (with-out-str (prn e#)), :traceback (with-out-str (.printStackTrace e#))})
                                                            @*result*))))]
                            (swap! *result* assoc :output output# :return output#)
                            @*result*))
           :get_keyword_arguments (fn
                                    [kw-name#]
                                    (let [clj-kw-name# (clojurify-name kw-name#)
                                          a-fn# (find-kw-fn ~this-ns clj-kw-name#)]
                                      (vec (map str (last (:arglists (meta a-fn#)))))))
           :get_keyword_documentation (fn
                                        [kw-name#]
                                        (let [clj-kw-name# (clojurify-name kw-name#)
                                              a-fn# (find-kw-fn ~this-ns clj-kw-name#)]
                                          (:doc (meta a-fn#))))
           :stop_remote_server (fn []
                                 (.stop @*server*))})
         wrap-rpc)))

(defn server-start!
  ([hndlr] (server-start! hndlr {:port 8270, :join? false}))
  ([hndlr opts] (reset! *server* (run-jetty hndlr opts))))

(defn server-stop!
  []
  (.stop @*server*))

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

  (defonce a-server (run-jetty #'app-handler {:port 8271 :join? false}))
  (doto (Thread. #(run-jetty #'app-handler {:port 8271})) .start)
)