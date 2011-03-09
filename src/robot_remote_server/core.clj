;;; ## RobotFramework XML-RPC Remote Server in Clojure
;;;
;;; This XML-RPC server is designed to be used with RobotFramework (RF), to
;;; allow developers to write RF keywords in Clojure.
;;;
;;; If you use Leiningen, just run `lein run` to use the example keyword
;;; library included in the `robot-remote-server.keyword` namespace.
;;;
;;; Otherwise, `(:use)` the `robot-remote-server.core` namespace in your own
;;; namespace containing RF keywords and add `(server-start! (init-handler))`
;;; to start the remote server.
;;;
;;; You can pass a map of options to `(server-start!)` like you would to
;;; `(run-jetty)`. To stop the server, use `(server-stop!)` or send
;;; `:stop_remote_server` via RPC.
;;;
;;; Because RF sends requests to the /RPC2 path, that has been enforced for this
;;; server using the `wrap-rpc` middleware defined in this namespace.
;;; 
(ns robot-remote-server.core
  (:require [necessary-evil.core :as xml-rpc]
            [clojure.string :as str])
  (:import org.mortbay.jetty.Server)
  (:use [robot-remote-server keyword]
        ring.adapter.jetty))

(def *robot-remote-server* (atom nil))

(defn find-kw-fn
  "Given a namespace and a fn-name as string, return the function in that namespace by that name"
  [a-ns fn-name]
  (ns-resolve a-ns (symbol fn-name)))

(defn clojurify-name
  "Make it nicer for Clojure developers to write keywords; replace underscores with dashes"
  [s]
  (str/replace s "_" "-"))

(defn wrap-rpc
  "Ring middleware to limit server's response to the particular path that RobotFramework petitions"
  [handler]
  (fn [req]
    (when (= "/RPC2" (:uri req))
      (handler req))))

(defn get-keyword-arguments*
  "Get arguments for a given RF keyword function identified by the string `kw-name` and located in the `a-ns` namespace"
  [a-ns kw-name]
  (let [clj-kw-name (clojurify-name kw-name)
        a-fn (find-kw-fn a-ns clj-kw-name)]
    (vec (map str (last (:arglists (meta a-fn)))))))

(defn get-keyword-documentation*
  "Get documentation string for a given RF keyword function identified by the string `kw-name` and located in the `a-ns` namespace"
  [a-ns kw-name]
  (let [clj-kw-name (clojurify-name kw-name)
        a-fn (find-kw-fn a-ns clj-kw-name)]
    (:doc (meta a-fn))))

(defn get-keyword-names*
  "Get a list of RF keyword functions located in the `a-ns` namespace"
  [a-ns]
  (vec
   (map #(str/replace % "-" "_") ; RF expects underscores
        (remove #(or (re-find #"(\*|!)" %) (re-find #"^-" %)) ; non-keyword functions
                (map str
                     (map first (ns-publics a-ns)))))))
(defn run-keyword*
  "Given a RF-formatted string representation of a Clojure function `kw-name` in the `a-ns` namespace called with `args` as a vector, evaluate the function"
  [a-ns kw-name args]
  (let [result {:status "PASS", ; RF expects this map
                :return "",
                :output "",
                :error "",
                :traceback ""}
        clj-kw-name (clojurify-name kw-name) ; translate RF keyword to Clojure fn
        a-fn (find-kw-fn a-ns clj-kw-name)
        output (with-out-str (try
                                (apply a-fn args)
                                (catch Exception e
                                  (assoc result
                                    :status "FAIL"
                                    :error (with-out-str (prn e))
                                    :traceback (with-out-str (.printStackTrace e))))))]
    (assoc result :output output :return output)))

;; WARNING: Less-than-functional code follows

(defmacro init-handler
  "Create handler for XML-RPC server. Set expose-stop to false to prevent exposing the `stop_remote_server` RPC command. Justification for using macro: delayed evaluation of *ns*"
  [expose-stop]
  (let [this-ns *ns*]
    (if (true? expose-stop)
      `(->
        (xml-rpc/end-point
         {:get_keyword_arguments       (fn [kw-name#]
                                         (get-keyword-arguments* ~this-ns kw-name#))
          :get_keyword_documentation   (fn [kw-name#]
                                         (get-keyword-documentation* ~this-ns kw-name#))
          :get_keyword_names           (fn []
                                         (get-keyword-names* ~this-ns))
          :run_keyword                 (fn [kw-name# args#]
                                         (run-keyword* ~this-ns kw-name# args#))
          :stop_remote_server          (fn []
                                         (.stop @*robot-remote-server*))})
        wrap-rpc)
      `(->
        (xml-rpc/end-point
         {:get_keyword_arguments       (fn [kw-name#]
                                         (get-keyword-arguments* ~this-ns kw-name#))
          :get_keyword_documentation   (fn [kw-name#]
                                         (get-keyword-documentation* ~this-ns kw-name#))
          :get_keyword_names           (fn []
                                         (get-keyword-names* ~this-ns))
          :run_keyword                 (fn [kw-name# args#]
                                         (run-keyword* ~this-ns kw-name# args#))})
        wrap-rpc))))

(defn server-start!
  "Given a Ring handler `hndlr`, start a Jetty server"
  ([hndlr] (server-start! hndlr {:port 8270, :join? false}))
  ([hndlr opts] (reset! *robot-remote-server* (run-jetty hndlr opts))))

(defn server-stop!
  "Stop the global Jetty server instance"
  []
  (.stop @*robot-remote-server*))